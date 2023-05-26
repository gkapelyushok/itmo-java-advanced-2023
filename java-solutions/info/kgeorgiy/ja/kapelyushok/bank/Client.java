package info.kgeorgiy.ja.kapelyushok.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Objects;

public final class Client {
    /** Utility class. */
    private Client() {}

    public static void main(final String... args) throws RemoteException {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Arguments should be: <firstName> <lastName> <passport> <subId> <amount>");
            return;
        }
        String firstName = args[0];
        String lastName = args[1];
        String passport = args[2];
        String subId = args[3];
        int amount;
        try {
            amount = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            System.out.println("Amount should be integer");
            return;
        }

        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        Person person = bank.getPerson(passport, "remote");
        if (person == null) {
            bank.createPerson(firstName, lastName, passport);
        } else {
            if (!person.getFirstName().equals(firstName) || !person.getLastName().equals(lastName)) {
                System.out.println("Incorrect personal data");
                return;
            }
        }

        Account account = bank.getAccount(passport + ":" + subId);
        if (account == null) {
            System.out.println("Creating account");
            account = bank.createAccount(passport + ":" + subId);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Money: " + account.getAmount());
        System.out.println("Adding money");
        account.setAmount(account.getAmount() + amount);
        System.out.println("Money: " + account.getAmount());
    }
}