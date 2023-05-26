package info.kgeorgiy.ja.kapelyushok.bank;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@RunWith(JUnit4.class)
public class BankTest {
    private static final int PORT = 8888;
    private static Bank bank;
    private static Registry registry;
    private static final String ACCOUNT_ID = "%s:%d";

    @BeforeClass
    public static void beforeAll() throws RemoteException {
        registry = LocateRegistry.createRegistry(PORT);
    }

    @Before
    public void beforeEach() throws RemoteException {
        bank = new RemoteBank(PORT);
        Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, PORT);
        registry.rebind("bank", stub);
    }

    @Test
    public void test01_RemoteAndLocalPersons() throws RemoteException {
        String firstName = "test01_firstName";
        String lastName = "test01_lastName";
        String passport = "test01_passport";

        bank.createPerson(firstName, lastName, passport);
        Person remotePerson = bank.getPerson(passport, "remote");
        Person localPerson = bank.getPerson(passport, "local");

        Assert.assertNotNull(remotePerson);
        Assert.assertNotNull(localPerson);

        String subId = "test01_subId";
        String accountId = remotePerson.getPassport() + ":" + subId;
        bank.createAccount(accountId);

        Assert.assertEquals(1, remotePerson.getAccounts().size());
        Assert.assertEquals(0, localPerson.getAccounts().size());
    }

    private void testAccount(String id) throws RemoteException {
        Account account = bank.createAccount(id);
        Assert.assertNotNull(account);
        Assert.assertEquals(0, account.getAmount());
        account.setAmount(10);
        Assert.assertEquals(10, account.getAmount());
    }
    @Test
    public void test02_createManyAccounts() throws RemoteException {
        String firstName = "test02_firstName";
        String lastName = "test02_lastName";
        String passport = "test02_passport";
        bank.createPerson(firstName, lastName, passport);
        for (int i = 0; i < 1000; i++) {
            testAccount(String.format(ACCOUNT_ID, passport, i));
        }
    }

    @Test
    public void test03_createManyPersons() throws RemoteException {
        String firstName = "test03_firstName";
        String lastName = "test03_lastName";
        String passport = "test03_passport";
        for (int i = 0; i < 1000; i++) {
            Person person = bank.createPerson(firstName + i, lastName + i, passport + i);
            Assert.assertNotNull(person);
            for (int j = 0; j < 10; j++) {
                testAccount(String.format(ACCOUNT_ID, person.getPassport(), j));
            }
        }
    }

    @Test
    public void test04_parallelAccounts() throws RemoteException {
        ExecutorService workers = Executors.newFixedThreadPool(10);
        String firstName = "test04_firstName";
        String lastName = "test04_lastName";
        String passport = "test04_passport";
        bank.createPerson(firstName, lastName, passport);
        IntStream.of(1000).forEach(i -> workers.submit(() -> {
            try {
                testAccount(String.format(ACCOUNT_ID, passport, i));
            } catch (RemoteException e) {
                System.err.println("RemoteException while testing parallel accounts: "+ e.getMessage());
            }
        }));
    }

    @Test
    public void test05_parallelPersons() {
        ExecutorService workers = Executors.newFixedThreadPool(10);
        String firstName = "test05_firstName";
        String lastName = "test05_lastName";
        String passport = "test05_passport";
        IntStream.of(1000).forEach(i -> workers.submit(() -> {
            try {
                Person person = bank.createPerson(firstName + i, lastName + i, passport + i);
                for (int j = 0; j < 10; j++) {
                    testAccount(String.format(ACCOUNT_ID, person.getPassport(), j));
                }
            } catch (RemoteException e) {
                System.err.println("RemoteException while testing parallel persons: "+ e.getMessage());
            }
        }));
    }

}
