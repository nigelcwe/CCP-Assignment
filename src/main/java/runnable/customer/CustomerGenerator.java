package runnable.customer;

import entities.Cafe;

import java.time.LocalTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CustomerGenerator implements Runnable {
    private final Cafe cafe;
    private boolean lastOrder = false;
    private boolean closingTime = false;

    public CustomerGenerator(Cafe cafe) {
        this.cafe = cafe;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(105);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Customer Generator has started." + "\u001B[0m");
        int count = 0;
        while (!lastOrder) {
            if (count == 10) break;
            if (count < 10) {
                createCustomer(cafe);
                count ++;
            }
            try {
                TimeUnit.SECONDS.sleep((long)((Math.random() * 2) + 1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        createNullCustomer(cafe, 4);

        while (!closingTime) {
            if (count == 10) break;
            if (count < 10) {
                createCustomer(cafe);
                count ++;
            }
            try {
                TimeUnit.SECONDS.sleep((long)((Math.random() * 2) + 1));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized (this) {
            while (!closingTime) {
                try {
                    this.wait(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        createNullCustomer(cafe, 4);

        System.out.println("\u001B[32m" + Thread.currentThread().getName() + " : " + LocalTime.now() + " : " + "Customer Generator has ended safely." + "\u001B[0m");
    }

    private void createCustomer(Cafe cafe) {
        Customer customer = new Customer(cafe);
        customer.inTime = new Date();
        Thread thCustomer = new Thread(customer);
        customer.name = "Customer " + thCustomer.getId();
        thCustomer.setName("Thread-Customer-" + thCustomer.getId());
        thCustomer.start();
    }

    private void createNullCustomer(Cafe cafe, int count) {
        while (count > 0) {
            Customer nullCustomer = new Customer(cafe);
            Thread thNullCustomer = new Thread(nullCustomer);
            thNullCustomer.start();
            count --;
        }
    }

    public synchronized void setLastOrder() {
        lastOrder = true;
    }

    public synchronized void setClosingTime() {
        closingTime = true;
    }
}
