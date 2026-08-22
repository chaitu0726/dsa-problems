# Salesforce Platform Developer I (PD1) — Practice Exam
### Original Practice Questions (No Answers)

> These are original practice questions written to match the official PD1 exam objectives (Salesforce Fundamentals, Data Modeling & Management, Logic & Process Automation, User Interface, Testing/Debugging/Deployment). They are **not** real exam questions.

---

## Section 1: Salesforce Fundamentals

**Q1.** Which statement about the Salesforce multi-tenant architecture is correct?

A. Each customer org runs on a dedicated physical server
B. All customers share the same application and database architecture, with data logically separated
C. Multi-tenancy only applies to Salesforce sandboxes
D. Multi-tenant orgs cannot use custom objects

**Q2.** Which two are true about Salesforce sandboxes? (Choose 2)

A. A Developer sandbox copies all production data
B. A Full sandbox copies both metadata and data from production
C. Sandboxes are refreshed on a schedule determined by sandbox type
D. All sandbox types have the same storage limits as production

**Q3.** Which declarative tool would you use to automatically update a field on a related record when a record is saved, without writing code?

A. Visualforce Controller
B. Flow
C. Apex Trigger
D. Apex Batch Class

**Q4.** What is the primary difference between a role hierarchy and a sharing rule?

A. Role hierarchy grants access downward automatically; sharing rules extend access to specific users/groups beyond org-wide defaults
B. Sharing rules restrict access; role hierarchy has no effect on access
C. Role hierarchy only applies to custom objects
D. There is no difference — they accomplish the same thing

**Q5.** Which org-wide default (OWD) setting would require the use of sharing rules or Apex managed sharing to grant broader access?

A. Public Read/Write
B. Private
C. Controlled by Parent
D. Public Full Access

**Q6.** A developer needs to know which edition-specific limits apply to their org. Where should they look?

A. Setup → Company Information and Salesforce Limits page
B. The Apex Developer Guide only
C. Governor limits documentation only
D. There is no way to check org-specific limits

**Q7.** Which of the following is NOT one of the core Salesforce platform metadata types?

A. Custom Object
B. Custom Field
C. Docker Container
D. Validation Rule

---

## Section 2: Data Modeling and Management

**Q8.** What happens to child records in a Master-Detail relationship when the master record is deleted?

A. Nothing happens to child records
B. Child records are automatically deleted
C. Child records become orphaned and read-only
D. The delete operation is blocked automatically

**Q9.** Which relationship type allows a child record to exist without a parent record?

A. Master-Detail
B. Lookup
C. Both A and B behave identically
D. Junction Object only

**Q10.** You need to create a many-to-many relationship between Students and Courses. What is the recommended approach?

A. Create two separate Master-Detail relationships on the Student object
B. Create a junction object with two Master-Detail (or Lookup) relationships, one to each object
C. Use a single Lookup field on both objects
D. Many-to-many relationships are not supported on the Salesforce platform

**Q11.** Which statement about roll-up summary fields is TRUE?

A. Roll-up summary fields can be created on a Lookup relationship
B. Roll-up summary fields can only be created on the master side of a Master-Detail relationship, summarizing values from the detail side
C. Roll-up summary fields require Apex code
D. Roll-up summary fields can summarize data across unrelated objects

**Q12.** Which tool would you use to import up to 5 million records into Salesforce, including complex mapping and scheduling?

A. Data Import Wizard
B. Data Loader
C. Report Export
D. Change Sets

**Q13.** What is the maximum number of Master-Detail relationships a single custom object can have?

A. 1
B. 2
C. Unlimited
D. It depends on the org's edition only

**Q14.** External IDs are primarily useful for:

A. Encrypting sensitive fields
B. Upserting records and matching data during integration/import
C. Enforcing field-level security
D. Creating roll-up summary fields

---

## Section 3: Logic and Process Automation (Apex, Triggers, Flow)

**Q15.** What is the correct trigger syntax to handle both insert and update events on the Account object?

A. `trigger AccTrigger on Account (before insert, before update) { }`
B. `trigger AccTrigger on Account (before insert or before update) { }`
C. `trigger AccTrigger insert update on Account { }`
D. `trigger AccTrigger on Account.insert.update { }`

**Q16.** In a `before update` trigger, which context variable(s) let you compare old and new values of a record?

A. `Trigger.new` only
B. `Trigger.old` and `Trigger.new`
C. `Trigger.newMap` only
D. Comparison of old/new values is not possible in triggers

**Q17.** Why should SOQL queries never be placed inside a `for` loop in Apex?

A. SOQL queries inside loops run faster
B. It risks hitting the governor limit on total SOQL queries per transaction
C. Salesforce automatically blocks all loops containing SOQL
D. It has no real performance impact

**Q18.** Which is the correct way to bulkify a trigger that updates related Contact records when Accounts are updated?

A. Query Contacts inside the loop for each Account
B. Collect Account Ids in a Set, query related Contacts once outside the loop, then perform bulk DML
C. Use a `future` method for every single record
D. Call `update` inside the loop for each Contact individually

**Q19.** What does the `with sharing` keyword do on an Apex class?

A. Enforces the running user's record-level sharing rules for that class's operations
B. Grants the class access to all records regardless of sharing settings
C. Shares the Apex class's source code with other orgs
D. Has no effect on record access

**Q20.** Which Apex feature would you use to process 500,000 records in chunks while respecting governor limits?

A. `@future` method
B. Batch Apex
C. A single anonymous Apex script
D. A Visualforce page controller

**Q21.** What is the purpose of the `Database.executeBatch()` method?

A. To execute SOQL queries synchronously
B. To start execution of a Batch Apex class asynchronously
C. To roll back a DML transaction
D. To schedule a Flow

**Q22.** Which annotation allows an Apex class to run on a time-based schedule (e.g., nightly)?

A. `@future`
B. `@InvocableMethod`
C. Implementing the `Schedulable` interface with `System.schedule()`
D. `@RestResource`

**Q23.** What is a key limitation of `@future` methods?

A. They can accept sObjects as parameters directly
B. Parameters must be primitive data types, collections of primitives, or IDs — not sObjects
C. They run synchronously
D. They can only be called from a Visualforce page

**Q24.** In Process Builder / Flow, what is the recommended way to avoid recursive execution when a Flow updates a record that triggers itself again?

A. Add a formula/decision check for whether the specific update has already been applied, or use appropriate entry conditions
B. Recursion is not possible in Flow
C. Disable all Flows on that object
D. Use only Screen Flows

**Q25.** Which of the following correctly describes order of execution for a record save in Salesforce (partial, simplified)?

A. Workflow rules run before validation rules
B. System validation rules and before triggers execute prior to duplicate rules
C. Before triggers execute, then system validation rules, then after triggers, then assignment rules, and finally workflow rules (in a generalized simplified order)
D. After triggers always run before before triggers

**Q26.** What happens if an unhandled exception occurs inside a trigger during a DML operation on a batch of records?

A. Only the failing record is skipped; all others are always committed automatically
B. The entire operation for that transaction can be rolled back, depending on how DML is performed (all-or-none vs partial)
C. Salesforce silently ignores the exception
D. Exceptions in triggers are impossible

**Q27.** Which statement about Apex Queueable is TRUE compared to `@future`?

A. Queueable Apex supports complex parameter types like sObjects and custom classes, unlike `@future`
B. Queueable Apex cannot be chained
C. Queueable Apex is always synchronous
D. Queueable Apex has no governor limits

**Q28.** What is the correct way to define a custom exception class in Apex?

A. `public class MyException implements Exception { }`
B. `public class MyException extends Exception { }`
C. `public exception class MyException { }`
D. Custom exceptions are not supported in Apex

**Q29.** A validation rule should prevent saving an Opportunity if the CloseDate is in the past. Which formula fits in the rule's error condition?

A. `CloseDate < TODAY()`
B. `CloseDate > TODAY()`
C. `ISBLANK(CloseDate)`
D. `CloseDate = TODAY()`

---

## Section 4: User Interface

**Q30.** Which technology would you use to build a highly customizable, reusable UI component using JavaScript and HTML that runs natively on the Lightning platform?

A. Visualforce
B. Lightning Web Components (LWC)
C. Apex Batch
D. SOSL

**Q31.** What decorator in a Lightning Web Component marks a property as reactive and exposable to a parent component?

A. `@track`
B. `@api`
C. `@wire`
D. `@AuraEnabled`

**Q32.** Which decorator is used in LWC to call an Apex method and automatically re-run it when reactive parameters change?

A. `@api`
B. `@wire`
C. `@track`
D. `@RestResource`

**Q33.** In a Visualforce page, which tag would you use to display a related list of Contacts on an Account detail page using standard controller data?

A. `<apex:pageBlockTable>` bound to `Account.Contacts`
B. `<apex:include>`
C. `<apex:variable>`
D. `<apex:outputPanel>` alone with no binding

**Q34.** What must an Apex method have to be called from a Lightning Web Component?

A. It must be `private` and non-static
B. It must be `public` (or `global`) and annotated with `@AuraEnabled`, typically `static`
C. It must return `void` only
D. It must be inside a trigger

---

## Section 5: Testing, Debugging, and Deployment

**Q35.** What is the minimum code coverage percentage required org-wide to deploy Apex to production?

A. 50%
B. 65%
C. 75%
D. 90%

**Q36.** Which annotation marks a class as containing only test methods?

A. `@isTest`
B. `@TestVisible`
C. `@Deprecated`
D. `@Future`

**Q37.** What is the purpose of `Test.startTest()` and `Test.stopTest()` in a test class?

A. They are optional and have no functional purpose
B. They reset governor limits within the test and force asynchronous operations (like `@future`, batch, queueable) enclosed between them to execute synchronously before `stopTest()` completes
C. They delete all test data automatically
D. They disable triggers during the test

**Q38.** Why should test classes use `@testSetup` or create their own test data rather than referencing existing org data?

A. Because tests run in an isolated data context and cannot see real org records (except some setup/config data) by default
B. Because using existing data is faster
C. Because Salesforce requires production data in every test
D. There is no reason; either approach is identical

**Q39.** Which statement about `System.assert` methods in test classes is TRUE?

A. They are unnecessary if code coverage is above 75%
B. They actually validate expected outcomes, and are essential for meaningful (not just coverage-driven) tests
C. They can only be used in production code, not tests
D. They automatically fix failing tests

**Q40.** Which of these is a valid way to move metadata from a sandbox to production?

A. Change Sets
B. Salesforce CLI / SFDX source deployment with a scratch org or package
C. Metadata API-based deployment tools (e.g., Workbench, ANT Migration Tool)
D. All of the above

**Q41.** What does the Salesforce CLI command `sf project deploy start` (or legacy `sfdx force:source:deploy`) primarily do?

A. Deletes metadata from the target org
B. Deploys local source/metadata to a connected Salesforce org
C. Only runs Apex tests without deploying anything
D. Creates a new sandbox

**Q42.** When debugging Apex, which tool lets you view detailed execution logs including SOQL, DML, and governor limit consumption?

A. Setup → Debug Logs
B. Data Loader
C. Report Builder
D. Process Builder

**Q43.** What happens if a required test class doesn't achieve at least 75% overall coverage when deploying to production?

A. The deployment succeeds anyway with a warning
B. The deployment is blocked/fails
C. Salesforce automatically writes additional test methods
D. Coverage requirements only apply to sandboxes

---

## Section 6: Mixed / Scenario-Based

**Q44.** A developer writes a trigger that queries related records for every record in `Trigger.new` individually inside a loop. On a bulk data load of 300 records, what is the most likely outcome?

A. No issue; Salesforce optimizes this automatically
B. A `System.LimitException: Too many SOQL queries` error
C. The trigger silently skips extra records
D. It will only affect UI-based single record saves, never bulk operations

**Q45.** Which of the following best describes "bulkification" in the context of Apex development?

A. Writing code that is designed to efficiently handle multiple records at once, rather than assuming only one record will be processed
B. Making code run only for a single record for simplicity
C. Increasing a specific governor limit manually
D. A feature exclusive to Batch Apex

**Q46.** A recursive trigger keeps re-firing when Account records are updated by the trigger itself. Which pattern helps prevent this?

A. A static Boolean flag in a helper class to track whether the trigger logic has already executed in the current transaction
B. Deleting the trigger entirely
C. Wrapping the whole trigger body in a try/catch with no other logic
D. Recursion cannot occur in Apex triggers

**Q47.** Which statement correctly differentiates a Custom Setting from a Custom Metadata Type?

A. Custom Settings and Custom Metadata Types are identical in every way
B. Custom Metadata Type records are metadata (deployable across environments and readable without SOQL query limits in some contexts); Custom Settings store org- or profile-specific configuration data, some cached for fast access
C. Custom Settings can never be hierarchical
D. Custom Metadata Types cannot be created declaratively

**Q48.** Which statement best describes the relationship between Profiles and Permission Sets?

A. Permission Sets replace Profiles entirely and Profiles can be ignored
B. Every user must have exactly one Profile, and Permission Sets are used to grant additional permissions/access on top of that Profile without changing it
C. A user can only have one Permission Set assigned, ever
D. Profiles can only restrict access, never grant it

**Q49.** What is the correct governor limit for SOQL queries in a synchronous Apex transaction?

A. 50
B. 100
C. 150
D. 200

**Q50.** Which best practice should be followed when writing SOQL in Apex to avoid hitting the heap size or row limits on large data sets?

A. Query all fields using `SELECT *` for simplicity
B. Query only the specific fields and records actually needed, and use selective filters
C. Always query without a `WHERE` clause to be safe
D. Store the entire object's records in a `Map<Id, SObject>` regardless of size

---

*End of practice questions. See the companion file "PD1_Practice_Exam_Solutions.md" for answers and explanations.*
