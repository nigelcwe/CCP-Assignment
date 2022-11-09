package runnable.staff;

import entities.Cafe;

import java.time.LocalTime;

public class Waiter extends Staff {
    public Waiter(Cafe cafe) {
        this.cafe = cafe;
        this.title = "Waiter";
    }

    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + " has started." + "\u001B[0m");
        while (!lastOrder) {
            cafe.serveCustomer(this);
        }
        if (!closingTime) System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + ": Waiting for closing time.");
        synchronized (this) {
            while (!closingTime) {
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + ": Going home now.");
        System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + " has ended safely." + "\u001B[0m");
    }
}
