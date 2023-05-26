package info.kgeorgiy.ja.kapelyushok.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public abstract class AbstractAccount implements Account, Serializable {
    private final String id;
    private int amount;

    protected AbstractAccount(final String id) {
        this.id = id;
        amount = 0;
    }

    protected AbstractAccount(Account account) throws RemoteException {
        id = account.getId();
        amount = account.getAmount();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized int getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final int amount) {
        this.amount = amount;
    }
}
