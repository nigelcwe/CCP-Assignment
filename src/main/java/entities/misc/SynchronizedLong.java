package entities.misc;

public class SynchronizedLong {
    private long value;

    public SynchronizedLong() {
        value = 0;
    }

    public SynchronizedLong(long initialValue) {
        value = initialValue;
    }

    public synchronized long get() { return this.value; }
    public synchronized void set(long value) { this.value = value; }
    public synchronized void add(long value) { this.value = this.value + value; }
}
