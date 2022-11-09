package runnable.staff;

import entities.Cafe;

import java.time.LocalTime;

public class Owner extends Staff {
    public Owner(Cafe cafe) {
        this.cafe = cafe;
        this.title = "Owner";
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

        synchronized (this) {
            while (!lastOrder) {
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!closingTime) {
            notifyLastOrder();
        }
        // ---------- Last Order ----------

        // Taking last order
        if (cafe.servingLst.size() > 0) cafe.serveCustomer(this);

        synchronized (this) {
            while (!closingTime) {
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        notifyClosingTime();

        // ---------- Closing Time ----------

        // To reject customers who were already in the cafe before closing time.
        while (cafe.servingLst.size() > 0) {
            cafe.serveCustomer(this);
        }

        if (cafe.seatingLst.size() > 0) {
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + ": There are \u001B[31m" + cafe.seatingLst.size() + "\u001B[0m more customers in the cafe.");
            int seatingLstSize = cafe.seatingLst.size();
            synchronized (this) {
                while (cafe.seatingLst.size() > 0) {
                    try {
                        this.wait(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (cafe.seatingLst.size() == seatingLstSize) continue;
                    System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title +  ": There are \u001B[31m" + cafe.seatingLst.size() + "\u001B[0m customers in the cafe.");
                    seatingLstSize = cafe.seatingLst.size();
                }
            }
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + ": No customers left, going home now.");
        cafe.printStats();
        System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + " has ended safely." + "\u001B[0m");
    }

    public  void notifyClosingTime() {
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + ": We're closing now.");
    }

    public void notifyLastOrder() {
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + title + ": Any last orders?");
    }
}
