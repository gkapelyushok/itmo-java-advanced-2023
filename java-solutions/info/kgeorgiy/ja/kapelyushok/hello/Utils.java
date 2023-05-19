package info.kgeorgiy.ja.kapelyushok.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static DatagramPacket getRequestPacket(String requestName, SocketAddress socketAddress) {
        return new DatagramPacket(requestName.getBytes(StandardCharsets.UTF_8), requestName.getBytes().length, socketAddress);
    }
    public static DatagramPacket getResponsePacket(DatagramSocket socket) throws SocketException {
        return new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
    }

    public static String getData(DatagramPacket packet) {
        return new String(packet.getData(),
                          packet.getOffset(),
                          packet.getLength(),
                          StandardCharsets.UTF_8);
    }
}
