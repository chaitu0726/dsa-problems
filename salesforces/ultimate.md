# PD1 Ultimate Guide: Reference + Sandbox Assignments (module-wise)

**What this is.** One file replacing the four parts you pasted (duplicates removed, gaps filled). Each module has: **Reference** (what each thing CAN and CAN'T do, how often it runs, gotchas) and **Assignments** (steps and snippets to try in a sandbox: Do → Change → Expect → Lesson).

**How to use it.** For each module: read the reference once, then do the assignments. Don't skim the assignments. Seeing the error yourself is what removes the confusion.

**Honest notes**
- I can't run Apex or Salesforce here. "Expect" results come from documented behaviour and known error messages. **Trust what your org shows over what I wrote**, and tell me where they differ. Items marked **(verify)** are the ones I'm least sure of.
- It covers what the exam tends to twist, not every possible detail.

## Contents
0. Exam facts and governor limits
1. Fields, relationships, declarative tools, config storage, data management
2. Security
3. Apex language
4. Triggers and order of execution
5. SOQL, SOSL, DML
6. Async Apex
7. Flow and automation
8. Integration (callouts, REST, events)
9. Testing
10. Lightning Web Components (+ Lightning pages)
11. Aura
12. Visualforce
13. Environments, packages, deployment
14. Debugging
15. Exam traps, decision tables, final recall

---

# MODULE 0: Exam facts and governor limits

| Item | Value (confirm on the official exam guide) |
|---|---|
| Scored questions | 60 (plus up to 5 unscored pilot questions) |
| Time | 105 minutes |
| Passing score | 68% (41 of 60), as you noted |
| Domain weights | Fundamentals 23% · Process Automation & Logic 30% · User Interface 25% · Testing, Debugging & Deployment 22% |

## Governor limits (per transaction)
| Limit | Sync | Async |
|---|---|---|
| SOQL queries | **100** | **200** |
| SOQL rows retrieved | **50,000** | **50,000** |
| SOSL queries / rows per query | 20 / 2,000 | 20 / 2,000 |
| DML statements | **150** | **150** |
| DML rows | **10,000** | **10,000** |
| Callouts / cumulative callout time | 100 / 120 s | 100 / 120 s |
| Callout request/response size | 6 MB | 12 MB |
| CPU time | **10,000 ms** | **60,000 ms** |
| Heap | **6 MB** | **12 MB** |
| `@future` calls | 50 | 0 from future/batch |
| Queueable jobs enqueued | 50 | **1** |
| `Messaging.sendEmail` invocations | 10 | 10 |
| Trigger chunk size | **200** | 200 |
| Trigger recursion depth | 16 | 16 |

**Other numbers:** coverage **75%** · batch scope default **200**, max **2,000** · 5 batch jobs active, 100 in flex queue · 100 scheduled jobs · `OFFSET` max 2,000 · child-to-parent **5** levels · master-detail **2** per object · External IDs **25** per object · roll-ups **25** per object (default) · Visualforce view state **170 KB** · Quick Deploy window about **10 days** · Recycle Bin **15 days** · Data Import Wizard **50,000** records · scratch org 1–30 days (default 7) · Full sandbox refresh **29** days.

Limits reset per transaction; in Batch Apex they reset **per `execute()` chunk**; `Test.startTest()` gives the code under test a fresh set.

---

---

# MODULE 1: Fields, relationships, declarative tools, config storage, data management

## Reference

### 1.1 Field types (all of them)

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
| **Phone** | Up to 40 chars | Be filtered in SOQL | **Not allowed as External ID**; no automatic formatting by default |
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

### 1.2 Relationship rules

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

### 1.3 Declarative tools: can / can't

| Tool | CAN | CAN'T | Runs |
|---|---|---|---|
| **Validation rule** | Block save when formula is **true**; reference parent fields; use `$Permission` bypass; fire from UI, API, Apex, Flow, Data Loader | Reference **child** records; call code; run on delete; show messages other than text | On insert/update, **after before triggers**, before save |
| **Formula field** | Read parent fields, text/date math | Be written; cross unlimited relationships (limited spans) | Every read |
| **Record type** | Different picklist values, page layouts (via assignment), business processes (Lead, Opportunity, Case, Solution) | Control **FLS or record access** | Chosen on create; changeable |
| **Page layout** | Field/button/related list arrangement, UI-required | Secure a field, affect API/Apex | UI only |
| **Dynamic Forms** | Field visibility rules per field on record pages | Replace FLS | Lightning record pages |
| **Approval process** | Multi-step approvals, record locking, email alerts, field updates | Run Apex directly (use Apex actions in flow); edit by non-admins while locked | When submitted |
| **Duplicate rules** | Alert/block using matching rules | Run on all objects (supported objects only) | On create/update |


### Extra: External ID and field-history facts
- **External ID** allowed types: **Auto Number, Email, Number, Text**. Up to **25** per object. Used for **upsert** and for resolving relationships in Data Loader.
- **Phone, Long Text, Encrypted and Formula fields can't be External IDs.**
- **Field history tracking:** up to 20 fields per object (verify); kept 18 months in the UI, 24 months via API (verify).
- **Global value set** = one picklist definition reused across fields. **Restricted** picklists block values not in the list, even via API.
- **Dependent picklist:** controlling field is a picklist or checkbox (never multi-select).

### Configuration storage: custom metadata vs custom settings vs labels
| | Custom metadata type | Custom setting | Custom label |
|---|---|---|---|
| Records deploy as metadata | **Yes** | **No** (data) | **Yes** |
| Apex access | `Type__mdt.getInstance('DevName')`, `getAll()` (no SOQL limit use); SOQL also allowed | Hierarchy: `getInstance()`, `getOrgDefaults()`; List: `getAll()` (no SOQL limit use) | `System.Label.Name` |
| Per user/profile values | No | **Hierarchy** type: yes | No |
| Edit from Apex DML | **No** (use Metadata API) | Yes | No |
| Translatable | No | No | **Yes** |
| Typical use | App config, mappings, feature flags that must deploy | Per-user/profile defaults, bypass flags | UI text in Apex/VF/Aura/LWC |

### Data management tools
| Tool | Can | Can't |
|---|---|---|
| **Data Import Wizard** | Up to **50,000** records; accounts, contacts, leads, solutions, campaign members, custom objects; match on name/Id/External ID | Other standard objects; run unattended; delete or hard delete |
| **Data Loader** | Insert, update, **upsert**, delete, **hard delete**, export, **export all**; up to millions of records; Bulk API option; command line | **Merge**; run in the browser |
| **Data Export service** | Scheduled weekly/monthly zip of CSVs of all data | Be near real-time |
| **Recycle Bin** | Restore for **15 days** | Restore hard-deleted records |
| **Duplicate management** | Matching rule + duplicate rule (alert or block) on supported objects | Run on all objects; fix existing duplicates automatically |
| **Reports export** | CSV/Excel of report data | Bulk update |

---

## Assignments: Module 1 (do these in a sandbox)

#### 1.1 Relationships: lookup vs master-detail
**Do**
1. Create custom object `Invoice__c` with a number field `Amount__c` and a **Lookup** to Account (`Account__c`).
2. Open Account → Fields & Relationships → try to add a **Roll-Up Summary**. Note it isn't possible.
3. Delete `Account__c` and recreate it as **Master-Detail** to Account. Add roll-up `Total__c` = SUM of `Amount__c`.
4. Create an Account with 2 invoices; check the roll-up.
5. **Delete the Account.** Check the Recycle Bin. **Undelete** it.
6. Create `Student__c`, `Course__c`, junction `Enrollment__c` with **two master-detail** fields. Delete one Student and watch its enrollments disappear.
**Expect:** roll-up appears only with master-detail; deleting the master deletes children; undelete restores both.
**Change and observe:** try to add a master-detail from `Invoice__c` to itself (blocked); try to make a lookup with blank values into master-detail (blocked until all rows have a parent).
**Lesson:** master-detail = required parent, cascade delete, inherited sharing, roll-ups, max 2, no self.

#### 1.2 Field types: what you can't write or filter
**Do:** on Account create `Name_Len__c` (Formula, Number: `LEN(Name)`), `Notes__c` (Long Text Area), `Secret_Enc__c` (Text, Encrypted), `Ref_No__c` (Auto Number `REF-{0000}`). Then run:
```apex
Account a = new Account(Name = 'FieldLab');
insert a;
a = [SELECT Id, Name_Len__c, Ref_No__c FROM Account WHERE Id = :a.Id];
System.debug(a.Name_Len__c + ' / ' + a.Ref_No__c);     // expect 8 / REF-0000 (number may differ)
```
**Change one line at a time:**
| Change | Expect |
|---|---|
| `a.Name_Len__c = 5;` | Compile error: field is not writeable |
| `a.Ref_No__c = 'x';` | Compile error: field is not writeable |
| `[SELECT Id FROM Account WHERE Notes__c = 'x']` | Error: field can not be filtered in a query call |
| `[SELECT Id FROM Account WHERE Secret_Enc__c = 'x']` | Same style of error (encrypted fields can't be filtered) |
| `[SELECT Id FROM Account ORDER BY Notes__c]` | Error: can't sort a long text field |
**Lesson:** formula and auto-number are read-only; long and encrypted text can't be filtered or sorted.

#### 1.3 Validation rule: DML vs Database methods
**Do:** add an Account validation rule `AND(ISBLANK(Industry), Name = 'NeedsIndustry')`, error message `Industry required`. Run:
```apex
Account a = new Account(Name = 'NeedsIndustry');
try { insert a; } catch (DmlException e) { System.debug(e.getDmlStatusCode(0) + ' | ' + e.getDmlMessage(0)); }
Database.SaveResult r = Database.insert(a, false);
System.debug(r.isSuccess() + ' | ' + r.getErrors()[0].getMessage());
```
**Expect:** `FIELD_CUSTOM_VALIDATION_EXCEPTION | Industry required`, then `false | Industry required` (no exception).
**Lesson:** validation rules fire for Apex DML too; `Database.insert(rec, false)` reports instead of throwing.

#### 1.4 External ID upsert (Data Loader)
**Do:** add `Legacy_Id__c` (Text, **External ID**, Unique) on Account. Make a CSV with `Legacy_Id__c,Name` rows `L-1,Lab One` and `L-2,Lab Two`. Data Loader → **Upsert** → Account → External ID `Legacy_Id__c`. Run it twice, editing a name and adding `L-3` the second time.
**Expect:** first run inserts 2; second updates `L-1`, leaves `L-2`, inserts `L-3`.
**Lesson:** upsert matches on Id or External ID; Data Loader handles big volumes; no merge.

---

---

# MODULE 2: Security

## Reference

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


## Assignments: Module 2

#### 2.1 `with sharing` vs FLS (needs a test class, because `runAs` works only in tests)
**Do:** create `Secret__c` (Text) on Account, visible **only** to System Administrator in the FLS step. Set the Account OWD to **Public Read Only**. Then save and run this test, and read the log (filter `USER_DEBUG`):
```apex
@isTest
private class SecLabTest {
    @isTest static void fls() {
        insert new Account(Name = 'Lab Sec', Secret__c = 'top');
        User u = new User(Alias='std1', Email='std1@example.com', EmailEncodingKey='UTF-8', LastName='Std',
            LanguageLocaleKey='en_US', LocaleSidKey='en_US',
            ProfileId=[SELECT Id FROM Profile WHERE Name='Standard User'].Id,
            TimeZoneSidKey='America/New_York', Username='std1' + System.currentTimeMillis() + '@example.com');
        insert u;
        System.runAs(u) {
            List<Account> a = [SELECT Id, Name, Secret__c FROM Account WHERE Name = 'Lab Sec'];
            System.debug('A system mode rows=' + a.size() + ' secret=' + (a.isEmpty() ? null : a[0].Secret__c));
            try { List<Account> b = [SELECT Id, Secret__c FROM Account WITH USER_MODE]; System.debug('B worked'); }
            catch (QueryException e) { System.debug('B blocked: ' + e.getMessage()); }
            try { List<Account> c = [SELECT Id, Secret__c FROM Account WITH SECURITY_ENFORCED]; System.debug('C worked'); }
            catch (QueryException e) { System.debug('C blocked: ' + e.getMessage()); }
            SObjectAccessDecision d = Security.stripInaccessible(AccessType.READABLE, a);
            System.debug('D keeps Secret__c? ' + d.getRecords()[0].getPopulatedFieldsAsMap().containsKey('Secret__c'));
        }
    }
}
```
**Expect:** A returns the secret (system mode ignores FLS); B and C throw; D prints `false`.
**Change:** declare a helper class `with sharing` and run the same query from it: the secret is **still** returned. Move the `runAs` code to Execute Anonymous: error (runAs only in tests).
**Lesson:** `with sharing` = records only. `USER_MODE` / `SECURITY_ENFORCED` throw. `stripInaccessible` strips.

#### 2.2 OWD, role hierarchy, manual sharing
**Do**
1. Create roles **VP** (top) and **Rep** (under VP). Create two Rep users (Rep A, Rep B) and one VP user, all on a profile with Read/Edit/Create on Opportunity.
2. Set Opportunity OWD to **Private**. Log in as **Rep A** (use "Login as"), create an opportunity.
3. Check visibility as Rep B, then as the VP.
4. As Rep A, use **Sharing** on the record to share it with Rep B (Read Only). Check Rep B.
5. Change the owner of the opportunity to Rep B; log back in as Rep A.
6. On the Opportunity sharing settings, untick or tick **Grant Access Using Hierarchies** (custom objects only; for standard objects it's fixed). Repeat the test on a custom object.
**Expect:** Rep B can't see the record until shared; VP sees it (hierarchy); after the owner change, the manual share is gone for the old owner's targets (verify what remains).
**Lesson:** OWD baseline → hierarchy up → manual sharing (lost on owner change).

#### 2.3 Permission set adds access
**Do:** make a permission set that grants **Delete** on Case. Assign it to one user. Compare with a user on the same profile without it.
**Change:** try to find a way for a permission set to remove Delete from the profile (not possible, except via **muting** in a permission set *group*).
**Lesson:** permission sets only add; muting belongs to permission set groups.

---

---

# MODULE 3: Apex language

## Reference

**Primitives:** Integer (32-bit), Long (64-bit), Double, Decimal, String, Boolean, Date, Datetime, Time, Id, Blob, Object. **Every uninitialized variable is `null`**, including Boolean.

| Rule | Detail |
|---|---|
| `==` on strings | **Case-insensitive** (`'abc' == 'ABC'` is true). `equals()` is case-sensitive; `equalsIgnoreCase()` ignores case |
| Map/Set with String keys | **Case-sensitive** (`'a'` ≠ `'A'`) |
| SOQL `=` and `LIKE` on text | **Case-insensitive** |
| Id comparison | 15- and 18-char forms of the same Id are equal |
| `null` in math or `if (nullBoolean)` | `NullPointerException` (use `?.`, `??`, `== true`) |
| `List.get(i)` out of range | `ListException: List index out of bounds` |
| Collections | Passed **by reference**: `b = a;` shares the list. Copy with `new List<X>(a)` or `a.clone()` (both **shallow**); `deepClone()` for sObject lists |
| Modify a list during `for (x : list)` | `FinalException: Cannot modify a collection while it is being iterated` |
| `Set.add()` | Returns `false` if the value already exists |
| `Map.put()` | Returns the previous value (or null) |
| `Map.get(missing)` | `null` |
| `switch on` | Works on Integer, Long, String, Enum, sObject; `when 'A','B'`, `when else` |
| Ternary | `x != null ? x : 'default'` |
| Safe navigation | `a?.b?.c` returns null instead of throwing |
| Coalesce | `a ?? b` |
| Constants | `static final` (assigned once, only at declaration or in static initializer) |
| Static variable | Lasts **one transaction**; shared across all instances; shared across trigger chunks |
| Instance variable | One per object |
| Access modifiers | `private` (default for members), `protected`, `public`, `global` (visible across namespaces). Top-level classes must be `public` or `global` |
| Class modifiers | `virtual` (can extend), `abstract` (can have abstract methods, cannot instantiate), `with/without/inherited sharing` |
| Method override | Parent `virtual`/`abstract`; child uses `override`; call parent with `super` |
| Interface | Signatures only; a class can implement **many**; extends **one** class |
| Constructors | Overloading allowed; no return type; `this(...)` chains constructors |
| Properties | `public String x { get; private set; }` read-only outside; `{ get { ... } }` computed |
| SObject not queried field | `SObjectException: SObject row was retrieved via SOQL without querying the requested field` |
| Update with only some fields set | Only those fields are written |
| `Id` null before insert; set after | After `insert acc;`, `acc.Id` is populated |
| Custom exception | `public class X extends Exception {}`; name ends with `Exception` |
| Catch order | Specific before generic; `finally` always runs |
| `LimitException` | **Uncatchable**; whole transaction rolls back |
| Date math | Use `addDays`, `addMonths`, `daysBetween` |
| Dynamic | `Schema.SObjectType`, `sObj.get('Field')`, `Database.query(string)`, `Type.forName('Cls')` |

**Annotations**

| Annotation | CAN | CAN'T |
|---|---|---|
| `@isTest` | Mark class/method test; test code doesn't count toward org code limit | Be called from non-test code |
| `@testSetup` | See Module 9 | |
| `@TestVisible` | Expose private members to tests | – |
| `@future` | Static void, primitive/collection-of-primitive params, `callout=true` | Return values, take sObjects, chain, run from another future/batch |
| `@InvocableMethod` | Be used in Flow; one per class; static; **List** parameter | Have more than one per class |
| `@InvocableVariable` | Group inputs in a wrapper class | – |
| `@AuraEnabled` | Expose to LWC/Aura; `cacheable=true` for `@wire` | Perform DML when `cacheable=true` |
| `@RemoteAction` | Static method callable from Visualforce JavaScript | Be non-static |
| `@RestResource`, `@HttpGet/@HttpPost…` | Expose Apex REST | – |
| `@ReadOnly` | Raise query limits for read-only reports/remote actions | Do DML |
| `@Deprecated` | Mark for managed packages | – |

### Collections: comparison and experiment

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


### Extra Apex details often twisted
| Topic | Rule |
|---|---|
| **Enum** | `public enum Season { WINTER, SPRING }`; `Season.WINTER.name()`, `Season.values()`; usable in `switch on`; compare with `==` |
| **Sorting custom classes** | Implement `Comparable` (`compareTo(Object o)`) so `List.sort()` works |
| **Type conversion** | `Integer.valueOf('abc')` throws `TypeException`; `String.valueOf(x)`; `Decimal.setScale(2)` rounds |
| **instanceof / casting** | `if (o instanceof Account) { Account a = (Account) o; }`; bad cast throws `TypeException` |
| **Date/Time** | `Date.today()`, `d.addDays(n)`, `d.daysBetween(other)`, `Date.newInstance(y,m,d)`, `Datetime.now()`, `dt.format()`, `Time.newInstance(h,m,s,ms)` |
| **Limits class** | `Limits.getQueries()` / `getLimitQueries()`, `getDmlStatements()`, `getDmlRows()`, `getHeapSize()`, `getCpuTime()`, `getCallouts()` |
| **Static initializer** | `static { ... }` runs once per transaction when the class is first used |
| **Initialization order** | static fields/blocks → instance fields → constructor |
| **User/context** | `UserInfo.getUserId()`, `UserInfo.getProfileId()`, `System.today()` |
| **Schema describe** | `Account.SObjectType.getDescribe().fields.getMap()`; field checks `isAccessible()`, `isCreateable()`, `isUpdateable()`, `isDeletable()` |
| **Common interfaces** | `Comparable`, `Queueable`, `Schedulable`, `Database.Batchable<SObject>`, `Database.Stateful`, `Database.AllowsCallouts`, `HttpCalloutMock`, `Finalizer` |

### Experiments: break and fix (language)

| # | Snippet | Change | Expected | Fix |
|---|---|---|---|---|
| 1 | `Account a = [SELECT Name FROM Account LIMIT 1]; System.debug(a.Industry);` | none | `System.SObjectException: SObject row was retrieved via SOQL without querying the requested field` | Add `Industry` to the SELECT |
| 2 | `Account a = [SELECT Id FROM Account WHERE Name='NoSuch'];` | none | `System.QueryException: List has no rows for assignment to SObject` | Assign to a `List<Account>` and check `isEmpty()` |
| 3 | `public virtual class A { public virtual String s(){return 'A';} } public class B extends A { public String s(){return 'B';} }` | none | Compile error: method overrides without `override` | Add `override` |
| 4 | `final Integer x = 1; x = 2;` | none | Compile error: Final variable has already been initialized | Remove `final` |
| 5 | `Account a = new Account(Id = existingId, Rating = 'Hot'); update a;` | add `Name = null` | Only assigned fields change; `Name = null` would clear Name | Set only what you mean to change |
| 6 | `update new List<Account>{ a, a };` (same Id twice) | none | `Duplicate id in list` error | De-duplicate with a Map |


## Assignments: Module 3

#### 3.1 Apex rules checker (anonymous Apex; compare your output to the expected comment on each line)
```apex
System.debug('abc' == 'ABC');                                        // true  (== ignores case)
System.debug('abc'.equals('ABC'));                                   // false
Boolean b; System.debug(b);                                          // null
Set<String> s = new Set<String>{'a'}; System.debug(s.contains('A')); // false (case-sensitive)
System.debug(s.add('a'));                                            // false (already there)
Map<String,Integer> m = new Map<String,Integer>{'a' => 1};
System.debug(m.put('a', 2));                                         // 1 (previous value)
System.debug(m.get('zzz'));                                          // null
List<Integer> x = new List<Integer>{1,2,3};
List<Integer> y = x; y.add(4); System.debug(x.size());               // 4 (same list)
List<Integer> z = x.clone(); z.add(5); System.debug(x.size());       // 4 (clone is separate)
try { Integer q = x[99]; } catch (Exception e) { System.debug(e.getTypeName()); }        // System.ListException
try { for (Integer i : x) { x.add(9); } } catch (Exception e) { System.debug(e.getTypeName()); } // System.FinalException (verify)
Account a = null; System.debug(a?.Name ?? 'none');                   // none
try { Boolean n; if (n) { } } catch (Exception e) { System.debug(e.getTypeName()); }     // System.NullPointerException
System.debug(String.isBlank('  ') + ' ' + String.isEmpty('  '));     // true false
try { Integer.valueOf('abc'); } catch (Exception e) { System.debug(e.getTypeName()); }   // System.TypeException
System.debug('Salesforce'.substring(0, 5));                          // Sales
```
**Change and observe:** make `x` a `final` list and add to it (allowed: final stops reassignment only); reassign it (compile error).

#### 3.2 Inheritance and sharing inheritance
**Do:** create these classes and run the anonymous call.
```apex
public virtual class Animal { public virtual String sound() { return 'generic'; } }
public class Dog extends Animal { public override String sound() { return 'woof'; } }
// anonymous
Animal a = new Dog();
System.debug(a.sound());                      // woof (runtime type wins)
```
**Change:** delete `override` (compile error); delete `virtual` from `Animal.sound` (compile error).
**Sharing check:** make `public with sharing class Caller { public static Integer n(){ return Helper.n(); } }` and `public without sharing class Helper { public static Integer n(){ return [SELECT COUNT() FROM Account]; } }`. Run as a restricted user (via a test with `runAs`): Helper uses **its own** declared mode.
**Lesson:** `override` is mandatory; an explicitly declared class keeps its own sharing mode.

---

#### 3.3 One snippet per OOP rule (create each class via Developer Console → File → New → Apex Class; run the anonymous lines in Execute Anonymous)

**A. `virtual`**
```apex
public virtual class Animal { public virtual String sound() { return 'generic'; } }
public class Dog extends Animal { public override String sound() { return 'woof'; } }
// anonymous
Animal a = new Dog();
System.debug(a.sound());     // woof
```
Break it: remove `override` → compile error (method overrides a virtual method without `override`). Remove `virtual` from `Animal` → error (class can't be extended).

**B. `abstract`**
```apex
public abstract class Shape {
    public abstract Decimal area();
    public String describe() { return 'Area=' + area(); }     // abstract classes CAN hold bodies
}
public class Sq extends Shape {
    private Decimal s;
    public Sq(Decimal s) { this.s = s; }
    public override Decimal area() { return s * s; }
}
// anonymous
System.debug(new Sq(3).describe());      // Area=9
```
Break it: `new Shape();` → compile error (can't instantiate abstract). Delete `area()` from `Sq` → error (must implement abstract method).

**C. Interface**
```apex
public interface Greeter { String greet(String n); }
public interface Logger  { void log(String m); }
public class Both implements Greeter, Logger {          // MANY interfaces
    public String greet(String n) { return 'Hi ' + n; }
    public void log(String m) { System.debug(m); }
}
// anonymous
System.debug(new Both().greet('Sam'));   // Hi Sam
```
Break it: add a method **body** inside `Greeter` → error. Add a field (`Integer x = 1;`) → error. Remove `log` from `Both` → error (must implement all methods).

**D. `final`**
```apex
// anonymous
final Integer x = 1;
final List<Integer> l = new List<Integer>();
l.add(5);                                 // allowed: the reference is fixed, not the contents
System.debug(l);                          // (5)
```
Break it: add `x = 2;` → compile error (final variable can't be reassigned). Add `l = new List<Integer>();` → same error.

**E. `static`**
```apex
public class Counter {
    public static Integer n = 0;          // shared by all instances
    public Integer i = 0;                 // one per instance
    public Counter() { n++; i++; }
}
// anonymous run 1
new Counter(); Counter c = new Counter();
System.debug(Counter.n + ' ' + c.i);      // 2 1
```
Run this **second** anonymous block afterwards: `System.debug(Counter.n);` → `0` (static values don't survive between transactions).

**F. Constructors (overload, `this(...)`, `super(...)`)**
```apex
public virtual class Person {
    public String name; public Integer age;
    public Person() { this('Unknown', 0); }                 // this(...) must be the first statement
    public Person(String n) { this(n, 18); }
    public Person(String n, Integer a) { name = n; age = a; }
}
public class Kid extends Person { public Kid(String n) { super(n, 5); } }
// anonymous
System.debug(new Person().name + ' ' + new Person('Ann').age + ' ' + new Kid('Kai').age);   // Unknown 18 5
```
Break it: add `return 5;` inside a constructor → compile error (constructors return nothing). Put `this(...)` after another statement → error.

**G. Inner classes and sharing** (needs a test, because `runAs` works only in tests; set Account OWD to **Private** first)
```apex
public with sharing class Outer {
    public static Integer outerCount() { return [SELECT COUNT() FROM Account]; }
    public without sharing class Inner { public Integer innerCount() { return [SELECT COUNT() FROM Account]; } }
}
```
```apex
@isTest
private class OuterTest {
    @isTest static void t() {
        insert new Account(Name = 'Hidden from std user');            // owned by the test runner (admin)
        User u = new User(Alias='in1', Email='in1@example.com', EmailEncodingKey='UTF-8', LastName='In',
            LanguageLocaleKey='en_US', LocaleSidKey='en_US',
            ProfileId=[SELECT Id FROM Profile WHERE Name='Standard User'].Id,
            TimeZoneSidKey='America/New_York', Username='in1' + System.currentTimeMillis() + '@example.com');
        insert u;
        System.runAs(u) {
            System.debug('outer (with sharing)   = ' + Outer.outerCount());               // expect 0
            System.debug('inner (without sharing) = ' + new Outer.Inner().innerCount());  // expect 1 or more
        }
    }
}
```
Expect: the outer count is limited by sharing; the inner class **declares its own** mode, so it isn't limited. An inner class with **no** keyword does not take `Outer`'s `with sharing` either (it follows the normal inherit-from-caller rule, verify in your org). Also note Apex allows only **one level** of nesting.

**H. Access modifiers**
```apex
public virtual class Base {
    private   String p  = 'private';
    protected String pr = 'protected';
    public    String pu = 'public';
    String d = 'default is private';
    protected virtual String hello() { return 'base'; }
}
public class Child extends Base {
    public String test() { return pr + ' ' + hello() + ' ' + pu; }   // protected and public are visible
    // return p;   <-- would not compile: private is not visible to a subclass
}
// anonymous
System.debug(new Child().test());          // protected base public
```
Break it (anonymous): `new Child().pr` → error (not visible outside the class/subclasses); `new Base().p` → error. For `global`, a global class/method matters only across namespaces (managed packages): `global class G { global static String x() { return 'g'; } }` works the same in your own org, so this one can't be shown in a single sandbox.

**I. Properties (`get; private set;`)**
```apex
public class Acct {
    public String name { get; private set; }          // read-only outside
    public String upper { get { return name.toUpperCase(); } }   // computed, no setter
    public Acct(String n) { name = n; }
    public void rename(String n) { name = n; }        // inside the class, the private setter works
}
// anonymous
Acct a = new Acct('sam');
a.rename('lee');
System.debug(a.name + ' ' + a.upper);                 // lee LEE
```
Break it: `a.name = 'x';` → compile error (setter is private). `a.upper = 'X';` → error (no setter).

---

---

# MODULE 4: Triggers and order of execution

## Reference

| Event | `new` | `old` | `newMap` | `oldMap` | CAN | CAN'T |
|---|---|---|---|---|---|---|
| before insert | ✔ editable | ✘ | ✘ | ✘ | Set fields, `addError`, query | Use `Id` (null), use `newMap` |
| before update | ✔ editable | ✔ | ✔ | ✔ | Compare old/new, set fields, `addError` | – |
| before delete | ✘ | ✔ | ✘ | ✔ | `addError` on **old** records to block delete | Edit records |
| after insert | ✔ **read-only** | ✘ | ✔ | ✘ | Use `Id`, create related records | Edit `Trigger.new` directly (`FinalException`) |
| after update | ✔ read-only | ✔ | ✔ | ✔ | Update other records | Edit `Trigger.new`; recursion without a guard |
| after delete | ✘ | ✔ | ✘ | ✔ | Clean up related data | `addError` to block |
| after undelete | ✔ read-only | ✘ | ✔ | ✘ | Rebuild related data | Exist as *before* undelete |

- A trigger runs per chunk of **up to 200** records; **limits are shared across all chunks** of the transaction.
- `addError` on a record in a bulk save fails **that record** (partial) when `allOrNone=false`; with plain DML it fails the **whole** statement.
- Triggers fire from UI, API, Apex DML, flows, Data Loader, cascade deletes (children).
- `Trigger.isExecuting`, `isBefore`, `isAfter`, `isInsert`, `isUpdate`, `isDelete`, `isUndelete`, `operationType` (enum).
- **Handler pattern:** one trigger per object, logic in a class. **Recursion:** use a static `Set<Id>` (a static Boolean blocks later chunks).
- A trigger in a **before** context can edit only the triggering records; edits to **other** records need DML.

**Order of execution (full):**
1. Load record / system validation (required fields, types, formats)
2. **Before-save record-triggered flows**
3. **Before triggers**
4. System validation again + **custom validation rules**
5. **Duplicate rules**
6. Save to database (not committed)
7. **After triggers**
8. Assignment rules, auto-response rules, **workflow rules** (field updates re-run before/after **update** triggers once)
9. Escalation rules
10. **After-save record-triggered flows** (and other automation)
11. Roll-up summary (and cross-object) recalculation; parent may fire its own triggers
12. **Commit**
13. Post-commit: emails, async Apex (future/queueable/batch), platform events after commit

### Experiments: trigger lab (Account)

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


## Assignments: Module 4 (also do the "Try it: trigger lab" above)

#### 4.1 Order of execution: flow vs trigger vs validation
**Do**
1. Create a **before-save** record-triggered flow on Account (on create): Assignment `{!$Record.Site}` = `FLOW`.
2. Create this trigger:
```apex
trigger AccOrder on Account (before insert) {
    for (Account a : Trigger.new) { a.Site = 'TRIGGER saw: ' + a.Site; }
}
```
3. Add an Account validation rule: `CONTAINS(Site, 'TRIGGER saw: FLOW')` with message `Order proves flow ran first` (fails when the text is present, so you can see the message).
4. Run: `insert new Account(Name='Order Lab', Site='orig');`
**Expect:** the validation fires because Site is `TRIGGER saw: FLOW`, which proves: before-save flow → before trigger → validation rule. Delete the rule, rerun, and query the saved Site value.
**Lesson:** order is system validation → before-save flow → before trigger → custom validation → save → after trigger → ... → after-save flow.

#### 4.2 Recursion guard: Boolean vs Set<Id>
**Do**
```apex
public class RecGuard { public static Set<Id> done = new Set<Id>(); public static Boolean ran = false; }
trigger AccRec on Account (after update) {
    List<Account> upd = new List<Account>();
    for (Account a : Trigger.new) {
        if (!RecGuard.done.contains(a.Id)) { RecGuard.done.add(a.Id); upd.add(new Account(Id = a.Id, Description = 'r')); }
    }
    if (!upd.isEmpty()) update upd;
}
```
Run `update [SELECT Id FROM Account LIMIT 5];` (works). **Change:** remove the `done` check so the trigger always updates → `maximum trigger depth exceeded`. Then switch to the Boolean version (`if (!RecGuard.ran) { RecGuard.ran = true; ... }`) and update **400** accounts: only the first 200 are processed.
**Lesson:** static variables last the whole transaction across chunks, so use a **Set<Id>** and not a Boolean.

---

---

# MODULE 5: SOQL, SOSL, DML

## Reference

**SOQL**

| Topic | Rule |
|---|---|
| `SELECT *` | **Not allowed.** `FIELDS(STANDARD)` allowed; `FIELDS(ALL)`/`CUSTOM` need `LIMIT 200` or less (verify) |
| JOIN | **None.** Use relationships |
| Child → parent | Dot path up to **5 levels** (`Contact.Account.Owner.Manager.Name`) |
| Parent → child | Subquery with plural relationship name; **filter/order inside the subquery** |
| Custom relationship | `Parent__r.Field__c` (`__r`); standard parent is singular (`Account.Name`) |
| Aggregates | `COUNT()`, `COUNT(field)` (**ignores nulls**), `COUNT_DISTINCT`, `SUM`, `AVG`, `MIN`, `MAX`; result type `AggregateResult`; unaliased columns are `expr0`, `expr1` |
| Non-aggregated field with aggregate | **Error unless in GROUP BY** |
| `HAVING` vs `WHERE` | HAVING filters aggregates; WHERE filters rows before grouping |
| `ROLLUP` / `CUBE` | Subtotals in GROUP BY |
| `ORDER BY` | `ASC`/`DESC`, `NULLS FIRST/LAST` |
| `LIMIT` / `OFFSET` | `OFFSET` max **2,000** |
| Long Text Area / Rich Text / Encrypted | **Not allowed in WHERE / ORDER BY / GROUP BY** |
| `LIKE` | `%` any chars, `_` one char, case-insensitive |
| Date literals | `TODAY`, `LAST_N_DAYS:7`, `NEXT_QUARTER`, `THIS_MONTH` (no parentheses) |
| `NULL` | `WHERE Field = null` |
| Multi-select picklist | `INCLUDES('a;b')`, `EXCLUDES(...)` |
| Polymorphic | `What.Type = 'Account'`, `TYPEOF What WHEN Account THEN Name END` |
| Semi-join | `WHERE Id IN (SELECT AccountId FROM Opportunity ...)` |
| Anti-join | `WHERE Id NOT IN (SELECT ...)` |
| `ALL ROWS` | Include deleted (and archived Task/Event) |
| `FOR UPDATE` | Locks the rows until the transaction ends; not with aggregate queries |
| `FOR VIEW` / `FOR REFERENCE` | Update `LastViewedDate` / `LastReferencedDate` |
| `WITH SECURITY_ENFORCED`, `WITH USER_MODE` | FLS/CRUD enforcement (throws) |
| Bind variables | `:var`, works with sObjects, collections, and dynamic SOQL (`Database.query`; `Database.queryWithBinds` for explicit maps) |
| Results | `List<SObject>`; single assignment needs exactly **1** row (**0 or >1 = `QueryException`**); `COUNT()` returns Integer |
| SOQL for loop | `for (Account a : [SELECT ...])` fetches **200 at a time** (heap-friendly) but **still counts rows against 50,000** |

**SOSL**

| Topic | Rule |
|---|---|
| Syntax | `FIND 'term*' IN ALL FIELDS RETURNING Account(Id, Name WHERE ...), Contact(...)` |
| Scope options | `ALL FIELDS`, `NAME FIELDS`, `EMAIL FIELDS`, `PHONE FIELDS`, `SIDEBAR FIELDS` |
| Searches | Text, email, phone fields only (**not** numbers, dates) |
| Wildcards | `*` and `?`; at least 2 characters |
| Returns | `List<List<SObject>>` in RETURNING order |
| Limits | 20 queries, **2,000** rows per query |
| Tests | Returns **empty** unless you call `Test.setFixedSearchResults` |
| Freshness | New records are indexed with a short delay |

**DML**

| Statement | CAN | CAN'T / gotchas |
|---|---|---|
| `insert` | Insert list; Id populated after | Pass records with Id; exceed 10,000 rows |
| `update` | Update by Id | Update without Id; write read-only (formula, auto-number, rollup) fields |
| `upsert` | Match on **Id or External ID**; insert or update | Contain **duplicate external IDs** in one call (error) |
| `delete` | To Recycle Bin (15 days) | Delete without Id |
| `undelete` | Restore from bin (query `ALL ROWS` for Ids) | Restore hard-deleted records |
| `merge` | **Leads, Contacts, Accounts** only, up to **3** records at once | Merge other objects |
| `Database.*(list, false)` | Partial success; `SaveResult`/`UpsertResult`/`DeleteResult` | Know failed Ids reliably (use list index) |
| `Database.DMLOptions` | `assignmentRuleHeader.useDefaultRule`, `allowFieldTruncation`, `duplicateRuleHeader.allowSave` | – |
| `Database.convertLead` | Convert leads in Apex | – |
| Savepoint | `Database.setSavepoint()` / `Database.rollback(sp)` | Roll back callouts, emails, or async jobs already queued (they are not rolled back); Ids of rolled-back inserts stay on the object but are invalid |
| Setup + non-setup DML | – | **MIXED_DML_OPERATION** in one transaction (User/Group etc. + normal objects). Fix: async or `System.runAs` in tests |

### Experiments: SOQL

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

### Experiments: DML

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


## Assignments: Module 5

#### 5.1 DML and SOQL playground (anonymous Apex)
```apex
// partial success
List<Account> l = new List<Account>{ new Account(Name = 'Lab Good'), new Account() };
for (Database.SaveResult s : Database.insert(l, false)) System.debug(s.isSuccess() + ' ' + s.getErrors());
// true first; false + REQUIRED_FIELD_MISSING second

// all-or-nothing
try { insert new List<Account>{ new Account(Name = 'Lab Good2'), new Account() }; }
catch (DmlException e) { System.debug(e.getDmlStatusCode(0)); }
System.debug([SELECT COUNT() FROM Account WHERE Name = 'Lab Good2']);   // 0

// duplicate Id in list
Account a1 = new Account(Name = 'Lab Dup'); insert a1;
try { update new List<Account>{ a1, a1 }; } catch (Exception e) { System.debug(e.getMessage()); }  // Duplicate id in list

// zero rows into a single sObject
try { Account x = [SELECT Id FROM Account WHERE Name = 'NoSuchName']; } catch (QueryException e) { System.debug(e.getMessage()); }

// aggregates
for (AggregateResult ar : [SELECT Name n, COUNT(Id) c FROM Account WHERE Name LIKE 'Lab%' GROUP BY Name]) {
    System.debug(ar.get('n') + ' ' + ar.get('c'));
}
// semi and anti join
System.debug([SELECT COUNT() FROM Account WHERE Id IN (SELECT AccountId FROM Contact)]);
System.debug([SELECT COUNT() FROM Account WHERE Id NOT IN (SELECT AccountId FROM Contact)]);
```
**Change:** add `WHERE COUNT(Id) > 1` before `GROUP BY` (syntax error; use `HAVING`). Add `FOR UPDATE` plus `ORDER BY` (error). Select a long-text field in `WHERE` (error).

#### 5.2 Limits on purpose
```apex
// run each block separately
for (Integer i = 0; i < 101; i++) { List<Account> t = [SELECT Id FROM Account LIMIT 1]; }   // Too many SOQL queries: 101
```
```apex
try { for (Integer i = 0; i < 151; i++) insert new Account(Name = 'Lim' + i); }
catch (Exception e) { System.debug('caught?'); }                                            // NOT caught: LimitException
System.debug([SELECT COUNT() FROM Account WHERE Name LIKE 'Lim%']);                         // run in the query editor: 0
```
**Lesson:** the DML/SOQL limit exception is uncatchable and the whole transaction rolls back.

---

---

# MODULE 6: Async Apex

## Reference

| | `@future` | Queueable | Batch | Schedulable |
|---|---|---|---|---|
| Params | primitives, collections of primitives | any serializable (sObjects, custom classes) | constructor | – |
| Returns | nothing | job Id | job Id | job Id |
| Callouts | `@future(callout=true)` | `implements Database.AllowsCallouts` | `Database.AllowsCallouts` | not directly; start async |
| Chain | **no** | **yes (one child per execute)** | `finish()` can start another batch | start batch/queueable from `execute` |
| From trigger | yes | yes | yes | – |
| From future/batch | **no (future from future/batch not allowed)** | **max 1 enqueue** | `executeBatch` allowed in `finish` | – |
| Per transaction | **50** calls | **50** enqueues (sync), **1** (async) | – | 100 scheduled jobs at once |
| Order guaranteed | **no** | per chain | per chunk | at cron time |
| Limits | async limits (SOQL 200, heap 12 MB, CPU 60 s) | same | **fresh limits per `execute()`**, scope default 200, max 2,000 | – |
| Records | few | medium | up to **50 million** (QueryLocator) | – |
| State | none | member variables | `Database.Stateful` keeps instance variables across chunks (otherwise reset every `execute`) | none |
| Special | cannot be called from VF getters/setters/constructors | `System.attachFinalizer(...)` runs after job, success or fail | Status **Holding** = flex queue (up to 100 jobs); 5 running at a time | cron `sec min hour dayOfMonth month dayOfWeek [year]`; unique job names |
| In tests | runs at `stopTest` | runs at `stopTest`; **chaining not executed** (`Maximum stack depth` if chained in tests) | **one `execute()` chunk** | runs at `stopTest` |

Rules to remember: async work runs **after commit**; a failed async job does **not** roll back the original transaction; each async job is its own transaction with fresh limits; one `execute()` failing fails only that chunk.

### Experiments: async

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


## Assignments: Module 6 (also do the "Try it" async table above)

#### 6.1 Queueable chain with a Finalizer
```apex
public class Step1 implements Queueable {
    public void execute(QueueableContext c) { System.debug('Step1'); System.enqueueJob(new Step2()); }
}
public class Step2 implements Queueable {
    public void execute(QueueableContext c) {
        System.attachFinalizer(new Fin());
        System.debug('Step2');
    }
}
public class Fin implements Finalizer {
    public void execute(FinalizerContext ctx) { System.debug('Finalizer: ' + ctx.getResult()); }
}
// anonymous
System.enqueueJob(new Step1());
```
Open **Developer Console → Logs** (async jobs create their own logs). **Expect:** `Step1`, `Step2`, then `Finalizer: SUCCESS`. **Change:** `throw new MathException();` in `Step2.execute` → finalizer reports `UNHANDLED_EXCEPTION`.

#### 6.2 Batch + Stateful + chaining
Use the `LabBatch` from the async "Try it" above. Run `Database.executeBatch(new LabBatch(), 50);` with and without `Database.Stateful`, then check **Setup → Apex Jobs** and the log of `finish()`.

---

---

# MODULE 7: Flow and automation

## Reference

| Item | Rule |
|---|---|
| **Before-save** record-triggered | Update the **triggering record** with no DML; cannot create or delete other records; fastest |
| **After-save** record-triggered | Create/update other records, emails, actions, Apex actions; the triggering record's fields need an Update Records element |
| **Run when** | Created / updated / created or updated / deleted. Option **"Only when a record is updated to meet the condition requirements"** stops repeat runs |
| **Scheduled path / async path** | Run later or in a separate transaction (callouts need async) |
| **Scheduled-triggered flow** | Runs on a schedule on matching records |
| **Screen flow** | User interaction; can use LWCs; can't run without a user |
| **Autolaunched flow** | No trigger; called by Apex/other flows/processes; supports subflows |
| **Platform event-triggered flow** | Reacts to events |
| **Bulkification** | Record-triggered flows run per batch (up to 200 records) and are bulkified automatically **if you keep DML/queries out of loops** |
| **Limits** | Same governor limits as Apex in the same transaction (SOQL 100, DML 150); **flows and triggers share them** |
| **Fault connector** | Handles element errors; without one, the flow fails and rolls back the transaction |
| **Invocable Apex** | `@InvocableMethod` static, **one per class**, `List` parameter, bulk-safe, wrapper class with `@InvocableVariable` for multi-field inputs |
| **Versions** | One active version at a time; older versions kept |
| **Retired** | Workflow rules and Process Builder are retired/end of support; use Flow |
| **Run mode** | Record-triggered flows run in system context by default (flow screens run in user context) |


## Assignments: Module 7

#### 7.1 Before-save vs after-save, fault path, invocable action
**Do**
1. Flow A (before-save, Account created): set `{!$Record.Description}` = `A ran`. Note that no Update element is needed.
2. Flow B (after-save, Account created): **Create Records** → Task, Subject `Lab follow up`, WhatId `{!$Record.Id}`.
3. Run `insert new Account(Name='Flow Lab');` then check the Description and the Task.
4. Add an **Update Records** element in Flow B that updates a required field wrongly (to force an error). First without a fault connector (see the whole transaction fail), then add a **fault connector** to an **Action → Send Email** (or a Create Records of a Task) and run again.
5. Create an invocable action:
```apex
public class LabInvocable {
    @InvocableMethod(label='Lab hello')
    public static List<String> hello(List<Id> ids) {
        List<String> out = new List<String>();
        for (Id i : ids) out.add('Seen ' + i);
        return out;
    }
}
```
Call it from Flow B (Action → Apex action) and store the output in a variable, then use it in the Task description.
6. Bulk test: insert 200 accounts in one anonymous script and confirm no limit errors.
**Expect:** before-save sets Description with no extra DML; after-save creates the Task; without a fault connector a failed element rolls back the whole transaction.
**Lesson:** before-save = same record fast; after-save = related records and actions; fault connectors keep flows from failing silently; invocable methods are bulk (List).

---

---

# MODULE 8: Integration (callouts, REST, events)

## Reference

| Item | Rule |
|---|---|
| Callouts | 100 per transaction; each up to 120 s; **cumulative 120 s per transaction**; 6 MB (sync) / 12 MB (async) request/response |
| Trigger callouts | **Not synchronous.** Use `@future(callout=true)`/Queueable |
| DML before callout | `You have uncommitted work pending...` Callout first or separate transactions |
| Authorization | **Named Credential** (stores auth + endpoint, `callout:Name/path`) or **Remote Site Setting** (URL only) |
| Classes | `Http`, `HttpRequest` (`setMethod`, `setEndpoint`, `setHeader`, `setBody`, `setTimeout(ms)`), `HttpResponse` (`getStatusCode`, `getBody`) |
| JSON | `JSON.serialize`, `JSON.deserialize(str, Type)`, `JSON.deserializeUntyped` |
| Expose REST | `@RestResource(urlMapping='/Name/*')` + `@HttpGet/@HttpPost/@HttpPut/@HttpPatch/@HttpDelete` |
| SOAP | Consume via **WSDL2Apex**; expose with `webservice` keyword on a `global` class |
| Platform events | API name ends `__e`; publish with `EventBus.publish()` (or DML insert); subscribe via Apex trigger (`after insert`), Flow, LWC; **not queryable**; publish behavior can be "publish immediately" or "after commit" |
| Change Data Capture | Change events for record changes; subscribe like platform events |
| Email | `Messaging.sendEmail`: 10 invocations per transaction; limits per day; **no real email in tests** (`Limits.getEmailInvocations()`) |
| Mixed concerns | Callouts can't run in Queueable/Batch unless `Database.AllowsCallouts`/`callout=true` |

### Experiments: callouts

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


## Assignments: Module 8 (also do the callout "Try it" table above)

#### 8.1 Platform event publish and subscribe
**Do:** in Setup → **Platform Events** create `Lab_Event__e` with a Text field `Msg__c`. Create:
```apex
trigger LabEventTrg on Lab_Event__e (after insert) {
    for (Lab_Event__e e : Trigger.new) System.debug('Got ' + e.Msg__c);
}
```
Run `EventBus.publish(new Lab_Event__e(Msg__c = 'hi'));`.
**Expect:** the trigger runs asynchronously as the **Automated Process** user, so to see its log create a **trace flag for Automated Process** in Debug Logs. **Change:** try `SELECT Id FROM Lab_Event__e` (not queryable like normal objects).
**Lesson:** platform events are published to a bus, subscribers run in their own transaction.

#### 8.2 Apex REST resource
```apex
@RestResource(urlMapping='/LabAcct/*')
global with sharing class LabAcct {
    @HttpGet
    global static Account doGet() {
        String id = RestContext.request.requestURI.substringAfterLast('/');
        return [SELECT Id, Name FROM Account WHERE Id = :id];
    }
}
```
Call it from a REST client (Workbench REST Explorer, or Postman) at `/services/apexrest/LabAcct/<AccountId>`. **Change:** add a second `@HttpGet` method (error: only one per verb per class).

---

---

# MODULE 9: Testing

## Reference

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


## Assignments: Module 9 (Testing)

#### 9.1 Data factory, bulk, mock, async (uses `LabCallout` and `AccTrg` from earlier modules)
```apex
@isTest
public class TestDataFactory {
    public static List<Account> accounts(Integer n) {
        List<Account> l = new List<Account>();
        for (Integer i = 0; i < n; i++) l.add(new Account(Name = 'TDF ' + i));
        return l;
    }
}
```
```apex
@isTest
private class LabTests {
    @testSetup static void setup() { insert TestDataFactory.accounts(1); }

    @isTest static void bulk() {
        Test.startTest();
        insert TestDataFactory.accounts(200);
        Test.stopTest();
        Assert.areEqual(201, [SELECT COUNT() FROM Account WHERE Name LIKE 'TDF %'], 'count');
    }

    public class Mock implements HttpCalloutMock {
        public HttpResponse respond(HttpRequest r) {
            HttpResponse x = new HttpResponse(); x.setStatusCode(200); x.setBody('mocked'); return x;
        }
    }
    @isTest static void callout() {
        Test.setMock(HttpCalloutMock.class, new Mock());
        Test.startTest();
        String s = LabCallout.zen();
        Test.stopTest();
        Assert.areEqual('mocked', s);
    }
}
```
**Run** (Developer Console → Test → New Run), then open Overall Code Coverage.
**Change and observe**
| Change | Expect |
|---|---|
| Delete the `Test.setMock` line | `Methods defined as TestMethod do not support Web service callouts` |
| Add `@isTest(SeeAllData=true)` to the class | `@testSetup` not supported in that class (error) |
| Add a second `@testSetup` method | Error: only one allowed |
| Call `Test.startTest()` twice | Error: startTest can only be called once |
| Move the `Assert` before `Test.stopTest()` when testing a Queueable | Fails: job hasn't run yet |
| Add `System.runAs(u)` around an `insert new User(...)` and an `insert new Account(...)` outside it | Shows the mixed-DML fix pattern |
**Lesson:** `@testSetup` runs once and each method gets a rolled-back copy; async runs at `stopTest`; every callout needs a mock.

---

---

# MODULE 10: Lightning Web Components (+ Lightning pages)

## Reference

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


### Lightning pages and actions (App Builder)
| Item | Rule |
|---|---|
| Lightning page types | **App**, **Home**, **Record** (also Email app, Utility bar) |
| Activation | Org default, app default, or **app + record type + profile** assignment |
| Component visibility filters | Show a component by record field, user permission, device, or other conditions |
| **Dynamic Forms** | Place individual fields on a record page and show/hide them by rules; no page layout needed |
| **Quick actions** | Create/Update/Log a Call, **LWC action** (`lightning__RecordAction`), Aura (`force:lightningQuickAction`), Visualforce, Flow |
| Utility bar | Persistent components at the bottom of an app (`lightning__UtilityBar`) |
| Experience Cloud | LWC needs `lightningCommunity__Page` / `lightningCommunity__Default` targets |

## Assignments: Module 10 (needs VS Code + Salesforce Extension Pack; also do "Try it: LWC" above)

#### 10.1 Parent/child LWC with `@wire`, imperative Apex, events, `@api` method
**Apex**
```apex
public with sharing class LabAccountController {
    @AuraEnabled(cacheable=true)
    public static List<Account> getAccounts(String searchKey) {
        String k = '%' + (searchKey == null ? '' : searchKey) + '%';
        return [SELECT Id, Name, Industry FROM Account WHERE Name LIKE :k ORDER BY Name LIMIT 20];
    }
    @AuraEnabled
    public static Account addAccount(String name) { Account a = new Account(Name = name); insert a; return a; }
}
```
**Child `labAccountRow`**
```html
<template><lightning-button label={account.Name} onclick={handleClick}></lightning-button></template>
```
```js
import { LightningElement, api } from 'lwc';
export default class LabAccountRow extends LightningElement {
    @api account;
    handleClick() { this.dispatchEvent(new CustomEvent('selectitem', { detail: { recordId: this.account.Id } })); }
    @api highlight() { this.template.querySelector('lightning-button').variant = 'brand'; }
}
```
**Parent `labAccountList`**
```html
<template>
  <lightning-card title="Lab accounts">
    <div class="slds-p-around_small">
      <lightning-input label="Search" value={searchKey} onchange={handleSearch}></lightning-input>
      <template for:each={accounts} for:item="acc">
        <c-lab-account-row key={acc.Id} account={acc} onselectitem={handleSelect}></c-lab-account-row>
      </template>
      <p>Selected Id: {selectedId}</p>
      <lightning-button label="Add (imperative Apex)" onclick={handleAdd}></lightning-button>
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
    searchKey = ''; accounts = []; selectedId; wiredResult;
    @wire(getAccounts, { searchKey: '$searchKey' })
    wired(result) { this.wiredResult = result; if (result.data) this.accounts = result.data; }
    handleSearch(e) { this.searchKey = e.target.value; }
    handleSelect(e) { this.selectedId = e.detail.recordId; }
    async handleAdd() { await addAccount({ name: 'LWC Lab ' + Date.now() }); await refreshApex(this.wiredResult); }
}
```
**Meta XML (parent):** `isExposed` true; targets `lightning__AppPage` and `lightning__HomePage`. Deploy, then drop the component on an App Page in App Builder.
**Break it, one change at a time** (use the "Try it: LWC" table above for expected errors): remove `cacheable=true`; change the event to `new CustomEvent('selectItem')`; remove `key`; add `this.items.push` on an `@api` array; add `@api recordId` and place the component on an Account record page.
**Lesson:** events go up, `@api` goes down, `@wire` needs cacheable, imperative Apex for DML plus `refreshApex`.

---

---

# MODULE 11: Aura

## Reference

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


## Assignments: Module 11 (Aura, short)

#### 11.1 Bound vs unbound expressions
Create an Aura component `LabAura` in the Developer Console (File → New → Lightning Component):
```xml
<aura:component implements="flexipage:availableForAllPageTypes">
    <aura:attribute name="msg" type="String" default="hello"/>
    <lightning:input label="Type here" value="{!v.msg}"/>
    <p>Bound: {!v.msg}</p>
    <p>Unbound: {#v.msg}</p>
</aura:component>
```
Add it to a Home page in App Builder and type in the input. **Expect:** "Bound" updates as you type, "Unbound" stays `hello`. **Lesson:** `{!}` is bound (live), `{#}` is unbound (one-time).

---

---

# MODULE 12: Visualforce

## Reference

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


## Assignments: Module 12 (Visualforce)

#### 12.1 List controller, extension, partial refresh, view state
```apex
public with sharing class LabVfExt {
    private ApexPages.StandardSetController ssc;
    public String hello { get; private set; }
    public List<String> big;            // later change to: public transient List<String> big;
    public LabVfExt(ApexPages.StandardSetController c) {
        ssc = c; ssc.setPageSize(5); hello = 'Hi from extension';
        big = new List<String>();
        for (Integer i = 0; i < 3000; i++) big.add('row ' + i + ' lorem ipsum dolor sit amet');
    }
    public PageReference addMsg() {
        ApexPages.addMessage(new ApexPages.Message(ApexPages.Severity.INFO, 'Added from controller'));
        return null;
    }
}
```
```html
<apex:page standardController="Account" recordSetVar="accts" extensions="LabVfExt">
  <apex:form>
    <apex:pageMessages id="msgs"/>
    <apex:outputPanel id="tbl">
      <apex:pageBlock title="Accounts, page {!pageNumber}">
        <apex:pageBlockTable value="{!accts}" var="a">
          <apex:column value="{!a.Name}"/><apex:column value="{!a.Industry}"/>
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
Open `/apex/<PageName>`. Turn on **Development Mode** and **Show View State in Development Mode** (My Settings → Advanced User Details), refresh and read the View State tab. **Change one at a time**
| Change | Expect |
|---|---|
| Make `big` `transient` | View state shrinks |
| Remove `<apex:form>` | Error: commandButton must be in a form |
| Constructor takes `ApexPages.StandardController` | No matching constructor: list controllers need `StandardSetController` |
| Put `rendered="false"` on the `tbl` panel and rerender it | Nothing updates (target not in DOM) |
**Lesson:** list controller extension = `StandardSetController`; `rerender` needs a rendered target; `transient` cuts view state.

---

---

# MODULE 13: Environments, packages, deployment

## Reference

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


## Assignments: Module 13 (environments and deployment)

#### 13.1 Scratch org with the CLI (needs Dev Hub enabled in an org you can log into)
```bash
sf project generate --name pd1lab
cd pd1lab
sf org login web --set-default-dev-hub
# edit config/project-scratch-def.json to: {"orgName":"PD1 Lab","edition":"Developer"}
sf org create scratch --definition-file config/project-scratch-def.json --alias lab --duration-days 3 --set-default
sf project deploy start --source-dir force-app
sf apex run test --code-coverage --result-format human --wait 10
sf org list
sf org open
```
**Expect:** a scratch org with an expiry date shows in `sf org list`. **Change:** add `LabAcct.cls` to `force-app`, add its path to `.forceignore`, deploy, and confirm it is skipped.

#### 13.2 Validate and Quick Deploy (change set or CLI)
**Do:** in a sandbox, build an **outbound change set** with a trigger, handler and test classes; upload to a connected org. In the target org open **Inbound Change Sets**, click **Validate** (don't deploy to production), then open **Deployment Status** to read the tests and coverage. After a successful validation note the **Quick Deploy** option.
**CLI equivalent:** `sf project deploy start --dry-run` validates without saving.
**Lesson:** validation runs tests without changing the org; Quick Deploy reuses it for about 10 days; change sets need a deployment connection and can't delete.

#### 13.3 package.xml and destructive changes
**Do:** write a manifest and retrieve:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Package xmlns="http://soap.sforce.com/2006/04/metadata">
  <types><members>*</members><name>ApexClass</name></types>
  <version>62.0</version>
</Package>
```
`sf project retrieve start --manifest package.xml`. To delete a class, add `destructiveChanges.xml` listing it and deploy with the Metadata API/CLI (change sets can't delete).

---

---

# MODULE 14: Debugging

## Reference

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


## Assignments: Module 14 (debugging)

#### 14.1 Trace flag, log levels, limits summary
**Do**
1. Setup → **Debug Logs** → New trace flag for your user. Create a debug level with Apex Code and Database at **FINEST**.
2. Run Module 5.1's script with **Open Log** ticked in Execute Anonymous.
3. In the log, find `CODE_UNIT_STARTED`, `SOQL_EXECUTE_BEGIN`, `DML_BEGIN`, `USER_DEBUG`, and the **LIMIT_USAGE_FOR_NS** summary at the end.
4. Drop Apex Code to **ERROR**, rerun, and compare the log size.
5. Developer Console → **Query Editor**, **Checkpoints** (heap), and **Help → Preferences → Enable Query Plan** to view a query's index use.
6. In VS Code: **SFDX: Turn On Apex Debug Log for Replay Debugger**, run something, then launch the **Apex Replay Debugger** with a breakpoint.
**Lesson:** the trace flag decides who is logged and at what level; the limits summary shows how close you got.

---

---


# MODULE 15: Exam traps, decision tables, final recall

## 15.1 Pick the tool
| Need | Use |
|---|---|
| Block a save conditionally | **Validation rule** |
| Calculated read-only value | **Formula field** |
| Sum/count children | **Roll-up summary** (master-detail only) |
| Update the triggering record fast | **Before-save record-triggered flow** |
| Create related records / email | **After-save record-triggered flow** |
| Guided user input | **Screen flow** |
| Different picklists/layouts per user group | **Record types + page layout assignments** |
| Deployable configuration records | **Custom metadata type** |
| Per-user/profile values | **Hierarchy custom setting** |
| Translatable text | **Custom label** |
| Millions of records | **Batch Apex** |
| Pass sObjects to async / chain | **Queueable** |
| Unrelated LWCs talk | **Lightning Message Service** |
| Child to parent (LWC) | **CustomEvent** |
| Parent to child (LWC) | **`@api` property/method** |
| Reactive read-only data | **`@wire` + cacheable** |
| DML from LWC | **Imperative Apex** |
| Enforce field access in Apex | **`WITH USER_MODE` / `stripInaccessible`** |
| Enforce record access | **`with sharing`** |
| Text search across objects | **SOSL** |
| Version-controlled modular deploys | **CLI + scratch orgs + unlocked packages** |
| Production-sized performance test | **Full sandbox** |
| Representative subset, UAT | **Partial Copy sandbox** |

## 15.2 How the exam twists questions
1. **Shared limits.** "Each trigger chunk is 200" but limits are shared across the transaction.
2. **`with sharing` ≠ FLS.** Record access only.
3. **Static Boolean recursion guard** breaks chunking; use a Set of Ids.
4. **Formula/long text/encrypted:** can't be written or filtered.
5. **After-trigger records are read-only.** Before-trigger can edit with no DML.
6. **DML before callout** errors; callout first or go async.
7. **Test rules:** `@testSetup` once, data rolled back per method, not with `SeeAllData=true`; `startTest` once; async runs at `stopTest`; batch tests run one chunk; callouts need mocks.
8. **Queueable from async = 1**; `@future` can't be called from future/batch.
9. **Name tricks:** lowercase event names, `on`+name handlers, `__r` vs `__c`, `Contacts` plural child.
10. **Change sets can't delete**, need a deployment connection; `NoTestRun` isn't allowed for production Apex.
11. **Sandbox facts:** Developer = metadata only; templates only for Partial and Full; refresh overwrites; email = System only.
12. **LWC:** no template expressions; `key` required; `@api` data read-only; `connectedCallback` has no DOM.
13. "Best approach" questions: eliminate options that break a limit, a security rule or a syntax rule, then choose the most **declarative and bulk-safe** one.

## 15.3 Last-night checklist
- Re-read Module 0's limits table and Module 15.1.
- Re-do the assignments you got wrong (not new ones).
- Sleep. Don't learn new topics tonight.
- On the day: flag hard ones, first pass in about 60-70 minutes, review flagged ones, never leave a question blank.
