package de.haw.vs3;

import java.net.*;

/**
 * @author moritzspindelhirn
 * @todo Documentation
 * @category de.haw.vs3
 */
public class Station {

    public static void main(String[] args) {
        final String netInterfaceName = args[0];
        final String address = args[1];
        final int port = Integer.parseInt(args[2]);
        final char stationClass = args[3].charAt(0);
        final int utcOffset = Integer.parseInt(args[4]);
        final int teamNo = Integer.parseInt(args[5]);
        final int stationNo = Integer.parseInt(args[6]);

        Reader reader = new Reader();
        reader.start();

        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(netInterfaceName);

            SocketAddress group = new InetSocketAddress(address, port);

            MulticastSocket sendSocket = new MulticastSocket();
            MulticastSocket receiveSocket = new MulticastSocket();

            sendSocket.joinGroup(group, networkInterface);
            receiveSocket.joinGroup(group, networkInterface);

            Sender sender = new Sender(sendSocket, stationClass);
            sender.setTime(System.currentTimeMillis() + utcOffset);
            sender.setTeamNo(teamNo);
            sender.setStationNo(stationNo);
            sender.setReader(reader);

            Receiver receiver = new Receiver(receiveSocket, sender);

            sender.start();
            receiver.start();
        } catch (Exception e) {
            System.err.println("Error creating network stuff");
            e.printStackTrace();
        }
    }

}
