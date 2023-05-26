package info.kgeorgiy.ja.kapelyushok.bank;

import java.rmi.RemoteException;

public class LocalAccount extends AbstractAccount {
    public LocalAccount(Account account) throws RemoteException {
        super(account);
    }

}
