# PART 3 — JAVASCRIPT & ANGULAR (Deep)

---

## Q15 🔴 Explain the JavaScript Event Loop — call stack, task queue, microtask queue, and how async code actually executes.

**Answer:**

JavaScript is single-threaded — it can only execute one thing at a time. The event loop is the mechanism that allows it to handle async operations without blocking. Most candidates say "it's async" without explaining the actual execution model.

**The components:**

```
┌─────────────────────────────────┐
│           CALL STACK            │  ← synchronous execution (LIFO)
│  main() → fetchData() → parse() │
└─────────────────────────────────┘
         ↑ runs when stack empty
┌─────────────────────────────────┐
│        MICROTASK QUEUE          │  ← Promise.then, queueMicrotask, MutationObserver
│  (runs to completion before     │    ALWAYS drains completely before macrotasks
│   any macrotask runs)           │
└─────────────────────────────────┘
         ↑ runs when stack empty AND microtasks done
┌─────────────────────────────────┐
│     MACROTASK QUEUE (Task Queue)│  ← setTimeout, setInterval, I/O, MessageChannel
│  [task1, task2, task3]          │    ONE task per event loop iteration
└─────────────────────────────────┘
```

**Execution rules:**
1. Run the call stack to completion
2. Drain the entire microtask queue (including any microtasks added during draining)
3. Run ONE macrotask
4. Drain the entire microtask queue again
5. Repeat

**The classic interview puzzle — predict the output:**

```javascript
console.log('1'); // sync → call stack

setTimeout(() => console.log('2'), 0); // macrotask queue (even with 0ms delay)

Promise.resolve().then(() => console.log('3')); // microtask queue

Promise.resolve()
  .then(() => {
    console.log('4');
    return Promise.resolve(); // adding another microtask during microtask drain
  })
  .then(() => console.log('5')); // this runs before any macrotask

console.log('6'); // sync → call stack

// Output: 1, 6, 3, 4, 5, 2
// Explanation:
// Sync: 1, 6 (call stack, top to bottom)
// Microtasks drain: 3, 4 (then 5 added), 5 (new microtask drained immediately)
// One macrotask: 2 (setTimeout callback)
```

**Why this matters in practice:**

```javascript
// Mistake: assuming setTimeout(fn, 0) runs immediately after current function
function processData() {
    let result = null;
    setTimeout(() => { result = computeHeavy(); }, 0);
    return result; // null! setTimeout callback hasn't run yet
}

// Promise microtask timing — more predictable than setTimeout
async function fetchUser(id) {
    const user = await fetch(`/api/users/${id}`).then(r => r.json());
    // Everything after 'await' runs as a microtask when the Promise resolves
    // The call stack is freed while waiting, but resumes exactly here
    return user;
}

// queueMicrotask — explicit microtask scheduling
function scheduleWork(fn) {
    queueMicrotask(fn); // runs before setTimeout(fn, 0) — in microtask queue
}
```

**requestAnimationFrame — between macrotasks and rendering:**

```javascript
// Browser rendering happens between macrotask executions
// requestAnimationFrame fires just before the browser repaints
// Use for smooth animations — synced with display refresh (usually 60fps)

function animate() {
    updatePosition(); // update DOM
    requestAnimationFrame(animate); // schedule next frame — not a macrotask
}
requestAnimationFrame(animate);

// Microtasks that block for too long delay rendering:
Promise.resolve().then(() => {
    while(true) { } // infinite microtask loop — browser freezes! Never paints.
    // Because microtask queue must drain completely before rendering
});
```

---

## Q16 🔴 Explain JavaScript's prototype chain, `this` binding rules, and `class` vs prototype.

**Answer:**

**The prototype chain — how property lookup actually works:**

Every JavaScript object has an internal `[[Prototype]]` slot pointing to another object (or null). When you access a property, JS looks up the chain until it finds it or reaches null.

```javascript
const animal = {
    breathe() { return 'breathing'; }
};

const dog = Object.create(animal); // dog's [[Prototype]] = animal
dog.bark = function() { return 'woof'; };

const myDog = Object.create(dog); // myDog's [[Prototype]] = dog
myDog.name = 'Rex';

// Property lookup chain:
myDog.name;    // found on myDog itself → 'Rex'
myDog.bark();  // not on myDog → check dog → found → 'woof'
myDog.breathe(); // not on myDog → check dog → not there → check animal → found → 'breathing'
myDog.fly();   // not on myDog → dog → animal → Object.prototype → null → TypeError

// Object.prototype is at the top of every chain:
myDog.toString(); // found on Object.prototype
myDog.hasOwnProperty('name'); // found on Object.prototype
```

**Constructor functions and `.prototype`:**

```javascript
function Person(name) {
    this.name = name; // own property on the instance
}

// Person.prototype is an object shared by ALL Person instances
Person.prototype.greet = function() {
    return `Hello, I'm ${this.name}`;
};

const alice = new Person('Alice');
// 'new' does:
// 1. Creates new empty object {}
// 2. Sets its [[Prototype]] to Person.prototype
// 3. Runs Person() with 'this' = new object
// 4. Returns the object (unless constructor explicitly returns another object)

alice.greet(); // found on Person.prototype via [[Prototype]] chain
alice.hasOwnProperty('name'); // true — own property
alice.hasOwnProperty('greet'); // false — on prototype, not own
```

**`class` syntax — syntactic sugar over prototypes:**

```javascript
class Animal {
    #sound; // private field (truly private, not convention)
    
    constructor(sound) {
        this.#sound = sound; // not accessible outside the class
    }
    
    speak() { return this.#sound; } // on Animal.prototype
    
    static create(sound) { return new Animal(sound); } // on Animal itself, not instances
    
    get soundLength() { return this.#sound.length; } // getter — accessed as property
}

class Dog extends Animal {
    constructor(name) {
        super('woof'); // MUST call super() before using 'this' in derived class
        this.name = name;
    }
    
    // Override
    speak() {
        return `${this.name} says ${super.speak()}`; // super.speak() calls Animal.prototype.speak
    }
}

// Under the hood:
// Dog.prototype.__proto__ === Animal.prototype (prototype chain for instances)
// Dog.__proto__ === Animal (prototype chain for static methods)
```

**The four `this` binding rules — in priority order:**

```javascript
// Rule 1: new binding — 'this' = newly created object
const alice = new Person('Alice'); // this = alice inside Person()

// Rule 2: Explicit binding — call(), apply(), bind()
function greet() { return `Hello, ${this.name}`; }
greet.call({name: 'Alice'});        // this = {name: 'Alice'}
greet.apply({name: 'Bob'}, []);     // this = {name: 'Bob'}
const boundGreet = greet.bind({name: 'Charlie'}); // returns new function, this permanently bound

// Rule 3: Implicit binding — left of the dot
const obj = { name: 'Diana', greet };
obj.greet(); // this = obj because obj is left of the dot

// Rule 4: Default binding — global (or undefined in strict mode)
greet(); // this = window (browser) or global (Node) — or TypeError in strict mode

// Arrow functions — lexical 'this' (inherit from enclosing scope, cannot be bound)
class Timer {
    constructor() { this.ticks = 0; }
    
    start() {
        // Regular function — 'this' lost when called as callback
        setInterval(function() {
            this.ticks++; // 'this' = window/undefined — bug!
        }, 1000);
        
        // Arrow function — 'this' inherited from start() method = Timer instance
        setInterval(() => {
            this.ticks++; // 'this' = Timer instance — correct
        }, 1000);
    }
}
```

**Common `this` pitfall with class methods:**

```javascript
class Button {
    label = 'Click me';
    
    handleClick() { console.log(this.label); }
}

const btn = new Button();
document.addEventListener('click', btn.handleClick);
// When called as event handler, 'this' = the DOM element, not the Button instance
// console.log(this.label) → undefined

// Fix 1: Arrow function in class field (binds at construction time)
class Button {
    label = 'Click me';
    handleClick = () => console.log(this.label); // arrow function — lexical this
}

// Fix 2: bind in constructor
constructor() { this.handleClick = this.handleClick.bind(this); }

// Fix 3: Arrow function wrapper at call site
document.addEventListener('click', () => btn.handleClick());
```

---

## Q17 🔴 Explain `var`, `let`, `const` — scoping, hoisting, temporal dead zone, and closures.

**Answer:**

**Hoisting — declarations are moved to top of their scope before execution:**

```javascript
// var hoisting — DECLARATION hoisted, not initialization
console.log(x); // undefined (not ReferenceError) — declaration hoisted
var x = 5;
console.log(x); // 5

// Equivalent to what JS sees:
var x;           // hoisted to top of function/global scope
console.log(x); // undefined
x = 5;
console.log(x); // 5

// Function declarations are FULLY hoisted (declaration + body)
sayHello(); // works! Function is fully available before this line
function sayHello() { console.log('Hello'); }

// Function expressions are NOT fully hoisted
sayBye(); // TypeError: sayBye is not a function
var sayBye = function() { console.log('Bye'); };
// var sayBye is hoisted (value = undefined), but assignment isn't
```

**`var` scope — function scope (not block scope):**

```javascript
function example() {
    if (true) {
        var x = 10; // var is function-scoped, not block-scoped
    }
    console.log(x); // 10 — var leaks out of the if block!
}

// Classic bug with var in loops:
for (var i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 100); // prints 3, 3, 3 — not 0, 1, 2!
}
// All callbacks share the SAME 'i' variable — by the time they run, loop is done

// Fix with let:
for (let i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 100); // prints 0, 1, 2 — each iteration has own 'i'
}
// let creates a new binding per loop iteration
```

**`let` and `const` — block scoped, temporal dead zone:**

```javascript
// Block scoped — only accessible within {}
{
    let y = 20;
    const z = 30;
}
console.log(y); // ReferenceError: y is not defined
console.log(z); // ReferenceError: z is not defined

// Temporal Dead Zone (TDZ) — let/const ARE hoisted but NOT initialized
console.log(a); // ReferenceError: Cannot access 'a' before initialization
let a = 5;
// 'a' exists in the scope from the start but is in TDZ until the declaration line
// Accessing it before declaration = ReferenceError (better than var's silent undefined)

// const — reference is immutable, not value
const obj = { name: 'Alice' };
obj.name = 'Bob'; // ✅ — mutating the object is allowed
obj = {}; // ❌ — TypeError: Assignment to constant variable — can't rebind the reference

const arr = [1, 2, 3];
arr.push(4); // ✅ — mutating array is fine
arr = [];    // ❌ — can't rebind
```

**Closures — functions that capture their outer scope:**

```javascript
// A closure is formed when a function is defined inside another function
// and references variables from the outer function's scope.
// Those variables are KEPT ALIVE even after the outer function returns.

function makeCounter(initial = 0) {
    let count = initial; // this variable is captured in the closure
    
    return {
        increment() { count++; },
        decrement() { count--; },
        getCount() { return count; }
    };
    // makeCounter has returned, but count is still alive — referenced by the returned object
}

const counter = makeCounter(10);
counter.increment();
counter.increment();
counter.getCount(); // 12

// Each call to makeCounter creates a NEW closure with its own 'count'
const counter2 = makeCounter(0);
counter2.getCount(); // 0 — completely independent from counter

// Practical closure: module pattern (pre-ES6)
const auth = (function() {
    let token = null; // private — can't be accessed from outside
    
    return {
        login(t) { token = t; },
        logout() { token = null; },
        isLoggedIn() { return token !== null; },
        getToken() { return token; }
    };
})(); // IIFE — immediately invoked

auth.login('abc123');
auth.isLoggedIn(); // true
auth.token; // undefined — not accessible, encapsulated in closure

// Closure in async code — the common var-in-loop problem:
const fns = [];
for (var i = 0; i < 3; i++) {
    fns.push(() => i); // all capture the SAME 'i' variable
}
fns[0](); // 3 (not 0!) — i was incremented after all closures were created

// Fix: use let (creates new binding per iteration)
for (let i = 0; i < 3; i++) {
    fns.push(() => i); // each closure captures a different 'i'
}
fns[0](); // 0, fns[1]() → 1, fns[2]() → 2
```

---

## Q18 🔴 Explain `async/await` — what it compiles to, error handling, parallel vs sequential, and common mistakes.

**Answer:**

**`async/await` is syntactic sugar over Promises:**

```javascript
// This:
async function fetchUser(id) {
    const response = await fetch(`/api/users/${id}`);
    const user = await response.json();
    return user;
}

// Is roughly equivalent to:
function fetchUser(id) {
    return fetch(`/api/users/${id}`)
        .then(response => response.json())
        .then(user => user);
}

// async function ALWAYS returns a Promise — even if you return a plain value
async function getValue() { return 42; }
getValue(); // Promise<42> — not 42
getValue().then(v => console.log(v)); // 42
```

**How `await` actually works:**

```javascript
async function example() {
    console.log('A'); // runs synchronously
    
    const result = await somePromise; 
    // 'await' suspends execution of this async function HERE
    // The call stack is freed — other code can run
    // When somePromise resolves, this function is resumed from this exact point
    // Everything after 'await' runs as a microtask
    
    console.log('B', result); // runs after somePromise resolves
}

console.log('start');
example();
console.log('end');
// Output: start, A, end, B (result)
// 'end' runs before 'B' — example() is suspended at await, control returns to caller
```

**Error handling:**

```javascript
// Option 1: try/catch — cleaner for complex flows
async function processOrder(orderId) {
    try {
        const order = await orderService.find(orderId);
        const payment = await paymentService.charge(order.amount);
        await shippingService.schedule(order, payment);
        return { success: true };
    } catch (error) {
        // Catches ANY rejection from any awaited promise
        if (error instanceof PaymentDeclinedException) {
            return { success: false, reason: 'Payment declined' };
        }
        log.error('Order processing failed:', error);
        throw error; // re-throw unexpected errors
    } finally {
        await cleanup(); // always runs
    }
}

// Option 2: .catch() on the promise
async function fetchData() {
    const data = await fetch('/api/data').catch(err => {
        log.warn('Fetch failed, using cache:', err.message);
        return getCachedData(); // return fallback value
    });
    return data;
}

// Option 3: wrapper utility — avoid boilerplate try/catch everywhere
// "Go-style" error handling
async function safely(promise) {
    try {
        const data = await promise;
        return [null, data];
    } catch (error) {
        return [error, null];
    }
}

const [err, user] = await safely(userService.findById(id));
if (err) { handle(err); return; }
console.log(user); // safely typed, no try/catch clutter
```

**Sequential vs Parallel — the most important performance distinction:**

```javascript
// SEQUENTIAL — each await waits for the previous. Total time = sum of all durations.
async function sequential() {
    const user = await fetchUser(1);      // 200ms
    const orders = await fetchOrders(1);  // 150ms
    const settings = await fetchSettings(1); // 100ms
    return { user, orders, settings };
    // Total: ~450ms — wastes time because these are independent!
}

// PARALLEL — all start at the same time. Total time = max of all durations.
async function parallel() {
    const [user, orders, settings] = await Promise.all([
        fetchUser(1),      // all three start simultaneously
        fetchOrders(1),
        fetchSettings(1)
    ]);
    return { user, orders, settings };
    // Total: ~200ms — limited by slowest request only
}

// Promise.all — rejects if ANY promise rejects (fail-fast)
// If fetchOrders throws, the entire Promise.all rejects immediately
// fetchUser and fetchSettings results are lost

// Promise.allSettled — waits for ALL, returns each result (fulfilled or rejected)
const results = await Promise.allSettled([
    fetchUser(1),
    fetchOrders(1),
    fetchSettings(1)
]);
results.forEach(result => {
    if (result.status === 'fulfilled') { use(result.value); }
    else { log.warn('Failed:', result.reason); }
});
// All three always complete — partial results usable

// Promise.race — resolves/rejects with the FIRST settled promise
const result = await Promise.race([
    fetch('/api/primary'),
    fetch('/api/backup'),
    new Promise((_, reject) => setTimeout(() => reject(new Error('Timeout')), 5000))
]);
// Useful for: timeout patterns, trying multiple sources, taking fastest response

// Promise.any — resolves with FIRST fulfilled (ignores rejections)
// Only rejects if ALL promises reject
const fastestResponse = await Promise.any([
    fetch('/api/server1'),
    fetch('/api/server2'),
    fetch('/api/server3')
]);
```

**Common async/await mistakes:**

```javascript
// MISTAKE 1: Forgetting await — function returns Promise, not value
async function getUser(id) { return userRepo.find(id); }
const user = getUser(1); // user = Promise<User>, not User!
user.name; // undefined — need 'await getUser(1)'

// MISTAKE 2: await in forEach — forEach doesn't wait for async callbacks
async function processAll(ids) {
    ids.forEach(async (id) => {
        await processOne(id); // forEach fires all, doesn't wait for them
    });
    console.log('done'); // prints before any processOne completes!
}
// Fix: use for...of (respects await) or Promise.all
async function processAllFixed(ids) {
    for (const id of ids) {
        await processOne(id); // sequential — each waits
    }
    // Or parallel:
    await Promise.all(ids.map(id => processOne(id)));
}

// MISTAKE 3: Unnecessary sequential awaits on independent operations
async function wrong() {
    const a = await getA(); // 500ms
    const b = await getB(); // 500ms — waits for getA unnecessarily
    return [a, b]; // total: 1000ms
}
async function right() {
    const [a, b] = await Promise.all([getA(), getB()]); // total: 500ms
    return [a, b];
}

// MISTAKE 4: Unhandled promise rejections
async function fire() {
    await riskyOperation(); // if this rejects and nobody awaits fire()...
}
fire(); // UnhandledPromiseRejection — crashes in Node.js 15+
// Fix: always handle rejections
fire().catch(err => log.error(err));
// Or: await it in an async context
```

---

## Q19 🔴 Explain Angular's change detection — Zone.js, Default vs OnPush, and manual control.

**Answer:**

**Why Angular needs to know when to update the DOM:**

Angular renders your component templates. When your data changes, Angular needs to re-evaluate bindings and update the DOM. The question is: when does Angular know data changed? In React you call `setState()`. In Angular, Zone.js handles this automatically.

**Zone.js — the change detection trigger:**

Zone.js monkey-patches all asynchronous browser APIs when Angular bootstraps:

```typescript
// Zone.js patches these globally at startup:
// setTimeout, setInterval, Promise.then, XMLHttpRequest.send,
// addEventListener, removeEventListener, requestAnimationFrame, fetch

// When any patched operation completes, Zone.js notifies Angular:
// → ApplicationRef.tick() is called
// → Change detection runs on the entire component tree from root to leaf
```

So this just works without any manual notification:

```typescript
@Component({ template: `<div>{{ count }}</div>` })
export class MyComponent {
    count = 0;
    
    ngOnInit() {
        setTimeout(() => {
            this.count = 5; // Zone.js sees setTimeout callback completed
            // → triggers CD → Angular sees count changed → updates DOM
        }, 1000);
        
        this.http.get('/api/data').subscribe(data => {
            this.data = data; // HttpClient uses Zone.js → CD triggered
        });
    }
}
```

**Default change detection strategy — exhaustive but expensive:**

With the default strategy, Angular checks EVERY component in the tree after every CD trigger. For a tree with 500 components, all 500 are dirty-checked on every button click, every HTTP response, every setTimeout.

```
AppComponent
├── HeaderComponent      ← checked (hasn't changed, but still checked)
├── SidebarComponent     ← checked
└── OrderListComponent   ← checked
    ├── OrderItem × 200  ← ALL 200 checked even if only 1 order changed
```

Angular checks by comparing current binding value with previous value. String/number comparison is cheap, but 200+ components × multiple bindings = visible performance cost.

**OnPush — skip this component unless specific conditions are met:**

```typescript
@Component({
    selector: 'app-order-item',
    template: `
        <div [class.updated]="order.status === 'UPDATED'">
            {{ order.id }} — {{ order.status }}
        </div>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush // ← add this
})
export class OrderItemComponent {
    @Input() order: Order;
}
```

Angular skips this component's check UNLESS:
1. An `@Input()` reference changes (strict `===` comparison — not deep equality)
2. An Observable emits via `async` pipe in the template
3. A DOM event fires inside this component (click, keyup, etc.)
4. `ChangeDetectorRef.markForCheck()` is called manually

**The critical OnPush + mutation trap:**

```typescript
// Parent component
@Component({ template: `<app-order-item [order]="currentOrder">` })
export class ParentComponent {
    currentOrder: Order = { id: 1, status: 'PENDING' };
    
    updateStatus() {
        this.currentOrder.status = 'SHIPPED'; // MUTATION — same object reference!
        // Angular compares: currentOrder === currentOrder → true (same object)
        // OnPush sees no change → skips the OrderItemComponent → DOM NOT updated!
    }
    
    updateStatusCorrectly() {
        this.currentOrder = { ...this.currentOrder, status: 'SHIPPED' }; // NEW reference!
        // Angular: currentOrder !== previous currentOrder → OnPush sees change → updates DOM
    }
}
```

**OnPush with Observable — the recommended pattern:**

```typescript
@Component({
    template: `
        <div *ngFor="let order of orders$ | async; trackBy: trackById">
            {{ order.status }}
        </div>
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderListComponent {
    orders$: Observable<Order[]> = this.orderService.getOrders();
    
    // trackBy: prevents Angular from destroying and recreating ALL DOM nodes when the array changes
    // Without trackBy: new array emitted → 100 DOM nodes destroyed and recreated
    // With trackBy: Angular identifies unchanged items by ID, only updates changed ones
    trackById = (index: number, order: Order) => order.id;
    
    constructor(private orderService: OrderService) {}
}
// async pipe: subscribes automatically, unsubscribes on destroy, calls markForCheck() on emission
```

**Manual control — ChangeDetectorRef:**

```typescript
@Component({ changeDetection: ChangeDetectionStrategy.OnPush })
export class ManualComponent implements OnInit {
    data: SomeData;
    
    constructor(private cdr: ChangeDetectorRef, private ngZone: NgZone) {}
    
    ngOnInit() {
        // Data from a WebSocket library that runs OUTSIDE Angular's Zone
        this.externalLibrary.onData(data => {
            this.data = data;
            
            // Option A: markForCheck() — schedules check for next CD cycle
            this.cdr.markForCheck();
            // marks this component and all ancestors — runs on next CD sweep
            
            // Option B: detectChanges() — immediately runs CD for this subtree
            // this.cdr.detectChanges();
            // More immediate but more expensive — use sparingly
            
            // Option C: run inside Angular zone — triggers normal CD
            this.ngZone.run(() => {
                this.data = data; // assignment inside zone → CD triggered automatically
            });
        });
        
        // detach() / reattach() for completely manual control
        this.cdr.detach(); // completely remove from CD tree — never checked automatically
        setInterval(() => {
            this.updateData();
            this.cdr.detectChanges(); // manually trigger check exactly when needed
        }, 5000); // only check every 5 seconds — maximum control over performance
    }
}
```

---

## Q20 🔴 Explain RxJS in depth — cold vs hot observables, subjects, error handling, memory leaks, and higher-order operators.

**Answer:**

**Cold vs Hot Observables:**

```typescript
// COLD — producer created INSIDE, each subscriber gets its own execution
const cold$ = new Observable(subscriber => {
    console.log('Producer started'); // runs fresh for EACH subscriber
    subscriber.next(Math.random()); // each subscriber gets a DIFFERENT value
    subscriber.complete();
});

cold$.subscribe(v => console.log('Sub1:', v)); // Producer started, Sub1: 0.42
cold$.subscribe(v => console.log('Sub2:', v)); // Producer started, Sub2: 0.87
// Two independent executions — like two separate HTTP requests

// Cold examples: HttpClient.get() (each subscribe triggers a new request!),
//                of(), from(), interval(), timer()

// HOT — producer exists outside, subscribers share the same stream
const clicks$ = fromEvent(document, 'click');
// Mouse clicks happen regardless of subscribers
// Sub1 and Sub2 see the SAME click events — shared source

// Hot examples: fromEvent(), Subject, WebSocket stream, store selectors

// THE MOST COMMON BUG: forgetting HttpClient is cold
const users$ = this.http.get<User[]>('/api/users');
users$.subscribe(u => this.users = u);       // HTTP request #1
users$.subscribe(u => this.count = u.length); // HTTP request #2 — same URL, 2 requests!

// Fix: share() or shareReplay()
const sharedUsers$ = this.http.get<User[]>('/api/users').pipe(
    shareReplay(1) // cache last emission, replay to late subscribers, one HTTP request
);
sharedUsers$.subscribe(u => this.users = u);       // HTTP request fired
sharedUsers$.subscribe(u => this.count = u.length); // gets cached result immediately
```

**The four Subject types:**

```typescript
// Subject — no memory, late subscribers miss past emissions
const subject$ = new Subject<string>();
subject$.next('A'); // emitted before any subscriber — LOST
subject$.subscribe(v => console.log(v));
subject$.next('B'); // subscriber sees 'B'
subject$.next('C'); // subscriber sees 'C'

// BehaviorSubject — remembers CURRENT value, new subscribers get it immediately
const behavior$ = new BehaviorSubject<string>('initial');
behavior$.next('update1');
behavior$.subscribe(v => console.log(v)); // immediately receives 'update1'
behavior$.getValue(); // synchronous access to current value
// Use for: state management, current user, feature flags

// ReplaySubject(n) — replays last n emissions to new subscribers
const replay$ = new ReplaySubject<string>(3);
replay$.next('A'); replay$.next('B'); replay$.next('C'); replay$.next('D');
replay$.subscribe(v => console.log(v)); // receives B, C, D (last 3)
// Use for: recent notifications, command history, event replay

// AsyncSubject — only emits last value, only after complete()
const async$ = new AsyncSubject<string>();
async$.next('A'); async$.next('B'); async$.next('C');
async$.subscribe(v => console.log(v)); // nothing yet
async$.complete(); // NOW emits 'C' only
// Use for: one-shot computations where only final result matters
```

**Higher-order mapping operators — the most important RxJS concept:**

These operators handle Observables that emit Observables (nested async operations):

```typescript
// switchMap — cancel previous inner observable on new outer emission
// Perfect for: search, autocomplete — cancel stale request on new keystroke
this.searchControl.valueChanges.pipe(
    debounceTime(300),
    distinctUntilChanged(),
    switchMap(term => this.searchService.search(term))
    // If user types 'ang' then immediately 'angu':
    // - 'ang' request starts
    // - 'angu' emits → CANCELS 'ang' request → starts 'angu' request
    // Only the latest request's result reaches the subscriber
)

// mergeMap — run all inner observables CONCURRENTLY, merge results
// Perfect for: parallel operations where order doesn't matter
const uploadAll$ = filesToUpload$.pipe(
    mergeMap(file => this.uploadService.upload(file))
    // All files upload simultaneously, results emitted as each completes
);

// concatMap — queue inner observables, run SEQUENTIALLY (never concurrently)
// Perfect for: ordered operations — step 2 must wait for step 1
const submitSteps$ = formSteps$.pipe(
    concatMap(step => this.submitStep(step))
    // Step 1 fully completes → then step 2 starts → then step 3
);

// exhaustMap — ignore new outer emissions while inner observable is active
// Perfect for: login button, form submit — prevent duplicate submissions
this.loginButton.clicks$.pipe(
    exhaustMap(() => this.authService.login(credentials))
    // While login request is in flight: ALL new button clicks are ignored
    // Only when login completes can a new click trigger another request
);

// Visual comparison:
// switchMap:   A-B-C-| inner: each cancels previous   → only C completes
// mergeMap:    A-B-C-| inner: all run concurrently     → A,B,C all complete (order may vary)
// concatMap:   A-B-C-| inner: A completes, then B, then C
// exhaustMap:  A-B-C-| inner: A starts, B+C ignored while A runs
```

**Memory leaks — the #1 Angular production bug:**

```typescript
// LEAK: manual subscription without unsubscription
@Component({})
export class LeakyComponent implements OnInit {
    ngOnInit() {
        interval(1000).subscribe(n => {
            this.counter = n; // interval keeps running FOREVER after component is destroyed
            // Memory used by component never released, DOM events fire on dead component
        });
    }
}

// FIX 1: async pipe — Angular handles subscribe AND unsubscribe
@Component({ template: `{{ counter$ | async }}` })
export class SafeComponent {
    counter$ = interval(1000);
    // Angular automatically unsubscribes when component is destroyed
}

// FIX 2: takeUntilDestroyed — Angular 16+ (cleanest)
@Component({})
export class SafeComponent2 implements OnInit {
    private destroyRef = inject(DestroyRef);
    
    ngOnInit() {
        interval(1000).pipe(
            takeUntilDestroyed(this.destroyRef) // completes automatically on destroy
        ).subscribe(n => this.counter = n);
    }
}

// FIX 3: takeUntil + Subject — classic pattern (pre-Angular 16)
@Component({})
export class SafeComponent3 implements OnInit, OnDestroy {
    private destroy$ = new Subject<void>();
    
    ngOnInit() {
        interval(1000).pipe(takeUntil(this.destroy$)).subscribe(n => this.counter = n);
        this.dataService.stream$.pipe(takeUntil(this.destroy$)).subscribe(d => this.data = d);
        // All subscriptions with takeUntil(this.destroy$) cleaned up together
    }
    
    ngOnDestroy() {
        this.destroy$.next();    // emit to trigger completion in all takeUntil operators
        this.destroy$.complete(); // clean up the Subject itself
    }
}

// FIX 4: Subscription.add() — group multiple subscriptions
ngOnInit() {
    this.sub = new Subscription();
    this.sub.add(interval(1000).subscribe(...));
    this.sub.add(this.service.data$.subscribe(...));
}
ngOnDestroy() { this.sub.unsubscribe(); } // unsubscribes all
```

**Error handling in RxJS streams:**

```typescript
// catchError — recover from error, stream continues
this.http.get('/api/data').pipe(
    catchError(err => {
        if (err.status === 404) return of(defaultValue); // recover with fallback
        if (err.status === 503) return EMPTY;             // complete without emitting
        return throwError(() => err);                     // re-throw for unhandled errors
    })
);

// retry — automatic retry on error
this.http.get('/api/data').pipe(
    retry({ count: 3, delay: (err, count) => timer(Math.pow(2, count) * 1000) })
    // exponential backoff: 2s, 4s, 8s
);

// finalize — always runs on complete or error (like try/finally)
this.http.get('/api/data').pipe(
    tap(() => this.loading = true),
    finalize(() => this.loading = false)
);
```

---

## Q21 🔴 Explain Angular's dependency injection system — hierarchical injectors, providers, and injection tokens.

**Answer:**

**Angular has a hierarchical injector tree that mirrors the component tree:**

```
Root Injector (Application level)
  ↓ parent of
Platform Injector (platform-browser)
  ↓ parent of
Environment Injector (NgModule or standalone route)
  ↓ parent of
Component Injector (each component)
  ↓ parent of
Child Component Injector
```

When Angular needs to inject a dependency, it walks UP the injector tree until it finds a provider. The first provider found wins.

```typescript
// providedIn: 'root' — registered in root injector, SINGLETON across the entire app
@Injectable({ providedIn: 'root' })
export class AuthService { }
// Same instance everywhere in the app — tree-shakable (removed if unused)

// providedIn: 'any' — new instance per lazy-loaded module
@Injectable({ providedIn: 'any' })
export class LazyService { } // different instance in each lazy chunk

// Component-level provider — new instance per component (and its children)
@Component({
    selector: 'app-form',
    providers: [FormStateService] // new FormStateService instance for this component subtree
})
export class FormComponent { }
// FormComponent and all its children share this FormStateService
// BUT: sibling FormComponents get their OWN FormStateService instances

// This is powerful for form state, dialog state, feature state that should reset
```

**InjectionToken — for non-class dependencies:**

```typescript
// You can't inject a string, number, or interface — use InjectionToken
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');
export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG');

// Provide in app.config.ts or @NgModule
export const appConfig: ApplicationConfig = {
    providers: [
        { provide: API_BASE_URL, useValue: 'https://api.example.com' },
        { provide: APP_CONFIG, useFactory: () => loadConfig(), deps: [] }
    ]
};

// Inject with @Inject decorator
@Injectable({ providedIn: 'root' })
export class ApiService {
    constructor(@Inject(API_BASE_URL) private baseUrl: string) {}
    // Or with inject() function (Angular 14+, can be used anywhere in injection context)
}

// Modern approach: inject() function — no constructor needed
@Injectable({ providedIn: 'root' })
export class ApiService {
    private baseUrl = inject(API_BASE_URL);
    private http = inject(HttpClient);
    // Works in services, components, directives, pipes — wherever injection context exists
}
```

**Provider types:**

```typescript
providers: [
    // useClass — create a new instance of this class
    { provide: UserService, useClass: UserService }, // same as just: UserService
    
    // useExisting — alias one token to another (shares the same instance)
    { provide: LegacyUserService, useExisting: UserService },
    
    // useValue — provide a static value
    { provide: API_BASE_URL, useValue: environment.apiUrl },
    
    // useFactory — compute the value dynamically
    {
        provide: HttpClient,
        useFactory: (handler: HttpHandler) => new HttpClient(handler),
        deps: [HttpHandler]
    }
]
```

**Avoiding circular dependencies:**

```typescript
// If ServiceA needs ServiceB and ServiceB needs ServiceA:
// Angular will throw: Circular dependency detected

// Fix 1: introduce a third service with shared state
// Fix 2: use forwardRef() — defers reference resolution
@Injectable({ providedIn: 'root' })
export class ServiceA {
    constructor(@Inject(forwardRef(() => ServiceB)) private serviceB: ServiceB) {}
}
// forwardRef() wraps the reference in a thunk — resolved lazily when injection actually occurs
```

---

## Q22 🟡 What are Angular interceptors? Implement a complete HTTP interceptor chain.

**Answer:**

Interceptors sit between `HttpClient` and the actual HTTP request/response. They form a pipeline — each interceptor calls `next.handle(req)` to pass to the next interceptor (or the actual HTTP layer).

```typescript
// 1. Auth interceptor — attach JWT to every request
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    private authService = inject(AuthService);
    
    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const token = this.authService.getToken();
        
        if (!token) return next.handle(req); // no token, pass through
        
        // HttpRequest is immutable — must clone to modify
        const authReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token}`)
        });
        
        return next.handle(authReq);
    }
}

// 2. Error interceptor — handle 401 (token refresh), 403, 500 globally
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    private authService = inject(AuthService);
    private router = inject(Router);
    
    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                if (error.status === 401) {
                    // Token expired — try to refresh
                    return this.authService.refreshToken().pipe(
                        switchMap(newToken => {
                            const retryReq = req.clone({
                                headers: req.headers.set('Authorization', `Bearer ${newToken}`)
                            });
                            return next.handle(retryReq); // retry with new token
                        }),
                        catchError(() => {
                            this.router.navigate(['/login']); // refresh failed — logout
                            return throwError(() => error);
                        })
                    );
                }
                
                if (error.status === 403) {
                    this.router.navigate(['/forbidden']);
                }
                
                if (error.status >= 500) {
                    this.notificationService.showError('Server error. Please try again.');
                    // Log to Splunk / monitoring
                    this.logService.logError(error);
                }
                
                return throwError(() => error);
            })
        );
    }
}

// 3. Loading interceptor — show/hide global spinner
@Injectable()
export class LoadingInterceptor implements HttpInterceptor {
    private loadingService = inject(LoadingService);
    private activeRequests = 0;
    
    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        // Skip certain requests (polling, background sync)
        if (req.headers.has('X-Skip-Loading')) return next.handle(req);
        
        this.activeRequests++;
        this.loadingService.show();
        
        return next.handle(req).pipe(
            finalize(() => {
                this.activeRequests--;
                if (this.activeRequests === 0) {
                    this.loadingService.hide(); // hide only when ALL requests complete
                }
            })
        );
    }
}

// 4. Register all interceptors — order matters (first registered = outermost)
// providers array in app.config.ts or AppModule
export const appConfig: ApplicationConfig = {
    providers: [
        provideHttpClient(withInterceptors([
            authInterceptor,         // attaches token
            errorInterceptor,        // handles errors
            loadingInterceptor       // shows spinner
        ]))
    ]
};
// Request flow:  authInterceptor → errorInterceptor → loadingInterceptor → HTTP
// Response flow: HTTP → loadingInterceptor → errorInterceptor → authInterceptor
```

---
