# PD1 Complete Reference: what each thing CAN and CAN'T do

**Purpose:** the real questions are twisted, not hard. They test edge rules ("does this run once or per method?", "can a formula field be written?"). This file lists, per feature, **what it can do, what it can't, how often it runs, and the gotchas**, then gives **experiments** (run → change one line → see the error → fix).

**Honest notes**
- I cannot run Apex or Salesforce here. The expected results below come from documented behavior and well-known error messages. **Run every experiment in a sandbox and trust what you see over what I wrote.** Where I'm less sure, I mark **(verify)**.
- "Every minute detail" is impossible. This covers what the exam tends to twist. Pair it with `PD1_Numbers_Cheatsheet.md` (limits) and `PD1_Master_Cheatsheet.md` (comparisons).
- Part 2 (experiments) starts at the heading **PART 2**.

---

# PART 1: REFERENCE

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

## 5. Apex language details

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
| `@testSetup` | See Section 14 | |
| `@TestVisible` | Expose private members to tests | – |
| `@future` | Static void, primitive/collection-of-primitive params, `callout=true` | Return values, take sObjects, chain, run from another future/batch |
| `@InvocableMethod` | Be used in Flow; one per class; static; **List** parameter | Have more than one per class |
| `@InvocableVariable` | Group inputs in a wrapper class | – |
| `@AuraEnabled` | Expose to LWC/Aura; `cacheable=true` for `@wire` | Perform DML when `cacheable=true` |
| `@RemoteAction` | Static method callable from Visualforce JavaScript | Be non-static |
| `@RestResource`, `@HttpGet/@HttpPost…` | Expose Apex REST | – |
| `@ReadOnly` | Raise query limits for read-only reports/remote actions | Do DML |
| `@Deprecated` | Mark for managed packages | – |

## 6. Triggers: can / can't per event

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

## 7. SOQL, SOSL, DML: can / can't

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

## 8. Async Apex: can / can't / how many

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

## 9. Flow: can / can't

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

## 10. Integration: can / can't

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
