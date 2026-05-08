## 1. Requirement Clarification

```
Before coding, ask questions like:

Multiple floors?
Different vehicle types?
Different parking spot types?
Ticket generation?
Payment support?
Entry/Exit gates?
Reservation support?
Real-time availability?
Pricing strategy?

This shows engineering maturity.

Typical Parking Lot Requirements
Functional Requirements
Vehicle enters parking lot
Allocate nearest available spot
Generate ticket
Vehicle exits
Calculate parking fees
Free the spot
Vehicle Types
Bike
Car
Truck
Spot Types
Bike spot
Compact spot
Large spot
What Good LLD Answers Include
```

## 1. Proper Classes

Entities modeled correctly.

## 2. Interfaces / Abstractions
```
Like:

ParkingStrategy
PaymentStrategy
```
## 3. Extensibility
```Can we add:

EV parking?
New payment modes?
Reservation?
Dynamic pricing?

without changing existing code much?
```
## Step-by-Step Thought Process

## Step 1: Identify Core Entities
```
For Parking Lot:

Entity	Responsibility
ParkingLot	Main controller
ParkingFloor	Contains spots
ParkingSpot	Individual spot
Vehicle	Vehicle details
Ticket	Entry info
EntranceGate	Entry operations
ExitGate	Exit operations
Payment	Fee processing
```
## Step 2: Find Relationships
```
ParkingLot
 ├── Floors
       ├── Spots
Vehicle -> assigned Spot
Vehicle -> Ticket
Exit -> Payment
```
## Step 3: Use Inheritance Smartly
```
Vehicle Hierarchy
Vehicle
 ├── Car
 ├── Bike
 └── Truck
Spot Hierarchy
ParkingSpot
 ├── BikeSpot
 ├── CompactSpot
 └── LargeSpot
```
## Step 4: Add Interfaces for Extensibility
```
This is where many candidates fail.

Instead of hardcoding logic:

BAD:

if(vehicle == CAR)

GOOD:

ParkingStrategy
PaymentStrategy
FeeStrategy
```
## Recommended LLD Structure
```
UML-ish View
Vehicle
  ↑
Car, Bike, Truck

ParkingSpot
  ↑
BikeSpot, CompactSpot, LargeSpot

ParkingLot
 └── ParkingFloor
        └── ParkingSpot

Ticket
Payment

Interfaces:
- ParkingStrategy
- FeeStrategy
- PaymentStrategy
```

## COMPLETE TEMPLATE (Interview Ready)
```java
// 1. Vehicle
enum VehicleType {
    BIKE,
    CAR,
    TRUCK
}

abstract class Vehicle {
    String number;
    VehicleType type;

    Vehicle(String number, VehicleType type) {
        this.number = number;
        this.type = type;
    }

    public VehicleType getType() {
        return type;
    }
}

class Car extends Vehicle {
    Car(String number) {
        super(number, VehicleType.CAR);
    }
}

class Bike extends Vehicle {
    Bike(String number) {
        super(number, VehicleType.BIKE);
    }
}
// 2. Parking Spot
enum SpotType {
    BIKE,
    COMPACT,
    LARGE
}

abstract class ParkingSpot {
    int id;
    SpotType type;
    boolean occupied;
    Vehicle vehicle;

    ParkingSpot(int id, SpotType type) {
        this.id = id;
        this.type = type;
    }

    boolean isAvailable() {
        return !occupied;
    }

    void parkVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        occupied = true;
    }

    void removeVehicle() {
        vehicle = null;
        occupied = false;
    }

    abstract boolean canFitVehicle(Vehicle vehicle);
}
// 3. Spot Implementations
class CompactSpot extends ParkingSpot {

    CompactSpot(int id) {
        super(id, SpotType.COMPACT);
    }

    @Override
    boolean canFitVehicle(Vehicle vehicle) {
        return vehicle.getType() == VehicleType.CAR;
    }
}

class BikeSpot extends ParkingSpot {

    BikeSpot(int id) {
        super(id, SpotType.BIKE);
    }

    @Override
    boolean canFitVehicle(Vehicle vehicle) {
        return vehicle.getType() == VehicleType.BIKE;
    }
}
// 4. Ticket
class Ticket {
    String ticketId;
    Vehicle vehicle;
    ParkingSpot spot;
    long entryTime;

    Ticket(String ticketId, Vehicle vehicle, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = System.currentTimeMillis();
    }
}
// 5. Parking Floor
class ParkingFloor {

    int floorNumber;
    List<ParkingSpot> spots;

    ParkingFloor(int floorNumber, List<ParkingSpot> spots) {
        this.floorNumber = floorNumber;
        this.spots = spots;
    }

    ParkingSpot findAvailableSpot(Vehicle vehicle) {

        for(ParkingSpot spot : spots) {

            if(spot.isAvailable() &&
               spot.canFitVehicle(vehicle)) {

                return spot;
            }
        }

        return null;
    }
}
// 6. Parking Strategy (IMPORTANT)

Interviewers LOVE this.

interface ParkingStrategy {
    ParkingSpot findSpot(List<ParkingFloor> floors,
                         Vehicle vehicle);
}
Default Strategy
class NearestParkingStrategy
        implements ParkingStrategy {

    @Override
    public ParkingSpot findSpot(List<ParkingFloor> floors,
                                Vehicle vehicle) {

        for(ParkingFloor floor : floors) {

            ParkingSpot spot =
                floor.findAvailableSpot(vehicle);

            if(spot != null) {
                return spot;
            }
        }

        return null;
    }
}
// 7. Fee Strategy
interface FeeStrategy {
    double calculateFee(Ticket ticket);
}
Hourly Fee Strategy
class HourlyFeeStrategy
        implements FeeStrategy {

    @Override
    public double calculateFee(Ticket ticket) {

        long duration =
            System.currentTimeMillis()
            - ticket.entryTime;

        long hours =
            (duration / (1000 * 60 * 60)) + 1;

        return hours * 20;
    }
}
// 8. Parking Lot (MAIN CLASS)
class ParkingLot {

    List<ParkingFloor> floors;
    ParkingStrategy parkingStrategy;
    FeeStrategy feeStrategy;

    Map<String, Ticket> activeTickets =
            new HashMap<>();

    ParkingLot(List<ParkingFloor> floors,
               ParkingStrategy parkingStrategy,
               FeeStrategy feeStrategy) {

        this.floors = floors;
        this.parkingStrategy = parkingStrategy;
        this.feeStrategy = feeStrategy;
    }

    Ticket parkVehicle(Vehicle vehicle) {

        ParkingSpot spot =
            parkingStrategy.findSpot(floors,
                                     vehicle);

        if(spot == null) {
            throw new RuntimeException(
                "Parking Full");
        }

        spot.parkVehicle(vehicle);

        Ticket ticket =
            new Ticket(UUID.randomUUID().toString(),
                       vehicle,
                       spot);

        activeTickets.put(ticket.ticketId,
                          ticket);

        return ticket;
    }

    double exitVehicle(String ticketId) {

        Ticket ticket =
            activeTickets.get(ticketId);

        double fee =
            feeStrategy.calculateFee(ticket);

        ticket.spot.removeVehicle();

        activeTickets.remove(ticketId);

        return fee;
    }
}
```
## Why This Design is GOOD
```
1. Open Closed Principle

Want:

dynamic pricing
VIP parking
EV spots

Add new strategy/classes.

No major modification.

2. Single Responsibility

Each class has one responsibility.

3. Extensible

Can add:

Reservation
Multiple entry gates
Online payment
Display boards
EV charging
Important Interview Discussion Points
Why Strategy Pattern?

Because parking allocation logic may change.

Examples:

nearest spot
random spot
cheapest spot
VIP priority
Why Abstract Classes?

Common behavior shared.

Why Composition?
ParkingLot HAS-A Floor
Floor HAS-A Spot

Preferred over inheritance.
```
## Common Follow-Up Questions
```
Q1: How to make it thread-safe?

Answer:

Synchronize parking allocation
Use locks
Use ConcurrentHashMap
Q2: How to optimize search?

Current:

O(number_of_spots)

Optimization:

Maintain available spots heap/set
Separate queues per spot type
Q3: How to support EV vehicles?

Add:

EVSpot
EVCar

Minimal changes needed.

Q4: How to scale?

Split:

Entry service
Payment service
Spot allocation service

Use DB + caching.
```
