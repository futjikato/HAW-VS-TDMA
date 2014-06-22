package de.haw.vs3;

import java.net.MulticastSocket;

/**
 * Receiver receives messages over multicast socket
 */
public final class Receiver extends Thread {

    private MulticastSocket socket;

    public Receiver(MulticastSocket socket) {
        this.socket = socket;
    }

    public void run() {
        // todo
    }
}
