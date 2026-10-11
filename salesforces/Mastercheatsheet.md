# Platform Developer I – Master Cheat Sheet (all topics)

Use this with `PD1_Numbers_Cheatsheet.md` (limits and sizes). This sheet is about **telling similar things apart**, since confusion is what's costing you marks.

## 0. Your 3 mocks at a glance

| Mock | Score | Fund. | Logic | UI | Test/Deploy |
|---|---|---|---|---|---|
| 1 | 70% (pass) | 11/15 | 10/17 | 9/14 | 12/14 |
| 2 | 65% (fail) | 10/14 | 10/17 | 10/16 | 9/13 |
| 3 | 78.3% (pass) | 9/14 | 16/18 | 10/14 | 12/14 |

The pass mark is 68%, so you're on the edge. **Fundamentals and UI are flat at about 65-70%.** Your misses fall into five clusters, and sections 1-5 below target them:
1. LWC vs Aura vs Visualforce **syntax** mixed up
2. Sharing vs FLS (**who enforces what**)
3. Package and sandbox types
4. Small Apex/SOQL **operators and methods**
5. Limits you know roughly but not exactly (see numbers sheet)

---

## 1. Confusion-busters: X vs Y

### 1.1 Security: three separate layers

| Layer | Question it answers | Tools |
|---|---|---|
| **Object** | Can the user create/read/edit/delete this object? | Profile, permission set (CRUD) |
| **Field** | Can the user see/edit this field? | Field-level security (FLS) |
| **Record** | Which records can the user see? | OWD → role hierarchy → sharing rules → manual sharing → Apex managed sharing, teams |

- **OWD** = baseline, the *most restrictive* setting. Everything else only **opens** access.
- Role hierarchy gives access **upward** to managers (if "Grant Access Using Hierarchies" is on).
- **Apex managed sharing** = inserting `__Share` rows from code.
- **View All / Modify All** on an object overrides record sharing for that object.

### 1.2 Apex and security

| Feature | Enforces record sharing | Enforces CRUD/FLS |
|---|---|---|
| `with sharing` | ✔ | ✘ |
| `without sharing` | ✘ | ✘ |
| `inherited sharing` | uses the caller's mode; **`with sharing` if entry point** | ✘ |
| `WITH USER_MODE` (SOQL/DML) | ✔ | ✔ |
| `WITH SECURITY_ENFORCED` | ✘ | ✔ on SELECT fields (throws if inaccessible) |
| `Security.stripInaccessible()` | ✘ | ✔ (**removes** inaccessible fields) |
| `Schema...isAccessible()/isUpdateable()` | ✘ | ✔ (manual checks) |

**Rule of thumb:** Apex runs in system mode by default; sharing keywords only change *record* visibility.

### 1.3 Relationships

| | Lookup | Master-detail |
|---|---|---|
| Parent required | No (optional) | **Yes** |
| Delete parent | Children stay (or block delete) | **Children deleted (cascade)** |
| Sharing / owner | Independent | **Inherited from master** |
| Roll-up summary | ✘ (needs Flow/Apex) | **✔ on the master** |
| Per object | many (total relationships 40) | **max 2** |
| Self-relationship | ✔ | **✘** |

- **Many-to-many** = junction object with **two master-detail** relationships.
- A lookup → master-detail conversion needs **every child to have a parent value**.
- Roll-up types: **COUNT, SUM, MIN, MAX** (no AVG).
- **Lookup filter** limits which records can be picked.
- **Polymorphic** fields: `WhoId` (Contact/Lead), `WhatId` (many objects) on Task/Event.

### 1.4 Ids and keys
- **15-char** Id is **case-sensitive**; **18-char** is **case-insensitive** (safe for Excel, API returns 18).
- **External ID**: up to 25 per object; used for **upsert** and integrations.

### 1.5 Declarative tool picker

| Need | Use |
|---|---|
| Block a save if data is bad | **Validation rule** |
| Calculated read-only value | **Formula field** (can reference parent fields) |
| Sum/count children | **Roll-up summary** (master-detail only) |
| Different layouts/picklists per user group | **Page layout + record types** |
| Multi-step sign-off | **Approval process** |
| Update the triggering record fast | **Before-save record-triggered flow** |
| Create/update related records, send email | **After-save record-triggered flow** |
| User-guided input | **Screen flow** |
| Configuration data that deploys | **Custom metadata type** |
| Per org/profile/user values | **Hierarchy custom setting** |
| Translatable text | **Custom label** |
| Complex/bulk logic, callouts with logic | **Apex** |

### 1.6 Custom metadata vs custom settings vs custom labels

| | Records deployable | Access in Apex | Typical use |
|---|---|---|---|
| Custom metadata type | ✔ (metadata) | `Type__mdt.getInstance('DevName')` | App config, mappings |
| Custom setting (hierarchy) | ✘ (data) | `Setting__c.getInstance()` | Per-user/profile values |
| Custom label | ✔ | `System.Label.Name` | Translatable text |

---

## 2. Apex language essentials

### Collections

| | Ordered | Duplicates | Access |
|---|---|---|---|
| **List** | ✔ | ✔ | index `list[0]`, `.size()`, `.add(i, v)` inserts at index |
| **Set** | ✘ | ✘ | `.contains()`, `.size()` |
| **Map** | key-based | keys unique | `.get(key)` → **null** if missing, `.keySet()`, `.values()` |

- Records → map: `new Map<Id, Account>([SELECT Id FROM Account])`
- Ids of a list: `new Map<Id, Account>(accts).keySet()`

### OOP keywords
- `virtual` / `abstract` → class/method can be extended; child uses **`override`**; call parent with **`super`**.
- Class implementing an interface must implement **all** methods (unless abstract).
- Class can have **multiple constructors** (different params).
- `static final Integer MAX = 10;` is a constant. Statics last **one transaction**.
- Access: `private` (class only) → `protected` → `public` (namespace) → `global` (everywhere).

### Operators and helpers you've been missing

| Need | Code |
|---|---|
| Safe navigation | `acct?.Name` (returns null, no exception) |
| Null coalescing | `a ?? b` |
| Blank check (null/empty/whitespace) | `String.isBlank(s)` (`isEmpty` ignores whitespace) |
| Generic field read | `sObj.get('Name')` / `put('Name', v)` |
| Cast | `Account a = (Account) sObj;` |
| Switch | `switch on x { when 'A' {…} when else {…} }` |
| Dynamic SOQL | `Database.query(str)` (use bind variables / `escapeSingleQuotes`) |
| Remainder | `Math.mod(10, 3)` → 1 |
| Substring | `'Salesforce'.substring(0, 5)` → `Sales` (end excluded) |
| Decimal for money | `Decimal`, not `Double` |

### Exceptions

| Exception | Cause |
|---|---|
| `DmlException` | DML failure |
| `QueryException` | SOQL returns **0 or >1 rows into a single sObject** |
| `NullPointerException` | Dereferencing null |
| `ListException` | Bad list index |
| `LimitException` | Governor limit, **cannot be caught**, transaction aborts and rolls back |
| `CalloutException` | Callout problem (including DML-then-callout) |

Custom exception: `public class MyEx extends Exception {}` (class name ends with `Exception`).

---

## 3. Triggers

### Context variables

| Variable | Insert | Update | Delete | Undelete |
|---|---|---|---|---|
| `new` | ✔ | ✔ | ✘ | ✔ |
| `old` | ✘ | ✔ | ✔ | ✘ |
| `newMap` | **after only** | ✔ | ✘ | ✔ |
| `oldMap` | ✘ | ✔ | ✔ | ✘ |

Booleans: `isInsert, isUpdate, isDelete, isUndelete, isBefore, isAfter`.

| | Before | After |
|---|---|---|
| Modify the record itself | ✔ no DML needed | ✘ read-only, needs DML on a copy |
| Id available on insert | ✘ | ✔ |
| Best for | validation, defaulting fields | related records, callouts via async |

- Block a save: `record.addError('msg')`.
- Best practice: **one trigger per object**, logic in a **handler class**, **bulkified**, **no SOQL/DML in loops**, static flag to stop recursion.

### Order of execution (short form)
1. System validation
2. **Before-save flows**
3. **Before triggers**
4. Custom validation rules, duplicate rules
5. Save (not committed)
6. **After triggers**
7. Assignment/auto-response/workflow rules
8. Escalation rules
9. **After-save flows**
10. Roll-up summary, then commit
11. Post-commit (emails, async)

---

## 4. SOQL / SOSL / DML

### SOQL quick syntax

| Need | Syntax |
|---|---|
| Parent → children | `SELECT Name, (SELECT LastName FROM Contacts) FROM Account` (plural relationship name) |
| Child → parent | `SELECT Name, Account.Name FROM Contact` (dot) |
| Custom relationship | `Parent__r.Name` |
| Filter by set | `WHERE Id IN :ids` |
| Multi-select picklist | `WHERE Skills__c INCLUDES ('A;B')` |
| Pattern | `LIKE 'Acme%'` |
| Count only | `SELECT COUNT() FROM Account` |
| Aggregate | `GROUP BY` then **`HAVING`** (filters aggregates) |
| Sort | `ORDER BY CreatedDate DESC` |
| Include recycle bin | `… WHERE IsDeleted = true ALL ROWS` |
| Lock rows | `FOR UPDATE` |
| Bind variable | `:myVar` |
| Chunked iteration | `for (Account a : [SELECT Id FROM Account])` (**saves heap**) |

Aggregate queries return **`List<AggregateResult>`**.

### SOQL vs SOSL

| | SOQL | SOSL |
|---|---|---|
| Searches | one object (+ relationships) | **text across many objects** |
| Returns | `List<SObject>` | **`List<List<SObject>>`** |
| Use for | precise filters, aggregates | find a term anywhere |

### DML

| Statement | Behaviour |
|---|---|
| `insert/update/delete/undelete` | all-or-nothing; throws `DmlException` |
| `upsert` | uses **Id or External ID** |
| `merge` | **Leads, Contacts, Accounts** only |
| `Database.insert(list, false)` | **partial success**, returns `List<Database.SaveResult>`; check `isSuccess()` |
| `Database.setSavepoint()` / `Database.rollback(sp)` | undo within a transaction |
| **Mixed DML** | setup objects (User, Group…) + non-setup objects in one transaction → split into `@future`/Queueable |

---

## 5. Async Apex picker

| | `@future` | Queueable | Batch | Schedulable |
|---|---|---|---|---|
| Params | primitives/collections only | **any (sObjects, custom types)** | via constructor | – |
| Returns | void | **job Id** | job Id | job Id |
| Chaining | ✘ | ✔ | `finish()` | – |
| Volume | small | medium | **millions** | triggers other jobs |
| Key methods | `static void` | `execute(QueueableContext)` | `start / execute / finish` | `execute(SchedulableContext)` |
| Start with | `myMethod()` | `System.enqueueJob()` | `Database.executeBatch(job, scope)` | `System.schedule(name, cron, job)` |

- Batch needs `Database.Batchable<SObject>`; `Database.Stateful` keeps instance variables across chunks.
- Cron example: `0 0 8 * * ?` = daily 8 AM (sec min hour day month weekday).
- Callouts in async: `@future(callout=true)` or `Database.AllowsCallouts` on batch.
- Everything shows in **Setup → Apex Jobs**.

---

## 6. Flow and automation

| Flow type | Trigger | Use |
|---|---|---|
| Record-triggered (**before save**) | create/update | update same record fast, no DML |
| Record-triggered (**after save**) | create/update/delete | related records, emails, actions |
| Screen flow | user | guided input |
| Scheduled-triggered | time | batch-like updates |
| Platform event-triggered | event | react to events |
| Autolaunched | called by something | subflow / Apex-invoked |

- **Fault connector** handles element errors.
- Call Apex with **`@InvocableMethod`**: **static**, **one per class**, input is a **List**.
- Workflow rules and Process Builder are retired in favour of Flow.

---

## 7. Callouts and integration

- Classes: **`Http`, `HttpRequest`, `HttpResponse`**. `req.setMethod('GET')`, `setEndpoint`, `setHeader`, `setBody`.
- Authorize endpoints with a **Named Credential** (preferred, stores auth) or **Remote Site Setting**.
- **DML before callout in the same transaction → "uncommitted work pending"**. Callout first, or go async.
- Success status code: **200**.
- JSON: `JSON.serialize`, `JSON.deserialize(str, Type)`, `JSON.deserializeUntyped`.
- **Platform events**: API name ends `__e`, publish with `EventBus.publish()`, subscribe with Apex trigger/Flow/LWC; **not queryable** with SOQL.

---

## 8. Lightning Web Components

### Syntax cheat sheet

| Need | LWC syntax |
|---|---|
| Click handler | `onclick={handleClick}` |
| Change handler | `onchange={handleChange}` |
| Loop | `<template for:each={items} for:item="item">` + **`key={item.id}`** on first child |
| Conditional | `<template lwc:if={cond}>` / `lwc:elseif` / `lwc:else` (older: `if:true`) |
| Read data attribute | `data-id="x"` → `event.target.dataset.id` |
| Query own DOM | `this.template.querySelector('.cls')` |
| Slot (parent injects content) | `<slot></slot>` in child |
| Public property | `@api name;` |
| Public method | `@api refresh() {}` |

### Decorators

| Decorator | Meaning |
|---|---|
| `@api` | public, parent can set (treat as **read-only** inside the child) |
| `@track` | only needed to watch **inside** objects/arrays (primitives are reactive by default) |
| `@wire` | reactive data from an adapter or **cacheable** Apex |

### Apex from LWC

| | `@wire` | Imperative |
|---|---|---|
| Apex needs | `@AuraEnabled(cacheable=true)` static | `@AuraEnabled` static (cacheable optional) |
| DML allowed | ✘ | ✔ |
| Triggered | automatically/reactively | when you call it (returns a **promise**) |
| Refresh | `refreshApex()` | call again |

### Events

| Direction | How |
|---|---|
| Parent → child | set `@api` property, or call `@api` method via `querySelector` |
| Child → parent | **`this.dispatchEvent(new CustomEvent('selectitem', { detail }))`** and parent uses **`onselectitem={handler}`** (lowercase, no dashes) |
| Up several levels | `{ bubbles: true, composed: true }` |
| Unrelated components | **Lightning Message Service** (also works with Aura and VF) |

### Lifecycle
`constructor` → `connectedCallback` (inserted into DOM, do initial work) → `render` → `renderedCallback` (after every render) → `disconnectedCallback` (**cleanup**) · `errorCallback` (**catches child errors**).

### Meta XML (`.js-meta.xml`)
- `isExposed` = **true** plus `<targets>`: `lightning__RecordPage`, `lightning__AppPage`, `lightning__HomePage`, **`lightning__FlowScreen`**.
- Record page needs **`@api recordId`** (and `@api objectApiName` if needed).
- Flow inputs/outputs go in `<targetConfig>` / `<property>`; send values back with **`FlowAttributeChangeEvent`**.

### Useful imports

| Need | Import |
|---|---|
| Component base | `import { LightningElement, api, wire } from 'lwc'` |
| Apex method | `import m from '@salesforce/apex/Cls.method'` |
| Field | `import F from '@salesforce/schema/Account.Name'` |
| Label | `import L from '@salesforce/label/c.My_Label'` |
| Static resource | `@salesforce/resourceUrl/name` |
| Toast | `import { ShowToastEvent } from 'lightning/platformShowToastEvent'` |
| Navigate | `NavigationMixin` from `lightning/navigation` |
| LDS | `getRecord`, `createRecord`, `updateRecord` from `lightning/uiRecordApi` |
| Refresh wire | `refreshApex` from `@salesforce/apex` |

### Base components
`lightning-record-form` / `record-view-form` / `record-edit-form` (**LDS**, enforce FLS/sharing, no Apex), `lightning-datatable` (table), `lightning-card`, `lightning-input`, `lightning-spinner` (loading).
Styling: **SLDS**. Tests: **Jest**. Security: **Lightning Web Security**.

---

## 9. Aura (older framework)

| Need | Aura |
|---|---|
| Bind attribute | **`{!v.attribute}`** |
| Call Apex | `var a = component.get("c.methodName"); a.setCallback(this, fn); $A.enqueueAction(a);` |
| Component event | handled by the **parent hierarchy** |
| **Application event** | handled by **any component** in the app |
| Fire toast | `$A.get("e.force:showToast")` |

- **LWC can live inside Aura. Aura cannot live inside LWC.**
- Prefer LWC for new development.

---

## 10. Visualforce

| Need | Visualforce |
|---|---|
| Merge expression | **`{!expression}`** |
| Standard controller | `<apex:page standardController="Account">` (free `save()`, `edit()`, `delete()`) |
| Standard **list** controller | add **`recordSetVar="accts"`** |
| Controller extension | constructor takes **`ApexPages.StandardController`** |
| Custom controller | `controller="MyClass"`; `{!acc}` calls **`getAcc()`** |
| Form actions | wrap in **`<apex:form>`** |
| Partial refresh | **`rerender="id"`** |
| JS-callable action | **`apex:actionFunction`** |
| Show errors | `ApexPages.addMessage(new ApexPages.Message(ApexPages.Severity.ERROR,'msg'))` + **`<apex:pageMessages/>`** |
| Navigate | action returns a **`PageReference`** (`null` = stay) |
| Reduce view state | **`transient`** (limit **170 KB**) |
| Table | `<apex:pageBlockTable>` |

---

## 11. Testing

### Annotations and methods

| Item | Purpose |
|---|---|
| `@isTest` | marks test class/method (not counted against org code size) |
| `@testSetup` | **runs once per class**; each method sees a fresh rolled-back copy |
| `@TestVisible` | lets tests access **private** members |
| `@IsTest(SeeAllData=true)` | access org data (**discouraged**, default is false) |
| `@IsTest(isParallel=true)` | parallel run |
| `Test.startTest()/stopTest()` | **fresh limits** for code between; **async finishes at stopTest** |
| `System.runAs(user)` | test as a specific user (sharing/permissions) |
| `Test.setMock(HttpCalloutMock.class, mock)` | fake HTTP callouts |
| `Test.setMock(WebServiceMock.class, mock)` | fake SOAP callouts |
| `Test.loadData` | load records from a CSV static resource |
| `Test.isRunningTest()` | true inside tests |
| `Assert.areEqual(exp, act, msg)` | modern assertions (`System.assertEquals` still works) |

### What a good test covers
**Positive, negative, bulk (200 records), and different users.** Create your own data (never hard-code Ids) and assert results; coverage alone isn't enough.
- Test a trigger blocking a save with `Database.insert(rec, false)` or `try/catch`.
- Test exceptions with `try/catch` + `Assert.fail()` if nothing is thrown.
- Batch tests run **one chunk** inside `startTest/stopTest`.
- **Coverage rules:** ≥ **75% overall**, every trigger has **some** coverage, **all tests must pass**.

---

## 12. Debugging

| Tool | Use |
|---|---|
| Debug logs + **trace flag** | capture logs for a user; flags expire |
| Log levels | ERROR < WARN < INFO < **DEBUG (default for `System.debug`)** < FINE < FINER < **FINEST** |
| Log categories | DB (SOQL/DML), Apex Code, Callout, Workflow, Validation… |
| **Developer Console** | logs, **Query Editor**, **Execute Anonymous**, test runs |
| **Apex Replay Debugger** (VS Code) | step through a recorded log |
| `Limits.getQueries()` / `getLimitQueries()` | used vs allowed |
| **Apex Jobs** page | async job status |
| **Deployment Status** page | deploy progress and errors |

---

## 13. Environments, packages and deployment

### Sandboxes

| Type | Data | Refresh |
|---|---|---|
| **Developer** | **metadata only**, 200 MB | 1 day |
| **Developer Pro** | metadata only, 1 GB | 1 day |
| **Partial Copy** | metadata + **sample data via template**, 5 GB | 5 days |
| **Full** | **everything**, same as production, **best for performance/load testing** | **29 days** |

- **Sandbox templates** work with **Partial and Full** only.
- **Scratch org**: temporary (1–30 days, default 7), created from **`project-scratch-def.json`**, for source-driven development and CI.
- Apex **cannot be edited directly in production**.

### Packages

| Type | Upgradeable | Notes |
|---|---|---|
| **Unmanaged** | **✘** | just a copy of components |
| **Managed** | ✔ | namespace, IP protected, AppExchange |
| **Unlocked** | ✔ | **your own org's modular metadata**, no IP protection, optional namespace |

### Deployment tools

| Tool | Notes |
|---|---|
| **Change sets** | only between orgs with a **deployment connection** (prod ↔ its sandboxes); clicks, no version control |
| **Salesforce CLI (`sf`)** | **source-driven, with version control** |
| Metadata API / `package.xml` | manifest of components; **`destructiveChanges.xml`** deletes components |
| **DevOps Center** | work items + pipeline over source control |
| **Validate (check-only)** then **Quick Deploy** | skip re-running tests if deployed within **10 days** |

### Key CLI commands
`sf org login web` (authorize) · `sf org open` · `sf project deploy start` · `sf project retrieve start` · `sf apex run test` · `sf org create scratch` · config in **`sfdx-project.json`**.

---

## 14. Exam keywords and traps

| If the question says… | Think… |
|---|---|
| "most efficient / least code" | declarative (Flow, formula, validation rule) |
| "bulk", "200 records", "governor limit" | no SOQL/DML in loops, collections, maps |
| "millions of records" | Batch Apex |
| "pass complex objects to async" | Queueable (not `@future`) |
| "unrelated components communicate" | Lightning Message Service |
| "child to parent" | CustomEvent |
| "parent to child" | `@api` property/method |
| "needs DML from LWC" | imperative Apex |
| "read-only reactive data" | `@wire` + cacheable |
| "respect user's field access" | `USER_MODE` / `stripInaccessible` (not `with sharing`) |
| "respect user's record access" | `with sharing` |
| "multiple objects text search" | SOSL |
| "per org/profile/user config" | hierarchy custom setting |
| "deployable config records" | custom metadata |
| "version control, CI/CD" | CLI + scratch orgs / unlocked packages |
| "performance testing with prod data volume" | Full sandbox |

**Technique:** read the **last line first** (what is being asked), spot **Choose 2/3**, eliminate options that break a hard rule (limit, security layer, wrong syntax), then pick the **best** (not just a working) answer.

---

## 15. Your 10-minute pre-exam recall

1. OWD baseline → hierarchy up → sharing rules open access. **with sharing ≠ FLS.**
2. Master-detail: max 2, cascade delete, roll-up, no self-relationship.
3. Before = edit own record; After = read-only; `newMap` not in before insert.
4. 100 SOQL / 150 DML / 50,000 rows / 6 MB heap / 10 s CPU; async doubles most.
5. Callout after DML = error.
6. LWC: `onselectitem`, `@api` down, CustomEvent up, `@wire` needs cacheable.
7. Aura `{!v.x}`, application event; VF `{!x}`, `recordSetVar`, `transient`.
8. Dev sandbox metadata only, Full = 29 days + load testing, templates = Partial/Full.
9. Unmanaged = no upgrade; Unlocked = your own modular code.
10. 75% coverage, all tests pass, `startTest/stopTest`, `@testSetup`, no hard-coded Ids.
