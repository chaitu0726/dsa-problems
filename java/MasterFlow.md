# DSA Problem Solving Flowchart

A practical interview-oriented guide to decide:

* which data structure to use
* which algorithm pattern fits
* how to recognize problem types quickly

---

# MASTER FLOW

```text
START
│
├── Is input mostly ARRAY / STRING?
│      │
│      ├── Need FAST LOOKUP / COUNT / FREQUENCY?
│      │        └── HashMap / HashSet
│      │
│      ├── Need PAIR / TRIPLET / SORTED relation?
│      │        └── Two Pointers
│      │
│      ├── Need SUBARRAY / SUBSTRING?
│      │        │
│      │        ├── Contiguous window?
│      │        │       └── Sliding Window
│      │        │
│      │        └── Prefix relation?
│      │                └── Prefix Sum
│      │
│      ├── Array SORTED?
│      │        └── Binary Search
│      │
│      ├── Need NEXT GREATER / MONOTONIC relation?
│      │        └── Monotonic Stack
│      │
│      ├── Need TOP K / smallest/largest?
│      │        └── Heap / Priority Queue
│      │
│      ├── Need ALL combinations / possibilities?
│      │        └── Backtracking
│      │
│      └── Need OPTIMAL answer over choices?
│               └── Dynamic Programming
│
├── Is it LINKED LIST?
│      │
│      ├── Reverse / reorder?
│      │       └── Pointer manipulation
│      │
│      ├── Detect cycle?
│      │       └── Fast & Slow Pointer
│      │
│      └── Merge sorted lists?
│              └── Two pointers / Heap
│
├── Is it TREE?
│      │
│      ├── Level-wise?
│      │       └── BFS / Queue
│      │
│      ├── Path / recursive structure?
│      │       └── DFS / Recursion
│      │
│      ├── Ordered BST?
│      │       └── BST properties / Inorder
│      │
│      └── Need shortest path?
│              └── BFS
│
├── Is it GRAPH?
│      │
│      ├── Connected components?
│      │       └── DFS / BFS / Union Find
│      │
│      ├── Shortest path?
│      │       │
│      │       ├── Unweighted?
│      │       │       └── BFS
│      │       │
│      │       └── Weighted?
│      │               └── Dijkstra
│      │
│      ├── Detect cycle?
│      │       └── DFS / Union Find
│      │
│      ├── Dependency ordering?
│      │       └── Topological Sort
│      │
│      └── Minimum connection cost?
│              └── MST (Prim/Kruskal)
│
└── Optimization problem?
       │
       ├── Greedy works locally?
       │       └── Greedy
       │
       ├── Overlapping subproblems?
       │       └── Dynamic Programming
       │
       └── Need exhaustive exploration?
               └── Backtracking
```

---

# ARRAY & STRING PATTERN GUIDE

## 1. HashMap / HashSet

### Signals

* frequency/count
* duplicates
* lookup in O(1)
* pair existence
* visited tracking

### Keywords

* count
* duplicate
* frequency
* lookup
* repeated
* unique

### Common Problems

* Two Sum
* Contains Duplicate
* Group Anagrams
* Longest Consecutive Sequence

---

# 2. Two Pointers

## Signals

* sorted array
* pair/triplet target
* palindrome
* shrink from both ends

## Keywords

* pair sum
* sorted
* palindrome
* closest
* triplet

## Common Problems

* Two Sum II
* 3Sum
* Container With Most Water
* Valid Palindrome

---

# 3. Sliding Window

## Signals

* contiguous subarray/substring
* longest/shortest window
* at most K
* fixed size window

## Keywords

* substring
* subarray
* consecutive
* longest
* smallest
* window

## Common Problems

* Longest Substring Without Repeating Characters
* Minimum Window Substring
* Permutation in String

---

# 4. Prefix Sum

## Signals

* cumulative sums
* subarray sum = K
* range queries

## Keywords

* sum between indices
* range sum
* cumulative
* prefix

## Common Problems

* Subarray Sum Equals K
* Range Sum Query
* Continuous Subarray Sum

---

# 5. Binary Search

## Signals

* sorted array
* search space reduction
* minimize/maximize answer

## Keywords

* sorted
* minimum possible
* maximum possible
* search answer

## Common Problems

* Binary Search
* Koko Eating Bananas
* Search Rotated Array
* Capacity to Ship Packages

---

# 6. Monotonic Stack

## Signals

* next greater/smaller
* nearest greater
* histogram
* temperatures type

## Keywords

* next greater
* previous smaller
* nearest
* monotonic

## Common Problems

* Daily Temperatures
* Largest Rectangle in Histogram
* Next Greater Element

---

# 7. Heap / Priority Queue

## Signals

* top K
* kth largest/smallest
* stream processing
* repeated min/max retrieval

## Keywords

* top K
* kth largest
* priority
* closest
* median

## Common Problems

* Kth Largest Element
* Top K Frequent
* Find Median from Data Stream

---

# 8. Backtracking

## Signals

* all combinations
* all permutations
* decision tree
* choose/not choose

## Keywords

* generate all
* combinations
* permutations
* subsets
* possible ways

## Common Problems

* Subsets
* Combination Sum
* N Queens
* Generate Parentheses

---

# 9. Dynamic Programming

## Signals

* optimal answer
* repeated computations
* choices at each step
* memoization possible

## Keywords

* maximum/minimum
* number of ways
* best possible
* optimal

## Common Problems

* House Robber
* Coin Change
* LIS
* Knapsack

---

# LINKED LIST GUIDE

```text
Need reverse?
   → Reverse Linked List pattern

Need middle node?
   → Fast & Slow pointer

Need cycle detection?
   → Floyd Cycle Detection

Need merge sorted lists?
   → Two pointers

Need constant insert/delete?
   → Doubly Linked List
```

---

# TREE GUIDE

```text
Need level order?
   → BFS + Queue

Need path traversal?
   → DFS

Need recursion naturally?
   → Tree DFS recursion

BST ordered?
   → Inorder traversal

Need ancestor relation?
   → LCA
```

---

# GRAPH GUIDE

```text
Need traversal?
   → DFS / BFS

Need shortest path?
   → BFS (unweighted)
   → Dijkstra (weighted)

Need dependency ordering?
   → Topological Sort

Need connected groups?
   → Union Find

Need cycle detection?
   → DFS / Union Find
```

---

# GREEDY VS DP

## Use Greedy when:

* local best gives global best
* interval scheduling
* sorting + choosing works

Examples:

* Jump Game
* Gas Station
* Merge Intervals

---

## Use DP when:

* greedy fails
* multiple future possibilities matter
* overlapping subproblems exist

Examples:

* Coin Change
* House Robber
* LIS

---

# INTERVIEW SHORTCUTS

## If you hear:

### “Top K”

→ Heap

### “Substring/Subarray”

→ Sliding Window / Prefix Sum

### “All combinations”

→ Backtracking

### “Sorted array”

→ Binary Search / Two Pointers

### “Next Greater”

→ Monotonic Stack

### “Optimal/minimum/maximum”

→ DP or Greedy

### “Shortest path”

→ BFS or Dijkstra

### “Dependencies”

→ Topological Sort

### “Repeated fast lookup”

→ HashMap

---

# MOST IMPORTANT INTERVIEW PATTERNS

Priority order:

1. HashMap
2. Sliding Window
3. Two Pointers
4. Binary Search
5. Stack
6. Heap
7. DFS/BFS
8. Backtracking
9. DP
10. Graph Algorithms

---

# GOLDEN INTERVIEW RULE

Before coding ask:

```text
1. Is input sorted?
2. Need fast lookup?
3. Need contiguous segment?
4. Need all combinations?
5. Need optimal answer?
6. Is this graph/tree?
7. Can previous work be reused?
```

Those 7 questions usually reveal the correct pattern quickly.
