# Salesforce Platform Developer I — Complete Study & Sandbox Workbook

> **Purpose:** One module-wise reference for Platform Developer I preparation, with concise rules, exam traps, practical sandbox assignments, and Apex snippets to run. Use this as a working workbook: read a topic, complete the lab, record the result, then revise the trap list.
>
> **Accuracy note:** Snippets and expected outcomes are study guidance, not executed in your org. Salesforce releases and org configuration can change behavior; verify uncertain details in your sandbox and current Salesforce documentation. Never run destructive or high-volume experiments in production.

## How to use this workbook

1. Work in a **Developer Edition org or sandbox**, not production.
2. Create a dedicated test Account/Contact naming prefix such as `PD1-Lab`.
3. Run Apex snippets in **Developer Console → Debug → Open Execute Anonymous Window** or VS Code with Salesforce CLI.
4. For Flow/LWC labs, create a separate component/flow per exercise and activate only when ready.
5. Keep a lab log: **hypothesis → steps → observed result → explanation**.
6. Do not paste all snippets at once; run each independently and check logs.

## Module map

1. Data model, declarative tools, and security
2. Apex fundamentals and object-oriented programming (practical)
3. Apex language reference, triggers, and test framework
4. Trigger lab and bulk processing
5. SOQL, SOSL, DML, and asynchronous Apex
6. Flow, callouts, and integrations
7. LWC, Aura, Visualforce, deployment, and debugging
8. Test strategy and final capstone

## Suggested sandbox data

Use a few test Accounts and Contacts. Keep all lab-created records identifiable by `Name` or `Description` containing `PD1-Lab`. For trigger tests that create records, use bulk lists and avoid loops containing SOQL or DML.

---

# Module 1 — Salesforce data model, declarative tools, and security

## 1. Field types (all of them)

| Type | Stores / size | CAN | CAN'T / gotchas |
|---|---|---|---|
| **Auto Number** | System-generated text sequence, format like `INV-{0000}` | Be unique; be an External ID | **Cannot be edited or written** by users, Apex or API. Gaps appear after deletes/failed saves |
| **Formula** | Calculated at read time. Return types: Checkbox, Currency, Date, Date/Time, Number, Percent, Text, Time | Reference parent fields across relationships; be used in SOQL SELECT/WHERE | **Read-only** (writing it is a compile error). Formula text limit 3,900 chars, compiled 5,000. Cannot reference child records. Cannot be used as a roll-up source in some cases (verify) |
| **Roll-Up Summary** | COUNT, SUM, MIN, MAX of child records | Have filter criteria | **Only on the master side of master-detail** (not lookup). **No AVG**. 25 per object (default). Read-only |
| **Lookup** | Optional pointer to a parent | Be empty; self-reference; filtered with lookup filter; delete options: clear value / don't allow delete | No roll-up summary. Child doesn't inherit sharing/owner. Counts toward 40 relationship fields per object |
| **Master-Detail** | Required parent | Cascade delete; inherit sharing and owner; roll-ups; reparenting if enabled | **Max 2 per object. No self-relationship.** Detail has no Owner field. Parent must exist before conversion from lookup. Cannot be on some standard objects (e.g., Opportunity to Account can't be converted) |
| **External Lookup** | Child (standard/custom/external) → *external* object, matched by external object's External ID | Link Salesforce records to external data | Parent must be an external object |
| **Indirect Lookup** | External (child) → *Salesforce* parent using a unique External ID field | Link external child records to Salesforce | Parent field must be **External ID + Unique** |
| **Hierarchical** | Lookup to the same object | **User object only** | Not available on other objects (custom/other standard objects use a normal self-lookup) |
| **Checkbox** | true/false | Default checked/unchecked | In Apex a checkbox is `true/false`, but an uninitialized **Boolean variable is `null`** |
| **Currency** | Up to 18 digits incl. decimals | Multi-currency adds `CurrencyIsoCode` | Use `Decimal` in Apex, never `Double` |
| **Number** | Up to 18 digits total (before + after decimal) | Be External ID / unique | Decimal places set at field level |
| **Percent** | Number displayed with % | – | Stored as number (e.g., 12.5) |
| **Date / Date/Time / Time** | Date only / date + time (UTC stored) / time only | Date literals in SOQL | Time zone applies to Date/Time display |
| **Email** | Up to 80 chars, validated | Be used for email alerts, External ID | Format-validated |
| **Phone** | Up to 40 chars | Be External ID? (text-like) | No automatic formatting by default |
| **URL** | Up to 255 chars | Clickable link | – |
| **Text** | Up to **255** chars | Be External ID/Unique (case sensitivity option); filter and sort in SOQL | – |
| **Text (Encrypted)** | Up to **175** chars, masked | Mask types | **Cannot be unique, External ID, used in SOQL WHERE/ORDER BY, formulas or filters** |
| **Text Area** | Up to 255 chars, multi-line | Filter in SOQL | No rich formatting |
| **Text Area (Long)** | Up to **131,072** chars (default 32,768) | Many lines | **Cannot be used in SOQL WHERE, ORDER BY, GROUP BY** or reports as filter |
| **Text Area (Rich)** | Up to **131,072** chars incl. HTML; images allowed | Formatting | Same query limits as long text |
| **Picklist** | One value; up to 1,000 values per field (verify) | Restricted option, global value set, dependent picklist, record-type-specific values | Unrestricted picklists accept API values not in the list; **record types hide values, they don't delete them** |
| **Picklist (Multi-Select)** | Semicolon-separated values | Be dependent; query with `INCLUDES` / `EXCLUDES` | **Cannot be a controlling field**; cannot use `=` for one value reliably; not usable in some formulas (use `INCLUDES`) |
| **Geolocation** | Compound latitude/longitude | `DISTANCE()` and `GEOLOCATION()` in SOQL | Compound field; individual parts accessed as `Field__Latitude__s`, `Field__Longitude__s` |
| **Standard Id** | 15-char (case-sensitive) / 18-char (case-insensitive) | Apex treats both as equal | API returns 18; Excel needs 18 |

**Field-level rules:** required at field level = enforced everywhere (UI, API, Apex); required on **page layout** = UI only; **validation rule** = conditional. A field **hidden by layout but visible by FLS** still appears in reports, API, SOQL, and Apex.

## 2. Relationship rules (delta table)

| | Lookup | Master-Detail |
|---|---|---|
| Parent required | no | yes |
| Parent delete | children stay / clear / blocked | children deleted (and restored on undelete) |
| Sharing & owner | independent | inherited, **no Owner field on detail** |
| OWD of child | its own | **Controlled by Parent** |
| Roll-up on parent | no | yes |
| Self-reference | yes | **no** |
| Count | counts in 40 | **max 2** |
| Reparent | always | only if "Allow reparenting" |
| Many-to-many | – | junction object with **two** master-details; either parent delete removes junction rows; **first master created is the primary** (owner/sharing inherited from it) |

## 3. Declarative tools: can / can't

| Tool | CAN | CAN'T | Runs |
|---|---|---|---|
| **Validation rule** | Block save when formula is **true**; reference parent fields; use `$Permission` bypass; fire from UI, API, Apex, Flow, Data Loader | Reference **child** records; call code; run on delete; show messages other than text | On insert/update, **after before triggers**, before save |
| **Formula field** | Read parent fields, text/date math | Be written; cross unlimited relationships (limited spans) | Every read |
| **Record type** | Different picklist values, page layouts (via assignment), business processes (Lead, Opportunity, Case, Solution) | Control **FLS or record access** | Chosen on create; changeable |
| **Page layout** | Field/button/related list arrangement, UI-required | Secure a field, affect API/Apex | UI only |
| **Dynamic Forms** | Field visibility rules per field on record pages | Replace FLS | Lightning record pages |
| **Approval process** | Multi-step approvals, record locking, email alerts, field updates | Run Apex directly (use Apex actions in flow); edit by non-admins while locked | When submitted |
| **Duplicate rules** | Alert/block using matching rules | Run on all objects (supported objects only) | On create/update |

## 4. Security: can / can't

| Item | CAN | CAN'T |
|---|---|---|
| **OWD** | Set baseline: Private, Public Read Only, Public Read/Write, Public Read/Write/Transfer (Leads, Cases), Controlled by Parent, Public Full Access (Campaigns) | Be loosened below by profile/permission sets; it is the **most restrictive** layer |
| **Role hierarchy** | Give managers access to subordinates' records | Apply to custom objects if "Grant Access Using Hierarchies" is off; grant **upward only** |
| **Sharing rule** | Open access (owner-based or criteria-based) to roles, roles + subordinates, public groups | **Restrict** access, or share *to* a lower level than OWD |
| **Manual sharing** | One record to one user/group | Survive **owner change** (it is removed) |
| **Teams (Account/Opportunity/Case)** | Per-person access on a record | Exceed what the team role defines |
| **Implicit sharing** | Owner of a child gets read on parent; parent owner gets access to children | Be turned off |
| **Apex managed sharing** | Insert `__Share` rows with custom sharing reasons; **persist after owner change** | Be created on **detail** objects (controlled by parent) or on Public Read/Write objects (no share table) |
| **Profile** | Exactly **one per user**; login hours, IP ranges, default record type, layout assignment | Be multiple per user |
| **Permission set** | **Add** access; many per user | Remove access (except **muting** inside a permission set *group*) |
| **View All / Modify All** (object) | Override record sharing for one object | Cross to other objects |
| **View All Data / Modify All Data** (system) | Same across **all** objects | – |
| **FLS** | Hide/read-only a field everywhere (UI, API, reports, Apex only if you enforce it) | Be bypassed by page layout settings |

**Apex & enforcement**

| | Record sharing | CRUD/FLS | Throws or strips |
|---|---|---|---|
| `with sharing` | yes | **no** | – |
| `without sharing` | no | no | – |
| `inherited sharing` | caller's mode (**with sharing at entry point**) | no | – |
| `WITH USER_MODE` | yes | yes | **throws** on inaccessible field/object |
| `WITH SECURITY_ENFORCED` | no | yes (SELECT fields) | **throws** |
| `Security.stripInaccessible` | no | yes | **removes** fields silently |
| `Schema…isAccessible()/isUpdateable()` | no | manual check | boolean |

A class that declares its own sharing keyword **uses it regardless of caller**. A class with **no keyword inherits** from the caller (and runs without sharing as an entry point, except `inherited sharing` which defaults to with sharing). Triggers have no sharing keyword; the classes they call apply it.


# Module 2 — Apex fundamentals and object-oriented programming

Use these exercises alongside the Apex language reference in Module 3.

## Lab 2.1 — Execute Anonymous: primitives and collections

Run in Execute Anonymous:

```apex
System.debug('abc' == 'ABC');
System.debug('abc'.equals('ABC'));

List<Integer> numbers = new List<Integer>{1, 2, 2, 3};
Set<Integer> uniqueNumbers = new Set<Integer>(numbers);
Map<String, Integer> scores = new Map<String, Integer>{'Asha' => 10};

System.debug('list=' + numbers.size());       // 4
System.debug('set=' + uniqueNumbers.size()); // 3
System.debug(scores.get('missing'));         // null
System.debug(scores.put('Asha', 12));        // previous value: 10
```

**Assignment:** Change the list to include `null`; test `contains`, `clone`, and map key case sensitivity. Write down which collection best fits each use case: ordered duplicates, uniqueness, or key-to-value lookup.

## Lab 2.2 — Query and update safely

```apex
List<Account> accts = [
    SELECT Id, Name, Industry
    FROM Account
    WHERE Name LIKE 'PD1-Lab%'
    LIMIT 20
];

for (Account a : accts) {
    System.debug(a.Name + ' | ' + a.Industry);
}
```

**Assignment:** Create two test Accounts through the UI, run the query, then update one field using a sparse sObject:

```apex
Account patch = new Account(Id = 'REPLACE_WITH_REAL_ACCOUNT_ID', Rating = 'Hot');
update patch;
```

Replace the placeholder with an actual Account Id. Observe that fields not assigned in `patch` are not overwritten. Do not use a fabricated Id.

## Lab 2.3 — OOP mini-exercise

Create an Apex class `PD1DiscountCalculator` with a public method that accepts a `Decimal` price and returns the price after a 10% discount. Add a second method that accepts a discount percentage. Validate null and negative inputs. Then create a test class with positive, zero, null, and invalid-input cases.

# Module 3 — Apex language, triggers, and testing (reference)

Every "Expected" result is **from my knowledge, not run in an org**. Run each snippet in Execute Anonymous (or as a test) and trust your org if wording differs.

---

## 1. Apex language: what's twisted

### Strings, numbers, defaults
| Rule | Detail |
|---|---|
| `==` on Strings | **Case-insensitive** |
| `.equals()` | **Case-sensitive** |
| Uninitialized variables | `null`, including Boolean and Integer |
| `Decimal` vs `Double` | Decimal is exact (money); Double is floating point |
| Id `==` | 15- and 18-char forms of the same record compare equal |
| String methods | `isBlank` (null/empty/whitespace), `isEmpty` (null or empty only), `substring(start, endExclusive)`, `contains`, `split`, `format` |
| `Integer` range | -2,147,483,648 to 2,147,483,647; overflow wraps |
| Safe navigation `?.` | Returns null instead of throwing |
| `??` | Null coalescing |

```apex
System.debug('abc' == 'ABC');        // expected: true
System.debug('abc'.equals('ABC'));   // expected: false
String s; System.debug(s);           // expected: null
System.debug(String.isBlank('  '));  // expected: true
System.debug(String.isEmpty('  '));  // expected: false
```

### Collections
| | List | Set | Map |
|---|---|---|---|
| Ordered | Yes (index) | No | By key only |
| Duplicates | Yes | No | Keys unique, values may repeat |
| Null | Allowed | Allowed (one) | Allowed key |
| Case | – | **String elements are case-sensitive** | **String keys are case-sensitive** |
| Key method | `add(i,v)` inserts at index; `sort()`; `size()` | `contains`, `addAll` | `get` → **null** if missing; `put` returns previous value; `containsKey`; `keySet()`, `values()` |
| Copy | `clone()` is **shallow**; `deepClone()` copies sObjects | `clone()` | `clone()` shallow |

**References:** `List b = a;` makes **one** list. Changes via `b` show in `a`.

```apex
List<Integer> a = new List<Integer>{1,2,3};
List<Integer> b = a;  b.add(4);
System.debug(a.size());                 // expected: 4
List<Integer> c = a.clone(); c.add(5);
System.debug(a.size());                 // expected: 4 (clone is separate)
try { Integer x = a[10]; } catch (ListException e) { System.debug(e.getTypeName()); }   // expected: System.ListException
Map<String,Integer> m = new Map<String,Integer>{'A'=>1};
System.debug(m.get('a'));               // expected: null (keys are case-sensitive)
System.debug(m.put('A', 2));            // expected: 1 (previous value)
```

### Classes and OOP
| Topic | Can | Can't |
|---|---|---|
| `virtual` class/method | Be extended / overridden (child uses `override`) | Be overridden without `override` |
| `abstract` | Hold method bodies and abstract methods; be extended | Be instantiated |
| Interface | Declare signatures; many per class | Hold method bodies or fields |
| `final` | Fix a variable's value | Be reassigned after assignment |
| `static` | Share across instances **for one transaction** | Persist between transactions |
| Constructors | Overload with different params; chain with `this(...)`/`super(...)` | Return values |
| Inner classes | Be `public`/`private`; hold their own sharing declaration | **Inherit the outer class's sharing** |
| Access | `private` (class), `protected` (class + subclasses), `public` (namespace), `global` (everywhere) | – |
| Properties | `public String x { get; private set; }` = read-only outside | – |

**Static vs instance initialization:** static blocks run once when the class is first used in the transaction; instance init and constructor run per `new`.

### Exceptions
| Item | Rule |
|---|---|
| `try/catch/finally` | `finally` always runs |
| Catch order | Specific first; `Exception` last |
| `LimitException` | **Cannot be caught**; transaction aborts and rolls back |
| `DmlException` | `getDmlMessage(i)`, `getNumDml()` |
| `QueryException` | 0 or >1 rows into a single sObject |
| Custom exception | `public class MyEx extends Exception {}` (name must end with `Exception`) |

```apex
try { throw new MathException(); } catch (MathException e) { System.debug('A'); } finally { System.debug('B'); }
// expected: A then B
```

### Try it: break and fix (language)
| # | Snippet | Change | Expected | Fix |
|---|---|---|---|---|
| 1 | `Account a = [SELECT Name FROM Account LIMIT 1]; System.debug(a.Industry);` | none | `System.SObjectException: SObject row was retrieved via SOQL without querying the requested field` | Add `Industry` to the SELECT |
| 2 | `Account a = [SELECT Id FROM Account WHERE Name='NoSuch'];` | none | `System.QueryException: List has no rows for assignment to SObject` | Assign to a `List<Account>` and check `isEmpty()` |
| 3 | `public virtual class A { public virtual String s(){return 'A';} } public class B extends A { public String s(){return 'B';} }` | none | Compile error: method overrides without `override` | Add `override` |
| 4 | `final Integer x = 1; x = 2;` | none | Compile error: Final variable has already been initialized | Remove `final` |
| 5 | `Account a = new Account(Id = existingId, Rating = 'Hot'); update a;` | add `Name = null` | Only assigned fields change; `Name = null` would clear Name | Set only what you mean to change |
| 6 | `update new List<Account>{ a, a };` (same Id twice) | none | `Duplicate id in list` error | De-duplicate with a Map |

---

## 2. Triggers

### Events and context
| | Before | After |
|---|---|---|
| Modify records in `Trigger.new` | **Yes**, no DML needed | **No**, read-only (`FinalException`) |
| `Id` on insert | null | Available |
| `CreatedDate` etc. on insert | null | Available |
| Formula fields and roll-ups | **Not computed yet** | Computed |
| Best for | Validation, defaulting | Related records, callouts via async, using Id |

| Variable | Insert | Update | Delete | Undelete |
|---|---|---|---|---|
| `new` | ✔ | ✔ | ✘ | ✔ |
| `old` | ✘ | ✔ | ✔ | ✘ |
| `newMap` | after only | ✔ | ✘ | ✔ |
| `oldMap` | ✘ | ✔ | ✔ | ✘ |

Events: `before/after insert`, `before/after update`, `before/after delete`, **`after undelete` only**. Flags: `isInsert, isUpdate, isDelete, isUndelete, isBefore, isAfter, isExecuting, size, operationType`.

| Behavior | Detail |
|---|---|
| Chunk size | Up to **200** records per invocation |
| `addError('m')` on record | Blocks the record (all-or-nothing batch unless partial DML) |
| `addError('m')` on field | Blocks and shows at that field: `rec.Name.addError('m')` |
| `upsert` | Fires insert and/or update triggers |
| `merge` | Delete trigger on losers, update on the winner |
| Multiple triggers on one object | **Order is not guaranteed** (use one trigger + handler) |
| Recursion | Same record update in an after trigger re-fires; stop with a static **Set<Id>** (a Boolean breaks chunking) |
| Callouts | Not directly; use `@future(callout=true)` or Queueable |
| Triggers and sharing | Run in system mode; sharing comes from classes called |
| Recursion depth | Max **16** |

### Try it: trigger lab (Account)
```apex
trigger AccTrg on Account (before insert, after insert) {
    if (Trigger.isBefore) {
        System.debug('newMap null? ' + (Trigger.newMap == null));
        for (Account a : Trigger.new) { a.Description = 'set'; }
    } else {
        System.debug('newMap size ' + Trigger.newMap.size());
    }
}
```
| Change | Expected |
|---|---|
| Insert 450 accounts in a list | Three invocations (200, 200, 50) |
| Add `for (Account a : Trigger.new) { a.Description='x'; }` in the **after** branch | `System.FinalException: Record is read-only` |
| Add `[SELECT Id FROM Contact LIMIT 1]` inside the for loop, insert 150 accounts | `Too many SOQL queries: 101` |
| Add `insert new Account(Name='n');` in the before-insert branch | Recursive trigger depth issue / depth exceeded; guard with a static flag |
| Add `Trigger.old` in a before insert branch | `Trigger.old` is null/empty in insert; avoid reading it |
| Add `rec.addError('stop')` for one record in a list inserted with `insert list` | Whole statement fails; `Database.insert(list,false)` saves others |

---

## 3. Test framework: every rule

### Annotations and methods
| Item | Can | Can't | How often it runs |
|---|---|---|---|
| `@isTest` (class) | Mark a test class (excluded from the org's Apex code size limit) | Be used on a class with test setup + `SeeAllData=true` | – |
| `@isTest` / `testMethod` (method) | `static void`, **no parameters** | Return a value; be called by another test as a test | Each method is its own run |
| **`@testSetup`** | Create shared data **once per class**; `static void`, **no parameters**; only **one per class**; can call helpers | Be used in a class annotated `SeeAllData=true`; pass data to methods by variable (query instead); persist changes made inside test methods | **Runs once** before the first test method; each method then gets a fresh rolled-back copy |
| `Test.startTest()` | Reset governor limits for the code under test; mark start of async capture | Be called more than **once** per method | Once per method |
| `Test.stopTest()` | **Run queued async jobs (future, Queueable, batch, scheduled) to completion** | Be called before `startTest()` | Once per method |
| `@isTest(SeeAllData=true)` | Read existing org data (discouraged) | Be combined with `@testSetup` | Class-level or method-level |
| `System.runAs(user)` | Run as another user (sharing, permissions); be used multiple times; avoid mixed DML | Be used outside tests | Per block |
| `Test.setMock(HttpCalloutMock.class, m)` | Fake HTTP; one active mock | Be skipped when code calls out | Per method |
| `Test.setMock(WebServiceMock.class, m)` | Fake SOAP | – | – |
| `Test.loadData(Obj.sObjectType, 'resource')` | Load records from a CSV static resource | – | – |
| `Test.getStandardPricebookId()` | Access the standard pricebook **without** `SeeAllData` | – | – |
| `Test.setFixedSearchResults(ids)` | Make SOSL return known Ids | – | – |
| `Test.setCreatedDate(id, dt)` | Set a record's CreatedDate | – | – |
| `Test.isRunningTest()` | Branch code in test (use sparingly) | – | – |
| `@TestVisible` | Expose private/protected members to tests | – | – |
| `@isTest(isParallel=true)` | Allow parallel test execution | Be safe with shared data | – |

**Assertions:** `Assert.areEqual(expected, actual, msg)`, `areNotEqual`, `isTrue`, `isFalse`, `isNull`, `isNotNull`, `fail(msg)`, `isInstanceOfType`. `System.assertEquals` still works.

**Coverage rules:** ≥ **75% overall** to deploy to production; **every trigger needs some coverage**; **all tests that run must pass**; test class lines don't count; comments don't count; `System.debug` lines do count.

**Governor limits in tests:** same as production, **fresh for each test method**; async work runs at `stopTest`; a **batch** executes **one chunk only**; scheduled jobs run at `stopTest`.

### @testSetup, exactly
```apex
@isTest
private class SetupTest {
    @testSetup static void setup() { insert new Account(Name = 'Seed'); }

    @isTest static void t1() {
        Account a = [SELECT Id, Name FROM Account WHERE Name = 'Seed'];
        a.Name = 'Changed'; update a;
        System.debug([SELECT COUNT() FROM Account WHERE Name = 'Changed']);   // expected: 1
    }
    @isTest static void t2() {
        System.debug([SELECT COUNT() FROM Account WHERE Name = 'Changed']);   // expected: 0 (rolled back)
        System.debug([SELECT COUNT() FROM Account WHERE Name = 'Seed']);      // expected: 1 (fresh copy)
    }
}
```
| Change | Expected |
|---|---|
| Add a second `@testSetup` method | Save/compile error (only one allowed) |
| Add `(SeeAllData=true)` to the class | Test setup not supported in that class (error) |
| Add a parameter `static void setup(Integer x)` | Compile error: must have no parameters |
| Store the Id in a `static Id` variable inside `setup()` and read it in `t1` | Static value is **not reliably available**; query by a unique field instead |
| Throw an exception in `setup()` | **All** tests in the class fail |

### Break-it snippets: tests
| # | Snippet | Expected error / result | Fix |
|---|---|---|---|
| 1 | Call a method with an HTTP callout in a test, no mock | `System.CalloutException: Methods defined as TestMethod do not support Web service callouts` | `Test.setMock(HttpCalloutMock.class, new Mock())` |
| 2 | `Test.startTest(); ... Test.startTest();` | Exception: startTest may only be called once | Call once |
| 3 | `insert new Account(Name='x'); insert new User(...)` in a test | `MIXED_DML_OPERATION` | Wrap the User insert in `System.runAs(new User(Id=UserInfo.getUserId())) { ... }` |
| 4 | Enqueue a Queueable, then assert its result **before** `Test.stopTest()` | Assertion fails (job hasn't run) | Move the assertion after `stopTest()` |
| 5 | Test inserts 200 records and the trigger has SOQL in a loop | `Too many SOQL queries: 101` | Bulkify the trigger |
| 6 | `Assert.areEqual(1, 2, 'counts differ')` | `System.AssertException: Assertion Failed: counts differ: Expected: 1, Actual: 2` | Fix expected value or the code |
| 7 | Hard-code an Id `'001000000000001'` | Fails in other orgs / `INVALID_CROSS_REFERENCE_KEY` | Create or query the record |
| 8 | SOSL in a test returns 0 rows | `search` returns empty | `Test.setFixedSearchResults(new List<Id>{ acc.Id })` |
| 9 | `System.runAs(u)` in Execute Anonymous | Error: runAs only valid in tests | Move to a test method |
| 10 | Batch test with 500 records and assert all processed | Only the first chunk ran | Keep test data ≤ scope size |

### Good test checklist
1. Positive, negative, **bulk (200)** and different-user tests.
2. Create your own data (`@testSetup` or a factory); no `SeeAllData`, no hard-coded Ids.
3. Mock every callout.
4. Wrap async in `startTest/stopTest`, assert after `stopTest`.
5. Assert real outcomes, not just coverage.

# Module 4 — Trigger lab and bulk record processing

## Lab 3.1 — Before trigger: default a field

Create an Account trigger in Setup → Apex Triggers or VS Code:

```apex
trigger PD1AccountDefaults on Account (before insert) {
    for (Account a : Trigger.new) {
        if (a.Description == null) {
            a.Description = 'PD1-Lab defaulted by before trigger';
        }
    }
}
```

**Try it**
1. Insert an Account without Description and inspect the saved value.
2. Insert an Account with a Description and confirm it is preserved.
3. Explain why no extra `update` DML is required in a before trigger.

## Lab 3.2 — Bulk-safe validation

Create a before-insert/before-update trigger that blocks Accounts whose Name starts with `PD1-BLOCK`. Use `addError` on the record. Test one record and a list of 200 records; then compare `insert records` with `Database.insert(records, false)`.

## Lab 3.3 — Bulkification challenge

Write a trigger handler that, after Contacts are inserted, collects their AccountIds into a Set, queries the related Accounts **once**, and writes a debug line for each Account. Do not put SOQL or DML inside a loop.

**Assignment:** Submit 200 Contacts in one operation. Confirm the trigger works for all records and remains within governor limits. Add a static `Set<Id>` guard only if your design actually needs recursion protection; avoid a blanket static Boolean that skips later trigger chunks.

# Module 5 — SOQL, SOSL, DML, and asynchronous Apex (reference)

All "Expected" outcomes are **from my knowledge, not run in an org**. Error text may differ slightly; verify marked items against the docs.

---

## 1. SOQL: can and can't

| Topic | Can | Can't |
|---|---|---|
| Fields | List fields; `FIELDS(STANDARD)`, `FIELDS(ALL)` (needs `LIMIT 200` or less, verify) | `SELECT *` |
| Relationships | Child-to-parent up to **5** levels (`Contact.Account.Owner.Name`); parent-to-child with a **subquery** (plural child name) | Filter the parent by a child in `WHERE` without a semi-join |
| Standard vs custom names | Standard parent `Account`; standard children `Contacts`; custom `Parent__r` and `Child__r` | Use `__c` after a relationship |
| Semi-join / anti-join | `WHERE Id IN (SELECT AccountId FROM Opportunity ...)`, `NOT IN (...)` | Use the subquery field list for more than one field |
| Aggregates | `COUNT()`, `COUNT(f)`, `COUNT_DISTINCT`, `SUM`, `AVG`, `MIN`, `MAX`, `GROUP BY`, `HAVING`, `GROUP BY ROLLUP/CUBE` | Mix non-aggregated fields without `GROUP BY`; use `WHERE` on aggregates (use `HAVING`) |
| Result of aggregate | `List<AggregateResult>`; aliases or `expr0`, `expr1` | Return `List<Account>` |
| `COUNT()` | Returns an **Integer** in `[SELECT COUNT() ...]` | Be mixed with other fields |
| Sorting | `ORDER BY f ASC/DESC NULLS FIRST/LAST` | Be combined with `FOR UPDATE` (verify) |
| Paging | `LIMIT`, `OFFSET` (max **2,000**) | Offset beyond 2,000 |
| Filters | `=, !=, <, >, LIKE ('%'), IN, NOT IN, INCLUDES, EXCLUDES`, date literals (`TODAY`, `LAST_N_DAYS:7`, `NEXT_QUARTER`) | Use `()` on literals; filter on **Long Text**, **Rich Text** or **Encrypted** fields |
| Deleted rows | `ALL ROWS` with `IsDeleted = true` | Return Recycle Bin rows without `ALL ROWS` |
| Locking | `FOR UPDATE` | Lock without a transaction concern |
| Security | `WITH USER_MODE`, `WITH SECURITY_ENFORCED`, `USING SCOPE` | Enforce FLS with `with sharing` |
| Variables | Bind with `:var` (variable, expression, list/set for `IN`) | Use `{!var}` or `@var` |
| Dynamic | `Database.query(str)`; use binds or `String.escapeSingleQuotes()` | Be safe with raw concatenation |
| Counting | Each query counts toward **100/200** queries; rows toward **50,000** | Escape limits via a SOQL for loop (rows still count) |

**Empty results:** a `List` assignment returns an empty list; a **single sObject** assignment throws `QueryException` when 0 or more than 1 rows come back.

**SOQL for loop:** `for (Account a : [SELECT ...])` fetches in chunks of 200 and saves **heap**. It still counts as rows and as one query.

### Try it
| # | Snippet | Expected | Fix |
|---|---|---|---|
| 1 | `SELECT Id FROM Account WHERE Description = 'x'` | `field 'Description' can not be filtered in a query call` (Description is a long text) | Filter on another field |
| 2 | `SELECT Name, COUNT(Id) FROM Account` | Error: aggregate and non-aggregate fields need GROUP BY | Add `GROUP BY Name` |
| 3 | `SELECT Industry, COUNT(Id) FROM Account GROUP BY Industry WHERE COUNT(Id) > 1` | Syntax error (WHERE before GROUP BY, and aggregates not allowed there) | Use `GROUP BY ... HAVING COUNT(Id) > 1` |
| 4 | `SELECT Id FROM Account ORDER BY Name FOR UPDATE` | Error: ORDER BY not allowed with FOR UPDATE (verify) | Remove ORDER BY |
| 5 | `Integer c = [SELECT COUNT() FROM Account];` | Works, returns an Integer | – |
| 6 | `SELECT Name, Contacts.LastName FROM Account` | Error: invalid relationship usage | `(SELECT LastName FROM Contacts)` |
| 7 | `for (Account a : [SELECT Id FROM Account]) {}` with 60,000 accounts | `Too many query rows: 50001` | Use Batch Apex |
| 8 | Query with `'...' + input + '...'` and input = `x' OR Name != '` | Returns far more than intended (injection) | Use `:input` binds |

---

## 2. SOSL

| Topic | Rule |
|---|---|
| Syntax | `FIND 'term*' IN ALL FIELDS RETURNING Account(Name), Contact(LastName)` |
| Search groups | `ALL FIELDS`, `NAME FIELDS`, `EMAIL FIELDS`, `PHONE FIELDS`, `SIDEBAR FIELDS` |
| Wildcards | `*` (many) and `?` (one) |
| Result | **`List<List<SObject>>`**, order follows `RETURNING` |
| Limits | **20** SOSL queries per transaction; **2,000** rows per query |
| Tests | Return **empty** unless `Test.setFixedSearchResults()` |
| Use when | Searching text across **many objects** |

---

## 3. DML

| Operation | Notes |
|---|---|
| `insert` | Id populated after success |
| `update` | Only assigned fields change; all-or-nothing |
| `upsert` | Match on **Id** or an **External ID** (`upsert list Obj.Ext__c;`) |
| `delete` | Cascades to master-detail children; goes to Recycle Bin (15 days) |
| `undelete` | Restores from Recycle Bin |
| `merge` | **Leads, Contacts, Accounts**; up to **3** records at a time |
| `Database.insert(list, false)` | **Partial success**; returns `List<Database.SaveResult>` (`isSuccess()`, `getErrors()`) |
| `Database.DMLOptions` | `allowFieldTruncation`, `assignmentRuleHeader`, etc. |
| Limits | **150** statements, **10,000** rows per transaction |

| Gotcha | Detail |
|---|---|
| Duplicate Ids in a list | `update` fails ("Duplicate id in list") |
| Mixed DML | Setup objects (User, Group, PermissionSet...) with non-setup objects in one transaction → `MIXED_DML_OPERATION`; split with `@future`/Queueable (in tests use `System.runAs`) |
| Savepoints | `Database.setSavepoint()` and `Database.rollback(sp)`; Ids of rolled back records stay assigned in memory |
| Row locks | `UNABLE_TO_LOCK_ROW` from concurrent updates |
| Field truncation | Long strings cause `STRING_TOO_LONG` unless `allowFieldTruncation` |
| Mixed object lists | A list of mixed sObject types is processed in chunks; max **10 chunks** (verify) |
| Order | `Database.update(list,false)` results align to the input order |

### Try it
```apex
List<Account> l = new List<Account>{ new Account(Name='Good'), new Account() };
```
| Change | Expected |
|---|---|
| `insert l;` | `DmlException: REQUIRED_FIELD_MISSING`; **neither** saved |
| `Database.insert(l, false);` | First saved, second fails; inspect `SaveResult` |
| `insert l;` wrapped in `try/catch` | Same failure, caught; nothing saved |
| `update new List<Account>{a,a};` | `Duplicate id in list` |
| `insert acct; insert new Group(Name='g');` in one transaction | `MIXED_DML_OPERATION` |
| Insert 151 records one by one in a loop | `Too many DML statements: 151` (**uncatchable**) |

---

## 4. Async Apex: exact comparison

| | `@future` | Queueable | Batch | Schedulable |
|---|---|---|---|---|
| Declaration | `@future static void m(...)` | `implements Queueable` | `implements Database.Batchable<SObject>` | `implements Schedulable` |
| Parameters | **Primitives or collections of primitives only** | Any (sObjects, custom types) via members | Via constructor | Via constructor |
| Return | `void` | Job Id from `System.enqueueJob` | Job Id from `Database.executeBatch` | Job Id from `System.schedule` |
| Chaining | **No** | **Yes** (enqueue from `execute`) | Start another from `finish` | – |
| Callouts | `@future(callout=true)` | Add `Database.AllowsCallouts` | Add `Database.AllowsCallouts` | Not directly; start async |
| Per-transaction caps | **50** future calls | **50** enqueued sync; **1** from async | **5** active, **100** in flex queue | **100** scheduled jobs |
| From another async | **Not** from future or batch | From Queueable/future/batch (only **1**) | `executeBatch` from `finish` | – |
| Volume | Small | Medium | **Millions** (QueryLocator up to 50M rows) | Starts other jobs |
| Cleanup | – | **Finalizer** (`System.attachFinalizer`) | `finish()` | – |
| Order guaranteed | No | No (chain order yes) | Chunks not guaranteed in parallel | – |

**Batch details:** `start` returns `QueryLocator` (SOQL) or `Iterable`; `execute(bc, scope)` per chunk (default **200**, max **2,000**); `finish(bc)` once after all chunks. **Limits reset per execute.** Instance variables **reset between chunks unless `Database.Stateful`**. Job status **Holding** = waiting in the flex queue.

**Schedulable cron:** `seconds minutes hours day-of-month month day-of-week [year]`. One of day-of-month/day-of-week must be `?`. `0 0 8 * * ?` = daily 8 AM. `0 0 6 ? * MON-FRI` = weekdays 6 AM. `0 0 * * * ?` = hourly.

### Try it
```apex
public class LabFuture { @future public static void run(Set<Id> ids) {} }
public class LabBatch implements Database.Batchable<SObject> {      // no Stateful
    public Integer total = 0;
    public Database.QueryLocator start(Database.BatchableContext bc){ return Database.getQueryLocator('SELECT Id FROM Account'); }
    public void execute(Database.BatchableContext bc, List<Account> s){ total += s.size(); }
    public void finish(Database.BatchableContext bc){ System.debug('total=' + total); }
}
```
| Change | Expected |
|---|---|
| Change `run(Set<Id>)` to `run(List<Account>)` | Compile error: unsupported parameter type |
| Remove `static` | Compile error: future methods must be static |
| Call `LabFuture.run(...)` inside `LabBatch.execute` | Runtime error: future method can't be called from batch |
| Run the batch as written | `finish` prints `total=0` (state reset per chunk) |
| Add `, Database.Stateful` | `finish` prints the real total |
| Call `System.enqueueJob` twice in `execute` | Too many queueable jobs (limit 1 in async) |
| `System.schedule('x','0 0 8 * * *', new Sched())` | Invalid cron (day-of-week or day-of-month must be `?`) |
| Loop `LabFuture.run(...)` 60 times | `Too many future calls: 51` |
| `Database.executeBatch(new LabBatch(), 5000)` | Scope greater than 2,000 is invalid / adjusted (verify) |

---

## 5. Flow

| Type | Can | Can't |
|---|---|---|
| **Record-triggered, before-save** | Update fields on the **triggering record** with no DML; fastest | Create/delete records, send email, call actions, update other records |
| **Record-triggered, after-save** | Create/update related records, emails, actions, Apex, subflows | Be as fast; update the triggering record without an extra DML (it does an update) |
| **Screen flow** | Collect input from users | Run unattended |
| **Scheduled-triggered** | Run on a schedule for matching records | React instantly to changes |
| **Platform-event-triggered** | React to events | Use the record context of a DML |
| **Autolaunched** | Be called from Apex, other flows, buttons | Show screens |

| Rule | Detail |
|---|---|
| Entry conditions | "Only when a record is **updated to meet the condition requirements**" prevents re-runs |
| Prior values | `{!$Record__Prior.Field}` |
| Faults | **Fault connector** per element |
| Bulk | Flows are bulkified; avoid DML/Get Records **inside loops**; update a **collection** after the loop |
| Limits | Share Apex governor limits; about **2,000** executed elements per interview (verify) |
| Apex from Flow | `@InvocableMethod`: **static**, **one per class**, one `List` parameter; wrap inputs in a class with `@InvocableVariable` |
| Apex starts Flow | `new Flow.Interview.My_Flow(inputs).start();` |
| Order | Before-save flows run **before** before triggers; after-save flows run **after** after triggers and workflow |

---

## 6. Callouts and integration

| Topic | Rule |
|---|---|
| Classes | `Http`, `HttpRequest`, `HttpResponse` |
| Request | `setEndpoint`, `setMethod`, `setHeader`, `setBody`, `setTimeout(ms)` (default 10 s, max **120 s**) |
| Auth/endpoint | **Named Credential** (preferred) or **Remote Site Setting** |
| Limits | **100** callouts; cumulative callout time **120 s**; request/response up to **6 MB** sync / **12 MB** async |
| **DML before callout** | **`You have uncommitted work pending`** error; callout first, or go async |
| From a trigger | Not synchronously; use `@future(callout=true)` or Queueable with `Database.AllowsCallouts` |
| Testing | `Test.setMock(HttpCalloutMock.class, ...)`; `StaticResourceCalloutMock`; `WebServiceMock` for SOAP |
| JSON | `JSON.serialize`, `JSON.deserialize(str, Type)`, `JSON.deserializeUntyped` |
| Status codes | 200 OK, 201 Created, 301/302 redirect, 400 bad request, 401 unauthorized, 404 not found, 500 server error |
| SOAP | Generate Apex from a WSDL (WSDL2Apex) |
| Apex REST | `@RestResource(urlMapping='/Account/*')`, `@HttpGet`, `@HttpPost`, `@HttpPut`, `@HttpPatch`, `@HttpDelete`; **one method per verb per class** |
| Platform events | API name ends `__e`; publish with `EventBus.publish()`; subscribers: Apex trigger (`after insert`), Flow, LWC; **not queryable** with SOQL |

### Try it
```apex
public class LabCallout {
    public static String zen() {
        HttpRequest r = new HttpRequest();
        r.setEndpoint('https://api.github.com/zen'); r.setMethod('GET'); r.setHeader('User-Agent','lab');
        return new Http().send(r).getBody();
    }
}
```
| Change | Expected |
|---|---|
| Run `LabCallout.zen()` with no Remote Site Setting | `System.CalloutException: Unauthorized endpoint, please check Setup->Security->Remote site settings` |
| Add the Remote Site Setting | Works |
| `insert new Account(Name='x'); LabCallout.zen();` | `CalloutException: You have uncommitted work pending...` |
| Call it in a test without a mock | `Methods defined as TestMethod do not support Web service callouts` |
| Call it in a trigger directly | Callout not allowed from the trigger context (error) |
| `r.setTimeout(150000)` | Exceeds the allowed maximum (error or capped at 120 s) |
| Make 101 callouts in a loop | `Too many callouts: 101` |

---

## 7. Recall (Part 3)
- `Database.insert(list,false)` partial; `insert list` all-or-nothing; duplicates in list fail.
- `@future`: static, void, primitives only, no chain, not from future/batch, max 50.
- Queueable: any types, chain, job Id, Finalizer. Batch: Stateful, scope 200/2,000, 5 active.
- DML before callout = error. Callouts need Remote Site or Named Credential.
- Long text can't be filtered. `HAVING` for aggregates. Anti-join: `NOT IN (SELECT ...)`.
- SOQL for loop saves heap, not rows.

# Module 6 — Flow, callouts, and integrations

## Lab 5.1 — Record-triggered Flow

**Business requirement:** When an Account is created or updated with `Type = Prospect` and `NumberOfEmployees > 500`, set a custom checkbox `Needs_Review__c` to true.

1. Create the checkbox field on Account.
2. Setup → Flows → New Flow → **Record-Triggered Flow**.
3. Object: Account; trigger on create or update; entry conditions as above.
4. Choose **Fast Field Updates / before-save** because only the triggering record is changed.
5. Add an Assignment setting `$Record.Needs_Review__c = True`.
6. Save, debug, activate, and test with matching and non-matching Accounts.

**Follow-up assignment:** Change the requirement so a related Task is created. Decide why this needs an after-save flow and use Create Records. Add a fault path where appropriate.

## Lab 5.2 — Scheduled path

Create a Date field `Subscription_Expiry__c` on a suitable test object. Build a record-triggered Flow with a scheduled path one month before expiry (where supported by the selected date/time field and flow configuration). Test dates carefully; consider null expiry dates and records updated after scheduling.

## Lab 5.3 — Approval process

Create an Account approval process for prospect Accounts with more than 500 employees. Configure:
- Entry criteria
- Initial submission action (for example, set a review status)
- Approver assignment
- Final approval action
- Final rejection action
- Record locking and email alert if available

Submit a test record and verify each outcome. Note that the exact wizard labels may vary; use the standard Setup approval-process configuration when the jump-start wizard does not offer the desired object.

## Lab 5.4 — Mock a callout (advanced)

Create an Apex service that sends an HTTP GET request to a configurable endpoint through a Named Credential. Add an `HttpCalloutMock` test so the test does not call the real endpoint. Keep secrets in a Named Credential, never in source code.

**Assignment:** Explain why synchronous callouts directly from a trigger are not allowed, and compare Queueable Apex implementing `Database.AllowsCallouts` with `@future(callout=true)`.

# Module 7 — LWC, Aura, Visualforce, deployment, and debugging

All "Expected" outcomes are **from my knowledge, not run in an org**. LWC and Visualforce errors vary by release; check the browser console and the Developer Console.

---

## 1. Lightning Web Components

### Decorators and reactivity
| Item | Can | Can't |
|---|---|---|
| `@api` property | Be set by the parent; be public; use a **setter** to react to each change | Be mutated by the child that receives it (values from the parent are **read-only**; clone first) |
| `@api` method | Be called by the parent via `this.template.querySelector('c-child').method()` | Be called from an unrelated component |
| `@track` | Observe changes **inside** objects/arrays | Be needed for primitives (fields are already reactive) |
| `@wire` | Run automatically; reactive when parameters use `$prop`; return `{ data, error }` | Run DML; run when a **reactive parameter is `undefined`** (it waits) |

### Template rules
| Rule | Detail |
|---|---|
| Expressions | **None**: use a property or **getter** (`lwc:if={isWon}`) |
| Loops | `for:each={items} for:item="item"` and a unique **`key`** on the first element inside |
| Conditionals | `lwc:if`, `lwc:elseif`, `lwc:else` (older: `if:true`/`if:false`) |
| One root | Exactly one `<template>` root |
| Attribute names | camelCase property `recordId` is set as **`record-id`** |
| Data attributes | `data-id="x"` is read as `event.currentTarget.dataset.id` |
| DOM queries | `this.template.querySelector(...)` (shadow DOM); `this.refs.name` with `lwc:ref="name"` |
| Slots | `<slot>` default; named slots `<slot name="header">` and `slot="header"` in the parent |

### Lifecycle order
`constructor` → `connectedCallback` → `render` → `renderedCallback`. For parent and child:
1. Parent constructor → parent `connectedCallback`
2. Child constructor → child `connectedCallback` → child `renderedCallback`
3. Parent `renderedCallback`

`disconnectedCallback` runs on removal (**clean up timers and listeners**). `errorCallback` catches errors from **child** components. In `connectedCallback` the template isn't rendered, so `this.template.querySelector(...)` returns null.

### Apex from LWC
| | `@wire` | Imperative |
|---|---|---|
| Needs | `@AuraEnabled(cacheable=true)`, `static` | `@AuraEnabled`, `static` (cacheable optional) |
| DML | **No** | **Yes** |
| Trigger | Automatic, reactive | Your call; returns a **promise** |
| Refresh | `refreshApex(this.wiredResult)` | Call again |
| Data | Immutable; clone before sorting/pushing | – |

- Show a friendly error with `throw new AuraHandledException('msg')`.
- Methods may accept primitives, sObjects, collections and Apex classes with `@AuraEnabled` fields.

### Lightning Data Service (no Apex)
| Component / adapter | Use |
|---|---|
| `lightning-record-form` | Quick view/edit/readonly (`mode`) with a default layout |
| `lightning-record-view-form` + `lightning-output-field` | Read-only custom layout |
| `lightning-record-edit-form` + `lightning-input-field` | Custom edit layout; `onsubmit` + `event.preventDefault()` + `submit(fields)` to change fields |
| `getRecord`, `getFieldValue` | Reactive record data |
| `createRecord`, `updateRecord`, `deleteRecord` | Imperative DML via LDS |
| `getObjectInfo`, `getPicklistValues` | Labels and picklists (record-type aware) |
| `lightning-file-upload` (with `record-id`) | Attach files without Apex |

LDS enforces **FLS and sharing** and shares a cache across components, so an `updateRecord` in one component updates another that shows the same record.

### Events
| Direction | How |
|---|---|
| Parent → child | `@api` property or method |
| Child → parent | `this.dispatchEvent(new CustomEvent('selectitem', { detail }))`; parent: `onselectitem={handler}` (lowercase name, no spaces) |
| Event options | `bubbles` (default false), `composed` (default false) |
| Unrelated components | **Lightning Message Service** (also works with Aura and Visualforce): subscribe in `connectedCallback`, **unsubscribe in `disconnectedCallback`** |

| `bubbles` | `composed` | Reaches |
|---|---|---|
| false | false | Only the direct parent's handler on the element |
| true | false | Ancestors within the **same shadow tree** |
| true | true | **Across shadow boundaries** to any ancestor (use sparingly; relaying is cleaner) |

### Meta XML and targets
`isExposed` **true** plus `<targets>`: `lightning__RecordPage`, `lightning__AppPage`, `lightning__HomePage`, `lightning__Tab`, **`lightning__RecordAction`** (quick action), **`lightning__FlowScreen`**, `lightning__UtilityBar`, **`lightningCommunity__Page`** (Experience). `<targetConfig>` holds properties and `<objects>` to limit a record page to certain objects. Record pages inject `@api recordId` and `@api objectApiName`. Flow outputs use **`FlowAttributeChangeEvent`**.

### Other
| Topic | Rule |
|---|---|
| Imports | Apex `@salesforce/apex/Cls.method`; schema `@salesforce/schema/Account.Name`; label `@salesforce/label/c.My_Label`; static resource `@salesforce/resourceUrl/name`; user `@salesforce/user/Id` |
| Toast | `ShowToastEvent` from `lightning/platformShowToastEvent` |
| Navigation | `NavigationMixin` (`Navigate`, `GenerateUrl`) |
| Security | Lightning Web Security (Locker is the older model) |
| Jest | Unit tests; mock Apex with `jest.mock('@salesforce/apex/...', ..., { virtual: true })`; `await Promise.resolve()` to wait for rerender |
| Nesting | LWC can sit **inside** Aura; Aura can't sit inside LWC |
| Shared code | A JS-only module imported as `c/utils` |

### Try it: LWC (change one line, observe)
| # | Change | Expected |
|---|---|---|
| 1 | Remove `cacheable=true` from the Apex method used by `@wire` | Wire error: method must be cacheable |
| 2 | Add `this.items.push('x')` to a child with `@api items` | Error: cannot add property; object not extensible (read-only) |
| 3 | Remove `key={acc.Id}` in a `for:each` | Template compile error: iterated element needs a key |
| 4 | Write `lwc:if={stage === 'Won'}` | Template compile error: expressions not allowed |
| 5 | Dispatch `new CustomEvent('selectItem')` and use `onselectitem` | Handler never fires (name must match lowercase) |
| 6 | Use `onselectitem` on a grandchild's event without `bubbles`/`composed` | Grandparent doesn't receive it |
| 7 | Query `this.template.querySelector('input')` in `connectedCallback` | `null` (not rendered yet) |
| 8 | Pass `undefined` to a `@wire` reactive parameter | Wire doesn't fire until it has a value |
| 9 | Set `isExposed` to false | Component doesn't appear in App Builder |
| 10 | Add `@api recordId` on a component placed on a record page | `recordId` is filled in automatically |

---

## 2. Aura

| Topic | Rule |
|---|---|
| Binding | `{!v.attr}` = **bound** (updates flow both ways); `{#v.attr}` = **unbound** (one-time) |
| Calling Apex | `var a = component.get("c.method"); a.setCallback(this, function(r){ r.getState(); ... }); $A.enqueueAction(a);` Action states: `SUCCESS`, `ERROR`, `INCOMPLETE` |
| Events | **Component event**: handled by the parent hierarchy. **Application event**: handled by **any** component |
| `aura:method` | Exposes a method callable by a parent |
| `aura:handler name="init"` | Runs code on load |
| LDS | `force:recordData` |
| Interfaces | `flexipage:availableForAllPageTypes`, `force:hasRecordId`, `force:lightningQuickAction`, `lightning:isUrlAddressable` |
| Interop | Aura can contain LWC and listen to its `CustomEvent`s |
| Files | Controller (handles events), Helper (shared logic), Renderer, Style |

---

## 3. Visualforce

### Controllers
| Type | Declared as | Extension constructor takes |
|---|---|---|
| Standard (single record) | `standardController="Account"` | `ApexPages.StandardController` |
| Standard **list** | add `recordSetVar="accts"` | **`ApexPages.StandardSetController`** |
| Custom | `controller="MyClass"` | – |
| Extension | `extensions="MyExt"` | As above |

| Rule | Detail |
|---|---|
| Getters | `{!acc}` calls `getAcc()` |
| Fields loaded | Standard controllers load **only fields referenced on the page**; add others with `addFields()` |
| `apex:form` | Required for `commandButton`/`commandLink`/inputs |
| `rerender` | Needs a target that **exists in the DOM** (`rendered="false"` components don't); rerender a parent container |
| `action` on `apex:page` | Runs on load; may return a `PageReference` to redirect |
| Return `null` | Stay on the page and keep view state |
| `PageReference` + `setRedirect(true)` | New request, **view state lost** |
| View state | Limit **170 KB**; use `transient` for variables that aren't needed after the request |
| `readOnly="true"` | Raises the SOQL row limit for report-style pages |
| JavaScript | `apex:actionFunction`, `@RemoteAction` static methods |
| Messages | `ApexPages.addMessage(new ApexPages.Message(ApexPages.Severity.ERROR,'msg'))` and `<apex:pageMessages/>` |
| URL params | `ApexPages.currentPage().getParameters().get('id')` |
| Merge fields | `{!$User.Id}`, `{!$CurrentPage.parameters.id}` |
| Components | `apex:pageBlockTable` (styled), `apex:dataTable`, `apex:repeat` (custom markup), `apex:inputField` (adapts to field type), `apex:outputField` |
| Embed Lightning | `apex:includeLightning` |

### Try it: Visualforce
| # | Change | Expected |
|---|---|---|
| 1 | Remove `<apex:form>` around a `commandButton` | Error: commandButton must be inside a form |
| 2 | Extension for a list controller uses `ApexPages.StandardController` in its constructor | Page error: no matching constructor; use `StandardSetController` |
| 3 | `rerender="panel"` where `panel` has `rendered="false"` | Nothing updates; wrap it in an always-rendered container |
| 4 | Store a 3,000-element list in a non-transient field | View state grows (check the View State tab); make it `transient` |
| 5 | Use `{!acc.Name}` without `getAcc()` | Error: unknown property |
| 6 | Return `null` from an action vs a new `PageReference` | `null` stays on the page; `PageReference` navigates |

---

## 4. Environments, packages, deployment

### Sandboxes
| Type | Data | Storage | Refresh |
|---|---|---|---|
| **Developer** | Metadata only | 200 MB data | 1 day |
| **Developer Pro** | Metadata only | 1 GB data | 1 day |
| **Partial Copy** | Metadata + sample data via a **template** | 5 GB data (verify) | 5 days |
| **Full** | **All** data and metadata (like production) | Same as production | **29 days** |

- Templates work only with **Partial and Full**.
- After a refresh, **email deliverability is "System email only"**.
- A refresh **overwrites** the sandbox. Back up or deploy unsaved work first.
- **Scratch org:** temporary (1–30 days, default 7), created from a **Dev Hub** using `project-scratch-def.json`.
- **Apex can't be edited directly in production**; develop in a sandbox/scratch and deploy.

### Packages
| Type | Upgradeable | Notes |
|---|---|---|
| Unmanaged | **No** | Copy of components |
| Managed | Yes | Namespace, IP protection, AppExchange |
| Unlocked | Yes | Your own modular metadata, dependencies, optional namespace |

### Tools
| Tool | Can | Can't |
|---|---|---|
| **Change sets** | Move metadata between **connected** orgs (production and its sandboxes) | Delete components; work between unrelated orgs; track versions |
| **Salesforce CLI (`sf`)** | Source-driven, repeatable, version control, scratch orgs | – |
| **Metadata API / `package.xml`** | Retrieve/deploy; **`destructiveChanges.xml`** deletes; `checkOnly` validates | – |
| **DevOps Center** | Work items, pipeline, source control | – |

**Test levels:** `NoTestRun` (**not allowed for production with Apex**), `RunSpecifiedTests`, `RunLocalTests` (default for production with Apex; **excludes managed package tests**), `RunAllTestsInOrg`.
**Quick Deploy:** available for about **10 days** after a successful validation, without re-running tests.
**Coverage:** ≥ 75% overall, every trigger has some coverage, all tests pass.

### Key CLI
`sf org login web` · `sf org list` · `sf org open` · `sf org create scratch --definition-file ... --duration-days 3` · `sf project deploy start` (`--dry-run` validates) · `sf project retrieve start` · `sf apex run test --code-coverage --class-names MyTest` · `sf package create --package-type Unlocked`. Files: `sfdx-project.json`, `.forceignore`, `package.xml`.

### Try it
| # | Do | Expected |
|---|---|---|
| 1 | Add a class to `package.xml` with `<members>*</members>` for `ApexClass` | All classes retrieved |
| 2 | Deploy to production with `NoTestRun` and Apex included | Rejected |
| 3 | Validate, wait 11 days, Quick Deploy | Validation expired; validate again |
| 4 | Send an outbound change set to an unconnected org | Not possible; no deployment connection |
| 5 | Add a file path to `.forceignore` and deploy | That file is skipped |

---

## 5. Debugging

| Item | Rule |
|---|---|
| Trace flag | Needed per user; has an expiry; sets the **debug level** |
| Levels (low → high) | `ERROR`, `WARN`, `INFO`, **`DEBUG`** (default for `System.debug`), `FINE`, `FINER`, `FINEST` |
| Categories | Apex Code, **Database** (SOQL/DML), Callout, Workflow, Validation, System, Visualforce |
| `System.debug(LoggingLevel.ERROR, msg)` | Logs at the chosen level |
| Limits | `Limits.getQueries()` (used) and `Limits.getLimitQueries()` (allowed); log ends with **LIMIT_USAGE_FOR_NS** |
| Tools | Developer Console (logs, **Query Editor**, **Execute Anonymous**, **Checkpoints** for heap, **Query Plan** for SOQL index use), **Apex Replay Debugger** (VS Code), **Apex Jobs**, **Deployment Status** |
| Raise detail | Increase Database and Apex Code levels |
| Async jobs | Check **Apex Jobs**; batch **Holding** = flex queue |
| Logs for another user | Create a trace flag for **that** user |

---

## 6. Final recall (Part 4)
- LWC: no template expressions; `key` on loops; `@api` data is read-only; `@wire` needs cacheable; `connectedCallback` has no DOM.
- Events: lowercase name, `on`+name, `bubbles`+`composed` to cross shadow boundaries.
- Aura: `{!}` bound, `{#}` unbound; application event = any component.
- VF: list extension = `StandardSetController`; `rerender` needs a rendered target; `transient` cuts view state.
- Dev sandbox metadata only; Partial = template + 5 days; Full = 29 days; templates for Partial/Full.
- Change sets can't delete; CLI + scratch orgs for source-driven; `NoTestRun` is not allowed for production Apex.
- Quick Deploy ≈ 10 days; `RunLocalTests` excludes managed package tests.

# Module 8 — Test strategy and final capstone

## Test class starter

```apex
@isTest
private class PD1AccountDefaultsTest {
    @isTest
    static void defaultsDescriptionWhenMissing() {
        Account a = new Account(Name = 'PD1-Lab Test Account');
        Test.startTest();
        insert a;
        Test.stopTest();

        Account saved = [
            SELECT Description
            FROM Account
            WHERE Id = :a.Id
        ];
        System.assertEquals(
            'PD1-Lab defaulted by before trigger',
            saved.Description
        );
    }
}
```

If you use the trigger from Module 3, this test should verify its behavior. Adjust the expected value to match your implementation.

## Test assignments

- Add a test for a Description that is already populated.
- Test bulk input (at least 200 records).
- Test negative paths, including `addError`.
- Use `@testSetup` for shared test data when it improves clarity.
- Mock every callout.
- Enqueue asynchronous work between `Test.startTest()` and `Test.stopTest()`, then assert the outcome after `stopTest()`.
- Never hard-code record IDs. Avoid `SeeAllData=true` unless a specific case requires it.
- Assert business outcomes, not only code coverage.

## Capstone: Prospect review automation

Build a small end-to-end solution in your sandbox:
1. Account fields: `Needs_Review__c`, `Review_Status__c`, and optionally `Subscription_Expiry__c`.
2. Before-save Flow to default fields on the triggering Account.
3. Validation rule preventing an invalid status combination.
4. Apex trigger or handler for one clearly justified requirement that Flow does not handle better.
5. A bulk-safe test class covering positive, negative, and 200-record cases.
6. An after-save Flow that creates a related Task when review is required.
7. Optional approval process for review decisions.
8. Optional LWC that displays Accounts needing review, using Lightning Data Service or a cacheable Apex read method.
9. Deploy the metadata to a second sandbox or validate a deployment through Salesforce CLI/change sets.
10. Write a short README explaining data model, security, automation order, test cases, and trade-offs.

## Final revision checklist

- [ ] I can distinguish lookup, master-detail, junction objects, formula, and roll-up summary fields.
- [ ] I can explain OWD, role hierarchy, sharing rules, profiles, permission sets, CRUD, and FLS.
- [ ] I can write bulk-safe Apex using List, Set, Map, and sparse updates.
- [ ] I know trigger context variables and before-vs-after restrictions.
- [ ] I can write SOQL relationship queries, aggregates, semi-joins, and SOSL searches.
- [ ] I know partial-success DML and how to inspect result objects.
- [ ] I can choose Queueable, Batch, Scheduled Apex, or Future appropriately.
- [ ] I can choose before-save vs after-save Flow and use fault paths.
- [ ] I can test asynchronous work and mock HTTP callouts.
- [ ] I understand LWC reactivity, lifecycle, events, LDS, and Apex wire vs imperative calls.
- [ ] I can explain Aura/Visualforce fundamentals and modern alternatives.
- [ ] I can debug logs and describe deployment/test requirements.

## Important note about limits

This workbook contains selected limit values and edge rules, not a complete substitute for the separate governor-limit cheat sheet. Confirm exam-critical numbers against current Salesforce documentation and the version of the exam guide you are using.
