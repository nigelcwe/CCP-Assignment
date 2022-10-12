import entities.Cafe;
import runnable.Clock;
import runnable.customer.CustomerGenerator;
import runnable.staff.Owner;
import runnable.staff.Waiter;

public class main {
    public static void main(String[] args) {
        Cafe cafe = new Cafe();
        Owner owner = new Owner(cafe);
        Waiter waiter = new Waiter(cafe);
        CustomerGenerator custGen = new CustomerGenerator(cafe);
        Clock clock = new Clock(custGen, owner, waiter, cafe);

        Thread thOwner = new Thread(owner);
        Thread thWaiter = new Thread(waiter);
        Thread thCustGen = new Thread(custGen);
        Thread thClock = new Thread(clock);

        thOwner.start();
        thWaiter.start();
        thCustGen.start();
        thClock.start();
    }
}
