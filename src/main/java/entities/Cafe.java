package entities;

import entities.drinks.Cappuccino;
import entities.drinks.Drink;
import entities.drinks.Juice;
import runnable.customer.Customer;
import runnable.staff.Staff;
import runnable.staff.Waiter;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Cafe {
    public int numChair = 10;
    public LinkedList<Customer> servingLst;
    public LinkedList<Customer> seatingLst;
    private BlockingQueue<Staff> glassQueue = new ArrayBlockingQueue<Staff>(1);
    private BlockingQueue<Staff> cupQueue = new ArrayBlockingQueue<Staff>(1);
    private BlockingQueue<Staff> juiceQueue = new ArrayBlockingQueue<Staff>(1);
    private BlockingQueue<Staff> milkQueue = new ArrayBlockingQueue<Staff>(1);
    private BlockingQueue<Staff> coffeeQueue = new ArrayBlockingQueue<Staff>(1);
    private boolean lastOrder = false;
    private boolean closingTime = false;
    private AtomicInteger custCount;
    private AtomicInteger juiceCount;
    private AtomicInteger cappuccinoCount;
    private AtomicLong minWait;
    private AtomicLong maxWait;
    private AtomicLong avgWait;
    private AtomicLong totalWait;

    public Cafe() {
        servingLst = new LinkedList<Customer>();
        seatingLst = new LinkedList<Customer>();
        custCount = new AtomicInteger(0);
        juiceCount = new AtomicInteger(0);
        cappuccinoCount = new AtomicInteger(0);
        minWait = new AtomicLong(0);
        maxWait = new AtomicLong(0);
        avgWait = new AtomicLong(0);
        totalWait = new AtomicLong(0);
    }

    public void serveCustomer(Staff staff) {
        Customer customer;
        synchronized (servingLst) {
            while (servingLst.size() == 0) {
                if (seatingLst.size() != 0) {
                    System.out.println(staff.title + ": All customers have ordered.");
                } else {
                    System.out.println(staff.title + ": Cafe is empty.");
                }
                try {
                    servingLst.wait();
                } catch (InterruptedException e) {
                    System.out.println(e.getStackTrace()[0]);
                }
            }
            customer = (Customer)((LinkedList<?>)servingLst).poll();
        }

        // Checking for null customer and returning accordingly
        if (customer.inTime == null) {
            try {
                if (staff.getClass() == Waiter.class) {
                    while(!lastOrder) Thread.sleep(50);
                    return;
                }
                if (!lastOrder) {
                    while (!lastOrder) Thread.sleep(50);
                } else {
                    while (!closingTime) Thread.sleep(50);
                }
                return;
            } catch (InterruptedException e) {
                System.out.println(e.getStackTrace()[0]);
            }
        }

        // Checking if customer is still in service list
        if (!seatingLst.contains(customer)) return;

        synchronized (customer) {
            customer.hasOrdered = true;
            customer.notify();
            System.out.println(staff.title + " is serving " + customer.name);
        }

        // To reject customers who were already in the cafe
        // before closing time but have not ordered.
        if (closingTime) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println(e.getStackTrace()[0]);
            }
            synchronized (customer) {
                customer.hasDrink = false;
                customer.notify();
            }
            return;
        }

        // Prepping drinks
        Drink drink;

        if (customer.orderDrink()) {
            drink = new Juice();
            System.out.println(customer.name + " ordered a juice.");
        } else {
            drink = new Cappuccino();
            System.out.println(customer.name + " ordered a cappuccino.");
        }

        if (drink.getClass() == Juice.class) {
            Juice juice = (Juice)drink;
            getGlass(juice, staff);
            getJuice(juice, staff);
            juice.isReady = true;
            juiceCount.getAndIncrement();
            System.out.println("\u001B[34m" + staff.title + " has delivered the juice to " + customer.name + "\u001B[0m");
        } else {
            Cappuccino cappuccino = (Cappuccino)drink;
            getCup(cappuccino, staff);
            while (!cappuccino.isReady) {
                getMilkOrCoffee(cappuccino, staff);
                if (cappuccino.hasMilk && cappuccino.hasCoffee) {
                    mixCappuccino(staff);
                    cappuccino.isReady = true;
                }
            }
            cappuccinoCount.getAndIncrement();
            System.out.println("\u001B[34m" + staff.title + " has delivered the cappuccino to " + customer.name + "\u001B[0m");
        }

        custCount.getAndIncrement();

        synchronized (customer) {
            customer.hasDrink = true;
            customer.notify();
        }
    }

    private void getGlass(Juice juice, Staff staff) {
        try {
            glassQueue.put(staff);
            System.out.println("\u001B[34m" + staff.title + " is getting a glass.\u001B[0m");
            Thread.sleep(500);
            System.out.println("\u001B[34m" + staff.title + " got a glass.\u001B[0m");
            glassQueue.take();
            juice.hasGlass = true;
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    private void getJuice(Juice juice, Staff staff) {
        try {
            juiceQueue.put(staff);
            System.out.println("\u001B[34m" + staff.title + " is getting juice.\u001B[0m");
            Thread.sleep(800);
            System.out.println("\u001B[34m" + staff.title + " got some juice.\u001B[0m");
            juiceQueue.take();
            juice.hasJuice = true;
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    private void getCup(Cappuccino cappuccino, Staff staff) {
        try {
            cupQueue.put(staff);
            System.out.println("\u001B[34m" + staff.title + " is getting a cup.\u001B[0m");
            Thread.sleep(500);
            System.out.println("\u001B[34m" + staff.title + " got a cup.\u001B[0m");
            cupQueue.take();
            cappuccino.hasCup = true;
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    private void getMilk(Cappuccino cappuccino, Staff staff) {
        if (cappuccino.hasMilk) return;
        try {
            System.out.println("\u001B[34m" + staff.title + " is attempting to get milk.\u001B[0m");
            boolean isAvailable = milkQueue.offer(staff);
            if (!isAvailable && !cappuccino.hasCoffee) {
                System.out.println("\u001B[34m" + staff.title + " failed to get milk.\u001B[0m");
                return;
            }
            else if (!isAvailable) {
                System.out.println("\u001B[34m" + staff.title + " is queueing to get milk.\u001B[0m");
                milkQueue.put(staff);
            }
            System.out.println("\u001B[34m" + staff.title + " is getting milk.\u001B[0m");
            Thread.sleep(1000);
            milkQueue.take();
            System.out.println("\u001B[34m" + staff.title + " got some milk.\u001B[0m");
            cappuccino.hasMilk = true;
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    private void getCoffee(Cappuccino cappuccino, Staff staff) {
        if (cappuccino.hasCoffee) return;
        try {
            System.out.println("\u001B[34m" + staff.title + " is attempting to get coffee.\u001B[0m");
            boolean isAvailable = coffeeQueue.offer(staff);
            if (!isAvailable && !cappuccino.hasMilk) {
                System.out.println("\u001B[34m" + staff.title + " failed to get coffee.\u001B[0m");
                return;
            }
            else if (!isAvailable) {
                System.out.println("\u001B[34m" + staff.title + " is queueing to get coffee.\u001B[0m");
                coffeeQueue.put(staff);
            }
            System.out.println("\u001B[34m" + staff.title + " is getting coffee.\u001B[0m");
            Thread.sleep(1000);
            System.out.println("\u001B[34m" + staff.title + " got some coffee.\u001B[0m");
            coffeeQueue.take();
            cappuccino.hasCoffee = true;
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    private void getMilkOrCoffee(Cappuccino cappuccino, Staff staff) {
        getMilk(cappuccino, staff);
        getCoffee(cappuccino, staff);
    }

    private void mixCappuccino(Staff staff) {
        try {
            System.out.println("\u001B[34m" + staff.title + " is mixing a cappuccino.\u001B[0m");
            Thread.sleep(800);
            System.out.println("\u001B[34m" + staff.title + " has finished mixing a cappuccino.\u001B[0m");
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    public synchronized void setLastOrder() {
        lastOrder = true;
    }

    public synchronized void setClosingTime() {
        closingTime = true;
    }

    public void add(Customer customer) {
        // To filter out null customers as they do not need to be added into the seating list
        if (customer.inTime == null) {
            synchronized (servingLst) {
                servingLst.offer(customer);
                if (servingLst.size() == 1) {
                    servingLst.notify();
                }
            }
            return;
        }

        System.out.println("\u001B[33m" + customer.name + " entering cafe at " + customer.inTime + "\u001B[0m");

        synchronized (seatingLst) {
            if (seatingLst.size() == numChair) {
                System.out.println("No chair available for " + customer.name);
                System.out.println("\u001B[33m" + customer.name + " exits the cafe.\u001B[0m");
                return;
            }
            seatingLst.offer(customer);
        }
        synchronized (servingLst) {
            servingLst.offer(customer);
            if (servingLst.size() == 1) {
                servingLst.notify();
            }
        }
    }

    public boolean awaitOrder(Customer customer) {
        long duration = 0;
        try {
            duration = (long)((Math.random() * 2) + 1);
            customer.wait(duration * 1000);
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
        if (customer.hasOrdered) return true;
        else {
            synchronized (seatingLst) {
                seatingLst.remove(customer);
            }
            System.out.println("\u001B[33m" + customer.name + " was not served within " + duration + " seconds and has left the cafe.\u001B[0m");
            return false;
        }
    }

    public void enjoyDrink(Customer customer) {
        long duration = (long)((Math.random() * 5) + 1);
        try {
            customer.wait();

            // Rejected customers
            if (!customer.hasDrink) {
                System.out.println("\u001B[33m" + customer.name + " was rejected and left.\u001B[0m");
                seatingLst.remove(customer);
                return;
            }

            // Calculating waitingDuration
            customer.waitingDuration = new Date().getTime() - customer.inTime.getTime();

            // Normal customers
            System.out.println(customer.name + " is enjoying their drink.");
            TimeUnit.SECONDS.sleep(duration);
            System.out.println(customer.name + " has finished their drink in " + duration + " seconds.");
            synchronized (seatingLst) {
                seatingLst.remove(customer);
            }
            System.out.println("\u001B[33m" + customer.name + " exits the cafe.\u001B[0m");
        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace()[0]);
        }
    }

    public void logStats(Customer customer) {

        // Filter out rejected customers
        if (!customer.hasDrink) return;

        totalWait.getAndAdd(customer.waitingDuration);
        if (minWait.get() == 0) {
            minWait.getAndSet(customer.waitingDuration);
            maxWait.getAndSet(customer.waitingDuration);
            avgWait.getAndSet(customer.waitingDuration);
            return;
        }
        if (customer.waitingDuration < minWait.get()) minWait.getAndSet(customer.waitingDuration);
        if (customer.waitingDuration > maxWait.get()) maxWait.getAndSet(customer.waitingDuration);
        avgWait.getAndSet(totalWait.get() / custCount.get());
    }

    public void printStats() {
        System.out.println("");
        System.out.println("\u001B[35m" + "---------- Statistics for the day ----------" + "\u001B[0m");
        System.out.println("Minimum waiting time       : " + String.format("%.3f", (minWait.get()/1000.0)) + " seconds");
        System.out.println("Maximum waiting time       : " + String.format("%.3f", (maxWait.get()/1000.0)) + " seconds");
        System.out.println("Average waiting time       : " + String.format("%.3f", (avgWait.get()/1000.0)) + " seconds");
        System.out.println("Number of customers served : " + custCount.get());
        System.out.println("Glasses of juice served    : " + juiceCount.get());
        System.out.println("Cups of cappuccino served  : " + cappuccinoCount.get());
        System.out.println("");
    }
}
