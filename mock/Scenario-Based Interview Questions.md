
## Real-world problem solving · Production incidents · Architecture decisions · Team situations

> **What interviewers measure with scenario questions:**
> - Do you think in systems, not just code?
> - Do you handle ambiguity well?
> - Do you consider failure cases before they happen?
> - Do you communicate decisions clearly with tradeoffs?
> - Do you learn from production incidents?

---

# PART 1 — PRODUCTION INCIDENT SCENARIOS

---

## S1 🔴 Your Spring Boot service suddenly starts throwing OutOfMemoryError in production. Walk me through your diagnosis and fix.

**Answer:**

**Immediate response (first 5 minutes):**

Do NOT restart blindly first. Take a heap dump before restarting — once the JVM restarts, the evidence is gone.

```bash
# Get the PID of the running Java process
jps -l
# or
ps aux | grep java

# Take a heap dump BEFORE restarting (while OOM is happening)
jmap -dump:format=b,file=/tmp/heapdump.hprof <PID>

# Or if the JVM is already crashed, configure JVM flags to auto-dump on OOM:
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/dumps/heapdump.hprof

# Check current heap usage without dump
jmap -heap <PID>

# Quick thread dump to check for thread leak
jstack <PID> > /tmp/threaddump.txt
```

**Now restart to restore service, then investigate:**

```bash
# Step 1: Check GC logs for patterns
# Add these JVM flags if not already present:
-Xlog:gc*:file=/var/log/gc.log:time,uptime:filecount=5,filesize=20m

# Look for: GC overhead limit exceeded = GC running >98% of time, reclaiming <2%
# This means heap is full and GC can't free enough space

# Step 2: Analyse heap dump with Eclipse MAT (Memory Analyzer Tool)
# Open heapdump.hprof in MAT
# Run "Leak Suspects" report — MAT identifies the object retaining the most memory

# Common findings:
# - One HashMap holding millions of entries (cache without eviction)
# - ArrayList growing unboundedly (result set not paged)
# - ThreadLocal not cleaned up (leak in thread pools)
# - Static collections accumulating objects
```

**Common OOM causes and their fixes:**

```java
// CAUSE 1: Unbounded in-memory cache
// Symptom: HashMap or List in a static field growing forever
public class RequestCache {
    private static final Map<String, Response> cache = new HashMap<>(); // NEVER evicts!
    
    public void put(String key, Response response) {
        cache.put(key, response); // grows forever — OOM
    }
}

// Fix: Use a cache with eviction policy
private static final Map<String, Response> cache = Caffeine.newBuilder()
    .maximumSize(10_000)           // cap at 10k entries
    .expireAfterWrite(15, MINUTES) // TTL
    .build()
    .asMap();

// CAUSE 2: Loading entire result set into memory
@Query("SELECT * FROM audit_logs")  // returns 50 million rows
List<AuditLog> findAll(); // all loaded into heap at once — OOM

// Fix: Stream or paginate
@QueryHints(@QueryHint(name = HINT_FETCH_SIZE, value = "50"))
Stream<AuditLog> streamAll(); // process row-by-row, close stream when done

// Or paginate:
Page<AuditLog> findAll(Pageable pageable);
// Call: repository.findAll(PageRequest.of(page, 1000))

// CAUSE 3: ThreadLocal leak in thread pools
// Thread pools reuse threads — ThreadLocal values survive request boundaries
public class RequestContext {
    private static final ThreadLocal<UserContext> context = new ThreadLocal<>();
    
    public static void set(UserContext ctx) { context.set(ctx); }
    public static UserContext get() { return context.get(); }
    // Missing: remove()!
}

// Fix: always clean up in a finally block
try {
    RequestContext.set(userContext);
    doWork();
} finally {
    RequestContext.remove(); // CRITICAL — prevents leak in thread pools
}

// CAUSE 4: Metaspace OOM (class loading leak, different from heap OOM)
// java.lang.OutOfMemoryError: Metaspace
// Caused by: dynamic class generation (CGLib proxies, Groovy scripts, JSP recompilation)
// Fix: -XX:MaxMetaspaceSize=512m (add a cap) and investigate what's generating classes

// CAUSE 5: Direct memory OOM (off-heap)
// java.lang.OutOfMemoryError: Direct buffer memory
// Caused by: NIO ByteBuffer.allocateDirect() without releasing
// Netty, gRPC use direct memory — configure: -XX:MaxDirectMemorySize=512m
```

**Monitoring to prevent recurrence:**

```yaml
# Prometheus + Spring Actuator
management:
  metrics:
    export:
      prometheus:
        enabled: true

# Key metrics to alert on:
# jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
# jvm_gc_pause_seconds_sum rate > 0.1 (GC taking >10% of time)
# jvm_memory_used_bytes{area="nonheap"} — watch Metaspace growth
```

---

## S2 🔴 Your API response times suddenly spike from 50ms to 5000ms. No code was deployed. How do you diagnose it?

**Answer:**

"No code deployed" tells you immediately: this is an infrastructure, data, or dependency issue. Not a bug you introduced.

**Step 1: Determine the blast radius (5 min)**

```bash
# Is it all endpoints or specific ones?
# Check APM (Datadog/New Relic/Grafana) — filter by endpoint
# If all endpoints: problem is in shared infrastructure (DB, network, JVM)
# If specific endpoint: problem is in its dependency (one downstream service, one DB query)

# Check if it correlates with:
# - Traffic spike? → scale issue
# - Time of day? → scheduled job competing for DB
# - External dependency status pages? → third-party issue
```

**Step 2: Check the obvious suspects**

```bash
# Database
# Check slow query log (MySQL/PostgreSQL)
# PostgreSQL:
SELECT query, calls, total_time/calls AS avg_ms, rows
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 20;
# Is one query suddenly taking 4 seconds that used to take 5ms?

# Check for lock contention
SELECT pid, wait_event_type, wait_event, state, query
FROM pg_stat_activity
WHERE wait_event IS NOT NULL;
# Many rows waiting on Lock? → lock contention

# Check DB connection pool
# HikariCP metrics: hikaricp_connections_pending (waiting for a connection)
# If non-zero: pool exhausted — all connections in use, requests queuing

# Check CPU on DB server
top -b -n 1 | head -20
# CPU 100%? Long-running query or missing index

# Check if a new large data volume caused index scan → seq scan
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 123;
# "Seq Scan" on a large table = missing or unused index
```

**Step 3: Check the application**

```bash
# Thread dump — are threads blocked?
jstack <PID> | grep -A 3 "BLOCKED\|WAITING"
# Many threads BLOCKED on same lock? → contention in application code
# Many threads WAITING on DB connection? → pool exhausted (see above)

# GC pressure? (sudden GC spikes cause latency spikes)
jstat -gcutil <PID> 1000 20
# High %GC (>10%)? Memory pressure causing frequent GC pauses

# External service dependency check
# If your service calls PaymentService, InventoryService:
# Check their health endpoints and response times
# Retry storms: if downstream is slow, retries compound the problem
```

**Step 4: Common root causes and fixes**

```java
// ROOT CAUSE 1: Database query plan changed (statistics staleness)
// New data volume changed the query planner's cost estimate → switched from index scan to seq scan
// Fix: update statistics
ANALYZE orders;  -- PostgreSQL
ANALYZE TABLE orders;  -- MySQL

// Or force index use while you investigate:
SELECT /*+ INDEX(orders idx_orders_user_id) */ * FROM orders WHERE user_id = 123;

// ROOT CAUSE 2: Connection pool exhausted
// Requests queuing because all DB connections are in use
// Fix: check for connections not being returned (finally block missing)
try (Connection conn = dataSource.getConnection()) { // try-with-resources ensures return
    // use connection
} // auto-returned to pool

// Increase pool size (short-term fix):
spring.datasource.hikari.maximum-pool-size=50  # was 10

// ROOT CAUSE 3: N+1 query problem manifesting at scale
// 100 users → 101 queries, fine
// 10,000 users → 10,001 queries, takes 5 seconds
// Fix: JOIN FETCH or batch loading (covered in Spring JPA section)

// ROOT CAUSE 4: Third-party service degraded
// Your service has 5s timeout, downstream is taking 4.9s → your p99 = 5s
// Fix: reduce timeout, add circuit breaker, return cached/degraded response
@CircuitBreaker(name = "paymentService", fallbackMethod = "cachedResult")
@TimeLimiter(name = "paymentService") // timeout = 2s, not 30s default

// ROOT CAUSE 5: Disk I/O pressure (log files, temp files)
df -h    # disk full? Java can't write temp files, logs
iostat -x 1 10  # disk utilisation, await time
```

---

## S3 🔴 You deployed a new feature and it's causing a race condition in production. How do you find it and fix it?

**Answer:**

Race conditions are some of the hardest bugs to find because they're non-deterministic. They appear under load and often disappear when you add logging.

**Identifying a race condition:**

```
Symptoms:
- Intermittent data corruption (wrong values, duplicate records)
- Works fine in testing but fails in production (different load, different thread counts)
- Fails more under high load
- Adding sleep() or logging makes it go away (Heisenbug)
- Stack traces show concurrent modification: ConcurrentModificationException

Tools:
- Thread dumps (jstack) to see what threads are doing simultaneously
- Logging with thread names: [pool-1-thread-3] instead of just timestamps
- Java race condition detectors: ThreadSanitizer, FindBugs/SpotBugs with concurrency rules
```

**Common race condition patterns in Spring Boot:**

```java
// PATTERN 1: Non-atomic check-then-act
// Classic: check if username exists, then insert
@Transactional
public User createUser(String username) {
    // Thread A: checks → not found
    // Thread B: checks → not found  (both proceed!)
    if (userRepo.existsByUsername(username)) {
        throw new DuplicateUsernameException();
    }
    // Thread A: inserts
    // Thread B: inserts → duplicate!
    return userRepo.save(new User(username));
}

// Fix 1: Unique constraint in DB (ALWAYS have this as the last line of defense)
@Column(unique = true)
private String username;
// DB enforces uniqueness — second insert gets ConstraintViolationException → catch and handle

// Fix 2: Optimistic locking for check-then-update
@Entity
public class Account {
    @Version  // optimistic lock version field
    private Long version;
    private BigDecimal balance;
}

// If two threads read Account version=1, both try to update:
// Thread A: UPDATE ... WHERE id=1 AND version=1 → success, version becomes 2
// Thread B: UPDATE ... WHERE id=1 AND version=1 → 0 rows updated → OptimisticLockException
// Catch and retry:
@Retryable(value = OptimisticLockingFailureException.class, maxAttempts = 3)
@Transactional
public void updateBalance(Long accountId, BigDecimal amount) {
    Account account = accountRepo.findById(accountId).orElseThrow();
    account.setBalance(account.getBalance().add(amount));
    accountRepo.save(account);
}

// Fix 3: Pessimistic locking (for high-contention scenarios like seat booking)
@Transactional
public Seat reserveSeat(Long seatId, Long userId) {
    Seat seat = seatRepo.findByIdWithLock(seatId); // SELECT ... FOR UPDATE
    if (seat.isReserved()) throw new SeatAlreadyReservedException();
    seat.reserve(userId);
    return seatRepo.save(seat);
}

// In repository:
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.id = :id")
Seat findByIdWithLock(@Param("id") Long id);
// First thread gets lock, second waits, gets it only after first commits

// PATTERN 2: Shared mutable state in a singleton service (stateful bean)
@Service  // Singleton — one instance, many threads
public class OrderProcessor {
    private List<Order> pendingOrders = new ArrayList<>(); // NOT thread-safe!
    
    public void addOrder(Order order) {
        pendingOrders.add(order); // ArrayList.add() is not atomic — concurrent adds can corrupt
    }
}

// Fix: use thread-safe data structure
private List<Order> pendingOrders = new CopyOnWriteArrayList<>(); // thread-safe reads + writes
// or if high write rate:
private Queue<Order> pendingOrders = new ConcurrentLinkedQueue<>();

// PATTERN 3: Double-checked locking without volatile (covered in design patterns)
// PATTERN 4: Lazy initialisation of cached values
@Service
public class ConfigService {
    private Config cachedConfig; // null initially
    
    public Config getConfig() {
        if (cachedConfig == null) {    // Thread A and B both see null
            cachedConfig = loadConfig(); // Both load, wasteful but not data-corrupting
        }                               // For mutable state this IS corrupting
        return cachedConfig;
    }
}

// Fix: use AtomicReference or synchronize
private final AtomicReference<Config> cachedConfig = new AtomicReference<>();

public Config getConfig() {
    return cachedConfig.updateAndGet(existing -> existing != null ? existing : loadConfig());
}
```

---

## S4 🔴 A microservice that was running fine starts getting cascading failures at 3 AM. How do you design to prevent this in future?

**Answer:**

Cascading failure: Service A is slow → Service B calls A, threads pile up waiting → Service B runs out of threads → Service C calls B, same thing → entire system down.

**The root cause chain:**

```
InventoryService slow (DB query degraded at 3AM - nightly backup running)
     ↓
OrderService calls InventoryService - each thread waits 30 seconds
OrderService has 200 threads
     ↓
After 200 concurrent requests, all threads waiting on InventoryService
New requests to OrderService → queue fills → reject
     ↓
PaymentService calls OrderService → same waiting pattern
     ↓
Everything down, even unrelated endpoints (same thread pool)
```

**Prevention layers:**

```java
// LAYER 1: Timeouts — never wait forever
// RestTemplate
HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
factory.setConnectionRequestTimeout(1000); // 1s to get connection from pool
factory.setConnectTimeout(3000);           // 3s to connect
factory.setReadTimeout(5000);              // 5s to get response
// 5 seconds max per call, even if downstream is hanging

// WebClient (reactive)
WebClient.builder()
    .baseUrl(inventoryServiceUrl)
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .responseTimeout(Duration.ofSeconds(5))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
    ))
    .build();

// LAYER 2: Circuit Breaker — stop calling a failing service
resilience4j:
  circuitbreaker:
    instances:
      inventoryService:
        sliding-window-size: 20
        failure-rate-threshold: 50        # open if 50% of last 20 calls fail
        wait-duration-in-open-state: 30s  # don't call for 30s, then try again
        slow-call-duration-threshold: 3s  # calls > 3s count as failures
        slow-call-rate-threshold: 50      # open if 50% are slow

// LAYER 3: Bulkhead — isolate thread pools per dependency
// Without bulkhead: all service calls share one thread pool
// InventoryService slow → all 200 threads busy → OrderService can't serve ANY request

// With bulkhead: InventoryService gets its own thread pool
resilience4j:
  thread-pool-bulkhead:
    instances:
      inventoryService:
        max-thread-pool-size: 20   # at most 20 threads waiting on InventoryService
        core-thread-pool-size: 10
        queue-capacity: 10
  # Even if InventoryService is down, 180 threads still serve other requests

// LAYER 4: Fallback — return degraded but usable response
@CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
public InventoryStatus checkInventory(String productId) {
    return inventoryClient.getStatus(productId);
}

// Fallback options (choose based on business impact):
// Option A: Return cached last-known value
public InventoryStatus inventoryFallback(String productId, Exception ex) {
    return inventoryCache.getOrDefault(productId,
        InventoryStatus.unknown(productId, "Using cached data - inventory service unavailable"));
}
// Option B: Return optimistic "assume in stock" with warning
public InventoryStatus inventoryFallback(String productId, Exception ex) {
    log.warn("Inventory service unavailable for {}, assuming in stock", productId);
    return InventoryStatus.assumeInStock(productId);
}
// Option C: Return "check back later" — order created but not confirmed
public InventoryStatus inventoryFallback(String productId, Exception ex) {
    return InventoryStatus.pendingVerification(productId);
}

// LAYER 5: Retry with backoff — don't hammer a struggling service
@Retry(name = "inventoryService")
resilience4j:
  retry:
    instances:
      inventoryService:
        max-attempts: 3
        wait-duration: 500ms
        exponential-backoff-multiplier: 2
        # 500ms → 1s → 2s between retries
        retry-exceptions:
          - java.net.ConnectException       # network issue, worth retrying
          - java.net.SocketTimeoutException # timeout, maybe transient
        ignore-exceptions:
          - com.example.ProductNotFoundException  # 404 - retrying won't help

// LAYER 6: Async for non-critical operations
// If checking inventory doesn't need to block order creation:
@Async
public CompletableFuture<InventoryStatus> checkInventoryAsync(String productId) {
    return CompletableFuture.supplyAsync(() -> inventoryClient.getStatus(productId));
}

// LAYER 7: Health checks and automated recovery
management:
  health:
    circuitbreakers:
      enabled: true
# /actuator/health shows circuit breaker states
# PagerDuty alert when circuit opens → on-call wakes up at 3AM
```

**Post-incident: what to document:**

```
1. Timeline: when did it start, when detected, when resolved
2. Root cause: nightly backup degraded DB → slow queries → cascading
3. Contributing factors: no timeout on InventoryService calls, no bulkhead
4. Actions taken during incident: restarted backfill job, increased DB resources
5. Prevention:
   - Add read timeout (5s) to all inter-service calls ← immediate
   - Add circuit breakers to InventoryService calls ← this week
   - Move nightly backup to lower-traffic window ← next sprint
   - Add monitoring: alert on GC pause > 2s, DB query > 1s ← this week
6. Detection gap: it took 45 min to detect — need better alerting
```

---

## S5 🔴 You need to migrate a live database table with 500 million rows with zero downtime. How do you approach it?

**Answer:**

Zero downtime migration = cannot take the table offline. Cannot do `ALTER TABLE` and wait 6 hours while the table is locked.

**The strategy: Expand-Contract (Blue-Green Schema)**

```
Phase 1: EXPAND — add new structure alongside old (backward compatible)
Phase 2: MIGRATE — backfill data from old to new
Phase 3: CUTOVER — switch application to use new structure
Phase 4: CONTRACT — remove old structure (after confidence period)
```

**Concrete example: Splitting `users.full_name` into `first_name` + `last_name`**

```sql
-- PHASE 1: EXPAND (deploy this, no downtime, instant)
ALTER TABLE users ADD COLUMN first_name VARCHAR(100);
ALTER TABLE users ADD COLUMN last_name VARCHAR(100);
-- Table has: full_name (existing), first_name (null), last_name (null)
-- Application still reads/writes full_name — no change yet
```

```java
// PHASE 1 app change: dual-write (write to both old and new columns)
@Transactional
public User createUser(String fullName) {
    User user = new User();
    user.setFullName(fullName);           // old column — keep writing
    String[] parts = fullName.split(" ", 2);
    user.setFirstName(parts[0]);          // new columns — start writing
    user.setLastName(parts.length > 1 ? parts[1] : "");
    return userRepo.save(user);
}
// Reading still uses full_name — safe
```

```sql
-- PHASE 2: BACKFILL (500M rows — must be done in batches, not one UPDATE)
-- One giant UPDATE locks the table for hours — DO NOT do this

-- Instead: batch updates with small transactions, with throttling
UPDATE users
SET first_name = SPLIT_PART(full_name, ' ', 1),
    last_name  = SPLIT_PART(full_name, ' ', 2)
WHERE id BETWEEN :start AND :end
  AND first_name IS NULL;  -- only rows not yet migrated
```

```java
// Backfill job — runs in background, production traffic unaffected
@Service
public class UserMigrationJob {

    @Scheduled(fixedDelay = 100) // run every 100ms
    public void migrateNextBatch() {
        List<Long> ids = userRepo.findUnmigratedIds(PageRequest.of(0, 1000)); // 1000 at a time
        if (ids.isEmpty()) {
            log.info("Migration complete");
            return;
        }

        userRepo.migrateNameBatch(ids.get(0), ids.get(ids.size() - 1));

        // Throttle: sleep between batches to avoid overwhelming DB
        Thread.sleep(50); // 50ms pause = at most 20 batches/second
    }
}

// In repository:
@Modifying
@Transactional
@Query(value = """
    UPDATE users
    SET first_name = SPLIT_PART(full_name, ' ', 1),
        last_name = SPLIT_PART(full_name, ' ', 2)
    WHERE id BETWEEN :startId AND :endId
      AND first_name IS NULL
    """, nativeQuery = true)
void migrateNameBatch(@Param("startId") Long startId, @Param("endId") Long endId);
```

```java
// PHASE 3: CUTOVER — switch reads to new columns
// Deploy application change to read first_name/last_name instead of full_name
// Keep writing to full_name too (rollback safety net)
public String getFirstName(User user) {
    // New logic reads from new columns, falls back to old for unmigrated rows
    return user.getFirstName() != null ? user.getFirstName()
        : user.getFullName().split(" ")[0];
}

// Once 100% of rows migrated AND app deployed AND running stable for a week:

-- PHASE 4: CONTRACT — remove old column
ALTER TABLE users DROP COLUMN full_name;
-- Also: remove dual-write code from application
```

**Additional zero-downtime techniques:**

```sql
-- Adding NOT NULL column to large table (naive approach locks table):
ALTER TABLE orders ADD COLUMN version INT NOT NULL DEFAULT 0; -- PostgreSQL acquires lock!

-- Safe approach:
-- Step 1: Add as nullable
ALTER TABLE orders ADD COLUMN version INT;
-- Step 2: Backfill in batches
UPDATE orders SET version = 0 WHERE id BETWEEN :start AND :end AND version IS NULL;
-- Step 3: Add NOT NULL constraint with VALIDATE (in PostgreSQL, validates without lock)
ALTER TABLE orders ALTER COLUMN version SET NOT NULL;
-- Step 4: Add DEFAULT for future inserts
ALTER TABLE orders ALTER COLUMN version SET DEFAULT 0;

-- Adding an index (blocks table in naive approach):
-- Safe: CREATE INDEX CONCURRENTLY (PostgreSQL) — builds index without locking
CREATE INDEX CONCURRENTLY idx_orders_user_id ON orders(user_id);
-- Takes longer but production traffic unaffected
```

---

# PART 2 — ARCHITECTURE DECISION SCENARIOS

---

## S6 🔴 Your team debates: REST vs GraphQL vs gRPC for your new microservices API. How do you decide?

**Answer:**

**REST — the right default:**

```
Use REST when:
✅ Public API (external consumers — standard, well-understood)
✅ Simple CRUD operations — resources map cleanly to HTTP verbs
✅ Caching matters — HTTP caching (ETags, Cache-Control) works naturally with REST
✅ Team is unfamiliar with GraphQL/gRPC — REST tooling is mature everywhere
✅ Browser clients — REST+JSON works everywhere, gRPC requires proxy

Limitations:
❌ Over-fetching: /api/users/123 returns all user fields even if you need only name
❌ Under-fetching: need user + orders + addresses = 3 separate requests
❌ No real-time support (need WebSocket separately)
```

**GraphQL — right for complex, multi-consumer UIs:**

```
Use GraphQL when:
✅ Multiple different clients (mobile, web, TV) need different data shapes
   Mobile: user{name, avatar}  Web: user{name, avatar, address, orders{status}}
   Same endpoint, client specifies what it needs
✅ Reducing network round-trips is critical (mobile on 4G)
✅ Rapidly evolving product — add fields without versioning API
✅ Complex nested data with many relationships

Limitations:
❌ Caching is complex (POST requests, complex query strings)
❌ N+1 problem at API layer (must use DataLoader batching pattern)
❌ Learning curve for backend team + tooling overhead
❌ Not great for file uploads
❌ Security: complex queries can be expensive — need query depth/complexity limits

Example implementation with Spring Boot:
```

```java
// Spring for GraphQL (Spring Boot 3.x)
@Controller
public class UserGraphQLController {

    @QueryMapping  // maps to GraphQL query { user(id: "123") { ... } }
    public User user(@Argument String id) {
        return userService.findById(id);
    }

    @SchemaMapping(typeName = "User", field = "orders")
    public List<Order> orders(User user) {
        // Called for EACH user when orders field is requested
        // N+1 problem: 100 users = 100 DB calls
        return orderService.findByUserId(user.getId());
    }

    // Fix N+1 with BatchLoader (DataLoader pattern)
    @BatchMapping  // Spring for GraphQL built-in batch support
    public Map<User, List<Order>> orders(List<User> users) {
        // Called ONCE with all users — single batch DB call
        List<Long> ids = users.stream().map(User::getId).toList();
        Map<Long, List<Order>> ordersByUser = orderService.findByUserIds(ids);
        return users.stream().collect(Collectors.toMap(u -> u, u -> ordersByUser.getOrDefault(u.getId(), List.of())));
    }
}
```

**gRPC — right for internal service-to-service:**

```
Use gRPC when:
✅ High-performance internal service communication (10x faster than REST)
✅ Streaming (client streaming, server streaming, bidirectional)
✅ Strict API contracts (Protobuf schema = compiler-enforced contracts)
✅ Polyglot services (Java service calling Python ML service — same Protobuf schema)
✅ Low latency is critical (trading, real-time bidding)

Limitations:
❌ Cannot call from browser directly (requires gRPC-web proxy)
❌ Protobuf binary format — not human-readable, harder to debug
❌ Tooling less mature than REST for testing

Protobuf schema:
```

```protobuf
// user.proto
syntax = "proto3";
package com.example;

service UserService {
  rpc GetUser (GetUserRequest) returns (UserResponse);
  rpc ListUsers (ListUsersRequest) returns (stream UserResponse);  // server streaming
  rpc CreateUser (CreateUserRequest) returns (UserResponse);
}

message GetUserRequest { string id = 1; }
message UserResponse {
  string id = 1;
  string name = 2;
  string email = 3;
}
```

**Decision matrix:**

| Criterion | REST | GraphQL | gRPC |
|---|---|---|---|
| Browser support | ✅ Native | ✅ Native | ⚠️ Needs proxy |
| Performance | Medium | Medium | High |
| Caching | ✅ HTTP cache | ⚠️ Complex | ❌ Manual |
| Learning curve | Low | High | Medium |
| Streaming | ❌ | ⚠️ Subscriptions | ✅ Native |
| Contract enforcement | ❌ | ⚠️ Schema | ✅ Protobuf |
| Best for | Public APIs, CRUD | Complex UI, mobile | Internal services |

**The real answer:** Use all three.
- **gRPC** between microservices internally (performance, contracts)
- **REST** for public/partner APIs (compatibility, caching)
- **GraphQL** for the mobile/web BFF (Backend-For-Frontend) layer

---

## S7 🔴 You need to implement idempotency for a payment API. How do you design it?

**Answer:**

Idempotency means: calling the same API multiple times with the same parameters produces the same result as calling it once. Critical for payments — retry a failed request, money must not be charged twice.

**The pattern:**

```java
// Client generates a unique Idempotency-Key per payment attempt
// If the request fails (network timeout, 500), client retries with SAME key
// Server recognizes the key and returns the stored result — no double charge

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping
    public ResponseEntity<PaymentResult> processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid PaymentRequest request) {

        // Step 1: Check if we've seen this key before
        Optional<IdempotencyRecord> existing = idempotencyStore.find(idempotencyKey);

        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();

            // Request is still being processed (concurrent duplicate)
            if (record.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                return ResponseEntity.status(409) // Conflict
                    .body(PaymentResult.processingInProgress(idempotencyKey));
            }

            // Already completed — return stored result (do NOT reprocess)
            return ResponseEntity.status(record.getHttpStatus())
                .body(record.getResponse());
        }

        // Step 2: Mark as in-progress BEFORE processing (prevent concurrent processing)
        IdempotencyRecord record = IdempotencyRecord.builder()
            .key(idempotencyKey)
            .status(IdempotencyStatus.IN_PROGRESS)
            .requestHash(hashRequest(request)) // detect different request body with same key
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plus(24, HOURS))
            .build();
        idempotencyStore.save(record);

        try {
            // Step 3: Process payment
            PaymentResult result = paymentService.processPayment(request);

            // Step 4: Store the result
            record.complete(200, result);
            idempotencyStore.save(record);

            return ResponseEntity.ok(result);

        } catch (PaymentDeclinedException e) {
            record.complete(402, PaymentResult.declined(e.getMessage()));
            idempotencyStore.save(record);
            return ResponseEntity.status(402).body(record.getResponse());

        } catch (Exception e) {
            // Don't store the result — allow retry
            record.fail();
            idempotencyStore.save(record);
            throw e; // let global exception handler return 500
            // Client can retry — new attempt will find FAILED record and reprocess
        }
    }
}
```

**Idempotency store (Redis with TTL):**

```java
@Service
public class RedisIdempotencyStore {
    private final RedisTemplate<String, IdempotencyRecord> redis;
    private static final String PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    public Optional<IdempotencyRecord> find(String key) {
        return Optional.ofNullable(redis.opsForValue().get(PREFIX + key));
    }

    public void save(IdempotencyRecord record) {
        redis.opsForValue().set(PREFIX + record.getKey(), record, TTL);
    }
}
```

**Handling concurrent duplicate requests (race condition):**

```java
// Problem: Two requests with same key arrive simultaneously
// Both check "does key exist?" → both see nothing → both start processing → double charge

// Fix: Use Redis SET NX (set if not exists) as an atomic lock
public boolean tryCreateIdempotencyRecord(String key, IdempotencyRecord record) {
    Boolean created = redis.opsForValue()
        .setIfAbsent(PREFIX + key, record, TTL); // atomic: only one thread succeeds
    return Boolean.TRUE.equals(created);
}

// In controller:
if (!idempotencyStore.tryCreateIdempotencyRecord(idempotencyKey, inProgressRecord)) {
    // Another request with same key is already being processed
    return ResponseEntity.status(409).body(PaymentResult.processingInProgress(idempotencyKey));
}
// Only one request proceeds — atomic guarantee
```

**Request fingerprinting (detect same key, different body):**

```java
private String hashRequest(PaymentRequest request) {
    // Hash the request body — if key is reused with different amount, reject
    return DigestUtils.sha256Hex(objectMapper.writeValueAsString(request));
}

// On second request with same key:
if (existing.isPresent() && !existing.get().getRequestHash().equals(hashRequest(request))) {
    return ResponseEntity.status(422) // Unprocessable Entity
        .body(PaymentResult.error("Idempotency key reused with different request body"));
}
```

---

## S8 🟡 A junior developer on your team committed secrets (API keys) to Git. How do you handle it?

**Answer:**

**Immediate action (first 30 minutes):**

```bash
# Step 1: Rotate the exposed credentials IMMEDIATELY
# Do NOT wait to clean up Git first — assume the key is compromised NOW
# Go to AWS/GCP/Stripe/whatever console and invalidate/rotate the key

# Step 2: Check for unauthorized usage
# AWS: CloudTrail logs for API calls with the exposed key
# GCP: Cloud Audit Logs
# Stripe: Dashboard → Logs → filter by API key
# Look for: unknown IP addresses, unusual times, unknown operations

# Step 3: Remove from Git history
# Option A: git filter-branch (older approach)
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch path/to/file-with-secret' \
  --prune-empty --tag-name-filter cat -- --all

# Option B: BFG Repo Cleaner (faster, recommended)
java -jar bfg.jar --delete-files file-with-secret.properties
# or replace specific text:
java -jar bfg.jar --replace-text secrets.txt  # secrets.txt lists strings to redact
git reflog expire --expire=now --all && git gc --prune=now --aggressive
git push --force

# Step 4: Invalidate all cached versions
# GitHub/GitLab may have already indexed the file — contact support to purge caches
# Forks: any fork made before deletion still has the secret — contact forkers
# GitHub secret scanning: check if they already detected and notified you
```

**Prevent recurrence:**

```bash
# Pre-commit hook with git-secrets or detect-secrets
pip install detect-secrets
detect-secrets scan > .secrets.baseline
# Commits with detected secrets are blocked

# .gitignore additions:
*.env
.env.local
*.pem
*.key
application-prod.properties
secrets/

# Use environment variables or secret managers instead of files:
# AWS Secrets Manager, HashiCorp Vault, GCP Secret Manager
# Never hardcode: use ${API_KEY} resolved from environment
```

```java
// Spring Boot — never hardcode, always externalise
@Value("${stripe.api.key}")  // from environment variable STRIPE_API_KEY
private String stripeApiKey;

// In production: set as environment variable, not in application.properties
// STRIPE_API_KEY=sk_live_... (injected by Kubernetes secret or ECS task definition)
```

**Handling the junior developer:**

```
This is a blameless post-mortem situation, not a disciplinary one.

The team failed the developer:
- No pre-commit hooks to catch secrets
- No training on secret management
- No code review caught it before merge

Action items:
1. Implement pre-commit hooks (detect-secrets) — automated prevention
2. Add secrets check to CI/CD pipeline — second layer
3. Team training session on secret management (30 min, not blame)
4. Update onboarding documentation to cover this explicitly
5. Consider: trunk-based development with required reviews for sensitive repos

What NOT to do:
- Publicly blame or embarrass the developer
- Make them feel they can't ask for help next time
- Treat it as unique to them — it happens to everyone
```

---

# PART 3 — TEAM AND PROCESS SCENARIOS

---

## S9 🟡 You are asked to estimate a 6-month project in 2 hours. How do you approach it?

**Answer:**

The pressure to give a precise number in 2 hours for 6 months of work is a red flag — but refusing helps nobody. The goal is an honest estimate with explicit uncertainty.

**The PERT estimation technique:**

```
For each feature, estimate three scenarios:
  O = Optimistic (everything goes perfectly)
  M = Most likely (realistic, normal hiccups)
  P = Pessimistic (unknowns, blockers, dependencies)

PERT estimate = (O + 4M + P) / 6
Standard deviation = (P - O) / 6

Example: User Authentication module
  O = 3 days (existing OAuth lib, no edge cases)
  M = 7 days (some customisation needed, SSO integration)
  P = 15 days (security audit required, legacy system integration)
  PERT = (3 + 28 + 15) / 6 = 7.7 days ≈ 8 days
  SD = (15-3)/6 = 2 days → 8 ± 2 days (67% confidence)
  → With 95% confidence: 8 + 2×2 = 12 days
```

**Structure the 2-hour session:**

```
30 min: Break down the project into epics and features (high-level WBS)
45 min: Estimate each feature using PERT (3-point estimation)
15 min: Add buffers:
  - Integration and testing: +20% of dev estimate
  - Unknown unknowns: +15%
  - Code review, PR cycles, meetings: +10%
  - Deployment and operational setup: +5%
  Total overhead: ~50% on top of pure dev estimate

10 min: Sanity check — does this feel right given team size?
20 min: Present findings with confidence intervals and assumptions
```

**What to communicate:**

```
"Based on a 2-hour analysis, my estimate is:
  - Optimistic (if no major surprises): 4 months
  - Most likely: 5.5 months
  - Pessimistic (if dependencies are difficult): 8 months

Key assumptions that could change this:
  1. Team of 4 developers, no changes to team composition
  2. The legacy CRM integration is read-only (if we need to write back, add 3 weeks)
  3. Security audit is not in scope (if required, add 4 weeks)
  4. No requirements changes after month 2

I'd recommend a 2-day spike to de-risk the CRM integration assumption before committing to a date."

This is honest, defensible, and shows senior thinking.
```

---

## S10 🟡 You inherit a codebase with no tests and technical debt. Your manager wants new features immediately. How do you handle it?

**Answer:**

This is the classic greenfield-vs-brownfield tension. The answer is never "stop features and write tests" because the business can't wait. But it's also never "ignore debt" because it compounds.

**The pragmatic approach:**

```
Rule: Add tests for every new feature and every bug fix you touch.
      Don't retrofit tests for code you're not touching.
      Focus debt reduction on the highest-risk, most-changed areas.

Rationale: If you never touch a 500-line untested method in PaymentService,
           it poses zero risk today. If you're about to change it, add tests first.
```

**The Boy Scout Rule: leave the campsite cleaner than you found it**

```java
// You're asked to add a discount feature to checkout
// The existing checkout code is untested, 300 lines, no separation of concerns

// WRONG approach: Add discount to the mess
public void checkout(Cart cart) {
    // ... 300 lines of spaghetti ...
    // Just add discount here somewhere
    if (cart.hasPromoCode()) { /* new discount logic inline */ }
    // ... more spaghetti
}

// RIGHT approach: Strangle the legacy, add tests for your new code
// Step 1: Write a test for the EXACT behaviour you're about to add
@Test
void applyDiscountReducesTotalByCorrectAmount() {
    Cart cart = new Cart();
    cart.addItem(new Item("BOOK", 100));
    cart.setPromoCode("SAVE10");
    
    BigDecimal total = checkoutService.calculateTotal(cart);
    
    assertThat(total).isEqualTo(BigDecimal.valueOf(90)); // 10% off
}

// Step 2: Refactor checkout to have a calculateTotal() method
// (extracted from the spaghetti) so you can test it in isolation
// Step 3: Implement the discount — it's now tested and isolated
```

**Communicate the debt cost in business terms:**

```
Don't say: "We have high cyclomatic complexity and low test coverage"
Do say: "The checkout module takes 3 days to add a feature that should take 4 hours.
         It caused 2 production incidents last quarter, each costing 4 hours of on-call.
         If we invest 2 sprints in refactoring checkout, we'll save that time on every
         feature we build in it for the next 2 years."

This frames it as ROI, not technical purity. Managers understand ROI.

Action: Track actual velocity in problem areas. Show the data.
If adding a feature in checkout takes 3 days but adding a feature in
the well-tested orders module takes 0.5 days, that's the business case.
```

---

## S11 🟡 How do you handle a code review where a senior developer keeps rejecting your PRs with subjective feedback?

**Answer:**

This is a real career situation that requires both technical and interpersonal skill.

**First: distinguish subjective from objective feedback**

```
Objective (legitimate): "This has an N+1 query — here's how to fix it"
Subjective (worth discussing): "I prefer method names like getUser vs fetchUser"
Opinion (not worth arguing): "I think the class should be in a different package"

Not all feedback is equal. Some is worth fighting for, some isn't worth the energy.
```

**For truly subjective/style issues:**

```
1. Propose a team coding standard document — resolve it once, not PR-by-PR
2. Add linter/formatter rules (Checkstyle, SpotBugs, SonarQube) — automate style enforcement
3. "I understand your preference. I'll follow it for now and suggest we add it to our
   standards doc so the whole team is aligned." — de-escalate while flagging

Never make it personal. "You're being too harsh" → bad
Better: "Can we agree on the style for this class so I can apply it consistently?"
```

**For patterns you genuinely disagree with:**

```
1. Ask for clarification: "Can you help me understand why you prefer this approach?
   I want to make sure I understand the reasoning, not just the change."
2. Share your reasoning: "I chose this approach because X. Do you see a risk with it
   that I'm missing?"
3. Propose an experiment: "Can we try both approaches and measure performance?"
4. Accept and learn: Senior engineers have seen things you haven't. Sometimes the
   preference is based on experience you don't have context for.
5. Escalate to tech lead only if the pattern is blocking delivery AND you've tried 1-4.
```

**Self-reflection: is the feedback actually valid?**

```
It's easy to feel unfairly targeted. Before escalating:
- Read the feedback a day later with fresh eyes
- Ask a third colleague for their perspective
- Check: did you document your reasoning in the PR description?
  A well-documented PR ("I chose X because Y, considered Z but rejected because W")
  gets fewer rejections because reviewers understand intent

The best PRs are pre-reviewed: talk to the reviewer before opening it.
"I'm about to implement X using approach Y — does that sound reasonable to you?"
Prevents most rejections before they happen.
```

---

## S12 🔴 You're asked to choose between building vs buying a core component. What factors drive your decision?

**Answer:**

**The build vs buy framework:**

```
Default answer for most components: BUY (use an open-source or SaaS solution)
Reason to build: only if buying creates an unacceptable constraint

Cost of building is almost always underestimated:
  - Initial development: 1x
  - Testing and hardening: 1x
  - Documentation: 0.5x
  - Ongoing maintenance: 0.5x/year
  - Security patches: 0.3x/year
  - Opportunity cost (what else could the team build?): large

Total: 3x initial, plus ongoing indefinitely
```

**Factors pushing toward BUY:**

```
✅ The problem is not your core competency
   (Authentication, logging, monitoring, caching — solved problems, don't reinvent)

✅ A mature solution exists with a large community
   (Spring Security, Redis, Elasticsearch — battle-tested at massive scale)

✅ Compliance requirements are met by the vendor
   (Payment processing — PCI DSS. Use Stripe, not your own card vault)

✅ Integration is simple and vendor lock-in is acceptable

✅ Time to market matters more than perfect fit
```

**Factors pushing toward BUILD:**

```
✅ The component IS your core differentiator
   (Google built their own search index — it IS their product)

✅ Available solutions have unacceptable performance, security, or licensing constraints

✅ Your requirements are so specific that all available solutions need heavy modification
   (If you're spending 80% of effort working around the tool, build instead)

✅ Total cost of ownership of vendor > cost of building over 3 years
   (Some SaaS tools are very expensive at scale — model this carefully)

✅ Vendor lock-in risk is high for a business-critical component
```

**Real example — rate limiting:**

```
Scenario: You need rate limiting for your API

Option A: Build your own (Redis + Lua script — 2 days to implement)
Option B: Use Resilience4j (already in your stack, 1 day to configure)
Option C: Use API Gateway (Kong, AWS API Gateway) — 0.5 days to configure

Decision: Use Resilience4j (already a dependency) or Kong if you already have it
Reason: Rate limiting is a solved problem. Your 2 days are better spent on
        features that differentiate your product.

When to build: if your rate limiting has very specific business rules
(e.g., dynamic limits per customer tier, geo-specific limits, usage-based billing)
that no off-the-shelf solution supports — then build the domain-specific part
on top of Redis primitives, not from scratch.
```

---
