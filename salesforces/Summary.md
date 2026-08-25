# PD1 — Final Revision Cheat Sheet
*Quick-glance version for the day/night before the exam. Pair with the full study summary for depth.*

**Format:** 60 questions, 105–110 min, ~68% to pass (you can miss ~19). Single-best-answer, not multiple-select. 30–40% of questions include actual Apex code snippets you must read and reason about.

---

## 1. Developer Fundamentals (27%) — at a glance
- Multi-tenancy → why governor limits exist.
- MVC: Model = objects/fields, View = Lightning/Visualforce, Controller = Apex/standard controller.
- Declarative vs. programmatic: default to declarative (Flow, validation rules, formulas) unless requirement can't be met that way.
- Roll-up summary fields → **Master-Detail only**. Lookup relationships can't do roll-ups natively.
- External IDs → used for `upsert` against external systems; enable "External ID" checkbox on a custom field.
- Agentforce for Developers → new topic; know it's AI-assisted dev tooling, know its limitations exist (it's a tool, not a replacement for testing/review).

**Remember for questions:**
- Master-Detail: child inherits sharing/ownership from parent, supports roll-ups, cascade delete. Lookup: independent ownership, no roll-ups, no cascade delete by default.
- Many-to-many = junction object with two Master-Detail relationships.

---

## 2. Process Automation and Logic (28%) — at a glance
**Declarative:** Flow (Screen, Record-Triggered, Autolaunched, Scheduled) is the current tool — Process Builder/Workflow Rules are legacy.

**Apex basics:** primitive types, collections (List/Set/Map), control flow (if/for/while/switch), access modifiers, `with sharing`/`without sharing`/`inherited sharing`.

**SOQL/SOSL/DML:**
- SOQL = one object + relationships (parent-to-child subquery, child-to-parent dot notation).
- SOSL = multi-object full-text search: `FIND 'text' IN ALL FIELDS RETURNING Account, Contact`.
- DML = insert/update/upsert/delete/undelete/merge. `Database.insert(list, false)` = allow partial success.

**Governor limits — memorize these numbers:**
| Limit | Value |
|---|---|
| SOQL queries (sync) | 100 |
| DML statements | 150 |
| Records returned by SOQL | 50,000 |
| Records per DML statement | 10,000 |
| CPU time (sync) | 10 sec |

**Triggers:**
- Events: `before/after insert, update, delete, undelete`.
- Context vars: `Trigger.new`, `.old`, `.newMap`, `.oldMap`, `.isBefore/isAfter/isInsert/isUpdate/isDelete/isUndelete`.
- **Save order of execution** (know this sequence — commonly tested):
  1. System validation (required fields, field formats)
  2. `before` triggers
  3. Custom validation rules
  4. Duplicate rules
  5. Save to database (not committed)
  6. `after` triggers
  7. Assignment rules
  8. Auto-response rules
  9. Workflow rules (+ field updates re-run before/after triggers once)
  10. Escalation rules, Process Builder/Flow, entitlement rules, roll-up summary/cross-object workflow updates
  11. Commit

**Remember for questions:**
- **SOQL/DML in a for-loop = wrong answer, every time.** Fix pattern: collect IDs in a Set/List → single SOQL with `WHERE Id IN :idSet` → build a `Map<Id,SObject>` → do DML once, outside the loop, in bulk.
- Assume every trigger may run on up to 200 records — no single-record assumptions.
- One trigger per object + handler class pattern.
- Async Apex — match the tool to the constraint:
  - **@future** — simple fire-and-forget, needed for callouts from a trigger, no return value, no chaining, primitive params only.
  - **Queueable** — like future but supports complex/sObject params and can be **chained**.
  - **Batch (`Database.Batchable`)** — large data volumes, records processed in chunks (start/execute/finish).
  - **Scheduled (`Schedulable`)** — time/CRON-based recurring jobs.

---

## 3. User Interface (25%) — at a glance
**Visualforce:**
- Standard Controller = auto CRUD for one object.
- Standard Set Controller = list views/pagination.
- Custom Controller = your own Apex class, full control.
- Controller Extension = adds to standard/custom controller without replacing it (`extensions="X"`, constructor takes the controller).
- View state, `<apex:actionFunction>`/`<apex:actionSupport>` for AJAX-like behavior.
- Only Visualforce can natively render as **PDF**.

**Lightning:**
- LWC = modern standard (HTML+JS, `@api`, `@track`, `@wire`). Aura = older framework, still exists.
- `@AuraEnabled(cacheable=true)` to expose Apex methods to LWC/Aura.
- Components can be surfaced via Lightning App Builder, Quick Actions, Flow screens, Experience Cloud.

**Security (explicitly named exam topic now):**
- Enforce CRUD/FLS in Apex (`WITH SECURITY_ENFORCED` in SOQL, `Security.stripInaccessible()`, or manual `Schema.sObjectType` checks).
- Avoid SOQL injection: use bind variables (`:variable`), not string concatenation of user input.
- Avoid XSS: escape output, don't render unsanitized user input directly in markup.

**Remember for questions:**
- "Need a PDF output" → Visualforce, not LWC.
- "Need best mobile/modern performance" → LWC.
- A question describing string-concatenated SOQL built from user input → SOQL injection risk, answer = use bind variables.

---

## 4. Testing, Debugging, and Deployment (20%) — at a glance
**Testing:**
- `@isTest` classes, test methods.
- `@testSetup` → shared test data created once per test class run.
- `Test.startTest()` / `Test.stopTest()` → resets governor limits for the enclosed code; forces async jobs (future/batch/queueable/scheduled) to execute synchronously when `stopTest()` is hit.
- **75% minimum code coverage** to deploy to production — org-wide aggregate, but every trigger should have some coverage.
- `System.assert()`/`Assert.*` methods are required — a test that runs code but never asserts an outcome does not meaningfully "test" it, even if it contributes to coverage %.
- Mock callouts with a class implementing `HttpCalloutMock`.
- Avoid `SeeAllData=true` unless required — tests should use their own data, not rely on org data (a test that "passes locally but fails on deployment" is a classic sign of this issue, or of hardcoded IDs).
- Must be able to test **triggers, controllers, classes, and Flows** — not just Apex classes in isolation.

**Debugging:**
- Debug logs, log levels (DEBUG/INFO/WARN/ERROR/FINE).
- Developer Console: Execute Anonymous, checkpoints, log inspection.
- Apex Replay Debugger for offline log analysis.

**Deployment:**
- Change Sets — same production org's sandboxes only, outbound/inbound.
- Salesforce DX / Salesforce CLI / scratch orgs — modern, source-driven, scriptable; this is now its own named exam topic, not just "extra credit."
- All-or-nothing deployment behavior (a failing component can block the whole deployment).

**Remember for questions:**
- "Coverage below 75%" scenario → answer is add test methods, not just re-run.
- "Tests pass locally, fail on deploy" → look for `SeeAllData=true` or hardcoded record IDs/org-specific data.
- "Need to test a Queueable/Batch/Future job synchronously in a test" → wrap the call in `Test.startTest()`/`Test.stopTest()`.

---

## Cross-Cutting Traps (show up across multiple sections)
1. SOQL/DML inside a loop.
2. Confusing Lookup vs Master-Detail behavior (ownership, cascade delete, roll-ups).
3. Save order of execution sequencing.
4. `with sharing` / `without sharing` / `inherited sharing` — which one is the default (a class with no keyword runs in the sharing context of the class that called it, i.e. effectively "inherited" behavior, but explicit keywords override that).
5. Forgetting bulkification — assume 200 records, not 1.
6. Picking the wrong async Apex tool for the described constraint.
7. Assuming code coverage % alone means a test is valid — assertions matter.
8. Security: bind variables vs string concatenation in SOQL; CRUD/FLS enforcement.

---

*Use this as your last-pass skim before the exam; go back to the full study summary for anything that doesn't feel solid yet.*
