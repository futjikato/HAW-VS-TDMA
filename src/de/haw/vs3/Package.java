package de.haw.vs3;

import java.nio.ByteBuffer;

/**
 * @author moritzspindelhirn
 * @todo Documentation
 * @category de.haw.vs3
 */
public final class Package {

    private char staticClass;

    private String payload;

    private int nextSlotNo;

    private long timestamp;

    public static Package createFromByteArray(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);

        Package newPackage = new Package();

        newPackage.staticClass = bb.getChar();

        byte[] payloadBuffer = new byte[23];
        bb.get(payloadBuffer);
        newPackage.payload = new String(payloadBuffer);

        newPackage.nextSlotNo = bb.get();

        newPackage.timestamp = bb.getLong();

        return newPackage;
    }

    public byte[] getByteArray() {
        ByteBuffer bb = ByteBuffer.allocate(34);

        bb.put((byte)staticClass);

        byte[] payloadBytes = new byte[24];
        byte[] payloadStrBytes = payload.getBytes();
        System.arraycopy(payloadStrBytes, 0, payloadBytes, 0, 24);
        bb.put(payloadBytes);

        bb.put((byte) nextSlotNo);

        bb.putLong(timestamp);

        return bb.array();
    }

    public char getStaticClass() {
        return staticClass;
    }

    public void setStaticClass(char staticClass) {
        this.staticClass = staticClass;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getNextSlotNo() {
        return nextSlotNo;
    }

    public void setNextSlotNo(int nextSlotNo) {
        this.nextSlotNo = nextSlotNo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
