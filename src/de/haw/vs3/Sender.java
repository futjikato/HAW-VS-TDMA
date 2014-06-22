package de.haw.vs3;

import java.net.MulticastSocket;

/**
 * Sender
 *
 * Send packets over multicast socket
 */
public final class Sender extends Thread {

    private MulticastSocket socket;

    private char stationClass;

    public Sender(MulticastSocket socket, char stationClass) {
        this.socket = socket;
        this.stationClass = stationClass;
    }

    public void run() {
        //todo
    }

}
