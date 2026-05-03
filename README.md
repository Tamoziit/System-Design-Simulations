# System Design Simulations

Low-level simulations of well-known system design problems in Java — built as interview prep and academic reference material.

Each chapter covers one problem: theory, a real-world scenario, and implementation breakdown.

---

## Table of Contents

| # | Problem | Concepts |
|---|---------|----------|
| 1 | [LRU Cache](#chapter-1-lru-cache) | Doubly Linked List, HashMap, Eviction Policy |

---

## Chapter 1: LRU Cache

> **Files:** `LRUCache/LRUCache.java` · `LRUCache/LRUFrame.java` · `LRUCache/Main.java`

---

### 1.1 Theoretical Overview

**LRU (Least Recently Used)** is a cache eviction policy that discards the item that was accessed *least recently* when the cache is full and a new item needs to be inserted.

#### Core Invariant
> The cache always holds the `N` most recently used items. Any access (read or write) on a key marks it as the most recently used.

#### Data Structure Choice

A naive implementation (e.g., sorted list by timestamp) gives `O(n)` for every operation. The optimal solution uses **two data structures in tandem**:

| Structure | Role | Time Complexity |
|-----------|------|----------------|
| `HashMap<key, Node>` | O(1) key lookup | O(1) get/put |
| Doubly Linked List | Ordered by recency (LRU ← → MRU) | O(1) insert/remove |

Together they achieve **O(1)** for both `get` and `put`.

#### Sentinel Nodes (Dummy Head & Tail)
Instead of null-checking, the list is bounded by two permanent dummy nodes:
```
[sentinel_head] ↔ [LRU node] ↔ ... ↔ [MRU node] ↔ [sentinel_tail]
```
- `head.next` → always the true LRU (eviction target)
- `tail.prev` → always the true MRU (insertion point)

This eliminates all edge-case handling for empty list, single node, etc.

#### Operations Summary

| Operation | Steps |
|-----------|-------|
| `get(key)` — **hit** | Look up in map → remove from list → insert at tail (MRU) → return value |
| `get(key)` — **miss** | Key not in map → return `-1` |
| `put(key, val)` — **hit** | Remove old node from list → insert new node at tail → update map |
| `put(key, val)` — **miss, not full** | Insert new node at tail → add to map |
| `put(key, val)` — **miss, full** | Evict `head.next` (LRU) from list + map → insert new node at tail → add to map |

---

### 1.2 Real-World Scenario: CDN Edge Cache

**Scenario:** A CDN edge server (e.g., Cloudflare, Akamai) sits between users and an origin server. It caches rendered HTML pages or API responses locally. The edge node has limited RAM — say it can hold only **3 cached responses** at a time.

**Problem:** Thousands of users hit different URLs. We want to serve repeat requests from cache (fast, cheap) without hitting the origin for every request. When the cache is full and a new URL is requested, which cached response do we drop?

**Answer:** Drop the one that was accessed *least recently* — it's least likely to be requested again in the near future (temporal locality).

```
User A → GET /home        → MISS  → fetch from origin → cache [/home]
User B → GET /pricing     → MISS  → fetch from origin → cache [/home, /pricing]
User C → GET /docs        → MISS  → fetch from origin → cache [/home, /pricing, /docs]
User A → GET /home        → HIT   ✓ served from edge  → promotes /home to MRU
                                    cache order: LRU [/pricing ↔ /docs ↔ /home] MRU
User D → GET /blog        → MISS  → cache full → EVICT /pricing (LRU)
                                    cache: [/docs ↔ /home ↔ /blog]
User B → GET /pricing     → MISS  → /pricing was evicted, must re-fetch from origin
```

**Why it matters:** This exact eviction logic runs inside Redis, Memcached, CPU L1/L2 caches, browser HTTP caches, and database buffer pools. Getting the eviction policy wrong leads to cache thrashing — evicting hot data and constantly re-fetching from the expensive origin.

---

### 1.3 Implementation: Key Areas

#### `LRUFrame` — The List Node
```java
public class LRUFrame {
    int key, val;
    LRUFrame prev, next;
}
```
Each node stores the key (needed for O(1) map removal on eviction) and value, plus doubly-linked pointers.

#### `LRUCache` — Core Structure
```java
private final HashMap<Integer, LRUFrame> cache;   // O(1) key lookup
private final LRUFrame head = new LRUFrame(0, 0); // sentinel LRU boundary
private final LRUFrame tail = new LRUFrame(0, 0); // sentinel MRU boundary
```

#### `remove(node)` — O(1) Detach
```java
node.prev.next = node.next;
node.next.prev = node.prev;
```
Works for any node position including edges, because sentinels absorb the boundary cases.

#### `insertAtTail(node)` — O(1) Mark as MRU
```java
tail.prev.next = node;
node.prev = tail.prev;
node.next = tail;
tail.prev = node;
```
Always inserts immediately before the sentinel tail → marks the node as MRU.

#### `get(key)` — Hit promotes to MRU
```java
LRUFrame node = cache.get(key);
remove(node);        // detach from current position
insertAtTail(node);  // re-insert as MRU
return node.val;
```

#### `put(key, value)` — Eviction logic
```java
if (cache.containsKey(key)) {
    remove(cache.get(key));           // cache hit: remove stale position
} else if (cache.size() == capacity) {
    cache.remove(head.next.key);      // evict LRU: map removal
    remove(head.next);                // evict LRU: list removal
}
LRUFrame node = new LRUFrame(key, value);
insertAtTail(node);
cache.put(key, node);
```

> **Critical detail:** `head.next.key` is why `LRUFrame` stores the key — without it, we'd need a reverse map to identify which HashMap entry to remove during eviction, making it O(n).

---

### 1.4 Test Cases & Verified Output

9 cases are tested in `Main.java` covering all behavioral branches:

| Case | Scenario | Key Assertion |
|------|----------|---------------|
| 1 | Fill to capacity | Order matches insertion: `LRU [1↔2↔3] MRU` |
| 2 | Cache hit on `get` | Accessed key moves to MRU end |
| 3 | Cache miss on `get` | Returns `-1`, list unchanged |
| 4 | Eviction on overflow | LRU node evicted; subsequent get on it is MISS |
| 5 | Update existing key on `put` | Value updated, key promoted to MRU |
| 6 | LRU saved by last-moment access | Pre-eviction `get` rescues the key; next oldest is evicted instead |
| 7 | Capacity = 1 | Every new `put` evicts the only existing entry |
| 8 | Repeated `get` on same key | Key stays at MRU, list order remains stable |
| 9 | No re-access pattern | Eviction follows insertion order (degrades to FIFO) |

**Sample output — Case 6 (the interesting one):**
```
CASE 6: LRU saved by last-moment get --> next oldest evicted

put(1, 10) → LRU [1:10] MRU
put(2, 20) → LRU [1:10 <-> 2:20] MRU
put(3, 30) → LRU [1:10 <-> 2:20 <-> 3:30] MRU
get(1)     → HIT  → LRU [2:20 <-> 3:30 <-> 1:10] MRU   ← 1 rescued
put(4, 40) → EVICT 2 → LRU [3:30 <-> 1:10 <-> 4:40] MRU
get(2)     → MISS (-1)   ← 2 was evicted
get(1)     → HIT  (10)   ← 1 was saved by the earlier get
```

---
