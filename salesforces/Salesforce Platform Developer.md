# Salesforce Platform Developer I (PD1) — Detailed 4-Week Preparation Plan

## For a Java / Spring Boot / Angular Developer

**Target certification:** Salesforce Platform Developer I (PD1)  
**Preparation duration:** 4 weeks  
**Recommended study time:** 1.5–2 hours on weekdays, 3–4 hours on weekends  
**Primary learning resource:** Salesforce Trailhead / the official PD1 preparation Trailmix  
**Approach:** Trailhead + hands-on Salesforce development + scenario-based exam practice

---

# 1. Goal of This Plan

The objective is not merely to complete Trailhead modules.

By the end of the four weeks, you should be able to:

- Understand the Salesforce platform and its data model.
- Build and query Salesforce data using SOQL and SOSL.
- Write Apex classes and triggers.
- Write bulkified Apex.
- Understand and design around governor limits.
- Understand Flow and when to choose Flow vs Apex.
- Understand Salesforce security and sharing.
- Understand Lightning Web Components at a certification level.
- Write Apex test classes.
- Debug Apex and understand deployment concepts.
- Solve scenario-based PD1 questions.
- Identify why an answer is correct rather than memorizing it.
- Decide when you are ready to schedule the exam.

---

# 2. Important Strategy for an Experienced Java Developer

Your Java/Spring Boot/Angular background is a major advantage.

You already understand:

- Object-oriented programming
- Classes and interfaces
- Collections
- REST APIs
- Databases
- SQL
- Transactions
- Unit testing
- Dependency/service layers
- Frontend components
- Debugging

Therefore, **do not spend excessive time learning programming fundamentals**.

Your biggest learning curve will be Salesforce-specific concepts:

1. Salesforce data model
2. Governor limits
3. Bulkification
4. Salesforce security/sharing
5. Flow
6. Trigger execution
7. Salesforce transactions
8. SOQL/SOSL
9. Metadata/deployment
10. Salesforce-specific exam scenarios

---

# 3. Java/Spring Boot → Salesforce Mental Mapping

| Java / Spring / Angular | Salesforce |
|---|---|
| Java class | Apex class |
| Java method | Apex method |
| Java interface | Apex interface |
| Java List | Apex List |
| Java Set | Apex Set |
| Java Map | Apex Map |
| PostgreSQL | Salesforce database |
| SQL | SOQL |
| Full-text/search-style querying | SOSL |
| Service class | Apex service class |
| Database repository | SOQL/DML |
| Spring business logic | Apex / Flow |
| Event-driven business logic | Trigger / Flow |
| JUnit | Apex test class |
| REST API | Salesforce REST API |
| Angular component | Lightning Web Component |
| Angular `@Input` | LWC `@api` |
| Angular `@Output` | LWC custom events |
| Spring Security | Salesforce security model |
| DB transaction | Salesforce transaction |
| Database/resource constraints | Governor limits |
| Build/deployment artifacts | Salesforce metadata |
| Environment | Salesforce Org / Sandbox |
| Application configuration | Salesforce metadata/configuration |

The mapping is useful, but don't assume Salesforce behaves exactly like Spring Boot.

---

# 4. The Biggest Conceptual Shift

A traditional application often looks like:

```text
User
  |
  v
Angular
  |
  v
Spring Controller
  |
  v
Spring Service
  |
  v
Repository
  |
  v
PostgreSQL
```

Salesforce is more platform-controlled:

```text
                    Salesforce Platform

                         User
                           |
                           v
                     Lightning UI
                           |
             +-------------+-------------+
             |                           |
             v                           v
           Flow                         Apex
             |                           |
             +-------------+-------------+
                           |
                           v
                        Trigger
                           |
                           v
                    Salesforce Data
                           ^
                           |
                Security + Limits
```

Salesforce gives you a large amount of application functionality out of the box.

That means the key question often becomes:

> "Should I write code?"

rather than:

> "How do I write the code?"

---

# 5. Recommended Time Allocation

Use approximately:

- **30% Trailhead**
- **30% hands-on development**
- **40% practice questions and revision**

Do not equate Trailhead completion with exam readiness.

Trailhead teaches the platform.

Practice questions teach you how the certification tests your understanding.

---

# 6. Four-Week Overview

## Week 1 — Salesforce Fundamentals and Data

Focus:

- Salesforce architecture
- Objects
- Fields
- Records
- Relationships
- Security
- SOQL
- SOSL
- DML
- Metadata

## Week 2 — Apex and Triggers

Focus:

- Apex syntax
- Collections
- SOQL from Apex
- DML
- Governor limits
- Bulkification
- Triggers
- Trigger context variables
- Transactions
- Asynchronous Apex basics

## Week 3 — Automation, UI, Testing, Deployment

Focus:

- Flow
- Flow vs Apex
- LWC fundamentals
- Apex testing
- Debugging
- Deployment
- Metadata
- Sandbox concepts

## Week 4 — Exam Preparation

Focus:

- Revision
- Scenario-based questions
- Mock exams
- Weak-area analysis
- Final cheat sheet
- Exam readiness

---

# 7. WEEK 1 — Salesforce Fundamentals + Data Model

## Day 1 — Salesforce Architecture

### Learn

- Salesforce Platform
- Salesforce Org
- Standard objects
- Custom objects
- Records
- Fields
- Metadata
- Apps
- Tabs
- Declarative vs programmatic development

### Understand the hierarchy

```text
Salesforce Org
|
+-- Objects
|    |
|    +-- Standard Objects
|    |
|    +-- Custom Objects
|
+-- Records
|
+-- Fields
|
+-- Metadata
|
+-- Automation
|
+-- Security
|
+-- UI
```

### Important questions

You should be able to explain:

- What is an Org?
- What is an Object?
- What is a Record?
- What is a Field?
- What is Metadata?
- What is the difference between standard and custom objects?
- What can be configured declaratively?
- When is Apex required?

### Hands-on

Create a small custom application.

Suggested objects:

```text
Customer
Order
Product
```

Create fields and relationships between them.

---

# 8. Day 2 — Salesforce Data Model

## Objects

Understand:

- Standard objects
- Custom objects
- Standard fields
- Custom fields

Examples:

```text
Account
Contact
Lead
Opportunity
Case
```

Custom objects typically have names ending in:

```text
__c
```

---

## Relationships

Master these concepts:

### Lookup relationship

A relatively flexible relationship between records.

```text
Account
   |
   +---- Contact
```

### Master-detail relationship

A stronger parent-child relationship.

Important concepts:

- Child depends on parent.
- Ownership/security behavior is different.
- Roll-up summary fields are associated with master-detail relationships.
- Deletion behavior matters.

### Junction object

Used to model many-to-many relationships.

```text
Student
   |
   +---- Enrollment ----+
                        |
                        v
                      Course
```

A junction object commonly uses two master-detail relationships.

---

# 9. Day 3 — Salesforce Security

This is a high-priority exam topic.

Learn:

- Profiles
- Permission Sets
- Object-level security
- Field-level security
- Record-level security
- Organization-Wide Defaults (OWD)
- Role hierarchy
- Sharing rules
- Manual sharing

Think about security in layers:

```text
User
 |
 +-- Object permissions
 |
 +-- Field permissions
 |
 +-- Record access
```

### Important distinction

A user may:

- Have access to an object
- Not have access to a field
- Have access to some records
- Not have access to other records

These are different security dimensions.

### Questions to practice

- Can the user access the object?
- Can the user access the field?
- Can the user access this particular record?
- Which mechanism grants that access?

---

# 10. Day 4 — SOQL

Coming from PostgreSQL, SOQL should be relatively easy.

Example:

```sql
SELECT Id, Name
FROM Account
WHERE Industry = 'Technology'
```

Learn:

- SELECT
- WHERE
- ORDER BY
- LIMIT
- GROUP BY
- Aggregate functions
- Relationships
- Parent-to-child queries
- Child-to-parent queries

### Child-to-parent

```sql
SELECT Id, FirstName, Account.Name
FROM Contact
```

### Parent-to-child

```sql
SELECT Id, Name,
       (SELECT Id, FirstName
        FROM Contacts)
FROM Account
```

Understand the direction of the relationship.

---

# 11. Day 5 — SOSL and DML

## SOSL

SOSL is designed for searching across Salesforce data.

Conceptually:

```text
Search term
    |
    +-- Account
    +-- Contact
    +-- Lead
    +-- other searchable objects
```

Understand when SOSL is more appropriate than SOQL.

---

## DML

Learn:

```apex
insert records;
update records;
delete records;
undelete records;
upsert records;
```

Also understand the `Database` class:

```apex
Database.insert(records);
Database.update(records);
Database.delete(records);
```

Study:

- `allOrNone`
- Partial success
- `Database.SaveResult`
- DML statements vs Database methods

---

# 12. Week 1 Weekend — Hands-On Mini Project

Create:

```text
Customer
   |
   +---- Contact
   |
   +---- Order
            |
            +---- Product
```

Practice:

1. Create objects.
2. Create fields.
3. Create relationships.
4. Insert records.
5. Query records.
6. Update records.
7. Delete records.
8. Write relationship queries.
9. Experiment with permissions.
10. Observe record visibility.

### Week 1 checkpoint

You should confidently explain:

- Object vs record vs field
- Standard vs custom object
- Lookup vs master-detail
- Junction object
- SOQL vs SOSL
- DML vs Database methods
- Profile vs permission set
- Object vs field vs record security

---

# 13. WEEK 2 — Apex + Governor Limits + Triggers

This is one of the most important weeks.

## Day 6 — Apex Fundamentals

Learn:

- Classes
- Methods
- Constructors
- Variables
- Access modifiers
- Static members
- Interfaces
- Exceptions
- Collections

Example:

```apex
public class AccountService {

    public static void processAccounts(
        List<Account> accounts
    ) {
        // business logic
    }
}
```

This should feel familiar from Java.

---

# 14. Day 7 — Apex Collections

Master:

```text
List
Set
Map
```

Example:

```apex
List<Account> accounts;
```

```apex
Set<Id> accountIds;
```

```apex
Map<Id, Account> accountsById;
```

Understand why Maps are particularly useful in bulk processing.

Example:

```apex
Map<Id, Account> accountMap =
    new Map<Id, Account>(accounts);
```

You should be comfortable retrieving records by Id without repeatedly searching a List.

---

# 15. Day 8 — Governor Limits

This is one of the most important Salesforce concepts.

Salesforce limits how many resources a transaction can consume.

Think:

```text
Salesforce Transaction
|
+-- SOQL queries
|
+-- DML statements
|
+-- CPU time
|
+-- Heap size
|
+-- Other platform limits
```

Why?

Salesforce is a multi-tenant platform.

Your code shares platform resources with code from many other customers.

Therefore Salesforce enforces limits to protect the platform.

---

# 16. Day 9 — Bulkification

This is extremely important.

## Bad

```apex
for (Account a : accounts) {

    List<Contact> contacts = [
        SELECT Id
        FROM Contact
        WHERE AccountId = :a.Id
    ];
}
```

This performs SOQL repeatedly inside a loop.

## Better

```apex
Set<Id> accountIds = new Set<Id>();

for (Account a : accounts) {
    accountIds.add(a.Id);
}

List<Contact> contacts = [
    SELECT Id, AccountId
    FROM Contact
    WHERE AccountId IN :accountIds
];
```

The general pattern is:

```text
Collect IDs
     |
     v
Run one query
     |
     v
Build Map
     |
     v
Process records
     |
     v
Perform bulk DML
```

### Exam rule

Whenever you see:

```text
SOQL inside loop
```

or:

```text
DML inside loop
```

immediately investigate whether the code violates bulkification principles.

---

# 17. Day 10 — Triggers

Learn:

```apex
trigger AccountTrigger
on Account (before insert, before update) {

}
```

Understand trigger events:

- before insert
- after insert
- before update
- after update
- before delete
- after delete
- after undelete

Learn:

```text
Trigger.new
Trigger.old
Trigger.newMap
Trigger.oldMap
```

Understand which context variables are available in each event.

---

# 18. Trigger Design

Avoid putting a large amount of business logic directly inside a trigger.

A common architecture is:

```text
Trigger
   |
   v
Handler
   |
   v
Service
   |
   v
SOQL / DML
```

Example conceptual structure:

```text
AccountTrigger
      |
      v
AccountTriggerHandler
      |
      v
AccountService
      |
      +-- Query
      +-- Business logic
      +-- DML
```

For the exam, focus on behavior and platform rules first.

---

# 19. Transactions

Understand that a Salesforce transaction is a unit of work.

Multiple operations can happen within the same transaction:

```text
Request
  |
  +-- Flow
  +-- Trigger
  +-- Apex
  +-- DML
  +-- More automation
  |
  +-- Commit
```

Governor limits apply within transaction contexts.

This is why repeated queries and DML operations matter.

---

# 20. Asynchronous Apex

Learn the purpose and basic characteristics of:

- Future methods
- Queueable Apex
- Batch Apex
- Scheduled Apex

Know why asynchronous processing is useful.

Conceptually:

```text
Synchronous

Request
  |
  +-- process
  |
  +-- response


Asynchronous

Request
  |
  +-- enqueue work
  |
  +-- response
          |
          v
      background job
```

You do not need to become an advanced asynchronous Apex developer for PD1, but understand the differences and appropriate use cases.

---

# 21. Week 2 Weekend — Apex Mini Project

Create:

```text
Account
   |
   +-- Contact
   |
   +-- Opportunity
```

Requirement:

> Whenever an Account changes, perform business logic involving related Contacts.

Implement:

1. Trigger
2. Handler
3. Service class
4. SOQL
5. Map/Set
6. Bulk processing
7. DML
8. Test class

Then intentionally write an unbulkified version.

Understand why the first version fails at scale.

---

# 22. WEEK 3 — Flow + LWC + Testing + Deployment

## Day 11 — Flow

Learn:

- Record-triggered Flow
- Screen Flow
- Scheduled Flow
- Before-save Flow
- After-save Flow

Understand the purpose of Flow as Salesforce's declarative automation platform.

---

# 23. Flow vs Apex

This is an important exam skill.

Think:

```text
Requirement
    |
    v
Can declarative automation handle it?
    |
   Yes
    |
    v
Flow
```

If the requirement needs more complex programmatic behavior:

```text
Requirement
    |
    v
Complex / programmatic logic
    |
    v
Apex
```

Do not memorize an oversimplified rule.

Instead learn:

- What Flow can do
- What Flow cannot do
- What Apex can do
- Governor-limit implications
- Maintainability
- Transaction behavior
- When declarative automation is preferred

---

# 24. Day 12 — Automation Scenarios

Practice scenarios such as:

### Scenario A

"When an Opportunity is created, update a field on the Account."

Ask:

```text
Flow?
Trigger?
Apex?
```

### Scenario B

"Complex processing across several objects with custom algorithms."

Ask:

```text
Flow?
Apex?
```

### Scenario C

"Simple record-based automation."

Consider:

```text
Record-triggered Flow
```

The exam often tests the best platform solution rather than whether you can write code.

---

# 25. Day 13 — Lightning Web Components

Because you know Angular, LWC should be manageable.

Understand:

```text
LWC
|
+-- HTML
|
+-- JavaScript
|
+-- XML metadata
```

Learn:

- Component structure
- Properties
- `@api`
- Events
- Reactive behavior
- Wire service
- Apex integration
- Lifecycle basics

---

# 26. Angular → LWC Comparison

| Angular | LWC |
|---|---|
| Component | Component |
| Template | HTML template |
| `@Input()` | `@api` |
| `@Output()` | Custom event |
| Service | Apex / JavaScript service patterns |
| Observable | Wire / reactive mechanisms |
| Lifecycle hooks | LWC lifecycle hooks |

Do not spend too much time becoming an advanced LWC developer.

For PD1, learn the concepts required to understand and answer certification questions.

---

# 27. Day 14 — Apex Testing

Learn:

```apex
@isTest
private class AccountServiceTest {

    @isTest
    static void testAccountCreation() {

        Account account = new Account(
            Name = 'Test Account'
        );

        insert account;

        System.assertNotEquals(
            null,
            account.Id
        );
    }
}
```

Important topics:

- `@isTest`
- Test classes
- Test methods
- Test data
- Assertions
- `Test.startTest()`
- `Test.stopTest()`
- Governor limits in tests
- Code coverage
- Testing positive and negative cases

Do not think:

> "The purpose of a test is just to increase code coverage."

The test should validate behavior.

---

# 28. Day 15 — Debugging and Deployment

Learn:

- Debug logs
- Developer Console
- Debugging Apex
- Execution logs
- Metadata
- Change Sets
- Sandbox
- Production
- Deployment concepts

Conceptual environment:

```text
Development
     |
     v
Sandbox
     |
     v
Production
```

Understand what Salesforce metadata represents and how configuration/development artifacts move between environments.

---

# 29. Week 3 Weekend — Full Mini Project

Build:

```text
                    Salesforce

                      Account
                         |
              +----------+----------+
              |                     |
              v                     v
           Contact             Opportunity
                                    |
                                    v
                           Opportunity Product
```

Implement:

### Data

- Account
- Contact
- Opportunity
- Product

### Automation

- Flow
- Apex

### Backend

- SOQL
- DML
- Trigger
- Service class

### UI

- Basic LWC

### Testing

- Apex test classes

### Deployment

Understand which metadata/configuration would need to move.

---

# 30. WEEK 4 — Exam Preparation

At this point, stop trying to learn every Salesforce feature.

Switch to certification mode.

---

# 31. Day 16 — Developer Fundamentals Revision

Review:

- Salesforce architecture
- Data model
- Objects
- Relationships
- Security
- Metadata
- Deployment
- APIs
- Declarative vs programmatic development

Create a one-page summary.

---

# 32. Day 17 — Apex Revision

Review heavily:

```text
Apex
 |
 +-- Classes
 |
 +-- Collections
 |
 +-- SOQL
 |
 +-- DML
 |
 +-- Triggers
 |
 +-- Governor limits
 |
 +-- Bulkification
 |
 +-- Transactions
 |
 +-- Asynchronous Apex
```

You should be able to explain every item without notes.

---

# 33. Day 18 — Automation + UI

Review:

```text
Flow
Apex
Triggers
LWC
Visualforce
```

Focus on:

> "Which Salesforce tool should solve this problem?"

rather than:

> "Can I implement this in Apex?"

---

# 34. Day 19 — Testing + Deployment

Review:

- Test classes
- Assertions
- `Test.startTest()`
- `Test.stopTest()`
- Code coverage
- Debugging
- Logs
- Sandboxes
- Metadata
- Deployment

Then take a full practice exam.

---

# 35. Day 20 — Mock Exam

Take approximately 60 questions under exam-like conditions.

Rules:

- No notes
- No Google
- Time yourself
- Mark uncertain questions
- Review every incorrect answer

Track results:

| Question | Result | Topic | Why wrong? |
|---|---|---|---|
| 1 | Correct | SOQL | — |
| 2 | Wrong | Security | Confused record vs object access |
| 3 | Wrong | Flow | Picked Apex unnecessarily |
| 4 | Correct | Apex | — |

---

# 36. Day 21 — Final Revision

Spend:

- 60% of time on weak areas
- 40% on overall revision

Example:

```text
SOQL          90%
Apex          85%
Security      60%  <-- focus
Flow          70%  <-- focus
Testing       90%
LWC           80%
Deployment    75%
```

Do not waste your final day rereading topics you already know extremely well.

---

# 37. High-Priority Topic List

## Tier 1 — Must Know

1. Governor limits
2. Bulkification
3. Apex
4. SOQL
5. Triggers
6. Salesforce data model
7. Security and sharing
8. Flow
9. Testing

## Tier 2 — Important

10. DML
11. Database methods
12. Asynchronous Apex
13. LWC fundamentals
14. Debugging
15. Deployment
16. Relationships
17. Transactions

## Tier 3 — Do Not Over-Invest

18. Advanced LWC
19. Advanced Visualforce
20. Advanced UI development
21. Advanced integration architecture

---

# 38. Governor Limits — Study Checklist

You should understand:

- Why governor limits exist
- What a transaction is
- SOQL query limits
- DML limits
- CPU time
- Heap size
- How loops interact with limits
- How bulkification avoids limits
- Synchronous vs asynchronous contexts at a conceptual level

The exact current numerical limits should be learned from Salesforce's current documentation/material rather than relying on an old cheat sheet.

---

# 39. Bulkification — Study Checklist

You should recognize these patterns immediately:

### Bad

```apex
for (Account a : accounts) {
    update a;
}
```

### Better

```apex
for (Account a : accounts) {
    // modify
}

update accounts;
```

And:

### Bad

```apex
for (Account a : accounts) {

    List<Contact> contacts = [
        SELECT Id
        FROM Contact
        WHERE AccountId = :a.Id
    ];
}
```

### Better

```apex
Set<Id> accountIds = new Set<Id>();

for (Account a : accounts) {
    accountIds.add(a.Id);
}

List<Contact> contacts = [
    SELECT Id, AccountId
    FROM Contact
    WHERE AccountId IN :accountIds
];
```

---

# 40. Trigger Context Checklist

Know:

```text
Trigger.new
Trigger.old
Trigger.newMap
Trigger.oldMap
```

And know when each is available.

Also understand:

- Before vs after
- Insert
- Update
- Delete
- Undelete

---

# 41. Order of Execution

Study Salesforce's current official order-of-execution documentation.

A simplified mental model is:

```text
Record Save
    |
    v
Validation / Initial Processing
    |
    v
Before-save automation
    |
    v
Before Trigger
    |
    v
Validation / Save Processing
    |
    v
After Trigger
    |
    v
Other Automation
    |
    v
After-save processing
    |
    v
Commit
```

Important:

**Do not memorize this simplified diagram as the official exact order.**

Salesforce's actual order contains more stages and has changed over time.

For the exam, understand the interactions between:

- Flows
- Triggers
- Validation rules
- Workflow/automation
- DML
- Commit

---

# 42. Security Checklist

Be able to distinguish:

```text
Object access
     |
     v
Field access
     |
     v
Record access
```

Study:

- Profiles
- Permission Sets
- OWD
- Role hierarchy
- Sharing rules
- Manual sharing
- Field-level security
- Object permissions

Scenario question:

> User can access Account but cannot see a particular field.

Think:

```text
Field-Level Security
```

Another:

> User can access Account records but cannot access a particular Account record.

Think:

```text
Record-level security
```

---

# 43. SOQL Checklist

Master:

- Basic SELECT
- WHERE
- ORDER BY
- LIMIT
- Aggregate functions
- GROUP BY
- Parent-to-child queries
- Child-to-parent queries
- Relationship fields
- SOQL from Apex
- Bind variables
- Bulk querying

---

# 44. Flow Checklist

Understand:

- Record-triggered Flow
- Screen Flow
- Scheduled Flow
- Before-save Flow
- After-save Flow
- Variables
- Decisions
- Get Records
- Create Records
- Update Records
- Delete Records
- Flow invocation and automation context

Most importantly:

> Know when Flow is preferable to Apex.

---

# 45. Apex Testing Checklist

Know:

```apex
@isTest
```

```apex
Test.startTest();
```

```apex
Test.stopTest();
```

And assertions:

```apex
System.assertEquals(expected, actual);
```

Understand:

- Test data
- Isolation
- Positive cases
- Negative cases
- Exception testing
- Governor limits
- Async testing
- Code coverage

---

# 46. Practice Question Strategy

Do not simply mark:

```text
Wrong answer -> memorize correct answer
```

Instead record:

```text
Question:
My answer:
Correct answer:
Why my answer was wrong:
Salesforce concept:
Rule to remember:
```

Example:

```text
Question:
100 records need to be processed.

My answer:
Perform SOQL inside the loop.

Correct answer:
Query all required records once.

Concept:
Bulkification.

Rule:
Avoid SOQL and DML inside loops.
```

This method is much more valuable.

---

# 47. Scenario-Based Thinking

PD1 questions often describe a business requirement.

Your thinking process should be:

```text
Requirement
     |
     v
What Salesforce feature solves this?
     |
     +-- Data model?
     |
     +-- Security?
     |
     +-- Flow?
     |
     +-- Apex?
     |
     +-- Trigger?
     |
     +-- LWC?
     |
     +-- Async Apex?
     |
     v
What platform limitation applies?
     |
     v
Choose the best solution
```

---

# 48. Common Traps for Java Developers

## Trap 1 — Treating Salesforce like PostgreSQL

Don't assume:

```text
"I can just query the database whenever I want."
```

Salesforce has platform query limits.

---

## Trap 2 — Treating Apex like normal Java

Apex syntax may feel familiar, but Salesforce execution rules are different.

You need to think about:

- Governor limits
- Bulkification
- Transactions
- Sharing
- Security
- Platform automation

---

## Trap 3 — Choosing Apex for everything

Because you are a backend developer, your instinct may be:

> "I'll write a service/trigger."

But Salesforce often expects a declarative solution where appropriate.

Always ask:

```text
Can Flow solve this?
```

---

## Trap 4 — Ignoring security

A Java developer may focus on:

```text
Controller
Service
Repository
```

Salesforce questions frequently care about:

```text
Who can see this?
Who can edit this?
Which records are accessible?
Which field is visible?
```

---

## Trap 5 — Memorizing code instead of behavior

The exam is not simply an Apex syntax test.

Understand:

```text
Why?
When?
What limitation?
What platform feature?
What execution context?
```

---

# 49. Suggested Hands-On Exercises

## Exercise 1 — Data Model

Create:

```text
Customer
Order
Product
OrderItem
```

Model:

```text
Customer 1 ---- * Order
Order    1 ---- * OrderItem
Product  1 ---- * OrderItem
```

---

## Exercise 2 — SOQL

Write queries to:

1. Find all customers.
2. Find customers by a field.
3. Find orders for a customer.
4. Find order items.
5. Query parent fields from child records.
6. Query child records from parent records.
7. Perform an aggregate query.

---

## Exercise 3 — Apex

Create:

```text
OrderService
```

Implement:

- Create orders
- Update orders
- Query orders
- Process multiple orders

---

## Exercise 4 — Bulkification

Create a deliberately bad implementation.

Then rewrite it using:

```text
List
Set
Map
```

---

## Exercise 5 — Trigger

Create a trigger on Order.

Implement a business rule.

Make sure the implementation supports multiple records.

---

## Exercise 6 — Flow

Create a record-triggered Flow for a simple business requirement.

Then ask:

> Could this be done in Apex?

Understand why Flow may be preferable.

---

## Exercise 7 — LWC

Build a simple component that:

- Displays records.
- Calls Apex.
- Displays results.
- Handles an event.

---

## Exercise 8 — Testing

Create tests for:

- Successful processing
- Empty input
- Invalid input
- Multiple records
- Exception cases

---

# 50. Exam Readiness Checklist

Before booking the exam, you should be able to answer "yes" to most of these.

## Platform

- [ ] I understand Salesforce Org.
- [ ] I understand objects, records and fields.
- [ ] I understand standard vs custom objects.
- [ ] I understand relationships.

## Data

- [ ] I understand Lookup.
- [ ] I understand Master-Detail.
- [ ] I understand Junction Objects.
- [ ] I can write basic SOQL.
- [ ] I understand relationship queries.
- [ ] I understand SOSL.
- [ ] I understand DML.

## Apex

- [ ] I can write Apex classes.
- [ ] I understand List, Set and Map.
- [ ] I can write SOQL from Apex.
- [ ] I understand DML.
- [ ] I understand exceptions.
- [ ] I understand triggers.

## Governor Limits

- [ ] I understand why limits exist.
- [ ] I understand bulkification.
- [ ] I can identify SOQL inside loops.
- [ ] I can identify DML inside loops.
- [ ] I understand transaction boundaries.

## Automation

- [ ] I understand Flow.
- [ ] I understand record-triggered Flow.
- [ ] I understand before-save vs after-save concepts.
- [ ] I can decide between Flow and Apex.

## Security

- [ ] I understand profiles.
- [ ] I understand permission sets.
- [ ] I understand OWD.
- [ ] I understand role hierarchy.
- [ ] I understand sharing rules.
- [ ] I understand field-level security.
- [ ] I understand record-level security.

## UI

- [ ] I understand LWC basics.
- [ ] I understand component communication.
- [ ] I understand Apex-LWC interaction.

## Testing

- [ ] I understand `@isTest`.
- [ ] I understand assertions.
- [ ] I understand `Test.startTest()`.
- [ ] I understand `Test.stopTest()`.
- [ ] I understand test data.
- [ ] I understand code coverage.

## Deployment

- [ ] I understand metadata.
- [ ] I understand sandbox concepts.
- [ ] I understand deployment/change sets at a certification level.

## Exam Practice

- [ ] I have completed multiple mock exams.
- [ ] I consistently score around 80–85%+.
- [ ] I understand why my wrong answers are wrong.
- [ ] I can answer scenario questions without notes.

---

# 51. Daily Study Template

Use this structure every day.

## First 30–45 minutes

Trailhead.

Focus on understanding, not speed.

## Next 30–45 minutes

Hands-on.

Create something in your Salesforce environment.

## Final 20–30 minutes

Revision/questions.

Write:

```text
Today I learned:
1.
2.
3.

Things I still don't understand:
1.
2.

Important exam concept:
1.
```

---

# 52. What to Do While You Are Starting the Trailmix

For now, **start the Trailmix you linked**.

Don't try to follow this roadmap rigidly if the Trailmix changes the order slightly.

Use the Trailmix as your primary curriculum and this document as your:

- Study checklist
- Topic map
- Hands-on guide
- Revision guide
- Exam preparation plan

Whenever a Trailhead module introduces a topic, map it to the relevant section of this document.

Example:

```text
Trailhead teaches SOQL
        |
        v
Come here:
"SOQL Checklist"
        |
        v
Practice:
Write 5–10 queries
        |
        v
Exam:
Solve scenario questions
```

---

# 53. Recommended Learning Loop

For every Salesforce topic:

```text
Trailhead
   |
   v
Understand concept
   |
   v
Build it yourself
   |
   v
Break it intentionally
   |
   v
Understand the error/limitation
   |
   v
Solve scenario questions
   |
   v
Write your own rule
```

This is much more effective than simply completing modules.

---

# 54. Final 4-Week Target

At the end of Week 4, you should have:

```text
Trailhead
   +
Hands-on Org
   +
Apex Practice
   +
Flow Practice
   +
SOQL Practice
   +
Security Understanding
   +
Testing
   +
Mock Exams
   =
PD1 Exam Readiness
```

---

# 55. Most Important Principles to Remember

If you remember only a few things, remember these:

### 1. Salesforce is a platform, not just a programming language.

### 2. Governor limits affect how you design Apex.

### 3. Bulkification is fundamental.

### 4. SOQL/DML should generally be designed for collections rather than individual records.

### 5. Security is part of application design.

### 6. Always consider declarative automation before writing Apex when appropriate.

### 7. Triggers must be designed for bulk operations.

### 8. Understand Flow, Apex, triggers and LWC as different platform tools.

### 9. Testing is about validating behavior, not merely coverage.

### 10. PD1 is scenario-oriented, so learn the "why", not just the syntax.

---

# 56. Your Starting Point

### Right now

1. Start the Salesforce PD1 Trailmix.
2. Complete the first module.
3. Create/use a Salesforce Developer environment for hands-on practice.
4. Do not worry about memorizing everything.
5. Keep notes specifically for Salesforce concepts that are unfamiliar from Java/Spring.
6. After each major topic, practice it yourself.

### After finishing the first major section

Return to this roadmap and mark off the relevant topics.

The goal is:

```text
Learn → Build → Practice → Review → Test
```

not:

```text
Trailhead → Trailhead → Trailhead → Exam
```

---

# 57. Official Resources

Use Salesforce's current official documentation and Trailhead material for exact platform behavior and current limits, because Salesforce features and limits can change.

Primary resources:

- Salesforce Trailhead
- Salesforce Platform Developer I certification study guide
- Salesforce official documentation
- Salesforce Apex Developer Guide
- Salesforce SOQL/SOSL documentation
- Salesforce Flow documentation
- Salesforce Lightning Web Components documentation

For numerical governor limits, order of execution, and exam-specific details, always prefer the current Salesforce documentation over old blog posts or old PDFs.

---

# 58. Suggested Progress Tracker

Copy this section and update it as you progress.

## Week 1

- [ ] Salesforce architecture
- [ ] Objects
- [ ] Fields
- [ ] Records
- [ ] Relationships
- [ ] Security
- [ ] SOQL
- [ ] SOSL
- [ ] DML
- [ ] Week 1 project

## Week 2

- [ ] Apex fundamentals
- [ ] List
- [ ] Set
- [ ] Map
- [ ] SOQL in Apex
- [ ] DML in Apex
- [ ] Governor limits
- [ ] Bulkification
- [ ] Triggers
- [ ] Trigger context variables
- [ ] Transactions
- [ ] Async Apex basics
- [ ] Week 2 project

## Week 3

- [ ] Flow
- [ ] Flow vs Apex
- [ ] Record-triggered Flow
- [ ] LWC
- [ ] Apex-LWC interaction
- [ ] Apex testing
- [ ] Debugging
- [ ] Deployment
- [ ] Week 3 project

## Week 4

- [ ] Fundamentals revision
- [ ] Apex revision
- [ ] Security revision
- [ ] Flow revision
- [ ] Testing revision
- [ ] Deployment revision
- [ ] Mock exam #1
- [ ] Weak-area analysis
- [ ] Mock exam #2
- [ ] Final revision
- [ ] Readiness check

---

# 59. Final Advice

Do not rush the first week.

Salesforce has a lot of terminology that will initially feel unfamiliar even though you are an experienced developer.

Once the Salesforce mental model clicks, your Java background should make:

- Apex
- Collections
- SOQL
- DML
- Testing
- APIs

much easier.

The areas where you should deliberately slow down are:

**Salesforce security + governor limits + bulkification + Flow + platform execution behavior.**

Those are the areas where an experienced traditional backend developer can still make mistakes because the platform behaves differently from a normal Spring Boot/PostgreSQL application.
