package runnable;

import entities.Cafe;
import runnable.customer.CustomerGenerator;
import runnable.staff.Owner;
import runnable.staff.Waiter;

import java.time.LocalTime;

public class Clock implements Runnable{
    private CustomerGenerator custGen;
    private Owner owner;
    private Waiter waiter;
    private Cafe cafe;

    public Clock(CustomerGenerator custGen, Owner owner, Waiter waiter, Cafe cafe) {
        this.custGen = custGen;
        this.owner = owner;
        this.waiter = waiter;
        this.cafe = cafe;
    }

    @Override
    public void run() {
        try {
            System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Clock has started." + "\u001B[0m");
            Thread.sleep(11000);
            notifyLastOrder();
            Thread.sleep(1000);
            notifyClosingTime();
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
        System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Clock has ended safely." + "\u001B[0m");
    }

    public void notifyLastOrder() {
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Clock:\u001B[31m 10 minutes till closing.\u001B[0m");
        owner.setLastOrder();
        waiter.setLastOrder();
        cafe.setLastOrder();
        synchronized (cafe.servingLst) { cafe.servingLst.notifyAll(); }
    }

    public void notifyClosingTime() {
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Clock:\u001B[31m It's closing time.\u001B[0m");
        owner.setClosingTime();
        waiter.setClosingTime();
        cafe.setClosingTime();
        custGen.setClosingTime();
        synchronized (cafe.servingLst) { cafe.servingLst.notifyAll(); }
    }
}
