# Salesforce Platform Developer I – Exam Prep Pack

> **Note:** All questions below are **original practice questions** written to match the style, difficulty and topics of the Platform Developer I exam. They are not real/leaked exam questions (exam dumps violate Salesforce's certification agreement and can get you banned). Practice questions like these are a better way to test real understanding anyway.

## Exam Quick Facts (verify on the official exam guide before booking)

| Item | Detail |
|---|---|
| Format | ~60 scored multiple-choice / multiple-select questions (+ a few unscored) |
| Time | 105 minutes |
| Passing score | ~65% (check current guide) |
| Fee | USD 200 (retake USD 100) |

| Domain (Trailhead trailmix weights) | Weight |
|---|---|
| Developer Fundamentals | 23% |
| Process Automation and Logic | 30% |
| User Interface | 25% |
| Testing, Debugging, and Deployment | 22% |

---

# PART A – TOPIC CHECKLIST & CHEAT SHEET

## 1. Developer Fundamentals (23%)
- Multitenancy, governor limits, MVC on the platform (Model = objects/fields, View = VF/LWC/Aura/page layouts, Controller = Apex/Flows)
- Data model: objects, fields, record types, lookup vs master-detail, many-to-many (junction object), hierarchical, roll-up summary (master-detail only), formula fields, validation rules
- Security: OWD, role hierarchy, sharing rules, manual sharing, profiles, permission sets, FLS, `with sharing` / `without sharing` / `inherited sharing`, `WITH USER_MODE`, `WITH SECURITY_ENFORCED`, `Security.stripInaccessible`
- Apex basics: primitives, collections (List/Set/Map), SObjects, classes, interfaces, inheritance (`virtual`/`abstract`), static, constants, properties, enums
- Data management: Data Import Wizard (≤ 50,000 records, limited objects) vs Data Loader (up to millions, all objects), upsert with External ID
- Declarative vs programmatic: when to use Flow, validation rules, formula fields, approval processes vs Apex

## 2. Process Automation and Logic (30%)
- Triggers: events, context variables, bulkification, one-trigger-per-object, handler pattern, recursion control
- Order of execution
- SOQL (relationships, aggregates, bind variables, FOR loops), SOSL, DML, `Database` methods, savepoints
- Flows (record-triggered before/after save, screen, autolaunched), invocable Apex
- Approval processes
- Async Apex: `@future`, Queueable, Batch, Schedulable
- Exception handling, custom exceptions
- Callouts (HttpRequest/HttpResponse, Named Credentials, Remote Site Settings), platform events
- Custom metadata types, custom settings, custom labels

## 3. User Interface (25%)
- Lightning Web Components: decorators (`@api`, `@track`, `@wire`), lifecycle hooks, events, slots, Apex (wire vs imperative), Lightning Data Service, base components, navigation, toasts
- Aura basics and interoperability
- Visualforce: standard/custom controllers, extensions, list controllers, view state, `apex:form`
- Lightning App Builder / record pages / Flow screens with LWC

## 4. Testing, Debugging, and Deployment (22%)
- Test classes: `@isTest`, `@testSetup`, `Test.startTest/stopTest`, `System.runAs`, `SeeAllData`, mocks (`HttpCalloutMock`, `Test.setMock`), assertions
- 75% code coverage, all tests must pass, triggers need coverage
- Debug logs, trace flags, log levels, Developer Console, Replay Debugger
- Sandboxes, scratch orgs, change sets, Salesforce CLI (`sf`), Metadata API, unlocked packages, VS Code

## Governor Limits Cheat Sheet (per transaction)

| Limit | Synchronous | Asynchronous |
|---|---|---|
| SOQL queries | 100 | 200 |
| Rows returned by SOQL | 50,000 | 50,000 |
| SOSL queries | 20 | 20 |
| SOSL rows returned | 2,000 | 2,000 |
| DML statements | 150 | 150 |
| Rows processed by DML | 10,000 | 10,000 |
| Callouts | 100 | 100 |
| CPU time | 10,000 ms | 60,000 ms |
| Heap size | 6 MB | 12 MB |
| Records in a trigger chunk | 200 | 200 |
| `@future` calls | 50 | 0 from future/batch |
| Queueable jobs enqueued | 50 | 1 |
| Batch default scope / max | 200 / 2,000 | |
| Visualforce view state | 170 KB | |

## Order of Execution (simplified)
1. System validation (required fields, field formats)
2. **Before-save record-triggered flows**
3. **Before triggers**
4. Custom validation rules
5. Duplicate rules
6. Record saved to DB (not committed)
7. **After triggers**
8. Assignment, auto-response, workflow rules (field updates may re-fire before/after update triggers)
9. Escalation rules
10. **After-save record-triggered flows**
11. Roll-up summary / cross-object updates
12. Commit to database
13. Post-commit logic (emails, async jobs)

## Trigger Context Variables Quick Table

| Variable | Insert | Update | Delete | Undelete |
|---|---|---|---|---|
| `Trigger.new` | ✔ | ✔ | ✘ | ✔ |
| `Trigger.old` | ✘ | ✔ | ✔ | ✘ |
| `Trigger.newMap` | after only | ✔ | ✘ | ✔ |
| `Trigger.oldMap` | ✘ | ✔ | ✔ | ✘ |
| `Trigger.new` editable | before only | before only | – | ✘ |

## LWC Lifecycle
`constructor` → `connectedCallback` → `render` → `renderedCallback` → (`disconnectedCallback` on removal) · `errorCallback` for child errors

---

# PART B – PRACTICE QUESTIONS (110)

*Choose ONE answer unless the question says "Choose 2". Answers and explanations are in Part C.*

## Section 1 – Developer Fundamentals

**Q1.** Why does Salesforce enforce governor limits on Apex code?
- A. To reduce licensing costs for customers
- B. To enforce MVC separation
- C. To make sure no single tenant monopolizes shared resources in a multitenant environment
- D. To limit the number of users that can log in

**Q2.** In the MVC pattern on the Salesforce platform, which item represents the **Model**?
- A. Objects, fields and relationships
- B. Visualforce pages
- C. Apex controllers
- D. Lightning components

**Q3.** A developer needs a roll-up summary field on Account that totals a field on a custom child object `Invoice__c`. Which relationship type is required?
- A. Lookup
- B. Hierarchical
- C. External lookup
- D. Master-detail

**Q4.** What is the standard way to model a many-to-many relationship between `Student__c` and `Course__c`?
- A. A single lookup field on each object
- B. A junction object with two master-detail relationships
- C. A hierarchical relationship on `Course__c`
- D. A formula field on `Student__c`

**Q5.** What happens to detail records when their master record in a master-detail relationship is deleted?
- A. They become orphaned
- B. Deletion of the master is blocked
- C. They are deleted as well
- D. They are re-parented to the record owner

**Q6.** *(Choose 2)* Which statements are true about a **lookup** relationship?
- A. The parent field can be left empty (optional)
- B. The child inherits sharing and ownership from the parent
- C. Deleting the parent always deletes the children
- D. It can be configured to prevent deletion of a parent that has children

**Q7.** A company needs to load 200,000 Account records. Which tool should be used?
- A. Data Import Wizard
- B. Data Loader
- C. Quick Create
- D. Report export

**Q8.** Which DML operation updates existing records and inserts new ones based on an External ID field?
- A. insert
- B. update
- C. merge
- D. upsert

**Q9.** Which keyword makes an Apex class respect the sharing rules of the running user?
- A. `with sharing`
- B. `without sharing`
- C. `global`
- D. `virtual`

**Q10.** Which keyword makes an Apex class use the sharing mode of the class that called it, defaulting to `with sharing` when it is the entry point?
- A. `with sharing`
- B. `without sharing`
- C. `inherited sharing`
- D. `global sharing`

**Q11.** *(Choose 2)* Apex runs in system mode by default. Which approaches enforce the running user's field/object permissions?
- A. The `with sharing` keyword
- B. `WITH USER_MODE` in SOQL
- C. `Security.stripInaccessible()`
- D. `@AuraEnabled` annotation

**Q12.** Which Apex collection stores an **unordered** group of **unique** elements?
- A. Set
- B. List
- C. Map
- D. Array

**Q13.** What does `myMap.get('missingKey')` return when the key is not in a `Map<String, Account>`?
- A. It throws a `MapException`
- B. An empty Account
- C. `false`
- D. `null`

**Q14.** What is the value of an uninitialized `Boolean` variable in Apex?
- A. `false`
- B. `null`
- C. `true`
- D. It causes a compile error

**Q15.** What must an Apex class be declared as in order to be extended by another class?
- A. `final`
- B. `static`
- C. `virtual` or `abstract`
- D. `global` only

**Q16.** A non-abstract class implements an interface. What is required?
- A. It must implement every method declared in the interface
- B. It must implement at least one method
- C. It must extend the interface
- D. It must be declared `abstract`

**Q17.** Which sharing setting defines the **baseline (most restrictive)** level of record access in an org?
- A. Sharing rules
- B. Permission sets
- C. Organization-Wide Defaults
- D. Manual sharing

**Q18.** Opportunity OWD is Private and "Grant Access Using Hierarchies" is enabled. A sales rep owns an opportunity. Who can see it by default (besides admins with "View All")?
- A. Peer reps in the same role
- B. The rep's managers above in the role hierarchy
- C. All internal users
- D. Only users in lower roles

**Q19.** Which requirement can **only** be implemented with Apex rather than declarative tools?
- A. Update a field when a record is created
- B. Prevent saving when a field is blank
- C. Calculate a value from two number fields
- D. Process 10 million records nightly in chunks

**Q20.** What do record types allow you to do?
- A. Offer different page layouts, picklist values and business processes to different users
- B. Create a new object
- C. Control field-level security
- D. Increase governor limits

**Q21.** Which sandbox type copies **all** production data and metadata?
- A. Developer
- B. Developer Pro
- C. Partial Copy
- D. Full

**Q22.** What is a key advantage of custom metadata types over custom objects for configuration?
- A. Records are data and cannot be deployed
- B. Records are metadata and can be deployed with change sets and packages
- C. They can only be used in Flow
- D. They have no API names

## Section 2 – Process Automation and Logic

**Q23.** Which trigger event should be used to modify fields on the record being saved **without** an extra DML statement?
- A. after insert
- B. after update
- C. before insert / before update
- D. after undelete

**Q24.** In which trigger context is `Trigger.newMap` **not** available?
- A. after insert
- B. before update
- C. after update
- D. before insert

**Q25.** *(Choose 2)* In which trigger events is `Trigger.old` available?
- A. before insert
- B. before update
- C. after insert
- D. before delete

**Q26.** What is the problem with this trigger?
```apex
trigger ContactTrigger on Contact (before insert) {
    for (Contact c : Trigger.new) {
        Account a = [SELECT Id, Name FROM Account WHERE Id = :c.AccountId];
        c.Description = a.Name;
    }
}
```
- A. It should be an after insert trigger
- B. The SOQL inside the loop will exceed the 100-query limit when 200 records are inserted
- C. Triggers cannot query Account
- D. `Trigger.new` is read-only

**Q27.** Which is the recommended trigger design pattern?
- A. One trigger per object that delegates to a handler class
- B. Multiple triggers per object, one per event
- C. All logic written inside the trigger body
- D. Triggers calling other triggers

**Q28.** How can recursion in a trigger be prevented?
- A. Use a `final` instance variable
- B. Disable the trigger in Setup
- C. Use a static Boolean variable in a helper class
- D. Use `@future`

**Q29.** What is the maximum number of records in a single chunk of `Trigger.new` for a bulk insert?
- A. 100
- B. 200
- C. 500
- D. 2,000

**Q30.** What is the synchronous SOQL query limit per transaction?
- A. 20
- B. 100
- C. 150
- D. 200

**Q31.** What is the limit on DML statements per transaction?
- A. 20
- B. 100
- C. 150
- D. 200

**Q32.** What is the maximum total number of records retrieved by SOQL queries in one transaction?
- A. 2,000
- B. 10,000
- C. 20,000
- D. 50,000

**Q33.** Which query correctly returns Accounts with their related Contacts?
- A. `SELECT Name, (SELECT LastName FROM Contacts) FROM Account`
- B. `SELECT Name, Contacts.LastName FROM Account`
- C. `SELECT Name FROM Account JOIN Contact`
- D. `SELECT Name, Contact__r.LastName FROM Account`

**Q34.** Which query returns each Contact's Account name?
- A. `SELECT Name, (SELECT Name FROM Accounts) FROM Contact`
- B. `SELECT Name, Accounts.Name FROM Contact`
- C. `SELECT Name, Account.Name FROM Contact`
- D. `SELECT Name, Contact.Account FROM Contact`

**Q35.** `SELECT Industry, COUNT(Id) cnt FROM Account GROUP BY Industry` returns a list of which type?
- A. `List<Account>`
- B. `List<Integer>`
- C. `Map<String, Integer>`
- D. `List<AggregateResult>`

**Q36.** Which is the correct way to use a variable in SOQL?
- A. `WHERE Name = {!acctName}`
- B. `WHERE Name = :acctName`
- C. `WHERE Name = @acctName`
- D. `WHERE Name = $acctName`

**Q37.** *(Choose 2)* How can SOQL injection be prevented in dynamic SOQL?
- A. Use bind variables
- B. Use `without sharing`
- C. Use `String.escapeSingleQuotes()`
- D. Wrap the query in `try/catch`

**Q38.** What does a SOSL query return in Apex?
- A. `List<SObject>`
- B. `Map<Id, SObject>`
- C. `List<List<SObject>>`
- D. `AggregateResult`

**Q39.** When is SOSL preferred over SOQL?
- A. Searching text across multiple objects at once
- B. Retrieving records by Id
- C. Aggregating numeric values
- D. Querying child records through subqueries

**Q40.** Which statement allows some records to succeed while others fail in a DML operation?
- A. `insert records;`
- B. `try { insert records; } catch`
- C. `upsert records;`
- D. `Database.insert(records, false);`

**Q41.** Which method undoes DML after a savepoint was set?
- A. `Database.undo(sp)`
- B. `Database.rollback(sp)`
- C. `System.revert(sp)`
- D. `Database.restore(sp)`

**Q42.** Which exception is thrown when `Account a = [SELECT Id FROM Account WHERE Name = 'NoSuchName'];` returns zero rows?
- A. `NullPointerException`
- B. `DmlException`
- C. `QueryException`
- D. `ListException`

**Q43.** Which declaration of a custom exception is valid?
- A. `public class MyAppException extends Exception {}`
- B. `public class MyApp implements Exception {}`
- C. `public class MyAppError extends Exception {}`
- D. `public exception MyAppException {}`

**Q44.** *(Choose 2)* Which are requirements of an `@future` method?
- A. It must be `static` and return `void`
- B. It can accept SObject parameters
- C. It can return a job Id
- D. Parameters must be primitives or collections of primitives

**Q45.** *(Choose 2)* What advantages does Queueable Apex have over `@future`?
- A. It runs synchronously
- B. It can use non-primitive types (e.g., SObjects) as member variables
- C. It returns a job Id that can be monitored
- D. It has no governor limits

**Q46.** Which interface must a batch class implement?
- A. `Schedulable`
- B. `Queueable`
- C. `Database.Batch`
- D. `Database.Batchable<SObject>`

**Q47.** What does `Database.Stateful` provide?
- A. Retains instance variable values across `execute` invocations
- B. Prevents rollback
- C. Allows callouts
- D. Increases scope size

**Q48.** What is the default batch size (scope) when `Database.executeBatch(job)` is called without a size?
- A. 100
- B. 50
- C. 200
- D. 2,000

**Q49.** A nightly job must process roughly 5 million records. Which approach is best?
- A. `@future` method
- B. Queueable Apex
- C. Batch Apex started by a Schedulable class
- D. A trigger

**Q50.** Which interface must be implemented to schedule Apex with `System.schedule`?
- A. `Database.Schedule`
- B. `Schedulable`
- C. `Runnable`
- D. `Queueable`

**Q51.** Which record-triggered flow type should be used to update fields on the triggering record most efficiently?
- A. After-save
- B. Screen flow
- C. Scheduled flow
- D. Before-save (fast field updates)

**Q52.** *(Choose 2)* Which are true about `@InvocableMethod`?
- A. The method must be `static`
- B. A class can contain several invocable methods
- C. The method can take a single non-list sObject parameter
- D. A class can contain only one invocable method

**Q53.** Which Apex class submits a record to an approval process programmatically?
- A. `Approval.ProcessSubmitRequest`
- B. `Approval.Submit`
- C. `ProcessInstance.Start`
- D. `Database.submitApproval`

**Q54.** Which runs first in the order of execution?
- A. Before triggers
- B. After triggers
- C. Before-save record-triggered flows
- D. After-save record-triggered flows

**Q55.** What is true about records in `Trigger.new` in an **after** trigger?
- A. Fields can be edited freely without DML
- B. They are read-only; changing them requires separate DML on a copy/query
- C. They are not yet in the database
- D. They are null

**Q56.** *(Choose 2)* Which are true about platform events?
- A. The API name ends with `__e`
- B. Events can be queried with SOQL after publishing
- C. They can be published with `EventBus.publish()`
- D. They cannot be subscribed to by Apex triggers

**Q57.** A method inserts a record and then makes an HTTP callout in the same transaction. What happens?
- A. It works normally
- B. Callout runs first automatically
- C. The DML is rolled back silently
- D. "You have uncommitted work pending" exception

**Q58.** What must be configured before Apex can make a callout to an external endpoint?
- A. A Connected App only
- B. A Named Credential or Remote Site Setting
- C. A custom permission
- D. A scheduled job

**Q59.** Which is valid Apex `switch` syntax?
- A. ```switch on status { when 'New' { x = 1; } when else { x = 0; } }```
- B. ```switch (status) { case 'New': x = 1; break; default: x = 0; }```
- C. ```switch status { 'New' -> x = 1; }```
- D. ```select status when 'New' then x = 1;```

**Q60.** What is printed?
```apex
Set<Integer> s = new Set<Integer>{1, 2, 2, 3};
System.debug(s.size());
```
- A. 2
- B. 4
- C. 3
- D. Compile error

**Q61.** What is an efficient way to get a `Map<Id, Account>` from a query?
- A. `new Map<Id, Account>([SELECT Id, Name FROM Account])`
- B. `Map<Id, Account> m = [SELECT Id FROM Account].toMap();`
- C. `Map.fromQuery(...)`
- D. A for loop is the only way

**Q62.** Which method executes a SOQL string built at runtime?
- A. `Database.runQuery()`
- B. `Schema.query()`
- C. `Database.query()`
- D. `System.soql()`

**Q63.** Where should translatable user-facing text be stored for use in Apex, Visualforce, Aura and LWC?
- A. Custom Settings
- B. Custom Labels
- C. Static Resources
- D. Custom Metadata

**Q64.** Which method returns the number of SOQL queries issued so far in the transaction?
- A. `System.getQueries()`
- B. `Limits.getLimitQueries()`
- C. `Database.countQueries()`
- D. `Limits.getQueries()`

## Section 3 – User Interface

**Q65.** Which decorator exposes a property of an LWC as public so a parent can set it?
- A. `@track`
- B. `@api`
- C. `@wire`
- D. `@public`

**Q66.** When is `@track` still needed in modern LWC?
- A. For every reactive property
- B. To expose a property publicly
- C. To observe mutations inside object properties or array elements
- D. It has been removed

**Q67.** Where is the best place to perform initial setup (such as an imperative Apex call) when the component is inserted into the DOM?
- A. `connectedCallback()`
- B. `constructor()`
- C. `renderedCallback()` only
- D. `disconnectedCallback()`

**Q68.** How does a child LWC communicate with its parent?
- A. Call the parent's methods directly
- B. Set an `@api` property on the parent
- C. Use Lightning Message Service only
- D. Dispatch a `CustomEvent`

**Q69.** A child dispatches `new CustomEvent('selectitem')`. How does the parent listen?
- A. `<c-child on-selectitem={handle}>`
- B. `<c-child onSelectItem={handle}>`
- C. `<c-child onselectitem={handle}>`
- D. `<c-child event="selectitem">`

**Q70.** What is required for an Apex method to be used with `@wire`?
- A. `@future`
- B. `@AuraEnabled(cacheable=true)`
- C. `@InvocableMethod`
- D. `global` access

**Q71.** When must an LWC call Apex imperatively instead of using `@wire`?
- A. When the method performs DML (non-cacheable)
- B. When reading a record
- C. When using a record page
- D. Never; `@wire` always works

**Q72.** Which approach lets you display and edit a record with built-in FLS/CRUD enforcement and no Apex?
- A. A custom Visualforce page
- B. `@wire` with custom SOQL
- C. An Apex controller with `without sharing`
- D. `lightning-record-form` / `lightning-record-edit-form` (Lightning Data Service)

**Q73.** *(Choose 2)* To make an LWC available on a record page and access the current record Id:
- A. Set `isExposed` to true and add the `lightning__RecordPage` target in the meta XML
- B. Wrap it in an Aura component
- C. Declare `@api recordId`
- D. Create a Visualforce page

**Q74.** What must each element in an iterated template have when using `for:each`?
- A. An `id` attribute
- B. A unique `key` directive
- C. A `name` attribute
- D. Nothing

**Q75.** Which is the current recommended conditional rendering directive in LWC templates?
- A. `<template if:true>`
- B. `<template rendered>`
- C. `<template lwc:if={condition}>`
- D. `<template when>`

**Q76.** Which is used to navigate (e.g., to a record page) from an LWC?
- A. `NavigationMixin`
- B. `window.location.href` only
- C. `$A.navigate`
- D. `PageReference` Apex class

**Q77.** Which class displays a toast message from an LWC?
- A. `ToastEvent` from `lightning/toast`
- B. `$A.get('e.force:showToast')`
- C. `alert()`
- D. `ShowToastEvent` from `lightning/platformShowToastEvent`

**Q78.** Which statement about Aura and LWC interoperability is correct?
- A. Aura can be nested in LWC
- B. LWC can be used inside Aura components, but Aura cannot be used inside LWC
- C. They cannot be combined
- D. Both can nest each other freely

**Q79.** Which framework is used to unit-test LWCs?
- A. Apex Test
- B. Selenium
- C. Jest
- D. JUnit

**Q80.** How do you select an element in the component's own template from JavaScript?
- A. `this.template.querySelector('.cls')`
- B. `document.querySelector('.cls')`
- C. `$('.cls')`
- D. `this.querySelectorAll` only

**Q81.** How are custom labels imported in an LWC?
- A. `import LABEL from '@salesforce/customLabel/Welcome'`
- B. `import LABEL from 'c/Welcome'`
- C. `{!$Label.Welcome}`
- D. `import LABEL from '@salesforce/label/c.Welcome'`

**Q82.** Which `apex:page` attribute uses the standard controller for Account?
- A. `controller="Account"`
- B. `standardController="Account"`
- C. `extensions="Account"`
- D. `object="Account"`

**Q83.** Which attribute enables a standard list controller on a Visualforce page?
- A. `listVar`
- B. `collection`
- C. `recordSetVar`
- D. `setController`

**Q84.** Which keyword reduces Visualforce view state size in a controller?
- A. `transient`
- B. `static`
- C. `final`
- D. `private`

**Q85.** Which tag must wrap an `apex:commandButton` for it to submit data?
- A. `apex:block`
- B. `apex:actionRegion`
- C. `apex:pageBlock`
- D. `apex:form`

**Q86.** What parameter type does the constructor of a Visualforce controller extension receive?
- A. `ApexPages.Controller`
- B. `ApexPages.StandardController`
- C. `PageReference`
- D. `SObject`

**Q87.** Which target must an LWC declare to be used on a Flow screen?
- A. `lightning__AppPage`
- B. `lightning__HomePage`
- C. `lightning__FlowScreen`
- D. `lightning__Tab`

**Q88.** Which method signature works with `@wire`?
- A. `@AuraEnabled(cacheable=true) public static List<Account> getAccounts()`
- B. `@AuraEnabled public List<Account> getAccounts()`
- C. `@future public static void getAccounts()`
- D. `@InvocableMethod public static List<Account> getAccounts()`

## Section 4 – Testing, Debugging, and Deployment

**Q89.** What is the minimum overall Apex code coverage required to deploy to production?
- A. 50%
- B. 65%
- C. 75%
- D. 100%

**Q90.** *(Choose 2)* Which are true for deploying Apex to production?
- A. Overall coverage must be at least 75%
- B. Every class needs 100% coverage
- C. Every trigger must have some test coverage
- D. Tests are not required if deployed from a sandbox

**Q91.** Which annotation marks a class or method as a test and excludes it from the org's Apex code size limit?
- A. `@test`
- B. `@isTest`
- C. `@testMethod`
- D. `@unitTest`

**Q92.** What is the purpose of `Test.startTest()` and `Test.stopTest()`?
- A. Provide fresh governor limits for the code between them and force async code to complete at `stopTest`
- B. Start and stop the debug log
- C. Commit test data
- D. Switch to production data

**Q93.** What does `@testSetup` do?
- A. Runs before every test method
- B. Creates data that persists after tests
- C. Creates data once per class, available (with rollback per method) to all test methods
- D. Defines mock responses

**Q94.** What is the default for `SeeAllData` and the recommended practice?
- A. `true`; always use org data
- B. `true`; only in managed packages
- C. `false`; use it for all tests
- D. `false`; create your own test data

**Q95.** What does `System.runAs(user)` allow?
- A. Bypassing validation rules
- B. Testing code in the context of a specific user (sharing and permissions)
- C. Skipping code coverage
- D. Executing callouts

**Q96.** Why are HTTP callouts mocked in test classes?
- A. To save API calls only
- B. Because callouts are not allowed in tests, so `Test.setMock(HttpCalloutMock.class, mock)` provides a fake response
- C. To enable `SeeAllData`
- D. To increase coverage automatically

**Q97.** Which is the modern assertion style in Apex tests?
- A. `System.assert(true)` only
- B. `assertEquals()` in the trigger
- C. `Assert.areEqual(expected, actual, 'message')`
- D. `Test.assert()`

**Q98.** What is the best practice for creating test records across multiple test classes?
- A. Copy the data code in every class
- B. Use `SeeAllData=true`
- C. Query production data
- D. A reusable `@isTest` test data factory class

**Q99.** How do you capture debug logs for a specific user?
- A. Create a trace flag for the user (Debug Logs in Setup or Developer Console)
- B. Enable "Debug Mode" in the profile
- C. Use `System.log()`
- D. Email logs from the Apex job

**Q100.** What log level does `System.debug('msg')` use if none is specified?
- A. INFO
- B. DEBUG
- C. ERROR
- D. FINE

**Q101.** Which statement about change sets is correct?
- A. They deploy between any two orgs
- B. They support all metadata types
- C. They deploy only between orgs that have a deployment connection (e.g., production and its sandboxes)
- D. They are used for scratch orgs

**Q102.** *(Choose 2)* Which statements about scratch orgs are true?
- A. They are created from a configuration (definition) file
- B. They are permanent
- C. They are full copies of production
- D. They are temporary (1–30 days)

**Q103.** Where can Apex code be edited directly?
- A. Production via Developer Console
- B. Sandbox, Developer Edition or scratch orgs
- C. Any org
- D. Only with a change set

**Q104.** Which Salesforce CLI command deploys source to an org?
- A. `sf apex run`
- B. `sf org create scratch`
- C. `sf project deploy start`
- D. `sf data query`

**Q105.** What is the refresh interval for a Full sandbox?
- A. 1 day
- B. 5 days
- C. 14 days
- D. 29 days

**Q106.** Which tool lets you step through a previously recorded debug log in VS Code?
- A. Apex Replay Debugger
- B. Workbench
- C. Org Browser
- D. Query Editor

**Q107.** How should you test that a method throws an expected exception?
- A. Ignore exceptions in tests
- B. Use `try/catch` and assert on the caught exception (and fail if none is thrown)
- C. Use `@isTest(expectException=true)`
- D. Use `Test.expectError()`

**Q108.** Why should triggers be tested with 200 records?
- A. To meet the 75% rule
- B. To make the test run async
- C. To verify the code is bulk-safe within governor limits
- D. To enable `SeeAllData`

**Q109.** What happens to data created in a test method after it finishes?
- A. It is rolled back and never committed
- B. It is committed to the database
- C. It is saved for the next test
- D. It is emailed to the admin

**Q110.** Where in Setup can you run all or selected Apex tests in the org?
- A. Apex Jobs
- B. Apex Classes → Compile
- C. Debug Logs
- D. Apex Test Execution

---

# PART C – ANSWERS & EXPLANATIONS

## Section 1 – Developer Fundamentals
- **Q1 – C.** Governor limits protect shared resources in a multitenant architecture so no single tenant can consume excess CPU, memory or DB resources.
- **Q2 – A.** Model = data structure (objects/fields/relationships). View = VF/LWC/Aura. Controller = Apex/Flow logic.
- **Q3 – D.** Roll-up summary fields exist only on the master side of a master-detail relationship (lookups need Flow/Apex or declarative lookup rollups).
- **Q4 – B.** A junction object has two master-detail relationships (one to each parent object).
- **Q5 – C.** Detail records are cascade-deleted (and restored if the master is undeleted).
- **Q6 – A, D.** Lookups are optional by default and can be set to block deletion of the parent. Inheriting sharing/ownership and cascade delete are master-detail traits.
- **Q7 – B.** Import Wizard is limited to 50,000 records and certain objects; Data Loader handles large volumes (up to millions) and all objects.
- **Q8 – D.** `upsert` matches on Id or an External ID field to update or insert.
- **Q9 – A.** `with sharing` enforces the sharing rules of the current user (it does not enforce FLS/CRUD).
- **Q10 – C.** `inherited sharing` uses the caller's sharing mode; as an entry point it defaults to `with sharing`, which is safer.
- **Q11 – B, C.** `WITH USER_MODE` (and `WITH SECURITY_ENFORCED`) and `Security.stripInaccessible()` enforce FLS/CRUD. `with sharing` only handles record sharing.
- **Q12 – A.** Sets store unique, unordered values.
- **Q13 – D.** `Map.get()` returns `null` for a missing key.
- **Q14 – B.** Uninitialized variables (including Boolean) are `null`, so always initialize Booleans.
- **Q15 – C.** Classes must be `virtual` or `abstract` to be extended.
- **Q16 – A.** A concrete class must implement all interface methods (only abstract classes may skip some).
- **Q17 – C.** OWD sets the baseline; other tools (role hierarchy, sharing rules, manual sharing) can only open access further.
- **Q18 – B.** With hierarchies enabled, users above the owner in the role hierarchy get access. Peers do not.
- **Q19 – D.** Batch processing of millions of records needs Batch Apex. The others are possible via formula, validation rule or Flow.
- **Q20 – A.** Record types control page layouts, picklist values and business processes (not FLS).
- **Q21 – D.** Full sandboxes copy all data and metadata. Partial copies a sample; Developer/Dev Pro copy metadata only.
- **Q22 – B.** Custom metadata records are metadata, deployable and packageable, ideal for configuration.

## Section 2 – Process Automation and Logic
- **Q23 – C.** Before triggers can change `Trigger.new` fields with no extra DML.
- **Q24 – D.** In before insert, records have no Id yet, so there is no `newMap`.
- **Q25 – B, D.** `Trigger.old` exists for update and delete events only.
- **Q26 – B.** SOQL in a loop is not bulk-safe. A 200-record batch causes 200 queries (limit 100). Collect AccountIds in a Set, query once into a Map, then loop.
- **Q27 – A.** One trigger per object plus a handler class gives predictable ordering and testable logic.
- **Q28 – C.** A static variable persists for the transaction, so it can act as a recursion guard.
- **Q29 – B.** Triggers process in chunks of 200.
- **Q30 – B.** 100 synchronous (200 asynchronous).
- **Q31 – C.** 150 DML statements per transaction.
- **Q32 – D.** 50,000 total SOQL rows (DML rows limit is 10,000).
- **Q33 – A.** Parent-to-child uses a subquery with the child relationship name (plural, e.g. `Contacts`).
- **Q34 – C.** Child-to-parent uses dot notation with the relationship name (`Account.Name`).
- **Q35 – D.** Aggregate queries return `List<AggregateResult>`.
- **Q36 – B.** Bind variables use a colon prefix: `:variable`.
- **Q37 – A, C.** Bind variables and `String.escapeSingleQuotes()` sanitize input.
- **Q38 – C.** SOSL returns a list of lists of SObjects, one inner list per object in the `RETURNING` clause.
- **Q39 – A.** SOSL is for text searches across multiple objects and fields; SOQL is for precise queries on one object and its relationships.
- **Q40 – D.** `Database.insert(list, false)` allows partial success; the result array shows failures.
- **Q41 – B.** `Database.setSavepoint()` then `Database.rollback(sp)`.
- **Q42 – C.** Assigning a query returning 0 (or more than 1) rows to a single SObject throws `QueryException`. Use a List to avoid that.
- **Q43 – A.** Custom exceptions extend `Exception` and the class name must end with "Exception".
- **Q44 – A, D.** `@future` methods are static, return void and take only primitive/collection-of-primitive parameters.
- **Q45 – B, C.** Queueable supports complex member variables, returns a job Id, and allows job chaining.
- **Q46 – D.** `Database.Batchable<SObject>` with `start`, `execute` and `finish`.
- **Q47 – A.** `Database.Stateful` keeps member variable state across batch chunks (otherwise state resets each `execute`).
- **Q48 – C.** The default batch size is 200 (max 2,000).
- **Q49 – C.** Batch Apex handles up to 50 million records via QueryLocator, and Schedulable runs it nightly.
- **Q50 – B.** Implement `Schedulable` and call `System.schedule(name, cronExpression, instance)`.
- **Q51 – D.** Before-save flows update the triggering record without extra DML, so they are the fastest.
- **Q52 – A, D.** Invocable methods must be static and each class may have only one; the parameter must be a List.
- **Q53 – A.** `Approval.ProcessSubmitRequest` plus `Approval.process()`.
- **Q54 – C.** After system validation, before-save flows run, then before triggers.
- **Q55 – B.** In after triggers, `Trigger.new` is read-only; query or copy records and perform DML to change them.
- **Q56 – A, C.** Platform events end with `__e` and are published via `EventBus.publish()` (or insert DML). They cannot be queried with SOQL and can be subscribed to with Apex triggers.
- **Q57 – D.** Performing DML before a callout causes "uncommitted work pending". Do callouts first or use async.
- **Q58 – B.** The endpoint must be authorized with a Named Credential or Remote Site Setting.
- **Q59 – A.** Apex uses `switch on <expr> { when <value> {...} when else {...} }`.
- **Q60 – C.** A Set removes duplicates, so the size is 3.
- **Q61 – A.** The Map constructor accepting a query result builds an Id-keyed map in one step.
- **Q62 – C.** `Database.query(String)` runs dynamic SOQL.
- **Q63 – B.** Custom Labels hold translatable text usable in Apex, VF, Aura and LWC.
- **Q64 – D.** `Limits.getQueries()` is the count used, and `Limits.getLimitQueries()` is the maximum.

## Section 3 – User Interface
- **Q65 – B.** `@api` makes properties/methods public.
- **Q66 – C.** All fields are reactive for primitives; `@track` is needed only to observe internal mutation of objects or arrays.
- **Q67 – A.** `connectedCallback` fires when the component is inserted into the DOM; the template isn't rendered yet.
- **Q68 – D.** Children dispatch `CustomEvent`s and parents handle them.
- **Q69 – C.** Handler attribute is `on` + event name in lowercase: `onselectitem`.
- **Q70 – B.** `@wire` requires `@AuraEnabled(cacheable=true)` on a static method.
- **Q71 – A.** Methods that perform DML cannot be cacheable, so call them imperatively.
- **Q72 – D.** LDS-based components handle CRUD/FLS and caching with no Apex.
- **Q73 – A, C.** Add the target and `isExposed` in the meta XML, and expose `@api recordId`.
- **Q74 – B.** Each iterated item needs a unique `key`.
- **Q75 – C.** `lwc:if`, `lwc:elseif`, `lwc:else` are the current directives (`if:true`/`if:false` are older and still seen).
- **Q76 – A.** Use `NavigationMixin` with a PageReference object.
- **Q77 – D.** `ShowToastEvent` from `lightning/platformShowToastEvent`.
- **Q78 – B.** LWC can be composed in Aura, but Aura can't live inside LWC.
- **Q79 – C.** Jest (via `@salesforce/sfdx-lwc-jest`).
- **Q80 – A.** Because of shadow DOM, use `this.template.querySelector`.
- **Q81 – D.** Pattern is `@salesforce/label/c.LabelName`.
- **Q82 – B.** `<apex:page standardController="Account">`.
- **Q83 – C.** `recordSetVar` turns the standard controller into a list controller.
- **Q84 – A.** `transient` variables aren't stored in view state (limit 170 KB).
- **Q85 – D.** Command buttons/links must be inside `apex:form`.
- **Q86 – B.** Extension constructors take `ApexPages.StandardController` (or a custom controller instance).
- **Q87 – C.** `lightning__FlowScreen` target.
- **Q88 – A.** Must be static and `@AuraEnabled(cacheable=true)`.

## Section 4 – Testing, Debugging, and Deployment
- **Q89 – C.** 75% overall Apex coverage required.
- **Q90 – A, C.** 75% overall, every trigger needs some coverage, and all tests that run must pass. 100% per class is not required.
- **Q91 – B.** `@isTest` (test code doesn't count toward the org's Apex size limit).
- **Q92 – A.** `startTest`/`stopTest` give a fresh set of limits and run asynchronous code at `stopTest`.
- **Q93 – C.** `@testSetup` runs once; every test method sees a fresh rolled-back copy of that data.
- **Q94 – D.** `SeeAllData` defaults to false; create your own test data.
- **Q95 – B.** `System.runAs` tests sharing and permissions in another user's context.
- **Q96 – B.** Real callouts aren't made in tests; use `HttpCalloutMock` with `Test.setMock`.
- **Q97 – C.** The `Assert` class (`Assert.areEqual`, `isTrue`, etc.) is the modern approach; `System.assertEquals` still works.
- **Q98 – D.** A reusable `@isTest` factory class keeps test data DRY.
- **Q99 – A.** Create a trace flag with a debug level for the user.
- **Q100 – B.** Default is DEBUG. Levels: NONE, ERROR, WARN, INFO, DEBUG, FINE, FINER, FINEST.
- **Q101 – C.** Change sets need a deployment connection between related orgs.
- **Q102 – A, D.** Scratch orgs are defined by a config file and are temporary (default 7 days, max 30).
- **Q103 – B.** Apex can't be edited directly in production; edit in sandbox/dev/scratch and deploy.
- **Q104 – C.** `sf project deploy start` (formerly `sfdx force:source:deploy`).
- **Q105 – D.** Full sandbox refresh is every 29 days. (Developer/Dev Pro: 1 day, Partial: 5 days.)
- **Q106 – A.** The Apex Replay Debugger replays a debug log in VS Code.
- **Q107 – B.** Wrap the call in try/catch and assert on the exception; fail if none was thrown.
- **Q108 – C.** Triggers must handle bulk; testing 200 records confirms no limits are hit.
- **Q109 – A.** Test data is rolled back automatically and never committed.
- **Q110 – D.** Setup → Apex Test Execution (or Developer Console → Test → New Run).

---

# PART D – LAST-WEEK STUDY PLAN

| Day | Focus |
|---|---|
| 1 | Data model, security, Fundamentals questions (Q1–Q22) |
| 2 | Triggers, order of execution, SOQL/SOSL/DML (Q23–Q43) |
| 3 | Async Apex, flows, callouts, platform events (Q44–Q64) |
| 4 | LWC deep dive and Visualforce (Q65–Q88) |
| 5 | Testing, debugging, deployment (Q89–Q110) |
| 6 | Redo every question you missed; build a quick LWC and a trigger with tests in a Dev org |
| 7 | Light revision of cheat sheet (limits, order of execution, trigger variables); rest |

**Exam tips**
- Flag and skip long code questions, then return to them.
- Eliminate wrong options first. Questions often hinge on a single word like *bulk*, *declarative*, *cacheable* or *before*.
- Prefer declarative (Flow/validation rule) when the question says "most efficient / least code"; prefer Apex when it mentions complex logic, bulk volume or callouts.
- Watch for "Choose 2" and "Choose 3" instructions.

Good luck! 🍀
