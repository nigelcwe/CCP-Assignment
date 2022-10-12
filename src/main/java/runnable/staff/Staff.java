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

    public void setLastOrder() {
        this.lastOrder = true;
    }
}
