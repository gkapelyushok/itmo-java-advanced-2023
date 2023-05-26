package info.kgeorgiy.ja.kapelyushok.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;

public final class Server {
    private final static int PORT = 8888;

    public static void main(final String... args) throws RemoteException {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : PORT;
        Registry registry = LocateRegistry.createRegistry(port);
        final Bank bank = new RemoteBank(port);
        try {
            Bank stub = (Bank) UnicastRemoteObject.exportObject(bank, port);
            registry.rebind("bank", stub);
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
