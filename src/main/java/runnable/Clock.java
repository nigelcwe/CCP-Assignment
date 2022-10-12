package runnable;

import entities.Cafe;
import runnable.customer.CustomerGenerator;
import runnable.staff.Owner;
import runnable.staff.Waiter;

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
            System.out.println("\u001B[32m" + "Clock has started." + "\u001B[0m");
            Thread.sleep(11000);
            notifyLastOrder();
            Thread.sleep(1000);
            notifyClosingTime();
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
        System.out.println("\u001B[32m" + "Clock has ended safely." + "\u001B[0m");
    }

    public void notifyLastOrder() {
        System.out.println("Clock:\u001B[31m 10 minutes till closing.\u001B[0m");
        cafe.setLastOrder();
        custGen.setLastOrder();
        owner.notifyLastOrder();
        owner.setLastOrder();
        waiter.setLastOrder();
    }

    public void notifyClosingTime() {
        System.out.println("Clock:\u001B[31m It's closing time.\u001B[0m");
        cafe.setClosingTime();
        custGen.setClosingTime();
        owner.setClosingTime();
        waiter.setClosingTime();
    }
}
