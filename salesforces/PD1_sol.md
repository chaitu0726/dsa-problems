# Salesforce Platform Developer I (PD1) — Practice Exam
### Answer Key with Explanations

> Companion file to "PD1_Practice_Exam_Questions.md". Original practice content, not real exam questions.

---

## Section 1: Salesforce Fundamentals

**Q1. Answer: B**
Salesforce uses a multi-tenant architecture: all customers share the same underlying application and database infrastructure, but data is logically isolated per org. No dedicated physical servers per customer.

**Q2. Answer: B, C**
A Full sandbox copies both metadata and data (a full copy of production). Sandbox types (Developer, Developer Pro, Partial Copy, Full) each have defined refresh intervals. Developer sandboxes do NOT copy all data — only metadata. Storage limits vary by sandbox type, not identical to production.

**Q3. Answer: B**
Flow (specifically Record-Triggered Flow) is the declarative, no-code way to update related records automatically on save. Visualforce and Apex require code; Batch Apex is for large-volume async processing, not a real-time single-record update trigger point.

**Q4. Answer: A**
Role hierarchy automatically grants users above a record owner in the hierarchy access to that record (when OWD allows). Sharing rules are used to extend access beyond OWD to specific users/groups/roles that wouldn't otherwise have it via hierarchy or ownership.

**Q5. Answer: B**
"Private" OWD means only the owner (and those above in role hierarchy) can see records by default — sharing rules or Apex managed sharing are needed to open access further. Public Read/Write and Public Full Access already grant broad access, so they wouldn't need this.

**Q6. Answer: A**
Setup → Company Information (and the "Salesforce Limits" or System Overview page) shows org-specific consumption of storage, API calls, and other edition-based limits.

**Q7. Answer: C**
Docker Container is not a Salesforce metadata type. Custom Object, Custom Field, and Validation Rule are all core metadata components.

---

## Section 2: Data Modeling and Management

**Q8. Answer: B**
In a Master-Detail relationship, deleting the master record cascades and deletes all associated detail (child) records automatically. This is a key distinguishing behavior vs. Lookup relationships.

**Q9. Answer: B**
A Lookup relationship allows the child to exist independently — the lookup field can even be blank (unless required). Master-Detail requires a parent and ties record ownership/sharing to it.

**Q10. Answer: B**
The standard many-to-many pattern on Salesforce is a junction object containing two relationship fields (Master-Detail or Lookup), one linking to each of the two objects being related (e.g., Enrollment object linking Student and Course).

**Q11. Answer: B**
Roll-up summary fields live on the master object in a Master-Detail relationship and aggregate (SUM, COUNT, MIN, MAX) values from detail records. They are not available on Lookup relationships natively (without workarounds like Apex/Flow) and don't require code by default.

**Q12. Answer: B**
Data Loader is designed for large-volume imports/exports (up to millions of records), supports complex field mapping, and can be scheduled via command line. The Data Import Wizard is limited to a much smaller volume (50,000 records) and simpler UI-driven mapping.

**Q13. Answer: B**
A custom object can have up to 2 Master-Detail relationships (enabling patterns like junction objects for many-to-many relationships).

**Q14. Answer: B**
External IDs mark a field as a unique identifier from an external system, primarily used for `upsert` operations and matching records during data integration/import, avoiding duplicates.

---

## Section 3: Logic and Process Automation

**Q15. Answer: A**
The correct Apex trigger syntax lists multiple events in a comma-separated list within parentheses: `trigger AccTrigger on Account (before insert, before update) { }`.

**Q16. Answer: B**
`Trigger.old` holds the previous version of records (before update) and `Trigger.new` holds the version being saved — comparing corresponding elements lets you detect field changes.

**Q17. Answer: B**
Apex enforces a per-transaction limit on the number of SOQL queries (100 synchronous / 200 asynchronous). Placing a query inside a loop multiplies query count by the number of loop iterations, risking a `LimitException`.

**Q18. Answer: B**
Best practice ("bulkification"): collect the relevant Ids into a Set, run a single SOQL query outside the loop using `WHERE Id IN :setOfIds` (or similar), then perform DML once on a collected List — avoiding SOQL/DML inside loops entirely.

**Q19. Answer: A**
`with sharing` makes the Apex class respect the running user's record-level sharing rules (OWD, roles, sharing rules) for the operations it performs. Without it (or with `without sharing`), the class runs in system context, ignoring sharing.

**Q20. Answer: B**
Batch Apex is purpose-built to process large numbers of records in manageable chunks (default scope 200), each executing as its own transaction with its own governor limit allocation — ideal for 500,000 records.

**Q21. Answer: B**
`Database.executeBatch()` kicks off asynchronous execution of a class that implements `Database.Batchable`, dividing records into batches and calling `execute()` for each.

**Q22. Answer: C**
Implementing the `Schedulable` interface and calling `System.schedule()` (with a CRON expression) is how you run Apex on a recurring time-based schedule. `@future` is for asynchronous execution triggered immediately, not scheduled.

**Q23. Answer: B**
`@future` methods can only accept primitive data types (or collections/arrays of primitives) and Ids as parameters — they cannot accept sObjects or custom objects directly, because their parameters must be serializable in a specific way.

**Q24. Answer: A**
Common recursion-prevention patterns include checking whether the update is actually needed (comparing old/new values), using a static flag, or setting Flow entry conditions so the Flow doesn't re-trigger on its own resulting update.

**Q25. Answer: C**
A simplified generalized order of execution is: system validation → before triggers → (in more detail: before triggers, then system validation, custom validation rules, duplicate rules) → record saved to database (but not committed) → after triggers → assignment rules → auto-response rules → workflow rules → escalation rules, then commit. Note: real order of execution is more nuanced; check Salesforce's official Order of Execution documentation for the authoritative sequence.

**Q26. Answer: B**
Whether an exception rolls back the entire transaction depends on how DML was executed. A single `insert`/`update` statement without `allOrNone` set to false will fail entirely and roll back on any error; using `Database.insert(records, false)` allows partial success, letting good records commit while bad ones are reported as failures.

**Q27. Answer: A**
Unlike `@future` methods, Queueable Apex can accept complex parameter types (including sObjects and custom classes) since you pass state via the class's member variables in the constructor. Queueable jobs can also be chained (one Queueable enqueuing another).

**Q28. Answer: B**
Custom Apex exception classes must extend the built-in `Exception` class: `public class MyException extends Exception { }`.

**Q29. Answer: A**
The validation rule's formula should evaluate to `TRUE` when the record is invalid (blocking save). `CloseDate < TODAY()` evaluates true when the close date is in the past, triggering the error.

---

## Section 4: User Interface

**Q30. Answer: B**
Lightning Web Components (LWC) is the modern, standards-based JavaScript/HTML framework built natively for the Lightning platform, offering high performance and reusability. Visualforce is the older, page-based markup framework.

**Q31. Answer: B**
`@api` exposes a property (or method) as public, making it accessible/settable by a parent component. `@track` is used for reactivity of private, non-primitive fields (largely automatic in newer LWC versions); `@wire` is for wiring data service calls.

**Q32. Answer: B**
`@wire` connects a component property or function to a data source (like an Apex method or Lightning Data Service), automatically re-invoking it whenever reactive parameters passed to it change.

**Q33. Answer: A**
`<apex:pageBlockTable>` bound to the standard controller's relationship (e.g., `{!Account.Contacts}`) is the standard way to render a related list of child records in Visualforce.

**Q34. Answer: B**
Apex methods called from LWC (or Aura) must be `public` or `global`, typically `static`, and annotated with `@AuraEnabled` (add `cacheable=true` if used with `@wire` for read-only calls).

---

## Section 5: Testing, Debugging, and Deployment

**Q35. Answer: C**
Salesforce requires at least 75% overall Apex code coverage (with no individual trigger having 0% coverage) to deploy to production. Note: 75% is a minimum gate, not a target — good practice aims higher with meaningful assertions.

**Q36. Answer: A**
`@isTest` marks a class (or method) as test-only code. It doesn't count against the org's Apex code size limit and is excluded from production execution paths outside test context.

**Q37. Answer: B**
`Test.startTest()` resets governor limits for the code that follows, giving your test a "fresh" set of limits, and any asynchronous operations (`@future`, Batch, Queueable, scheduled jobs) enclosed between `startTest()` and `stopTest()` are forced to run synchronously by the time `stopTest()` finishes, so you can assert their results.

**Q38. Answer: A**
By default, Apex tests run in an isolated, rolled-back data context and cannot see existing org records (except some configuration data like Users, Profiles, and certain setup objects, or unless `@isTest(SeeAllData=true)` is used, which is discouraged). Tests should create their own data via `@testSetup` or within the test method for reliability and isolation.

**Q39. Answer: B**
`System.assertEquals`, `System.assert`, and `System.assertNotEquals` verify that the actual behavior matches expected outcomes. Code coverage alone (executing lines) does not prove correctness — meaningful assertions are what make a test valuable.

**Q40. Answer: D**
All listed options are valid deployment methods: Change Sets (declarative, org-to-org within the same Salesforce "family"), Salesforce CLI/SFDX (source-driven development, scratch orgs, packages), and Metadata API tools like Workbench or the ANT Migration Tool.

**Q41. Answer: B**
`sf project deploy start` (or the legacy `sfdx force:source:deploy`) deploys local source metadata from your project into a connected/target Salesforce org.

**Q42. Answer: A**
Setup → Debug Logs lets you capture and view detailed Apex execution logs, including SOQL/DML statements executed, governor limit consumption, and flow of execution — essential for debugging.

**Q43. Answer: B**
If the org-wide Apex test coverage falls below 75% (or a covered trigger has 0% coverage) during a deployment to production, the deployment fails/is blocked until sufficient coverage is achieved.

---

## Section 6: Mixed / Scenario-Based

**Q44. Answer: B**
Querying inside a loop for every record in a 300-record bulk operation would issue up to 300 SOQL queries in a single transaction, exceeding the 100-query synchronous governor limit and throwing `System.LimitException: Too many SOQL queries: 101`.

**Q45. Answer: A**
Bulkification means writing Apex (triggers, classes) so that it correctly and efficiently handles collections of records (e.g., up to 200 in a trigger context) rather than assuming only a single record will ever be processed — critical for surviving bulk data operations like imports or API calls.

**Q46. Answer: A**
A common and simple pattern is a static Boolean (or Set of processed Ids) in a helper/utility class that gets checked and set at the start of the trigger logic, preventing the same logic from re-executing multiple times within one transaction due to recursive updates.

**Q47. Answer: B**
Custom Metadata Type records behave like metadata — they can be deployed via change sets/packages across orgs and queried without counting against certain SOQL governor limits in some contexts (though normal SOQL limits still generally apply; some access patterns use the more efficient `getAll()`/schema access). Custom Settings store configuration data (Org-wide or hierarchical, per-profile/user) and are often cached for fast, limit-friendly access within Apex.

**Q48. Answer: B**
Every user has exactly one Profile controlling baseline object/field/system permissions. Permission Sets (and Permission Set Groups) are used to grant additional, granular permissions on top of the Profile without needing to clone/modify profiles — and a user can be assigned multiple Permission Sets.

**Q49. Answer: B**
The synchronous Apex transaction SOQL query limit is 100 (asynchronous/batch context allows 200).

**Q50. Answer: B**
Best practice is to query only the fields and records actually needed (using selective `WHERE` filters and explicit field lists) to avoid excessive heap usage and to stay within the SOQL row limit (50,000 rows retrieved per transaction), especially important on large data sets.

---

## Score Guide

| Score | Assessment |
|---|---|
| 45–50 correct | Exam-ready — strong grasp across all domains |
| 35–44 correct | Solid foundation — review weak sections below |
| 25–34 correct | More study needed, especially Apex/Triggers (Section 3) and Testing (Section 5) |
| Below 25 | Recommend completing the Trailhead "Apex Programming" and "Platform Developer I" trails before attempting the real exam |

**Weakest section tip:** If you missed multiple questions in Section 3 (Logic and Process Automation), that's the single highest-weighted domain on the real PD1 exam — prioritize triggers, bulkification, governor limits, and async Apex (Future/Queueable/Batch/Schedulable) in your review.
