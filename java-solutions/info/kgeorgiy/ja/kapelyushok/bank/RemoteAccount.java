package info.kgeorgiy.ja.kapelyushok.bank;

public class RemoteAccount implements Account {
    private final String id;
    private int amount;

    public RemoteAccount(final String id) {
        this.id = id;
        amount = 0;
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