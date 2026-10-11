# Platform Developer I – Numbers Cheat Sheet

> Written from my knowledge of the standard Salesforce limits. I couldn't load the full limits table from the official page, so skim the official **Apex Developer Guide → Execution Governors and Limits** once before the exam. Limits can change between releases.

---

## 1. Per-transaction Apex governor limits (most tested)

| Limit | Synchronous | Asynchronous (future, queueable, batch, scheduled) |
|---|---|---|
| SOQL queries issued | **100** | **200** |
| Records retrieved by SOQL | **50,000** | **50,000** |
| Records via `Database.getQueryLocator` | 10,000 (sync) | up to **50 million** in a batch `start()` |
| SOSL queries issued | **20** | **20** |
| Records returned by one SOSL query | **2,000** | **2,000** |
| DML statements | **150** | **150** |
| Records processed by DML | **10,000** | **10,000** |
| Callouts (HTTP / web service) | **100** | **100** |
| Callout timeout (default / max) | 10 s / 120 s | 10 s / 120 s |
| Callout request/response size | 6 MB | 12 MB |
| CPU time | **10,000 ms** | **60,000 ms** |
| Heap size | **6 MB** | **12 MB** |
| Total execution time | 10 minutes | 10 minutes |
| `@future` calls per invocation | **50** | 0 from future/batch, 50 from queueable |
| Queueable jobs enqueued | **50** | **1** |
| `Messaging.sendEmail` invocations | 10 | 10 |
| Trigger recursion/stack depth | 16 | 16 |

**Remember:** Limits are **per transaction**, and in Batch Apex they reset for every `execute()` chunk. `Test.startTest()` gives the code between start/stop a fresh set.

---

## 2. Triggers

| Item | Number |
|---|---|
| Records per trigger chunk (`Trigger.new` size) | **200** |
| Bulk insert of 500 records | 3 trigger invocations (200 + 200 + 100) |
| Recursive trigger depth | 16 |
| Code coverage to deploy | **75%** overall (each trigger needs some coverage) |
| Max size of a class or trigger | 1,000,000 characters |

---

## 3. Async Apex

| Item | Number |
|---|---|
| Batch default scope | **200** |
| Batch max scope | **2,000** |
| Batch concurrently running jobs | **5** |
| Batch jobs held in the flex queue | **100** |
| Records a batch `QueryLocator` can cover | **50 million** |
| Scheduled Apex jobs at one time | **100** |
| Async Apex executions per 24 h | 250,000 or licenses × 200 (whichever is greater) |
| Queueable chain depth (production) | no fixed limit (5 in Developer/Trial orgs) |
| Executions of a batch in a test method | 1 chunk only |

---

## 4. SOQL / SOSL / DML

| Item | Number |
|---|---|
| Child-to-parent relationship traversal | up to **5 levels** (`Contact.Account.Owner.Manager.Name`) |
| `OFFSET` maximum | **2,000** |
| SOQL for loop chunk size | **200** records at a time |
| Max SOQL statement length | 100,000 characters |
| Records returned if no `LIMIT` | up to 50,000 total per transaction |
| `merge` supported types | Leads, Contacts, Accounts (up to **3** records merged at once) |
| Recycle Bin retention | **15 days** |
| Data Import Wizard | **50,000** records |
| Data Loader | up to **5 million** records |

---

## 5. Data model

| Item | Number |
|---|---|
| Master-detail relationships per object | **2** |
| Total relationship fields (lookup + M-D) per object | 40 |
| Roll-up summary fields per object | 25 (default) |
| External ID fields per object | **25** |
| 15-char vs 18-char Id | 15 = case-sensitive, 18 = case-insensitive |
| Cascade delete | Master-detail deletes children; recycle-bin restore brings them back |

### Field size reference

| Field type | Max |
|---|---|
| Text | 255 characters |
| Text Area | 255 characters |
| Long Text Area | 131,072 characters (default 32,768) |
| Rich Text Area | 131,072 characters |
| Email | 80 characters |
| Phone | 40 characters |
| URL | 255 characters |
| Number / Currency precision | 18 digits total |
| Formula (text result) | 3,900 characters |
| Formula (compiled size) | 5,000 characters |

### Apex number types

| Type | Range |
|---|---|
| Integer | -2,147,483,648 to 2,147,483,647 (32-bit) |
| Long | -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807 (64-bit) |
| Double | 64-bit floating point |
| Decimal | arbitrary precision; use for currency |

---

## 6. UI limits

| Item | Number |
|---|---|
| Visualforce view state | **170 KB** |
| Static resource size | 5 MB per file (250 MB total per org) |
| Flow executed elements per interview | 2,000 |
| Flow record-triggered batch | up to 200 records per interview run |

---

## 7. Testing and deployment

| Item | Number |
|---|---|
| Minimum overall coverage | **75%** |
| Every test that runs must | pass |
| Quick Deploy window after validation | **10 days** |
| Metadata API deployment size | 39 MB (zip), 10,000 files |
| Scratch org lifetime | 1–30 days (default **7**) |
| Debug log max size | 20 MB per log |

### Sandbox facts

| Sandbox | Refresh interval | Data |
|---|---|---|
| Developer | 1 day | Metadata only, 200 MB data storage |
| Developer Pro | 1 day | Metadata only, 1 GB data storage |
| Partial Copy | 5 days | Sample data, 5 GB |
| Full | **29 days** | All data and metadata |

---

## 8. Exam facts (confirm on the official exam guide)

| Item | Detail |
|---|---|
| Scored questions | 60 |
| Unscored pilot questions | up to 5 |
| Time | 105 minutes |
| Passing score | 68% (41 of 60) |
| Domain weights | Fundamentals 23% · Process Automation & Logic 30% · User Interface 25% · Testing, Debugging & Deployment 22% |

---

## 9. Memory hooks

- **100 / 200** → SOQL queries (sync / async)
- **150** → DML statements, **10,000** → DML rows
- **50,000** → SOQL rows, **2,000** → SOSL rows
- **6 MB / 12 MB** → heap (sync / async)
- **10 s / 60 s** → CPU (sync / async)
- **100** → callouts
- **200** → trigger chunk, batch default scope, SOQL for loop chunk
- **2** → master-detail per object
- **75%** → coverage

## 10. Quick scenarios

| Scenario | Result |
|---|---|
| 200 records inserted, trigger runs SOQL in a loop | 200 queries → hits the **100** limit (sync) |
| Loop does `insert` per record for 151 records | `LimitException` at the 151st DML statement (uncatchable) |
| Query returns 60,000 rows | Fails at **50,000** rows |
| Batch of 1,000,000 records, scope 200 | 5,000 `execute()` calls, limits reset each time |
| DML then callout in same transaction | "Uncommitted work pending" error |
| Callout, then DML | Works |
| `@future` called from a batch `execute()` | Not allowed |
| 6 MB of data held in a collection (sync) | Heap limit reached |
