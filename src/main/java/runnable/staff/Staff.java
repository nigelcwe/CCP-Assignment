package runnable.staff;

import entities.Cafe;

public class Staff implements Runnable {
    protected Cafe cafe;
    public String title = "";
    protected boolean lastOrder = false;
    protected boolean closingTime = false;

    @Override
    public void run() {

    }

    public synchronized void setLastOrder() {
        this.lastOrder = true;
    }

    public synchronized void setClosingTime() {
        this.closingTime = true;
    }
}
