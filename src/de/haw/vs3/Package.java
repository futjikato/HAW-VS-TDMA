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

    private int staticNo;

    private long timestamp;

    public static Package createFromByteArray(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);

        Package newPackage = new Package();

        newPackage.staticClass = bb.getChar();

        byte[] payloadBuffer = new byte[23];
        bb.get(payloadBuffer);
        newPackage.payload = new String(payloadBuffer);

        newPackage.staticNo = bb.get();

        newPackage.timestamp = bb.getLong();

        return newPackage;
    }

    public byte[] getByteArray() {
        ByteBuffer bb = ByteBuffer.allocate(34);

        bb.putChar(staticClass);

        byte[] payloadBytes = new byte[23];
        byte[] payloadStrBytes = payload.getBytes();
        System.arraycopy(payloadStrBytes, 0, payloadBytes, 0, 23);
        bb.put(payloadBytes);

        bb.put((byte) staticNo);

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

    public int getStaticNo() {
        return staticNo;
    }

    public void setStaticNo(int staticNo) {
        this.staticNo = staticNo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
