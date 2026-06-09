# PART 2 — SPRING BOOT (Deep)

---

## Q9 🔴 Walk me through how Spring Boot auto-configuration works — every step, not just the annotations.

**Answer:**

Let's trace exactly what happens when you annotate your main class with `@SpringBootApplication` and call `SpringApplication.run()`. Most candidates recite annotation names — the question is asking for the mechanism underneath.

**Step 1: `@SpringBootApplication` decomposes into three annotations**

```java
@SpringBootApplication
public class MyApp {
    public static void main(String[] args) {
        SpringApplication.run(MyApp.class, args);
    }
}

// @SpringBootApplication is shorthand for ALL THREE of these:
@SpringBootConfiguration    // = @Configuration — marks class as bean source (Java config class)
@EnableAutoConfiguration    // ← this is the one that triggers auto-configuration
@ComponentScan              // scans com.example.myapp and all sub-packages for @Component etc.
```

**Step 2: What `@EnableAutoConfiguration` actually does**

It is meta-annotated with:
```java
@Import(AutoConfigurationImportSelector.class)
```

`AutoConfigurationImportSelector` implements Spring's `ImportSelector` interface. During context refresh, Spring calls `selectImports()` on it. This method returns a list of fully-qualified class names — Spring then registers all of them as configuration classes. This is the core mechanism: auto-configuration classes are just regular `@Configuration` classes that are discovered and imported automatically.

**Step 3: Where the candidate list comes from**

`selectImports()` reads a file bundled inside `spring-boot-autoconfigure.jar`:

In **Spring Boot 2.x:**
```
META-INF/spring.factories
key: org.springframework.boot.autoconfigure.EnableAutoConfiguration
value: list of 130+ AutoConfiguration class names
```

In **Spring Boot 3.x** (new location):
```
META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
(one class name per line)
```

Every starter you add to your `pom.xml` can bundle its own entries in this file. When you add `spring-boot-starter-data-jpa`, its `spring-boot-autoconfigure.jar` contributes `JpaAutoConfiguration` to the list.

**Step 4: Each auto-configuration class is conditional — most will be skipped**

The entire system depends on conditions. Here is what `DataSourceAutoConfiguration` actually looks like:

```java
@AutoConfiguration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
// Only activate if DataSource class is on the classpath.
// You add spring-boot-starter-jdbc → HikariCP → DataSource.class present → condition passes.
// You don't add any JDBC dependency → DataSource.class absent → entire class skipped.

@ConditionalOnMissingBean(type = "io.r2dbc.spi.ConnectionFactory")
// Don't activate if R2DBC (reactive) is being used instead of JDBC

@Import({ DataSourcePoolMetadataProvidersConfiguration.class,
          DataSourceInitializationConfiguration.class })
public class DataSourceAutoConfiguration {
    
    @ConditionalOnMissingBean({ DataSource.class, XADataSource.class })
    // ← THIS IS THE KEY ONE. Only create a DataSource bean if YOU haven't already defined one.
    // This is the "back-off" mechanism. Your beans always take priority.
    @Import({ EmbeddedDataSourceConfiguration.class,
              DataSourceConfiguration.Hikari.class,
              DataSourceConfiguration.Tomcat.class })
    static class PooledDataSourceConfiguration { ... }
}
```

**The full set of condition annotations and what each checks:**

| Condition annotation | What it checks |
|---|---|
| `@ConditionalOnClass` | Is this class on the classpath? |
| `@ConditionalOnMissingClass` | Is this class ABSENT from the classpath? |
| `@ConditionalOnBean` | Is a bean of this type already registered? |
| `@ConditionalOnMissingBean` | Is there NO bean of this type yet? (the back-off mechanism) |
| `@ConditionalOnProperty` | Does `application.properties` have this property? With this value? |
| `@ConditionalOnResource` | Does this classpath resource exist? |
| `@ConditionalOnWebApplication` | Is this a web application? |
| `@ConditionalOnNotWebApplication` | Is this NOT a web application? |
| `@ConditionalOnExpression` | Does this SpEL expression evaluate to true? |

**Step 5: Your beans always win — the "user configuration takes priority" rule**

`@ConditionalOnMissingBean` is used throughout auto-configuration specifically so that if YOU define a bean of the same type, Spring skips the auto-configured version entirely.

```java
// You define this in any @Configuration class:
@Bean
public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource();
    ds.setJdbcUrl("jdbc:postgresql://...");
    ds.setMaximumPoolSize(20); // your custom settings
    return ds;
}

// What happens:
// DataSourceAutoConfiguration evaluates @ConditionalOnMissingBean(DataSource.class)
// → finds your DataSource bean already registered
// → condition FAILS → entire auto-configuration block skipped
// → your DataSource is used everywhere, not the auto-configured HikariCP defaults
```

**Step 6: Ordering between auto-configurations**

Auto-configurations can depend on each other:

```java
@AutoConfigureBefore(WebMvcAutoConfiguration.class)     // run me before this
@AutoConfigureAfter(DataSourceAutoConfiguration.class)  // run me after this
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)          // numeric ordering
```

This ensures that when `JpaAutoConfiguration` runs, it can rely on `DataSourceAutoConfiguration` having already provided a `DataSource` bean.

**Step 7: Debugging — how to see what was configured and what was skipped**

```properties
# application.properties
debug=true
```

This prints the **Conditions Evaluation Report** at startup:
```
============================
CONDITIONS EVALUATION REPORT
============================

Positive matches: (auto-configured)
   DataSourceAutoConfiguration matched:
      - @ConditionalOnClass found required classes 'javax.sql.DataSource' (DataSourceAutoConfiguration)

Negative matches: (not auto-configured, and why)
   WebFluxAutoConfiguration did not match:
      - @ConditionalOnClass did not find required class 'reactor.netty.http.server.HttpServer'
```

```java
// Or exclude specific auto-configurations:
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
// Or in properties:
// spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

---

## Q10 🔴 How does Spring's dependency injection and the bean lifecycle work internally — BeanFactory vs ApplicationContext, and every lifecycle phase?

**Answer:**

**BeanFactory vs ApplicationContext — what's the actual difference:**

`BeanFactory` is the root container interface. It creates beans **lazily** — only when `getBean()` is called. Very lightweight. Almost never used directly in application code.

`ApplicationContext` extends `BeanFactory` and adds:
- **Eager singleton instantiation** — ALL singleton beans created at startup (you catch wiring errors immediately, not on first use)
- `ApplicationEventPublisher` — for `@EventListener` and `publishEvent()`
- `MessageSource` — for internationalization
- `ResourceLoader` — for classpath/file resources
- Auto-detection and registration of `BeanPostProcessor` beans
- Support for `@Autowired`, `@Value`, `@PostConstruct`, `@PreDestroy` (via special BeanPostProcessors)

In practice: always use `ApplicationContext`. `BeanFactory` is for embedded/constrained environments.

**The complete bean lifecycle — every step:**

```
PHASE 1: BEAN DEFINITION SCANNING (before any beans are created)
────────────────────────────────────────────────────────────────
@ComponentScan processes:
    @Component, @Service, @Repository, @Controller → BeanDefinition created for each
@Configuration classes processed → @Bean methods registered as BeanDefinitions
Result: BeanDefinitionRegistry has all definitions, no instances yet

PHASE 2: BeanFactoryPostProcessor
────────────────────────────────────────────────────────────────
Called BEFORE any bean is instantiated.
Can MODIFY bean definitions themselves at this stage.
Most important example: PropertySourcesPlaceholderConfigurer
  → This is what resolves ${app.timeout} in @Value("${app.timeout}")
  → It runs here, before instantiation, so all ${} placeholders are resolved
     before values are injected into beans

PHASE 3: INSTANTIATION
────────────────────────────────────────────────────────────────
Constructor called.
Constructor injection happens here — injected beans must already exist.
(This is why circular dependencies in constructor injection fail —
 Bean A's constructor needs B, but B's constructor needs A — deadlock)

PHASE 4: PROPERTY POPULATION (Dependency Injection)
────────────────────────────────────────────────────────────────
@Autowired fields set via reflection
@Autowired setter methods called
@Value fields resolved and injected

PHASE 5: AWARE INTERFACES
────────────────────────────────────────────────────────────────
Spring calls these if the bean implements them:
  BeanNameAware.setBeanName(name)         — bean learns its own name in the container
  BeanFactoryAware.setBeanFactory(bf)     — gets reference to BeanFactory
  ApplicationContextAware.setApplicationContext(ctx)  — gets the full context
These are called in this order.

PHASE 6: BeanPostProcessor.postProcessBeforeInitialization()
────────────────────────────────────────────────────────────────
Called for EVERY bean, before its init method.
THIS is where @PostConstruct is processed (by CommonAnnotationBeanPostProcessor)
THIS is where @Autowired is processed (by AutowiredAnnotationBeanPostProcessor)
You can wrap or even replace the bean object here.
AOP proxy creation begins here for some advisor types.

PHASE 7: INITIALIZATION
────────────────────────────────────────────────────────────────
@PostConstruct method called — good for setup that needs injected dependencies
  (cannot do this in constructor — dependencies not injected yet)
InitializingBean.afterPropertiesSet() — programmatic alternative
@Bean(initMethod = "init") — for third-party classes you can't annotate

PHASE 8: BeanPostProcessor.postProcessAfterInitialization()
────────────────────────────────────────────────────────────────
Called AFTER init. Bean is now fully initialized.
THIS IS WHERE AOP PROXIES ARE CREATED.
@Transactional, @Async, @Cacheable — the proxy wrapping happens here.
What gets stored in the ApplicationContext is the PROXY, not your original object.
This is why @Transactional self-invocation doesn't work — see Q11.

PHASE 9: BEAN READY
────────────────────────────────────────────────────────────────
Stored in the singleton cache.
Available for injection into other beans.
This is the bean as it will exist for the lifetime of the application.

PHASE 10: SHUTDOWN (context.close() or JVM shutdown hook)
────────────────────────────────────────────────────────────────
@PreDestroy method called — cleanup (close connections, flush buffers)
DisposableBean.destroy()
@Bean(destroyMethod = "close") — for third-party beans
Note: prototype-scoped beans are NOT destroyed by Spring — caller owns lifecycle.
```

**Circular dependency and how Spring resolves it for setter/field injection:**

```java
@Service class A { @Autowired B b; }
@Service class B { @Autowired A a; }

// Spring resolves this using "early exposure" (three-level cache):
// 1. Create A instance (constructor called, no injection yet)
// 2. Put half-baked A in "early singleton cache" (singletonFactories)
// 3. Start injecting A's dependencies — needs B
// 4. Create B instance
// 5. B needs A — Spring finds half-baked A in early cache → injects it
// 6. B fully initialized
// 7. A gets B injected → A fully initialized
// 8. Early cache entry replaced with final, fully initialised A

// CONSTRUCTOR injection circular deps CANNOT be resolved this way:
// A's constructor needs B — but B doesn't exist yet — can't create A
// Spring throws BeanCurrentlyInCreationException
// This is actually good — forces you to break the circular dependency
```

---

## Q11 🔴 Explain Spring AOP — how proxying works and what are the limitations?

**Answer:**

**What AOP is for:**

Cross-cutting concerns — logging, transactions, security, caching, metrics — appear in many methods. Without AOP you manually add the same boilerplate everywhere. AOP separates this into an **Aspect** that is woven into method execution transparently.

**Core terminology:**

```
Aspect    — the class with the cross-cutting logic (e.g., TransactionAspect)
Advice    — what runs and when (@Before, @After, @AfterReturning, @AfterThrowing, @Around)
Join Point — a point in execution where advice can be applied (method call in Spring AOP)
Pointcut  — expression selecting which join points to intercept
Weaving   — process of applying aspects to target objects (Spring does this at runtime)
```

**How Spring's runtime proxy actually works:**

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Around("@annotation(Loggable)") // pointcut: methods annotated with @Loggable
    public Object logMethod(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName();
        log.info("Entering: {}", method);
        long start = System.currentTimeMillis();
        
        Object result = pjp.proceed(); // call the real method
        
        log.info("Exiting: {} took {}ms", method, System.currentTimeMillis() - start);
        return result;
    }
}
```

Spring creates a proxy object wrapping your bean. Two proxy mechanisms:

**JDK Dynamic Proxy** — used when target implements at least one interface:
```java
// Spring generates a class implementing the same interface at runtime using java.lang.reflect.Proxy
// The InvocationHandler intercepts every method call, runs advice, then delegates to real bean
UserRepository proxy = (UserRepository) Proxy.newProxyInstance(
    classLoader,
    new Class[]{UserRepository.class},
    (proxyObj, method, args) -> {
        // run @Before advice
        Object result = method.invoke(realBean, args); // call real method
        // run @After advice
        return result;
    }
);
```

**CGLIB Proxy** — used when no interface exists (Spring Boot default even for interfaces):
```java
// CGLIB generates a SUBCLASS of your bean at runtime using bytecode manipulation
// class PaymentService$$EnhancerByCGLIB$$12ab34 extends PaymentService {
//     @Override
//     public void processPayment(Payment p) {
//         // run advice
//         super.processPayment(p); // call real method
//         // run advice
//     }
// }
```

**The self-invocation limitation — this is the #1 AOP interview gotcha:**

```java
@Service
public class OrderService {
    
    @Transactional(readOnly = true)
    public Order findOrder(Long id) {
        return orderRepo.findById(id).orElseThrow();
    }
    
    public List<Order> findAll(List<Long> ids) {
        return ids.stream()
            .map(this::findOrder) // 'this' = the REAL OrderService, NOT the proxy!
            // The call bypasses the proxy entirely
            // @Transactional on findOrder is completely ignored
            .collect(Collectors.toList());
    }
}

// What Spring stores in the context:
// OrderService$$Proxy → wraps real OrderService
// External callers get the proxy → advice runs
// Internal calls (this.method()) → go directly to real object → no proxy → no advice

// Fix 1: Self-injection — inject the proxy into itself
@Service
public class OrderService {
    @Autowired
    private OrderService self; // Spring injects the PROXY here, not 'this'
    
    public List<Order> findAll(List<Long> ids) {
        return ids.stream()
            .map(self::findOrder) // goes through proxy → @Transactional fires
            .collect(Collectors.toList());
    }
}

// Fix 2 (cleaner): Move the annotated method to a separate bean
@Service
public class OrderQueryService {
    @Transactional(readOnly = true)
    public Order findOrder(Long id) { return orderRepo.findById(id).orElseThrow(); }
}

@Service
public class OrderService {
    @Autowired OrderQueryService queryService;
    
    public List<Order> findAll(List<Long> ids) {
        return ids.stream()
            .map(queryService::findOrder) // external bean call → goes through proxy → works
            .collect(Collectors.toList());
    }
}
```

**Other AOP limitations:**

```java
// 1. Cannot advise private methods — proxy can't intercept them
@Transactional // IGNORED on private
private void internalProcess() { ... }
// Fix: make it public or protected

// 2. Cannot advise final methods or final classes (CGLIB can't subclass/override)
@Service
public final class FinalService { // CGLIB fails here
    @Transactional
    public void process() { ... }
}

// 3. Works only on Spring-managed beans — @Aspect on a manually new'd object has no effect
OrderService svc = new OrderService(); // not Spring-managed
svc.processOrder(); // no AOP, even if aspects are defined
```

**Pointcut expression quick reference:**

```java
// All methods in a package
@Pointcut("execution(* com.example.service.*.*(..))")
// any return type, com.example.service package, any class, any method, any args

// All public methods
@Pointcut("execution(public * *(..))")

// Methods starting with "find" anywhere under com.example (.. = any sub-packages)
@Pointcut("execution(* com.example..*.find*(..))")

// By annotation — cleanest for selective advice
@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")

// Combine
@Pointcut("inServiceLayer() && !getterMethods()")
```

---

## Q12 🔴 How does `@Transactional` work — propagation, isolation, and the most common bugs?

**Answer:**

**The mechanism under the hood:**

`@Transactional` works entirely through AOP proxying. When the annotated method is called via the proxy:

1. `TransactionInterceptor` intercepts the call
2. `PlatformTransactionManager` checks: is there an existing transaction on this thread?
3. Based on propagation, it starts/joins/suspends a transaction
4. Your method runs
5. If method returns normally → **commit**
6. If `RuntimeException` or `Error` thrown → **rollback**
7. If checked exception thrown → **commit** (this surprises most people)

```java
@Transactional
// equivalent to:
@Transactional(
    propagation = Propagation.REQUIRED,  // join existing or create new
    isolation = Isolation.DEFAULT,       // use DB default
    readOnly = false,
    rollbackFor = {RuntimeException.class, Error.class},
    noRollbackFor = {},
    timeout = -1                         // no timeout
)
```

**Propagation levels — each one matters:**

```java
// REQUIRED (default) — join existing transaction, create one if none exists
// If methodA calls methodB and both are REQUIRED:
//   → they share ONE transaction. If either throws, BOTH roll back.
@Transactional(propagation = Propagation.REQUIRED)
public void methodB() { ... }

// REQUIRES_NEW — always create a NEW transaction, SUSPEND existing one
// Use case: audit logging that must persist even if the main operation rolls back
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void writeAuditLog(String event) {
    auditRepo.save(new AuditEntry(event));
    // Committed independently — even if caller rolls back, this audit entry persists
}

// NESTED — savepoint inside existing transaction
// If nested rolls back → rolls back to savepoint only, outer continues
// If outer rolls back → nested also rolls back (it's inside the outer)
// Supported in JDBC, not all JPA providers
@Transactional(propagation = Propagation.NESTED)
public void nestedOp() { ... }

// SUPPORTS — use existing transaction if present; run without one if not
@Transactional(propagation = Propagation.SUPPORTS)
public List<Order> findOrders() { return orderRepo.findAll(); } // fine either way

// NOT_SUPPORTED — suspend existing transaction, run without one
// Use case: sending an email or making an HTTP call — don't hold a DB transaction open
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void sendEmail(String to, String body) { emailService.send(to, body); }

// NEVER — throw if called within a transaction
@Transactional(propagation = Propagation.NEVER)
public void assertNoTransaction() { } // fails loudly — good for detecting wrong callers

// MANDATORY — must be called within an existing transaction; throw if none
@Transactional(propagation = Propagation.MANDATORY)
public void helperMethod() { } // enforces callers must open a transaction first
```

**Isolation levels:**

```java
// READ_UNCOMMITTED → can read uncommitted (dirty) data from other transactions
// Rarely used — data may be rolled back, you've read phantom data

// READ_COMMITTED (default: PostgreSQL, Oracle, SQL Server)
// Only reads committed data. Prevents dirty reads.
// Non-repeatable reads possible: same SELECT in same tx may return different rows
//   (another tx committed a change between your two reads)

// REPEATABLE_READ (default: MySQL InnoDB)
// Same SELECT returns same rows within transaction.
// Phantom reads possible: a new INSERT by another tx can add rows your range query sees

// SERIALIZABLE — full isolation. Transactions appear to run sequentially.
// Highest consistency, lowest concurrency. Heavy locking. Use sparingly.
```

**The five most common `@Transactional` bugs:**

```java
// BUG 1: Checked exception does NOT trigger rollback by default
@Transactional
public void processPayment() throws PaymentException { // checked exception
    paymentRepo.save(payment);
    throw new PaymentException("Card declined"); // COMMITS despite exception!
}
// Fix: declare rollbackFor
@Transactional(rollbackFor = PaymentException.class)
public void processPayment() throws PaymentException { ... } // now rolls back

// Or make PaymentException extend RuntimeException (better design anyway)

// BUG 2: Self-invocation bypasses proxy → no transaction
@Service
public class OrderService {
    public void placeOrder(Order order) {
        validateOrder(order);
        processOrder(order); // 'this.processOrder' → no proxy → @Transactional ignored!
    }
    
    @Transactional
    public void processOrder(Order order) { orderRepo.save(order); }
}

// BUG 3: @Transactional on private methods — silently ignored
@Transactional // proxy can't intercept private methods — annotation has no effect
private void internalProcess() { ... }

// BUG 4: @Transactional on @PostConstruct — proxy doesn't exist yet
@PostConstruct
@Transactional // IGNORED — Spring calls @PostConstruct on the REAL bean during lifecycle
public void init() {
    repo.save(initialData()); // no transaction
}
// Fix: use ApplicationReadyEvent
@EventListener(ApplicationReadyEvent.class)
@Transactional
public void initAfterStartup() { repo.save(initialData()); } // proxy exists, works

// BUG 5: readOnly = true doesn't prevent JPA dirty checking
@Transactional(readOnly = true)
public Order findOrder(Long id) {
    Order order = orderRepo.findById(id).get();
    order.setStatus("MODIFIED"); // JPA flush will write this change at transaction end!
    // readOnly is a HINT to the driver/cache, not an enforcement
    return order;
}
// Fix: use a DTO/projection, or detach the entity: entityManager.detach(order)
```

---

## Q13 🔴 How do you design and secure a REST API in Spring Boot — authentication, authorisation, common vulnerabilities?

**Answer:**

**Spring Security filter chain — what runs on every request:**

```
Incoming request
      ↓
DelegatingFilterProxy (bridges Servlet container to Spring Security)
      ↓
FilterChainProxy → your SecurityFilterChain
      ↓
SecurityContextPersistenceFilter   ← restore SecurityContext for this request
BearerTokenAuthenticationFilter    ← extract and validate JWT
ExceptionTranslationFilter         ← convert security exceptions to 401/403 responses
AuthorizationFilter                ← check URL-level permissions
      ↓
Your Controller
```

**Full JWT implementation:**

```java
// Security configuration
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // enables @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtFilter) 
            throws Exception {
        return http
            .csrf(csrf -> csrf.disable())           // stateless API — CSRF not applicable
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS)) // no session state
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")       // ROLE_ prefix auto-added
                .requestMatchers(HttpMethod.GET, "/api/orders").hasAnyRole("USER","ADMIN")
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // cost factor 12 — slow enough to defeat brute force
        // BCrypt is deliberately slow: ~250ms per hash at factor 12
        // This makes brute force infeasible — attacker can only try ~4/sec
    }
}

// JWT filter
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees exactly one execution per request
    // even in forward/include scenarios
    
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                     FilterChain chain) throws ServletException, IOException {
        String authHeader = req.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(req, res); // no JWT — pass through, will fail at authorization
            return;
        }
        
        String token = authHeader.substring(7); // strip "Bearer "
        
        try {
            String username = jwtUtil.extractUsername(token); // validates signature internally
            
            // Only set authentication if not already set — prevents re-processing
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.isTokenValid(token, userDetails)) {
                    // Create authentication object with granted authorities
                    UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                        );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    
                    // Store in SecurityContext — downstream code can call SecurityContextHolder.getContext()
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JwtException e) {
            // Expired, invalid signature, malformed — don't set authentication
            // Let ExceptionTranslationFilter return 401
            log.warn("JWT validation failed: {}", e.getMessage());
        }
        
        chain.doFilter(req, res); // always continue the chain
    }
}

// Method-level security with SpEL
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(authentication, #id)")
    // @orderSecurity refers to a Spring bean named 'orderSecurity'
    // authentication = current SecurityContext authentication
    // #id = the @PathVariable id — SpEL accesses method parameters by name
    public Order getOrder(@PathVariable Long id) { ... }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order:delete')")
    // Authority (fine-grained string) vs Role (coarse-grained, prefixed with ROLE_)
    public void deleteOrder(@PathVariable Long id) { ... }
}

// Custom security evaluator bean
@Component("orderSecurity")
public class OrderSecurityEvaluator {
    @Autowired OrderRepository orderRepo;
    
    public boolean isOwner(Authentication auth, Long orderId) {
        String username = auth.getName();
        return orderRepo.findById(orderId)
            .map(order -> order.getOwnerUsername().equals(username))
            .orElse(false);
    }
}
```

**Common API vulnerabilities:**

```java
// 1. MASS ASSIGNMENT — never bind request body directly to your entity
@PostMapping
public User create(@RequestBody User user) { // attacker sends: {"role":"ADMIN"} in JSON
    return userRepo.save(user); // VULNERABLE — they just made themselves admin
}
// Fix: use a DTO
@PostMapping
public User create(@RequestBody CreateUserRequest req) {
    User user = new User();
    user.setUsername(req.getUsername());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setRole(Role.USER); // role ALWAYS set server-side, never from request
    return userRepo.save(user);
}

// 2. SENSITIVE DATA EXPOSURE
@Entity
public class User {
    @JsonIgnore // never serialized in API responses
    private String passwordHash;
    
    @JsonIgnore
    private String mfaSecret;
}
// Alternatively: use a separate UserResponse DTO that only contains safe fields

// 3. CORS — never use wildcard in production
// Wildcard (*) with credentials is a security error — browsers block it
http.cors(cors -> cors.configurationSource(req -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com")); // explicit whitelist only
    config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    config.setAllowCredentials(true);
    config.setAllowedHeaders(List.of("Authorization","Content-Type"));
    return config;
}));

// 4. INSECURE DIRECT OBJECT REFERENCE (IDOR)
// Always verify ownership before returning data
@GetMapping("/{id}")
public Order getOrder(@PathVariable Long id, Authentication auth) {
    Order order = orderRepo.findById(id).orElseThrow();
    if (!order.getOwnerUsername().equals(auth.getName())) {
        throw new AccessDeniedException("Not your order");
    }
    return order;
}

// 5. INPUT VALIDATION — use Bean Validation
@PostMapping
public Order createOrder(@RequestBody @Valid OrderRequest request) { ... }

public record OrderRequest(
    @NotBlank String description,
    @NotNull @Positive BigDecimal amount,
    @NotNull @Future LocalDate deliveryDate,
    @Size(max = 50) @Pattern(regexp = "[A-Z]{2}") String currencyCode
) {}
// @Valid triggers validation — returns 400 with error details on failure
```

---

## Q14 🟡 What is Spring Data JPA? Explain query derivation, `@Query`, `Specification`, and projections.

**Answer:**

**Query derivation — Spring parses your method names into JPQL:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // findBy + property + operator + combinator
    // → SELECT u FROM User u WHERE u.username = ?1 AND u.email = ?2
    Optional<User> findByUsernameAndEmail(String username, String email);
    
    // Top, First for limiting
    List<User> findTop5ByDepartmentOrderBySalaryDesc(String dept);
    
    // Count, Exists, Delete variants
    long countByDepartment(String dept);
    boolean existsByEmail(String email);
    
    @Modifying @Transactional
    long deleteByLastLoginAtBefore(LocalDateTime cutoff);
    
    // Nested property traversal with underscore to disambiguate
    List<User> findByAddress_City(String city);
    // → WHERE u.address.city = ?1
    
    // Operators: Between, LessThan, GreaterThan, Like, Containing, In, IsNull
    List<User> findBySalaryBetween(BigDecimal min, BigDecimal max);
    List<User> findByNameContainingIgnoreCase(String part); // wraps with % and lowercases
    List<User> findByRoleIn(List<Role> roles);
    List<User> findByDeletedAtIsNull();
    
    // Pagination and sorting
    Page<User> findByDepartment(String dept, Pageable pageable);
    // Usage: repo.findByDepartment("Engineering", PageRequest.of(0, 20, Sort.by("name")))
    // Returns: Page<User> with totalElements, totalPages, content
}
```

**`@Query` — custom JPQL or native SQL:**

```java
// JPQL (entity-based, database-portable)
@Query("SELECT u FROM User u WHERE u.salary > :minSalary AND u.department = :dept")
List<User> findHighEarners(@Param("minSalary") BigDecimal min, @Param("dept") String dept);

// JOIN FETCH — solves N+1 problem by loading associations in one query
@Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
Optional<User> findByIdWithOrders(@Param("id") Long id);
// Without JOIN FETCH: 1 query for User + N queries for orders = N+1 problem
// With JOIN FETCH: 1 query joining users and orders tables

// Native SQL — when JPQL can't express it (DB-specific functions, complex joins)
@Query(value = "SELECT * FROM users WHERE LOWER(name) = LOWER(:name)", nativeQuery = true)
List<User> findByNameCaseInsensitive(@Param("name") String name);

// Modifying queries — for UPDATE/DELETE
@Modifying
@Transactional
@Query("UPDATE User u SET u.status = :status WHERE u.lastLoginAt < :cutoff")
int deactivateInactiveUsers(@Param("status") String status, @Param("cutoff") LocalDateTime cutoff);
// @Modifying is required — tells Spring this isn't a SELECT
// Returns int — count of affected rows
// @Transactional required — Spring Data doesn't add it automatically for @Modifying
```

**`Specification` — dynamic, composable queries for optional filters:**

```java
// When you have a search API where any filter may or may not be provided
public class UserSpec {
    
    public static Specification<User> hasName(String name) {
        // Return null = no restriction (ignored in WHERE clause)
        return (root, query, cb) -> name == null ? null
            : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
    
    public static Specification<User> hasDepartment(String dept) {
        return (root, query, cb) -> dept == null ? null
            : cb.equal(root.get("department"), dept);
    }
    
    public static Specification<User> salaryBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("salary"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("salary"), min);
            return cb.between(root.get("salary"), min, max);
        };
    }
}

// Combine dynamically
public List<User> search(UserSearchRequest req) {
    Specification<User> spec = Specification
        .where(UserSpec.hasName(req.getName()))         // null filters are ignored
        .and(UserSpec.hasDepartment(req.getDepartment()))
        .and(UserSpec.salaryBetween(req.getMinSalary(), req.getMaxSalary()));
    
    return userRepo.findAll(spec, Sort.by("name")); // repo extends JpaSpecificationExecutor<User>
}
```

**Projections — fetch only the columns you need:**

```java
// Interface projection — Spring generates implementation via reflection
public interface UserSummary {
    String getName();
    String getEmail();
    // Spring generates SELECT u.name, u.email FROM users — not SELECT *
    
    default String getDisplayName() { return getName() + " <" + getEmail() + ">"; }
}

// DTO projection via record (Java 16+) — maps constructor arguments
public record UserDto(String name, String email) {}

// In repository
List<UserSummary> findByDepartment(String dept);
List<UserDto> findByRole(Role role);

// JPQL with interface projection — alias must match interface method name
@Query("SELECT u.name as name, u.email as email FROM User u WHERE u.salary > :min")
List<UserSummary> findHighEarnerSummaries(@Param("min") BigDecimal min);
// 'as name' maps to getName(), 'as email' maps to getEmail()
```

---
