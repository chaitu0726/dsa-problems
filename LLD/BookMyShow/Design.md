# BookMyShow Ticket Booking System - LLD

---

# Requirements

## Core Features

1. Search movies by city
2. View theatres and shows
3. View seat layout
4. Select seats
5. Lock seats temporarily
6. Make payment
7. Confirm booking
8. Cancel booking
9. View booking history

---

## Non-Functional Requirements

1. No double booking
2. Handle concurrent users
3. Seat locking with timeout
4. Consistency of booking state
5. Extensible payment system
6. Scalable architecture
7. Failure handling and retries
8. High availability

---

# High Level Design

```text
City
 └── Theatre
      └── Audi
           └── Seat

Movie
Show
 └── ShowSeat

User
Booking
Payment
SeatLock
```

---

# Core Idea

## Important Design Decision

Seat availability is NOT stored inside Seat.

Reason:
- Seat A1 can be booked for one show
- Same A1 can be available for another show

Therefore:
- Seat = Physical seat
- ShowSeat = Seat status for a specific show

---

# Entities & Responsibilities

| Entity | Responsibility |
|---|---|
| City | Contains theatres |
| Theatre | Contains audis and shows |
| Audi | Physical screen/auditorium |
| Seat | Physical seat |
| Movie | Movie metadata |
| Show | Movie timing |
| ShowSeat | Seat availability for show |
| User | Booking owner |
| Booking | Ticket booking |
| Payment | Payment details |
| SeatLock | Temporary seat lock |

---

# Relationships

```text
City
 └── Theatre
      └── Audi
           └── Seat

Movie
 └── Show
      └── ShowSeat

User
 └── Booking
      └── Payment
```

---

# Booking Flow

```text
User selects seats
        ↓
Seats locked temporarily
        ↓
Booking created with PENDING status
        ↓
Payment initiated
        ↓
Payment success callback
        ↓
Booking CONFIRMED
        ↓
Seats marked BOOKED
```

---

# Concurrency Handling

## Problem
Two users trying to book same seat simultaneously.

## Solution

Use:
- DB row level locking
OR
- Optimistic locking
OR
- Distributed locking (Redis)

Example:

```sql
SELECT * FROM show_seat
WHERE id = ?
FOR UPDATE;
```

Booking and seat updates should happen inside transaction.

---

# Seat Locking

## Why Needed?

Prevent:
- race conditions
- double booking

## Flow

```text
User selects seats
    ↓
Seats become LOCKED
    ↓
Lock expires after 2 mins
    ↓
Seats become AVAILABLE again
```

---

# Extensibility

## Easily Add

- New payment methods
- Dynamic pricing
- Coupons
- Notifications
- Multi-language support

---

# Design Patterns Used

| Pattern | Usage |
|---|---|
| Strategy Pattern | Payment methods |
| Factory Pattern | Payment object creation |
| Facade Pattern | BookingSystem APIs |
| Singleton (optional) | Services |

---

# Enums

```java
enum SeatStatus {
    AVAILABLE,
    LOCKED,
    BOOKED
}

enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    FAILED,
    EXPIRED
}

enum PaymentStatus {
    INITIATED,
    SUCCESS,
    FAILED,
    REFUNDED
}

enum SeatType {
    GOLD,
    SILVER,
    PLATINUM
}

enum PaymentMode {
    UPI,
    CREDIT_CARD
}




// # Models

// ## User


class User {

    private String userId;

    private String name;

    private String email;

}

// ## City


class City {

    private String cityId;

    private String name;

    private List<Theatre> theatres;

}




// ## Theatre


class Theatre {

    private String theatreId;

    private String name;

    private String city;

    private List<Audi> audis;

}


// ## Audi

class Audi {

    private String audiId;

    private String name;

    private List<Seat> seats;

}

// ## Seat

class Seat {

    private String seatId;

    private int row;

    private int column;

    private SeatType type;

}


// ## Movie

class Movie {

    private String movieId;

    private String name;

    private int durationInMins;

    private String language;

}

// ## Show

class Show {

    private String showId;

    private Movie movie;

    private Audi audi;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private List<ShowSeat> showSeats;

}

// ## ShowSeat

class ShowSeat {

    private String showSeatId;

    private Show show;

    private Seat seat;

    private SeatStatus status;

    private double price;

}

// ## SeatLock

class SeatLock {

    private String lockId;

    private ShowSeat showSeat;

    private User lockedBy;

    private LocalDateTime lockedAt;

    private LocalDateTime expiryTime;

}

// ## Payment

class Payment {

    private String paymentId;

    private double amount;

    private PaymentMode mode;

    private PaymentStatus status;

}

// ## Booking

class Booking {

    private String bookingId;

    private User user;

    private Show show;

    private List<ShowSeat> seats;

    private BookingStatus status;

    private Payment payment;

    private LocalDateTime createdAt;

}

// # Services

// ## MovieService

class MovieService {

    public List<Movie> getMoviesByCity(String city) {

        return null;
    }

    public List<Show> getShows(Movie movie) {

        return null;
    }

}

// ## SeatLockService

class SeatLockService {

    public boolean lockSeats(
            User user,
            List<ShowSeat> seats
    ) {

        return true;
    }

    public void unlockExpiredSeats() {

    }

}

// ## BookingService

class BookingService {

    public Booking createBooking(
            User user,
            Show show,
            List<ShowSeat> seats
    ) {

        return null;
    }

    public void cancelBooking(
            String bookingId
    ) {

    }

}

// # Payment Strategy Pattern

// ## PaymentStrategy

abstract class PaymentStrategy {

    public abstract Payment pay(
            double amount
    );

}

// ## UPI Payment


class UPIPayment extends PaymentStrategy {

    @Override
    public Payment pay(double amount) {

        return null;
    }

}


// ## Credit Card Payment


class CreditCardPayment extends PaymentStrategy {

    @Override
    public Payment pay(double amount) {

        return null;
    }

}

// ## Payment Factory


class PaymentFactory {

    public static PaymentStrategy getStrategy(
            PaymentMode mode
    ) {

        if(mode == PaymentMode.UPI) {
            return new UPIPayment();
        }

        return new CreditCardPayment();
    }

}

// ## Payment Service

class PaymentService {

    public Payment processPayment(
            double amount,
            PaymentMode mode
    ) {

        PaymentStrategy strategy =
                PaymentFactory.getStrategy(mode);

        return strategy.pay(amount);
    }

}

// # Main Facade

class BookingSystem {

    private MovieService movieService;

    private BookingService bookingService;

    private PaymentService paymentService;

    private SeatLockService seatLockService;

}
```

---

# Important Interview Discussion Points

## 1. Why ShowSeat?

Because seat availability is show-specific.

---

## 2. How Prevent Double Booking?

Using:
- Transactions
- Row locking
- Distributed locking

---

## 3. Why Booking Status Needed?

Because payment is asynchronous.

Booking lifecycle:

```text
PENDING → CONFIRMED
PENDING → FAILED
CONFIRMED → CANCELLED
```

---

## 4. What Happens If Payment Fails?

- Booking becomes FAILED
- Seats unlocked automatically

---

## 5. How Seat Lock Expiry Works?

Background scheduler/cron:
- checks expired locks
- releases seats

---

# Scalability Improvements

## Can Add Later

### Caching
- Redis for movie/show cache

### Message Queue
- Kafka/RabbitMQ for notifications

### Database Scaling
- Read replicas

### Search Optimization
- Elasticsearch

---

# APIs (Optional)

## Search Movies

```text
GET /movies?city=Mumbai
```

## Get Shows

```text
GET /shows/{movieId}
```

## Lock Seats

```text
POST /seats/lock
```

## Book Ticket

```text
POST /booking
```

## Cancel Booking

```text
POST /booking/cancel
```

---

# Final Notes

## Most Important Part Of This LLD

Interviewers mainly evaluate:

1. Entity modeling
2. Seat locking
3. Concurrency handling
4. Booking lifecycle
5. Transaction management

The hardest part is NOT payment.

The hardest part is:
- avoiding double booking
- ensuring consistency under concurrency
- handling failures correctly
