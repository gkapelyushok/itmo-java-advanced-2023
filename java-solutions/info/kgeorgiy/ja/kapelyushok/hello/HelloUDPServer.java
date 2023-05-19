package info.kgeorgiy.ja.kapelyushok.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService workers;
    /**
     * Starts a new Hello server.
     * This method should return immediately.
     *
     * @param port    server port.
     * @param threads number of working threads.
     */
    @Override
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            workers = Executors.newFixedThreadPool(threads);
            IntStream.range(0, threads).forEach(threadId -> workers.submit(this::receiveRequests));
        } catch (SocketException e) {
            System.out.println("Failed to create socket: " + e.getMessage());
        }
    }

    private void receiveRequests() {
        try {
            DatagramPacket requestPacket = Utils.getResponsePacket(socket);
            while (!socket.isClosed()) {
                socket.receive(requestPacket);
                String responseString = "Hello, " + Utils.getData(requestPacket);
                DatagramPacket responsePacket = Utils.getRequestPacket(responseString, requestPacket.getSocketAddress());
                socket.send(responsePacket);
            }
        } catch (IOException e) {
            System.out.println("Exception during receiving packets: " + e.getMessage());
        }
    }

    /**
     * Stops server and deallocates all resources.
     */
    @Override
    public void close() {
        socket.close();
        workers.close();
    }
}
