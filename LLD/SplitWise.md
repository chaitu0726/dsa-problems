# Splitwise LLD (Interview Ready)

# Requirements

Design a system like Splitwise where users can:
- Create groups
- Add expenses
- Split expenses among users
- Track balances
- Settle payments

Main focus:
```text
Expense Management Module
```

because it contains the core business logic.

---

# Core Functional Requirements

## User Module
- Register/Login
- Create Group
- Add/Remove Members
- Add Expense
- View Balances
- Settle Payments

---

## Expense Module
User should be able to:
- Add expense in group or individually
- Split expense:
  - Equally
  - Exact amount
  - Percentage based

---

# High Level Flow

```text
User selects Group/Friend
        ↓
Add Description
        ↓
Add Total Amount
        ↓
Choose Currency
        ↓
Choose Split Type
        ↓
Add Participants
        ↓
Create Expense
        ↓
Calculate Splits
        ↓
Update Balance Sheet
```

---

# Why Expense Management is Core?

Because main complexity exists in:
- Split calculation
- Balance tracking
- Extensibility
- Validation
- Settlement handling

User/Group modules are mostly CRUD operations.

---

# Design Patterns Used

| Pattern | Usage |
|---|---|
| Strategy Pattern | Different split algorithms |
| Factory Pattern | Create splitting strategies |
| Singleton (Optional) | Central balance manager |
| Builder Pattern (Optional) | Expense creation |

---

# Components

# 1. User

```java
class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
}
```

Responsibilities:
- Create expense
- View balances
- Settle balances

---

# 2. Group

```java
class Group {
    private String groupId;
    private String groupName;
    private List<User> members;
    private List<Expense> expenses;
}
```

Responsibilities:
- Maintain members
- Maintain group expenses

---

# 3. Expense

Represents one transaction.

```java
class Expense {
    private String expenseId;
    private String description;
    private BigDecimal amount;
    private Currency currency;
    private User paidBy;
    private List<Split> splits;
    private ExpenseType expenseType;
    private ExpenseMode expenseMode;
    private String groupId;
    private LocalDateTime createdAt;
}
```

---

# 4. Split

Represents how much one user owes.

```java
class Split {
    private User user;
    private BigDecimal amount;
}
```

Why separate Split class?
- Better extensibility
- Cleaner design
- Easier future customization

---

# Enums

```java
enum ExpenseType {
    EQUAL,
    EXACT,
    PERCENTAGE
}

enum ExpenseMode {
    INDIVIDUAL,
    GROUP
}

enum Currency {
    INR,
    USD,
    EUR
}
```

---

# Core Design

# Strategy Pattern

Different split algorithms should have different implementations.

---

# Splitting Strategy Interface

```java
interface SplittingStrategy {
    List<Split> calculateSplit(Expense expense);

    void validate(Expense expense);
}
```

---

# Equal Split Strategy

```java
class EqualSplitStrategy implements SplittingStrategy {

    @Override
    public List<Split> calculateSplit(Expense expense) {
        // Equal split logic
    }

    @Override
    public void validate(Expense expense) {
        // Validation
    }
}
```

---

# Exact Split Strategy

```java
class ExactSplitStrategy implements SplittingStrategy {

    @Override
    public List<Split> calculateSplit(Expense expense) {
        // Exact split logic
    }

    @Override
    public void validate(Expense expense) {
        // Sum must equal total amount
    }
}
```

---

# Percentage Split Strategy

```java
class PercentageSplitStrategy implements SplittingStrategy {

    @Override
    public List<Split> calculateSplit(Expense expense) {
        // Percentage split logic
    }

    @Override
    public void validate(Expense expense) {
        // Percentages should sum to 100
    }
}
```

---

# Why Strategy Pattern?

Because:
```text
Split calculation logic changes based on split type.
```

Benefits:
- Open Closed Principle
- Easy extensibility
- Cleaner business logic
- Avoids huge if-else blocks

---

# Factory Pattern

Factory creates correct strategy.

```java
class SplitStrategyFactory {

    static SplittingStrategy getStrategy(ExpenseType type) {

        switch(type) {
            case EQUAL:
                return new EqualSplitStrategy();

            case EXACT:
                return new ExactSplitStrategy();

            case PERCENTAGE:
                return new PercentageSplitStrategy();

            default:
                throw new IllegalArgumentException();
        }
    }
}
```

---

# Expense Service

Main orchestration layer.

```java
class ExpenseService {

    private BalanceService balanceService;

    public Expense createExpense(Expense expense) {

        SplittingStrategy strategy =
            SplitStrategyFactory.getStrategy(
                expense.getExpenseType()
            );

        strategy.validate(expense);

        List<Split> splits =
            strategy.calculateSplit(expense);

        expense.setSplits(splits);

        balanceService.updateBalances(expense);

        return expense;
    }
}
```

---

# Balance Management (Most Important)

This is the heart of Splitwise.

---

# Balance Sheet

```java
class BalanceSheet {

    private Map<User,
            Map<User, BigDecimal>> balances;
}
```

Meaning:

```text
balances[A][B] = 500
```

means:
```text
B owes A ₹500
```

---

# Example

```text
A paid ₹1000

B owes ₹300
C owes ₹700
```

Stored as:

```text
balances[A][B] = 300
balances[A][C] = 700
```

---

# Balance Service

```java
class BalanceService {

    public void updateBalances(Expense expense) {

        User paidBy = expense.getPaidBy();

        for(Split split : expense.getSplits()) {

            User borrower = split.getUser();

            if(borrower.equals(paidBy)) {
                continue;
            }

            // borrower owes paidBy
            // update balances
        }
    }
}
```

---

# Settlement

```java
class Settlement {
    private User payer;
    private User receiver;
    private BigDecimal amount;
    private LocalDateTime settledAt;
}
```

Responsibilities:
- Full settlement
- Partial settlement
- Update balance sheet

---

# Validations

# Equal Split
```text
Amount should split properly
```

# Exact Split
```text
Sum(all splits) == total amount
```

# Percentage Split
```text
Sum(all percentages) == 100
```

# General
```text
No negative amount
Participants cannot be empty
Payer must be participant
```

---

# Why BigDecimal Instead of double?

Financial systems should avoid floating point precision issues.

Bad:
```java
double amount;
```

Good:
```java
BigDecimal amount;
```

Example:
```text
0.1 + 0.2 != 0.3
```

---

# Time Complexity

# Expense Creation
```text
O(n)
```

where:
```text
n = number of participants
```

because we iterate over all splits.

---

# Extensibility

Future enhancements:
- Recurring expenses
- Multi currency support
- Expense simplification
- Payment gateway integration
- Notifications
- Expense categories

---

# Debt Simplification (Advanced)

Example:

```text
A owes B ₹100
B owes C ₹100
```

Optimized:

```text
A owes C ₹100
```

Can be solved using:
```text
Graph + Greedy settlement
```

---

# Final Architecture

```text
User
Group
Expense
Split
Settlement
BalanceSheet
```

Services:

```text
ExpenseService
BalanceService
SettlementService
GroupService
```

Strategies:

```text
SplittingStrategy
    -> EqualSplitStrategy
    -> ExactSplitStrategy
    -> PercentageSplitStrategy
```

---

# Final Expense Creation Flow

```text
Create Expense
      ↓
Validate Expense
      ↓
Choose Split Strategy
      ↓
Calculate Splits
      ↓
Update Balance Sheet
      ↓
Persist Expense
      ↓
Notify Users
```

---

# Key Interview Talking Points

## Why Strategy Pattern?
```text
Different split algorithms require different business logic.
Strategy Pattern keeps system extensible and clean.
```

---

## Why Separate Split Class?
```text
Improves extensibility and supports future custom split types.
```

---

## Why Balance Sheet?
```text
Fetching balances becomes O(1) instead of recalculating every time.
```

---

## Why BigDecimal?
```text
Avoid floating point precision issues in financial systems.
```

---

# Final Summary

Main focus of Splitwise LLD is:
```text
Expense splitting + balance tracking
```

Most important design aspects:
- Strategy Pattern
- Clean balance management
- Validation layer
- Extensible split logic
- Proper financial handling
