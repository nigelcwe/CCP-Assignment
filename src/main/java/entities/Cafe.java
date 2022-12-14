package entities;

import entities.drinks.Cappuccino;
import entities.drinks.Drink;
import entities.drinks.Juice;
import entities.misc.SynchronizedInt;
import entities.misc.SynchronizedLong;
import runnable.customer.Customer;
import runnable.staff.Staff;

import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;

public class Cafe {
    public int numChair = 10;
    public final LinkedList<Customer> servingLst;
    public final LinkedList<Customer> seatingLst;
    private final ArrayBlockingQueue<Staff> glassQueue = new ArrayBlockingQueue<>(1);
    private final ArrayBlockingQueue<Staff> cupQueue = new ArrayBlockingQueue<>(1);
    private final ArrayBlockingQueue<Staff> juiceQueue = new ArrayBlockingQueue<>(1);
    private final ArrayBlockingQueue<Staff> milkQueue = new ArrayBlockingQueue<>(1);
    private final ArrayBlockingQueue<Staff> coffeeQueue = new ArrayBlockingQueue<>(1);
    private boolean lastOrder = false;
    private boolean closingTime = false;
    private final SynchronizedInt custCount;
    private final SynchronizedInt juiceCount;
    private final SynchronizedInt cappuccinoCount;
    private final SynchronizedLong minWait;
    private final SynchronizedLong maxWait;
    private final SynchronizedLong avgWait;
    private final SynchronizedLong totalWait;

    public Cafe() {
        servingLst = new LinkedList<>();
        seatingLst = new LinkedList<>();
        custCount = new SynchronizedInt();
        juiceCount = new SynchronizedInt();
        cappuccinoCount = new SynchronizedInt();
        minWait = new SynchronizedLong();
        maxWait = new SynchronizedLong();
        avgWait = new SynchronizedLong();
        totalWait = new SynchronizedLong();
    }

    public void serveCustomer(Staff staff) {
        Customer customer;
        synchronized (servingLst) {
            while (servingLst.size() == 0) {
                if (seatingLst.size() != 0) {
                    System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + ": All customers have ordered.");
                } else {
                    System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + ": Cafe is empty.");
                }
                try {
                    servingLst.wait();
                    if (closingTime) return;
                    if (lastOrder) return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            customer = (Customer)((LinkedList<?>)servingLst).poll();
        }

        // Checking if customer is still in seating list
        if (!seatingLst.contains(customer)) return;

        synchronized (customer) {
            customer.hasOrdered = true;
            customer.notify();
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is serving " + customer.name);
        }

        // To reject customers who were already in the cafe
        // before closing time but have not ordered.
        if (closingTime) {

            // To set a time delay just in case closingTime was just called
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + ": " + customer.name + " ordered a juice.");
        } else {
            drink = new Cappuccino();
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + ": " + customer.name + " ordered a cappuccino.");
        }

        if (drink.getClass() == Juice.class) {
            Juice juice = (Juice)drink;
            getGlass(juice, staff);
            getJuice(juice, staff);
            juice.isReady = true;
            juiceCount.increment();
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " has delivered the juice to " + customer.name + "\u001B[0m");
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
            cappuccinoCount.increment();
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " has delivered the cappuccino to " + customer.name + "\u001B[0m");
        }

        custCount.increment();

        synchronized (customer) {
            customer.hasDrink = true;
            customer.notify();
        }
    }

    private void getGlass(Juice juice, Staff staff) {
        try {
            glassQueue.put(staff);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is getting a glass.\u001B[0m");
            Thread.sleep(500);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " got a glass.\u001B[0m");
            glassQueue.take();
            juice.hasGlass = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getJuice(Juice juice, Staff staff) {
        try {
            juiceQueue.put(staff);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is getting juice.\u001B[0m");
            Thread.sleep(800);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " got some juice.\u001B[0m");
            juiceQueue.take();
            juice.hasJuice = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getCup(Cappuccino cappuccino, Staff staff) {
        try {
            cupQueue.put(staff);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is getting a cup.\u001B[0m");
            Thread.sleep(500);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " got a cup.\u001B[0m");
            cupQueue.take();
            cappuccino.hasCup = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getMilk(Cappuccino cappuccino, Staff staff) {
        if (cappuccino.hasMilk) return;
        try {
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is attempting to get milk.\u001B[0m");
            boolean isAvailable = milkQueue.offer(staff);
            if (!isAvailable && !cappuccino.hasCoffee) {
                System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " failed to get milk.\u001B[0m");
                return;
            }
            else if (!isAvailable) {
                System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is queueing to get milk.\u001B[0m");
                milkQueue.put(staff);
            }
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is getting milk.\u001B[0m");
            Thread.sleep(1000);
            milkQueue.take();
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " got some milk.\u001B[0m");
            cappuccino.hasMilk = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getCoffee(Cappuccino cappuccino, Staff staff) {
        if (cappuccino.hasCoffee) return;
        try {
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is attempting to get coffee.\u001B[0m");
            boolean isAvailable = coffeeQueue.offer(staff);
            if (!isAvailable && !cappuccino.hasMilk) {
                System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " failed to get coffee.\u001B[0m");
                return;
            }
            else if (!isAvailable) {
                System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is queueing to get coffee.\u001B[0m");
                coffeeQueue.put(staff);
            }
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is getting coffee.\u001B[0m");
            Thread.sleep(1000);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " got some coffee.\u001B[0m");
            coffeeQueue.take();
            cappuccino.hasCoffee = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void getMilkOrCoffee(Cappuccino cappuccino, Staff staff) {
        getMilk(cappuccino, staff);
        getCoffee(cappuccino, staff);
    }

    private void mixCappuccino(Staff staff) {
        try {
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " is mixing a cappuccino.\u001B[0m");
            Thread.sleep(800);
            System.out.println("\u001B[34m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + staff.title + " has finished mixing a cappuccino.\u001B[0m");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setLastOrder() {
        lastOrder = true;
    }

    public void setClosingTime() {
        closingTime = true;
    }

    public boolean add(Customer customer) {
        customer.inTime = new Date();
        System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " trying to enter cafe at " + customer.inTime + "\u001B[0m");

        if (lastOrder){
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Cafe is only receiving last orders.");
            System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " leaves.\u001B[0m");
            return false;
        }
        else if (closingTime){
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Cafe is closed.");
            System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " leaves.\u001B[0m");
            return false;
        }
        if (seatingLst.size() == numChair) {
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "No chair available for " + customer.name);
            System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " leaves.\u001B[0m");
            return false;
        }

        customer.inTime = new Date();
        System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " successfully enters the cafe at " + customer.inTime + "\u001B[0m");
        synchronized (seatingLst) {
            seatingLst.offer(customer);
        }
        synchronized (servingLst) {
            servingLst.offer(customer);
            if (servingLst.size() == 1) {
                servingLst.notify();
            }
        }
        return true;
    }

    public boolean awaitOrder(Customer customer) {
        long duration = 0;
        try {
            duration = (long)((Math.random() * 2) + 1);
            customer.wait(duration * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Customer was served in time
        if (customer.hasOrdered) return true;

        // Rejected customers
        else if (this.closingTime) {
            synchronized (seatingLst) {
                seatingLst.remove(customer);
            }
            System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " was rejected and left.\u001B[0m");
            return false;
        }

        // Customers who were not served in time
        else {
            synchronized (seatingLst) {
                seatingLst.remove(customer);
            }
            System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " was not served within " + duration + " seconds and has left the cafe.\u001B[0m");
            return false;
        }
    }

    public void enjoyDrink(Customer customer) {
        long duration = (long)((Math.random() * 5) + 1);
        try {
            customer.wait();

            // Calculating waitingDuration
            customer.waitingDuration = new Date().getTime() - customer.inTime.getTime();

            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " is enjoying their drink.");
            Thread.sleep(duration * 1000);
            System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " has finished their drink in " + duration + " seconds.");
            synchronized (seatingLst) {
                seatingLst.remove(customer);
            }
            logStats(customer);
            System.out.println("\u001B[33m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " exits the cafe.\u001B[0m");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logStats(Customer customer) {
        System.out.println(Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + customer.name + " is providing their waiting statistics.");

        // Filter out rejected customers
//        if (!customer.hasDrink) return;

        totalWait.add(customer.waitingDuration);
        if (minWait.get() == 0) {
            minWait.set(customer.waitingDuration);
            maxWait.set(customer.waitingDuration);
            avgWait.set(customer.waitingDuration);
            return;
        }
        if (customer.waitingDuration < minWait.get()) minWait.set(customer.waitingDuration);
        if (customer.waitingDuration > maxWait.get()) maxWait.set(customer.waitingDuration);
        avgWait.set(totalWait.get() / custCount.get());
    }

    public void printStats() {
        System.out.println("");
        System.out.println(Thread.currentThread().getName() + " : " + "Owner: ");
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
