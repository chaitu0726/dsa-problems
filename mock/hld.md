
## Scalability · Availability · Distributed Systems · Real System Design

> **HLD Interview Framework — use this structure every time:**
> 1. Clarify requirements (functional + non-functional) — 5 min
> 2. Estimate scale (users, QPS, storage, bandwidth) — 3 min
> 3. Define the API — 3 min
> 4. Draw the high-level architecture — 10 min
> 5. Deep dive into components — 15 min
> 6. Address bottlenecks, failure scenarios, tradeoffs — 5 min

---

## Q1 🔴 Design a URL Shortener (like bit.ly)

**Answer:**

**Step 1: Clarify requirements**

Functional:
- Given a long URL, generate a unique short URL
- Given a short URL, redirect to the original long URL
- Custom aliases (optional)
- URL expiry (optional)
- Analytics: click counts, geo, referrer (optional)

Non-functional:
- 100M URLs created per day
- 10:1 read:write ratio → 1B redirects/day
- Redirects must be <100ms (latency-critical)
- High availability (99.99%)
- Short URLs must be unique globally

**Step 2: Estimate scale**

```
Write QPS: 100M / 86400s ≈ 1,160 writes/sec
Read QPS:  1B / 86400s ≈ 11,600 reads/sec

Storage per URL: ~500 bytes (long URL 200 + short URL 8 + metadata 300)
Total storage for 5 years: 100M × 365 × 5 × 500B ≈ 91 TB

Bandwidth: 11,600 reads/sec × 200 bytes ≈ 2.3 MB/s (manageable)
```

**Step 3: API Design**

```
POST /api/v1/shorten
  Request: { "longUrl": "https://...", "alias": "custom", "expiresAt": "2027-01-01" }
  Response: { "shortUrl": "https://sho.rt/aB3xYz", "shortCode": "aB3xYz" }

GET /{shortCode}
  Response: HTTP 301/302 redirect to long URL
  (301 = permanent — browser caches, reduces server load)
  (302 = temporary — every visit hits server — better for analytics)

GET /api/v1/analytics/{shortCode}
  Response: { "clicks": 1204, "topCountries": [...], "clicksPerDay": [...] }

DELETE /api/v1/urls/{shortCode}
  Auth required
```

**Step 4: High-Level Architecture**

```
User
  ↓
DNS → Load Balancer (L7, nginx/ALB)
  ↓
┌─────────────────────────────────┐
│  URL Service (Spring Boot)      │  ← stateless, horizontally scalable
│  - Shortening logic             │
│  - Redirect logic               │
│  - Rate limiting per IP         │
└─────────────────────────────────┘
  ↓              ↓              ↓
Redis Cache   PostgreSQL    Analytics Queue
(redirects)   (source of     (Kafka)
              truth)
                              ↓
                        Analytics Service
                        (ClickHouse/BigQuery)
```

**Step 5: Core Algorithm — generating the short code**

```
Option A: MD5 hash of long URL
  - MD5("https://...") → 128-bit hash
  - Take first 7 characters → "aB3xYz9"
  - Risk: COLLISION — different URLs might produce same prefix
  - Fix: check DB, if collision regenerate with offset

Option B: Base62 encoding of a unique ID (RECOMMENDED)
  Characters: a-z, A-Z, 0-9 = 62 characters
  7 characters = 62^7 = 3.5 trillion combinations — enough for billions of URLs

  Unique ID generation options:
  1. Database auto-increment ID (simple, single point of failure)
  2. Twitter Snowflake ID (distributed, timestamp-based, 64-bit)
  3. Ticket Server (Flickr approach — central counter server)

  ID = 10000001 → Base62 encode → "aB3xYz"
  Base62 decoding maps back to the ID → DB lookup
```

```java
public class Base62Encoder {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = 62;

    public String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(CHARS.charAt((int)(id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }

    public long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + CHARS.indexOf(c);
        }
        return result;
    }
}
```

**Step 6: Database schema**

```sql
CREATE TABLE urls (
    id          BIGSERIAL PRIMARY KEY,       -- the unique ID we encode to short code
    short_code  VARCHAR(10) UNIQUE NOT NULL, -- "aB3xYz"
    long_url    TEXT NOT NULL,
    user_id     BIGINT,                      -- nullable for anonymous
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    expires_at  TIMESTAMPTZ,
    is_active   BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_urls_short_code ON urls(short_code); -- fast redirect lookup
CREATE INDEX idx_urls_expires_at ON urls(expires_at); -- for cleanup job
```

**Step 7: Caching strategy**

```
Redirect flow (read-heavy, latency-critical):

1. Check Redis: GET shortCode → longUrl (cache hit → 301 redirect, <5ms)
2. Cache miss → DB query → cache the result with TTL (1 day)
3. For viral/popular URLs: TTL never expires (refresh on every access)

Cache invalidation:
- URL deleted → DELETE from Redis immediately
- URL expired → background job cleans up, or check expiry in cache entry

Cache key: "url:{shortCode}" → "{longUrl}"
Cache TTL: 24 hours (renewals on access for popular URLs)
Estimated cache hit rate: >95% (80-20 rule — top 20% of URLs get 80% of traffic)
```

**Bottlenecks and how to address them:**

```
1. DB write bottleneck (1,160 writes/sec):
   → Write to primary DB, async replicate to read replicas
   → Shard by short_code range or hash

2. Hot redirects (single viral URL gets millions of hits):
   → CDN caching: 301 redirects get cached at CDN edge nodes
   → Edge locations serve the redirect without hitting origin

3. ID generation at scale:
   → Snowflake IDs: 41-bit timestamp + 10-bit machine ID + 12-bit sequence
   → 4096 IDs/ms per machine, globally unique, no coordination needed

4. Analytics writes (1B events/day):
   → Kafka queue → async consumers → ClickHouse column store
   → Never write analytics in the redirect path — adds latency
```

---

## Q2 🔴 Design a Notification System at Scale (100M users)

**Answer:**

**Requirements:**

Functional:
- Send notifications via Email, SMS, Push, In-App
- User preference management (opt-out per channel per notification type)
- Scheduling (send at 9am user's timezone)
- Templates with personalisation
- Priority levels (OTP = critical, marketing = low)

Non-functional:
- 1M notifications/minute peak
- OTP delivered in <5 seconds
- Marketing notifications: eventual delivery (within 30 min acceptable)
- At-least-once delivery guarantee
- Deduplication (don't send same notification twice)

**Scale estimates:**

```
1M notifications/minute = ~17,000/sec peak
Types: 60% Push, 25% Email, 10% SMS, 5% In-App
Push: 10,000/sec (Firebase can handle this)
Email: 4,250/sec (SES, SendGrid)
SMS: 1,700/sec (Twilio — expensive, careful)
```

**Architecture:**

```
API / Event Source
(Order Service, Auth Service, Marketing Service)
        ↓
[Notification API] → validates, stores raw request
        ↓
[Kafka] Topic: notification-requests
        ↓
[Notification Router Service]
  - Loads user preferences
  - Checks DND (do not disturb)
  - Deduplicates (idempotency key in Redis, TTL 24h)
  - Fans out to per-channel Kafka topics
        ↓
┌─────────────────────────────────────────────┐
│  Push Topic   │  Email Topic  │  SMS Topic  │
└─────────────────────────────────────────────┘
        ↓             ↓              ↓
  Push Worker    Email Worker    SMS Worker
  (Firebase,     (SES/SendGrid)  (Twilio)
   APNs)
        ↓             ↓              ↓
              [Delivery Status DB]
              (tracks each delivery attempt,
               status: SENT/DELIVERED/FAILED/BOUNCED)
```

**Priority queues:**

```
Kafka topic: notifications-critical (OTP, security alerts)
  - 32 partitions, consumers always running, highest priority
  - SLA: delivered in <5 seconds

Kafka topic: notifications-transactional (order updates, payment receipts)
  - 16 partitions, SLA: <2 minutes

Kafka topic: notifications-marketing (promotions, newsletters)
  - 8 partitions, SLA: 30 minutes
  - Rate-limited to avoid carrier blocking
```

**Retry and failure handling:**

```
Delivery failure → exponential backoff retry (1s, 2s, 4s, 8s)
After 5 retries → Dead Letter Queue (DLQ)
DLQ processor: alert operations team + try fallback channel
  (SMS failed → try push as fallback)

Bounce handling (email): 
  - Hard bounce (invalid address) → mark email as invalid, remove from future sends
  - Soft bounce (mailbox full) → retry after 24h

Throttling (SMS):
  - Carriers reject bulk SMS from single number → rotate sender numbers
  - Respect TRAI/FCC sending limits per number per day
```

**Template and personalisation:**

```sql
CREATE TABLE notification_templates (
    template_id   VARCHAR(50) PRIMARY KEY,  -- "ORDER_SHIPPED_EMAIL"
    channel       VARCHAR(20),              -- EMAIL, SMS, PUSH
    locale        VARCHAR(10),              -- en-IN, hi-IN
    subject       TEXT,                     -- "Your order {{orderId}} has shipped!"
    body          TEXT,                     -- HTML with {{variables}}
    version       INT
);

-- Rendering:
Template: "Hello {{firstName}}, your order {{orderId}} shipped via {{carrier}}"
Variables: { firstName: "Alice", orderId: "ORD-123", carrier: "FedEx" }
Output: "Hello Alice, your order ORD-123 shipped via FedEx"
```

---

## Q3 🔴 Design a Real-Time Chat System (like WhatsApp/Slack)

**Answer:**

**Requirements:**

Functional: 1-1 messaging, group chats (up to 1000 members), message delivery status (sent/delivered/read), online presence, message history

Non-functional: 50M DAU, messages delivered in <500ms, 100M messages/day, messages stored for 5 years

**Scale:**

```
100M messages/day = ~1,200 messages/sec average, 10x peak = 12,000/sec
Message size: ~1KB average
Storage: 100M × 365 × 5 × 1KB = 182 TB over 5 years
Online users peak: 20M simultaneously → 20M WebSocket connections
```

**Architecture:**

```
Mobile/Web Client
    ↕ (WebSocket — persistent connection)
Load Balancer (Layer 4, TCP — not HTTP — to preserve WebSocket)
    ↕
Chat Server (stateful — holds WebSocket connections)
  - Connection Manager: userId → WebSocket connection
  - Message Router: where does the recipient connect?
    ↓
Service Registry (Redis) — stores which Chat Server each user is connected to
  Key: "ws:{userId}" → "chat-server-5:2345"  (server + connection ID)

Message flow:
Alice → ChatServer-A → Message Queue (Kafka)
     → ChatServer-B → Bob's WebSocket (if Bob is connected to B)
     → if Bob offline → store in Cassandra, deliver on reconnect

Message persistence: Apache Cassandra
  - Partition key: (chatId, bucket) where bucket = month (limits partition size)
  - Sort key: messageId (TimeUUID — naturally time-ordered)
  - Enables fast range queries: "give me last 50 messages in chatId X"

Presence service: Redis
  SETEX "presence:{userId}" 30 "online"  — refreshed every 15s by client heartbeat
  Key expires after 30s → user appears offline
```

**Message delivery guarantee:**

```
1. Alice sends message → assigned a client-generated messageId (UUID)
2. ChatServer persists to Cassandra
3. ChatServer publishes to Kafka: topic "messages", key=chatId (ensures ordering per chat)
4. Consumer checks if Bob is online (Redis lookup)
   - Online: deliver via Bob's WebSocket, mark DELIVERED
   - Offline: store in "pending:{bobId}" Redis list, deliver on reconnect
5. Bob's client sends ACK → message marked DELIVERED in DB
6. Bob opens message → client sends READ receipt → Alice sees ✓✓

At-least-once delivery: messages persist before ACK from recipient
Deduplication: client ignores messages with already-seen messageId
```

**Group messages:**

```
Naive approach: for 1000-member group, fan out 1000 messages = expensive

Better approach: "fan-out on read"
1. Store message ONCE with groupId
2. Each member, on opening chat, reads from group's message log
3. Track last_read_message_id per (userId, groupId)
4. Push notifications tell client "new messages" — client pulls

Hybrid: small groups (<50) fan-out on write (immediate push)
         large groups (>50) fan-out on read (pull on open)
```

---

## Q4 🔴 Design a Rate Limiter

**Answer:**

**Use cases:** Prevent abuse, enforce API quotas, protect downstream services, fair usage.

**Algorithms:**

```
1. Token Bucket (most common)
   - Bucket holds max N tokens. Tokens added at rate R/sec.
   - Each request consumes 1 token.
   - If bucket empty → reject (429)
   - Allows burst up to N, then limits to R/sec sustained
   - Good for: API rate limiting with burst tolerance

2. Leaky Bucket
   - Requests fill a queue (bucket). Processed at fixed rate.
   - If queue full → reject
   - Smooths out burst traffic — guarantees steady processing rate
   - Good for: protecting a fixed-capacity downstream service

3. Fixed Window Counter
   - Count requests in current time window (e.g., this minute)
   - Reset counter at window boundary
   - Problem: allows 2x burst at window boundary
     (100 req at 12:00:59 + 100 req at 12:01:00 = 200 in 2 seconds)

4. Sliding Window Log
   - Store timestamp of each request in a sorted set
   - On new request: remove timestamps older than window, count remaining
   - Accurate but memory-intensive (stores all timestamps)

5. Sliding Window Counter (best balance)
   - Current window count + weighted previous window count
   - current_count + (previous_count × overlap_fraction)
   - e.g., 70% into current window: current + 0.30 × previous
   - Very accurate, low memory
```

**Distributed implementation with Redis:**

```java
// Sliding window counter using Redis
public class RedisRateLimiter {
    private final RedisTemplate<String, String> redis;
    private final int maxRequests;
    private final Duration windowDuration;

    public RateLimitResult isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowDuration.toMillis();
        String key = "rate:" + clientId;

        // Use Lua script for atomicity — read + write in single operation, no race condition
        String luaScript = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window_start = tonumber(ARGV[2])
            local max_requests = tonumber(ARGV[3])
            local window_ms = tonumber(ARGV[4])
            
            -- Remove entries outside the window
            redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start)
            
            -- Count remaining entries
            local count = redis.call('ZCARD', key)
            
            if count < max_requests then
                -- Allow: add current timestamp as member (use timestamp as both score and member)
                redis.call('ZADD', key, now, now .. '-' .. math.random())
                redis.call('PEXPIRE', key, window_ms)
                return {1, max_requests - count - 1}  -- allowed, remaining
            else
                -- Get oldest entry to calculate retry-after
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                local retry_after = oldest[2] + window_ms - now
                return {0, 0, retry_after}  -- denied, retry-after ms
            end
        """;

        List<Object> result = redis.execute(
            RedisScript.of(luaScript, List.class),
            List.of(key),
            String.valueOf(now), String.valueOf(windowStart),
            String.valueOf(maxRequests), String.valueOf(windowDuration.toMillis())
        );

        boolean allowed = ((Long) result.get(0)) == 1L;
        long remaining = ((Long) result.get(1));
        return new RateLimitResult(allowed, remaining, allowed ? 0 : (Long) result.get(2));
    }
}

// Spring Filter
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final RedisRateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                     FilterChain chain) throws ServletException, IOException {
        String clientId = extractClientId(req); // IP or API key
        RateLimitResult result = rateLimiter.isAllowed(clientId);

        res.addHeader("X-RateLimit-Limit", "100");
        res.addHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));

        if (!result.isAllowed()) {
            res.setHeader("Retry-After", String.valueOf(result.getRetryAfterMs() / 1000));
            res.sendError(429, "Too Many Requests");
            return;
        }
        chain.doFilter(req, res);
    }

    private String extractClientId(HttpServletRequest req) {
        String apiKey = req.getHeader("X-API-Key");
        return apiKey != null ? "key:" + apiKey : "ip:" + req.getRemoteAddr();
    }
}
```

**Multi-tier rate limiting:**

```
Tier 1: Per IP — 1000 req/min (DDoS protection)
Tier 2: Per API key — 100 req/min (fair usage)
Tier 3: Per endpoint — e.g., /payments: 10 req/min (resource protection)
Tier 4: Per user — 50 req/min (authenticated endpoints)

Apply all tiers: request denied if ANY tier limit exceeded
```

---

## Q5 🔴 Design a Distributed Cache (like Redis)

**Answer:**

**Core operations:** GET key, SET key value [TTL], DELETE key, EXISTS key

**Data structures to support:** String, Hash, List, Set, Sorted Set

**Architecture decisions:**

**1. In-memory storage:**

```
Data stored entirely in RAM — nanosecond access vs millisecond for disk
Memory limit → eviction policies decide what to remove when full:
  - LRU (Least Recently Used) — evict the key accessed least recently
  - LFU (Least Frequently Used) — evict the key accessed least often
  - TTL expiration — keys with shortest TTL evicted first
  - Random — simplest, decent performance

Implementation of LRU:
  HashMap (O(1) lookup) + Doubly Linked List (O(1) move to front/evict from back)
  Combined = LinkedHashMap with accessOrder=true (Java's built-in LRU)
```

**2. Persistence (optional — makes it durable):**

```
RDB (Redis Database): Periodic snapshot of entire dataset to disk
  - Compact, fast restart
  - Risk: data loss between snapshots

AOF (Append-Only File): Log every write operation
  - fsync every second: at most 1 second of data loss
  - fsync always: no data loss, but slow
  - File grows large — compact with BGREWRITEAOF

Hybrid: RDB for fast restart + AOF for durability (Redis default in production)
```

**3. Replication:**

```
Primary-Replica model:
  - Writes go to primary
  - Primary asynchronously replicates to replicas
  - Replicas serve reads — scale read throughput
  - Replication lag: typically <1ms on same network

Failover (Redis Sentinel):
  - Sentinel processes monitor primary health
  - If primary down: Sentinels elect a new primary from replicas
  - Client re-routes to new primary (Sentinel provides discovery)
  - Minimum 3 Sentinels for quorum (majority vote)
```

**4. Clustering (horizontal sharding):**

```
Redis Cluster: 16,384 hash slots
  Slot = CRC16(key) % 16384
  Nodes own ranges of slots: Node1: 0-5460, Node2: 5461-10922, Node3: 10923-16383
  Each node has a replica

Client uses cluster-aware library:
  1. Hash the key → slot → which node?
  2. Send to that node
  3. If key moved (MOVED error) → update routing table, retry

Limitations of clustering:
  Multi-key operations (MGET, transactions) only work if all keys hash to the same slot
  Use hash tags to force keys to same slot: {user:123}:profile, {user:123}:orders
  Both hash to slot of "user:123"
```

---

## Q6 🔴 Design a Search Autocomplete System

**Answer:**

**Requirements:** As user types, show top 5 suggestions within 100ms. 10M DAU, 1B searches/day.

**Scale:**

```
1B searches / 86400s = ~11,600 QPS
Average query: 5 characters → 5 prefix requests per search
Effective prefix QPS: 58,000/sec
Data: top 1M search queries stored with frequency
```

**Approaches:**

**Option 1: Trie (prefix tree)**

```
Trie stores all prefixes:
"apple" → a → ap → app → appl → apple (5 nodes)
Each node stores: the character, children map, top-K results for this prefix

Advantage: very fast lookup O(length of prefix)
Disadvantage: Updating (real-time frequency updates) is expensive — must update ancestors
Memory: can be large for 1M+ words with all prefixes

Optimised: store top-K at each node (precomputed):
Node "app": stores ["apple", "application", "approve", "approach", "apps"]
Query "app": O(1) — just return precomputed top-K for this node
Update: asynchronous — rebuild top-K periodically (every hour)
```

**Option 2: Redis Sorted Set (simpler, production-ready)**

```java
// Key: "autocomplete:prefix" → Sorted Set of (term, frequency)
// zrangebyscore or zrevrange to get top-K by frequency

@Service
public class AutocompleteService {
    private final RedisTemplate<String, String> redis;
    private static final int TOP_K = 5;

    // Called when a user completes a search (to track frequency)
    public void trackSearch(String searchTerm) {
        String term = searchTerm.toLowerCase().trim();
        // Increment frequency for all prefixes of this term
        for (int i = 1; i <= term.length(); i++) {
            String prefix = term.substring(0, i);
            redis.opsForZSet().incrementScore("ac:" + prefix, term, 1.0);
        }
    }

    // Called on each keystroke
    public List<String> getSuggestions(String prefix) {
        String key = "ac:" + prefix.toLowerCase();
        Set<String> results = redis.opsForZSet().reverseRange(key, 0, TOP_K - 1);
        return results == null ? List.of() : new ArrayList<>(results);
    }
}
```

**Caching:**

```
Popular prefixes ("a", "ap", "th", "is") → enormous traffic
Cache their suggestions in a faster layer (local in-memory cache, Guava/Caffeine)
TTL: 5 minutes — updated as trending searches change

Cache hit rates:
  - 1-2 char prefixes: >99% (few unique values, high repetition)
  - 3-4 char prefixes: 90%+
  - 5+ char prefixes: cache only top 1000 — rest go to Redis
```

---

## Q7 🟡 CAP Theorem — explain it practically, not theoretically.

**Answer:**

CAP theorem states that in a distributed system, you can only guarantee TWO of:
- **C**onsistency — every read returns the most recent write
- **A**vailability — every request gets a response (not an error)
- **P**artition tolerance — system continues working despite network partition

**The catch:** Network partitions ARE going to happen in real distributed systems. So you're really choosing between CP and AP.

**CP — choose consistency over availability:**

```
Example: Banking transaction system
  - Network partition occurs (server A can't reach server B)
  - AP: Both servers accept writes → accounts diverge → money created from thin air
  - CP: System refuses writes during partition → returns error → no diverge

ZooKeeper is CP:
  - If a ZooKeeper node can't reach quorum, it stops accepting writes
  - Your config service returns errors during partition
  - Correct but unavailable
```

**AP — choose availability over consistency:**

```
Example: Shopping cart
  - Network partition occurs
  - Items added to cart on Server A not visible on Server B yet
  - AP: Both servers continue — user might see stale cart, but can still browse
  - CP: Block all cart operations during partition — bad UX

DynamoDB, Cassandra are AP:
  - Always available for reads and writes
  - "Eventually consistent" — replicas sync up when partition heals
  - Stale reads possible during partition
```

**PACELC — the better model for real decisions:**

CAP only addresses behaviour during partition. PACELC also addresses normal operation:
- During Partition: Availability vs Consistency (CAP)
- Else (normal): Latency vs Consistency (new dimension)

```
Low latency often requires relaxing consistency:
  - Write to one replica and return immediately (low latency, risk of loss)
  - vs. Write to all replicas before returning (consistent, higher latency)

Example: DynamoDB write consistency levels:
  - Eventually consistent: write acks from 1 replica, propagates async
  - Strongly consistent: write acks from majority, higher latency
```

---

## Q8 🟡 SQL vs NoSQL — when to choose which, with real examples.

**Answer:**

**SQL (Relational):** PostgreSQL, MySQL, Oracle
**NoSQL:** Document (MongoDB), Key-Value (Redis), Wide-Column (Cassandra), Graph (Neo4j)

**Choose SQL when:**

```
1. Data has clear relationships and you need JOINs
   Example: e-commerce — orders JOIN users JOIN products JOIN inventory
   A denormalized NoSQL schema would duplicate product info in every order

2. ACID transactions are required
   Example: banking — transfer money from A to B must be atomic
   NoSQL eventual consistency = money could appear lost during sync

3. Schema is stable and well-defined
   Example: HR system — employee records have fixed, well-known structure

4. Complex ad-hoc queries (aggregations, GROUP BY, window functions)
   SQL was built for this — NoSQL requires application-level logic

5. Data integrity via foreign keys and constraints
   Example: you want DB to enforce that every order must have a valid userId
```

**Choose NoSQL when:**

```
1. Massive scale with simple access patterns
   Cassandra: write 1M events/sec, read by primary key
   SQL: can't handle this write throughput without extreme sharding complexity

2. Schema flexibility — data structure varies per record
   MongoDB: product catalog — phones have different attributes than books
   SQL: requires nullable columns or EAV anti-pattern for variable attributes

3. Horizontal scaling is mandatory
   Redis Cluster, Cassandra, DynamoDB: add nodes, capacity grows linearly
   SQL: primary-replica scaling has limits — writes still go to one node

4. Key-value or time-series access patterns
   Redis: session cache, rate limits — always access by single key
   Cassandra: IoT sensor data — always query by deviceId + time range
   SQL would work but is overkill

5. Document storage where object model = storage model
   MongoDB: user profile with nested addresses, preferences, history
   SQL: requires multiple normalized tables + JOINs to reassemble
```

**Hybrid is the real answer at scale:**

```
Netflix architecture:
- MySQL → billing (ACID, relationships, financial data)
- Cassandra → viewing history (massive scale, time-series, simple reads)
- Redis → session cache, search suggestions
- Elasticsearch → search (full-text, complex queries)
- S3 → video files (object storage)

No single database fits all use cases — use the right tool per problem.
```

---
