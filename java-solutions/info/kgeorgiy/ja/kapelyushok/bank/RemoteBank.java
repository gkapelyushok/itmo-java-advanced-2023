package info.kgeorgiy.ja.kapelyushok.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(final String id) throws RemoteException {
        String[] tokens = id.split(":");
        String passport = tokens[0];
        String subId = tokens[1];
        Person person = persons.get(passport);
        final Account account = new RemoteAccount(subId);
        if (person.getAccounts().putIfAbsent(subId, account) == null) {
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return person.getAccounts().get(subId);
        }
    }

    @Override
    public Account getAccount(final String id) throws RemoteException {
        String[] tokens = id.split(":");
        String passport = tokens[0];
        String subId = tokens[1];
        return persons.get(passport).getAccounts().get(subId);
    }

    @Override
    public Person createPerson(String firstName, String lastName, String passport) throws RemoteException {
        if (firstName == null || lastName == null) {
            return null;
        }
        RemotePerson person = new RemotePerson(firstName, lastName, passport);
        if (persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return getPerson(passport, "remote");
        }
    }

    @Override
    public Person getPerson(String passport, String mode) throws RemoteException {
        Person person = persons.get(passport);
        if (mode.equals("remote")) {
            return person;
        } else {
            Map<String, Account> localAccounts = new HashMap<>();
            for (Map.Entry<String, Account> e : person.getAccounts().entrySet()) {
                localAccounts.put(e.getKey(), new LocalAccount(e.getValue()));
            }
            return new LocalPerson(person.getFirstName(), person.getLastName(), person.getPassport(), localAccounts);
        }
    }
}

