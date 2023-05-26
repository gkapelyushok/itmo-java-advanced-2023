package info.kgeorgiy.ja.kapelyushok.bank;

import java.io.Serializable;
import java.rmi.RemoteException;

public class LocalAccount implements Account, Serializable {
    private final String id;
    private int amount;

    public LocalAccount(Account account) throws RemoteException {
        id = account.getId();
        amount = account.getAmount();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public void setAmount(final int amount) {
        this.amount = amount;
    }
}
