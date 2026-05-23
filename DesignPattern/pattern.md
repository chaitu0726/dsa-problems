# Design Patterns Notes (Interview Ready)

---

# 1. Singleton Pattern

## Definition
Ensures only ONE object of a class exists in the application.

Used when shared global instance is needed.

---

# Real World Examples

- Logger
- DB Connection Pool
- Configuration Manager
- Cache Manager

---

# Key Idea

```txt
One class
→ only one object
→ global access point
```

---

# Java Example

```java
class Singleton {

    private static Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {

        if(instance == null) {
            instance = new Singleton();
        }

        return instance;
    }
}
```

Usage:

```java
Singleton s1 = Singleton.getInstance();
Singleton s2 = Singleton.getInstance();

System.out.println(s1 == s2); // true
```

---

# Problems

Not thread safe.

Multiple threads may create multiple objects.

---

# Thread Safe Singleton

```java
class Singleton {

    private static Singleton instance;

    private Singleton(){}

    public static synchronized Singleton getInstance() {

        if(instance == null) {
            instance = new Singleton();
        }

        return instance;
    }
}
```

---

# Best Version (Interview Preferred)

```java
class Singleton {

    private Singleton(){}

    private static class Holder {
        private static final Singleton INSTANCE =
                new Singleton();
    }

    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

---

# Advantages

- Saves memory
- Shared resource management
- Global access

---

# Disadvantages

- Hard to test
- Global state issue
- Multithreading complexity

---

# Interview Questions

## Why constructor private?
To prevent external object creation.

## Why singleton used in Spring?
Spring beans are singleton by default.

---

---

# 2. Factory Pattern

## Definition

Factory creates objects WITHOUT exposing creation logic.

---

# Problem Without Factory

```java
Vehicle v = new Car();
```

Client tightly coupled with Car class.

---

# Factory Solution

```txt
Client
  ↓
Factory
  ↓
Creates object
```

---

# Java Example

```java
interface Vehicle {
    void drive();
}

class Car implements Vehicle {
    public void drive() {
        System.out.println("Car driving");
    }
}

class Bike implements Vehicle {
    public void drive() {
        System.out.println("Bike driving");
    }
}

class VehicleFactory {

    public static Vehicle getVehicle(String type) {

        if(type.equals("car"))
            return new Car();

        return new Bike();
    }
}
```

Usage:

```java
Vehicle v = VehicleFactory.getVehicle("car");
v.drive();
```

---

# Advantages

- Loose coupling
- Centralized creation logic
- Easy extension

---

# Real World Examples

- Notification service
- Payment gateway
- Spring BeanFactory

---

# Interview Question

## Difference between Factory and Builder?

Factory:
```txt
Creates object
```

Builder:
```txt
Creates complex object step by step
```

---

---

# 3. Strategy Pattern

## Definition

Encapsulates interchangeable algorithms separately.

Allows behavior change at runtime.

---

# Real World Example

Google Maps:
- Car route
- Bike route
- Walking route

Different strategy.

---

# Without Strategy

Huge if-else blocks.

---

# With Strategy

```txt
Context
   ↓
Strategy Interface
   ↓
Different implementations
```

---

# Java Example

```java
interface PaymentStrategy {
    void pay(int amount);
}

class CreditCardPayment
        implements PaymentStrategy {

    public void pay(int amount) {
        System.out.println("Paid via card");
    }
}

class UpiPayment
        implements PaymentStrategy {

    public void pay(int amount) {
        System.out.println("Paid via UPI");
    }
}

class PaymentService {

    private PaymentStrategy strategy;

    public PaymentService(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void processPayment(int amount) {
        strategy.pay(amount);
    }
}
```

Usage:

```java
PaymentService p =
    new PaymentService(new UpiPayment());

p.processPayment(1000);
```

---

# Advantages

- Open/Closed Principle
- Removes if-else
- Runtime behavior change

---

# Real Examples

- Payment systems
- Sorting algorithms
- Compression algorithms

---

---

# 4. Observer Pattern

## Definition

One object changes state
→ multiple objects get notified automatically.

One-to-many relationship.

---

# Real World Example

YouTube subscribers.

Channel uploads video
→ all subscribers notified.

---

# Structure

```txt
Subject
   ↓ notify
Observers
```

---

# Java Example

```java
interface Observer {
    void update(String msg);
}

class User implements Observer {

    private String name;

    User(String name) {
        this.name = name;
    }

    public void update(String msg) {
        System.out.println(name + " got " + msg);
    }
}

class YoutubeChannel {

    List<Observer> observers = new ArrayList<>();

    void subscribe(Observer o) {
        observers.add(o);
    }

    void notifyUsers(String msg) {

        for(Observer o : observers) {
            o.update(msg);
        }
    }
}
```

Usage:

```java
YoutubeChannel yt = new YoutubeChannel();

yt.subscribe(new User("John"));

yt.notifyUsers("New Video Uploaded");
```

---

# Advantages

- Loose coupling
- Event-driven systems
- Dynamic subscriptions

---

# Real Examples

- Kafka consumers
- Event listeners
- Spring Events
- GUI button listeners

---

---

# 5. Builder Pattern

## Definition

Used to create complex objects step by step.

---

# Problem Without Builder

Constructor explosion:

```java
User(String a)
User(String a, int b)
User(String a, int b, String c)
```

Very messy.

---

# Builder Solution

```java
class User {

    private String name;
    private int age;
    private String city;

    private User(UserBuilder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.city = builder.city;
    }

    static class UserBuilder {

        private String name;
        private int age;
        private String city;

        public UserBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder setAge(int age) {
            this.age = age;
            return this;
        }

        public UserBuilder setCity(String city) {
            this.city = city;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
```

Usage:

```java
User u = new User.UserBuilder()
                    .setName("John")
                    .setAge(25)
                    .build();
```

---

# Advantages

- Readable
- Handles optional params
- Immutable object creation

---

# Real Examples

- Lombok @Builder
- StringBuilder
- HttpRequest builder

---

---

# 6. Adapter Pattern

## Definition

Converts incompatible interfaces into compatible ones.

Acts as bridge.

---

# Real World Example

Mobile charger adapter.

---

# Problem

Old system expects XML.

New service gives JSON.

Adapter converts.

---

# Java Example

```java
interface AndroidCharger {
    void charge();
}

class IphoneCharger {

    public void chargeIphone() {
        System.out.println("Charging iPhone");
    }
}

class Adapter implements AndroidCharger {

    IphoneCharger iphoneCharger =
            new IphoneCharger();

    public void charge() {
        iphoneCharger.chargeIphone();
    }
}
```

Usage:

```java
AndroidCharger charger = new Adapter();
charger.charge();
```

---

# Advantages

- Reuse old code
- Integrate incompatible systems

---

# Real Examples

- JDBC drivers
- Legacy system integration

---

---

# 7. Decorator Pattern

## Definition

Adds functionality dynamically WITHOUT modifying original class.

---

# Real World Example

Coffee:
- base coffee
- add milk
- add sugar
- add cream

---

# Structure

```txt
Base Object
   ↓
Decorator
   ↓
Adds behavior
```

---

# Java Example

```java
interface Coffee {
    String getDescription();
}

class BasicCoffee implements Coffee {

    public String getDescription() {
        return "Basic Coffee";
    }
}

class MilkDecorator implements Coffee {

    private Coffee coffee;

    MilkDecorator(Coffee coffee) {
        this.coffee = coffee;
    }

    public String getDescription() {
        return coffee.getDescription() + " + Milk";
    }
}
```

Usage:

```java
Coffee coffee =
    new MilkDecorator(new BasicCoffee());

System.out.println(coffee.getDescription());
```

---

# Advantages

- Flexible
- Avoid subclass explosion
- Runtime behavior addition

---

# Real Examples

- Java IO streams
- Spring security filters

---

---

# 8. Chain of Responsibility

## Definition

Request passes through chain of handlers.

Each handler decides:
- process
- or forward

---

# Real World Example

Spring Security Filter Chain.

---

# Flow

```txt
Request
   ↓
Auth Handler
   ↓
Logging Handler
   ↓
Validation Handler
```

---

# Java Example

```java
abstract class Handler {

    protected Handler next;

    public void setNext(Handler next) {
        this.next = next;
    }

    abstract void handle(String request);
}

class AuthHandler extends Handler {

    void handle(String request) {

        System.out.println("Auth done");

        if(next != null)
            next.handle(request);
    }
}

class LoggingHandler extends Handler {

    void handle(String request) {

        System.out.println("Logging done");

        if(next != null)
            next.handle(request);
    }
}
```

Usage:

```java
Handler auth = new AuthHandler();
Handler log = new LoggingHandler();

auth.setNext(log);

auth.handle("API Request");
```

---

# Advantages

- Loose coupling
- Flexible chain
- Dynamic processing pipeline

---

# Real Examples

- Servlet filters
- Spring Security
- Middleware pipelines

---

# Quick Revision Table

| Pattern | Purpose |
|---|---|
| Singleton | Single object |
| Factory | Object creation |
| Strategy | Change algorithm dynamically |
| Observer | Event notification |
| Builder | Complex object creation |
| Adapter | Convert incompatible interfaces |
| Decorator | Add behavior dynamically |
| Chain of Responsibility | Request processing pipeline |

---

# Important Interview Tip

Most interviewers expect:
- WHY pattern used
- problem solved
- real-world use case

More than remembering definitions.
