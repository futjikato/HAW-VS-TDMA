package de.haw.vs3;

import java.io.IOException;
import java.lang.Runnable;
import java.lang.Thread;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sender
 *
 * Send packets over multicast socket
 */
public final class Sender extends Thread {

    private final static int FRAME_LENGTH_MS = 1000;

    private final static int SLOT_AMOUNT = 25;

    private SocketAddress socketAddress;

    private MulticastSocket socket;

    private char stationClass;

    private Set<Integer> reservedSlots;

    private Random rng;

    private ReentrantLock lock;

    private Reader reader;

    /**
     * slot to send the next package in
     */
    private int nextSlot;

    /**
     * Offset relative to UTC
     */
    private long timeOffset;

    /**
     * slot to reserve in next package
     */
    private int sendSlot;

    public Sender(SocketAddress sockAddress, MulticastSocket socket, char stationClass) throws IOException {
        this.socket = socket;
        this.lock = new ReentrantLock();
        this.socket.setTimeToLive(1);

        this.stationClass = stationClass;
        this.socketAddress = sockAddress;

        this.reservedSlots = new CopyOnWriteArraySet<Integer>();
        this.rng = new Random();
    }

    @Override
	public void run() {
        try {
            waitForNextFrame();
            nextSlot = getRandomFreeSlot();
            sendSlot = getRandomFreeSlot();
            while(!isInterrupted()) {

                SlotThread st = new SlotThread(SLOT_AMOUNT);
                st.start();
                System.err.println(String.format("The next SlotThread started at %d", System.currentTimeMillis()));

                waitForNextFrame();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Station out.");
        }
    }

    private class SlotThread extends Thread{
        private int slotAmount;

        public SlotThread(int numberOfSlots){
            this.slotAmount = numberOfSlots;
        }

        @Override
		public void run(){
            for (int slotCount = 1; slotCount <= slotAmount; slotCount++){
                if(reservedSlots.contains(nextSlot)) {
                    System.out.println("Collision -> new slot");
                    try {
                        nextSlot = getRandomFreeSlot();
					} catch (Exception e) {
						e.printStackTrace();
					}
                }

                if(sendSlot == slotCount) {
                    System.out.println(String.format("Send by %s", Thread.currentThread().getName()));
                    sendPackage();
                    return;
                }

                waitForNextSlot(slotCount);
            }

        }
    }

    private void sendPackage() {
        try {
            Package pack = createpackage();
            DatagramPacket packet = new DatagramPacket(new byte[0], 0, socketAddress);
            packet.setData(pack.getByteArray());
            lock.lock();
            socket.send(packet);
            lock.unlock();

            sendSlot = nextSlot;
            nextSlot = getRandomFreeSlot();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void waitForNextSlot(int currentSlot) {
        //                  SlotTime                    -                   Time that is already passed
        long waitTime = (FRAME_LENGTH_MS / SLOT_AMOUNT) - (Station.getTime(timeOffset) % (FRAME_LENGTH_MS / SLOT_AMOUNT));

        // if no time is passed we would wait 0 ms ... that should not happen ;)
        if(waitTime <= 0) {
            waitTime += (FRAME_LENGTH_MS / SLOT_AMOUNT);
        }

        System.out.println(String.format("[SLOT %d] It is %d and I wait for %d ms", currentSlot, Station.getTime(timeOffset), waitTime));

        try {
            sleep(waitTime);
        } catch (InterruptedException e) {
            System.err.println("Station interrupt.");
            interrupt();
        }
    }

    private void waitForNextFrame() throws Exception {
        //                  FrameTime   -           Time passed
        long waitTime = FRAME_LENGTH_MS - (Station.getTime(timeOffset) % FRAME_LENGTH_MS);

        try {
            sleep(waitTime);
        } catch (InterruptedException e) {
            System.err.println("Station interrupt.");
            interrupt();
        }

        resetFrame();

        // jump to middle of slot
        waitTime = (FRAME_LENGTH_MS / SLOT_AMOUNT / 2);

        try {
            sleep(waitTime);
        } catch (InterruptedException e) {
            System.err.println("Station interrupt.");
            interrupt();
        }
    }

    public void resetFrame() throws Exception {
        // clear reserved slots
        reservedSlots.clear();
    }

    public void addReservedSlot(int slotNo) {
        reservedSlots.add(slotNo);
    }

    public void syncTime(long offset) {
        timeOffset = (long)(offset + timeOffset) / 2;
    }

    public void setOffset(int offset) {
        this.timeOffset = offset;
    }

    private Package createpackage() throws Exception {
        Package pack = new Package();

        // might be too large but the right amount will be send because of length check in Package class
        String finalPayload = reader.getDatasourceString();

        pack.setStaticClass(stationClass);
        pack.setPayload(finalPayload);
        pack.setNextSlotNo(nextSlot);
        pack.setTimestamp(Station.getTime(timeOffset));

        return pack;
    }

    private int getRandomFreeSlot() throws Exception {
        Set slots = new HashSet<Integer>();
        for(int i = 1; i <= SLOT_AMOUNT ; i++) {
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
