package de.haw.vs3;

import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Sender
 *
 * Send packets over multicast socket
 */
public final class Sender extends Thread {

    private final static int FRAME_LENGTH_MS = 1000;

    private final static int SLOT_AMOUNT = 25;

    private MulticastSocket socket;

    private char stationClass;

    private Set<Integer> reservedSlots;

    private long timestamp;

    private Random rng;

    private int teamNo;

    private int stationNo;

    private Reader reader;

    private int nextSlot;
    private int lastSlot;
    private long frameStartTime;

    public Sender(MulticastSocket socket, char stationClass) {
        this.socket = socket;
        this.stationClass = stationClass;

        this.reservedSlots = new CopyOnWriteArraySet<Integer>();
        this.rng = new Random();
    }

    public void run() {
        while(!isInterrupted()) {
            waitForNextFrame();
            sendPackage();
        }
    }

    private void sendPackage() {
        try {
            Package pack = createpackage();
            DatagramPacket packet = new DatagramPacket(new byte[0], 0, socket.getRemoteSocketAddress());
            packet.setData(pack.getByteArray());
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void waitForNextFrame() {
        

        // time till sending next slot
    }

    public void addReservedSlot(int slotNo) {
        reservedSlots.add(slotNo);
    }

    public void syncTime(long timestamp) {
        this.timestamp = (timestamp + this.timestamp) / 2;
    }

    public void setTime(long time) {
        timestamp = time;
    }

    public void setStationNo(int stationNo) {
        this.stationNo = stationNo;
    }

    public void setTeamNo(int teamNo) {
        this.teamNo = teamNo;
    }

    private Package createpackage() throws Exception {
        Package pack = new Package();

        // might be too large but the right amount will be send because of length check in Package class
        String payload = "team 9-";
        String dsStr = reader.getDatasourceString();
        String finalPayload = payload.concat(dsStr);

        // save current slot to calculate time left in this frame
        lastSlot = nextSlot;
        // get next free slot
        nextSlot = getRandomFreeSlot();

        pack.setNextSlotNo(nextSlot);
        pack.setPayload(finalPayload);
        pack.setStaticClass(stationClass);
        pack.setTimestamp(timestamp);

        return pack;
    }

    private int getRandomFreeSlot() throws Exception {
        Set slots = new HashSet<Integer>();
        for(int i = 0 ; i < SLOT_AMOUNT ; i++) {
            if(!reservedSlots.contains(i)) {
                slots.add(i);
            }
        }

        if(slots.isEmpty()) {
            throw new Exception("No free slot to send in. Too many stations!");
        }

        int index = rng.nextInt(slots.size());
        Integer[] slotAry = (Integer[]) slots.toArray(new Integer[slots.size()]);

        return slotAry[index];
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }
}
