package info.kgeorgiy.ja.kapelyushok.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    Account createAccount(String id) throws RemoteException;
    Account getAccount(String id) throws RemoteException;
    Person createPerson(String firstName, String lastName, String passport) throws RemoteException;
    Person getPerson(String passport, String mode) throws RemoteException;
}