package runnable.customer;

import entities.Cafe;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Customer implements Runnable {
    public String name;
    public Date inTime = null;
    public Cafe cafe;
    public boolean hasDrink = false;
    public boolean hasOrdered = false;
    public long waitingDuration = 0;

    public Customer(Cafe cafe) {
        this.cafe = cafe;
    }

    @Override
    public void run() {
        goForDrink();
    }

    private synchronized void goForDrink() {
        cafe.add(this);
        if (inTime == null) return;
        boolean tryOrder = cafe.awaitOrder(this);
        if (!tryOrder) return;
        cafe.enjoyDrink(this);
        cafe.logStats(this);
    }

    public boolean orderDrink() { //true = juice, false = cappuccino
        Random randDrink = new Random();
        return randDrink.nextBoolean();
//        return false;
    }
}
