package info.kgeorgiy.ja.kapelyushok.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Person extends Remote {
    String getFirstName() throws RemoteException;
    String getLastName() throws RemoteException;
    String getPassport() throws RemoteException;
    Map<String, Account> getAccounts() throws RemoteException;
}
