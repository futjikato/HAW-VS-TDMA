package de.haw.vs3;

import java.net.*;
import java.util.Calendar;
import java.util.Date;

public class Station {

    public static void main(String[] args) {
        final String netInterfaceName = args[0];
        final String address = args[1];
        final int port = Integer.parseInt(args[2]);
        final char stationClass = args[3].charAt(0);
        final int utcOffset = Integer.parseInt(args[4]);

        Reader reader = new Reader();
        reader.start();

        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(netInterfaceName);

            SocketAddress sockAddress = new InetSocketAddress(address, port);

            MulticastSocket sendSocket = new MulticastSocket();
            sendSocket.setNetworkInterface(networkInterface);

            MulticastSocket receiveSocket = new MulticastSocket(port);
            receiveSocket.setNetworkInterface(networkInterface);
            receiveSocket.joinGroup(InetAddress.getByName(address));

            Sender sender = new Sender(sockAddress, sendSocket, stationClass);
            sender.setOffset(utcOffset);
            sender.setReader(reader);

            Receiver receiver = new Receiver(receiveSocket, sender);

            sender.start();
            receiver.start();
        } catch (Exception e) {
            System.err.println("Error creating network stuff");
            e.printStackTrace();
        }
    }

    public static long getTime(long offset) {
        // get time in UTC
        return System.currentTimeMillis() + offset;
    }

    public static long getOffset(long timestamp) {
        return (timestamp - System.currentTimeMillis());
    }

}
