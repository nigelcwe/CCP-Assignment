package runnable.staff;

import entities.Cafe;

public class Owner extends Staff {
    public Owner(Cafe cafe) {
        this.cafe = cafe;
        this.title = "Owner";
    }

    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
        System.out.println("\u001B[32m" + title + " has started." + "\u001B[0m");

        while (!lastOrder) {
            cafe.serveCustomer(this);
        }

        // ---------- Last Order ----------

        // Taking last order
        if (cafe.servingLst.size() > 0) cafe.serveCustomer(this);

        while (!closingTime) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println(e.getStackTrace()[0]);
            }
        }

        // ---------- Closing Time ----------

        // To reject customers who were already in the cafe before closing time.
        while (cafe.servingLst.size() > 0) {
            cafe.serveCustomer(this);
        }

        if (cafe.seatingLst.size() > 0) {
            System.out.println(title + ": There are \u001B[31m" + cafe.seatingLst.size() + "\u001B[0m more customers in the cafe.");
            int seatingLstSize = cafe.seatingLst.size();
            while (cafe.seatingLst.size() > 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    System.out.println(e.getStackTrace()[0]);
                }
                if (cafe.seatingLst.size() == seatingLstSize) continue;
                System.out.println(title +  ": There are \u001B[31m" + cafe.seatingLst.size() + "\u001B[0m customers in the cafe.");
                seatingLstSize = cafe.seatingLst.size();
            }
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
        System.out.println(title + ": No customers left, going home now.");
        cafe.printStats();
        System.out.println("\u001B[32m" + title + " has ended safely." + "\u001B[0m");
    }

    public synchronized void setClosingTime() {
        closingTime = true;
        System.out.println(title + ": We're closing now.");
    }

    public void notifyLastOrder() {
        System.out.println(title + ": Any last orders?");
    }
}
