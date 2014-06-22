package de.haw.vs3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

/**
 * Receiver receives messages over multicast socket
 */
public final class Receiver extends Thread {

    private MulticastSocket socket;

    private Sender sender;

    public Receiver(MulticastSocket socket, Sender sender) {
        this.socket = socket;
        this.sender = sender;
    }

    public void run() {
        while(!isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[34], 34);
                socket.receive(packet);

                Package pack = Package.createFromByteArray(packet.getData());
                processPackage(pack);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processPackage(Package packet) {
        System.out.println(String.format("Received message : %s ( Class %s | Send at %d )",
            packet.getPayload(),
            packet.getStaticClass(),
            packet.getTimestamp()));

        // inform sender about reserved slot
        sender.addReservedSlot(packet.getNextSlotNo());

        if(packet.getStaticClass() == 'A') {
            sender.syncTime(Station.getOffset(packet.getTimestamp()));
        }
    }
}
