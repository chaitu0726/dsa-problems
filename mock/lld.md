# Low Level Design (LLD) — Interview Prep (6+ YOE)
## Class Design · SOLID · Object Modelling · Real Systems

> **What interviewers actually want to see in LLD:**
> - Can you translate requirements into clean class hierarchies?
> - Do you apply SOLID without being prompted?
> - Do you know when NOT to over-engineer?
> - Can you defend your design decisions?

---

## Q1 🔴 Explain SOLID principles — with real code examples showing the violation AND the fix.

**Answer:**

---

### S — Single Responsibility Principle

> A class should have only one reason to change.

**Violation:**

```java
// This class does three completely different things
// If any of them changes, this class must be touched
public class Employee {
    private String name;
    private BigDecimal salary;

    // Responsibility 1: business logic
    public BigDecimal calculateBonus() {
        return salary.multiply(BigDecimal.valueOf(0.10));
    }

    // Responsibility 2: persistence
    public void saveToDatabase() {
        // JDBC code here — couples business object to DB
        Connection conn = DriverManager.getConnection("...");
        PreparedStatement ps = conn.prepareStatement("INSERT INTO employees ...");
        ps.execute();
    }

    // Responsibility 3: formatting/presentation
    public String toJsonString() {
        return "{\"name\":\"" + name + "\",\"salary\":" + salary + "}";
    }
}
```

**Fix:**

```java
// Each class has one reason to change
public class Employee {
    private String name;
    private BigDecimal salary;
    public BigDecimal calculateBonus() { return salary.multiply(BigDecimal.valueOf(0.10)); }
}

public class EmployeeRepository {
    public void save(Employee e) { /* JDBC or JPA logic */ }
    public Optional<Employee> findById(Long id) { /* query */ return Optional.empty(); }
}

public class EmployeeSerializer {
    public String toJson(Employee e) { /* Jackson or manual */ return "{}"; }
}
```

---

### O — Open/Closed Principle

> Open for extension, closed for modification. Add new behaviour without changing existing code.

**Violation:**

```java
// Every time a new payment method is added, this method must be modified and retested
public class PaymentProcessor {
    public void process(String paymentType, double amount) {
        if (paymentType.equals("CREDIT_CARD")) {
            // credit card logic
        } else if (paymentType.equals("PAYPAL")) {
            // PayPal logic
        } else if (paymentType.equals("UPI")) {
            // UPI logic — someone had to modify this class to add UPI
        }
        // Adding CRYPTO requires modifying this again — fragile
    }
}
```

**Fix:**

```java
// Abstraction — each payment type is self-contained
public interface PaymentGateway {
    void processPayment(double amount);
    String getGatewayName();
}

public class CreditCardGateway implements PaymentGateway {
    @Override public void processPayment(double amount) { /* Stripe API */ }
    @Override public String getGatewayName() { return "CREDIT_CARD"; }
}

public class UpiGateway implements PaymentGateway {
    @Override public void processPayment(double amount) { /* UPI SDK */ }
    @Override public String getGatewayName() { return "UPI"; }
}

// Adding CryptoGateway does NOT touch PaymentProcessor at all
public class CryptoGateway implements PaymentGateway {
    @Override public void processPayment(double amount) { /* crypto API */ }
    @Override public String getGatewayName() { return "CRYPTO"; }
}

public class PaymentProcessor {
    private final Map<String, PaymentGateway> gateways;

    public PaymentProcessor(List<PaymentGateway> gateways) {
        this.gateways = gateways.stream()
            .collect(Collectors.toMap(PaymentGateway::getGatewayName, g -> g));
    }

    public void process(String type, double amount) {
        PaymentGateway gateway = gateways.get(type);
        if (gateway == null) throw new UnsupportedPaymentTypeException(type);
        gateway.processPayment(amount);
    }
}
```

---

### L — Liskov Substitution Principle

> Objects of a subtype should be substitutable for objects of the supertype without altering correctness.

**Violation:**

```java
public class Rectangle {
    protected int width, height;
    public void setWidth(int w) { this.width = w; }
    public void setHeight(int h) { this.height = h; }
    public int area() { return width * height; }
}

// Square IS-A Rectangle mathematically — but LSP is violated
public class Square extends Rectangle {
    @Override
    public void setWidth(int w) { this.width = w; this.height = w; } // side effect!
    @Override
    public void setHeight(int h) { this.width = h; this.height = h; } // side effect!
}

// This code breaks when a Square is passed:
void testRectangle(Rectangle r) {
    r.setWidth(5);
    r.setHeight(10);
    assert r.area() == 50; // FAILS for Square: area = 100 (height set width too)
}
// Square cannot be substituted for Rectangle without changing program behaviour
```

**Fix:**

```java
// Don't force inheritance — prefer composition or a common interface
public interface Shape {
    int area();
}

public class Rectangle implements Shape {
    private final int width, height;
    public Rectangle(int width, int height) { this.width = width; this.height = height; }
    @Override public int area() { return width * height; }
}

public class Square implements Shape {
    private final int side;
    public Square(int side) { this.side = side; }
    @Override public int area() { return side * side; }
}
// Both substitutable for Shape — neither breaks the other's contract
```

---

### I — Interface Segregation Principle

> Clients should not be forced to depend on interfaces they do not use.

**Violation:**

```java
// Fat interface — every implementer must implement everything even if irrelevant
public interface Worker {
    void work();
    void eat();
    void sleep();
    void attendMeeting();
    void codeReview();
}

// Robot can work but can't eat or sleep — forced to implement meaninglessly
public class Robot implements Worker {
    @Override public void work() { /* actual work */ }
    @Override public void eat() { throw new UnsupportedOperationException(); } // violation!
    @Override public void sleep() { throw new UnsupportedOperationException(); }
    @Override public void attendMeeting() { /* maybe? */ }
    @Override public void codeReview() { /* maybe */ }
}
```

**Fix:**

```java
// Segregate into focused interfaces
public interface Workable { void work(); }
public interface Eatable { void eat(); }
public interface Sleepable { void sleep(); }
public interface Reviewer { void codeReview(); }

// Human employee implements all that apply
public class HumanEmployee implements Workable, Eatable, Sleepable, Reviewer {
    @Override public void work() { }
    @Override public void eat() { }
    @Override public void sleep() { }
    @Override public void codeReview() { }
}

// Robot only implements what makes sense
public class Robot implements Workable, Reviewer {
    @Override public void work() { }
    @Override public void codeReview() { }
    // No eat() or sleep() — not forced on it
}
```

---

### D — Dependency Inversion Principle

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

**Violation:**

```java
// High-level OrderService directly depends on low-level MySQLOrderRepository
// Changing to PostgreSQL or MongoDB requires modifying OrderService
public class OrderService {
    private MySQLOrderRepository repository = new MySQLOrderRepository(); // concrete dependency

    public void placeOrder(Order order) {
        repository.save(order); // tightly coupled
    }
}
```

**Fix:**

```java
// Abstraction (interface) that both sides depend on
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);
}

// Low-level module implements the abstraction
public class MySQLOrderRepository implements OrderRepository {
    @Override public void save(Order order) { /* MySQL */ }
    @Override public Optional<Order> findById(Long id) { return Optional.empty(); }
    @Override public List<Order> findByUserId(Long userId) { return List.of(); }
}

public class MongoOrderRepository implements OrderRepository {
    @Override public void save(Order order) { /* MongoDB */ }
    // ...
}

// High-level module depends only on the abstraction — injected, not instantiated
public class OrderService {
    private final OrderRepository repository; // interface, not concrete

    public OrderService(OrderRepository repository) { // injected via constructor
        this.repository = repository;
    }

    public void placeOrder(Order order) {
        repository.save(order);
    }
}
// Switch from MySQL to MongoDB: change the injection binding — OrderService untouched
```

---

## Q2 🔴 Design a Parking Lot system. Model the classes, relationships, and key operations.

**Answer:**

**Requirements to clarify first (always ask these in an interview):**
- Multiple floors? → Yes
- Vehicle types? → Bike, Car, Truck
- Pricing strategy? → Hourly, flat rate
- Reservations? → No, walk-in only
- Payment methods? → Cash/Card

**Class design:**

```java
// --- Enums ---
public enum VehicleType { BIKE, CAR, TRUCK }
public enum SpotSize { SMALL, MEDIUM, LARGE }
public enum TicketStatus { ACTIVE, PAID, CANCELLED }

// --- Vehicle hierarchy ---
public abstract class Vehicle {
    private final String licensePlate;
    private final VehicleType type;

    protected Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public abstract SpotSize getRequiredSpotSize();
    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType() { return type; }
}

public class Bike extends Vehicle {
    public Bike(String plate) { super(plate, VehicleType.BIKE); }
    @Override public SpotSize getRequiredSpotSize() { return SpotSize.SMALL; }
}

public class Car extends Vehicle {
    public Car(String plate) { super(plate, VehicleType.CAR); }
    @Override public SpotSize getRequiredSpotSize() { return SpotSize.MEDIUM; }
}

public class Truck extends Vehicle {
    public Truck(String plate) { super(plate, VehicleType.TRUCK); }
    @Override public SpotSize getRequiredSpotSize() { return SpotSize.LARGE; }
}

// --- Parking Spot ---
public class ParkingSpot {
    private final String spotId;       // "F1-A12" = floor 1, row A, spot 12
    private final SpotSize size;
    private Vehicle occupiedBy;        // null = available

    public ParkingSpot(String spotId, SpotSize size) {
        this.spotId = spotId;
        this.size = size;
    }

    public boolean isAvailable() { return occupiedBy == null; }

    public boolean canFit(Vehicle vehicle) {
        return isAvailable() && size.ordinal() >= vehicle.getRequiredSpotSize().ordinal();
        // LARGE spot can fit SMALL or MEDIUM vehicles too
    }

    public void park(Vehicle vehicle) {
        if (!canFit(vehicle)) throw new SpotNotAvailableException(spotId);
        this.occupiedBy = vehicle;
    }

    public Vehicle vacate() {
        Vehicle v = this.occupiedBy;
        this.occupiedBy = null;
        return v;
    }

    public String getSpotId() { return spotId; }
    public SpotSize getSize() { return size; }
}

// --- Floor ---
public class ParkingFloor {
    private final int floorNumber;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = spots;
    }

    public Optional<ParkingSpot> findAvailableSpot(Vehicle vehicle) {
        return spots.stream()
            .filter(spot -> spot.canFit(vehicle))
            .findFirst(); // can use more sophisticated strategy: nearest to entrance, etc.
    }

    public Map<SpotSize, Long> getAvailabilityCount() {
        return spots.stream()
            .filter(ParkingSpot::isAvailable)
            .collect(Collectors.groupingBy(ParkingSpot::getSize, Collectors.counting()));
    }
}

// --- Ticket ---
public class ParkingTicket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSpot spot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal amountCharged;
    private TicketStatus status;

    public ParkingTicket(Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = UUID.randomUUID().toString();
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
        this.status = TicketStatus.ACTIVE;
    }

    public Duration getParkingDuration() {
        LocalDateTime end = exitTime != null ? exitTime : LocalDateTime.now();
        return Duration.between(entryTime, end);
    }

    // Getters + setters for exitTime, amountCharged, status
}

// --- Pricing Strategy (Strategy Pattern) ---
public interface PricingStrategy {
    BigDecimal calculateFee(ParkingTicket ticket);
}

public class HourlyPricingStrategy implements PricingStrategy {
    private static final Map<VehicleType, BigDecimal> HOURLY_RATES = Map.of(
        VehicleType.BIKE, BigDecimal.valueOf(20),
        VehicleType.CAR, BigDecimal.valueOf(50),
        VehicleType.TRUCK, BigDecimal.valueOf(100)
    );

    @Override
    public BigDecimal calculateFee(ParkingTicket ticket) {
        long hours = ticket.getParkingDuration().toHours() + 1; // round up
        BigDecimal rate = HOURLY_RATES.get(ticket.getVehicle().getType());
        return rate.multiply(BigDecimal.valueOf(hours));
    }
}

public class FlatRatePricingStrategy implements PricingStrategy {
    @Override
    public BigDecimal calculateFee(ParkingTicket ticket) {
        return BigDecimal.valueOf(200); // flat daily rate regardless of duration
    }
}

// --- The ParkingLot (Facade/Controller) ---
public class ParkingLot {
    private static ParkingLot instance; // Singleton
    private final String name;
    private final List<ParkingFloor> floors;
    private final PricingStrategy pricingStrategy;
    private final Map<String, ParkingTicket> activeTickets; // ticketId → ticket

    private ParkingLot(String name, List<ParkingFloor> floors, PricingStrategy pricing) {
        this.name = name;
        this.floors = floors;
        this.pricingStrategy = pricing;
        this.activeTickets = new ConcurrentHashMap<>();
    }

    public static synchronized ParkingLot getInstance(String name, List<ParkingFloor> floors,
                                                       PricingStrategy pricing) {
        if (instance == null) instance = new ParkingLot(name, floors, pricing);
        return instance;
    }

    // Entry
    public ParkingTicket parkVehicle(Vehicle vehicle) {
        ParkingSpot spot = floors.stream()
            .map(floor -> floor.findAvailableSpot(vehicle))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElseThrow(() -> new ParkingLotFullException("No spot available for " + vehicle.getType()));

        spot.park(vehicle);
        ParkingTicket ticket = new ParkingTicket(vehicle, spot);
        activeTickets.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    // Exit
    public BigDecimal processExit(String ticketId) {
        ParkingTicket ticket = activeTickets.get(ticketId);
        if (ticket == null) throw new InvalidTicketException(ticketId);

        ticket.setExitTime(LocalDateTime.now());
        BigDecimal fee = pricingStrategy.calculateFee(ticket);
        ticket.setAmountCharged(fee);
        ticket.setStatus(TicketStatus.PAID);

        ticket.getSpot().vacate();
        activeTickets.remove(ticketId);
        return fee;
    }

    public boolean isFull(VehicleType type) {
        // Check if any floor has available spot for this vehicle type
        return floors.stream().noneMatch(f -> f.findAvailableSpot(createDummyVehicle(type)).isPresent());
    }

    public Map<SpotSize, Long> getTotalAvailability() {
        return floors.stream()
            .flatMap(f -> f.getAvailabilityCount().entrySet().stream())
            .collect(Collectors.groupingBy(Map.Entry::getKey,
                     Collectors.summingLong(Map.Entry::getValue)));
    }
}
```

**Key design decisions to mention:**
- `PricingStrategy` as Strategy Pattern → swap pricing without changing `ParkingLot`
- `Vehicle.getRequiredSpotSize()` as polymorphism → no instanceof checks
- `ParkingLot` as Singleton → single source of truth
- `ConcurrentHashMap` for `activeTickets` → thread safety for concurrent entries/exits
- `SpotSize.ordinal()` comparison allows larger spots to accommodate smaller vehicles

---

## Q3 🔴 Design a Library Management System.

**Answer:**

```java
// --- Core Entities ---
public class Book {
    private final String isbn;
    private final String title;
    private final String author;
    private final int totalCopies;
    private int availableCopies;

    public Book(String isbn, String title, String author, int copies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public synchronized boolean isAvailable() { return availableCopies > 0; }

    public synchronized void checkout() {
        if (!isAvailable()) throw new BookNotAvailableException(isbn);
        availableCopies--;
    }

    public synchronized void returnBook() {
        if (availableCopies >= totalCopies) throw new IllegalStateException("All copies already returned");
        availableCopies++;
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public int getAvailableCopies() { return availableCopies; }
}

public class Member {
    private final String memberId;
    private final String name;
    private final String email;
    private final int maxBooksAllowed;
    private final List<BookLoan> activeLoans;

    public Member(String memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.maxBooksAllowed = 5; // business rule
        this.activeLoans = new ArrayList<>();
    }

    public boolean canBorrow() { return activeLoans.size() < maxBooksAllowed; }
    public void addLoan(BookLoan loan) { activeLoans.add(loan); }
    public void removeLoan(BookLoan loan) { activeLoans.remove(loan); }
    public List<BookLoan> getActiveLoans() { return Collections.unmodifiableList(activeLoans); }
    public String getMemberId() { return memberId; }
}

public class BookLoan {
    private final String loanId;
    private final Book book;
    private final Member member;
    private final LocalDate checkoutDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    public BookLoan(Book book, Member member, int loanDays) {
        this.loanId = UUID.randomUUID().toString();
        this.book = book;
        this.member = member;
        this.checkoutDate = LocalDate.now();
        this.dueDate = checkoutDate.plusDays(loanDays);
        this.status = LoanStatus.ACTIVE;
    }

    public boolean isOverdue() {
        return status == LoanStatus.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    public long overdueDays() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    public void returnBook() {
        this.returnDate = LocalDate.now();
        this.status = LoanStatus.RETURNED;
    }

    public String getLoanId() { return loanId; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
}

// --- Fine Calculation (Strategy Pattern) ---
public interface FineStrategy {
    BigDecimal calculateFine(BookLoan loan);
}

public class StandardFineStrategy implements FineStrategy {
    private static final BigDecimal FINE_PER_DAY = BigDecimal.valueOf(5); // ₹5/day

    @Override
    public BigDecimal calculateFine(BookLoan loan) {
        if (!loan.isOverdue()) return BigDecimal.ZERO;
        return FINE_PER_DAY.multiply(BigDecimal.valueOf(loan.overdueDays()));
    }
}

// --- Reservation (for waitlisted books) ---
public class Reservation {
    private final String reservationId;
    private final Book book;
    private final Member member;
    private final LocalDateTime reservedAt;
    private ReservationStatus status;

    public Reservation(Book book, Member member) {
        this.reservationId = UUID.randomUUID().toString();
        this.book = book;
        this.member = member;
        this.reservedAt = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
    }

    public void fulfill() { this.status = ReservationStatus.FULFILLED; }
    public void cancel() { this.status = ReservationStatus.CANCELLED; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
}

// --- Library (Main Controller) ---
public class Library {
    private final Map<String, Book> bookCatalog;        // isbn → Book
    private final Map<String, Member> members;          // memberId → Member
    private final Map<String, BookLoan> activeLoans;    // loanId → BookLoan
    private final Map<String, Queue<Reservation>> reservations; // isbn → waitlist queue
    private final FineStrategy fineStrategy;
    private static final int DEFAULT_LOAN_DAYS = 14;

    public Library(FineStrategy fineStrategy) {
        this.bookCatalog = new HashMap<>();
        this.members = new HashMap<>();
        this.activeLoans = new HashMap<>();
        this.reservations = new HashMap<>();
        this.fineStrategy = fineStrategy;
    }

    // Catalog management
    public void addBook(Book book) { bookCatalog.put(book.getIsbn(), book); }
    public void registerMember(Member member) { members.put(member.getMemberId(), member); }

    // Checkout
    public BookLoan checkoutBook(String memberId, String isbn) {
        Member member = findMember(memberId);
        Book book = findBook(isbn);

        if (!member.canBorrow()) throw new BorrowLimitExceededException(memberId);
        if (!book.isAvailable()) {
            // Auto-add to waitlist
            addReservation(isbn, member);
            throw new BookNotAvailableException(isbn + " — added to waitlist");
        }

        book.checkout();
        BookLoan loan = new BookLoan(book, member, DEFAULT_LOAN_DAYS);
        member.addLoan(loan);
        activeLoans.put(loan.getLoanId(), loan);
        return loan;
    }

    // Return
    public BigDecimal returnBook(String loanId) {
        BookLoan loan = activeLoans.get(loanId);
        if (loan == null) throw new LoanNotFoundException(loanId);

        BigDecimal fine = fineStrategy.calculateFine(loan);
        loan.returnBook();
        loan.getBook().returnBook();
        loan.getMember().removeLoan(loan);
        activeLoans.remove(loanId);

        // Notify next in waitlist if anyone is waiting
        notifyNextInWaitlist(loan.getBook().getIsbn());

        return fine; // caller collects fine
    }

    // Reservation / waitlist
    private void addReservation(String isbn, Member member) {
        reservations.computeIfAbsent(isbn, k -> new LinkedList<>())
                    .add(new Reservation(findBook(isbn), member));
    }

    private void notifyNextInWaitlist(String isbn) {
        Queue<Reservation> queue = reservations.get(isbn);
        if (queue == null || queue.isEmpty()) return;
        Reservation next = queue.poll();
        next.fulfill();
        // In real system: send email/SMS notification
        System.out.println("Notifying " + next.getMember().name + ": " + isbn + " is now available");
    }

    // Search
    public List<Book> searchByTitle(String title) {
        return bookCatalog.values().stream()
            .filter(b -> b.getTitle().toLowerCase().contains(title.toLowerCase()))
            .collect(Collectors.toList());
    }

    public List<Book> searchByAuthor(String author) {
        return bookCatalog.values().stream()
            .filter(b -> b.getAuthor().equalsIgnoreCase(author))
            .collect(Collectors.toList());
    }

    public List<BookLoan> getOverdueLoans() {
        return activeLoans.values().stream()
            .filter(BookLoan::isOverdue)
            .collect(Collectors.toList());
    }

    private Member findMember(String id) {
        return Optional.ofNullable(members.get(id))
            .orElseThrow(() -> new MemberNotFoundException(id));
    }

    private Book findBook(String isbn) {
        return Optional.ofNullable(bookCatalog.get(isbn))
            .orElseThrow(() -> new BookNotFoundException(isbn));
    }
}
```

---

## Q4 🔴 Design an ATM system.

**Answer:**

```java
// --- Card & Account ---
public class Card {
    private final String cardNumber;
    private final String maskedNumber; // show only last 4
    private final LocalDate expiryDate;
    private boolean blocked;

    public Card(String cardNumber, LocalDate expiryDate) {
        this.cardNumber = cardNumber;
        this.maskedNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        this.expiryDate = expiryDate;
        this.blocked = false;
    }

    public boolean isValid() { return !blocked && LocalDate.now().isBefore(expiryDate); }
    public void block() { this.blocked = true; }
    public String getMaskedNumber() { return maskedNumber; }
}

public class BankAccount {
    private final String accountNumber;
    private BigDecimal balance;
    private final String pin; // hashed in real system (BCrypt)
    private int failedAttempts;
    private static final int MAX_ATTEMPTS = 3;

    public BankAccount(String accountNumber, BigDecimal balance, String pin) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.pin = pin;
        this.failedAttempts = 0;
    }

    public boolean validatePin(String inputPin) {
        if (failedAttempts >= MAX_ATTEMPTS) throw new AccountLockedException(accountNumber);

        if (this.pin.equals(inputPin)) {
            failedAttempts = 0; // reset on success
            return true;
        }
        failedAttempts++;
        if (failedAttempts >= MAX_ATTEMPTS) throw new AccountLockedException(accountNumber);
        return false;
    }

    public synchronized void withdraw(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) throw new InsufficientFundsException();
        balance = balance.subtract(amount);
    }

    public synchronized void deposit(BigDecimal amount) { balance = balance.add(amount); }
    public BigDecimal getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; }
}

// --- Transaction ---
public class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime timestamp;
    private final BigDecimal balanceAfter;
    private final TransactionStatus status;

    public Transaction(TransactionType type, BigDecimal amount, BigDecimal balanceAfter) {
        this.transactionId = UUID.randomUUID().toString();
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.balanceAfter = balanceAfter;
        this.status = TransactionStatus.SUCCESS;
    }
}

// --- ATM State Machine ---
// ATM behaviour changes depending on which state it's in
// State Pattern maps perfectly here

public interface ATMState {
    void insertCard(ATM atm, Card card);
    void enterPin(ATM atm, String pin);
    void selectTransaction(ATM atm, TransactionType type);
    void processTransaction(ATM atm, BigDecimal amount);
    void ejectCard(ATM atm);
}

public class IdleState implements ATMState {
    @Override
    public void insertCard(ATM atm, Card card) {
        if (!card.isValid()) {
            atm.showMessage("Card is expired or blocked");
            return;
        }
        atm.setCurrentCard(card);
        atm.setState(new CardInsertedState());
        atm.showMessage("Please enter your PIN");
    }

    @Override public void enterPin(ATM atm, String pin) { atm.showMessage("Please insert card first"); }
    @Override public void selectTransaction(ATM atm, TransactionType t) { atm.showMessage("Insert card first"); }
    @Override public void processTransaction(ATM atm, BigDecimal a) { atm.showMessage("Insert card first"); }
    @Override public void ejectCard(ATM atm) { atm.showMessage("No card inserted"); }
}

public class CardInsertedState implements ATMState {
    @Override public void insertCard(ATM atm, Card card) { atm.showMessage("Card already inserted"); }

    @Override
    public void enterPin(ATM atm, String pin) {
        BankAccount account = atm.getAccountForCard(atm.getCurrentCard());
        try {
            if (account.validatePin(pin)) {
                atm.setCurrentAccount(account);
                atm.setState(new AuthenticatedState());
                atm.showMessage("Authentication successful. Select transaction.");
            } else {
                atm.showMessage("Incorrect PIN. " + (3 - account.failedAttempts) + " attempts remaining.");
            }
        } catch (AccountLockedException e) {
            atm.getCurrentCard().block();
            atm.showMessage("Account locked — too many wrong attempts. Card blocked.");
            atm.setState(new IdleState());
        }
    }

    @Override public void selectTransaction(ATM atm, TransactionType t) { atm.showMessage("Enter PIN first"); }
    @Override public void processTransaction(ATM atm, BigDecimal a) { atm.showMessage("Enter PIN first"); }
    @Override public void ejectCard(ATM atm) { atm.setCurrentCard(null); atm.setState(new IdleState()); }
}

public class AuthenticatedState implements ATMState {
    @Override public void insertCard(ATM atm, Card card) { atm.showMessage("Already authenticated"); }
    @Override public void enterPin(ATM atm, String pin) { atm.showMessage("Already authenticated"); }

    @Override
    public void selectTransaction(ATM atm, TransactionType type) {
        atm.setSelectedTransaction(type);
        atm.showMessage("Enter amount for " + type);
    }

    @Override
    public void processTransaction(ATM atm, BigDecimal amount) {
        BankAccount account = atm.getCurrentAccount();
        try {
            switch (atm.getSelectedTransaction()) {
                case WITHDRAW:
                    if (!atm.hasSufficientCash(amount)) {
                        atm.showMessage("ATM does not have enough cash");
                        return;
                    }
                    account.withdraw(amount);
                    atm.dispenseCash(amount);
                    atm.recordTransaction(new Transaction(TransactionType.WITHDRAW, amount, account.getBalance()));
                    atm.showMessage("Dispensing " + amount + ". Balance: " + account.getBalance());
                    break;
                case BALANCE_INQUIRY:
                    atm.showMessage("Balance: " + account.getBalance());
                    break;
                case DEPOSIT:
                    account.deposit(amount);
                    atm.recordTransaction(new Transaction(TransactionType.DEPOSIT, amount, account.getBalance()));
                    atm.showMessage("Deposited " + amount + ". Balance: " + account.getBalance());
                    break;
            }
        } catch (InsufficientFundsException e) {
            atm.showMessage("Insufficient funds. Balance: " + account.getBalance());
        }
    }

    @Override
    public void ejectCard(ATM atm) {
        atm.setCurrentCard(null);
        atm.setCurrentAccount(null);
        atm.setState(new IdleState());
        atm.showMessage("Card ejected. Thank you!");
    }
}

// --- ATM Machine ---
public class ATM {
    private ATMState currentState;
    private Card currentCard;
    private BankAccount currentAccount;
    private TransactionType selectedTransaction;
    private BigDecimal cashAvailable;
    private final Map<String, BankAccount> cardToAccountMap; // cardNumber → account
    private final List<Transaction> transactionLog;

    public ATM(BigDecimal initialCash) {
        this.currentState = new IdleState();
        this.cashAvailable = initialCash;
        this.cardToAccountMap = new HashMap<>();
        this.transactionLog = new ArrayList<>();
    }

    // Delegating all operations to current state
    public void insertCard(Card card) { currentState.insertCard(this, card); }
    public void enterPin(String pin) { currentState.enterPin(this, pin); }
    public void selectTransaction(TransactionType type) { currentState.selectTransaction(this, type); }
    public void processTransaction(BigDecimal amount) { currentState.processTransaction(this, amount); }
    public void ejectCard() { currentState.ejectCard(this); }

    // Internal helpers
    public boolean hasSufficientCash(BigDecimal amount) { return cashAvailable.compareTo(amount) >= 0; }
    public void dispenseCash(BigDecimal amount) { cashAvailable = cashAvailable.subtract(amount); }
    public void recordTransaction(Transaction t) { transactionLog.add(t); }
    public void showMessage(String message) { System.out.println("[ATM]: " + message); }
    public BankAccount getAccountForCard(Card card) { return cardToAccountMap.get(card.getMaskedNumber()); }
    public void registerCard(Card card, BankAccount account) { cardToAccountMap.put(card.getMaskedNumber(), account); }

    // Setters/getters
    public void setState(ATMState state) { this.currentState = state; }
    public void setCurrentCard(Card card) { this.currentCard = card; }
    public void setCurrentAccount(BankAccount account) { this.currentAccount = account; }
    public void setSelectedTransaction(TransactionType type) { this.selectedTransaction = type; }
    public Card getCurrentCard() { return currentCard; }
    public BankAccount getCurrentAccount() { return currentAccount; }
    public TransactionType getSelectedTransaction() { return selectedTransaction; }
}
```

---

## Q5 🔴 Design a Notification System that supports Email, SMS, and Push — extensible for new channels.

**Answer:**

```java
// --- Notification Model ---
public class Notification {
    private final String notificationId;
    private final String recipientId;
    private final String subject;
    private final String body;
    private final NotificationPriority priority;
    private final Map<String, String> metadata; // flexible extra data

    public Notification(String recipientId, String subject, String body, NotificationPriority priority) {
        this.notificationId = UUID.randomUUID().toString();
        this.recipientId = recipientId;
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.metadata = new HashMap<>();
    }

    public Notification withMetadata(String key, String value) {
        this.metadata.put(key, value);
        return this; // fluent API
    }
    // Getters...
}

// --- Channel abstraction ---
public interface NotificationChannel {
    void send(Notification notification, UserContactInfo contactInfo);
    NotificationChannelType getChannelType();
    boolean supports(NotificationChannelType type);
}

// --- Channel implementations ---
public class EmailChannel implements NotificationChannel {
    private final EmailClient emailClient; // injected

    public EmailChannel(EmailClient emailClient) { this.emailClient = emailClient; }

    @Override
    public void send(Notification n, UserContactInfo contact) {
        if (contact.getEmail() == null) throw new MissingContactInfoException("email");
        emailClient.sendEmail(
            contact.getEmail(),
            n.getSubject(),
            n.getBody()
        );
    }

    @Override public NotificationChannelType getChannelType() { return NotificationChannelType.EMAIL; }
    @Override public boolean supports(NotificationChannelType type) { return type == NotificationChannelType.EMAIL; }
}

public class SmsChannel implements NotificationChannel {
    private final SmsGateway smsGateway;

    public SmsChannel(SmsGateway smsGateway) { this.smsGateway = smsGateway; }

    @Override
    public void send(Notification n, UserContactInfo contact) {
        if (contact.getPhoneNumber() == null) throw new MissingContactInfoException("phone");
        // SMS is short — truncate if needed
        String message = n.getBody().length() > 160 ? n.getBody().substring(0, 157) + "..." : n.getBody();
        smsGateway.send(contact.getPhoneNumber(), message);
    }

    @Override public NotificationChannelType getChannelType() { return NotificationChannelType.SMS; }
    @Override public boolean supports(NotificationChannelType type) { return type == NotificationChannelType.SMS; }
}

public class PushChannel implements NotificationChannel {
    private final PushService pushService;

    @Override
    public void send(Notification n, UserContactInfo contact) {
        if (contact.getDeviceToken() == null) throw new MissingContactInfoException("device token");
        pushService.sendPush(contact.getDeviceToken(), n.getSubject(), n.getBody(),
            n.getMetadata().get("deepLink"));
    }

    @Override public NotificationChannelType getChannelType() { return NotificationChannelType.PUSH; }
    @Override public boolean supports(NotificationChannelType type) { return type == NotificationChannelType.PUSH; }
}
// Adding WhatsApp, Slack: just create new Channel class — nothing else changes

// --- User preferences ---
public class UserNotificationPreferences {
    private final String userId;
    private final Set<NotificationChannelType> enabledChannels;
    private final Map<NotificationType, Set<NotificationChannelType>> typePreferences;
    private boolean doNotDisturb;
    private LocalTime dndStart;
    private LocalTime dndEnd;

    public boolean canSend(NotificationType type, NotificationChannelType channel) {
        if (doNotDisturb && isWithinDndWindow()) return false;
        if (!enabledChannels.contains(channel)) return false;
        Set<NotificationChannelType> preferred = typePreferences.get(type);
        return preferred == null || preferred.contains(channel);
    }

    private boolean isWithinDndWindow() {
        LocalTime now = LocalTime.now();
        return now.isAfter(dndStart) && now.isBefore(dndEnd);
    }
}

// --- Retry / Fallback decorator ---
public class RetryableChannel implements NotificationChannel {
    private final NotificationChannel delegate;
    private final int maxRetries;
    private final long retryDelayMs;

    public RetryableChannel(NotificationChannel delegate, int maxRetries, long retryDelayMs) {
        this.delegate = delegate;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public void send(Notification notification, UserContactInfo contactInfo) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                delegate.send(notification, contactInfo);
                return; // success
            } catch (Exception e) {
                if (attempt == maxRetries) throw new NotificationDeliveryException(
                    "Failed after " + maxRetries + " attempts", e);
                try { Thread.sleep(retryDelayMs * attempt); } // exponential backoff
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); throw new RuntimeException(ie); }
            }
        }
    }

    @Override public NotificationChannelType getChannelType() { return delegate.getChannelType(); }
    @Override public boolean supports(NotificationChannelType type) { return delegate.supports(type); }
}

// --- Notification Service ---
@Service
public class NotificationService {
    private final Map<NotificationChannelType, NotificationChannel> channels;
    private final UserContactRepository contactRepo;
    private final UserPreferencesRepository prefsRepo;

    public NotificationService(List<NotificationChannel> channelList,
                                UserContactRepository contactRepo,
                                UserPreferencesRepository prefsRepo) {
        this.channels = channelList.stream()
            .collect(Collectors.toMap(NotificationChannel::getChannelType, c -> c));
        this.contactRepo = contactRepo;
        this.prefsRepo = prefsRepo;
    }

    public void send(Notification notification, List<NotificationChannelType> requestedChannels) {
        UserContactInfo contact = contactRepo.findByUserId(notification.getRecipientId());
        UserNotificationPreferences prefs = prefsRepo.findByUserId(notification.getRecipientId());

        List<String> errors = new ArrayList<>();
        boolean atLeastOneSent = false;

        for (NotificationChannelType channelType : requestedChannels) {
            if (!prefs.canSend(NotificationType.GENERAL, channelType)) continue;

            NotificationChannel channel = channels.get(channelType);
            if (channel == null) { errors.add("Unknown channel: " + channelType); continue; }

            try {
                channel.send(notification, contact);
                atLeastOneSent = true;
            } catch (MissingContactInfoException e) {
                errors.add("Missing info for " + channelType + ": " + e.getMessage());
            } catch (NotificationDeliveryException e) {
                errors.add("Delivery failed for " + channelType + ": " + e.getMessage());
            }
        }

        if (!atLeastOneSent && !errors.isEmpty()) {
            throw new AllChannelsFailedException(errors);
        }
    }

    // Convenience method
    public void sendToAllChannels(Notification notification) {
        send(notification, List.of(NotificationChannelType.values()));
    }
}
```

---

## Q6 🔴 Design an Elevator System.

**Answer:**

```java
public enum Direction { UP, DOWN, IDLE }
public enum ElevatorStatus { MOVING, STOPPED, MAINTENANCE }

public class Elevator {
    private final int elevatorId;
    private int currentFloor;
    private Direction direction;
    private ElevatorStatus status;
    private final int minFloor;
    private final int maxFloor;
    private final TreeSet<Integer> upQueue;   // floors to serve going up (sorted ascending)
    private final TreeSet<Integer> downQueue; // floors to serve going down (sorted descending)

    public Elevator(int id, int minFloor, int maxFloor) {
        this.elevatorId = id;
        this.currentFloor = minFloor;
        this.direction = Direction.IDLE;
        this.status = ElevatorStatus.STOPPED;
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.upQueue = new TreeSet<>();
        this.downQueue = new TreeSet<>(Comparator.reverseOrder());
    }

    public void addRequest(int floor) {
        if (floor > currentFloor || (direction == Direction.UP)) upQueue.add(floor);
        else downQueue.add(floor);
    }

    // SCAN algorithm (elevator algorithm) — serves requests in current direction first
    public void step() {
        if (status == ElevatorStatus.MAINTENANCE) return;

        if (direction == Direction.UP || direction == Direction.IDLE) {
            if (!upQueue.isEmpty()) {
                int nextFloor = upQueue.first(); // lowest floor above current
                moveToFloor(nextFloor);
                upQueue.remove(nextFloor);
                if (upQueue.isEmpty() && !downQueue.isEmpty()) {
                    direction = Direction.DOWN;
                } else if (upQueue.isEmpty()) {
                    direction = Direction.IDLE;
                }
            } else if (!downQueue.isEmpty()) {
                direction = Direction.DOWN;
                step(); // serve downward requests
            }
        } else { // DOWN
            if (!downQueue.isEmpty()) {
                int nextFloor = downQueue.first(); // highest floor below current
                moveToFloor(nextFloor);
                downQueue.remove(nextFloor);
                if (downQueue.isEmpty() && !upQueue.isEmpty()) {
                    direction = Direction.UP;
                } else if (downQueue.isEmpty()) {
                    direction = Direction.IDLE;
                }
            } else if (!upQueue.isEmpty()) {
                direction = Direction.UP;
                step();
            }
        }
    }

    private void moveToFloor(int floor) {
        System.out.println("Elevator " + elevatorId + " moving from " + currentFloor + " to " + floor);
        this.currentFloor = floor;
        this.status = ElevatorStatus.STOPPED;
        openDoor();
    }

    private void openDoor() {
        System.out.println("Elevator " + elevatorId + " doors open at floor " + currentFloor);
        // simulate door open/close
    }

    // How "cost" is calculated for dispatcher — lower = better candidate
    public int costToServe(int requestFloor) {
        int distance = Math.abs(currentFloor - requestFloor);
        // If request is in same direction as current travel — very cheap
        if (direction == Direction.IDLE) return distance;
        if (direction == Direction.UP && requestFloor >= currentFloor) return distance;
        if (direction == Direction.DOWN && requestFloor <= currentFloor) return distance;
        // Request is behind us — expensive (must finish current direction first)
        return distance + maxFloor; // penalty for direction reversal
    }

    public int getCurrentFloor() { return currentFloor; }
    public Direction getDirection() { return direction; }
    public boolean isIdle() { return direction == Direction.IDLE; }
    public int getElevatorId() { return elevatorId; }
}

// --- Dispatcher — decides which elevator serves each request ---
public class ElevatorDispatcher {
    private final List<Elevator> elevators;

    public ElevatorDispatcher(List<Elevator> elevators) {
        this.elevators = elevators;
    }

    public void handleExternalRequest(int floor, Direction direction) {
        // Find elevator with minimum cost to serve this request
        Elevator best = elevators.stream()
            .filter(e -> e.getStatus() != ElevatorStatus.MAINTENANCE)
            .min(Comparator.comparingInt(e -> e.costToServe(floor)))
            .orElseThrow(() -> new NoElevatorAvailableException());

        best.addRequest(floor);
        System.out.println("Elevator " + best.getElevatorId() + " assigned to floor " + floor);
    }

    public void handleInternalRequest(int elevatorId, int destinationFloor) {
        elevators.stream()
            .filter(e -> e.getElevatorId() == elevatorId)
            .findFirst()
            .ifPresent(e -> e.addRequest(destinationFloor));
    }

    // Simulate one step for all elevators
    public void tick() { elevators.forEach(Elevator::step); }
}

// --- Building ---
public class Building {
    private final int totalFloors;
    private final ElevatorDispatcher dispatcher;

    public Building(int totalFloors, int elevatorCount) {
        this.totalFloors = totalFloors;
        List<Elevator> elevators = IntStream.rangeClosed(1, elevatorCount)
            .mapToObj(id -> new Elevator(id, 1, totalFloors))
            .collect(Collectors.toList());
        this.dispatcher = new ElevatorDispatcher(elevators);
    }

    public void pressHallButton(int floor, Direction direction) {
        dispatcher.handleExternalRequest(floor, direction);
    }

    public void pressFloorButton(int elevatorId, int floor) {
        dispatcher.handleInternalRequest(elevatorId, floor);
    }

    public void simulate(int steps) {
        for (int i = 0; i < steps; i++) {
            dispatcher.tick();
        }
    }
}
```

---

## Q7 🟡 Design an Order Management System for an e-commerce application.

**Answer:**

```java
// --- Value Objects (immutable) ---
public record Money(BigDecimal amount, String currency) {
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) throw new CurrencyMismatchException();
        return new Money(this.amount.add(other.amount), this.currency);
    }
    public Money multiply(int quantity) { return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency); }
}

public record Address(String line1, String line2, String city, String state, String pincode, String country) {}

// --- Order Item ---
public class OrderItem {
    private final String productId;
    private final String productName;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(String productId, String productName, int quantity, Money unitPrice) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Money getTotalPrice() { return unitPrice.multiply(quantity); }
    public String getProductId() { return productId; }
    public int getQuantity() { return quantity; }
}

// --- Order State Machine ---
// Order transitions: CREATED → PAYMENT_PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
//                  At any point before SHIPPED: → CANCELLED
//                  After DELIVERED: → RETURN_REQUESTED → REFUNDED

public enum OrderStatus {
    CREATED, PAYMENT_PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED,
    CANCELLED, RETURN_REQUESTED, REFUNDED
}

public class Order {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Address shippingAddress;
    private String trackingNumber;
    private final LocalDateTime createdAt;
    private final List<OrderEvent> eventLog; // full audit trail

    public Order(String customerId, List<OrderItem> items, Address shippingAddress) {
        this.orderId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = OrderStatus.CREATED;
        this.shippingAddress = shippingAddress;
        this.createdAt = LocalDateTime.now();
        this.eventLog = new ArrayList<>();
        recordEvent("Order created");
    }

    // State transitions — validate before transitioning
    public void confirmPayment() {
        assertStatus(OrderStatus.PAYMENT_PENDING, OrderStatus.CREATED);
        this.status = OrderStatus.CONFIRMED;
        recordEvent("Payment confirmed");
    }

    public void startProcessing() {
        assertStatus(OrderStatus.CONFIRMED);
        this.status = OrderStatus.PROCESSING;
        recordEvent("Order processing started");
    }

    public void ship(String trackingNumber) {
        assertStatus(OrderStatus.PROCESSING);
        this.status = OrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        recordEvent("Shipped with tracking: " + trackingNumber);
    }

    public void deliver() {
        assertStatus(OrderStatus.SHIPPED);
        this.status = OrderStatus.DELIVERED;
        recordEvent("Delivered to customer");
    }

    public void cancel(String reason) {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateTransitionException("Cannot cancel shipped/delivered order");
        }
        if (status == OrderStatus.CANCELLED) throw new InvalidOrderStateTransitionException("Already cancelled");
        this.status = OrderStatus.CANCELLED;
        recordEvent("Cancelled: " + reason);
    }

    public void requestReturn(String reason) {
        assertStatus(OrderStatus.DELIVERED);
        this.status = OrderStatus.RETURN_REQUESTED;
        recordEvent("Return requested: " + reason);
    }

    public Money getTotalAmount() {
        return items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(new Money(BigDecimal.ZERO, "INR"), Money::add);
    }

    private void assertStatus(OrderStatus... allowedStatuses) {
        for (OrderStatus allowed : allowedStatuses) {
            if (this.status == allowed) return;
        }
        throw new InvalidOrderStateTransitionException(
            "Cannot transition from " + status + ". Allowed: " + Arrays.toString(allowedStatuses));
    }

    private void recordEvent(String description) {
        eventLog.add(new OrderEvent(orderId, description, LocalDateTime.now()));
    }

    public String getOrderId() { return orderId; }
    public OrderStatus getStatus() { return status; }
    public List<OrderEvent> getEventLog() { return Collections.unmodifiableList(eventLog); }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
}
```

---
