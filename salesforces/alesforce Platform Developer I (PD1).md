# Salesforce Platform Developer I (PD1)
# Self-Contained Certification Study + Hands-On Lab Manual

## Who This Is For

This manual is tailored for a Salesforce developer who already has approximately 10 months of hands-on experience with:

- Apex
- LWC
- Flow
- Fields
- Objects
- Salesforce configuration
- General Salesforce development

The goal is **not** to teach Salesforce from zero.

The goal is:

> **Turn your existing practical Salesforce experience into PD1 certification-level theory, scenario-solving ability, and hands-on confidence.**

You should be able to work through this document without needing another study guide for normal topics.

Use Salesforce's current official documentation when an exact platform limit, release-specific behavior, or feature detail may have changed.

---

# How To Use This Manual

Every topic follows the same pattern:

1. **What is it?**
2. **What you need to know for PD1**
3. **Simple example**
4. **Important distinctions**
5. **Hands-on exercise — exact sandbox steps**
6. **What you should observe**
7. **Possible exam questions**
8. **Exam memory**

Do the hands-on exercises in your sandbox.

For concepts you already use every day, do the exercise once as **verification**, not as beginner training.

For concepts you rarely use, perform the exercise carefully and record what Salesforce actually does.

---

# PD1 Certification Structure

The current Salesforce Platform Developer I certification preparation material is organized around four areas:

| Area | Weight |
|---|---:|
| Developer Fundamentals | 27% |
| Process Automation and Logic | 28% |
| User Interface | 25% |
| Testing, Debugging, and Deployment | 20% |
| **Total** | **100%** |

Your practical experience means you can move quickly through familiar implementation topics, but you should not skip the theory behind them.

---

# 1. Salesforce Platform Fundamentals

## 1.1 What is Salesforce?

Salesforce is a cloud-based CRM and application-development platform.

It provides:

- CRM applications
- Data storage
- Security
- Automation
- APIs
- Apex
- LWC
- Flow
- Reporting
- Integration capabilities
- Custom objects and fields

For PD1, think of Salesforce as both:

```text
CRM Application
       +
Development Platform
```

### Exam perspective

Salesforce is a **multitenant platform**. Multiple customers use shared underlying infrastructure while their data and configurations remain logically isolated.

### Exam memory

> Salesforce = CRM + customizable cloud platform.

---

# 1.2 What is an Org?

A Salesforce **org** is an environment containing an organization's Salesforce data, users, configuration, customizations, metadata, security settings, and applications.

Think:

```text
Salesforce Org
│
├── Users
├── Data
├── Objects
├── Fields
├── Automation
├── Apex
├── LWC
├── Security
├── Reports
└── Metadata / Configuration
```

### Hands-on

You do not need to create anything.

1. Open Salesforce.
2. Click the **Setup gear**.
3. Click **Setup**.
4. Search **Company Information**.
5. Open **Company Information**.
6. Identify:
   - Organization Name
   - Organization ID
   - Edition
   - Instance

### What you should understand

You are looking at information identifying the Salesforce environment in which you are working.

### Exam memory

> **Org = Salesforce environment containing data, configuration, users, security and metadata.**

---

# 1.3 What is Multitenancy?

Multitenancy means Salesforce serves multiple customers using shared underlying platform infrastructure.

Conceptually:

```text
                 Salesforce Platform
                        |
        +---------------+---------------+
        |               |               |
       Org A           Org B           Org C
        |               |               |
     Customer A      Customer B      Customer C
```

Customers share platform resources, but Salesforce isolates their data and enforces platform limits.

### Why Governor Limits Exist

One customer should not consume unlimited shared resources and negatively affect other customers.

Therefore Salesforce enforces limits around things such as:

- SOQL
- DML
- CPU
- Heap
- Callouts
- Queries
- Other transaction resources

### Exam memory

> **Multitenancy → shared resources → governor limits.**

---

# 1.4 What is an Object?

An object is a Salesforce structure used to store a particular type of data.

Database analogy:

> Object ≈ table.

Examples:

- Account
- Contact
- Opportunity
- Case

Custom examples:

- Project__c
- Invoice__c
- Employee__c

### Standard vs Custom

**Standard object:** supplied by Salesforce.

**Custom object:** created by the organization.

Custom objects generally use:

```text
__c
```

### Hands-on

1. Go to **Setup**.
2. Open **Object Manager**.
3. Search **Account**.
4. Open it.
5. Open **Fields & Relationships**.
6. Inspect the standard fields.

Then search for one custom object already present in your org.

### What you should observe

Account is provided by Salesforce.

Your custom object is part of your organization's configuration.

### Exam memory

> Object = structure/type of Salesforce data.

---

# 1.5 What is a Record?

A record is one individual instance of an object.

```text
Object: Account

Records:
- TCS
- Infosys
- Accenture
```

Database analogy:

> Record ≈ row.

### Hands-on

1. Open **Accounts**.
2. Click **New**.
3. Set:

```text
Account Name = PD1 Test Account
```

4. Save.

You have created:

```text
Object = Account
Record = PD1 Test Account
```

### Exam memory

> Object defines the type; record is one instance.

---

# 1.6 What is a Field?

A field stores one piece of information about a record.

Example:

```text
Account
│
├── Name
├── Phone
├── Website
├── Industry
└── Annual Revenue
```

Database analogy:

> Field ≈ column.

### Exam memory

> Field = one piece of information stored on a record.

---

# 1.7 Standard Field vs Custom Field

A standard field is supplied by Salesforce.

Examples:

- Account Name
- Phone
- Website
- Industry

A custom field is created by the organization.

Custom fields generally use:

```text
__c
```

### Hands-on — Create a Custom Field

Create this on **Account**:

```text
Field Label:
Customer Tier

Field Type:
Picklist

Values:
Gold
Silver
Bronze
```

### Steps

1. Setup → **Object Manager**.
2. Account.
3. **Fields & Relationships**.
4. Click **New**.
5. Select **Picklist**.
6. Click **Next**.
7. Field Label:

```text
Customer Tier
```

8. Add values:

```text
Gold
Silver
Bronze
```

9. Keep the generated API name:

```text
Customer_Tier__c
```

10. Configure field-level security as appropriate for your sandbox.
11. Add it to the Account page layout.
12. Save.

### Verify

Open **PD1 Test Account** and set:

```text
Customer Tier = Gold
```

### Important distinction

```text
Customer_Tier__c
```

is the field definition/API name.

```text
Gold
```

stored on an Account record is data.

### Exam memory

> Custom field definition = metadata; value stored on a record = data.

---

# 1.8 What is Metadata?

Metadata describes the structure and configuration of Salesforce.

Examples:

- Object definitions
- Field definitions
- Picklist values
- Page layouts
- Flows
- Apex classes
- Triggers
- LWC metadata
- Permission configuration
- Custom metadata configuration

### Data vs Metadata

```text
DATA
-----
PD1 Test Account
Customer Tier = Gold


METADATA
--------
Customer_Tier__c
Type = Picklist
Values = Gold, Silver, Bronze
```

### Hands-on

Use the field you just created.

1. Open Setup.
2. Object Manager → Account.
3. Fields & Relationships.
4. Open **Customer Tier**.

Observe:

- Field Label
- Field Name
- Data Type
- Picklist values
- Field-level security

These definitions are configuration/metadata.

### Exam memory

> Data is business information; metadata describes/configures the application.

---

# 1.9 What is an App?

A Salesforce app is a configured collection of functionality/navigation intended for a particular business purpose.

An app may provide access to:

- Objects
- Tabs/navigation items
- Utilities
- Other configured features

### Hands-on

1. Open the **App Launcher**.
2. Open **Sales**.
3. Inspect the navigation bar.

Identify items such as:

- Accounts
- Contacts
- Leads
- Opportunities

### Exam memory

> App = configured collection of relevant Salesforce functionality/navigation.

---

# 1.10 What is a Tab?

A tab provides UI navigation/access to Salesforce functionality, commonly an object.

For example:

```text
Accounts
Contacts
Opportunities
```

may appear as navigation items.

### Important distinction

```text
Object
= stores/manages data

Tab
= provides UI access/navigation
```

### Exam memory

> Do not confuse an object with its tab.

---

# 1.11 What is Declarative Development?

Declarative development means configuring Salesforce using built-in platform tools rather than writing traditional code.

Examples:

- Flow
- Validation Rules
- Formula Fields
- Approval Processes
- Custom Fields
- Custom Objects
- Page Layouts
- Security configuration

### Exam perspective

Prefer a declarative solution when it appropriately satisfies the requirement.

Do not interpret that as:

> Always use Flow.

The correct solution depends on requirements, complexity, performance, maintainability, limits, and platform capabilities.

---

# 1.12 What is Programmatic Development?

Programmatic development means implementing functionality using code.

Examples:

- Apex
- Apex Triggers
- Apex Classes
- LWC JavaScript
- Visualforce where applicable

### Exam memory

```text
Declarative
→ Configure

Programmatic
→ Code
```

---

# 1.13 Declarative vs Programmatic — Hands-On Decision Exercise

Create this requirement:

> When an Account's Customer Tier changes to Gold, update a field called Priority Customer to true.

First ask:

> Can Salesforce configuration solve this?

Yes.

A Flow can perform this.

### Do not build the solution yet.

Instead, identify the design choice.

Now consider:

> Complex processing must call custom reusable Apex logic and process a large amount of related data.

Now programmatic Apex may be more appropriate.

### Exam takeaway

The exam often asks:

> Which solution is most appropriate?

Do not automatically select the technology you personally use most.

---

# 1.14 Object vs Record vs Field vs Tab vs App vs Metadata

Memorize this:

```text
APP
 ↓
TAB / NAVIGATION
 ↓
OBJECT
 ↓
RECORD
 ↓
FIELD
```

And:

```text
METADATA
= definitions/configuration

DATA
= actual business information
```

---

# 2. Salesforce Data Model

# 2.1 What is the Salesforce Data Model?

The data model describes:

- Objects
- Fields
- Relationships
- Records
- How business information is connected

Example:

```text
Account
   |
   +---- Contact
   |
   +---- Opportunity
   |
   +---- Case
```

---

# 2.2 What is a Standard Object?

A standard object is supplied by Salesforce.

Examples:

- Account
- Contact
- Lead
- Opportunity
- Case
- User

### Hands-on

1. Setup.
2. Object Manager.
3. Search Account.
4. Open Account.
5. Inspect Fields & Relationships.

Identify at least five standard fields.

---

# 2.3 What is a Custom Object?

A custom object is created by the organization.

### Hands-on — Create a PD1 Lab Object

Create:

```text
Label:
PD1 Project

Plural Label:
PD1 Projects

Object Name/API Name:
PD1_Project__c
```

### Steps

1. Setup.
2. Object Manager.
3. Click **Create** → **Custom Object**.
4. Label:

```text
PD1 Project
```

5. Plural Label:

```text
PD1 Projects
```

6. Object Name should become:

```text
PD1_Project
```

7. Enable appropriate options for reporting/searching as available.
8. Save.

### Verify

Open the object and inspect:

- Fields
- Page Layouts
- Buttons/Links
- Record Types
- Validation Rules

### Exam memory

> Custom object = organization-defined data structure.

---

# 2.4 What is a Lookup Relationship?

A lookup relationship connects two objects without the same level of dependency as master-detail.

Example:

```text
Project__c
   |
   +---- Lookup → Account
```

### Hands-on

Create a Lookup field on `PD1_Project__c`:

```text
Field Label:
Customer Account

Type:
Lookup Relationship

Related Object:
Account
```

### Steps

1. Setup → Object Manager.
2. Open **PD1 Project**.
3. Fields & Relationships.
4. New.
5. Select **Lookup Relationship**.
6. Select **Account**.
7. Label:

```text
Customer Account
```

8. Save.
9. Add to layout if required.

### Verify

Create a PD1 Project record.

Select an Account.

Observe that the project can reference the Account.

### Exam memory

> Lookup creates a relationship but does not impose all master-detail dependency behavior.

---

# 2.5 What is a Master-Detail Relationship?

Master-detail creates a stronger parent-child relationship.

The detail record is strongly dependent on the master.

Important concepts include:

- Ownership behavior
- Sharing implications
- Roll-up summaries
- Cascade behavior
- Relationship dependency

### Hands-on

Only perform this in your lab object if you do not already have a suitable master-detail relationship.

Create:

```text
PD1 Project
   |
   +---- PD1 Project Task
```

Create custom object:

```text
Label:
PD1 Project Task
Plural:
PD1 Project Tasks
```

Then create a **Master-Detail Relationship** from `PD1 Project Task` to `PD1 Project`.

### Verify

Create a Project.

Create several Tasks under it.

Then inspect the relationship and behavior when the parent is removed.

Do this only in your sandbox because relationship changes/deletions can have significant consequences.

### Exam memory

> Master-detail creates a stronger dependency between child and parent than lookup.

---

# 2.6 Lookup vs Master-Detail

| Area | Lookup | Master-Detail |
|---|---|---|
| Dependency | Lower | Stronger |
| Parent controls child ownership | Generally no | Yes |
| Roll-up summary | Not normally directly available | Supported |
| Sharing/ownership behavior | More independent | More dependent |
| Child can exist independently | Often yes | Strong dependency |

Do not memorize only the table. Understand the relationship behavior.

---

# 2.7 What is a Junction Object?

A junction object models a many-to-many relationship.

Example:

```text
Student
   |
   +---------+
             |
        Enrollment
             |
   +---------+
   |
Course
```

One Student can have many Courses.

One Course can have many Students.

### Hands-on design

Create:

```text
PD1 Student
PD1 Course
PD1 Enrollment
```

Use two relationships on Enrollment to connect Student and Course.

For a certification lab, focus on understanding the model rather than spending excessive time on UI configuration.

### Exam memory

> Junction object = commonly used to implement many-to-many relationships.

---

# 2.8 What is a Formula Field?

A formula field calculates a value from other data.

It does not behave like a normal manually entered stored value.

Example:

```text
Amount * 0.18
```

### Hands-on

Create on Account:

```text
Field Label:
PD1 Account Score

Type:
Formula

Return Type:
Number
```

Use a simple formula such as:

```text
IF(
    ISPICKVAL(Customer_Tier__c, "Gold"),
    100,
    50
)
```

### Steps

1. Object Manager → Account.
2. Fields & Relationships.
3. New.
4. Select **Formula**.
5. Label:

```text
PD1 Account Score
```

6. Return Type: **Number**.
7. Enter the formula.
8. Check syntax.
9. Save.

### Verify

Set:

```text
Customer Tier = Gold
```

The formula should evaluate to:

```text
100
```

Change it to Silver.

Observe the calculated result.

### Key concept

The formula result is calculated from current data.

### Exam memory

> Formula fields calculate values from other fields/data.

---

# 2.9 What is a Roll-Up Summary Field?

A roll-up summary calculates information from child records onto a master record in a master-detail relationship.

Common operations:

- COUNT
- SUM
- MIN
- MAX

### Hands-on

If you created:

```text
PD1 Project
   |
   +---- PD1 Project Task
```

create on `PD1 Project`:

```text
Field Label:
Total Tasks

Type:
Roll-Up Summary

Summarized Object:
PD1 Project Task

Roll-Up Type:
COUNT
```

### Verify

Create three Project Task records.

Open the parent Project.

Expected:

```text
Total Tasks = 3
```

Delete one child and verify the value changes.

### Exam memory

> Roll-up summaries summarize child records on a master record.

---

# 2.10 What is an External ID?

An External ID is a field used to identify Salesforce records using an identifier from an external system.

Typical integration example:

```text
ERP Customer ID
       |
       v
Salesforce External ID
```

### Hands-on

On Account:

1. Object Manager → Account.
2. Fields & Relationships.
3. New.
4. Select **Text**.
5. Label:

```text
ERP Customer ID
```

6. Enable **External ID** where available.
7. Save.

### Verify

Populate:

```text
ERP Customer ID = ERP-10001
```

The field can now serve as an external identifier for integration-related operations.

### Exam memory

> External ID = external-system identifier used to identify Salesforce records.

---

# 2.11 Data Modeling Exam Questions

Be able to answer:

- When should lookup be used?
- When should master-detail be used?
- What is a junction object?
- What is a formula field?
- What is a roll-up summary?
- What is an External ID?
- What is the difference between data and metadata?
- What happens when parent/child relationships change?

---

# 3. Salesforce Security

# 3.1 What is Salesforce Security?

Salesforce security controls:

- Who can access the org
- Which objects users can access
- Which fields they can see/edit
- Which records they can access
- Which actions they can perform

Think in layers:

```text
Org
 ↓
Object
 ↓
Field
 ↓
Record
```

---

# 3.2 What is Authentication?

Authentication answers:

> Who are you?

Examples:

- Username/password
- SSO
- MFA
- Identity providers

### Exam memory

> Authentication = identity.

---

# 3.3 What is Authorization?

Authorization answers:

> What are you allowed to do?

Examples:

- Object permissions
- Field permissions
- Record access
- System permissions

### Exam memory

> Authorization = permissions/access.

---

# 3.4 What is a Profile?

A profile defines a baseline set of permissions and settings for users.

Historically profiles have been used for:

- Object permissions
- Field permissions
- System permissions
- Login restrictions
- App/tab visibility

Modern Salesforce security design often uses **permission sets and permission set groups** to provide additional access.

---

# 3.5 What is a Permission Set?

A permission set grants additional permissions to users without changing their profile.

Think:

```text
Profile
   +
Permission Set
   =
User's effective permissions
```

### Hands-on

Create a lab permission set:

```text
Label:
PD1 Project Access
```

Grant appropriate access to `PD1 Project`.

Assign it to your sandbox user if appropriate.

### Steps

1. Setup.
2. Search **Permission Sets**.
3. New.
4. Label:

```text
PD1 Project Access
```

5. Open the permission set.
6. Object Settings.
7. PD1 Project.
8. Grant appropriate Read/Create/Edit access.
9. Save.
10. Manage Assignments.
11. Assign to your test user.

### Verify

Confirm the user can access the object.

### Exam memory

> Permission sets add permissions; they don't replace the user's baseline profile.

---

# 3.6 What is Field-Level Security?

Field-level security controls whether a user can:

- See a field
- Edit a field

### Hands-on

Use:

```text
Customer_Tier__c
```

1. Setup → Object Manager → Account.
2. Fields & Relationships.
3. Open Customer Tier.
4. Review Field-Level Security.

Understand:

```text
Visible
Read-only
Hidden
```

### Exam memory

> FLS controls field visibility and editability.

---

# 3.7 What is Object-Level Security?

Object permissions determine what users can do with an object.

Common permissions:

- Read
- Create
- Edit
- Delete
- View All
- Modify All

### Exam memory

> Object security controls access to object operations.

---

# 3.8 What is Record-Level Security?

Record-level security determines which specific records a user can access.

Major concepts:

- Organization-Wide Defaults
- Role hierarchy
- Sharing rules
- Manual sharing
- Account/territory sharing where applicable
- Apex managed sharing

---

# 3.9 What are Organization-Wide Defaults?

OWD defines the baseline record-level access for an object.

Conceptually:

```text
OWD
 ↓
Baseline record access
 ↓
Additional sharing mechanisms can open access
```

### Hands-on

1. Setup.
2. Search **Sharing Settings**.
3. Open.
4. Find Account.
5. Inspect the Organization-Wide Default.

Do not change production settings.

### Exam memory

> OWD establishes baseline record access.

---

# 3.10 Role Hierarchy

The role hierarchy can open record access upward through the hierarchy depending on object/security configuration.

Do not confuse:

```text
Role
≠
Profile
≠
Permission Set
```

### Exam memory

```text
Profile / Permission Set
→ What can you do?

Role / Sharing
→ Which records can you access?
```

---

# 4. Salesforce Automation

# 4.1 What is Automation?

Automation means Salesforce performs work automatically based on configured conditions/events.

Examples:

- Flow
- Approval Process
- Validation Rule
- Apex Trigger
- Scheduled automation

---

# 4.2 What is a Validation Rule?

A validation rule prevents a record from being saved when its formula evaluates to true.

Example:

> Amount cannot be negative.

Conceptually:

```text
User saves record
       |
       v
Validation formula
       |
   TRUE → Error
   FALSE → Continue
```

### Hands-on

Create on Account:

```text
Label:
PD1 Phone Required for Gold

Formula:
AND(
    ISPICKVAL(Customer_Tier__c, "Gold"),
    ISBLANK(Phone)
)
```

Error message:

```text
Phone is required for Gold accounts.
```

### Steps

1. Setup.
2. Object Manager.
3. Account.
4. Validation Rules.
5. New.
6. Enter the formula.
7. Enter error message.
8. Save.

### Verify

1. Open PD1 Test Account.
2. Set Customer Tier = Gold.
3. Clear Phone.
4. Save.

Expected:

> Record save is blocked.

Then enter a Phone value and save again.

### Exam memory

> Validation rules prevent invalid data from being saved.

---

# 4.3 What is Flow?

Flow is Salesforce's declarative automation framework.

Common Flow types include:

- Screen Flow
- Record-Triggered Flow
- Scheduled-Triggered Flow
- Autolaunched Flow

### Your focus

Since you already use Flow, focus on:

- When each type runs
- Before-save vs after-save
- Variables
- Get/Create/Update/Delete Records
- Decisions
- Loops
- Fault handling
- Invocable/Apex interaction
- Flow vs Apex selection

---

# 4.4 Record-Triggered Flow

A record-triggered Flow starts because a record is created, updated, or deleted according to its configuration.

### Hands-on

Create a simple **before-save record-triggered Flow** on Account.

Requirement:

> When Customer Tier becomes Gold, set a text field to `VIP`.

First create:

```text
Field Label:
PD1 Customer Status

Type:
Text

Length:
50
```

Then:

1. Setup → Flows.
2. New Flow.
3. Select **Record-Triggered Flow**.
4. Object = Account.
5. Trigger = A record is created or updated.
6. Entry condition:

```text
Customer Tier Equals Gold
```

7. Choose **Fast Field Updates** where appropriate.
8. Set:

```text
PD1 Customer Status = VIP
```

9. Save as:

```text
PD1 Account Gold Status Flow
```

10. Activate.

### Verify

Edit PD1 Test Account.

Set:

```text
Customer Tier = Gold
```

Save.

Verify:

```text
PD1 Customer Status = VIP
```

### Exam memory

> Before-save record-triggered Flow is appropriate for updating fields on the triggering record when its behavior fits the requirement.

---

# 4.5 Approval Process

An Approval Process automates approval requests for records.

Typical concepts:

- Entry criteria
- Submission
- Approval steps
- Approvers
- Approval actions
- Final approval actions
- Final rejection actions
- Recall actions

### Important certification fact

**In an approval process, the parts you define are:**

> **Steps, approvers, and actions.**

Correct answer:

> **A — Steps, approvers, and actions**

### Another certification fact

> There is no reason to plan an approval process before creating it.

**False.**

You should plan:

- Who approves
- Approval steps
- Entry criteria
- Approval actions
- Rejection actions
- Recall behavior

---

# 4.6 Approval Process — Detailed Hands-On

Use a sandbox object/record suitable for approval testing.

For a simple lab, use **Opportunity**.

### Requirement

An Opportunity with Amount >= 100,000 should require approval.

### Step 1 — Create a Test Opportunity

1. Open Opportunities.
2. Click New.
3. Set:

```text
Opportunity Name = PD1 Approval Test
Stage = Proposal/Price Quote
Close Date = choose a future date
Amount = 150000
```

4. Save.

### Step 2 — Open Approval Processes

1. Setup.
2. Search **Approval Processes**.
3. Select Opportunity.
4. Create a new approval process using the standard wizard/interface available in your org.

### Step 3 — Define Entry Criteria

Configure the process so records enter when:

```text
Amount >= 100000
```

### Step 4 — Configure Initial Submission Actions

Review available actions.

Understand what happens when the record is submitted.

### Step 5 — Configure Approval Step

Create an approval step.

Identify:

```text
Step
Approver
Criteria
Actions
```

### Step 6 — Configure Final Approval

Configure an action that makes the approved state visible, such as a field update appropriate to your sandbox.

### Step 7 — Configure Final Rejection

Configure a rejection action appropriate to your sandbox.

### Step 8 — Activate

Activate the approval process.

### Step 9 — Submit

Open:

```text
PD1 Approval Test
```

Submit it for approval.

Observe:

- Record status
- Approval request
- Assigned approver
- Available actions
- Record history

### Step 10 — Approve

Approve the request.

Observe the configured final approval behavior.

### Step 11 — Repeat and Reject

Create/prepare another qualifying Opportunity.

Submit it.

Reject it.

Observe the rejection behavior.

### Step 12 — Recall

If your configuration/user permissions support it, submit and recall a request.

Observe the resulting state.

### What you should be able to explain

Without opening Salesforce:

1. What causes the record to enter the process?
2. Who approves it?
3. What is an approval step?
4. What are the actions?
5. What happens on final approval?
6. What happens on rejection?
7. What happens on recall?

---

# 4.7 Flow vs Approval Process

Do not confuse:

```text
Flow
→ General declarative automation

Approval Process
→ Formal approval workflow
```

A requirement involving:

> "A manager must approve this record."

should immediately make you consider Approval Processes or the current Salesforce approval capabilities described by the exam material.

---

# 4.8 Flow vs Apex

Use the exam mindset:

```text
Requirement
     |
     v
Can declarative automation satisfy it appropriately?
     |
   Yes
     |
     v
Flow / configuration
```

If the requirement needs:

- Complex reusable logic
- Logic beyond declarative capabilities
- Certain programmatic integration/processing
- Fine-grained code behavior

consider Apex.

Do not memorize "Apex is always better."

---

# 5. Apex

# 5.1 What is Apex?

Apex is Salesforce's server-side, strongly typed programming language used to implement custom business logic.

It resembles Java/C# in several concepts.

Since you already know Java, this should be familiar.

### Exam focus

Concentrate on:

- Execution context
- Governor limits
- Collections
- SOQL
- DML
- Exceptions
- Triggers
- Classes
- Sharing
- Bulkification
- Testing
- Async Apex

---

# 5.2 Apex Classes

An Apex class encapsulates reusable logic/data.

Example:

```apex
public with sharing class AccountService {
    public static void processAccounts(List<Account> accounts) {
        // business logic
    }
}
```

### Important

Understand:

- Class
- Method
- Parameter
- Return type
- Static
- Instance
- Access modifiers
- `with sharing`
- `without sharing`
- `inherited sharing`

---

# 5.3 Apex Collections

Three critical collections:

```apex
List<T>
Set<T>
Map<K,V>
```

### List

Ordered collection.

```apex
List<Account> accounts;
```

### Set

Unique values.

```apex
Set<Id> accountIds;
```

### Map

Key/value lookup.

```apex
Map<Id, Account> accountsById;
```

### Exam memory

```text
List → ordered collection
Set → unique values
Map → key/value lookup
```

---

# 5.4 Hands-On — Collections

Create an Apex class:

```text
PD1CollectionLab
```

with a method that:

1. Creates a List of Account names.
2. Creates a Set of Account IDs.
3. Creates a Map of Account ID → Account.

Example:

```apex
public class PD1CollectionLab {
    public static void runLab() {
        List<String> names = new List<String>{
            'A', 'B', 'C'
        };

        Set<String> uniqueNames = new Set<String>{
            'A', 'A', 'B'
        };

        Map<String, String> values = new Map<String, String>{
            '1' => 'One',
            '2' => 'Two'
        };
    }
}
```

Execute it in Developer Console or an appropriate Apex execution environment.

Observe that the Set removes duplicate values.

---

# 5.5 What is SOQL?

SOQL stands for **Salesforce Object Query Language**.

It retrieves Salesforce records.

Example:

```apex
List<Account> accounts = [
    SELECT Id, Name, Industry
    FROM Account
    WHERE Industry = 'Technology'
];
```

### Important concepts

Study:

- SELECT
- FROM
- WHERE
- ORDER BY
- LIMIT
- Relationship queries
- Aggregate queries
- GROUP BY
- HAVING
- Bind variables

---

# 5.6 SOQL Hands-On

Run:

```apex
List<Account> accounts = [
    SELECT Id, Name, Industry
    FROM Account
    LIMIT 10
];

System.debug(accounts);
```

Then:

```apex
List<Account> accounts = [
    SELECT Id, Name
    FROM Account
    WHERE Name LIKE 'PD1%'
];
```

Then:

```apex
List<Contact> contacts = [
    SELECT Id, Name, Account.Name
    FROM Contact
    WHERE Account.Name LIKE 'PD1%'
];
```

### What you should understand

The third query is a child-to-parent relationship query.

---

# 5.7 Parent-to-Child SOQL

Example:

```apex
List<Account> accounts = [
    SELECT Id, Name,
        (SELECT Id, Name FROM Contacts)
    FROM Account
    WHERE Name LIKE 'PD1%'
];
```

The nested query retrieves child Contacts.

### Memory

```text
Child → Parent
Contact → Account

Parent → Child
Account → Contacts
```

---

# 5.8 Aggregate SOQL

Example:

```apex
List<AggregateResult> results = [
    SELECT Industry, COUNT(Id)
    FROM Account
    GROUP BY Industry
];
```

Know:

- COUNT
- SUM
- MIN
- MAX
- AVG
- GROUP BY
- HAVING

---

# 5.9 What is SOSL?

SOSL stands for **Salesforce Object Search Language**.

It searches text across multiple objects.

Conceptually:

```text
SOQL
→ Query structured records/object data

SOSL
→ Search text across multiple objects
```

### Hands-on

Run a simple SOSL search for a term you know exists in your sandbox.

Example:

```apex
List<List<SObject>> results = [
    FIND 'PD1'
    IN ALL FIELDS
    RETURNING Account(Id, Name), Contact(Id, Name)
];

System.debug(results);
```

### Exam memory

Know when a requirement is better suited to SOQL vs SOSL.

---

# 5.10 What is DML?

DML means Data Manipulation Language.

Common Apex DML operations:

```apex
insert
update
delete
undelete
upsert
```

Example:

```apex
Account a = new Account(Name = 'PD1 DML Test');
insert a;
```

---

# 5.11 Database Methods

Salesforce also provides Database methods:

```apex
Database.insert()
Database.update()
Database.delete()
Database.upsert()
```

One important distinction is how you handle partial success.

### Hands-on

Create multiple Accounts and use:

```apex
Database.insert(accounts, false);
```

Inspect:

```apex
Database.SaveResult[]
```

Understand that `allOrNone = false` can allow partial success.

---

# 5.12 Governor Limits

Governor limits protect shared Salesforce resources.

Important categories include:

- SOQL queries
- DML statements
- CPU time
- Heap size
- Callouts
- Query rows
- DML rows

Do not memorize old numeric values from random websites.

For exact numbers, use current Salesforce documentation.

### Certification concept

Know **why** limits exist and how your design avoids hitting them.

---

# 5.13 Bulkification

Salesforce triggers can receive multiple records.

Therefore logic must process collections rather than assume one record.

### Bad

```apex
for (Account a : Trigger.new) {
    List<Contact> contacts = [
        SELECT Id
        FROM Contact
        WHERE AccountId = :a.Id
    ];
}
```

This performs SOQL inside a loop.

### Better

```apex
Set<Id> accountIds = new Set<Id>();

for (Account a : Trigger.new) {
    accountIds.add(a.Id);
}

List<Contact> contacts = [
    SELECT Id, AccountId
    FROM Contact
    WHERE AccountId IN :accountIds
];
```

### Exam memory

> Avoid SOQL and DML inside loops.

---

# 5.14 Trigger

A trigger executes Apex automatically in response to record events.

Events include:

- before insert
- after insert
- before update
- after update
- before delete
- after delete
- after undelete

### Context variables

Know:

```text
Trigger.new
Trigger.old
Trigger.newMap
Trigger.oldMap
```

---

# 5.15 Trigger Context Hands-On

Create a trigger on your lab object:

```apex
trigger PD1ProjectTrigger on PD1_Project__c (before insert, before update) {
    for (PD1_Project__c project : Trigger.new) {
        if (String.isBlank(project.Name)) {
            // Name is normally required; this is simply a lab context.
        }
    }
}
```

More useful: create a field:

```text
PD1 Project Code
```

and populate it in a before-trigger from another field.

The important exercise is to inspect which context variables are available and whether the record has been saved yet.

### Exam memory

> Before triggers are useful when modifying the triggering record before save; after triggers are useful when the record has been saved and its final persisted state/Id is available.

---

# 5.16 Exception Handling

Apex supports:

```apex
try
catch
finally
```

Example:

```apex
try {
    insert account;
} catch (DmlException e) {
    System.debug(e.getMessage());
}
```

Know:

- DmlException
- QueryException
- NullPointerException
- Custom exceptions
- Handling vs propagating errors

---

# 5.17 Sharing

Know the difference:

```apex
with sharing
without sharing
inherited sharing
```

At certification level, understand that sharing keywords influence record-level sharing behavior for Apex execution.

Do not confuse this with:

- Object permissions
- Field-level security
- CRUD
- FLS

These are separate security concepts.

---

# 5.18 Asynchronous Apex

Main forms:

- Future
- Queueable
- Batch
- Scheduled Apex

Understand the problem each solves.

### General comparison

```text
Future
→ simple asynchronous work

Queueable
→ asynchronous work with richer job structure/chaining capabilities

Batch
→ large data volumes

Scheduled
→ execute according to a schedule
```

The exact platform capabilities and restrictions should be checked against current Salesforce documentation.

---

# 6. Apex Testing

# 6.1 What is an Apex Test Class?

A test class verifies Apex behavior.

Use:

```apex
@IsTest
```

Tests should verify behavior, not merely execute code.

---

# 6.2 Test.startTest() and Test.stopTest()

They mark the portion of a test where you want to isolate/test execution behavior and are especially important when testing asynchronous work.

Example:

```apex
Test.startTest();

    // code under test

Test.stopTest();
```

### Exam memory

> startTest/stopTest establish a test execution context and are important for testing governor limits and asynchronous execution behavior.

---

# 6.3 Assertions

A test should verify expected outcomes.

Example:

```apex
System.assertEquals(
    'Gold',
    account.Customer_Tier__c
);
```

### Bad test mindset

> Code executed without exception.

### Better test mindset

> The expected business behavior occurred.

---

# 6.4 Hands-On — Test Your Lab Flow/Apex

Create test data.

For example:

```apex
Account a = new Account(
    Name = 'PD1 Test Automation Account'
);

insert a;
```

Then query it and assert expected behavior.

If automation sets a field:

```apex
Account result = [
    SELECT Id, Customer_Tier__c
    FROM Account
    WHERE Id = :a.Id
];

System.assertEquals(
    'Gold',
    result.Customer_Tier__c
);
```

Adjust the test to match the automation you actually built.

---

# 6.5 Test Data

Good tests should create their own test data where appropriate rather than depending on arbitrary existing org records.

Know:

- Test isolation
- `@TestSetup`
- Test data factories
- Positive scenarios
- Negative scenarios
- Bulk scenarios
- Exception scenarios

---

# 7. LWC and User Interface

# 7.1 What is LWC?

Lightning Web Components are Salesforce's modern component-based UI framework based on web standards.

A component commonly contains:

```text
component
│
├── HTML
├── JavaScript
└── XML metadata
```

---

# 7.2 LWC Component Structure

Example:

```text
pd1AccountCard/
├── pd1AccountCard.html
├── pd1AccountCard.js
└── pd1AccountCard.js-meta.xml
```

### HTML

Defines UI.

### JavaScript

Defines behavior/state.

### XML

Defines metadata/configuration and targets.

---

# 7.3 What is @api?

`@api` exposes a public property or method from a component.

Typical parent → child communication:

```text
Parent
  |
  | @api property
  v
Child
```

---

# 7.4 What are Custom Events?

Events commonly allow child → parent communication.

```text
Child
  |
  | dispatchEvent()
  v
Parent
```

### Exam memory

```text
Parent → Child
@api

Child → Parent
Custom event
```

---

# 7.5 LWC Hands-On — Parent/Child Communication

Create two components:

```text
pd1Parent
pd1Child
```

### Child

Expose:

```javascript
@api message = 'Hello from child';
```

Dispatch a custom event:

```javascript
this.dispatchEvent(
    new CustomEvent('pd1change', {
        detail: 'Child clicked'
    })
);
```

### Parent

Handle:

```html
<c-pd1-child
    onpd1change={handleChange}>
</c-pd1-child>
```

In JavaScript:

```javascript
handleChange(event) {
    console.log(event.detail);
}
```

### Verify

Click the child button.

Observe the parent receiving the event.

### Exam memory

> `@api` commonly supports public component API/parent-to-child communication; custom events commonly support child-to-parent communication.

---

# 7.6 Wire Service

The wire service provides reactive access to Salesforce data/services.

You should understand:

- Wired Apex
- Wire adapters
- Reactive parameters
- Data/error patterns
- When imperative Apex is used

---

# 7.7 Imperative Apex vs Wired Apex

Understand the conceptual difference:

```text
@wire
→ reactive data provisioning

Imperative Apex
→ explicitly invoke Apex from JavaScript
```

The correct choice depends on requirements.

---

# 7.8 LWC Hands-On — Apex Integration

Use an existing Apex method if you already have one.

If creating a lab class:

```apex
public with sharing class PD1AccountController {
    @AuraEnabled(cacheable=true)
    public static List<Account> getAccounts() {
        return [
            SELECT Id, Name
            FROM Account
            ORDER BY Name
            LIMIT 10
        ];
    }
}
```

Then create an LWC that calls it using `@wire`.

### Verify

Display Account names.

Then deliberately create an error and observe the error state.

### Exam memory

Know:

- Why `@AuraEnabled` is used.
- Why `cacheable=true` is used for appropriate read-only wired methods.
- How data and errors are handled.
- When imperative Apex is more appropriate.

---

# 7.9 Visualforce

Know the basic role of Visualforce and how it differs from LWC.

At PD1 level, focus on:

- What Visualforce is
- Apex controllers
- Visualforce pages
- When legacy UI technologies may appear in Salesforce environments

Do not spend excessive time rebuilding old Visualforce applications unless it is a weakness.

---

# 8. Debugging

# 8.1 What is a Debug Log?

A debug log records execution information that helps developers understand Salesforce behavior.

It can show:

- Apex execution
- SOQL
- DML
- Flow execution
- Exceptions
- CPU usage
- Method calls
- Variables/log statements

---

# 8.2 Debug Log Hands-On

1. Setup.
2. Search **Debug Logs**.
3. Add yourself as a monitored user if necessary.
4. Perform a simple Salesforce action, such as updating your PD1 Test Account.
5. Open the generated log.

Search for:

```text
SOQL
DML
FLOW
EXCEPTION
USER_DEBUG
```

### Verify

Find where your transaction:

- Started
- Queried data
- Updated data
- Executed automation
- Finished or failed

---

# 8.3 Intentional Debugging Exercise

Create a small Apex method with:

```apex
System.debug('PD1 DEBUG START');
```

Run it.

Find:

```text
PD1 DEBUG START
```

in the log.

Then add another debug line after a query.

Use the log to understand execution order.

---

# 9. Deployment and Metadata

# 9.1 What is Deployment?

Deployment moves configuration/customization from one Salesforce environment to another.

Typical flow:

```text
Development
   ↓
Sandbox
   ↓
Testing
   ↓
Production
```

Depending on the team's process, tools can include:

- Change Sets
- Salesforce CLI
- Metadata API
- Source-driven development tools
- DevOps platforms

---

# 9.2 What is Salesforce DX?

Salesforce DX is a set of tools and practices supporting modern Salesforce development, source control, environments, testing, and deployment.

Understand the concepts rather than memorizing commands.

---

# 9.3 What is Salesforce CLI?

Salesforce CLI provides command-line tooling for Salesforce development and deployment.

Know that it can be used for activities such as:

- Authentication
- Metadata retrieval
- Metadata deployment
- Running tests
- Working with orgs
- Development workflows

### Hands-on

If your development environment permits it, authenticate a sandbox using the current Salesforce CLI instructions.

Do not spend hours setting up CLI if your current work environment already has it configured.

The certification objective is understanding what the tool is for.

---

# 9.4 Change Sets

Change Sets can move supported metadata between related Salesforce environments.

Understand:

- Outbound Change Set
- Inbound Change Set
- Dependencies
- Validation
- Deployment

### Exam memory

> Deployment is not simply copying code; metadata dependencies and testing matter.

---

# 10. Salesforce Execution and Order of Execution

This is an important certification area.

When a record is saved, multiple layers can participate:

```text
Record submission
       ↓
Validation / system processing
       ↓
Before-save automation
       ↓
Before triggers
       ↓
Database save
       ↓
After triggers
       ↓
After-save automation
       ↓
Other automation / commit-related processing
```

The exact order is more detailed and can change as Salesforce introduces features.

For exact current order, use Salesforce's current official Order of Execution documentation.

### Exam approach

When a question says:

> "What happens before/after the record is saved?"

pay attention to:

- Before vs after
- Validation
- Flow
- Trigger
- Workflow/legacy automation
- Roll-ups
- Sharing
- Commit

---

# 11. SOQL, SOSL and DML Comparison

| Technology | Purpose |
|---|---|
| SOQL | Query Salesforce records |
| SOSL | Search text across multiple objects |
| DML | Insert/update/delete/undelete/upsert data |

### Memory

```text
SOQL
→ Get records

SOSL
→ Search

DML
→ Change records
```

---

# 12. Governor Limits and Bulk Design

This topic deserves repeated revision.

## Bad Design

```apex
for (Account account : accounts) {
    update account;
}
```

Potential problem:

> DML inside loop.

## Better

```apex
for (Account account : accounts) {
    // modify
}

update accounts;
```

### Exam questions often test

- SOQL inside loop
- DML inside loop
- Single-record assumptions
- Large data volumes
- Collection use
- Query count
- DML count

### Hands-on

Create 200 test records using Apex.

Then make sure your logic processes them as a collection.

Your objective is not merely to create 200 records.

Your objective is to understand:

> "Would this design work when 200 records enter a trigger?"

---

# 13. Apex Trigger Design

A maintainable design often separates trigger entry from business logic.

Example:

```text
Trigger
   ↓
Handler
   ↓
Service
   ↓
Data / Integration
```

You do not need to memorize one architecture as the only correct approach.

For PD1, understand:

- Why bulkification matters.
- Why reusable logic matters.
- Why trigger logic should not assume one record.
- How recursion/automation interactions can occur.

---

# 14. CRUD, FLS and Sharing

This distinction is highly important.

## CRUD

Object-level operations:

- Create
- Read
- Update
- Delete

## FLS

Field-level:

- Field visible?
- Field editable?

## Sharing

Record-level:

- Which records can the user access?

### Memory

```text
CRUD
→ Object

FLS
→ Field

Sharing
→ Record
```

---

# 15. Record Types

Record Types can control different business processes and UI behavior for users.

They can influence:

- Picklist values
- Page layouts
- Business processes

### Hands-on

Inspect an existing standard object.

1. Setup → Object Manager.
2. Account.
3. Record Types.

If your sandbox has no suitable record type, do not create one just for the sake of it unless you want the practice.

### Exam memory

> Record Types are not simply "different object types." They support different business processes and UI configurations within an object.

---

# 16. Page Layouts

Page layouts control aspects of the record detail/edit UI, including:

- Field placement
- Requiredness in the layout context
- Buttons
- Related lists
- Sections

Do not confuse:

```text
Page Layout
≠
Field-Level Security
≠
Lightning Record Page
```

---

# 17. Lightning Record Pages

Lightning App Builder can configure the Lightning page experience.

It can control:

- Component placement
- Page regions
- Visibility rules
- Record page composition

### Exam distinction

```text
Page Layout
→ fields/buttons/related-list layout behavior

Lightning Record Page
→ Lightning component page composition
```

---

# 18. Formula vs Flow vs Apex

A common certification decision.

### Formula

Use when:

> You need a calculated/displayed value derived from existing data.

### Flow

Use when:

> Declarative automation is required.

### Apex

Use when:

> Programmatic logic is required beyond appropriate declarative capabilities.

### Memory

```text
Calculate
→ Formula

Automate
→ Flow

Complex custom code
→ Apex
```

This is a simplification; always read the exact scenario.

---

# 19. Validation Rule vs Flow vs Apex

### Validation Rule

Use when:

> The record must not be saved if a condition is violated.

### Flow

Use when:

> Salesforce needs to perform automation.

### Apex

Use when:

> Custom programmatic logic is required.

### Exam memory

```text
Block save
→ Validation Rule

Automate
→ Flow

Program complex logic
→ Apex
```

---

# 20. Approval Process vs Flow

### Approval Process

Formal approval workflow.

### Flow

General automation.

If the requirement explicitly says:

> "Record must be approved by manager."

think about approval functionality.

If the requirement says:

> "Automatically update field after condition."

think about Flow.

---

# 21. Testing Strategy

A good PD1 developer should understand:

```text
Positive test
Negative test
Bulk test
Boundary test
Exception test
Security-related test where appropriate
```

### Hands-on

For one automation/class, create test cases:

```text
Case 1: Normal input
Case 2: Invalid input
Case 3: Multiple records
Case 4: Boundary value
Case 5: Expected exception
```

Document expected vs actual results.

---

# 22. Debugging Strategy

When something fails:

```text
Requirement
   ↓
Reproduce
   ↓
Identify transaction
   ↓
Debug log
   ↓
Find first unexpected behavior
   ↓
Check data
   ↓
Check automation order
   ↓
Check permissions
   ↓
Fix
   ↓
Retest
```

This is also useful for scenario questions.

---

# 23. Integration Basics

PD1 includes platform development concepts that can involve integration.

Know:

- REST
- SOAP
- APIs
- Callouts
- Named Credentials at a conceptual level
- Authentication
- External IDs
- Serialization/deserialization
- Asynchronous processing where relevant

---

# 24. Apex Callouts

Understand:

> Apex can make HTTP callouts to external systems subject to Salesforce restrictions and appropriate configuration.

Know the basic concepts:

```text
Salesforce
   |
   | HTTP request
   v
External API
   |
   | response
   v
Salesforce
```

Do not make real external callouts from a random sandbox just for certification practice.

Focus on understanding the platform model.

---

# 25. Named Credentials

Named Credentials provide a centralized way to define endpoint/authentication configuration for integrations.

For certification, understand:

- Why they exist
- Authentication configuration concept
- Endpoint abstraction
- Why hard-coding credentials is inappropriate

---

# 26. Serialization and JSON

Apex commonly works with JSON for integrations.

Know:

```apex
JSON.serialize(object);
JSON.deserialize(jsonString, MyClass.class);
```

### Exam memory

> Serialization = object/data → JSON representation.

> Deserialization = JSON → Apex object/data structure.

---

# 27. Platform Events and Async Concepts

Understand conceptually:

- Synchronous processing
- Asynchronous processing
- Platform Events
- Queueable
- Batch
- Future
- Scheduled Apex

The exam may ask which approach fits a requirement.

Do not simply memorize names.

Ask:

> Why does this requirement need asynchronous/event-driven processing?

---

# 28. Salesforce IDs

Know the existence and distinction of Salesforce record IDs, including the 15-character and 18-character forms.

At a high level:

- 15-character IDs are case-sensitive.
- 18-character IDs are case-insensitive versions commonly used for integrations.

### Hands-on

Open your PD1 Test Account and inspect its record ID.

Understand that the ID uniquely identifies the record.

---

# 29. Custom Metadata vs Custom Settings

Know the conceptual distinction.

## Custom Metadata Types

Configuration metadata that can be packaged/deployed and used by applications.

## Custom Settings

Configuration/data structures with their own access and hierarchy behavior.

For current detailed behavior, use Salesforce's current documentation.

### Exam approach

If the requirement says:

> Configuration should be deployable/packageable as metadata.

consider Custom Metadata.

---

# 30. Custom Labels

Custom Labels store reusable text values that can be referenced from Salesforce functionality and code.

They are useful for:

- Messages
- Labels
- Translatable text
- Avoiding hard-coded user-facing strings

---

# 31. Permission Set Groups

Permission Set Groups bundle multiple permission sets.

Conceptually:

```text
Permission Set A
       +
Permission Set B
       +
Permission Set C
       ↓
Permission Set Group
       ↓
User
```

Use them to simplify permission management.

---

# 32. User Interface Security vs Data Security

A field being hidden from the UI does not automatically mean data access is equivalent everywhere.

Certification questions may distinguish:

- UI visibility
- FLS
- Object permissions
- Record sharing
- Apex execution context

Always identify the security layer being tested.

---

# 33. Important Certification Terminology

Know these words precisely:

| Term | Meaning |
|---|---|
| Org | Salesforce environment |
| Object | Data structure |
| Record | Individual data instance |
| Field | Data attribute |
| Metadata | Configuration/definition |
| SOQL | Query Salesforce data |
| SOSL | Search across objects |
| DML | Modify Salesforce data |
| Flow | Declarative automation |
| Apex | Programmatic server-side logic |
| LWC | Modern Salesforce UI component model |
| Trigger | Automatic Apex execution from record events |
| Profile | Baseline user permissions/settings |
| Permission Set | Additional permissions |
| OWD | Baseline record access |
| Sharing Rule | Opens record access |
| FLS | Field visibility/editability |
| CRUD | Object-level operations |
| Record Type | Business process/UI variation |
| Approval Process | Formal approval workflow |

---

# 34. High-Value Exam Comparisons

## Object vs Record

```text
Object = type
Record = instance
```

## Data vs Metadata

```text
Data = business information
Metadata = configuration/definition
```

## CRUD vs FLS vs Sharing

```text
CRUD = Object
FLS = Field
Sharing = Record
```

## SOQL vs SOSL

```text
SOQL = structured record query
SOSL = text search across objects
```

## Flow vs Apex

```text
Flow = declarative automation
Apex = programmatic logic
```

## Formula vs Flow

```text
Formula = calculate
Flow = automate
```

## Validation Rule vs Flow

```text
Validation = prevent invalid save
Flow = automate work
```

## Lookup vs Master-Detail

```text
Lookup = looser relationship
Master-detail = stronger dependency
```

## Parent → Child vs Child → Parent

```text
Account → Contacts
Contact → Account
```

---

# 35. Approval Process Certification Question Bank

## Question

In an approval process, the parts you define are:

A. Steps, approvers, and actions  
B. Requests, approvers, and approvees  
C. Submission actions and CEO approvals  
D. Submissions, authorizations, and Lightning actions

### Answer

**A — Steps, approvers, and actions**

### Memory

> Approval Process = Steps + Approvers + Actions

---

## Question

True or false: There is no reason to plan an approval process before you start creating it.

### Answer

**False.**

### Why?

An approval process should be planned so you know:

- Who approves
- What steps exist
- What criteria are used
- What happens on submission
- What happens on approval
- What happens on rejection
- What happens on recall

---

# 36. How To Convert Every Trailhead Question Into Knowledge

When you encounter a Trailhead question, do not memorize only:

```text
Answer = A
```

Instead record:

```text
Question
↓
Correct answer
↓
Why
↓
Underlying Salesforce feature
↓
Why other answers are wrong
↓
Hands-on verification
↓
Memory rule
```

This is especially important for questions testing small terminology/details.

---

# 37. Hands-On Lab Rules

For every feature you study:

## Build

Create the feature.

## Modify

Change its configuration.

## Break

Give it an invalid input or intentionally create a conflicting condition where safe.

## Observe

See Salesforce's behavior.

## Explain

Close Salesforce and explain it from memory.

## Convert to MCQ

Ask yourself:

> What could Salesforce ask about this?

---

# 38. Hands-On Lab Naming Convention

To keep your sandbox clean, use a `PD1` prefix.

Examples:

```text
PD1 Test Account

PD1 Project

PD1 Project Task

Customer_Tier__c

PD1 Customer Status

PD1 Account Gold Status Flow

PD1CollectionLab

PD1AccountController

PD1 Approval Test
```

This makes it easy to delete/identify your certification-lab components later.

---

# 39. Certification Knowledge Test

Before considering a topic complete, you should be able to answer:

### Concept

> What is it?

### Purpose

> Why does Salesforce provide it?

### Selection

> When would I choose it?

### Limitation

> When would I NOT choose it?

### Behavior

> What happens when it runs?

### Security

> Which permissions affect it?

### Limits

> What governor/platform limits matter?

### Scenario

> Can I identify it from a requirement?

---

# 40. Your Personal Question Bank Template

Copy this whenever you encounter a new question:

```text
## Question

Topic:

Question:

A.
B.
C.
D.

Correct Answer:

Why:

Why the other answers are wrong:

Underlying Concept:

Exam Trap:

Hands-On Exercise:

Memory Rule:
```

---

# 41. Your Mistake Log Template

```text
## Mistake

Topic:

Question:

My Answer:

Correct Answer:

Why I chose mine:

Why the correct answer is correct:

Concept I was missing:

What I should remember:

Hands-On Verification:

Date:
```

---

# 42. Mock Exam Analysis

Do not only record your score.

For every wrong answer classify the mistake:

```text
K = Knowledge gap
S = Scenario misunderstanding
T = Terminology
L = Limit/platform restriction
R = Read question incorrectly
G = Guess
```

Example:

```text
Question 17
Category = L
Reason = Forgot governor-limit behavior
Action = Revise bulkification
```

After a mock exam, count:

```text
Knowledge:
Scenario:
Terminology:
Limits:
Reading:
Guessing:
```

Your revision should focus on the largest categories.

---

# 43. Final Revision Sheet

Before the exam, your final notes should contain:

```text
Governor limits
Bulkification
SOQL/SOSL/DML
Trigger contexts
Flow behavior
Approval Processes
Security layers
Data modeling
Formula/Roll-up
Testing
Async Apex
LWC communication
Deployment
Debugging
Integration basics
```

Do not attempt to reread the entire Salesforce platform immediately before the exam.

---

# 44. PD1 Readiness Checklist

## Developer Fundamentals

- [ ] Salesforce architecture
- [ ] Org
- [ ] Multitenancy
- [ ] Objects
- [ ] Records
- [ ] Fields
- [ ] Metadata
- [ ] Relationships
- [ ] Lookup
- [ ] Master-detail
- [ ] Junction object
- [ ] Formula fields
- [ ] Roll-up summaries
- [ ] External IDs
- [ ] Security concepts
- [ ] Declarative vs programmatic
- [ ] Current certification-specific platform topics

## Process Automation and Logic

- [ ] Flow
- [ ] Approval Process
- [ ] Validation Rules
- [ ] Apex
- [ ] Collections
- [ ] SOQL
- [ ] SOSL
- [ ] DML
- [ ] Exceptions
- [ ] Governor limits
- [ ] Bulkification
- [ ] Triggers
- [ ] Async Apex
- [ ] Integration basics

## User Interface

- [ ] LWC
- [ ] Component structure
- [ ] `@api`
- [ ] Events
- [ ] Wire
- [ ] Imperative Apex
- [ ] Apex-LWC integration
- [ ] Visualforce basics
- [ ] Page Layouts
- [ ] Lightning Record Pages

## Testing, Debugging and Deployment

- [ ] Test classes
- [ ] Assertions
- [ ] Test data
- [ ] `Test.startTest`
- [ ] `Test.stopTest`
- [ ] Bulk tests
- [ ] Negative tests
- [ ] Debug logs
- [ ] Developer Console
- [ ] Salesforce DX
- [ ] Salesforce CLI
- [ ] Metadata
- [ ] Deployment
- [ ] Change Sets

---

# 45. Final Study Strategy For You

You already have practical Salesforce experience.

Therefore:

```text
Do not:
Read beginner Salesforce material for weeks.

Do:
Use your existing knowledge as the foundation.
```

Your preparation should look like:

```text
Trailhead Topic
      ↓
Read the explanation here
      ↓
Perform the sandbox lab
      ↓
Answer the included questions
      ↓
Do Trailhead scenario/flashcards
      ↓
Record anything you got wrong
      ↓
Move to next topic
```

When you encounter a topic you already know extremely well:

```text
Read
 ↓
Verify
 ↓
Do MCQ
 ↓
Move on
```

When you encounter a topic you have barely used:

```text
Read carefully
 ↓
Perform hands-on
 ↓
Experiment
 ↓
Answer scenarios
 ↓
Add to weak-area list
```

---

# 46. The Most Important Rule

Your goal is not:

> "I completed Trailhead."

Your goal is:

> "I understand the Salesforce behavior being tested by the certification."

And the strongest preparation loop is:

```text
THEORY
  ↓
HANDS-ON
  ↓
OBSERVE
  ↓
EXPLAIN
  ↓
MCQ
  ↓
MISTAKE LOG
  ↓
REVISION
```

That loop should be repeated throughout the entire PD1 syllabus.

---

# 47. What To Do With New Trailhead Questions

Whenever you encounter a question that looks like a small certification fact, capture it.

Example:

```text
In an approval process, the parts you define are:
```

Do not simply store:

```text
A
```

Store:

```text
Approval Process
→ Steps
→ Approvers
→ Actions
```

Then, if useful, verify it through the sandbox.

This turns your Trailmix into a growing **personal PD1 question bank and knowledge base**.

---

# 48. Source-of-Truth Rule

For certification preparation:

1. Use the current Salesforce certification Trailhead material as the primary syllabus.
2. Use this manual as your consolidated explanation and lab guide.
3. Use current Salesforce documentation for exact limits and release-sensitive behavior.
4. Treat random blogs, old PDFs, and old exam dumps as unreliable unless independently verified.

The Salesforce platform changes over time, so exact numbers and feature behavior should always be checked against current official documentation.

---

# 49. Completion Criteria

You are ready to seriously consider the exam when:

- You have completed the official PD1 preparation trail.
- You have performed hands-on verification for unfamiliar topics.
- You can explain the four certification areas.
- You can solve scenario questions without relying on memorization.
- You have reviewed your mistake log.
- You consistently score around 80–85%+ on good-quality practice exams.
- You understand why your answers are correct.
- You can explain the important differences between similar Salesforce features.

Most importantly:

> **You should be able to explain the answer, not just recognize it.**

---

# 50. Final PD1 Mental Model

Think about Salesforce in layers:

```text
                 SALESFORCE
                     |
        +------------+------------+
        |            |            |
       DATA        LOGIC           UI
        |            |            |
    Objects       Flow           LWC
    Fields        Apex           Pages
    Records       Triggers       Components
    Relations     Approval
                  SOQL/SOSL
                  DML
        |
     SECURITY
        |
 CRUD / FLS / Sharing
        |
      PLATFORM
        |
 Multitenancy / Limits
        |
 TESTING / DEBUGGING / DEPLOYMENT
```

When you see a certification scenario, identify **which layer is being tested**.

That will make many seemingly difficult questions much easier.
