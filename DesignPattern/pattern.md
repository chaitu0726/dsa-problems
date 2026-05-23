# Design Patterns with Real Company Examples (Interview Ready)

---

# 1. Singleton Pattern

# Core Idea

```txt
Only ONE object should exist globally.
```

---

# Real Company Example → Netflix Configuration Service

Imagine Netflix app servers.

Every service needs:
- DB configs
- API keys
- feature flags
- cache configs

You do NOT want:

```txt
1000 config objects
```

across threads/services.

Instead:
```txt
One shared configuration manager
```

used everywhere.

---

# Visualization

```txt
          All Services
      /       |        \
     /        |         \
AuthService  VideoService  PaymentService
      \        |         /
       \       |        /
        ConfigurationManager
             (Singleton)
```

---

# Why Singleton Here?

Because:
- configs should stay consistent
- object creation expensive
- globally shared state needed

---

# Java Example

```java
class ConfigManager {

    private static final ConfigManager INSTANCE =
            new ConfigManager();

    private ConfigManager(){}

    public static ConfigManager getInstance() {
        return INSTANCE;
    }
}
```

---

# Real Examples

| Company | Usage |
|---|---|
| Netflix | Config manager |
| AWS SDK | Credential manager |
| Spring | Singleton beans |
| Logging frameworks | Logger |

---

# Memorable Line

```txt
Singleton = One system-wide brain/object
```

---

---

# 2. Factory Pattern

# Core Idea

```txt
Factory decides WHICH object to create.
```

Client should NOT know object creation logic.

---

# Real Company Example → Amazon Payment System

Amazon checkout supports:
- UPI
- Credit Card
- Wallet
- Net Banking

---

# WITHOUT Factory

```java
if(type.equals("UPI")) {
   payment = new UpiPayment();
}
else if(type.equals("CARD")) {
   payment = new CardPayment();
}
```

Problem:
```txt
Object creation logic spread everywhere.
```

---

# WITH Factory

```txt
Client
   ↓
PaymentFactory
   ↓
Returns correct payment object
```

---

# Important Clarification

You asked:

> “How do we know to create UPI object?”

EXACTLY.

THAT is Factory's job.

---

# Factory Responsibility

```txt
Input:
"UPI"

Factory decides:
new UpiPayment()
```

Client code does NOT know implementation.

---

# Visualization

```txt
           User Chooses
               "UPI"
                  ↓
          PaymentFactory
                  ↓
          Creates UpiPayment
                  ↓
           Returns object
```

---

# Java Example

```java
interface Payment {
    void pay();
}

class UpiPayment implements Payment {
    public void pay() {
        System.out.println("UPI Payment");
    }
}

class CardPayment implements Payment {
    public void pay() {
        System.out.println("Card Payment");
    }
}

class PaymentFactory {

    public static Payment getPayment(String type) {

        if(type.equals("UPI"))
            return new UpiPayment();

        return new CardPayment();
    }
}
```

---

# Why Factory Makes Sense Here?

Because:
```txt
Object selection/creation is main complexity.
```

---

# Real Examples

| Company | Usage |
|---|---|
| Amazon | Payment method creation |
| AWS SDK | Different cloud clients |
| Database drivers | MySQL/Postgres connection |
| Notification systems | Email/SMS/Push creation |

---

# Memorable Line

```txt
Factory = Object creation decision maker
```

---

---

# 3. Strategy Pattern

# Core Idea

```txt
Object already exists,
but behavior/algorithm changes dynamically.
```

---

# IMPORTANT Difference

| Factory | Strategy |
|---|---|
| Creates object | Changes behavior |
| Focus on creation | Focus on algorithm |
| "Which object?" | "Which logic?" |

---

# Real Company Example → Uber Pricing Engine

Uber already has:
```txt
RideService
```

But fare calculation changes:

- normal pricing
- surge pricing
- festival pricing
- premium pricing

Ride service remains SAME.

Only pricing algorithm changes.

---

# Visualization

```txt
RideService
     ↓
Pricing Strategy
     ↓
NormalPricing
SurgePricing
PremiumPricing
```

---

# Why NOT Factory Here?

Because:
```txt
Ride object already exists.
```

We are NOT deciding:
```txt
Which ride object to create
```

We ARE deciding:
```txt
Which pricing algorithm to use
```

---

# Java Example

```java
interface PricingStrategy {
    double calculateFare(int distance);
}

class NormalPricing
        implements PricingStrategy {

    public double calculateFare(int distance) {
        return distance * 10;
    }
}

class SurgePricing
        implements PricingStrategy {

    public double calculateFare(int distance) {
        return distance * 20;
    }
}

class RideService {

    private PricingStrategy strategy;

    RideService(PricingStrategy strategy) {
        this.strategy = strategy;
    }

    void calculate(int distance) {
        System.out.println(
            strategy.calculateFare(distance)
        );
    }
}
```

---

# Real Examples

| Company | Usage |
|---|---|
| Uber | Pricing strategies |
| Netflix | Video recommendation algorithm |
| Google Maps | Route strategy |
| Compression tools | Zip/RAR strategies |

---

# Memorable Line

```txt
Strategy = Same object, different behavior
```

---

---

# 4. Observer Pattern

# Core Idea

```txt
One event happens
→ many systems notified automatically
```

---

# Real Company Example → YouTube Notifications

You subscribe to channels.

When creator uploads video:
```txt
Millions of subscribers notified automatically.
```

---

# Visualization

```txt
          YouTube Channel
                 |
     -------------------------
     |         |            |
 User A     User B       User C
```

---

# Why Observer?

Channel should NOT know:
```txt
exact subscriber logic
```

It simply:
```txt
broadcasts event
```

---

# Real Backend Flow

```txt
Video Uploaded
      ↓
Event Published
      ↓
Notification Service
Email Service
Push Notification
Analytics Service
```

---

# Real Examples

| Company | Usage |
|---|---|
| YouTube | Subscriber notifications |
| Kafka | Event consumers |
| Spring Boot | Event listeners |
| Stock apps | Price alerts |

---

# Memorable Line

```txt
Observer = Publish event once, many listeners react
```

---

---

# 5. Builder Pattern

# Core Idea

```txt
Build complex object step by step.
```

---

# Real Company Example → AWS EC2 Instance Creation

When creating EC2 instance:
- RAM
- CPU
- Region
- Security Group
- Storage
- Auto Scaling
- Tags

Huge constructor becomes nightmare.

---

# Without Builder

```java
new EC2(
  "t2.micro",
  8,
  "ap-south-1",
  true,
  false,
  ...
)
```

Impossible to remember.

---

# Builder Visualization

```txt
EC2Builder
   .setCPU()
   .setRAM()
   .setRegion()
   .enableAutoScaling()
   .build()
```

---

# Why Builder?

Because:
```txt
Complex object with many optional fields
```

---

# Real Examples

| Company | Usage |
|---|---|
| AWS SDK | Request builders |
| Lombok | @Builder |
| HTTP clients | Request creation |
| Elasticsearch | Query builders |

---

# Memorable Line

```txt
Builder = Construct giant object cleanly
```

---

---

# 6. Adapter Pattern

# Core Idea

```txt
Make incompatible systems talk to each other.
```

---

# Real Company Example → Netflix Legacy Migration

Netflix old service returns:
```xml
<movie>
```

New microservice expects:
```json
{
 "movie":
}
```

Changing old system impossible immediately.

So:
```txt
Adapter converts XML → JSON
```

---

# Visualization

```txt
Legacy Service (XML)
          ↓
      Adapter
          ↓
Modern Service (JSON)
```

---

# Why Adapter?

Because:
```txt
We cannot rewrite entire legacy system immediately.
```

---

# Real Examples

| Company | Usage |
|---|---|
| Netflix | Legacy migration |
| Payment gateways | Third-party integration |
| JDBC | DB driver adaptation |
| Cloud migrations | Old/new API integration |

---

# Memorable Line

```txt
Adapter = Translator between incompatible systems
```

---

---

# 7. Decorator Pattern

# Core Idea

```txt
Add new functionality dynamically
WITHOUT changing original code.
```

---

# Real Company Example → Amazon Order Pipeline

Base order service:
```txt
Place order
```

Now dynamically add:
- coupon
- fraud detection
- gift wrap
- premium delivery

without modifying core order logic repeatedly.

---

# Visualization

```txt
Base Order
    ↓
Coupon Decorator
    ↓
Fraud Check Decorator
    ↓
Gift Wrap Decorator
```

---

# Why Decorator?

Because:
```txt
Features added dynamically.
```

Avoid:
```txt
Huge inheritance hierarchy
```

---

# Real Examples

| Company | Usage |
|---|---|
| Amazon | Order processing features |
| Java IO | Buffered streams |
| Spring Security | Security layers |
| Logging frameworks | Additional log metadata |

---

# Memorable Line

```txt
Decorator = Wrap object with extra powers
```

---

---

# 8. Chain of Responsibility

# Core Idea

```txt
Request passes through multiple handlers.
```

Each handler:
- processes
- or forwards request

---

# Real Company Example → API Gateway in Netflix

Incoming request goes through:

1. Authentication
2. Rate Limiting
3. Logging
4. Authorization
5. Routing

---

# Visualization

```txt
Incoming Request
        ↓
Authentication Filter
        ↓
Rate Limiter
        ↓
Logging Filter
        ↓
Routing Layer
```

---

# Why Chain of Responsibility?

Because:
```txt
Each step independent and reusable.
```

Easy to:
- add new filters
- reorder filters
- remove filters

---

# Real Examples

| Company | Usage |
|---|---|
| Netflix | API gateway filters |
| Spring Security | Filter chain |
| Express.js | Middleware |
| Servlet Filters | Request processing |

---

# Memorable Line

```txt
Chain of Responsibility = Request processing pipeline
```

---

# FINAL SUPER IMPORTANT INTERVIEW DIFFERENCE

# Factory vs Strategy

This confuses MANY people.

---

# Factory

Question:
```txt
WHICH OBJECT should I create?
```

Example:
```txt
UPI Payment?
Card Payment?
Wallet Payment?
```

Factory decides object creation.

---

# Strategy

Question:
```txt
WHICH ALGORITHM/BEHAVIOR should I use?
```

Example:
```txt
Normal pricing?
Surge pricing?
Festival pricing?
```

Object already exists.
Behavior changes.

---

# Ultimate Memory Trick

| Pattern | Remember This |
|---|---|
| Singleton | One global object |
| Factory | Decide object creation |
| Strategy | Swap behavior dynamically |
| Observer | Broadcast events |
| Builder | Build complex object |
| Adapter | Translate incompatible systems |
| Decorator | Add features dynamically |
| Chain Responsibility | Pass request through pipeline |
