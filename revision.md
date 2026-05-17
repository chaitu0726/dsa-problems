# Full Stack Product Company Interview Revision Guide (6+ YOE)

Target Stack:
- Java
- Spring Boot
- JavaScript
- Angular
- System Design
- Product Company Interviews

Target Companies:
- Adobe
- Atlassian
- Intuit
- Amazon
- Google
- Meta

---

# 1. JAVA

---

# HashMap Internals

## Internal Structure
HashMap internally uses:
- Array of buckets
- Each bucket contains:
  - linked list (older approach)
  - balanced tree (Java 8+ when collisions become high)

```text
bucket[0] -> null
bucket[1] -> Node -> Node
bucket[2] -> TreeNode
```

## Put Operation
Steps:
1. Compute hash
2. Find bucket index
3. If empty → insert
4. If collision:
   - compare keys using equals()
   - append/update
5. Resize if threshold exceeded

## Formula
```java
index = (n - 1) & hash
```

## Load Factor
Default = 0.75

Resize occurs when:
```text
size > capacity * loadFactor
```

## Treeification
If bucket size > 8:
- linked list becomes Red Black Tree
- improves worst-case lookup from O(n) to O(log n)

## Time Complexity
Average:
- get → O(1)
- put → O(1)

Worst:
- O(log n)
- O(n) before Java 8

## Follow-up: Bad hashCode
If many keys generate same hash:
- collisions increase
- performance degrades
- all data goes to same bucket

---

# equals() vs hashCode()

## Contract
If two objects are equal:
```java
a.equals(b) == true
```
then:
```java
a.hashCode() == b.hashCode()
```

But same hashCode does NOT mean equal.

## Why Important
Hash-based collections:
- HashMap
- HashSet

use hashCode first, then equals.

## Common Bug
Override equals without hashCode.

Result:
- duplicate entries
- failed lookups

---

# ConcurrentHashMap

## Why Needed
HashMap is not thread-safe.

ConcurrentHashMap supports:
- concurrent reads
- concurrent writes
- high performance

## Internals
Java 8:
- CAS operations
- synchronized at bucket level
- no full map locking

## Why Faster Than synchronizedMap
synchronizedMap:
- single lock for whole map

ConcurrentHashMap:
- fine-grained locking
- multiple threads can work simultaneously

---

# synchronized vs volatile

## synchronized
Provides:
- mutual exclusion
- visibility
- atomicity

Only one thread enters block.

```java
synchronized(this) {
   count++;
}
```

## volatile
Provides:
- visibility only

Does NOT provide:
- atomicity

```java
volatile boolean running;
```

Use when:
- one thread updates
- others read

---

# Thread Lifecycle

```text
NEW -> RUNNABLE -> RUNNING -> WAITING/BLOCKED -> TERMINATED
```

## start() vs run()

### start()
- creates new thread execution path
- JVM schedules thread

### run()
- normal method call
- no new thread created

## Can We Restart Thread?
No.
Once terminated:
```java
thread.start(); // illegal second time
```
throws:
```text
IllegalThreadStateException
```

---

# ExecutorService + Thread Pool

## Why Thread Pools
Creating threads is expensive.

Thread pools:
- reuse threads
- improve performance
- control concurrency

## Types

### Fixed Thread Pool
```java
Executors.newFixedThreadPool(10)
```

### Cached Thread Pool
Creates threads dynamically.

### Scheduled Thread Pool
For delayed tasks.

## Production Tuning

### CPU Bound
Threads ~= CPU cores

### IO Bound
More threads allowed.

## Rejection Policies
When queue full:
- AbortPolicy
- CallerRunsPolicy
- DiscardPolicy

---

# CompletableFuture

## Why Used
For async programming.

## Example
```java
CompletableFuture.supplyAsync(() -> getUser())
    .thenApply(user -> transform(user))
    .thenAccept(System.out::println);
```

## Important Methods
- thenApply
- thenCompose
- thenCombine
- exceptionally

## thenApply vs thenCompose

### thenApply
Transforms result.

### thenCompose
Flattens nested futures.

---

# Deadlock

## Definition
Two threads waiting forever for each other.

## Example
```java
Thread 1: Lock A -> Lock B
Thread 2: Lock B -> Lock A
```

## Prevention
- consistent lock ordering
- timeout locks
- avoid nested locking

---

# Immutable Class

## Rules
- class final
- fields private final
- no setters
- defensive copies

## Benefits
- thread-safe
- cache-friendly
- predictable

---

# Java Memory Model

## Stack
Stores:
- local variables
- method calls

Thread-specific.

## Heap
Stores:
- objects
- shared data

## Happens-Before
Ensures memory visibility.

Example:
- unlock happens-before lock
- volatile write happens-before read

---

# 2. SPRING BOOT

---

# Spring Bean Lifecycle

## Flow
1. Container starts
2. Bean instantiated
3. Dependencies injected
4. PostConstruct executed
5. Bean ready
6. PreDestroy before shutdown

## IOC Container
Spring manages objects instead of developer.

---

# Filter vs Interceptor vs AOP

## Filter
Part of servlet layer.

Used for:
- logging
- auth
- request modification

Runs before DispatcherServlet.

## Interceptor
Spring-specific.

Used for:
- authentication
- authorization
- request timing

Runs before controller.

## AOP
Used for cross-cutting concerns.

Examples:
- transactions
- logging
- monitoring

Works at method level.

---

# @Transactional

## Purpose
Manages DB transactions automatically.

## ACID
- Atomicity
- Consistency
- Isolation
- Durability

## Propagation

### REQUIRED
Join existing or create new.

### REQUIRES_NEW
Always create new transaction.

## Isolation Levels
- READ_UNCOMMITTED
- READ_COMMITTED
- REPEATABLE_READ
- SERIALIZABLE

## Self Invocation Problem
Calling transactional method inside same class bypasses proxy.

---

# Spring Security + JWT

## JWT Flow
1. User logs in
2. Server generates token
3. Client sends token in header
4. Server validates token

## Access Token
Short-lived.

## Refresh Token
Used to generate new access token.

---

# REST API Best Practices

## Naming
```text
GET /users
POST /users
GET /users/{id}
```

## Status Codes
- 200 OK
- 201 Created
- 400 Bad Request
- 401 Unauthorized
- 404 Not Found
- 500 Internal Server Error

## Pagination
```text
/users?page=1&size=20
```

## Idempotency
PUT should be idempotent.

---

# Global Exception Handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {
}
```

Centralizes error handling.

---

# Caching

## Redis
In-memory datastore.

Used for:
- caching
- sessions
- rate limiting

## Cache Aside Pattern
1. Check cache
2. Miss -> DB
3. Store in cache

## Cache Invalidation
Hardest problem.

Strategies:
- TTL
- write-through
- explicit eviction

---

# JPA/Hibernate

## Lazy Loading
Data fetched only when needed.

## Eager Loading
Fetched immediately.

## N+1 Problem
One query for parent + N queries for child.

Solution:
- join fetch
- entity graph

## First Level Cache
Session scoped.

---

# Optimizing Slow APIs

## Steps
1. Analyze DB queries
2. Add indexes
3. Reduce joins
4. Add caching
5. Pagination
6. Async processing
7. Batch queries

---

# Microservices Basics

## API Gateway
Single entry point.

## Service Discovery
Services find each other dynamically.

## Circuit Breaker
Stops repeated failing calls.

## Distributed Tracing
Track request across services.

---

# 3. JAVASCRIPT

---

# Event Loop

## Components
- Call Stack
- Web APIs
- Callback Queue
- Microtask Queue

## Priority
1. Call Stack
2. Microtasks
3. Macrotasks

## Microtasks
- Promise callbacks

## Macrotasks
- setTimeout
- setInterval

---

# var vs let vs const

## var
- function scoped
- hoisted

## let
- block scoped

## const
- cannot reassign

---

# Closures

## Definition
Function remembers outer scope variables.

## Example
```javascript
function outer() {
   let count = 0;
   return function() {
      count++;
   }
}
```

## Use Cases
- data hiding
- callbacks
- memoization

---

# Promise vs async-await

## Promise
Represents future value.

## async-await
Syntactic sugar over promises.

```javascript
async function test() {
   const data = await fetchData();
}
```

---

# Debounce vs Throttle

## Debounce
Execute after delay.

Use:
- search input

## Throttle
Execute once per interval.

Use:
- scroll events

---

# this Keyword

Depends on call site.

## Arrow Function
Does not have own this.

Uses lexical this.

---

# == vs ===

## ==
Loose equality.

Performs type coercion.

## ===
Strict equality.

Preferred.

---

# Hoisting

Declarations moved to top.

var initialized with undefined.

let/const in temporal dead zone.

---

# Promise.all()

Runs promises in parallel.

Fails fast if one fails.

---

# Browser Rendering

## Steps
1. Parse HTML -> DOM
2. Parse CSS -> CSSOM
3. Render Tree
4. Layout
5. Paint

## Reflow
Layout recalculation.

## Repaint
Visual update only.

---

# 4. ANGULAR

---

# Lifecycle Hooks

## ngOnInit
Initialization logic.

## ngOnDestroy
Cleanup.

## ngOnChanges
Respond to input changes.

---

# Change Detection

## Default
Checks entire component tree.

## OnPush
Checks only when:
- input reference changes
- event occurs
- observable emits

Improves performance.

---

# RxJS

## Observable
Stream of async data.

## Subject
Both observable + observer.

## BehaviorSubject
Stores latest value.

---

# switchMap vs mergeMap vs concatMap

## switchMap
Cancels previous request.

Best for:
- search API

## mergeMap
Runs in parallel.

## concatMap
Runs sequentially.

---

# Lazy Loading

Loads feature modules on demand.

Improves startup performance.

---

# Dependency Injection

Angular injects services automatically.

```typescript
constructor(private service: UserService) {}
```

---

# trackBy

Prevents unnecessary DOM recreation.

```html
*ngFor="let item of items; trackBy: trackById"
```

---

# State Management

## Service-Based
Simple apps.

## NgRx
Complex shared state.

Redux-inspired.

---

# HTTP Interceptors

Used for:
- auth token
- logging
- request modification

---

# Route Guards

Protect routes.

Example:
- authenticated users only

---

# Angular Performance Optimization

## Techniques
- OnPush
- lazy loading
- virtual scrolling
- trackBy
- memoization

---

# 5. DSA

---

# Sliding Window

## Use Cases
- substring problems
- contiguous subarrays

## Pattern
Expand right pointer.
Shrink left when invalid.

---

# Binary Search

## Condition
Array or answer space must be monotonic.

## Rotated Array
One side always sorted.

---

# Heap

## Min Heap
Top element smallest.

Used for:
- kth largest
- scheduling

## Max Heap
Top element largest.

---

# LRU Cache

## Data Structures
- HashMap
- Doubly Linked List

## Complexity
- get -> O(1)
- put -> O(1)

---

# BFS vs DFS

## BFS
Level order.
Queue.

## DFS
Depth traversal.
Stack/recursion.

---

# Topological Sort

Used for dependency ordering.

Applicable only for DAG.

---

# Backtracking

## Pattern
1. Choose
2. Explore
3. Unchoose

Questions:
- subsets
- permutations
- generate parenthesis

---

# Dynamic Programming

## Key Idea
Store overlapping subproblem results.

## Steps
1. Define state
2. Transition
3. Base case

---

# Longest Increasing Subsequence

## O(n^2)
DP comparison.

## O(n log n)
Binary search on tails array.

---

# 6. LOW LEVEL DESIGN

---

# SOLID Principles

## S - Single Responsibility
One class -> one responsibility.

## O - Open Closed
Open for extension.
Closed for modification.

## L - Liskov Substitution
Child should replace parent safely.

## I - Interface Segregation
Small focused interfaces.

## D - Dependency Inversion
Depend on abstractions.

---

# Strategy Pattern

Encapsulate algorithms.

Example:
- payment strategies
- notification channels

---

# Factory Pattern

Creates objects without exposing creation logic.

---

# Observer Pattern

One-to-many notification.

Example:
- event listeners
- pub-sub

---

# Singleton Pattern

Single object instance.

Use carefully.

---

# Parking Lot Design

## Entities
- ParkingLot
- Floor
- Slot
- Vehicle
- Ticket

## Key Concepts
- inheritance
- extensibility
- slot allocation strategy

---

# Notification System

## Channels
- Email
- SMS
- Push

Use strategy pattern.

---

# Splitwise Design

## Core Concepts
- users
- expenses
- balance sheet
- settlement

---

# Logger Framework

## Features
- log levels
- appenders
- async logging

---

# 7. HIGH LEVEL DESIGN

---

# System Design Approach

## Step 1
Clarify requirements.

## Step 2
Estimate scale.

## Step 3
High-level components.

## Step 4
DB design.

## Step 5
Scaling.

## Step 6
Bottlenecks.

---

# URL Shortener

## Components
- API servers
- DB
- cache

## Encoding
Base62.

## Bottlenecks
- hot URLs
- DB reads

Solution:
- caching
- replication

---

# Twitter Feed

## Approaches

### Fanout on Write
Push posts to followers.

Good for read-heavy.

### Fanout on Read
Generate feed dynamically.

Good for celebrity users.

## Components
- cache
- ranking
- pagination

---

# Notification System

## Architecture
- producer
- queue
- worker
- retry service

## Important Concepts
- retries
- DLQ
- idempotency

---

# Chat System

## Components
- WebSocket server
- message queue
- DB

## Challenges
- ordering
- online presence
- scaling

---

# File Upload Service

## Features
- chunk upload
- resumable upload
- virus scan
- async processing

Use:
- object storage
- CDN

---

# Payment System

## Critical Concepts
- idempotency
- consistency
- retries
- reconciliation

---

# Redis Caching Strategies

## Cache Aside
Application manages cache.

## Write Through
Write cache + DB together.

## Write Back
Write cache first.
DB updated later.

---

# SQL vs NoSQL

## SQL
- structured
- joins
- ACID

Use for:
- transactions
- relational data

## NoSQL
- flexible schema
- horizontal scaling

Use for:
- large-scale distributed data

---

# Database Indexing

## Purpose
Speeds up reads.

## Downside
Slower writes.

Because every insert/update also updates indexes.

---

# Partitioning/Sharding

Split data across multiple DBs.

Improves scalability.

---

# Replication

## Master-Slave
Writes to master.
Reads from replicas.

---

# Kafka Basics

## Components
- Producer
- Consumer
- Broker
- Topic
- Partition

## Ordering
Guaranteed only within partition.

---

# API Gateway

## Responsibilities
- authentication
- routing
- rate limiting
- logging

---

# CDN

Caches static content closer to users.

Improves latency.

---

# CAP Theorem

Distributed system can guarantee only two:
- Consistency
- Availability
- Partition Tolerance

---

# Rate Limiter

## Algorithms

### Token Bucket
Tokens refill gradually.

### Sliding Window
Tracks requests in rolling window.

---

# Observability

## Logs
Detailed events.

## Metrics
Numeric monitoring.

## Tracing
Track request across services.

---

# 8. BEHAVIORAL QUESTIONS

---

# Production Issue Answer Structure

## STAR Format

### Situation
Production outage.

### Task
Restore system quickly.

### Action
- identified bottleneck
- rollback
- added monitoring

### Result
System stabilized.
Root cause fixed.

---

# Scalability Challenge

Talk about:
- bottleneck
- optimization
- metrics improvement
- tradeoffs

---

# Conflict With Teammate

Focus on:
- communication
- data-driven discussion
- alignment

---

# Technical Debt

Explain:
- short-term business need
- long-term maintenance impact
- phased cleanup strategy

---

# Mentoring Juniors

Talk about:
- code reviews
- architecture guidance
- debugging help
- knowledge sharing

---

# Ambiguous Requirements

Approach:
1. Clarify goals
2. Discuss tradeoffs
3. Build incrementally
4. Gather feedback

---

# FINAL INTERVIEW TIPS

---

# How Senior Candidates Answer

## Good Structure
1. Clarify
2. Give simple solution
3. Improve gradually
4. Discuss tradeoffs
5. Mention scaling

---

# Most Important Senior-Level Skill

Interviewers care about:
- reasoning
- tradeoffs
- maintainability
- production awareness

more than memorized answers.

---

# Common Follow-up Questions

## Why this design?

## What are tradeoffs?

## What breaks at scale?

## How monitor this?

## How rollback safely?

## SQL vs NoSQL?

## Why cache here?

## How make thread-safe?

---

# Final Revision Strategy

## Daily Revision

### Day 1
Java + concurrency

### Day 2
Spring Boot + DB + caching

### Day 3
JavaScript + Angular

### Day 4
DSA patterns

### Day 5
LLD

### Day 6
HLD

### Day 7
Behavioral + mock interview

---

# Final Advice

For 6+ YOE product-company interviews:

You do NOT need:
- competitive programming level DSA
- perfect memorization

You DO need:
- strong fundamentals
- production thinking
- clean communication
- tradeoff analysis
- scalable design understanding

If you are comfortable discussing everything in this document confidently, you are already in a strong position for most product-company interviews.

