# Salesforce Platform Developer I — Practice Question Bank
*Original practice questions written to mirror the official exam objectives and weighting. These are NOT real/leaked exam questions ("dumps") — they are created fresh to help you study the concepts the actual exam tests.*

**Exam sections covered (with approximate official weighting):**
1. Salesforce Fundamentals (~8%)
2. Data Modeling and Management (~17%)
3. Logic and Process Automation (~46%)
4. User Interface (~8%)
5. Testing, Debugging, and Deployment (~19%)

Answers + explanations are at the **end of the document**. Try to answer everything first, then check yourself.

---

## Section 1: Salesforce Fundamentals (Q1–Q10)

**Q1.** Which statement about the Lightning Platform's multitenant architecture is correct?
A) Each org gets its own physical database server
B) All orgs share the same application logic and database tables, isolated logically by org ID
C) Multitenancy only applies to Professional Edition and below
D) Custom code runs in a dedicated tenant separate from standard objects

**Q2.** A developer wants to know the maximum number of custom objects allowed in an org. Which factor determines this limit?
A) The number of active users
B) The org's Salesforce Edition
C) The number of installed managed packages only
D) There is no limit in any edition

**Q3.** Which of the following is enforced by the Force.com governor limits?
A) Maximum number of records a user can view in a list view
B) Maximum number of SOQL queries per Apex transaction
C) Maximum number of fields a user can edit per page load
D) Maximum number of reports a user can run per day

**Q4.** What is the primary purpose of a sandbox in Salesforce?
A) To permanently replace the production org
B) To provide isolated environments for development, testing, and training without affecting production data
C) To increase storage limits in production
D) To act as a backup billing account

**Q5.** Which environment type copies both metadata and all production data?
A) Developer Sandbox
B) Developer Pro Sandbox
C) Partial Copy Sandbox
D) Full Sandbox

**Q6.** Which of the following best describes a "managed package"?
A) A bundle of metadata that cannot be installed in more than one org
B) A distributable, versioned set of application components that can be installed in other orgs, often with intellectual property protection
C) A package that can only contain custom objects
D) A temporary container used only inside sandboxes

**Q7.** What does declarative development in Salesforce refer to?
A) Writing Apex classes and triggers
B) Building functionality using point-and-click tools like Flow, Process Builder, or schema builder without code
C) Writing JavaScript for Lightning Web Components
D) Configuring Java-based middleware

**Q8.** Which statement about Salesforce Editions is true?
A) All editions include unlimited API calls
B) Feature availability and limits (like number of custom objects, or availability of certain declarative tools) vary by edition
C) Editions only affect UI color themes
D) Apex is available in every edition including Group Edition without restriction

**Q9.** What is the role of the "AppExchange"?
A) An internal Salesforce tool for editing metadata
B) A marketplace where partners and customers can list and install apps, components, and consulting offerings
C) A code editor bundled with Developer Console
D) A object relationship diagram tool

**Q10.** Which is true regarding a scratch org?
A) It is a long-lived org meant to store production data
B) It's a source-driven, disposable, and configurable Salesforce environment typically used with Salesforce DX for development
C) It can only be created via the Setup UI
D) It replaces the need for version control systems

---

## Section 2: Data Modeling and Management (Q11–Q30)

**Q11.** What is the default behavior when a record is deleted that is on the master side of a master-detail relationship?
A) The child records are orphaned but remain
B) The child records are also deleted
C) The child record's lookup field is cleared to null
D) The deletion is blocked automatically

**Q12.** Which field type supports cascading delete behavior by default?
A) Lookup relationship
B) Master-detail relationship
C) Formula field
D) Roll-up summary field is unrelated to delete behavior; N/A

**Q13.** A roll-up summary field can be created on which type of relationship?
A) Any lookup relationship, always
B) Only master-detail relationships (or lookup relationships if "Enable indirect lookup" alternatives are set — in classic terms, roll-ups require master-detail)
C) Many-to-many relationships only
D) External lookup relationships only

**Q14.** How can a many-to-many relationship be implemented between two custom objects?
A) Using a single master-detail relationship
B) Using a junction object with two master-detail (or lookup) relationships, one to each object
C) It's not possible in Salesforce
D) By adding a picklist field referencing both objects

**Q15.** What happens to a lookup field's value on a child record when the referenced parent record is deleted, assuming standard lookup behavior (not required, no cascade)?
A) The child record is deleted too
B) The lookup field is cleared, unless "Don't allow deletion" or similar restriction is configured
C) The parent record deletion is always blocked
D) The child record's Owner field is changed

**Q16.** Which statement correctly differentiates a custom setting from a custom metadata type?
A) Custom settings are always deployable via Metadata API but custom metadata types are not
B) Custom metadata types are records treated as metadata and can be deployed/packaged; custom settings store data (list or hierarchy) primarily for use in code, and hierarchy custom settings support org/profile/user-level defaults
C) They are functionally identical with no differences
D) Custom settings can only store numeric data

**Q17.** Which statement about External Objects is true?
A) They store data physically inside the Salesforce org's database
B) They map to data stored outside Salesforce and are typically accessed in real time via Salesforce Connect
C) They cannot be queried using SOQL
D) They automatically sync data every 5 minutes

**Q18.** A developer needs to enforce that a field must always contain a valid email format. What is the most appropriate solution?
A) A picklist field
B) A validation rule using the REGEX() or EMAIL formatting function, or simply an Email field type
C) A roll-up summary field
D) A record type

**Q19.** What is the maximum number of master-detail relationships a single custom object can have?
A) 1
B) 2
C) Salesforce currently allows up to 2 master-detail relationships on one object
D) Unlimited

**Q20.** Which of these is true regarding record types?
A) They control field-level security
B) They allow different business processes, picklist values, and page layouts to be associated with different types of records on the same object
C) They determine sharing rules automatically
D) They are only available on standard objects

**Q21.** What is the purpose of a schema builder in Salesforce?
A) To write Apex triggers visually
B) To visually view and create objects, fields, and relationships in the data model
C) To manage user permissions
D) To create reports and dashboards

**Q22.** Which limit applies specifically to SOQL queries in a single Apex transaction (synchronous)?
A) Maximum of 100 SOQL queries
B) Maximum of 50,000 SOQL queries
C) No limit at all
D) Only 1 query is allowed per method

**Q23.** When importing data using Data Loader, which operation permanently removes records from the recycle bin as well?
A) Insert
B) Update
C) Hard Delete
D) Upsert

**Q24.** What does the "Upsert" operation do?
A) Only inserts new records
B) Only updates existing records
C) Inserts new records or updates existing ones based on a matching field (like an external ID or record ID)
D) Permanently deletes duplicate records

**Q25.** Which field attribute uniquely identifies a record when integrating with an external system and allows upsert operations based on it?
A) Formula field
B) External ID field
C) Auto Number field
D) Roll-Up Summary field

**Q26.** What is a "big object" primarily used for?
A) Storing massive volumes of data (billions of records) for archival while maintaining performance
B) Replacing custom objects entirely
C) Storing only metadata definitions
D) Increasing picklist value limits

**Q27.** Which statement about indexed fields is correct?
A) All custom fields are indexed by default
B) Standard indexed fields (like Name, Id, OwnerId) and custom fields marked as "External ID" or "Unique," or those for which Salesforce Support creates a custom index, can improve SOQL query performance
C) Indexing has no effect on query performance
D) Formula fields are always indexed automatically

**Q28.** A developer needs to prevent duplicate records based on a combination of two fields. What is the most direct native solution?
A) A trigger only
B) A Duplicate Rule combined with a Matching Rule
C) A validation rule referencing RecordType
D) A page layout restriction

**Q29.** In the order of execution for a record save, which of these occurs BEFORE a before-save Apex trigger runs?
A) After-save triggers
B) Validation rules run after before-triggers, but system validation (required fields, field-level data types) happens before triggers... which is correct order:
   1) Original record loaded/new record initialized
   2) System validation checks
   3) Before-save Apex triggers
C) Roll-up summary field recalculations
D) Workflow field updates

**Q30.** Which is true of the Salesforce order of execution regarding validation rules and before triggers?
A) Validation rules always execute after all triggers
B) Validation rules execute after before-triggers run once, and if a before-trigger modifies a field, validation rules still apply to the final field values before the record is committed
C) Validation rules never apply to records created via Apex
D) Validation rules only apply to records created via the UI

---

## Section 3: Logic and Process Automation (Q31–Q80)

**Q31.** Which of the following is true about Apex?
A) It is a loosely typed, interpreted scripting language
B) It is a strongly typed, object-oriented programming language that executes on the Lightning Platform
C) It can only be used to build UI components
D) It requires a separate compiler installed locally to deploy

**Q32.** What triggers the execution of an Apex trigger?
A) A user logging into Salesforce
B) A DML operation (insert, update, delete, undelete) on the associated sObject
C) A report being run
D) A page layout being edited

**Q33.** What is the correct syntax context for a trigger that should run logic both before and after insert?
A) `trigger MyTrigger on Account (before insert, after insert)`
B) `trigger MyTrigger on Account before insert after insert`
C) `trigger MyTrigger Account (before, after insert)`
D) Two separate triggers are always mandatory

**Q34.** Which trigger context variable holds the list of new versions of records in an insert or update trigger?
A) Trigger.old
B) Trigger.new
C) Trigger.newMap
D) Trigger.records

**Q35.** In an `after update` trigger, which trigger context variable(s) are available?
A) Only Trigger.new
B) Trigger.new, Trigger.newMap, Trigger.old, and Trigger.oldMap — all are available, but Trigger.new/newMap are read-only in after context
C) Only Trigger.old
D) None; after triggers cannot access context variables

**Q36.** Why is it considered a best practice to avoid SOQL queries or DML statements inside a `for` loop in Apex?
A) Loops cannot contain SOQL syntax
B) It risks hitting governor limits (e.g., 100 SOQL queries or 150 DML statements per transaction) when processing bulk records
C) SOQL inside loops causes compile errors
D) It is not actually a concern; this is a myth

**Q37.** What is a "trigger handler" pattern used for?
A) To bypass governor limits entirely
B) To separate business logic into a reusable class invoked by a lightweight trigger, improving maintainability and testability
C) To allow multiple triggers to run on the same object simultaneously without conflicts automatically
D) It's required by Salesforce for all triggers

**Q38.** What happens if two triggers exist on the same object for the same event (e.g., two `before insert` triggers)?
A) Salesforce throws a compile error preventing deployment
B) Both triggers execute, but the order between multiple triggers of the same context is not guaranteed, which is why a single trigger per object is recommended
C) Only the most recently created trigger executes
D) Only the trigger with the shortest name executes

**Q39.** Which statement about recursive triggers is correct?
A) Apex triggers can never cause recursion
B) A trigger can inadvertently invoke itself again (e.g., via a DML update in the same trigger causing another update event) unless recursion is guarded against, often with a static boolean flag
C) Salesforce automatically prevents all recursion
D) Recursion can only happen with Flow, not Apex

**Q40.** What is the maximum heap size for a synchronous Apex transaction?
A) 1 MB
B) 6 MB
C) 12 MB
D) 50 MB

**Q41.** What is the maximum heap size for an asynchronous Apex transaction (e.g., Batch, Future)?
A) 6 MB
B) 12 MB
C) 24 MB
D) 100 MB

**Q42.** Which collection type maintains insertion order and allows duplicate values?
A) Set
B) Map
C) List
D) None of these allow duplicates

**Q43.** Which collection type stores unique keys mapped to values?
A) List
B) Set
C) Map
D) Array

**Q44.** Which collection type stores only unique elements with no guaranteed order?
A) List
B) Set
C) Map
D) Array

**Q45.** What does SOQL stand for?
A) Salesforce Object Query Language
B) Structured Object Query Logic
C) Salesforce Online Query List
D) Standard Object Quick Lookup

**Q46.** Which of the following is a key difference between SOQL and SOSL?
A) SOQL can search across multiple object types in a single query, but SOSL cannot
B) SOSL can search text across multiple objects and fields in a single query, while SOQL queries one object (plus its related objects) at a time
C) SOQL is used only for reports; SOSL is used only for triggers
D) There is no difference

**Q47.** Which SOQL clause is used to group query results and works with aggregate functions like COUNT() or SUM()?
A) ORDER BY
B) GROUP BY
C) LIMIT
D) WITH SECURITY_ENFORCED

**Q48.** What is the purpose of the `WITH SECURITY_ENFORCED` (or `WITH USER_MODE`) clause in SOQL?
A) It encrypts query results
B) It enforces field- and object-level security (CRUD/FLS) checks on the query, throwing an exception if the running user lacks access
C) It sorts records by security clearance
D) It restricts a query to only Administrator profiles

**Q49.** Which of the following correctly performs a relationship query to get a list of Contacts along with their parent Account's Name?
A) `SELECT Id, Name, Account.Name FROM Contact`
B) `SELECT Id, Name FROM Contact JOIN Account`
C) `SELECT Id, Name, Account(Name) FROM Contact`
D) `SELECT Account.Name FROM Contact WHERE Account = TRUE`

**Q50.** How do you write a SOQL query that retrieves Accounts along with their related Contacts (child records) in one query?
A) `SELECT Id, (SELECT Id, LastName FROM Contacts) FROM Account`
B) `SELECT Id, Contacts.Id FROM Account`
C) `SELECT Id FROM Account JOIN Contact`
D) `SELECT Id FROM Account WHERE Contacts != NULL`

**Q51.** What is the term for a query on the "many" side referencing its parent, versus a query on the "one" side referencing its children?
A) Parent-to-child queries use dot notation; child-to-parent queries use subqueries — this is reversed. The correct terms are: Child-to-parent uses dot notation (e.g., Account.Name from Contact); Parent-to-child uses a nested subquery (e.g., (SELECT ... FROM Contacts))
B) Both directions use identical syntax
C) Neither direction is supported in SOQL
D) Only parent-to-child queries are allowed

**Q52.** What does the Apex `Database.query()` method allow that a static SOQL query (`[SELECT ...]`) does not?
A) It allows dynamic construction of a query string at runtime
B) It runs faster than static SOQL
C) It bypasses governor limits
D) It automatically handles bulk DML

**Q53.** Which statement is true about `Database.insert(recordList, false)`?
A) It will throw an exception and stop processing if any record fails
B) It allows partial success — records that fail validation don't stop other records in the same list from being inserted, and results can be checked via Database.SaveResult
C) It ignores all validation rules
D) It is identical to `insert recordList;`

**Q54.** What is the purpose of the `Database.SaveResult` (or `Database.DeleteResult`, etc.) class when using `Database` methods with partial success?
A) It stores the entire org's schema
B) It provides per-record success/failure status and error details after a partial DML operation
C) It automatically retries failed records
D) It is used only in test classes

**Q55.** Which of the following async Apex mechanisms is best suited for making a callout to an external web service immediately after a trigger fires?
A) Batch Apex
B) @future(callout=true) method or a Queueable class implementing Database.AllowsCallouts
C) A trigger cannot ever result in a callout, even asynchronously
D) Schedulable Apex only

**Q56.** Why can't a trigger directly perform a synchronous HTTP callout?
A) Salesforce doesn't support HTTP callouts at all
B) Because triggers execute within a database transaction, and Salesforce disallows callouts during an open transaction to avoid locking issues — callouts must be deferred to async contexts
C) Only Visualforce pages can do callouts
D) Callouts require a REST API only, not Apex

**Q57.** What is the primary benefit of Batch Apex?
A) It can process very large numbers of records (millions) by breaking them into manageable chunks/batches, each processed in its own transaction
B) It executes with unlimited heap size in every scope
C) It runs entirely synchronously
D) It is required for all triggers to function

**Q58.** Which three methods must a class implement to be used as Batch Apex?
A) start(), execute(), finish()
B) init(), run(), complete()
C) begin(), process(), end()
D) startBatch(), runBatch(), stopBatch()

**Q59.** What does the `start` method in `Database.Batchable` return?
A) A List of records only
B) A `Database.QueryLocator` or an `Iterable<sObject>` that defines the scope of records to process
C) An integer representing batch size
D) A Boolean indicating success

**Q60.** What is the default batch size for Batch Apex if not explicitly specified?
A) 1
B) 50
C) 100
D) 200

**Q61.** What is the maximum number of batches that can be queued or active at one time (approximately) in an org, and what governs how many times `execute` runs?
A) The number of active/queued batch jobs is limited (historically 5 by default, extendable via a Salesforce feature); execute() runs once per batch/chunk of records
B) Only one batch job can ever exist in an org
C) execute() runs exactly once regardless of record volume
D) There is no limit at all

**Q62.** Which interface allows a Queueable Apex job to be chained to run another job immediately after it finishes?
A) It's automatic for all Queueable classes—no special step needed beyond calling System.enqueueJob() from within execute()
B) Database.Batchable
C) Schedulable
D) Callable

**Q63.** What is a key advantage of Queueable Apex over simple @future methods?
A) Queueable methods can accept complex, non-primitive parameter types (like sObjects or custom objects) and return a job ID, and can be chained
B) @future methods can accept sObjects but Queueable cannot
C) Queueable Apex cannot run any DML
D) There is no functional difference between them

**Q64.** How many jobs can typically be added to the queue via `System.enqueueJob` in a single Apex transaction (synchronous context)?
A) Unlimited
B) 1 (in most contexts, though limits vary — synchronous Apex can generally enqueue only 1 queueable job per transaction, with higher limits allowed for asynchronous contexts)
C) 100
D) 50000

**Q65.** What interface must a class implement to run on a defined schedule (like a cron expression)?
A) Batchable
B) Schedulable
C) Queueable
D) Comparable

**Q66.** Which method must a Schedulable class implement?
A) start()
B) execute(SchedulableContext sc)
C) finish()
D) run()

**Q67.** What tool/class is used to schedule an Apex class to run at a specific time using a CRON expression programmatically?
A) System.schedule('JobName', cronExpression, new MyScheduledClass())
B) Database.schedule()
C) Trigger.schedule()
D) Apex.scheduleJob()

**Q68.** Which of these best describes a "Custom Apex Exception"?
A) A standard exception that cannot be extended
B) A user-defined class that extends the built-in `Exception` class, allowing developers to create meaningful, specific error types
C) An exception type reserved only for Salesforce internal use
D) A runtime-only construct not available in custom Apex

**Q69.** Which built-in exception is thrown when a SOQL query using `LIMIT 1` (or expecting one row) returns no rows and is assigned directly to a single sObject variable?
A) System.NullPointerException
B) System.QueryException (specifically "List has no rows for assignment")
C) System.DmlException
D) System.LimitException

**Q70.** What does the keyword `with sharing` do when declared on an Apex class?
A) It grants the class System Administrator access regardless of the running user
B) It enforces that the class runs in the context of the current user's record-level sharing rules and permissions
C) It disables all validation rules for the class
D) It is required on every Apex class or it won't compile

**Q71.** What is the default sharing behavior of an Apex class if neither `with sharing`, `without sharing`, nor `inherited sharing` is specified?
A) It always runs with sharing enforced
B) It runs in "without sharing" mode unless it's called from another class, in which case it inherits that calling class's sharing setting — this is the effect of the default (roughly equivalent in practice to how "inherited sharing" behaves, though the true default is "without sharing" if there's no calling context)
C) It throws a compile error
D) It always inherits the running user's profile permission set only

**Q72.** Which is true regarding Apex and CRUD/FLS (Field-Level Security) enforcement by default?
A) Apex automatically enforces object and field-level security for all DML and SOQL unless bypassed
B) Apex code runs in system mode by default and does NOT automatically enforce a user's object/field-level permissions unless the developer explicitly checks (e.g., using Schema methods, WITH SECURITY_ENFORCED, WITH USER_MODE, or Security.stripInaccessible)
C) CRUD/FLS enforcement is only relevant to Visualforce, not Apex
D) FLS is enforced automatically for all Standard Controllers and Apex identically with no differences

**Q73.** What does `Schema.sObjectType.Account.fields.Name.isAccessible()` check?
A) Whether the current user's profile/permission sets grant read access to the Account.Name field
B) Whether the Account object exists
C) Whether the field is required
D) Whether the field is a formula field

**Q74.** Which declarative automation tool has Salesforce recommended as the primary tool going forward, largely replacing Workflow Rules and Process Builder?
A) Approval Processes
B) Flow (Flow Builder)
C) Formula fields
D) Escalation Rules

**Q75.** In terms of order of execution, when do Flow (record-triggered, before-save) updates typically occur relative to Apex triggers?
A) Record-triggered flows configured to run before-save execute in a similar phase to before-triggers, and the relative order between before-save flows and before-triggers of the same object can matter for field updates
B) Flows always execute strictly after all Apex triggers finish, with no exceptions
C) Flows cannot update fields on the triggering record at all
D) Flows only run after the record has already committed to the database

**Q76.** Which statement about Apex Governor Limits is correct?
A) Governor limits apply only to Visualforce pages
B) Governor limits exist to ensure no single execution monopolizes shared multitenant resources, and limits reset per transaction
C) Governor limits can be permanently disabled by a System Administrator
D) Governor limits are identical for synchronous and asynchronous Apex in every category

**Q77.** What is the default maximum number of DML statements allowed per synchronous Apex transaction?
A) 50
B) 100
C) 150
D) 200

**Q78.** What is the default maximum number of records that can be processed by DML statements in a single transaction?
A) 1,000
B) 10,000
C) 50,000
D) 100,000

**Q79.** Which annotation is used to expose an Apex method so it can be called from JavaScript in a Lightning Web Component?
A) `@AuraEnabled`
B) `@RemoteAction`
C) `@Exposed`
D) `@InvocableMethod`

**Q80.** Which annotation allows an Apex method to be called from Flow or Process Builder as an invocable action?
A) `@AuraEnabled`
B) `@InvocableMethod`
C) `@future`
D) `@RestResource`

---

## Section 4: User Interface (Q81–Q92)

**Q81.** Which framework is Salesforce's modern, standards-based UI framework built on native Web Components?
A) Aura Components only
B) Lightning Web Components (LWC)
C) Visualforce exclusively
D) jQuery Mobile

**Q82.** In a Lightning Web Component, which decorator marks a property as reactive and settable from a parent component?
A) `@track`
B) `@api`
C) `@wire`
D) `@expose`

**Q83.** What is the purpose of the `@wire` decorator in LWC?
A) To style the component using CSS
B) To provision data reactively from Salesforce (e.g., Apex methods or UI API) into a component property or function
C) To register event listeners only
D) To declare a component as public

**Q84.** How does a child LWC communicate an event up to its parent component?
A) By directly calling a method on the parent
B) By dispatching a custom event (`this.dispatchEvent(new CustomEvent(...))`) that the parent listens for
C) LWC components cannot communicate with parents
D) By writing to a static Apex variable

**Q85.** Which statement about Visualforce is correct in the current platform context?
A) Visualforce is deprecated and cannot be used at all
B) Visualforce is still a supported, mature UI framework (Salesforce's older framework, most useful for PDF generation, email templates, and legacy needs) though Salesforce now emphasizes LWC for new development
C) Visualforce only works with Apex triggers, not controllers
D) Visualforce cannot reference standard controllers

**Q86.** What does a Visualforce "Standard Controller" provide?
A) Custom business logic defined entirely by the developer
B) Built-in CRUD operations and functionality tied to a specific standard or custom object, without writing Apex
C) Access only to custom objects, never standard ones
D) A way to bypass field-level security

**Q87.** Which of the following correctly describes an "Extension Controller" in Visualforce?
A) It replaces the standard controller entirely and cannot access its methods
B) It's an Apex class that adds functionality to a standard or custom controller using the `extensions` attribute on the page, while still allowing access to the base controller's methods via a constructor
C) It only works with custom controllers
D) It's a JavaScript-only concept

**Q88.** In LWC, which lifecycle hook is called after a component has finished rendering for the first time (fully rendered)?
A) `constructor()`
B) `connectedCallback()`
C) `renderedCallback()`
D) `disconnectedCallback()`

**Q89.** Which markup file is required alongside the JavaScript and HTML files to define metadata (like API version and exposure targets) for a Lightning Web Component?
A) `.js-meta.xml`
B) `.config.json`
C) `manifest.xml`
D) `.meta.yaml`

**Q90.** What does `lightning-record-edit-form` do in LWC?
A) It only displays read-only fields
B) It provides a base component to create/edit records honoring field-level security and validation rules, without needing custom Apex for basic operations
C) It is used exclusively for reports
D) It requires an Apex controller to function

**Q91.** Which statement about Lightning App Builder is correct?
A) It's used to write Apex triggers
B) It's a point-and-click tool to build Lightning pages (record pages, app pages, home pages) by dragging components onto a canvas
C) It replaces the need for permission sets
D) It only works with Visualforce pages

**Q92.** Which is the correct way to make an Apex method available to be called from a Visualforce page's controller as an action method bound to a button?
A) Mark it `@AuraEnabled` only
B) Define a public or global method (often returning PageReference) in the associated Apex controller/extension and reference it via the `action` attribute of a command button/link
C) Apex cannot be invoked from Visualforce buttons
D) Only JavaScript can handle Visualforce button clicks

---

## Section 5: Testing, Debugging, and Deployment (Q93–Q110)

**Q93.** What is the minimum code coverage percentage required org-wide to deploy Apex to production?
A) 50%
B) 65%
C) 75%
D) 100%

**Q94.** Which annotation marks a class as containing test methods?
A) `@isTest`
B) `@TestClass`
C) `@RunTest`
D) `@ApexTest`

**Q95.** What is the purpose of `Test.startTest()` and `Test.stopTest()` in a test method?
A) They are optional decorative statements with no functional effect
B) They reset governor limits for the code between them and force any asynchronous operations (e.g., @future, Batch, Queueable) enclosed to run synchronously before stopTest() completes
C) They permanently commit test data to production
D) They disable all validation rules during the test

**Q96.** Why should test methods avoid using `SeeAllData=true` in most cases?
A) It's required for all Apex tests to pass
B) Tests should create their own test data for isolation and reliability; relying on existing org data can make tests unpredictable and fragile, so Salesforce recommends against it except in specific cases (e.g., referencing certain setup objects)
C) SeeAllData=true is not a real annotation parameter
D) It always causes compile errors

**Q97.** What does `System.assertEquals(expected, actual)` do in a test?
A) It automatically fixes the code if values differ
B) It compares the expected and actual values, and fails (throws an assertion error) the test if they don't match, which is essential to verifying test outcomes
C) It logs a warning but never fails the test
D) It is used only for UI testing, not Apex

**Q98.** Which of the following is a recommended best practice for Apex unit tests?
A) Testing only the "happy path" and skipping bulk/negative/edge scenarios
B) Testing single-record scenarios, bulk (200+ record) scenarios, positive behavior, negative/exception behavior, and restricted-user permission scenarios
C) Hardcoding record IDs from production directly into test methods
D) Avoiding assertions to keep tests simple

**Q99.** What is a "Test.loadData()" method typically used for?
A) Deleting test records after a test run
B) Loading static resource CSV data into sObjects for use during test execution
C) Running load/performance testing against the org
D) Bypassing code coverage requirements

**Q100.** Which tool would a developer use to view real-time logs including SOQL queries, DML statements, and debug statements during code execution?
A) Setup > Object Manager
B) Debug Logs (via Developer Console or Setup)
C) Report Builder
D) Data Loader

**Q101.** What is the purpose of the `System.debug()` statement?
A) It permanently stores a debug record in a custom object
B) It outputs a custom message to the debug log for troubleshooting purposes during development
C) It automatically emails the developer
D) It halts execution until manually resumed

**Q102.** Which of these tools/approaches is best suited to inspect variable values, execution flow, and heap state step-by-step during Apex execution?
A) Checkpoints and the Interactive Debugger (or Apex Replay Debugger) using debug logs
B) Data Loader
C) Workflow Rules
D) List Views

**Q103.** What is Salesforce DX (SFDX) primarily used for?
A) Only for building Visualforce pages
B) A set of tools (CLI, source-driven development, scratch orgs) for modern application lifecycle management, version control integration, and CI/CD
C) Exclusively for managing user licenses
D) A reporting tool for executives

**Q104.** In a source-driven development model with Salesforce DX, where does the "source of truth" for metadata typically live?
A) Only inside the production org
B) In a version control system (e.g., Git), with orgs treated as deployment targets/environments
C) In a spreadsheet
D) Metadata has no source of truth in SFDX

**Q105.** Which deployment tool/mechanism allows deploying metadata using an XML manifest file (`package.xml`) that lists components to retrieve or deploy?
A) Data Loader
B) Metadata API (via tools like Workbench, SFDX, or Ant Migration Tool)
C) Report Builder
D) Flow Builder

**Q106.** What is a "Change Set" used for?
A) Migrating data records between orgs
B) Migrating metadata (like Apex classes, objects, fields, layouts) between related orgs (e.g., sandbox to production) within the same Salesforce instance hierarchy
C) Creating new user licenses
D) Running Apex tests automatically on a schedule

**Q107.** Why might all Apex tests in an org run automatically during a production deployment of Apex code?
A) It's optional and never required
B) Salesforce requires running relevant Apex tests (with the 75% org-wide coverage threshold) as part of validating a deployment containing Apex to production
C) Tests only run when manually triggered post-deployment
D) Only trigger-related tests ever run during deployment

**Q108.** What does the term "CI/CD" refer to in the context of Salesforce application development?
A) Customer Identification / Customer Data
B) Continuous Integration / Continuous Delivery (or Deployment) — automating the building, testing, and deployment of code changes
C) A Salesforce Edition name
D) A specific Apex design pattern

**Q109.** Which statement about unlocked packages and 2nd-generation packaging (2GP) is correct?
A) They are identical to Change Sets in every way
B) They allow modular, version-controlled, source-driven packaging of metadata, often used in modern DevOps pipelines, without necessarily protecting the underlying code (unlike managed packages)
C) They cannot include Apex classes
D) They are only for AppExchange-listed managed products

**Q110.** Which best describes the purpose of the Apex Replay Debugger?
A) It requires a live active session to debug older logs
B) It lets developers step through previously captured debug logs locally (e.g., in VS Code) to simulate and analyze execution without consuming a live debug session
C) It replays user clicks in the Salesforce UI
D) It is only used for Visualforce, not Apex

---

# ANSWER KEY & EXPLANATIONS

### Section 1: Salesforce Fundamentals

**Q1: B** — Multitenancy means many orgs share the same underlying application infrastructure and database schema, with data logically (not physically) separated by organization ID. This is core to how the Lightning Platform scales.

**Q2: B** — Limits such as the number of custom objects, custom fields, and available features are tied to the org's Edition (e.g., Enterprise vs. Unlimited vs. Professional).

**Q3: B** — Governor limits cap platform resource usage per transaction, including the number of SOQL queries, DML statements, CPU time, heap size, etc., to protect the shared multitenant environment.

**Q4: B** — Sandboxes are isolated copies of production used for development, QA, training, and testing changes safely.

**Q5: D** — A Full Sandbox copies all metadata and all data from production. Partial Copy only includes a sample of data; Developer/Developer Pro sandboxes copy metadata only (no or minimal data).

**Q6: B** — Managed packages are versioned, distributable units of functionality (often sold on AppExchange) that can obscure Apex code (IP protection) and support upgrades.

**Q7: B** — Declarative development uses configuration tools (Flow, Process Builder [legacy], Schema Builder, Validation Rules) rather than writing code.

**Q8: B** — Not all features/limits are the same across editions — e.g., Group/Essentials editions have fewer capabilities than Enterprise/Unlimited.

**Q9: B** — AppExchange is Salesforce's marketplace for third-party and Salesforce-built apps, components, and consulting listings.

**Q10: B** — Scratch orgs are short-lived, fully configurable, source-driven orgs created via Salesforce DX, ideal for feature development and testing in isolation.

---

### Section 2: Data Modeling and Management

**Q11: B** — Master-detail relationships cascade delete: deleting the master automatically deletes all detail (child) records.

**Q12: B** — Only master-detail relationships have this cascading delete/ownership behavior by design; lookups do not cascade delete by default.

**Q13: B** — Roll-up summary fields traditionally require a master-detail relationship (they aggregate values from detail records onto the master).

**Q14: B** — A junction object (a custom object with two master-detail or lookup relationships) is the standard way to model many-to-many relationships.

**Q15: B** — With a standard (non-required, non-restricted) lookup, deleting the parent typically clears the child's lookup field to null, unless configured otherwise (e.g., "Don't allow deletion of the lookup record that's part of a lookup relationship").

**Q16: B** — Custom Metadata Types behave like configurable, deployable "metadata records" (great for app configuration deployed across environments); Custom Settings store cached data quickly accessible in code, with special hierarchy-based defaults per org/profile/user.

**Q17: B** — External Objects, typically used with Salesforce Connect, represent data that lives outside Salesforce and is fetched on-demand, without duplicating storage in Salesforce.

**Q18: B** — Using the Email field type or a validation rule with proper regex/format functions ensures data integrity for email formatting.

**Q19: C** — Salesforce currently supports up to 2 master-detail relationships on one custom object.

**Q20: B** — Record types let you offer different picklist values, business processes, and page layouts based on the "type" of record, all on the same object.

**Q21: B** — Schema Builder gives a visual canvas to view/create objects, fields, and their relationships.

**Q22: A** — The synchronous Apex limit is 100 SOQL queries per transaction (async allows more — 200).

**Q23: C** — Hard Delete in Data Loader bypasses the recycle bin, permanently removing records immediately (requires the "Bulk API Hard Delete" permission).

**Q24: C** — Upsert intelligently inserts new records or updates existing ones based on a specified matching key (Id or an External ID field).

**Q25: B** — External ID fields are specifically designed to identify records against an external system and support upsert matching.

**Q26: A** — Big Objects are designed to store and manage massive datasets (billions of records) efficiently, mainly for archiving historical data while keeping it queryable.

**Q27: B** — Certain fields are indexed by default (Id, Name, OwnerId, CreatedDate, etc.), and marking a custom field as External ID or Unique adds an index; Salesforce Support can also add custom indexes on request.

**Q28: B** — Salesforce's native Duplicate Management feature (Matching Rules + Duplicate Rules) is the standard declarative approach to detect and prevent/alert on duplicates.

**Q29: B** — The order is: record initialized in memory → system validation (required fields/data types) → before-triggers run → custom validation rules → (assuming no errors) record saved → after-triggers → assignment rules, etc. (Simplified; exact real order also includes duplicate rules before validation rules in newer releases.)

**Q30: B** — Validation rules evaluate the field values as they exist after before-triggers have potentially modified them, before the record is finally committed.

---

### Section 3: Logic and Process Automation

**Q31: B** — Apex is Salesforce's strongly-typed, object-oriented, on-platform programming language, syntactically similar to Java.

**Q32: B** — Apex triggers fire in response to DML events (insert/update/delete/undelete) on a specific sObject.

**Q33: A** — Multiple trigger events are declared in the parentheses, comma-separated, e.g., `trigger MyTrigger on Account (before insert, after insert) { }`.

**Q34: B** — `Trigger.new` is the list of new record versions (available in insert/update/undelete triggers).

**Q35: B** — In `after update`, all four context variables are accessible, but `Trigger.new` and `Trigger.newMap` become read-only (you cannot modify fields directly on records in an after context).

**Q36: B** — Looping and issuing SOQL/DML inside the loop multiplies calls per record processed, quickly exceeding governor limits when bulk data (e.g., 200 records) is processed — hence "bulkification" best practices.

**Q37: B** — Trigger handler classes keep triggers thin (calling only a handler method), improving testability, reuse, and maintainability, and helping enforce "one trigger per object."

**Q38: B** — Multiple triggers on the same object/event will all fire, but their execution order relative to each other isn't guaranteed — a key reason to consolidate logic into one trigger (or a well-orchestrated framework).

**Q39: B** — Recursive triggers happen when a trigger's own DML re-triggers itself; static Boolean flags (since statics persist through a single transaction) are a common guard.

**Q40: B** — Synchronous Apex heap limit is 6 MB.

**Q41: C** — Asynchronous Apex heap limit is 12 MB.

**Q42: C** — A List preserves order and permits duplicate elements (like an array with dynamic sizing).

**Q43: C** — A Map associates unique keys with values (key-value pairs).

**Q44: B** — A Set holds unique, unordered elements (no duplicates allowed).

**Q45: A** — SOQL = Salesforce Object Query Language.

**Q46: B** — SOSL (Salesforce Object Search Language) performs text searches across multiple objects/fields simultaneously; SOQL is structured querying of one primary object (plus relationships).

**Q47: B** — `GROUP BY` is used with aggregate functions (COUNT, SUM, AVG, etc.) to group results.

**Q48: B** — `WITH SECURITY_ENFORCED` (and the newer `WITH USER_MODE`) enforce field- and object-level security checks on SOQL queries, throwing an exception (or silently filtering, depending on the mechanism) if the user lacks access.

**Q49: A** — Dot notation traverses a child-to-parent (lookup/master-detail) relationship directly in the SELECT clause.

**Q50: A** — Nested subqueries in parentheses retrieve related child records (parent-to-child query), using the child relationship name (e.g., `Contacts` is the default relationship name for child Contact records of an Account).

**Q51: A** — Child-to-parent: dot notation (`Account.Name` from Contact). Parent-to-child: subquery syntax (`(SELECT Id FROM Contacts)` from Account).

**Q52: A** — `Database.query()` (and `Database.queryWithBinds`) let you build a SOQL string dynamically at runtime, useful for flexible/dynamic query needs.

**Q53: B** — Passing `false` as the second parameter to `Database.insert()` (or similar DML database methods) allows partial success instead of throwing an exception on the first failure.

**Q54: B** — `Database.SaveResult` (and related result classes) let you inspect success/failure and specific error messages per record after a partial DML operation.

**Q55: B** — Since triggers can't make synchronous callouts, deferring to `@future(callout=true)` or a Queueable implementing `Database.AllowsCallouts` is the standard approach.

**Q56: B** — Callouts during an open database transaction risk long-held locks; Salesforce disallows mixing DML/callouts synchronously in certain contexts, especially within triggers, requiring async execution.

**Q57: A** — Batch Apex processes large datasets by splitting them into smaller "batches" (chunks), each running as its own transaction with fresh governor limits.

**Q58: A** — `Database.Batchable` requires implementing `start()`, `execute()`, and `finish()`.

**Q59: B** — `start()` returns a `Database.QueryLocator` (for SOQL-based scope, supporting larger record sets) or an `Iterable<sObject>`.

**Q60: D** — Default batch size is 200 records per batch/chunk (developer can specify a smaller custom size, e.g., in `Database.executeBatch(instance, scope)`).

**Q61: A** — Historically, orgs are limited in concurrent queued/active batch jobs (5 by default in many orgs, though Salesforce has evolved this); `execute()` runs once per chunk of records defined by batch size.

**Q62: A** — Any Queueable class can chain another job by calling `System.enqueueJob()` inside its own `execute()` method — no extra interface required.

**Q63: A** — Queueable Apex supports complex parameter types (not just primitives, as with @future) and returns a Job ID for monitoring; it also supports chaining, which @future does not.

**Q64: B** — In most synchronous Apex contexts you can enqueue only 1 job per transaction (though asynchronous contexts like a Queueable's own execute() historically allow more chaining flexibility, and current limits have evolved — check the latest Salesforce docs/limits for exact numbers).

**Q65: B** — The `Schedulable` interface allows a class to run on a schedule via a CRON expression.

**Q66: B** — Schedulable requires implementing `execute(SchedulableContext sc)`.

**Q67: A** — `System.schedule('JobName', cronExpr, new MyClass())` programmatically schedules an Apex job.

**Q68: B** — Custom exceptions extend Apex's built-in `Exception` class (e.g., `public class MyException extends Exception {}`), enabling more descriptive error handling.

**Q69: B** — Assigning a query with zero rows directly to a single sObject variable (instead of a List) throws `System.QueryException: List has no rows for assignment`.

**Q70: B** — `with sharing` makes the class respect the running user's org-wide defaults, sharing rules, roles, and manual sharing when querying/updating data.

**Q71: B** — Without an explicit sharing declaration, a class runs in system context (without sharing) by default when it's the "top" of the call stack, but the practical answer tested by Salesforce is that omitting the keyword means the class does NOT enforce sharing unless invoked from a "with sharing" context that it inherits from (this nuance is exactly why `inherited sharing` was introduced).

**Q72: B** — By default, Apex runs in system mode, ignoring the running user's object/field-level permissions unless the developer explicitly enforces them (via `WITH SECURITY_ENFORCED`, `WITH USER_MODE`, `Security.stripInaccessible()`, or manual Schema checks).

**Q73: A** — This describes checking Field-Level Security programmatically for the running user's access to a specific field.

**Q74: B** — Salesforce has been steering customers toward Flow Builder as the primary declarative automation tool, with Workflow Rules and Process Builder being retired/deprecated for new development.

**Q75: A** — Before-save (fast field update) record-triggered flows run in a similar phase as before-triggers; the precise interleaving order between multiple automations of the same "before" phase can affect final field values, so understanding this nuance matters for real-world design.

**Q76: B** — Governor limits protect the shared, multitenant platform from any single execution consuming excessive resources; they reset with each new transaction.

**Q77: C** — The default synchronous DML statement limit per transaction is 150.

**Q78: B** — The default limit on records processed by DML in one transaction is 10,000.

**Q79: A** — `@AuraEnabled` exposes an Apex method (and optionally a property) to Lightning components (both Aura and LWC via `@wire` or imperative calls).

**Q80: B** — `@InvocableMethod` (and `@InvocableVariable` for inputs) exposes an Apex method as an action callable from Flow, Process Builder, or Apex REST-invoked automation.

---

### Section 4: User Interface

**Q81: B** — LWC is Salesforce's modern framework built on native Web Components standards (Custom Elements, Shadow DOM, templates, ES modules).

**Q82: B** — `@api` exposes a property as public, allowing a parent component to set it (public reactive property).

**Q83: B** — `@wire` declaratively connects a component to a data source (Apex method, Lightning Data Service/UI API) and automatically re-invokes when reactive parameters change.

**Q84: B** — Child-to-parent communication in LWC is done via custom DOM events dispatched by the child and listened to by the parent.

**Q85: B** — Visualforce remains supported and useful in certain scenarios (e.g., generating PDFs, complex print layouts, legacy apps), though Salesforce's strategic direction for new UI work is LWC.

**Q86: B** — A Standard Controller automatically provides basic CRUD-like functionality tied to a specific object, without custom Apex.

**Q87: B** — An extension controller (declared via `extensions="MyExtensionClass"`) adds custom logic on top of a standard or custom controller, typically constructed by accepting the base controller in its constructor.

**Q88: C** — `renderedCallback()` fires after every render, including the first, once the DOM has been rendered.

**Q89: A** — The `.js-meta.xml` configuration file defines metadata like API version, whether the component is exposed, and which Lightning experiences/pages it can be used on.

**Q90: B** — `lightning-record-edit-form` (and its child `lightning-input-field`) automatically respects FLS, page layout-driven behavior (in some cases), and validation rules without needing a custom Apex controller for basic use cases.

**Q91: B** — Lightning App Builder is the drag-and-drop tool to assemble Lightning pages using standard, custom (Aura/LWC), and third-party components.

**Q92: B** — An action method (often returning a `PageReference`) defined in the Apex controller/extension is referenced via the button/link's `action` attribute to handle server-side logic on click.

---

### Section 5: Testing, Debugging, and Deployment

**Q93: C** — Salesforce requires at least 75% org-wide Apex code coverage (with no individual trigger having 0% coverage, and specific line coverage nuances) to deploy to production.

**Q94: A** — `@isTest` annotation marks a class (or individual methods) as test code, excluded from org code coverage requirements themselves and only compiled/run in test contexts.

**Q95: B** — `Test.startTest()`/`Test.stopTest()` reset governor limits for the enclosed block and force queued asynchronous operations to execute synchronously by the time `stopTest()` finishes, enabling verification of their results within the same test.

**Q96: B** — Best practice is for tests to create their own data (using `@testSetup` or directly in methods) for predictable, isolated, repeatable test runs; `SeeAllData=true` is discouraged except in narrow cases (like certain unchangeable setup/reference data).

**Q97: B** — Assertions like `System.assertEquals` are how a test actually verifies correctness — if the values don't match, the test fails, signaling a bug or unexpected behavior.

**Q98: B** — Comprehensive tests should include single-record and bulk (200+) scenarios, both positive and negative (exception) paths, and ideally check behavior under restricted user permissions using `System.runAs()`.

**Q99: B** — `Test.loadData()` reads data from a CSV static resource and inserts/returns it as sObjects for use in tests, useful for complex or large test datasets.

**Q100: B** — Debug Logs (viewable via Developer Console, Setup, or Workbench) show detailed execution traces including SOQL, DML, and debug statements.

**Q101: B** — `System.debug()` writes custom messages into the debug log at a chosen log level, aiding troubleshooting.

**Q102: A** — The Interactive Debugger (in sandboxes, part of Lightning Platform Debugger) or the free Apex Replay Debugger (via VS Code, using captured logs) let developers step through code execution and inspect variable states.

**Q103: B** — Salesforce DX provides a CLI, scratch orgs, and source-driven development workflows enabling modern version control and CI/CD practices.

**Q104: B** — In source-driven development, Git (or another VCS) holds the authoritative metadata source, and orgs (scratch, sandbox, production) are treated as deployable targets/environments rather than the source of truth.

**Q105: B** — The Metadata API (accessible via tools like Workbench, SFDX CLI, Ant Migration Tool, or IDEs) uses a `package.xml` manifest to define what components to retrieve or deploy.

**Q106: B** — Change Sets move metadata (not data records) between orgs that are connected/related in the same Salesforce release/instance family (e.g., a sandbox linked to its production org).

**Q107: B** — Salesforce enforces running the relevant Apex tests during production deployments that include Apex, checking against the 75% minimum org-wide coverage threshold before allowing the deployment to complete.

**Q108: B** — CI/CD (Continuous Integration/Continuous Delivery or Deployment) automates building, testing, and deploying changes frequently and reliably, often used with SFDX and Git-based pipelines.

**Q109: B** — Unlocked packages (2GP) support modular, source-driven, versioned metadata delivery similar in spirit to managed packages but generally without code obfuscation/IP protection, fitting well into DevOps practices.

**Q110: B** — The Apex Replay Debugger works offline against a previously captured debug log, letting developers step through code execution locally without needing an active live debug session.

---

*Disclaimer: These are original, self-authored practice questions created to reflect the topics, weighting, and style of the Salesforce Platform Developer I exam guide. They are not copied or derived from any real, leaked, or proprietary exam question bank. For the authoritative and most current exam blueprint, always refer to the official exam guide on Trailhead/Salesforce Certification.*
