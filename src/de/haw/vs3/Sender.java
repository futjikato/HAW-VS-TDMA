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

    private InetAddress address;

    private int port;

    private SocketAddress socketAddress;

    private MulticastSocket socket;

    private char stationClass;

    private Set<Integer> reservedSlots;

    private Random rng;

    private ReentrantLock lock;

    private int teamNo;

    private int stationNo;

    private Reader reader;

    private int nextSlot;
    private int timeOffset;
    private int sendSlot;
    private int currentSlot;

    public Sender(SocketAddress sockAddress, MulticastSocket socket, char stationClass) throws IOException {
        this.socket = socket;
        this.lock = new ReentrantLock();
        this.socket.setTimeToLive(1);

        this.stationClass = stationClass;
        // this.address = address;
        // this.port = port;
        this.socketAddress = sockAddress;

        this.reservedSlots = new CopyOnWriteArraySet<Integer>();
        this.rng = new Random();
    }

    @Override
	public void run() {
        try {
            waitForNextFrame();
            sendSlot = getRandomFreeSlot();
            while(!isInterrupted()) {

                SlotThread st = new SlotThread(this, SLOT_AMOUNT);
                st.start();

                waitForNextFrame();

                /*if(currentSlot == SLOT_AMOUNT) {
                    System.out.println("New Frame");
                    resetFrame();
                }*/

                /*if(reservedSlots.contains(sendSlot)) {
                    System.out.println("Collision -> new slot");
                    sendSlot = getRandomFreeSlot();
                }

                if(sendSlot == currentSlot) {
                    System.out.println("Send");
                    sendPackage();
                }*/


//                waitForNextSlot();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Station out.");
        }
    }

    private class SlotThread extends Thread{
        private Sender sender;
        private int slotAmount;

        public SlotThread(Sender sender, int numberOfSlots){
            this.sender = sender;
            this.slotAmount = numberOfSlots;
        }

        @Override
		public void run(){
            for (int slotCount = 0; slotCount < slotAmount; slotCount++){
                if(reservedSlots.contains(sendSlot)) {
                    System.out.println("Collision -> new slot");
                    try {
						sendSlot = getRandomFreeSlot();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }

                if(sendSlot == currentSlot) {
                    System.out.println("Send");
                    sendPackage();
                }

                waitForNextSlot();
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void waitForNextSlot() {
        //                  SlotTime                    -                   Time that is already passed
        long waitTime = (FRAME_LENGTH_MS / SLOT_AMOUNT) - (Station.getTime(timeOffset) % (FRAME_LENGTH_MS / SLOT_AMOUNT));
        waitTime -= (FRAME_LENGTH_MS / SLOT_AMOUNT / 2);

        // if no time is passed we would wait 0 ms ... that should not happen ;)
        if(waitTime <= 0) {
            waitTime += (FRAME_LENGTH_MS / SLOT_AMOUNT);
        }

        System.out.println(String.format("[SLOT %d] It is %d and I wait for %d ms", currentSlot, Station.getTime(timeOffset), waitTime));

        currentSlot++;

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
        // jump to middle of slot
        waitTime += (FRAME_LENGTH_MS / SLOT_AMOUNT / 2);

        System.out.println(String.format("[FRAME] It is %d and I wait for %d ms", Station.getTime(timeOffset), waitTime));

        resetFrame();

        try {
            sleep(waitTime);
        } catch (InterruptedException e) {
            System.err.println("Station interrupt.");
            interrupt();
        }
    }

    private void resetFrame() throws Exception {
        // random new next slot to reserve
        sendSlot = nextSlot;
        nextSlot = getRandomFreeSlot();

        // reset slot counter
        currentSlot = 0;

        // clear reserved slots
        reservedSlots.clear();
    }

    public void addReservedSlot(int slotNo) {
        reservedSlots.add(slotNo);
    }

    public void syncTime(int offset) {
        System.out.println(String.format("Synctime : timeOffset = (%d - %d) / 2 ( => %d )", offset, timeOffset, (offset - timeOffset) / 2));
        timeOffset = (offset - timeOffset) / 2;
    }

    public void setOffset(int offset) {
        this.timeOffset = offset;
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
        String payload = String.format("team %d-%d", teamNo, stationNo);
        String dsStr = reader.getDatasourceString();
        String finalPayload = payload.concat(dsStr);

        // get next free slot
        nextSlot = getRandomFreeSlot();

        pack.setNextSlotNo(nextSlot);
        pack.setPayload(finalPayload);
        pack.setStaticClass(stationClass);
        pack.setTimestamp(Station.getTime(timeOffset));

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
