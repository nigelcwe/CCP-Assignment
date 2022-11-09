package runnable.customer;

import entities.Cafe;

import java.time.LocalTime;
import java.util.Date;
import java.util.Random;

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
        boolean hasEntered = cafe.add(this);
        if (!hasEntered) return;
        boolean tryOrder = cafe.awaitOrder(this);
        if (!tryOrder) return;
        cafe.enjoyDrink(this);
        cafe.logStats(this);
    }

    public boolean orderDrink() { //true = juice, false = cappuccino
        Boolean randDrink = new Random().nextBoolean();
//        Boolean randDrink = false;
        if (randDrink) System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + this.name + " orders a juice.");
        else System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + this.name + " orders a cappuccino.");
        return randDrink;
    }
}
