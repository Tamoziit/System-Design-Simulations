public class LRUFrame {
    int key;
    int val;
    LRUFrame prev, next;

    public LRUFrame(int k, int v) {
        this.key = k;
        this.val = v;
    }

    // In LRUFrame -- displays just this single node
    public void display() {
        System.out.println("Frame [ key=" + key + " | val=" + val + " ]");
    }
}