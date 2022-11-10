package entities.misc;

public class SynchronizedInt {
    private int value;

    public SynchronizedInt() {
        value = 0;
    }

    public SynchronizedInt(int initialValue) {
        value = initialValue;
    }

    public synchronized int get() { return this.value; }
    public synchronized void increment() { value ++; }
}
