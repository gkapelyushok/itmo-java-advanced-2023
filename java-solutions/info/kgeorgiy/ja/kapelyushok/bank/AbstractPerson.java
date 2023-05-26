package info.kgeorgiy.ja.kapelyushok.bank;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractPerson implements Person, Serializable {
    private final String firstName;
    private final String lastName;
    private final String passport;
    private final Map<String, Account> accounts;

    protected AbstractPerson(String firstName, String lastName, String passport) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
        accounts = new ConcurrentHashMap<>();
    }

    protected AbstractPerson(String firstName, String lastName, String passport, Map<String, Account> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passport = passport;
        this.accounts = accounts;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    @Override
    public Map<String, Account> getAccounts() {
        return accounts;
    }
}
