package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

public class MSG1008 extends RTCM {

    private int antennaID;
    private int messageNumber;
    private int stationID;
    private String antennaDescriptor;
    private String serialNumber;

    public MSG1008(byte[] msg) {
        super.rawMsg = msg;

        super.setToBinaryBuffer(msg);

        messageNumber = toUnsignedInt(getBits(16, 12));
        stationID = toUnsignedInt(getBits(28, 12));
        int descriptorCounter = toUnsignedInt(getBits(40, 8));

        /* antennaDescriptor */
        antennaDescriptor = "";
        for (int i = 0; i < descriptorCounter; i++) {
            antennaDescriptor += (char) toUnsignedInt(getBits(48 + (i * 8), 8));
        }
        /* antennaDescriptor */

        int pointer = 48 + (descriptorCounter * 8); //pointer to next
        antennaID = toUnsignedInt(getBits(pointer, 8));

        /* serialNumber */
        int serialNumberCounter = toUnsignedInt(getBits(pointer + 8, 8));
        serialNumber = "";
        for (int i = 0; i < serialNumberCounter; i++) {
            serialNumber += (char) toUnsignedInt(getBits((pointer + 16) + (i * 8), 8));
        }
        /* serialNumber */

    }

    public int getAntennaID() {
        return antennaID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getStationID() {
        return stationID;
    }

    public String getAntennaDescriptor() {
        return antennaDescriptor;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
