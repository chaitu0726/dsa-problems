# Salesforce Governor Limits

## Brief Definition

**Salesforce Governor Limits** are runtime limits imposed by Salesforce to ensure that one transaction or piece of code does not consume excessive shared resources.

They are especially important when working with **Apex, Triggers, Flows, SOQL, DML, and asynchronous processing**.

The key idea is:

> **Governor Limits are mostly applied per transaction.**

If you exceed a governor limit, Salesforce generally throws a runtime exception and the transaction can fail.

### General Solutions When Approaching Limits

- **Bulkify** Apex and Triggers.
- Avoid **SOQL inside loops**.
- Avoid **DML inside loops**.
- Use `List`, `Set`, and `Map` to process records in bulk.
- Reduce unnecessary queries and DML operations.
- Process large volumes using **Batch Apex**.
- Use **Queueable Apex** or other asynchronous processing for expensive work.
- Optimize loops and complex logic to reduce **CPU time**.
- Query only the fields and records that are actually required.
- In Flow, use **collection variables** and minimize Get/Create/Update/Delete elements.

---

## Salesforce Governor Limits Table

| Governor Limit / Scenario | Synchronous Limit | Asynchronous Limit | What Is Acceptable? | If You Are Exceeding It → Solution |
|---|---:|---:|---|---|
| **SOQL Queries** | 100 | 200 | ≤ 100 SOQL queries/transaction | Don't put SOQL inside loops. Query records once and store them in `List`, `Map`, or `Set`. |
| **SOQL Rows Retrieved** | 50,000 | 50,000 | ≤ 50,000 records | Filter queries, query only required records/fields, use Batch Apex for large data volumes. |
| **DML Statements** | 150 | 150 | ≤ 150 DML statements | Don't perform DML inside loops. Collect records in Lists and perform one bulk `insert/update/delete`. |
| **DML Rows** | 10,000 | 10,000 | ≤ 10,000 records | Use Batch Apex, Queueable Apex, or process records in smaller chunks. |
| **CPU Time** | 10,000 ms | 60,000 ms | Stay below transaction limit | Optimize loops/algorithms, reduce SOQL/DML, avoid unnecessary processing, move heavy work asynchronously. |
| **Heap Size** | 6 MB | 12 MB | Stay below heap limit | Query fewer fields, process records in chunks, avoid storing unnecessary data, use Batch Apex. |
| **Callouts** | 100 | 100 | ≤ 100 callouts | Combine requests where possible; use Queueable/Future/Batch Apex for asynchronous callouts. |
| **Callout Timeout** | 120 sec cumulative | 120 sec cumulative | ≤ 120 seconds | Reduce external API response time, combine calls, or redesign integration. |
| **Future Method Invocations** | 50 | N/A | ≤ 50 `@future` calls/transaction | Use Queueable Apex or consolidate work into fewer async jobs. |
| **Queueable Jobs Added** | 50 | 1 | ≤ 50 from synchronous context; async chaining is more restricted | Don't enqueue jobs inside large loops. Chain Queueables where appropriate. |
| **Batch Apex `execute()` Scope** | — | 200 default | Usually ≤ 200 records per batch execution | Reduce scope size if processing is expensive or involves callouts. |
| **SOSL Queries** | 20 | 20 | ≤ 20 SOSL queries | Consolidate searches and avoid SOSL inside loops. |
| **Records in `FOR UPDATE` Queries** | 50,000 | 50,000 | ≤ 50,000 | Query only required records and avoid unnecessary locking. |
| **Email Invocations** | 10 | 10 | ≤ 10 `sendEmail()` calls | Collect emails and send in bulk rather than sending inside loops. |
| **Email Recipients** | 5,000 | 5,000 | ≤ 5,000 recipients | Consolidate emails and use appropriate email automation/platform features. |
| **Flow SOQL / Get Records** | Transaction-dependent | Transaction-dependent | Minimize database queries | Combine Gets, use collection processing, avoid Get Records inside loops. |
| **Flow DML / Create / Update / Delete** | Transaction-dependent | Transaction-dependent | Minimize DML operations | Use collection variables and perform bulk Create/Update/Delete outside loops. |
| **Apex Trigger Records** | 200 | — | Design for up to **200 records per transaction** | Always write bulkified triggers. Never assume a trigger runs for only one record. |
| **Trigger SOQL** | Shares 100 SOQL limit | — | Keep total transaction SOQL ≤ 100 | Query once using `Trigger.new` IDs and Maps. |
| **Trigger DML** | Shares 150 DML limit | — | Keep total transaction DML ≤ 150 | Build Lists and perform bulk DML outside loops. |

---

## Most Important Governor Limits for Interviews

These are the numbers to **memorize first** for Salesforce interviews:

| Limit | Number to Remember | Interview Keyword |
|---|---:|---|
| **SOQL Queries** | **100** | Don't query inside loops |
| **SOQL Queries — Async** | **200** | Async has higher SOQL limit |
| **SOQL Rows** | **50,000** | Filter / Batch |
| **DML Statements** | **150** | Don't DML inside loops |
| **DML Rows** | **10,000** | Batch / Queueable |
| **CPU Time — Sync** | **10 seconds** | Optimize processing |
| **CPU Time — Async** | **60 seconds** | Use async for heavy processing |
| **Heap Size — Sync** | **6 MB** | Reduce data in memory |
| **Heap Size — Async** | **12 MB** | Reduce data / Batch |
| **Callouts** | **100** | Async for callouts |
| **Future Calls** | **50** | Prefer Queueable |
| **Trigger Records** | **200** | Bulkify trigger |
| **SOSL Queries** | **20** | Avoid SOSL inside loops |
| **Email Invocations** | **10** | Bulk email |

---

## How to Think About Governor Limit Questions

### 1. SOQL Limit Exceeded

**Problem:** SOQL query inside a loop.

**Bad Approach**

```apex
for (Account acc : accounts) {
    List<Contact> contacts = [
        SELECT Id, Name
        FROM Contact
        WHERE AccountId = :acc.Id
    ];
}
```

**Solution:** Query all required records once and use a Map.

```apex
Set<Id> accountIds = new Set<Id>();

for (Account acc : accounts) {
    accountIds.add(acc.Id);
}

List<Contact> contacts = [
    SELECT Id, Name, AccountId
    FROM Contact
    WHERE AccountId IN :accountIds
];
```

**Interview Answer:** SOQL should be outside the loop. Use collections such as Set and Map to bulkify the query.

---

### 2. DML Limit Exceeded

**Problem:** DML operation inside a loop.

**Bad Approach**

```apex
for (Account acc : accounts) {
    acc.Description = 'Updated';
    update acc;
}
```

**Solution:** Collect records and perform one bulk DML.

```apex
List<Account> accountsToUpdate = new List<Account>();

for (Account acc : accounts) {
    acc.Description = 'Updated';
    accountsToUpdate.add(acc);
}

update accountsToUpdate;
```

**Interview Answer:** Never perform DML inside a loop. Collect records into a List and perform bulk DML outside the loop.

---

### 3. CPU Time Limit Exceeded

**Problem:** Too much processing in a single transaction.

**Solution**
- Optimize nested loops.
- Remove unnecessary processing.
- Reduce SOQL and DML.
- Avoid repeatedly performing the same calculation.
- Use Maps for fast lookups.
- Move heavy processing to asynchronous Apex.
- Use Batch Apex for very large datasets.

**Interview Answer:** Optimize the algorithm and reduce unnecessary processing. If the work is still CPU-intensive, move it to asynchronous processing.

---

### 4. Heap Size Limit Exceeded

**Problem:** Too much data is being stored in memory.

**Solution**
- Query only required fields.
- Query only required records.
- Don't keep unnecessary objects in collections.
- Process records in smaller chunks.
- Use Batch Apex for large datasets.

**Interview Answer:** Reduce the amount of data held in memory and process large datasets in batches.

---

### 5. DML Rows Limit Exceeded

**Problem:** More than 10,000 records need to be modified.

**Solution:** Use Batch Apex.

```
Batch Apex

1,000,000 records
        ↓
Batch 1 → 200 records
Batch 2 → 200 records
Batch 3 → 200 records
...
```

Each batch execution gets its own transaction and governor-limit allocation.

**Interview Answer:** Don't process more than 10,000 DML rows in a single transaction. Use Batch Apex to divide the work into multiple transactions.

---

### 6. Callout Limit

**Problem:** More than 100 HTTP callouts in one transaction.

**Solution**
- Combine multiple requests if the external API supports bulk requests.
- Use Queueable Apex.
- Use Batch Apex for large datasets.
- Don't make unnecessary callouts.

**Important:** If an Apex Trigger needs to call an external system, don't make the HTTP callout directly from the trigger.

Typical solution:

```
Trigger
   ↓
Queueable Apex
   ↓
HTTP Callout
   ↓
External System
```

---

### 7. Large Data Volume

When you have something like 1 million records, don't try to process everything in one transaction.

Use Batch Apex:

```
1,000,000 records
        ↓
Batch 1 → 200 records
Batch 2 → 200 records
Batch 3 → 200 records
...
```

Each batch execution gets its own transaction and governor-limit allocation.

---

### 8. Flow and Governor Limits

Flows also consume Salesforce platform resources.

**Common Flow Problem**

```
Loop
  ↓
Get Records
  ↓
Update Records
  ↓
Loop
  ↓
Get Records
  ↓
Update Records
```

This can cause excessive database operations.

**Better Design**

```
Get Records
     ↓
Collection
     ↓
Loop
     ↓
Modify Collection
     ↓
Update Records ONCE
```

**Interview Answer:** In Flow, avoid Get/Create/Update/Delete elements inside loops. Use collection variables and perform database operations outside the loop whenever possible.

---

## Easy Memory Trick

```
SOQL       → 100
DML        → 150
SOQL Rows  → 50,000
DML Rows   → 10,000
Trigger    → 200 records
Callout    → 100
CPU        → 10 sec Sync / 60 sec Async
Heap       → 6 MB Sync / 12 MB Async
```

### Most Important Rule

```
              GOVERNOR LIMIT
                    ↓
          ┌─────────┴─────────┐
          ↓                   ↓
       SOQL Limit          DML Limit
          ↓                   ↓
   Query inside loop?    DML inside loop?
          ↓                   ↓
        NO ❌                NO ❌
          ↓                   ↓
   Query outside loop    Bulk DML outside loop
          ↓                   ↓
      List / Set / Map       List
```

---

## Final Interview Rule

When you see a Salesforce governor-limit question, think:

- **SOQL inside loop?** → Move SOQL outside the loop.
- **DML inside loop?** → Collect records and perform bulk DML.
- **Too many records?** → Use Batch Apex.
- **Heavy processing / CPU issue?** → Optimize or use asynchronous Apex.
- **Too much data in memory?** → Reduce queried data or process in batches.
- **External API callout?** → Use appropriate asynchronous processing such as Queueable Apex.
- **Flow doing database operations inside loops?** → Use collections and perform database operations outside the loop.

For Salesforce interviews, don't just memorize the numbers. Be able to identify the anti-pattern and explain how you would bulkify, optimize, or move the processing asynchronously.
