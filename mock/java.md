
---

# PART 1 — CORE JAVA (Deep)

---

## Q1 🔴 Walk me through how the JVM executes a Java program — from `.java` file to running bytecode.

**Answer:**

Most candidates say "javac compiles it and the JVM runs it." The actual process has several distinct stages that directly affect performance and debugging.

**Stage 1: Compilation (javac)**

Your `.java` source is compiled into `.class` files containing **bytecode** — a platform-independent intermediate representation designed for the JVM, not for your CPU directly.

```bash
javac HelloWorld.java   # produces HelloWorld.class
javap -c HelloWorld     # disassemble — shows JVM instructions: getstatic, invokevirtual, return
```

**Stage 2: Class Loading**

When you run `java HelloWorld`, the JVM's **Class Loader** kicks in. Three built-in loaders form a delegation hierarchy:

```
Bootstrap ClassLoader       ← loads java.base (JDK core classes)
      ↓ delegates to
Extension ClassLoader        ← loads jdk/lib/ext
      ↓ delegates to
Application ClassLoader      ← loads your classpath (your code, libraries)
```

When a class is requested, each loader **first asks its parent** before trying itself — the **Parent Delegation Model**. This prevents your code from accidentally overriding `java.lang.String`.

**Stage 3: Bytecode Verification**

Before execution, the JVM verifier checks that bytecode doesn't violate JVM rules — no stack overflows, type safety, no illegal memory access. This is a security layer — you can't inject malicious bytecode.

**Stage 4: Interpretation vs JIT Compilation**

The JVM starts by **interpreting** bytecode — translating one instruction at a time. This is slow. The JVM monitors which methods are called frequently ("hot methods") and compiles those to native code using the **JIT (Just-In-Time) Compiler** (C1 for quick compilation, C2 for deeply optimised). This is why Java programs get faster the longer they run.

```
Interpreted (slow start) → JIT profiling → C1 compilation → C2 deep optimisation
```

**Stage 5: JVM Memory Areas**

```
Heap        → all objects. Managed by GC. Young Gen (Eden, S0, S1) + Old Gen
Stack       → per-thread. Stack frames (local vars, operand stack). Each method call = new frame
Metaspace   → class metadata, static fields, method bytecode (replaced PermGen in Java 8)
PC Register → per-thread, points to current bytecode instruction
```

**Why this matters in interviews:** Performance issues trace back here — slow startup (class loading overhead), memory leaks (objects never leaving Old Gen), StackOverflowError (deep recursion). Understanding this is the foundation for GC tuning: `-Xmx`, `-Xms`, `-XX:+UseG1GC`.

---

## Q2 🔴 Explain Java's memory model and the `volatile` keyword. Why isn't `volatile` always enough for thread safety?

**Answer:**

**The Problem: CPU Caches and Visibility**

Modern CPUs cache values in L1/L2/L3 caches. Thread A may update a variable in its CPU cache, but Thread B on a different core still reads the stale value from its own cache. This is a **visibility problem**.

```java
// WITHOUT volatile — broken
class StopFlag {
    private boolean running = true; // Thread B may cache this as true forever
    
    public void stop() { running = false; } // Thread A writes false to its cache only
    
    public void run() {
        while (running) { // Thread B never sees the update — infinite loop!
            doWork();
        }
    }
}
```

**What `volatile` actually does:**

1. **Visibility guarantee:** Every write is immediately flushed to main memory. Every read fetches from main memory, not CPU cache.
2. **Ordering guarantee (happens-before):** Writes to a volatile variable happen-before any subsequent reads of that variable across threads. Prevents compiler/CPU instruction reordering.

```java
class StopFlag {
    private volatile boolean running = true;
    
    public void stop() { running = false; } // immediately visible to all threads
    
    public void run() {
        while (running) { // always reads fresh from main memory — works correctly
            doWork();
        }
    }
}
```

**Why volatile is NOT enough for compound operations:**

`volatile` guarantees visibility and ordering, but NOT atomicity for multi-step operations.

```java
private volatile int counter = 0;

public void increment() {
    counter++; // NOT ATOMIC. This is: read → increment → write (3 separate steps)
}

// Race condition:
// Thread A reads counter = 5
// Thread B reads counter = 5   ← before A writes back
// Thread A writes counter = 6
// Thread B writes counter = 6  ← lost update — should be 7!
```

**The correct fixes:**

```java
// Option 1: AtomicInteger — lock-free, uses CAS (Compare-And-Swap)
private AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet(); // atomic — no race condition

// Option 2: synchronized — mutual exclusion
private int counter = 0;
public synchronized void increment() {
    counter++; // only one thread enters at a time
}
```

**Rule of thumb:**
- `volatile` → single flag/reference, one writer, many readers
- `AtomicInteger/AtomicReference` → single variable with compound operations
- `synchronized` / `ReentrantLock` → multi-step critical sections or multiple variables

---

## Q3 🔴 How does `HashMap` work internally? Compare with `LinkedHashMap`, `TreeMap`, and `ConcurrentHashMap`.

**Answer:**

**HashMap internals — step by step:**

A `HashMap<K,V>` is backed by an array of buckets (`Node<K,V>[]`). Exactly what happens on `put(key, value)`:

```
Step 1: hashCode() called on key
Step 2: Hash spread to avoid clustering: h = h ^ (h >>> 16)
Step 3: Bucket index: index = hash & (capacity - 1)
        (capacity always power of 2, so this equals hash % capacity)
Step 4: At that bucket:
        - Empty?             → insert new Node
        - Collision exists?  → walk the linked list, compare with .equals()
          - .equals() match  → UPDATE the value
          - No match         → APPEND to list
Step 5: Java 8+: When a bucket chain exceeds 8 nodes → converts to RED-BLACK TREE
        O(n) lookup → O(log n). Converts back to list if it shrinks below 6.
```

**Resizing (rehashing):**
Default capacity: 16. Load factor: 0.75. At 75% full (12 entries) → doubles to 32 and rehashes everything. Expensive — O(n). If you know your size: `new HashMap<>(expectedSize / 0.75 + 1)`.

**The equals/hashCode contract:**
If `a.equals(b)` → `a.hashCode() == b.hashCode()` MUST hold. Violate this and HashMap silently breaks.

```java
// BROKEN — equals overridden but not hashCode
class BadKey {
    int id;
    @Override public boolean equals(Object o) { return ((BadKey)o).id == this.id; }
    // hashCode uses Object default (memory address) → different hash → different bucket
}

Map<BadKey, String> map = new HashMap<>();
BadKey k1 = new BadKey(1);
map.put(k1, "hello");
map.get(new BadKey(1)); // returns null — different bucket entirely
```

**Comparison:**

| Map | Ordering | Thread-safe | Null key | Perf |
|---|---|---|---|---|
| `HashMap` | None | ❌ | ✅ (one) | O(1) avg |
| `LinkedHashMap` | Insertion or access order | ❌ | ✅ | O(1) avg |
| `TreeMap` | Sorted (natural/Comparator) | ❌ | ❌ | O(log n) |
| `ConcurrentHashMap` | None | ✅ | ❌ | O(1) avg |

**LinkedHashMap as LRU cache:**

```java
int capacity = 100;
Map<Integer, String> lruCache = new LinkedHashMap<>(capacity, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
        return size() > capacity; // auto-evict least recently accessed
    }
};
```

**ConcurrentHashMap internals (Java 8+):**
Uses an array of nodes with `synchronized` on individual buckets + CAS for insertions. No global lock — multiple threads write to different buckets simultaneously. `size()` uses a distributed counter (LongAdder-style) to avoid contention.

---

## Q4 🔴 Explain Java Streams — lazy evaluation, intermediate vs terminal, short-circuit. What are common performance mistakes?

**Answer:**

**Streams are lazy — nothing executes until a terminal operation is called:**

```java
List<String> names = List.of("Alice", "Bob", "Charlie", "Diana");

Stream<String> stream = names.stream()
    .filter(name -> {
        System.out.println("Filtering: " + name); // won't print yet
        return name.length() > 3;
    })
    .map(String::toUpperCase); // also not executed yet

// Nothing above ran. Now trigger execution:
List<String> result = stream.collect(Collectors.toList());
// NOW filter and map execute, in one pass through the data
```

**Intermediate operations** → return a Stream (lazy): `filter`, `map`, `flatMap`, `distinct`, `sorted`, `peek`, `limit`, `skip`

**Terminal operations** → trigger execution, produce a result: `collect`, `forEach`, `count`, `reduce`, `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch`, `min`, `max`

**Short-circuit — stop early when possible:**

```java
// findFirst stops as soon as it finds the first match
Optional<String> first = names.stream()
    .filter(n -> n.startsWith("C"))
    .findFirst(); // stops after "Charlie" — "Diana" never processed

// anyMatch stops at first true
boolean hasLong = names.stream()
    .anyMatch(n -> n.length() > 4); // stops at "Alice" — others not checked

// limit — only process n elements
names.stream().filter(...).limit(2).collect(Collectors.toList());
// stops pipeline after collecting 2 matches
```

**Common performance mistakes:**

```java
// MISTAKE 1: sorted() before filter() — sorts everything before reducing
list.stream().sorted().filter(x -> x > 10).collect(toList()); // bad
list.stream().filter(x -> x > 10).sorted().collect(toList()); // filter first, fewer to sort

// MISTAKE 2: Unnecessary intermediate collection
list.stream().filter(...).collect(Collectors.toList()) // materialises intermediate — wasteful
    .stream().map(...).collect(Collectors.toList());
// Fix: one pipeline
list.stream().filter(...).map(...).collect(Collectors.toList());

// MISTAKE 3: parallel stream on small data — overhead exceeds benefit
smallList.parallelStream().map(...).collect(toList()); // ForkJoinPool overhead for 20 items
// Parallel only pays off for large CPU-bound data sets (thousands+ items)

// MISTAKE 4: orElse vs orElseGet — orElse ALWAYS evaluates the argument
Optional<String> opt = Optional.empty();
String a = opt.orElse(computeExpensive());         // computeExpensive() called even if opt is present!
String b = opt.orElseGet(() -> computeExpensive()); // only called if opt is empty — use this
```

**flatMap — the most misunderstood:**

```java
List<List<String>> nested = List.of(List.of("a","b"), List.of("c","d"));

nested.stream().map(Collection::stream);    // Stream<Stream<String>> — not useful
nested.stream().flatMap(Collection::stream) // Stream<String>: a, b, c, d — flattened one level
      .collect(Collectors.toList());
```

---

## Q5 🔴 Explain Generics — type erasure, bounded wildcards, and PECS.

**Answer:**

**Type Erasure — generics exist only at compile time:**

Java generics are purely a compile-time feature. At runtime, all generic type info is erased. `List<String>` and `List<Integer>` are both just `List` at runtime.

```java
List<String> strings = new ArrayList<>();
List<Integer> ints = new ArrayList<>();
strings.getClass() == ints.getClass(); // true — both are ArrayList at runtime

// Ergo, you CANNOT:
// new T()                    — type unknown at runtime
// new T[]                    — can't create generic arrays
// instanceof List<String>    — just instanceof List (erased)
```

**Bounded wildcards — invariance is the problem:**

```java
// Without wildcards — List<Integer> is NOT a subtype of List<Number> (invariance)
public double sum(List<Number> list) { ... }
sum(new ArrayList<Integer>()); // COMPILE ERROR

// Upper bound (covariant reading — you READ from it)
public double sum(List<? extends Number> list) {
    for (Number n : list) { total += n.doubleValue(); } // reading is fine
    // list.add(new Integer(5)); ❌ — compiler rejects, type could be List<Double>
}
sum(new ArrayList<Integer>()); // ✅
sum(new ArrayList<Double>());  // ✅
```

**PECS — Producer Extends, Consumer Super:**

```java
// PRODUCER (you READ elements from it) → use extends
public void print(List<? extends Animal> animals) {
    for (Animal a : animals) { System.out.println(a.getName()); } // safe — guaranteed Animal
    // animals.add(new Dog()); ❌ — might be a List<Cat>
}

// CONSUMER (you WRITE elements into it) → use super
public void addDogs(List<? super Dog> list) {
    list.add(new Dog());    // ✅ — Dog always compatible with ? super Dog
    list.add(new Puppy());  // ✅ — Puppy extends Dog
    // Dog d = list.get(0); ❌ — might return Animal or Object, can't cast safely
}

// Collections.copy applies both:
// src is a producer → List<? extends T>
// dest is a consumer → List<? super T>
public static <T> void copy(List<? super T> dest, List<? extends T> src) { ... }
```

---

## Q6 🔴 What is the difference between checked and unchecked exceptions? How do you design a good exception hierarchy?

**Answer:**

```
Throwable
├── Error              — JVM failures (OutOfMemoryError, StackOverflowError) — never catch
└── Exception
    ├── RuntimeException (subclasses) — UNCHECKED — compiler does not force handling
    └── All other Exception subclasses — CHECKED — compiler forces try/catch or throws
```

**Checked** — represent recoverable conditions the caller should handle:

```java
public String readFile(String path) throws IOException {
    return Files.readString(Path.of(path)); // IOException is checked
}
// Caller must catch or declare throws — forced by compiler
```

**Unchecked (RuntimeException)** — programming errors or unrecoverable conditions:

```java
public void processRequest(Request request) {
    Objects.requireNonNull(request, "request must not be null"); // IllegalArgumentException
    if (request.getAmount() < 0) {
        throw new IllegalArgumentException("Amount cannot be negative: " + request.getAmount());
    }
}
// Callers not expected to catch these — fix the bug instead
```

**Designing a good hierarchy:**

```java
// Domain base exception — unchecked (modern preference)
public class PaymentException extends RuntimeException {
    private final String errorCode;
    
    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PaymentException(String message, String errorCode, Throwable cause) {
        super(message, cause); // ALWAYS chain the cause — preserves original stack trace
        this.errorCode = errorCode;
    }
}

// Specific subclass with domain data
public class InsufficientFundsException extends PaymentException {
    private final BigDecimal available;
    private final BigDecimal requested;
    
    public InsufficientFundsException(BigDecimal available, BigDecimal requested) {
        super("Insufficient funds: requested " + requested + " but " + available + " available",
              "PAY_001");
        this.available = available;
        this.requested = requested;
    }
    // Getters — callers can make decisions based on exact amounts
}
```

**Common mistakes:**

```java
// MISTAKE 1: Swallowing exceptions — worst practice
try { riskyOp(); } catch (Exception e) { /* silent */ }

// MISTAKE 2: Losing the cause chain
catch (SQLException e) {
    throw new ServiceException("DB error"); // original stack trace LOST
}
// Right:
throw new ServiceException("DB error", e); // chained — debuggable

// MISTAKE 3: Exceptions for flow control
try {
    int val = Integer.parseInt(input); // throws on invalid — expensive (stack trace creation)
} catch (NumberFormatException e) { ... }
// Right: validate first
if (input.matches("-?\\d+")) { int val = Integer.parseInt(input); }
```

---

## Q7 🔴 Explain `synchronized`, `ReentrantLock`, and `StampedLock`. When do you use each?

**Answer:**

**`synchronized` — built-in, simplest:**

```java
public class Counter {
    private int count = 0;
    
    public synchronized void increment() { count++; } // locks on 'this'
    
    private final Object lock = new Object();
    public void decrement() {
        synchronized(lock) { count--; } // lock on specific object — more granular
    }
}
```

Limitations: can't interrupt waiting thread, no tryLock, always exclusive (no read/write split), lock releases only at block end.

**`ReentrantLock` — explicit locking, more control:**

```java
public class SafeCache {
    private final ReentrantLock lock = new ReentrantLock();
    private Map<String, String> cache = new HashMap<>();
    
    public void put(String key, String value) {
        lock.lock();
        try { cache.put(key, value); }
        finally { lock.unlock(); } // ALWAYS in finally — else deadlock on exception
    }
    
    // Non-blocking attempt
    public boolean tryPut(String key, String value) {
        if (lock.tryLock()) {
            try { cache.put(key, value); return true; }
            finally { lock.unlock(); }
        }
        return false; // lock not available, don't wait
    }
    
    // Timeout
    public boolean timedPut(String key, String value) throws InterruptedException {
        if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
            try { cache.put(key, value); return true; }
            finally { lock.unlock(); }
        }
        return false;
    }
    
    // Interruptible — can cancel a waiting thread
    public void interruptiblePut(String key, String value) throws InterruptedException {
        lock.lockInterruptibly(); // throws if thread is interrupted while waiting
        try { cache.put(key, value); }
        finally { lock.unlock(); }
    }
}
```

**`ReentrantReadWriteLock` — for read-heavy scenarios:**

```java
private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
private final Lock readLock = rwLock.readLock();
private final Lock writeLock = rwLock.writeLock();

// Multiple threads hold readLock simultaneously — no blocking between readers
public String get(String key) {
    readLock.lock();
    try { return cache.get(key); }
    finally { readLock.unlock(); }
}

// writeLock is exclusive — blocks all readers and other writers
public void put(String key, String value) {
    writeLock.lock();
    try { cache.put(key, value); }
    finally { writeLock.unlock(); }
}
```

**`StampedLock` — optimistic reads (Java 8+, highest performance):**

```java
private final StampedLock lock = new StampedLock();
private double x, y;

public double distanceFromOrigin() {
    long stamp = lock.tryOptimisticRead(); // no actual lock — just a version stamp
    double currentX = x, currentY = y;    // read values (may be inconsistent)
    
    if (!lock.validate(stamp)) {           // did a write happen during our read?
        stamp = lock.readLock();           // yes — fall back to real read lock
        try { currentX = x; currentY = y; }
        finally { lock.unlockRead(stamp); }
    }
    // If no write happened, use the optimistically read values — no lock overhead!
    return Math.sqrt(currentX * currentX + currentY * currentY);
}

public void move(double dX, double dY) {
    long stamp = lock.writeLock();
    try { x += dX; y += dY; }
    finally { lock.unlockWrite(stamp); }
}
```

**Decision table:**

| Scenario | Use |
|---|---|
| Simple mutual exclusion, small critical section | `synchronized` |
| Need tryLock, timeout, or interruptible wait | `ReentrantLock` |
| Read-heavy, few writes (e.g., config, reference data) | `ReentrantReadWriteLock` |
| Very high read frequency, writes very rare | `StampedLock` |
| Single counter or reference with compound ops | `AtomicInteger` / `AtomicReference` |

---

## Q8 🟡 What are functional interfaces? Explain `Function`, `Predicate`, `Consumer`, `Supplier` with real examples.

**Answer:**

A **functional interface** has exactly one abstract method. The `@FunctionalInterface` annotation enforces this at compile time. Enables lambda expressions and method references.

```java
// Function<T, R> — takes T, returns R
Function<String, Integer> length = String::length;
Function<String, String> upperAndExclaim =
    ((Function<String, String>) String::toUpperCase).andThen(s -> s + "!");
upperAndExclaim.apply("hello"); // "HELLO!"

// Compose: f.compose(g) means f(g(x)) — g runs first
Function<Integer, Integer> times2 = x -> x * 2;
Function<Integer, Integer> plus3 = x -> x + 3;
times2.compose(plus3).apply(5); // times2(plus3(5)) = times2(8) = 16

// Predicate<T> — takes T, returns boolean
Predicate<String> isLong = s -> s.length() > 5;
Predicate<String> startsA = s -> s.startsWith("A");
Predicate<String> longAndA = isLong.and(startsA);
Predicate<String> longOrA = isLong.or(startsA);
Predicate<String> notLong = isLong.negate();

List.of("Alice", "Alexander", "Bob").stream()
    .filter(isLong.and(startsA))
    .collect(Collectors.toList()); // ["Alexander"]

// Consumer<T> — takes T, returns void (side effects)
Consumer<String> print = System.out::println;
Consumer<Employee> logAndSave = 
    ((Consumer<Employee>) e -> log.info("Saving: {}", e))
    .andThen(employeeRepo::save); // chained consumers

employees.forEach(logAndSave);

// Supplier<T> — takes nothing, returns T (lazy factory)
Supplier<List<String>> listFactory = ArrayList::new;
Supplier<LocalDate> today = LocalDate::now; // evaluated lazily when .get() called

// Critical difference — orElse vs orElseGet:
Optional<Config> opt = Optional.empty();
Config a = opt.orElse(loadConfig());         // loadConfig() ALWAYS called
Config b = opt.orElseGet(() -> loadConfig()); // only called if opt is empty
// For expensive operations, ALWAYS use orElseGet
```

---
