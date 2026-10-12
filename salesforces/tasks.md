# PD1 Sandbox Labs: 14 hands-on tasks

Each lab has **Do → Expect → Lesson**. Use a **Developer sandbox** or Developer Edition org, never production. Total time is roughly 4-5 hours, so pick by priority: **Labs 4, 5, 6, 9, 10, 11 first** (your weakest areas).

> I haven't run this code in an org. If something errors, read the message, because the error is often the lesson. Fix typos and keep going.

## Setup (10 minutes, once)
1. Open **Developer Console** (gear icon → Developer Console). You'll use **Debug → Open Execute Anonymous Window** (tick *Open Log*) and **File → New → Apex Class / Apex Trigger / Visualforce Page**.
2. **Setup → Debug Logs**: add a trace flag for your own user (level FINEST for Apex Code) so you can read logs.
3. For Labs 9 and 13 you need **VS Code + Salesforce Extension Pack + Salesforce CLI** (the Developer Console can't create LWC).
4. Cleanup after any lab, in anonymous Apex:
```apex
delete [SELECT Id FROM Account WHERE Name LIKE 'Lab%' OR Name LIKE 'Bulk %' OR Name LIKE 'LWC Lab%' OR Name LIKE 'Lim%' OR Name LIKE 'Mixed%' OR Name LIKE 'Good%' OR Name = 'Order Lab' OR Name = 'DML first'];
```

---

## Lab 1: Relationships (lookup vs master-detail)
**Do**
1. Setup → Object Manager → Create → Custom Object `Invoice` (API `Invoice__c`).
2. Add field: **Lookup** to Account (`Account__c`). Add a number field `Amount__c`.
3. On Account → Fields, try to add a **Roll-Up Summary**. Note that you can't (no master-detail).
4. Delete `Account__c` and recreate it as **Master-Detail** to Account (you may need to delete the invoices first, or convert the existing lookup if all invoices have an account). Now add a Roll-Up Summary `Total_Invoices__c` (SUM of `Amount__c`).
5. Create an Account, add two invoices, check the roll-up. **Delete the Account**, then check the Recycle Bin. Restore the Account and see the invoices return.
6. Create `Course__c`, `Student__c` and a junction `Enrollment__c` with **two master-detail** fields.

**Expect:** roll-up only appears with master-detail; deleting the master deletes the children; Recycle Bin restores both.

**Lesson:** Master-detail = required parent, cascade delete, inherited sharing, roll-ups, max 2 per object. A many-to-many is a junction object with two master-details.

---

## Lab 2: Security: sharing vs field-level security
**Do**
1. Create custom field `Secret__c` (Text) on Account. In the FLS step, make it visible **only to System Administrator**.
2. Setup → Sharing Settings: set Account OWD to **Public Read Only** (so the standard user can see the record).
3. Create a test class (Developer Console → File → New → Apex Class → `SecLabTest`):
```apex
@isTest
private class SecLabTest {
    @isTest static void fls() {
        insert new Account(Name = 'Lab Sec', Secret__c = 'top');
        Profile p = [SELECT Id FROM Profile WHERE Name = 'Standard User'];
        User u = new User(Alias='std1', Email='std1@example.com', EmailEncodingKey='UTF-8',
            LastName='Std', LanguageLocaleKey='en_US', LocaleSidKey='en_US', ProfileId=p.Id,
            TimeZoneSidKey='America/New_York',
            Username='std1' + System.currentTimeMillis() + '@example.com');
        insert u;
        System.runAs(u) {
            List<Account> a = [SELECT Id, Name, Secret__c FROM Account WHERE Name = 'Lab Sec'];
            System.debug('SYSTEM MODE rows=' + a.size() + ' secret=' + (a.isEmpty() ? null : a[0].Secret__c));
            try {
                List<Account> b = [SELECT Id, Secret__c FROM Account WITH USER_MODE];
                System.debug('USER_MODE worked');
            } catch (QueryException e) {
                System.debug('USER_MODE blocked: ' + e.getMessage());
            }
            SObjectAccessDecision d = Security.stripInaccessible(AccessType.READABLE, a);
            System.debug('stripInaccessible keeps Secret__c? '
                + d.getRecords()[0].getPopulatedFieldsAsMap().containsKey('Secret__c'));
        }
    }
}
```
4. Run it (Test → New Run), open the log and filter for `USER_DEBUG`.

**Expect:** the normal query returns `Secret__c` even for a user without access; `WITH USER_MODE` throws; `stripInaccessible` returns `false`.

**Lesson:** Apex runs in system mode. `with sharing` only controls **records**. To enforce **field access**, use `WITH USER_MODE`, `WITH SECURITY_ENFORCED` or `stripInaccessible`. `runAs` works only in tests.

---

## Lab 3: SOQL, SOSL and DML drills (anonymous Apex)
**Do:** first create ~5 accounts with contacts (any way you like), then run each block separately.
```apex
// 1 Parent-to-child subquery
for (Account a : [SELECT Name, (SELECT LastName FROM Contacts) FROM Account LIMIT 3])
    System.debug(a.Name + ' -> ' + a.Contacts.size());
// 2 Child-to-parent
System.debug([SELECT LastName, Account.Name FROM Contact LIMIT 3]);
// 3 Aggregate + HAVING
for (AggregateResult ar : [SELECT Industry, COUNT(Id) c FROM Account GROUP BY Industry HAVING COUNT(Id) > 0])
    System.debug(ar.get('Industry') + ' ' + ar.get('c'));
// 4 SOSL returns List<List<SObject>>
List<List<SObject>> r = [FIND 'Lab*' IN NAME FIELDS RETURNING Account(Name), Contact(LastName)];
System.debug('lists: ' + r.size());
```
```apex
// 5 Partial success
List<Account> l = new List<Account>{ new Account(Name = 'Good'), new Account() };
for (Database.SaveResult s : Database.insert(l, false)) System.debug(s.isSuccess() + ' ' + s.getErrors());
// 6 All-or-nothing
try { insert new List<Account>{ new Account(Name = 'Good2'), new Account() }; }
catch (DmlException e) { System.debug(e.getMessage()); }
System.debug('Good2 count = ' + [SELECT COUNT() FROM Account WHERE Name = 'Good2']);
// 7 QueryException
try { Account a = [SELECT Id FROM Account WHERE Name = 'NoSuchName']; }
catch (QueryException e) { System.debug(e.getTypeName()); }
// 8 Operators
Account n = null;
System.debug(n?.Name);
System.debug(n?.Name ?? 'default');
System.debug(String.isBlank('   '));
Map<String, Integer> m = new Map<String, Integer>(); System.debug(m.get('nope'));
// 9 Savepoint
Savepoint sp = Database.setSavepoint(); insert new Account(Name = 'Good3');
Database.rollback(sp); System.debug([SELECT COUNT() FROM Account WHERE Name = 'Good3']);
```
**Expect:** Block 5 saves "Good" but fails the blank one. Block 6 saves nothing (count 0). Block 7 prints `System.QueryException`. Block 9 prints 0.

**Lesson:** `Database.insert(list, false)` = partial success; plain `insert` = all-or-nothing. SOQL into a single sObject with 0 or more than 1 row throws `QueryException`. SOSL returns a list of lists. `HAVING` filters aggregates.

---

## Lab 4: Break the governor limits on purpose
**Do:** run each in anonymous Apex.
```apex
// A: 101 SOQL queries
for (Integer i = 0; i < 101; i++) { List<Account> a = [SELECT Id FROM Account LIMIT 1]; }
```
```apex
// B: 151 DMLs, caught or not?
Integer before = [SELECT COUNT() FROM Account];
try { for (Integer i = 0; i < 151; i++) insert new Account(Name = 'Lim' + i); }
catch (Exception e) { System.debug('Caught: ' + e); }
```
After B fails, run `SELECT COUNT() FROM Account WHERE Name LIKE 'Lim%'` in the Query Editor.
```apex
// C: watch the counters
System.debug(Limits.getQueries() + ' / ' + Limits.getLimitQueries());
List<Account> x = [SELECT Id FROM Account LIMIT 1];
System.debug(Limits.getQueries() + ' / ' + Limits.getLimitQueries());
System.debug(Limits.getDmlStatements() + ' / ' + Limits.getLimitDmlStatements());
```
**Expect:** A → `Too many SOQL queries: 101`. B → the `catch` doesn't run, no "Lim" accounts exist (rolled back). C shows `0 / 100` then `1 / 100`.

**Lesson:** Limits are per transaction, enforced at 101 queries / 151 DML statements. `LimitException` cannot be caught and the whole transaction rolls back. Never put SOQL or DML in loops.

---

## Lab 5: Trigger: before vs after, bulk, handler, recursion
**Do:** create the handler first, then the trigger.
```apex
public class AccountTriggerHandler {
    public static Boolean hasRun = false;
    public static void run() {
        if (Trigger.isBefore && Trigger.isInsert) {
            System.debug('BEFORE INSERT chunk=' + Trigger.new.size() + ' newMap null? ' + (Trigger.newMap == null));
            for (Account a : Trigger.new) {
                a.Description = 'TRIGGER saw: ' + a.Description;
            }
        }
        if (Trigger.isAfter && Trigger.isInsert) {
            System.debug('AFTER INSERT newMap size=' + Trigger.newMap.size());
            List<Contact> cs = new List<Contact>();
            for (Account a : Trigger.new) {
                cs.add(new Contact(LastName = 'Primary ' + a.Name, AccountId = a.Id));
            }
            insert cs;
        }
    }
}
```
```apex
trigger AccountTrigger on Account (before insert, after insert) {
    AccountTriggerHandler.run();
}
```
Experiments (anonymous Apex):
```apex
// A: bulk, 450 records
List<Account> l = new List<Account>();
for (Integer i = 0; i < 450; i++) l.add(new Account(Name = 'Bulk ' + i));
insert l;
```
- **B (read-only):** inside the after-insert block add `for (Account a : Trigger.new) { a.Description = 'x'; }`, then insert one account.
- **C (SOQL in loop):** in the before block add `for (Account a : Trigger.new) { Integer c = [SELECT COUNT() FROM Contact]; }`, then run A again. Remove it afterwards.
- **D (recursion):** add `after update` to the trigger events and, inside `run()`, add:
```apex
if (Trigger.isAfter && Trigger.isUpdate) {
    update [SELECT Id FROM Account WHERE Id IN :Trigger.newMap.keySet()];
}
```
Update one account → *maximum trigger depth exceeded*. Then change the condition to `... && !hasRun) { hasRun = true; ...` and retry.

**Expect:** A's log shows three `BEFORE INSERT` lines (200, 200, 50) and `newMap null? true`. B → `FinalException: Record is read-only`. C → 101 queries error. D → depth error, then fixed by the static flag.

**Lesson:** Triggers run in chunks of 200. `Trigger.newMap` doesn't exist in before insert. Records are editable only in **before** triggers. One trigger per object with a handler class; bulkify; use a static variable to stop recursion.

---

## Lab 6: Flow and order of execution
**Do**
1. Setup → Flows → New → **Record-Triggered Flow**. Object **Account**, trigger **A record is created**, **Optimize for: Fast Field Updates** (before-save). Add an **Assignment**: `{!$Record.Description}` Equals `FLOW`. Save and activate as `Lab Before Save`.
2. New second flow: Account, **created**, **Actions and Related Records** (after-save). Add **Create Records** → Task: Subject `Lab follow up`, WhatId `{!$Record.Id}`. Activate as `Lab After Save`.
3. Run in anonymous Apex:
```apex
insert new Account(Name = 'Order Lab', Description = 'orig');
System.debug([SELECT Description FROM Account WHERE Name = 'Order Lab'][0].Description);
System.debug([SELECT COUNT() FROM Task WHERE Subject = 'Lab follow up']);
```
4. Optional: create an **invocable action**:
```apex
public class LabInvocable {
    @InvocableMethod(label='Lab hello')
    public static List<String> hello(List<Id> ids) {
        List<String> out = new List<String>();
        for (Id i : ids) { out.add('Seen ' + i); }
        return out;
    }
}
```
Call it from the after-save flow with an **Action** element.

**Expect:** Description = `TRIGGER saw: FLOW` and one Task exists.

**Lesson:** The before-save flow ran **before** the before trigger (the trigger saw `FLOW`, not `orig`). Use before-save flows for fast same-record updates and after-save flows for related records and emails. Invocable methods are `static`, one per class, and take a `List`.

---

## Lab 7: Async Apex (future, Queueable, Batch, Schedulable)
**Do:** create the classes, then run the anonymous snippets. Check results under **Setup → Apex Jobs** and the Debug Logs.
```apex
public class LabFuture {
    @future
    public static void run(Set<Id> ids) { System.debug('future got ' + ids.size()); }
}
```
```apex
public class LabQueueable implements Queueable {
    private List<Account> accts;
    public LabQueueable(List<Account> a) { accts = a; }
    public void execute(QueueableContext ctx) {
        System.debug('Queueable got ' + accts.size() + ' accounts, job ' + ctx.getJobId());
    }
}
```
```apex
public class LabBatch implements Database.Batchable<SObject>, Database.Stateful {
    public Integer total = 0;
    public Integer chunks = 0;
    public Database.QueryLocator start(Database.BatchableContext bc) {
        return Database.getQueryLocator('SELECT Id FROM Account');
    }
    public void execute(Database.BatchableContext bc, List<Account> scope) {
        total += scope.size(); chunks++;
    }
    public void finish(Database.BatchableContext bc) {
        System.debug('Processed ' + total + ' in ' + chunks + ' chunks');
    }
}
```
```apex
public class LabSched implements Schedulable {
    public void execute(SchedulableContext sc) { Database.executeBatch(new LabBatch(), 200); }
}
```
Anonymous:
```apex
LabFuture.run(new Map<Id, Account>([SELECT Id FROM Account LIMIT 5]).keySet());
System.debug(System.enqueueJob(new LabQueueable([SELECT Id FROM Account LIMIT 5])));
System.debug(Database.executeBatch(new LabBatch(), 50));
System.debug(System.schedule('Lab nightly', '0 0 2 * * ?', new LabSched()));
```
Experiments: (1) change the future parameter to `List<Account>` and compile; (2) remove `Database.Stateful` and re-run the batch; (3) call `LabFuture.run` from inside `LabBatch.execute`.

**Expect:** (1) compile error, only primitives allowed; (2) `finish` prints `Processed 0` (state reset); (3) runtime error.

**Lesson:** Future = primitives only, no return, no chaining. Queueable = any types, returns a job Id, can chain. Batch = big volumes, limits reset per chunk, needs `Stateful` to carry values. Schedulable just starts jobs (cron `0 0 2 * * ?` = daily 2 AM).

---

## Lab 8: Callouts and mixed DML
**Do**
1. Setup → **Remote Site Settings** → New: URL `https://api.github.com`.
2. Class:
```apex
public class LabCallout {
    public static String zen() {
        HttpRequest r = new HttpRequest();
        r.setEndpoint('https://api.github.com/zen');
        r.setMethod('GET');
        r.setHeader('User-Agent', 'sf-lab');
        HttpResponse res = new Http().send(r);
        return res.getStatusCode() + ' ' + res.getBody();
    }
}
```
3. Anonymous:
```apex
System.debug(LabCallout.zen());                // works
```
```apex
insert new Account(Name = 'DML first');          // DML first
System.debug(LabCallout.zen());                // then callout
```
```apex
insert new Account(Name = 'Mixed 1');            // non-setup object
insert new Group(Name = 'Lab Group');            // setup object
```
4. Fix the first failure by calling out before the insert, or by enqueueing a Queueable that does the callout.
5. Optional: create a **Named Credential** for the same URL and use `callout:Your_Named_Credential` as the endpoint.

**Expect:** the second block throws `You have uncommitted work pending. Please commit or rollback before calling out`; the third throws `MIXED_DML_OPERATION`.

**Lesson:** No callout after uncommitted DML in the same transaction. Setup objects (User, Group…) and normal objects can't be mixed in one transaction, so split them with async Apex. Authorize endpoints with a Remote Site Setting or, better, a Named Credential.

---

## Lab 9: LWC (events, wire, imperative, `@api`)
**Do (VS Code, authorized to your sandbox)**
1. Apex controller:
```apex
public with sharing class LabAccountController {
    @AuraEnabled(cacheable=true)
    public static List<Account> getAccounts(String searchKey) {
        String k = '%' + (searchKey == null ? '' : searchKey) + '%';
        return [SELECT Id, Name, Industry FROM Account WHERE Name LIKE :k ORDER BY Name LIMIT 20];
    }
    @AuraEnabled
    public static Account addAccount(String name) {
        Account a = new Account(Name = name);
        insert a;
        return a;
    }
}
```
2. Child component `labAccountRow`:
```html
<template>
    <lightning-button label={account.Name} onclick={handleClick}></lightning-button>
</template>
```
```js
import { LightningElement, api } from 'lwc';
export default class LabAccountRow extends LightningElement {
    @api account;
    handleClick() {
        this.dispatchEvent(new CustomEvent('selectitem', { detail: { recordId: this.account.Id } }));
    }
    @api highlight() {
        this.template.querySelector('lightning-button').variant = 'brand';
    }
}
```
3. Parent `labAccountList`:
```html
<template>
    <lightning-card title="Lab accounts">
        <div class="slds-p-around_small">
            <lightning-input label="Search" value={searchKey} onchange={handleSearch}></lightning-input>
            <template for:each={accounts} for:item="acc">
                <c-lab-account-row key={acc.Id} account={acc} onselectitem={handleSelect}></c-lab-account-row>
            </template>
            <p>Selected Id: {selectedId}</p>
            <lightning-button label="Add via imperative Apex" onclick={handleAdd}></lightning-button>
        </div>
    </lightning-card>
</template>
```
```js
import { LightningElement, wire } from 'lwc';
import { refreshApex } from '@salesforce/apex';
import getAccounts from '@salesforce/apex/LabAccountController.getAccounts';
import addAccount from '@salesforce/apex/LabAccountController.addAccount';
export default class LabAccountList extends LightningElement {
    searchKey = '';
    accounts = [];
    selectedId;
    wiredResult;
    @wire(getAccounts, { searchKey: '$searchKey' })
    wired(result) {
        this.wiredResult = result;
        if (result.data) this.accounts = result.data;
    }
    handleSearch(e) { this.searchKey = e.target.value; }
    handleSelect(e) { this.selectedId = e.detail.recordId; }
    async handleAdd() {
        await addAccount({ name: 'LWC Lab ' + Date.now() });
        await refreshApex(this.wiredResult);
    }
}
```
4. `labAccountList.js-meta.xml`: set `<isExposed>true</isExposed>` with targets `lightning__AppPage` and `lightning__HomePage` (copy the structure from a generated component; use your org's API version).
5. Deploy (`SFDX: Deploy This Source to Org`), then drag the component onto a Lightning App Page (App Builder).
6. Experiments: (a) delete `cacheable=true` and reload; (b) change `onselectitem` to `onselectItem` or to `on-selectitem`; (c) remove `key=`; (d) add `@api recordId` and a second copy of the component to an Account record page.

**Expect:** (a) the wire errors, since @wire needs cacheable; (b) the click does nothing; (c) a template compile error; (d) `recordId` is auto-filled on a record page.

**Lesson:** Child → parent = `CustomEvent` (lowercase name, handler `on` + name). Parent → child = `@api` property or method. `@wire` needs `cacheable=true` and re-runs on reactive `$` parameters. Use imperative Apex for DML, then `refreshApex`.

---

## Lab 10: Visualforce (list controller, extension, view state)
**Do:** Developer Console → File → New → Apex Class `LabVfExt`:
```apex
public with sharing class LabVfExt {
    private ApexPages.StandardSetController ssc;
    public String hello { get; private set; }
    public List<String> big;                 // later add: transient
    public LabVfExt(ApexPages.StandardSetController c) {
        ssc = c;
        ssc.setPageSize(5);
        hello = 'Hi from extension';
        big = new List<String>();
        for (Integer i = 0; i < 3000; i++) big.add('row ' + i + ' lorem ipsum dolor sit amet');
    }
    public PageReference addMsg() {
        ApexPages.addMessage(new ApexPages.Message(ApexPages.Severity.INFO, 'Added from controller'));
        return null;
    }
}
```
File → New → Visualforce Page `LabVf`:
```html
<apex:page standardController="Account" recordSetVar="accts" extensions="LabVfExt">
    <apex:form>
        <apex:pageMessages id="msgs"/>
        <apex:outputPanel id="tbl">
            <apex:pageBlock title="Accounts - page {!pageNumber}">
                <apex:pageBlockTable value="{!accts}" var="a">
                    <apex:column value="{!a.Name}"/>
                    <apex:column value="{!a.Industry}"/>
                </apex:pageBlockTable>
            </apex:pageBlock>
        </apex:outputPanel>
        <apex:commandButton value="Next" action="{!next}" rerender="tbl"/>
        <apex:commandButton value="Previous" action="{!previous}" rerender="tbl"/>
        <apex:commandButton value="Message" action="{!addMsg}" rerender="msgs"/>
        <p>{!hello}</p>
    </apex:form>
</apex:page>
```
Open it at `/apex/LabVf`. Then: My Settings → Advanced User Details → enable **Development Mode** (and Show View State), refresh and open the **View State** tab. Change the field to `public transient List<String> big;`, save and compare.

**Expect:** paging works without a full page reload; the button adds a message; view state is large with `big` and tiny with `transient`.

**Lesson:** `recordSetVar` gives a list controller with free paging. A **list** controller extension takes `ApexPages.StandardSetController` (a record-page extension takes `StandardController`). Buttons must live in `apex:form`; `rerender` does partial refresh; `transient` shrinks view state (limit 170 KB).

---

## Lab 11: Test class (bulk, setup, mock, assertions)
**Do:** create `LabTests` (it tests Labs 5 and 8; keep their code deployed).
```apex
@isTest
private class LabTests {
    @testSetup static void setup() { insert new Account(Name = 'Seed'); }

    @isTest static void bulkInsert() {
        List<Account> l = new List<Account>();
        for (Integer i = 0; i < 200; i++) l.add(new Account(Name = 'Bulk ' + i));
        Test.startTest();
        insert l;
        Test.stopTest();
        Assert.areEqual(200, [SELECT COUNT() FROM Account WHERE Name LIKE 'Bulk %'], 'accounts');
        Assert.areEqual(200, [SELECT COUNT() FROM Contact WHERE LastName LIKE 'Primary Bulk %'], 'contacts');
        Assert.areEqual(1, [SELECT COUNT() FROM Account WHERE Name = 'Seed'], 'seed');
    }

    public class Mock implements HttpCalloutMock {
        public HttpResponse respond(HttpRequest r) {
            HttpResponse x = new HttpResponse();
            x.setStatusCode(200);
            x.setBody('mocked');
            return x;
        }
    }

    @isTest static void callout() {
        Test.setMock(HttpCalloutMock.class, new Mock());
        Test.startTest();
        String s = LabCallout.zen();
        Test.stopTest();
        Assert.areEqual('200 mocked', s);
    }
}
```
Run (Developer Console → Test → New Run). Experiments: (a) delete the `Test.setMock` line; (b) add `@isTest(SeeAllData=true)` to the class and note that `@testSetup` isn't allowed with it... remove it again; (c) temporarily add SOQL in the trigger's loop (Lab 5-C) and run the bulk test; (d) open **Setup → Apex Test Execution** and note coverage per class.

**Expect:** (a) error `Methods defined as TestMethod do not support Web service callouts`; (c) `Too many SOQL queries: 101`.

**Lesson:** Test with **200 records**, use `@testSetup` for shared data, `Test.startTest/stopTest` for fresh limits, mock every callout, and assert real outcomes. Deployment needs 75% coverage and all tests passing.

---

## Lab 12: Debugging
**Do**
1. Setup → Debug Logs → trace flag for yourself, Apex Code **FINEST**, Database **FINEST**.
2. Run Lab 5-A (bulk insert) via Execute Anonymous with *Open Log* ticked.
3. In the log viewer: filter **Debug Only** and then clear it. Find: `CODE_UNIT_STARTED`, `SOQL_EXECUTE_BEGIN`, `DML_BEGIN`, and the **LIMIT_USAGE_FOR_NS** block at the end.
4. Lower the Apex Code level to **ERROR**, repeat, and compare the log size.
5. Developer Console → **Query Editor**: run `SELECT Id, Name FROM Account ORDER BY CreatedDate DESC LIMIT 5`.
6. Optional (VS Code): **SFDX: Turn On Apex Debug Log for Replay Debugger**, run Lab 5, then launch the **Apex Replay Debugger** with a breakpoint in the handler.

**Lesson:** Trace flags decide which logs are written (and expire). Log levels: ERROR < WARN < INFO < **DEBUG** < FINE < FINER < FINEST. The limits summary at the end of the log shows how close you came to the governor limits.

---

## Lab 13: Deployment (change set, scratch org, validate and Quick Deploy)
**Part A: change set (needs a sandbox plus a deployment connection to another org)**
1. In the sandbox: Setup → **Outbound Change Sets** → New → add `AccountTrigger`, `AccountTriggerHandler`, `LabTests`, `LabCallout` → **Upload** to the target org (use a second sandbox or production).
2. In the target: Setup → **Inbound Change Sets** → open it → **Validate** (don't deploy to production). Open **Deployment Status** and read the results: which tests ran, coverage, any failures.
3. After a successful validation, notice the **Quick Deploy** option.

**Part B: Salesforce CLI and scratch org (needs a Dev Hub org)**
```bash
sf project generate --name pd1lab
cd pd1lab
sf org login web --set-default-dev-hub
sf org create scratch --definition-file config/project-scratch-def.json --alias lab --duration-days 3 --set-default
sf project deploy start --source-dir force-app
sf apex run test --code-coverage --result-format human --wait 10
sf org open
```
`config/project-scratch-def.json` is the file that defines the scratch org (`{"orgName":"PD1 Lab","edition":"Developer"}` is enough). Also open `package.xml` (a manifest of components) and note what `destructiveChanges.xml` would do.

**Expect:** the change set validation runs tests; the scratch org appears with an expiry date.

**Lesson:** Change sets move metadata only between **connected** orgs and are click-based. The CLI plus source control is repeatable and used with scratch orgs and unlocked packages. Validate first, then **Quick Deploy** within about 10 days without re-running tests. Apex can't be edited directly in production.

---

## Lab 14: Data Loader and External IDs
**Do**
1. Create Account field `Legacy_Id__c` (Text, tick **External ID** and **Unique**).
2. Make `accounts.csv`:
```
Legacy_Id__c,Name
L-1,Lab Upsert One
L-2,Lab Upsert Two
```
3. Open **Data Loader** → **Upsert** → Account → external ID `Legacy_Id__c` → map the fields → finish. Check the success file.
4. Edit the CSV (`Lab Upsert One` → `Lab Upsert One Updated`, add `L-3`) and upsert again.
5. Try **Data Import Wizard** for the same data and note which objects it offers.

**Expect:** the first run inserts two records. The second updates L-1, leaves L-2 and inserts L-3.

**Lesson:** `upsert` matches on **Id or an External ID**. Data Loader handles large volumes (millions); the wizard is limited to 50,000 records and certain objects. Data Loader has no merge.

---

## Progress tracker
- [ ] 1 Relationships  - [ ] 2 Security  - [ ] 3 SOQL/DML  - [ ] 4 Limits  - [ ] 5 Trigger
- [ ] 6 Flow + order  - [ ] 7 Async  - [ ] 8 Callouts  - [ ] 9 LWC  - [ ] 10 Visualforce
- [ ] 11 Tests  - [ ] 12 Debug  - [ ] 13 Deploy  - [ ] 14 Data Loader

After each lab, write one sentence in your own words, such as "after-trigger records are read-only". Those sentences are your real study notes.
