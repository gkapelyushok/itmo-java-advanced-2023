package info.kgeorgiy.ja.kapelyushok.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {

    /**
     * Runs Hello client.
     * This method should return when all requests are completed.
     *
     * @param host     server host
     * @param port     server port
     * @param prefix   request prefix
     * @param threads  number of request threads
     * @param requests number of requests per thread.
     */
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        new Sender(host, port, prefix, threads, requests).run();
    }
    private static class Sender {
        private final SocketAddress socketAddress;
        private final ExecutorService workers;
        private final String prefix;
        private final int threads;
        private final int requests;
        private final static String responseRegex = "[\\D]*%d[\\D]+%d[\\D]*";
        private final static int TIMEOUT = 100;

        public Sender(String host, int port, String prefix, int threads, int requests) {
            socketAddress = new InetSocketAddress(host, port);
            workers = Executors.newFixedThreadPool(threads);
            this.prefix = prefix;
            this.threads = threads;
            this.requests = requests;
        }

        public void run() {
            for (int threadId = 1; threadId <= threads; ++threadId) {
                int finalThreadId = threadId;
                workers.submit(() -> sendRequests(finalThreadId));
            }
            workers.close();
        }

        private void sendRequests(int threadId) {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT);
                for (int requestId = 1; requestId <= requests; ++requestId){
                    sendRequest(socket, threadId, requestId);
                }
            } catch (SocketException e) {
                System.out.println("Failed to create socket: " + e.getMessage());
            }
        }

        private void sendRequest(DatagramSocket socket, int threadId, int requestId) throws SocketException {
            String requestString = prefix + threadId + "_" + requestId;
            DatagramPacket requestPacket = Utils.getRequestPacket(requestString, socketAddress);
            DatagramPacket responsePacket = Utils.getResponsePacket(socket);
            String expectedResponseString = String.format(responseRegex, threadId, requestId);
            while (true) {
                try {
                    socket.send(requestPacket);
                    socket.receive(responsePacket);
                } catch (IOException e) {
                    continue;
                }
                String responseString = Utils.getData(responsePacket);
                for (int i = 0; i < responseString.length(); ++i) {
                    if (Character.isDigit(responseString.charAt(i))) {
                        responseString = responseString.substring(0, i) + Character.getNumericValue(responseString.charAt(i)) + responseString.substring(i + 1);
                    }
                }

                if (responseString.matches(expectedResponseString)) {
                    break;
                }
            }
        }
    }
}
