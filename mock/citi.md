

## How to use this document
- Each round mirrors the actual Citi hiring process
- Questions marked 🔴 Hard · 🟡 Medium · 🟢 Easy
- Coding questions: solve independently — no answers provided
- All other questions: detailed answer keys included
- Use STAR method (Situation → Task → Action → Result) for behavioural questions

---

# Round 1 — Online Assessment / Coding Test
**Format:** HackerRank or Workday · 60–90 min · Automated screening  
**Focus:** DSA problems + Spring/Java MCQs  

---

### Coding Problems (solve independently — no answer keys)

**Q1 🟡** Given a list of transactions, find all duplicate transaction IDs and return them sorted. Use Java Streams.

**Q2 🟡** Implement a rate limiter using a sliding window algorithm in Java. Make it thread-safe using concurrent utilities.

**Q3 🟡** Given a binary tree, return the level-order traversal as a list of lists (BFS).

**Q4 🔴** Given an array of stock prices, find the maximum profit from at most two non-overlapping buy-sell transactions.

**Q5 🟢** Flatten a nested list of integers using Java Streams (`flatMap`).

---

### MCQ — Spring Boot & Java Concepts

**Q6 🟢** What is the difference between `@RestController` and `@Controller`?

**Answer:**  
`@RestController` is a composed annotation combining `@Controller` and `@ResponseBody`. It automatically serializes return values to JSON/XML for every method. `@Controller` is used in traditional MVC applications with view resolvers (Thymeleaf, JSP) where methods return view names. Use `@RestController` for REST APIs.

---

**Q7 🔴** In Spring Boot, a `@Transactional` method calls another `@Transactional` method in the same class. What happens?

**Answer:**  
The inner `@Transactional` annotation is ignored. Spring's transaction management works via AOP proxies — when a method calls another method within the same bean, it bypasses the proxy and goes directly to the target object. So the outer transaction is reused (default `REQUIRED` propagation). The inner method never gets its own transaction.  
**Fix:** Inject the bean into itself (self-injection via `@Autowired`) or extract the inner method to a separate Spring-managed bean.

---

**Q8 🟢** What is the default scope of a Spring Bean?

**Answer:**  
Singleton — one instance per Spring IoC container. Other scopes: prototype (new instance per injection), request (per HTTP request), session (per HTTP session), application (per ServletContext).

---

**Q9 🟡** What is the difference between `@PathVariable` and `@RequestParam`?

**Answer:**  
`@PathVariable` extracts values from the URI path: `GET /users/{id}` → `@PathVariable Long id`.  
`@RequestParam` extracts query parameters: `GET /users?role=admin` → `@RequestParam String role`.  
`@RequestParam` can have default values and be marked optional; `@PathVariable` is always required by default.

---

**Q10 🟡** What does `Optional` in Java 8 solve and when should you NOT use it?

**Answer:**  
`Optional<T>` is a container that may or may not hold a non-null value. It forces callers to explicitly handle the absent case, reducing `NullPointerException` risk. You should NOT use it: as a method parameter (use overloading or null checks instead), in serialized classes/entities, or in collections (use empty collections instead). It is designed for return types only.

---

# Round 2 — Core Java & Spring Boot Technical
**Format:** 45–60 min · Senior engineer panel · Deep dive  
**Focus:** Spring internals, microservices, security, database — everything from JD  

---

**Q1 🔴** Walk me through how Spring Boot auto-configuration works internally. How does `@SpringBootApplication` trigger it?

**Answer:**  
`@SpringBootApplication` is a composed annotation of three things:
- `@Configuration` — marks the class as a source of bean definitions
- `@EnableAutoConfiguration` — triggers auto-configuration
- `@ComponentScan` — scans the package and sub-packages for components

`@EnableAutoConfiguration` reads the file `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` (Spring Boot 3.x) which lists all auto-configuration classes. Each class is conditional — e.g., `@ConditionalOnClass(DataSource.class)` only configures a data source if the class is on the classpath, `@ConditionalOnMissingBean` skips if you've already defined your own bean.  
You can exclude specific auto-configurations using `@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})` or `spring.autoconfigure.exclude` in `application.properties`.

---

**Q2 🔴** You reduced app load time by 50% at TIAA. Walk me through the exact caching and gzip strategy you used and the tradeoffs you considered.

**Answer (your personal story — adapt to what you actually did):**  
- **Profiling first:** Used tools (APM / Splunk) to identify the bottlenecks — slow DB queries, uncompressed payloads, redundant API calls.
- **Caching:** Implemented in-memory caching (e.g., Caffeine or Spring Cache with `@Cacheable`) for frequently read, rarely changing data. Configured TTL-based eviction to avoid stale data. For distributed scenarios, Redis can be used.
- **Gzip compression:** Enabled in Spring Boot via `server.compression.enabled=true`, `server.compression.min-response-size=1024`, `server.compression.mime-types=application/json,text/html`. This reduced payload size for API responses significantly.
- **Frontend:** Enabled Angular production build (`ng build --prod`) which includes AOT compilation, tree-shaking, and minification.
- **Tradeoffs:** Caching introduces stale data risk — addressed via cache invalidation on write operations. Gzip adds CPU overhead on the server — acceptable given our read-heavy traffic pattern. Measured result: 50% reduction in load time, 40% improvement in AppDex score.

---

**Q3 🟡** Explain the difference between `@Bean`, `@Component`, `@Service`, and `@Repository`. When would you use each?

**Answer:**  
- `@Component` — generic stereotype annotation. Spring detects and registers it as a bean via component scanning.
- `@Service` — semantic specialization of `@Component` for business logic layer. Functionally identical but communicates intent.
- `@Repository` — for the data access layer. Additionally enables Spring's exception translation — converts JDBC/JPA exceptions into Spring's `DataAccessException` hierarchy.
- `@Bean` — used inside a `@Configuration` class to declare a bean explicitly. Used when you cannot annotate the class itself (e.g., third-party library classes like `RestTemplate`, `ObjectMapper`).

---

**Q4 🔴** How did you implement JWT/OAuth in your applications? How do you handle token expiry and refresh in a microservices architecture?

**Answer:**  
- **JWT structure:** Three base64url-encoded parts — header (algorithm), payload (claims: userId, roles, expiry), signature. Stateless — the server verifies using a secret/public key without a DB lookup.
- **Spring Security integration:** Create a filter extending `OncePerRequestFilter`. Extract the token from the `Authorization: Bearer <token>` header, validate the signature and expiry, set the `SecurityContext`.
- **Refresh token flow:** Issue a short-lived access token (15 min) and a long-lived refresh token (7 days). Store refresh token in an `HttpOnly` cookie (not `localStorage` — prevents XSS). When access token expires, client sends refresh token to `/auth/refresh` endpoint, which issues a new access token.
- **Microservices:** API Gateway validates the JWT and passes user claims as request headers to downstream services. Downstream services trust headers from the gateway (internal network only).
- **Token revocation:** Maintain a Redis blacklist with TTL matching the token's remaining expiry. On logout, add the token's `jti` (JWT ID) claim to the blacklist.

---

**Q5 🟡** What is the N+1 query problem in Hibernate/JPA? How did you handle it?

**Answer:**  
The N+1 problem occurs when fetching N parent entities triggers N additional queries to fetch their associated children — resulting in N+1 total DB calls instead of 1.  
Example: fetching 100 `Order` entities, then Hibernate fires 100 separate queries to load each order's `items`.  
**Fix options:**
- `JOIN FETCH` in JPQL: `SELECT o FROM Order o JOIN FETCH o.items` — fetches everything in one query
- `@EntityGraph` on the repository method — declarative eager loading per query
- `@BatchSize(size=20)` — fetches children in batches instead of one-by-one
- `@NamedEntityGraph` for reusable graphs  
**Detection:** Enable `spring.jpa.show-sql=true` in development, use Splunk or APM in production to count queries per request.

---

**Q6 🔴** How do microservices handle distributed transactions? What pattern applies to your Unified Workflow project?

**Answer:**  
Traditional 2-Phase Commit (2PC) is too slow and creates tight coupling between services — avoided in microservices.  
**Saga Pattern** is the standard approach:
- **Choreography-based Saga:** Each service publishes events after completing its step. Other services listen and react. No central coordinator. Good for simple flows; harder to debug.
- **Orchestration-based Saga:** A central orchestrator service directs each participant step-by-step and handles compensating transactions on failure. Easier to track state.  
**Compensating transactions:** If step 3 of 5 fails, steps 1 and 2 must be undone via compensating actions (e.g., reverse a payment, release a reserved seat).  
**Outbox pattern:** Write the DB record and the event in the same transaction to an "outbox" table. A separate publisher picks it up and sends to Kafka — guarantees at-least-once delivery without distributed transactions.  
For the Unified Workflow project: describe how you ensured consistency across workflow steps, which events were emitted, and how failures were handled.

---

**Q7 🟡** What is the difference between `HashMap` and `ConcurrentHashMap`? When would you use each?

**Answer:**  
`HashMap` is not thread-safe — concurrent modifications can cause data corruption or infinite loops. `ConcurrentHashMap` is thread-safe and designed for concurrent access. It uses segment-level locking (Java 7) or CAS operations with synchronized blocks on individual buckets (Java 8+) — allowing multiple threads to read/write simultaneously without locking the entire map.  
Use `HashMap` in single-threaded contexts. Use `ConcurrentHashMap` when multiple threads need to read/write the same map (e.g., a shared cache, request counters). For read-heavy scenarios, `ConcurrentHashMap` is much faster than wrapping `HashMap` with `Collections.synchronizedMap()`.

---

**Q8 🟡** Explain the Spring Bean lifecycle from instantiation to destruction.

**Answer:**  
1. Spring container reads bean definitions (XML, annotations, or Java config)
2. Bean is instantiated (constructor called)
3. Dependencies are injected (setter injection or field injection)
4. Bean name/factory/context set if implementing `*Aware` interfaces
5. `BeanPostProcessor.postProcessBeforeInitialization()` called
6. `@PostConstruct` method (or `InitializingBean.afterPropertiesSet()`) called
7. `BeanPostProcessor.postProcessAfterInitialization()` called — bean is ready to use
8. Bean lives in the container
9. On shutdown: `@PreDestroy` method called (or `DisposableBean.destroy()`)

---

**Q9 🟡** How does `@Async` work in Spring Boot? What are common pitfalls?

**Answer:**  
`@Async` marks a method to be executed in a separate thread from Spring's `TaskExecutor`. You must enable it with `@EnableAsync` on a configuration class. The method must be in a different bean (same-class invocation bypasses the proxy — same issue as `@Transactional`).  
**Pitfalls:**
- Self-invocation: calling `@Async` from same class won't work — use a separate bean
- Return type: use `void` or `CompletableFuture<T>` — never return the actual value directly
- Exception handling: exceptions in void `@Async` methods are swallowed by default — configure an `AsyncUncaughtExceptionHandler`
- Thread pool sizing: default pool is small — configure a custom `ThreadPoolTaskExecutor` for production

---

**Q10 🟡** What is Spring Security's filter chain and how do you customise it?

**Answer:**  
Spring Security implements security as a chain of servlet filters. Each request passes through this chain before reaching your controller. Key filters include: `SecurityContextPersistenceFilter`, `UsernamePasswordAuthenticationFilter`, `BasicAuthenticationFilter`, `ExceptionTranslationFilter`, `FilterSecurityInterceptor`.  
Customise by extending `SecurityFilterChain` bean in a `@Configuration` class annotated with `@EnableWebSecurity`. Use `HttpSecurity` DSL to configure: which endpoints require authentication, which authentication mechanism to use (JWT, OAuth2, form login), CORS/CSRF settings, session management.  
For JWT: disable session creation (`STATELESS`), disable CSRF (stateless APIs don't need it), add your custom JWT filter before `UsernamePasswordAuthenticationFilter`.

---

# Round 3 — Angular & Frontend Technical
**Format:** 45 min · Frontend-focused engineer  
**Focus:** Angular architecture, RxJS, performance, TypeScript  

---

**Q1 🔴** Explain Angular's change detection mechanism. What is OnPush strategy and when would you use it?

**Answer:**  
Angular uses Zone.js to intercept async operations (setTimeout, Promises, HTTP calls, DOM events) and trigger change detection across the entire component tree by default.  
**Default strategy:** After every async event, Angular checks every component from root to leaf for changes — can be expensive in large apps.  
**OnPush strategy:** Change detection only fires for a component when:
- An `@Input` reference changes (not just a property mutation)
- An Observable the component subscribes to via `async` pipe emits a new value
- `ChangeDetectorRef.markForCheck()` is called manually
- A DOM event originates within the component itself  
**When to use:** Data-heavy dashboards, tables with hundreds of rows, components receiving immutable data objects. Combine with `async` pipe and `trackBy` in `*ngFor`. Reduces unnecessary re-renders significantly — this directly contributed to your AppDex improvement at TIAA.

---

**Q2 🔴** What are RxJS higher-order mapping operators? Give a concrete use case for each.

**Answer:**

| Operator | Behaviour | Use case |
|---|---|---|
| `switchMap` | Cancels previous inner observable on new emission | Search autocomplete — cancel stale HTTP request on every keystroke |
| `mergeMap` | Runs all inner observables concurrently | Upload multiple files in parallel |
| `concatMap` | Queues inner observables, runs sequentially | Submit a multi-step form where order matters |
| `exhaustMap` | Ignores new emissions while inner observable is active | Login button — prevent duplicate requests while one is in flight |

**Common mistake:** Using `mergeMap` for API calls that depend on previous results — use `concatMap` or `switchMap` instead.

---

**Q3 🟡** How would you structure a large Angular application? What lazy loading strategy would you use?

**Answer:**  
- **Feature modules:** One `NgModule` per domain feature (e.g., `RequestModule`, `WorkflowModule`, `OpsModule`). Each self-contained with its own components, services, routing.
- **Core module:** Singleton services (auth service, API interceptor, guards). Imported once in `AppModule`. Throw an error if imported elsewhere.
- **Shared module:** Reusable dumb components, pipes, directives. Exported for use in feature modules.
- **Lazy loading:** Configure feature routes with `loadChildren: () => import('./feature/feature.module').then(m => m.FeatureModule)`. The browser only downloads the feature bundle when the route is visited. Reduces initial bundle size.
- **Standalone components (Angular 14+):** Alternative to NgModules. Use `loadComponent` in routes for fine-grained lazy loading without module overhead.
- **Preloading strategy:** Use `PreloadAllModules` to preload lazy chunks in the background after the app loads — best of both worlds for UX.

---

**Q4 🔴** What is NgRx and when would you choose it over a `BehaviorSubject` service?

**Answer:**  
NgRx implements the Redux pattern in Angular: unidirectional data flow with Actions, Reducers, Effects, and Selectors. The entire app state lives in a single immutable store.  
**Choose NgRx when:**
- State is shared across many unrelated components
- You need time-travel debugging (Redux DevTools)
- Multiple async side effects must be coordinated
- Team is large (4+ developers) — enforces consistency
- Audit trail of state changes is important (financial apps — relevant for Citi!)  
**Choose BehaviorSubject service when:**
- State is local to a single feature or small set of components
- Team is small, velocity matters more than rigidity
- The overhead of actions/reducers/effects isn't justified  
**At TIAA:** A BehaviorSubject service likely sufficed for Request Summary. For a Citi-scale operational dashboard (Ops Hub style), NgRx would be the right call for auditability and maintainability.

---

**Q5 🟡** How do you handle HTTP errors globally in Angular using an Interceptor?

**Answer:**  
Implement the `HttpInterceptor` interface:
```typescript
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      retry(1),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // redirect to login or refresh token
        } else if (error.status === 403) {
          // show access denied message
        } else if (error.status >= 500) {
          // log to Splunk, show generic error toast
        }
        return throwError(() => error);
      })
    );
  }
}
```
Register in `AppModule` providers: `{ provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }`.  
Also use interceptors for: attaching JWT tokens to outgoing requests, adding correlation IDs for distributed tracing, logging request/response timing.

---

**Q6 🟡** What is the difference between `Subject`, `BehaviorSubject`, `ReplaySubject`, and `AsyncSubject`?

**Answer:**  
- `Subject` — multicast observable. New subscribers only receive future emissions — no replay of past values.
- `BehaviorSubject` — stores the current value. New subscribers immediately receive the latest value. Requires an initial value. Most common for state management services.
- `ReplaySubject(n)` — replays the last n emissions to new subscribers. Useful when late subscribers need history (e.g., recent notifications).
- `AsyncSubject` — only emits the last value, and only after the observable completes. Rarely used; suits HTTP-like one-shot scenarios.  
**Rule of thumb:** Use `BehaviorSubject` for state, `Subject` for events, `ReplaySubject` for caching recent values.

---

**Q7 🟡** How do you optimise Angular bundle size for a production deployment?

**Answer:**  
- `ng build --configuration production` — enables AOT compilation, tree-shaking (removes unused code), minification, and dead code elimination
- Lazy loading feature modules — don't load everything upfront
- `trackBy` in `*ngFor` — prevents unnecessary DOM re-creation
- `OnPush` change detection — reduces render cycles
- Avoid importing entire libraries — import only what you need: `import { debounceTime } from 'rxjs/operators'` not `import * as rxjs from 'rxjs'`
- Analyse bundle with `webpack-bundle-analyzer` — identify large dependencies
- Use Angular CDK virtual scrolling for large lists instead of rendering all rows  
**Your story:** Connect this to the 40% AppDex improvement — production build optimisations likely played a role.

---

**Q8 🟢** What is the `async` pipe and why is it preferred over manual subscription?

**Answer:**  
The `async` pipe subscribes to an Observable or Promise in the template and returns the latest value. It automatically unsubscribes when the component is destroyed — preventing memory leaks.  
Without `async` pipe, you must manually subscribe in `ngOnInit` and unsubscribe in `ngOnDestroy` (easy to forget). With `async` pipe, Angular handles the full lifecycle. Also works perfectly with `OnPush` change detection — the pipe calls `markForCheck()` when a new value arrives.  
```html
<!-- preferred -->
<div *ngIf="user$ | async as user">{{ user.name }}</div>
```

---

# Round 4 — System Design & Architecture
**Format:** 60 min · Principal/Lead engineer · Whiteboard-style  
**Focus:** Scalability, security, availability — financial domain thinking  

---

**Q1 🔴** Design a request tracking system (like your TIAA "Request Summary") handling 10,000 concurrent users.

**Answer framework:**

**Clarify first:**
- Read-heavy or write-heavy? (tracking = mostly reads)
- SLA? (e.g., p95 < 500ms)
- Data retention? (regulatory requirements at a bank)
- Real-time updates needed? (WebSocket vs polling)

**Architecture:**
```
Client (Angular SPA) 
  → CDN (static assets) 
  → API Gateway (Kong/Zuul) — auth, rate limiting, routing
  → Microservices:
      - Request Service (Spring Boot) — CRUD on requests
      - Notification Service — status change events
      - Audit Service — compliance log
  → Databases:
      - PostgreSQL (relational request data, transactional)
      - Redis (session cache, recent requests cache, rate limit counters)
      - Kafka (async event streaming — status updates, notifications)
  → Observability:
      - Splunk (centralised logging — you use this)
      - Prometheus + Grafana (metrics)
      - Distributed tracing (Zipkin/Jaeger)
```

**Scaling:**
- Horizontal pod autoscaling on OpenShift/Kubernetes based on CPU/RPS
- DB read replicas for read-heavy traffic
- Connection pooling (HikariCP in Spring Boot)
- Caching frequently-read request summaries in Redis with TTL

**Security:**
- JWT validation at API Gateway
- All internal service communication over mTLS
- PII fields encrypted at rest

---

**Q2 🔴** Design the compliance-focused record restriction feature for region-based access control (your TIAA experience).

**Answer:**

**Problem:** Users in Region A must never see data belonging to Region B, even if they share the same application.

**Access control model:**
- Use **Attribute-Based Access Control (ABAC)** rather than simple RBAC — more flexible for complex region/role combinations
- JWT claims carry: `userId`, `roles[]`, `region`, `permissions[]`
- Spring Security `@PreAuthorize("@accessService.canAccess(#recordId, authentication)")` on service methods

**Database layer:**
- Every record has a `region_code` column
- All queries include `WHERE region_code = :userRegion` enforced at the repository level
- Use Spring Data JPA Specifications or QueryDSL to inject this filter automatically
- Consider PostgreSQL Row-Level Security (RLS) as a database-enforced safety net

**Audit log:**
- Every data access attempt (success and failure) written to an immutable audit table
- Includes: userId, timestamp, recordId, region, action, outcome
- This is a compliance requirement at banks — mention it proactively

**Edge cases to discuss:**
- User's region changes mid-session → invalidate their JWT, force re-authentication
- Admin user who can access all regions → explicit `ALL_REGIONS` permission claim, logged separately
- Cross-region reporting → separate privileged service with its own audit trail

---

**Q3 🔴** Walk me through a CI/CD pipeline for a Spring Boot + Angular microservice deployed on OpenShift.

**Answer:**

```
1. Developer pushes code to Git (feature branch)
2. Pull Request → automated checks trigger:
   - Maven/Gradle compile
   - Unit tests (JUnit, Mockito)
   - Integration tests
   - SonarQube static analysis → quality gate (coverage threshold, no critical issues)
   
3. PR merged to main → Jenkins pipeline kicks off:
   - Build: mvn clean package / ng build --prod
   - Docker image built and tagged (commit SHA)
   - Image pushed to internal container registry
   
4. Deploy to DEV (automatic):
   - OpenShift rolling update — zero downtime
   - Smoke tests run against DEV
   
5. Deploy to UAT (automatic after DEV passes):
   - Regression test suite
   - Performance baseline check
   
6. Deploy to PROD (manual approval gate):
   - Change management ticket (ITSM) approved
   - OpenShift rolling update
   - Canary or blue-green deployment (route 10% traffic to new version)
   - Splunk alert watching error rate and latency post-deploy
   - Auto-rollback if error spike detected
```

**Tools you've used at TIAA:** Jenkins, Git, OpenShift, Splunk — mention them explicitly.

---

**Q4 🟡** When would you use MuleSoft vs direct REST APIs? (Based on your TIAA experience)

**Answer:**  
**Use MuleSoft when:**
- Integrating with legacy systems that don't expose REST (SOAP, FTP, JMS, SAP, mainframe)
- Complex data transformation between systems (e.g., converting a bank's proprietary format to JSON)
- Enterprise-wide API governance — MuleSoft acts as an API hub with policies, rate limiting, analytics
- Pre-built connectors needed for Salesforce, SAP, Workday — no custom code required
- Multiple protocol mediation in one place  
**Use direct REST when:**
- Both services speak HTTP/JSON — no transformation needed
- Latency is critical — MuleSoft adds a network hop
- Simple point-to-point integration between microservices in the same ecosystem
- Cost/complexity of a full MuleSoft license isn't justified  
**At TIAA (Ops Hub):** MuleSoft made sense for Salesforce integration because Salesforce has a pre-built MuleSoft connector, complex data mapping was needed, and the team already had the platform.

---

**Q5 🟡** How would you handle a production outage in one of your microservices without bringing down the whole system?

**Answer:**  
This tests resilience design thinking — key for a bank.

**Circuit Breaker pattern (Resilience4j):**
- Wrap outgoing service calls in a circuit breaker
- If failure rate exceeds threshold (e.g., 50% in last 10 calls), the circuit "opens" and fast-fails instead of waiting for timeout
- After a wait period, allows a test call through ("half-open") — if it succeeds, circuit closes

**Fallback strategies:**
- Return cached data if the downstream service is down
- Return a degraded response (e.g., show request list without status details)
- Queue the operation and process it when the service recovers

**Bulkhead pattern:**
- Isolate thread pools per downstream service — a slow service doesn't exhaust threads for healthy services

**At Citi context:** If the Notification Service goes down, Request Service should still allow users to view and submit requests — just disable real-time notifications gracefully.

**Observability:**
- Splunk alert fires on error spike
- Runbook documented in Confluence — on-call engineer follows steps
- Post-incident review within 24 hours — root cause documented

---

# Round 5 — HR & Behavioural
**Format:** 30–45 min · HR / hiring manager  
**Method:** STAR — Situation · Task · Action · Result  

---

**Q1 🟢** Tell me about yourself and why you're interested in this role at Citi.

**Answer structure (90 seconds):**  
"I'm a full-stack developer with 6+ years of experience building enterprise applications in Java, Spring Boot, and Angular — primarily in the financial services domain at TIAA. In my current role, I've led feature development that improved application performance by 50%, raised our AppDex score by 40%, and built compliance-focused access control systems. I've also worked with Salesforce, MuleSoft, and OpenShift in production environments.  
I'm drawn to Citi because of the scale and complexity of the problems — building systems that serve global banking operations is a different challenge from anything I could work on in a product startup. My domain experience in financial services means I understand the compliance, reliability, and security expectations from day one. I'm excited about the opportunity to work on systems at Citi's scale and grow toward a technical leadership role."

---

**Q2 🟡** Describe a time you significantly improved application performance. What was your approach and how did you measure success?

**STAR answer:**  
- **S:** Our enterprise Angular + Spring Boot application was suffering from slow load times, directly affecting user productivity metrics tracked via AppDex.
- **T:** I was tasked with identifying and resolving the performance bottlenecks without a full rewrite, on a tight timeline.
- **A:** I started by profiling — used Splunk to identify slow API endpoints and Chrome DevTools to analyse frontend bundle size and render times. I implemented server-side caching with configurable TTL for high-frequency read endpoints, enabled gzip compression on the Spring Boot server, and rebuilt the Angular app with production optimisations (AOT, lazy loading, OnPush). I also added HTTP response caching headers for static data.
- **R:** Load time reduced by 50%. AppDex score improved by 40%. These were tracked over two sprint cycles after release and reported to stakeholders.

---

**Q3 🟡** Tell me about a time you mentored a junior developer. What was the outcome?

**STAR answer:**  
- **S:** A junior developer joined the team mid-project and was struggling with Angular's reactive forms and RxJS patterns — causing delays in their assigned features.
- **T:** As the senior developer on the team, I took ownership of getting them up to speed without blocking the sprint.
- **A:** I scheduled 30-minute pair programming sessions twice a week, focused on the exact patterns they'd encounter in our codebase. I also started leaving detailed, educational code review comments — not just "fix this" but "here's why and here's the pattern we use." I assigned them progressively more complex tasks with check-ins.
- **R:** Within 6 weeks they were independently delivering features with minimal rework. Their PR approval rate improved significantly. The junior developer later said those sessions were the most impactful learning experience in their first year. This also freed up my time from rework reviews.

---

**Q4 🟡** How do you handle disagreements with product managers or business stakeholders on requirements?

**Answer:**  
My first step is always to understand their motivation, not just their ask. Often a business stakeholder is describing a solution when what they actually have is a problem — and there may be a better technical path to the same outcome.  
I approach it by: listening fully first, asking clarifying questions, then bringing data — load tests, architectural diagrams, or precedent from similar systems. I frame pushback as "here's the risk I see and here's an alternative" rather than a flat refusal.  
At TIAA, I regularly participated in architecture discussions with business teams. When a requested feature would have broken our access control model, I documented the compliance risk, proposed an alternative implementation, and we reached a solution that met the business need without the security gap. I escalate only when I've exhausted alternatives — and I always document the final decision regardless.

---

**Q5 🟡** Describe a situation where you had to learn something new quickly to deliver a project.

**STAR answer:**  
- **S:** We were tasked with integrating the Ops Hub dashboard with Salesforce using MuleSoft — a platform I hadn't worked with before.
- **T:** I needed to understand MuleSoft well enough to design and implement the API integration within the sprint timeline.
- **A:** I dedicated the first two days to MuleSoft documentation, completed the key Trailhead modules, and set up a local sandbox. I reached out to a colleague with prior MuleSoft experience for a one-hour knowledge transfer session. I then built a proof-of-concept integration, got feedback early, and iterated.
- **R:** The integration was delivered within the sprint. I also documented what I learned in Confluence for the team. This became a reference for two subsequent MuleSoft integrations done by other team members.

---

**Q6 🟡** Tell me about a time you handled a production incident. What was your process?

**STAR answer:**  
- **S:** A critical API endpoint in our request management system started returning 500 errors in production, affecting a significant number of users during business hours.
- **T:** As the on-call engineer, I had to identify the root cause and restore service as quickly as possible.
- **A:** I first checked Splunk for error logs — identified a NullPointerException in a service that had been deployed earlier that day. I quickly correlated the deploy time with the spike in errors. I rolled back the deployment on OpenShift immediately, which restored service within 10 minutes. I then reproduced the issue in UAT — a null check was missing for an optional field that some records didn't have. Fixed the bug, added a unit test covering that case, and redeployed through the proper pipeline.
- **R:** Total user-facing downtime was under 15 minutes. I documented the incident timeline, root cause, and corrective actions. We added a pre-deploy data validation check to prevent similar misses.

---

**Q7 🟢** Where do you see yourself in 3 years, and how does the Citi role fit?

**Answer:**  
In 3 years I want to be in a technical lead or architect role — someone who drives technical decisions across a team, not just delivers features. I want to go deeper on distributed systems, cloud-native architecture, and the kind of compliance-critical engineering that financial services demands.  
Citi is the right place for that trajectory. The scale here — systems serving millions of transactions, global regulatory requirements, teams working across geographies — is the environment where I'll develop the breadth and depth I'm aiming for. I also want to formalise cloud skills (AWS or GCP certification) in that timeframe, and eventually contribute to hiring and growing engineers on the team.

---

**Q8 🟢** Why are you leaving TIAA after 6 years?

**Answer:**  
I've had a genuinely strong run at TIAA — I'm proud of what I've built there. But after 6 years at the same organisation, I've reached a point where I've delivered the major initiatives available to me at my current level. I'm not leaving because of dissatisfaction — I'm leaving because I'm ready for a larger challenge, a new environment, and the kind of scale that an organisation like Citi offers. This is a natural career progression, not a flight from a problem.

---

## Quick reference — key numbers to remember

| Achievement | Metric |
|---|---|
| Load time improvement | 50% reduction |
| AppDex score improvement | 40% improvement |
| Experience | 6+ years |
| Current role | Senior Analyst, TIAA India |
| Domain | Financial services / enterprise ops |
| Key stack | Java 11/17, Spring Boot, Angular 8+, Microservices, Salesforce, MuleSoft, Jenkins, OpenShift, Splunk |

---

## Preparation checklist

- [ ] Practice the "Tell me about yourself" answer out loud — target 90 seconds
- [ ] Re-read your own TIAA project details — be specific, not vague
- [ ] Revise Spring AOP proxy internals (Transactional, Async pitfalls)
- [ ] Practice RxJS operator use-cases with real examples
- [ ] Draw the system design for your Request Summary project on paper
- [ ] Prepare 2–3 questions to ask the interviewer at the end of each round
- [ ] Review your caching implementation details from the 50% improvement project

---

## Questions to ask the interviewer (round-end)

1. "What does the tech stack look like day-to-day — are you already on Spring Boot 3 and Java 17, or is migration ongoing?"
2. "How does the team approach production incidents — do you have a blameless culture and structured post-mortems?"
3. "What does career progression from this band look like at Citi Pune — what does the path to a tech lead role look like?"
4. "Is the team building new systems from scratch or primarily maintaining and modernising existing ones?"

---

*Prepared for Chaitanya Mhatre · Citi Full Stack Developer (Java & Angular) · Pune · June 2026*
