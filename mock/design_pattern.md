
## Creational · Structural · Behavioural · Real-World Usage

> **What interviewers want:**
> - Know WHEN to use a pattern, not just how to implement it
> - Show real-world examples from your own experience
> - Know the tradeoffs — every pattern has a cost
> - Know anti-patterns: when NOT to use a pattern

---

# CREATIONAL PATTERNS

---

## Q1 🔴 Singleton — implementation, thread safety, and when NOT to use it.

**Answer:**

Singleton ensures only one instance of a class exists across the application. Used for: database connection pools, configuration, logging, caches, ATM machine state.

**Why naive implementation breaks in multi-threaded environments:**

```java
// BROKEN — double-checked without volatile is still broken
public class BadSingleton {
    private static BadSingleton instance;

    private BadSingleton() {}

    public static BadSingleton getInstance() {
        if (instance == null) {              // Thread A and B both see null
            instance = new BadSingleton();   // Both create instances — not singleton!
        }
        return instance;
    }
}
```

**Thread-safe implementations:**

```java
// Option 1: Eager initialisation — simplest, thread-safe by JVM class loading guarantee
// JVM loads a class exactly once, initialises static fields in a thread-safe manner
public class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton(); // created at class load

    private EagerSingleton() {}

    public static EagerSingleton getInstance() { return INSTANCE; }
    // Downside: instance created even if never used (eager)
}

// Option 2: Synchronized getInstance — thread-safe but locks on every call (expensive)
public class SynchronizedSingleton {
    private static SynchronizedSingleton instance;
    private SynchronizedSingleton() {}

    public static synchronized SynchronizedSingleton getInstance() {
        if (instance == null) instance = new SynchronizedSingleton();
        return instance;
    }
    // Downside: every call acquires lock — performance bottleneck in high-concurrency
}

// Option 3: Double-Checked Locking with volatile (most common in production)
public class DCLSingleton {
    private static volatile DCLSingleton instance; // volatile is CRITICAL here
    private DCLSingleton() {}

    public static DCLSingleton getInstance() {
        if (instance == null) {              // Check 1: fast path, no lock if already created
            synchronized (DCLSingleton.class) {
                if (instance == null) {      // Check 2: inside lock — prevents double creation
                    instance = new DCLSingleton();
                }
            }
        }
        return instance;
    }
    // volatile prevents instruction reordering:
    // Without volatile, another thread might see a partially constructed object
    // (reference assigned before constructor completes — CPU reordering)
}

// Option 4: Bill Pugh / Initialization-on-demand holder — BEST practice
// Exploits JVM class loading guarantee — inner class only loaded when first referenced
public class BillPughSingleton {
    private BillPughSingleton() {}

    private static class Holder {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
        // Created only when Holder class is loaded — which happens only when getInstance() is called
        // JVM guarantees thread-safe class initialisation
    }

    public static BillPughSingleton getInstance() { return Holder.INSTANCE; }
    // Thread-safe, lazy, no synchronization overhead — best of all worlds
}

// Option 5: Enum Singleton — safest (handles serialization, reflection attacks)
public enum EnumSingleton {
    INSTANCE;

    private int value;
    public int getValue() { return value; }
    public void setValue(int v) { this.value = v; }
}
// EnumSingleton.INSTANCE — guaranteed unique by JVM
// Deserialization returns the same enum constant — no new instance
// Reflection: cannot call private constructor on enum
```

**Defeating singleton and defenses:**

```java
// Attack 1: Reflection
Constructor<DCLSingleton> c = DCLSingleton.class.getDeclaredConstructor();
c.setAccessible(true);
DCLSingleton second = c.newInstance(); // new instance — singleton broken!

// Defense: check in constructor
private DCLSingleton() {
    if (instance != null) throw new RuntimeException("Use getInstance()");
}

// Attack 2: Serialization/Deserialization
// Deserializing a singleton creates a NEW instance
// Defense: implement readResolve()
protected Object readResolve() { return getInstance(); } // deserialization returns existing instance
```

**When NOT to use Singleton:**

- When it holds mutable global state — makes testing impossible (can't reset between tests)
- When it creates tight coupling — singletons are hard dependencies
- When you need multiple instances later — it's a design trap
- Prefer dependency injection over singleton service locator — Spring beans are singletons but injected, not globally accessed

---

## Q2 🔴 Factory Method vs Abstract Factory vs Builder — when to use each.

**Answer:**

**Factory Method — subclasses decide which class to instantiate:**

```java
// Problem: Creating objects, but the exact type depends on context
// Solution: Define a factory method in the base class, override in subclasses

public abstract class NotificationSender {
    // Factory Method — subclasses override this
    protected abstract Notification createNotification(String recipient, String message);

    // Template method — uses the factory method
    public void send(String recipient, String message) {
        Notification notification = createNotification(recipient, message); // polymorphic creation
        notification.validate();
        notification.send();
        notification.log();
    }
}

public class EmailSender extends NotificationSender {
    @Override
    protected Notification createNotification(String recipient, String message) {
        return new EmailNotification(recipient, message); // concrete type decided here
    }
}

public class SmsSender extends NotificationSender {
    @Override
    protected Notification createNotification(String recipient, String message) {
        return new SmsNotification(recipient, message);
    }
}

// Usage:
NotificationSender sender = new EmailSender();
sender.send("alice@example.com", "Your order shipped!");
// Caller doesn't know or care about EmailNotification — it works with any NotificationSender
```

**Abstract Factory — family of related objects, no concrete classes in client:**

```java
// Problem: UI components must be consistent (all Windows-style or all Mac-style)
// Solution: Factory interface for creating a FAMILY of related objects

public interface UIFactory {
    Button createButton();
    TextField createTextField();
    Dialog createDialog();
}

public class WindowsUIFactory implements UIFactory {
    @Override public Button createButton() { return new WindowsButton(); }
    @Override public TextField createTextField() { return new WindowsTextField(); }
    @Override public Dialog createDialog() { return new WindowsDialog(); }
}

public class MacUIFactory implements UIFactory {
    @Override public Button createButton() { return new MacButton(); }
    @Override public TextField createTextField() { return new MacTextField(); }
    @Override public Dialog createDialog() { return new MacDialog(); }
}

// Client works with abstract factory — no concrete types
public class Application {
    private final UIFactory factory;
    private Button button;
    private Dialog dialog;

    public Application(UIFactory factory) { this.factory = factory; }

    public void buildUI() {
        button = factory.createButton();  // Windows or Mac, doesn't matter
        dialog = factory.createDialog();  // consistent — same family
        button.render();
        dialog.render();
    }
}

// Wiring (at startup):
UIFactory factory = System.getProperty("os.name").contains("Windows")
    ? new WindowsUIFactory() : new MacUIFactory();
Application app = new Application(factory);
```

**Builder — constructing complex objects step by step:**

```java
// Problem: Constructor with 10 parameters — hard to read, easy to mix up argument order
// Solution: Separate construction from representation, fluent step-by-step API

public class HttpRequest {
    private final String url;          // required
    private final HttpMethod method;   // required
    private final Map<String, String> headers;
    private final String body;
    private final int timeoutMs;
    private final boolean followRedirects;
    private final int maxRetries;

    // Private constructor — only Builder can create
    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.body = builder.body;
        this.timeoutMs = builder.timeoutMs;
        this.followRedirects = builder.followRedirects;
        this.maxRetries = builder.maxRetries;
    }

    public static class Builder {
        // Required
        private final String url;
        private final HttpMethod method;
        // Optional with defaults
        private Map<String, String> headers = new HashMap<>();
        private String body;
        private int timeoutMs = 5000;
        private boolean followRedirects = true;
        private int maxRetries = 0;

        public Builder(String url, HttpMethod method) { // required params in constructor
            this.url = Objects.requireNonNull(url);
            this.method = Objects.requireNonNull(method);
        }

        public Builder header(String name, String value) { this.headers.put(name, value); return this; }
        public Builder body(String body) { this.body = body; return this; }
        public Builder timeout(int ms) { this.timeoutMs = ms; return this; }
        public Builder followRedirects(boolean f) { this.followRedirects = f; return this; }
        public Builder maxRetries(int retries) { this.maxRetries = retries; return this; }

        public HttpRequest build() {
            // Validate here — before creating the object
            if (method == HttpMethod.GET && body != null) {
                throw new IllegalStateException("GET requests cannot have a body");
            }
            return new HttpRequest(this);
        }
    }

    // Getters only — immutable after build
    public String getUrl() { return url; }
    public HttpMethod getMethod() { return method; }
}

// Usage:
HttpRequest request = new HttpRequest.Builder("https://api.example.com/users", HttpMethod.POST)
    .header("Authorization", "Bearer token123")
    .header("Content-Type", "application/json")
    .body("{\"name\":\"Alice\"}")
    .timeout(10000)
    .maxRetries(3)
    .build();
// Clear, readable, self-documenting, validated
```

**When to use which:**

| Pattern | Use when |
|---|---|
| Factory Method | Subclasses need to decide which class to create |
| Abstract Factory | Need a family of related objects, all same "theme" |
| Builder | Object has many optional parameters, complex construction, or needs validation |

---

## Q3 🔴 Prototype Pattern — deep vs shallow copy, and real Java examples.

**Answer:**

Prototype creates new objects by copying an existing one (the prototype). Avoids the cost of initialisation — useful when creating an object from scratch is expensive (database lookup, network call, heavy computation).

```java
public interface Prototype<T> {
    T clone();
}

// Example: Cell in a spreadsheet — cloning is cheaper than re-creating
public class SpreadsheetCell implements Prototype<SpreadsheetCell>, Cloneable {
    private String formula;
    private CellFormat format;       // mutable object — must deep copy
    private List<String> comments;   // mutable list — must deep copy

    public SpreadsheetCell(String formula, CellFormat format) {
        this.formula = formula;
        this.format = format;
        this.comments = new ArrayList<>();
    }

    // Shallow copy — format and comments are shared references (WRONG for mutable objects)
    public SpreadsheetCell shallowCopy() {
        try {
            return (SpreadsheetCell) super.clone(); // Object.clone() is shallow
        } catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
    }

    // Deep copy — every mutable object is also copied
    @Override
    public SpreadsheetCell clone() {
        SpreadsheetCell copy = new SpreadsheetCell(this.formula, this.format.deepCopy());
        copy.comments = new ArrayList<>(this.comments); // new list with copied contents
        return copy;
    }
    // Now: modifying copy.format does NOT affect original.format
}

// Prototype registry — manage a library of prototypes
public class CellTemplateRegistry {
    private final Map<String, SpreadsheetCell> templates = new HashMap<>();

    public void register(String name, SpreadsheetCell template) {
        templates.put(name, template);
    }

    public SpreadsheetCell create(String name) {
        SpreadsheetCell template = templates.get(name);
        if (template == null) throw new IllegalArgumentException("No template: " + name);
        return template.clone(); // return a deep copy — original template unmodified
    }
}

// Usage:
CellTemplateRegistry registry = new CellTemplateRegistry();
registry.register("currency", new SpreadsheetCell("=SUM(A1:A10)", new CurrencyFormat()));
registry.register("percentage", new SpreadsheetCell("=B1/C1", new PercentageFormat()));

SpreadsheetCell cell1 = registry.create("currency");  // cloned
SpreadsheetCell cell2 = registry.create("currency");  // another clone
cell1.setFormula("=SUM(B1:B10)");                      // modifying clone doesn't affect template
```

---

# STRUCTURAL PATTERNS

---

## Q4 🔴 Decorator Pattern — add behaviour without subclassing.

**Answer:**

Decorator wraps an object and adds behaviour dynamically at runtime. Avoids an explosion of subclasses for every combination of features.

```java
// Core abstraction
public interface DataWriter {
    void write(String data);
}

// Concrete base implementation
public class FileWriter implements DataWriter {
    private final String filename;
    public FileWriter(String filename) { this.filename = filename; }

    @Override
    public void write(String data) {
        // Write plain data to file
        System.out.println("Writing to " + filename + ": " + data);
    }
}

// Abstract decorator — wraps DataWriter, delegates to it
public abstract class DataWriterDecorator implements DataWriter {
    protected final DataWriter wrapped;
    protected DataWriterDecorator(DataWriter writer) { this.wrapped = writer; }
}

// Concrete decorators — add specific behaviour
public class EncryptionDecorator extends DataWriterDecorator {
    private final EncryptionService encryption;

    public EncryptionDecorator(DataWriter writer, EncryptionService encryption) {
        super(writer);
        this.encryption = encryption;
    }

    @Override
    public void write(String data) {
        String encrypted = encryption.encrypt(data);
        wrapped.write(encrypted); // delegate to wrapped writer (which may also be a decorator)
    }
}

public class CompressionDecorator extends DataWriterDecorator {
    public CompressionDecorator(DataWriter writer) { super(writer); }

    @Override
    public void write(String data) {
        String compressed = compress(data); // gzip, LZ4, etc.
        wrapped.write(compressed);
    }

    private String compress(String data) { return "[compressed]" + data; }
}

public class BufferingDecorator extends DataWriterDecorator {
    private final StringBuilder buffer = new StringBuilder();
    private final int bufferSize;

    public BufferingDecorator(DataWriter writer, int bufferSize) {
        super(writer);
        this.bufferSize = bufferSize;
    }

    @Override
    public void write(String data) {
        buffer.append(data);
        if (buffer.length() >= bufferSize) {
            wrapped.write(buffer.toString());
            buffer.setLength(0); // clear buffer
        }
    }

    public void flush() { if (buffer.length() > 0) { wrapped.write(buffer.toString()); buffer.setLength(0); } }
}

// Usage — compose decorators at runtime
DataWriter writer = new FileWriter("audit.log");
// Add encryption:
writer = new EncryptionDecorator(writer, new AES256Encryption());
// Add compression on top of encryption:
writer = new CompressionDecorator(writer);
// Add buffering on top of everything:
writer = new BufferingDecorator(writer, 1024);

writer.write("User login: alice@example.com");
// Execution: buffer → compress → encrypt → write to file
// Change the decoration order by reordering the wrapping — no class changes
```

**Real-world Java examples you use daily:**
- `BufferedInputStream` wraps `FileInputStream` — adds buffering
- `GZIPOutputStream` wraps any `OutputStream` — adds compression
- Spring's `HttpServletRequestWrapper` — add behaviour to HTTP request
- Spring Security filter chain — each filter decorates the chain

---

## Q5 🔴 Proxy Pattern — types and real applications.

**Answer:**

Proxy provides a surrogate or placeholder for another object. Controls access to it.

**Three flavours:**

```java
// 1. Virtual Proxy — defer expensive initialisation until first use
public interface Image {
    void display();
    int getWidth();
    int getHeight();
}

public class RealImage implements Image {
    private final String filename;
    private byte[] imageData;

    public RealImage(String filename) {
        this.filename = filename;
        loadFromDisk(); // EXPENSIVE — file I/O, decoding
    }

    private void loadFromDisk() {
        System.out.println("Loading " + filename + " from disk...");
        this.imageData = /* read file */ new byte[0];
    }

    @Override public void display() { System.out.println("Displaying " + filename); }
    @Override public int getWidth() { return 1920; }
    @Override public int getHeight() { return 1080; }
}

// Proxy — only loads image when display() is actually called
public class LazyImageProxy implements Image {
    private final String filename;
    private RealImage realImage; // null until needed

    public LazyImageProxy(String filename) {
        this.filename = filename; // just stores filename — no I/O yet
    }

    @Override
    public void display() {
        if (realImage == null) realImage = new RealImage(filename); // load on first use
        realImage.display();
    }

    @Override public int getWidth() { return 1920; } // can return without loading image
    @Override public int getHeight() { return 1080; }
}

// A list of 1000 images: only images the user scrolls to get loaded
List<Image> gallery = IntStream.range(0, 1000)
    .mapToObj(i -> new LazyImageProxy("image_" + i + ".jpg"))
    .collect(Collectors.toList());
gallery.get(5).display(); // only image_5 is loaded — others are proxies

// 2. Protection Proxy — access control
public class SecureOrderService implements OrderService {
    private final OrderServiceImpl realService;
    private final AuthService authService;

    public SecureOrderService(OrderServiceImpl realService, AuthService authService) {
        this.realService = realService;
        this.authService = authService;
    }

    @Override
    public void deleteOrder(Long orderId) {
        if (!authService.hasPermission("order:delete")) {
            throw new AccessDeniedException("Insufficient permission to delete orders");
        }
        realService.deleteOrder(orderId); // delegate only if authorised
    }

    @Override
    public Order findOrder(Long orderId) {
        // Read operations may have different access rules
        authService.assertAuthenticated();
        return realService.findOrder(orderId);
    }
}

// 3. Remote Proxy — represent a remote object locally (gRPC stub, RMI, Feign client)
// Feign client IS a remote proxy:
@FeignClient(name = "payment-service", url = "${payment.service.url}")
public interface PaymentServiceProxy {
    @PostMapping("/payments")
    PaymentResult processPayment(@RequestBody PaymentRequest request);
    // This interface generates a proxy that makes HTTP calls when methods are invoked
}

// 4. Caching Proxy — cache results of expensive operations
public class CachedProductService implements ProductService {
    private final ProductServiceImpl realService;
    private final Map<Long, Product> cache = new ConcurrentHashMap<>();
    private final Map<Long, Instant> cacheTime = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(15);

    @Override
    public Product findById(Long id) {
        Instant cachedAt = cacheTime.get(id);
        if (cachedAt != null && Instant.now().isBefore(cachedAt.plus(TTL))) {
            return cache.get(id); // cache hit
        }
        Product product = realService.findById(id); // cache miss — call real service
        cache.put(id, product);
        cacheTime.put(id, Instant.now());
        return product;
    }
}
```

---

## Q6 🔴 Adapter Pattern vs Facade Pattern — clear distinction with examples.

**Answer:**

**Adapter — makes incompatible interfaces work together:**

```java
// You have this existing class with a fixed interface:
public class LegacyPaymentSystem {
    public boolean makePayment(String cardNo, double amount, String currency) {
        // Old SOAP-based payment system
        return true;
    }
}

// Your modern system expects this interface:
public interface ModernPaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
}

// Adapter bridges the gap — wraps the old, exposes the new
public class LegacyPaymentAdapter implements ModernPaymentGateway {
    private final LegacyPaymentSystem legacy;

    public LegacyPaymentAdapter(LegacyPaymentSystem legacy) { this.legacy = legacy; }

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Translate ModernPaymentGateway call to LegacyPaymentSystem call
        boolean success = legacy.makePayment(
            request.getCardNumber(),
            request.getAmount().doubleValue(), // BigDecimal → double
            request.getCurrency().getCode()
        );
        return success ? PaymentResult.success(request) : PaymentResult.failed("Legacy system rejected");
    }
}

// Real-world: java.util.Arrays.asList() adapts arrays to List
// java.io.InputStreamReader adapts InputStream to Reader
// Spring's HandlerAdapter adapts different controller types to DispatcherServlet
```

**Facade — simplified interface to a complex subsystem:**

```java
// Complex subsystem — each class has its own detailed API
public class StockInventoryService { public boolean checkAvailability(String sku, int qty) { return true; } }
public class PricingEngine { public BigDecimal getPrice(String sku, String promoCode) { return BigDecimal.TEN; } }
public class PaymentProcessor { public PaymentResult charge(String cardToken, BigDecimal amount) { return null; } }
public class ShippingCalculator { public BigDecimal calculateShipping(Address address, List<String> skus) { return BigDecimal.ONE; } }
public class OrderRepository { public Order save(Order order) { return order; } }
public class EmailService { public void sendConfirmation(String email, Order order) {} }

// Without facade: caller must orchestrate all 6 services
// With facade: one simple method
public class CheckoutFacade {
    private final StockInventoryService inventory;
    private final PricingEngine pricing;
    private final PaymentProcessor payment;
    private final ShippingCalculator shipping;
    private final OrderRepository orderRepo;
    private final EmailService emailService;

    // All injected...

    // One method hides all the complexity
    public Order checkout(CheckoutRequest req) {
        // 1. Check stock
        req.getItems().forEach(item -> {
            if (!inventory.checkAvailability(item.getSku(), item.getQuantity())) {
                throw new OutOfStockException(item.getSku());
            }
        });

        // 2. Calculate total
        BigDecimal itemTotal = req.getItems().stream()
            .map(item -> pricing.getPrice(item.getSku(), req.getPromoCode()).multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingCost = shipping.calculateShipping(req.getShippingAddress(), req.getSkus());
        BigDecimal total = itemTotal.add(shippingCost);

        // 3. Charge payment
        PaymentResult paymentResult = payment.charge(req.getCardToken(), total);
        if (!paymentResult.isSuccess()) throw new PaymentFailedException(paymentResult.getMessage());

        // 4. Create and save order
        Order order = new Order(req, total, paymentResult.getTransactionId());
        Order saved = orderRepo.save(order);

        // 5. Send confirmation
        emailService.sendConfirmation(req.getCustomerEmail(), saved);

        return saved;
    }
}

// Caller:
Order order = checkoutFacade.checkout(req); // one line — all complexity hidden
```

**Key distinction:**
- **Adapter**: makes two incompatible interfaces compatible — structural translation
- **Facade**: simplifies a complex system — hides complexity, doesn't change interfaces

---

# BEHAVIOURAL PATTERNS

---

## Q7 🔴 Strategy Pattern — replace if/else chains with polymorphism.

**Answer:**

Strategy defines a family of algorithms, encapsulates each one, and makes them interchangeable. The algorithm can change independently from the clients that use it.

```java
// Without Strategy — fragile if/else chain
public class Sorter {
    public void sort(int[] arr, String algorithm) {
        if ("BUBBLE".equals(algorithm)) { /* bubble sort */ }
        else if ("QUICK".equals(algorithm)) { /* quicksort */ }
        else if ("MERGE".equals(algorithm)) { /* merge sort */ }
        // Adding HEAP sort requires modifying this class
    }
}

// With Strategy
public interface SortStrategy {
    void sort(int[] arr);
    String getName();
}

public class QuickSort implements SortStrategy {
    @Override public void sort(int[] arr) { /* quicksort impl */ }
    @Override public String getName() { return "QUICK"; }
}

public class MergeSort implements SortStrategy {
    @Override public void sort(int[] arr) { /* mergesort impl */ }
    @Override public String getName() { return "MERGE"; }
}

public class Sorter {
    private SortStrategy strategy;

    public Sorter(SortStrategy strategy) { this.strategy = strategy; }
    public void setStrategy(SortStrategy strategy) { this.strategy = strategy; } // swap at runtime

    public void sort(int[] arr) {
        System.out.println("Sorting with: " + strategy.getName());
        strategy.sort(arr);
    }
}

// Richer real-world example: route navigation
public interface RoutingStrategy {
    Route findRoute(Location from, Location to);
    String getMode();
}

public class DrivingStrategy implements RoutingStrategy {
    @Override public Route findRoute(Location from, Location to) { /* Dijkstra on road graph */ return null; }
    @Override public String getMode() { return "DRIVING"; }
}

public class WalkingStrategy implements RoutingStrategy {
    @Override public Route findRoute(Location from, Location to) { /* walking path */ return null; }
    @Override public String getMode() { return "WALKING"; }
}

public class TransitStrategy implements RoutingStrategy {
    @Override public Route findRoute(Location from, Location to) { /* public transit schedule */ return null; }
    @Override public String getMode() { return "TRANSIT"; }
}

public class Navigator {
    private RoutingStrategy strategy;

    public void setStrategy(RoutingStrategy s) { this.strategy = s; }

    public void buildRoute(Location from, Location to) {
        Route route = strategy.findRoute(from, to);
        displayRoute(route);
    }
}

// Usage:
Navigator nav = new Navigator();
nav.setStrategy(new DrivingStrategy());
nav.buildRoute(home, office); // driving route

// User switches to walking:
nav.setStrategy(new WalkingStrategy());
nav.buildRoute(home, office); // walking route — no if/else
```

---

## Q8 🔴 Observer Pattern — event-driven design, and Spring's event system.

**Answer:**

Observer defines a one-to-many dependency: when one object (Subject) changes state, all dependents (Observers) are notified automatically.

```java
// Generic observer interfaces
public interface Observer<T> {
    void onEvent(T event);
}

public interface Observable<T> {
    void subscribe(Observer<T> observer);
    void unsubscribe(Observer<T> observer);
    void notifyObservers(T event);
}

// Concrete subject
public class StockMarket implements Observable<StockPriceEvent> {
    private final Map<String, BigDecimal> prices = new HashMap<>();
    private final List<Observer<StockPriceEvent>> observers = new CopyOnWriteArrayList<>();
    // CopyOnWriteArrayList: thread-safe for iteration — observers may subscribe/unsubscribe during notification

    @Override public void subscribe(Observer<StockPriceEvent> o) { observers.add(o); }
    @Override public void unsubscribe(Observer<StockPriceEvent> o) { observers.remove(o); }

    @Override
    public void notifyObservers(StockPriceEvent event) {
        observers.forEach(o -> {
            try { o.onEvent(event); }
            catch (Exception e) { /* one observer failing shouldn't break others */ log.error("Observer failed", e); }
        });
    }

    public void updatePrice(String symbol, BigDecimal newPrice) {
        BigDecimal oldPrice = prices.put(symbol, newPrice);
        notifyObservers(new StockPriceEvent(symbol, oldPrice, newPrice, Instant.now()));
    }
}

// Concrete observers
public class AlertService implements Observer<StockPriceEvent> {
    @Override
    public void onEvent(StockPriceEvent event) {
        if (isSignificantMove(event)) {
            sendAlert(event.getSymbol(), event.getNewPrice(), event.getOldPrice());
        }
    }
    private boolean isSignificantMove(StockPriceEvent e) {
        if (e.getOldPrice() == null) return false;
        BigDecimal changePercent = e.getNewPrice().subtract(e.getOldPrice())
            .divide(e.getOldPrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        return changePercent.abs().compareTo(BigDecimal.valueOf(5)) > 0; // >5% move
    }
}

public class PortfolioTracker implements Observer<StockPriceEvent> {
    private final Map<String, Integer> holdings; // symbol → shares
    @Override
    public void onEvent(StockPriceEvent event) {
        Integer shares = holdings.get(event.getSymbol());
        if (shares != null) {
            BigDecimal gain = event.getNewPrice().subtract(event.getOldPrice()).multiply(BigDecimal.valueOf(shares));
            System.out.println("Portfolio impact: " + (gain.compareTo(BigDecimal.ZERO) > 0 ? "+" : "") + gain);
        }
    }
}

// Spring's Event system — built-in Observer implementation
// Publisher side:
@Service
public class OrderService {
    @Autowired ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        Order order = createAndSaveOrder(request);
        eventPublisher.publishEvent(new OrderPlacedEvent(this, order));
        // Event published AFTER transaction commits (see below for how to ensure this)
        return order;
    }
}

// Event class:
public class OrderPlacedEvent extends ApplicationEvent {
    private final Order order;
    public OrderPlacedEvent(Object source, Order order) { super(source); this.order = order; }
    public Order getOrder() { return order; }
}

// Listener side — decoupled, can be in a different class/module
@Component
public class OrderEventHandler {

    // @EventListener — synchronous by default, runs in same thread/transaction
    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        emailService.sendConfirmation(event.getOrder());
    }

    // @TransactionalEventListener — runs AFTER transaction commits (best for external side effects)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendConfirmationEmail(OrderPlacedEvent event) {
        // Only runs if the transaction that published the event committed successfully
        // Prevents sending email if the order creation rolled back
        emailService.sendOrderConfirmation(event.getOrder());
    }

    // Async listener — runs in a separate thread
    @Async
    @EventListener
    public void updateAnalytics(OrderPlacedEvent event) {
        analyticsService.record(event.getOrder()); // doesn't block the order creation
    }
}
```

---

## Q9 🔴 Command Pattern — undo/redo, queuing, and transaction log.

**Answer:**

Command encapsulates a request as an object. Allows: parameterising operations, queuing/logging/undoing them.

```java
public interface Command {
    void execute();
    void undo();
    String getDescription();
}

// Document editor example with full undo/redo
public class TextEditor {
    private StringBuilder text = new StringBuilder();

    public void insertText(int position, String content) {
        text.insert(position, content);
    }

    public void deleteText(int position, int length) {
        text.delete(position, position + length);
    }

    public String getText() { return text.toString(); }
}

public class InsertCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final String content;

    public InsertCommand(TextEditor editor, int position, String content) {
        this.editor = editor;
        this.position = position;
        this.content = content;
    }

    @Override public void execute() { editor.insertText(position, content); }
    @Override public void undo() { editor.deleteText(position, content.length()); } // reverse
    @Override public String getDescription() { return "Insert \"" + content + "\" at " + position; }
}

public class DeleteCommand implements Command {
    private final TextEditor editor;
    private final int position;
    private final int length;
    private String deletedContent; // saved for undo

    @Override
    public void execute() {
        deletedContent = editor.getText().substring(position, position + length);
        editor.deleteText(position, length);
    }

    @Override
    public void undo() { editor.insertText(position, deletedContent); } // restore deleted content

    @Override public String getDescription() { return "Delete " + length + " chars at " + position; }
}

// Command History — manages undo/redo stack
public class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // new action clears redo history (standard behavior)
        System.out.println("Executed: " + command.getDescription());
    }

    public void undo() {
        if (undoStack.isEmpty()) { System.out.println("Nothing to undo"); return; }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        System.out.println("Undid: " + command.getDescription());
    }

    public void redo() {
        if (redoStack.isEmpty()) { System.out.println("Nothing to redo"); return; }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        System.out.println("Redid: " + command.getDescription());
    }
}

// Financial transaction with command pattern (audit trail):
public class TransferCommand implements Command {
    private final BankAccount source;
    private final BankAccount target;
    private final BigDecimal amount;
    private boolean executed = false;

    @Override
    public void execute() {
        source.debit(amount);
        target.credit(amount);
        executed = true;
    }

    @Override
    public void undo() {
        if (!executed) return;
        target.debit(amount);   // reverse
        source.credit(amount);
        executed = false;
    }
}
```

---

## Q10 🔴 Template Method Pattern — define the skeleton, let subclasses fill in steps.

**Answer:**

Template Method defines the SKELETON of an algorithm in a base class, deferring some steps to subclasses. The overall structure stays the same; only specific steps vary.

```java
// Data processing pipeline — same structure, different formats
public abstract class DataProcessor {

    // TEMPLATE METHOD — defines the algorithm skeleton
    // final prevents subclasses from reordering steps
    public final void process(String source) {
        String rawData = readData(source);      // Step 1: read
        String validatedData = validate(rawData); // Step 2: validate (hook)
        String processedData = transform(validatedData); // Step 3: transform (abstract)
        saveResults(processedData);             // Step 4: save (hook)
        sendNotification();                     // Step 5: notify (concrete, shared)
    }

    // Abstract steps — subclasses MUST implement
    protected abstract String readData(String source);
    protected abstract String transform(String data);

    // Hook methods — subclasses MAY override (defaults provided)
    protected String validate(String data) {
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Data is empty");
        return data; // base validation — subclasses can add more
    }

    protected void saveResults(String data) {
        System.out.println("Default save: " + data); // subclasses can override
    }

    // Concrete method — same for all subclasses
    private void sendNotification() {
        System.out.println("Processing complete — notification sent");
    }
}

// CSV-specific implementation
public class CsvDataProcessor extends DataProcessor {
    @Override
    protected String readData(String filename) {
        return Files.readString(Path.of(filename)); // read CSV file
    }

    @Override
    protected String transform(String data) {
        return Arrays.stream(data.split("\n"))
            .skip(1) // skip header
            .map(line -> processRow(line))
            .collect(Collectors.joining("\n"));
    }

    @Override
    protected String validate(String data) {
        String base = super.validate(data); // call base validation first
        if (!data.contains(",")) throw new IllegalArgumentException("Not valid CSV");
        return base;
    }

    private String processRow(String line) { return line.toUpperCase(); }
}

// JSON-specific implementation
public class JsonDataProcessor extends DataProcessor {
    @Override
    protected String readData(String url) {
        return httpClient.get(url).body(); // fetch from API
    }

    @Override
    protected String transform(String data) {
        // Parse JSON, transform fields, return transformed JSON
        return data.replace("\"status\":\"active\"", "\"status\":\"ACTIVE\"");
    }

    @Override
    protected void saveResults(String data) {
        mongoCollection.insertOne(Document.parse(data)); // save to MongoDB
    }
}

// Usage:
DataProcessor csv = new CsvDataProcessor();
csv.process("employees.csv"); // same steps, CSV-specific behavior

DataProcessor json = new JsonDataProcessor();
json.process("https://api.example.com/data"); // same steps, JSON-specific behavior
```

**Real-world Spring example:**
`JdbcTemplate` is a classic Template Method implementation:
- The template handles: getting connection, managing transactions, handling exceptions, releasing connection
- You provide (the varying step): the SQL query and `RowMapper`

---

## Q11 🟡 Chain of Responsibility — request processing pipelines.

**Answer:**

Chain of Responsibility passes a request along a chain of handlers. Each handler decides to process it or pass it to the next.

```java
public abstract class RequestHandler {
    private RequestHandler next;

    public RequestHandler setNext(RequestHandler next) {
        this.next = next;
        return next; // enables chaining: handler1.setNext(handler2).setNext(handler3)
    }

    public final void handle(HttpRequest request, HttpResponse response) {
        if (canHandle(request)) {
            process(request, response);
        } else if (next != null) {
            next.handle(request, response); // pass to next handler
        } else {
            response.setStatus(404);
            response.setBody("No handler found");
        }
    }

    protected abstract boolean canHandle(HttpRequest request);
    protected abstract void process(HttpRequest request, HttpResponse response);
}

public class AuthenticationHandler extends RequestHandler {
    private final TokenService tokenService;

    @Override
    protected boolean canHandle(HttpRequest request) {
        return true; // always checks auth (it's a filter, not a terminating handler)
    }

    @Override
    protected void process(HttpRequest request, HttpResponse response) {
        String token = request.getHeader("Authorization");
        if (token == null || !tokenService.isValid(token)) {
            response.setStatus(401);
            response.setBody("Unauthorized");
            return; // stop chain
        }
        if (next != null) next.handle(request, response); // auth passed — continue
    }
}

public class RateLimitHandler extends RequestHandler {
    private final RateLimiter rateLimiter;

    @Override protected boolean canHandle(HttpRequest request) { return true; }

    @Override
    protected void process(HttpRequest request, HttpResponse response) {
        String clientId = request.getHeader("X-Client-ID");
        if (!rateLimiter.allowRequest(clientId)) {
            response.setStatus(429);
            response.setBody("Too Many Requests");
            return;
        }
        if (next != null) next.handle(request, response);
    }
}

public class LoggingHandler extends RequestHandler {
    @Override protected boolean canHandle(HttpRequest req) { return true; }

    @Override
    protected void process(HttpRequest request, HttpResponse response) {
        long start = System.currentTimeMillis();
        if (next != null) next.handle(request, response);
        log.info("{} {} → {} in {}ms", request.getMethod(), request.getPath(),
            response.getStatus(), System.currentTimeMillis() - start);
    }
}

// Build the chain:
RequestHandler chain = new LoggingHandler();
chain.setNext(new AuthenticationHandler(tokenService))
     .setNext(new RateLimitHandler(rateLimiter))
     .setNext(new BusinessLogicHandler());

// Process:
chain.handle(request, response);
// Flow: Logging → Auth → RateLimit → BusinessLogic
// This is exactly how Spring Security's filter chain works
```

---
