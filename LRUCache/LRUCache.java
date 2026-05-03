import java.util.HashMap;

public class LRUCache {
    private final int capacity;
    private final HashMap<Integer, LRUFrame> cache = new HashMap<>();
    private final LRUFrame head = new LRUFrame(0, 0); // sentinel: not holding real data [just to demarcate head & tail]
    private final LRUFrame tail = new LRUFrame(0, 0); // sentinel

    public LRUCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    private void remove(LRUFrame node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insertAtTail(LRUFrame node) {
        tail.prev.next = node;
        node.prev = tail.prev;
        node.next = tail;
        tail.prev = node;
    }

    public int get(int key) {
        if (!cache.containsKey(key))
            return -1;

        LRUFrame node = cache.get(key);
        remove(node); // removing from LRU frame
        insertAtTail(node); // marking MRU at tail

        return node.val;
    }

    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            remove(cache.get(key)); // if cache hit, remove from LRUFrame to be marked as MRU
        } else if (cache.size() == capacity) {
            cache.remove(head.next.key); // evicting LRU Frame [head frame: sentinel head --> true head]
            remove(head.next);
        }

        LRUFrame node = new LRUFrame(key, value); // updating the value of key (for both cache hit & miss)
        insertAtTail(node);
        cache.put(key, node); // updating cache with updated key
    }

    public void display() {
        System.out.println("Cache Map:");
        for (HashMap.Entry<Integer, LRUFrame> entry : cache.entrySet()) {
            System.out.println("  key=" + entry.getKey() + " -> val=" + entry.getValue().val);
        }
        System.out.print("\nFrames | LRU [");
        LRUFrame curr = head.next;
        while (curr != tail) {
            System.out.print(curr.key + ":" + curr.val);
            if (curr.next != tail)
                System.out.print(" <-> ");
            curr = curr.next;
        }
        System.out.println("] MRU");
        System.out.println("Size: " + cache.size() + "/" + capacity);
        System.out.println("---------------------------");
    }
}
