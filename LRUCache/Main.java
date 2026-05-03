public class Main {
    static LRUCache cache;

    static void section(String title) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║  " + title);
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    static void testGet(int key) {
        int result = cache.get(key);
        System.out.println("get(" + key + ") --> " + (result == -1 ? "MISS (-1)" : "HIT (" + result + ")"));
        cache.display();
    }

    static void testPut(int key, int val) {
        System.out.println("put(" + key + ", " + val + ")");
        cache.put(key, val);
        cache.display();
    }

    public static void main(String[] args) {
        // ─────────────────────────────────────────
        // CASE 1: Basic insertion up to capacity
        // ─────────────────────────────────────────
        section("CASE 1: Fill cache to capacity (cap=3)");
        cache = new LRUCache(3);
        testPut(1, 10);
        testPut(2, 20);
        testPut(3, 30);
        // Expected: LRU [1:10 ↔ 2:20 ↔ 3:30] MRU

        // ─────────────────────────────────────────
        // CASE 2: Get on existing key (cache hit)
        // promotes to MRU
        // ─────────────────────────────────────────
        section("CASE 2: Cache hit --> promotes to MRU");
        testGet(1); // hit --> 1 moves to MRU
        // Expected: LRU [2:20 ↔ 3:30 ↔ 1:10] MRU

        // ─────────────────────────────────────────
        // CASE 3: Get on missing key (cache miss)
        // ─────────────────────────────────────────
        section("CASE 3: Cache miss --> returns -1");
        testGet(99); // miss --> -1, no change
        // Expected: LRU [2:20 ↔ 3:30 ↔ 1:10] MRU (unchanged)

        // ─────────────────────────────────────────
        // CASE 4: Eviction of LRU on overflow
        // ─────────────────────────────────────────
        section("CASE 4: Eviction --> LRU evicted on overflow");
        testPut(4, 40); // cache full --> evicts LRU (2)
        // Expected: LRU [3:30 ↔ 1:10 ↔ 4:40] MRU
        testGet(2); // should be MISS — was evicted
        // Expected: MISS (-1)

        // ─────────────────────────────────────────
        // CASE 5: Update existing key (cache hit on put)
        // ─────────────────────────────────────────
        section("CASE 5: Update existing key --> val updated, moved to MRU");
        testPut(3, 300); // 3 exists --> update val, move to MRU
        // Expected: LRU [1:10 ↔ 4:40 ↔ 3:300] MRU

        // ─────────────────────────────────────────
        // CASE 6: LRU saved by last-moment access
        // (the scenario we discussed earlier)
        // ─────────────────────────────────────────
        section("CASE 6: LRU saved by last-moment get --> next oldest evicted");
        cache = new LRUCache(3);
        testPut(1, 10);
        testPut(2, 20);
        testPut(3, 30);
        // LRU [1:10 ↔ 2:20 ↔ 3:30] MRU
        testGet(1); // 1 was LRU, now becomes MRU
        // LRU [2:20 ↔ 3:30 ↔ 1:10] MRU
        testPut(4, 40); // cache full --> evicts 2 (new LRU), NOT 1
        // Expected: LRU [3:30 ↔ 1:10 ↔ 4:40] MRU
        testGet(2); // should be MISS — was evicted
        testGet(1); // should be HIT — was saved

        // ─────────────────────────────────────────
        // CASE 7: Capacity of 1 (extreme edge case)
        // ─────────────────────────────────────────
        section("CASE 7: Capacity = 1 --> every new put evicts previous");
        cache = new LRUCache(1);
        testPut(1, 10);
        testPut(2, 20); // evicts 1
        testGet(1); // MISS
        testGet(2); // HIT

        // ─────────────────────────────────────────
        // CASE 8: Repeated access to same key
        // ─────────────────────────────────────────
        section("CASE 8: Repeated get on same key --> always MRU, no eviction");
        cache = new LRUCache(3);
        testPut(1, 10);
        testPut(2, 20);
        testPut(3, 30);
        testGet(1);
        testGet(1);
        testGet(1); // 1 stays at MRU each time
        // Expected: LRU [2:20 ↔ 3:30 ↔ 1:10] MRU (stable)

        // ─────────────────────────────────────────
        // CASE 9: All keys accessed in order -->
        // eviction follows insertion order (like FIFO)
        // ─────────────────────────────────────────
        section("CASE 9: No re-access --> eviction order same as insertion (FIFO-like)");
        cache = new LRUCache(3);
        testPut(1, 10);
        testPut(2, 20);
        testPut(3, 30);
        testPut(4, 40); // evicts 1
        testPut(5, 50); // evicts 2
        testPut(6, 60); // evicts 3
        testGet(1); // MISS
        testGet(2); // MISS
        testGet(3); // MISS
    }
}