package runnable.staff;

import entities.Cafe;

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
        System.out.println("\u001B[32m" + title + " has started." + "\u001B[0m");
        while (!lastOrder) {
            cafe.serveCustomer(this);
        }
        if (!closingTime) System.out.println(title + ": Waiting for closing time.");
        while (!closingTime) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(title + ": Going home now.");
        System.out.println("\u001B[32m" + title + " has ended safely." + "\u001B[0m");
    }

    public synchronized void setClosingTime() {
        closingTime = true;
    }
}
