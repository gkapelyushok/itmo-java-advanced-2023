package info.kgeorgiy.ja.kapelyushok.bank;

import java.util.Map;

public class LocalPerson extends AbstractPerson {
    public LocalPerson(String firstName, String lastName, String passport, Map<String, Account> accounts) {
        super(firstName, lastName, passport, accounts);
    }
}
