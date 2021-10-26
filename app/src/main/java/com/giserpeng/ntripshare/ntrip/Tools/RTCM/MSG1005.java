package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

public class MSG1005 extends RTCM {

    private int messageNumber;
    private int stationID;
    private int ITRFyear;
    private boolean GPS;
    private boolean GLONASS;
    private boolean Galileo;
    private boolean referenceStation;
    private double ECEFX;
    private boolean oscillator;
    private boolean reserverd;
    private double ECEFY;
    private int quarterCycle;
    private double ECEFZ;

    //byte array with preamble(0xD3)
    public MSG1005(byte[] msg) {
        rawMsg = msg;

        for (int i = 1; i < msg.length; i++) {
            binaryBuffer += toBinaryString(msg[i]);
        }

        messageNumber = Integer.parseUnsignedInt(binaryBuffer.substring(16, 28), 2);//1005 1006
        stationID = Integer.parseUnsignedInt(binaryBuffer.substring(28, 40), 2);
        ITRFyear = Integer.parseUnsignedInt(binaryBuffer.substring(40, 46), 2);
        GPS = binaryBuffer.charAt(46) == RTCM.BIT1;
        GLONASS = binaryBuffer.charAt(47) == RTCM.BIT1;
        Galileo = binaryBuffer.charAt(48) == RTCM.BIT1;
        referenceStation = binaryBuffer.charAt(49) == RTCM.BIT1;
        ECEFX = toSignedLong(binaryBuffer.substring(50, 88)) * 0.0001d;
        oscillator = binaryBuffer.charAt(88) == RTCM.BIT1;
        reserverd = binaryBuffer.charAt(89) == RTCM.BIT1;
        ECEFY = toSignedLong(binaryBuffer.substring(90, 128)) * 0.0001d;
        quarterCycle = Integer.parseUnsignedInt(binaryBuffer.substring(128, 130), 2);
        ECEFZ = toSignedLong(binaryBuffer.substring(130, 168)) * 0.0001d;

    }

    public void Write() {

    }

    @Override
    public String toString() {
        return String.format("MSG %s: ID:%s ITRF:%s X=%s Y=%s Z=%s AntH:%s", messageNumber, stationID, ITRFyear, ECEFX,
                ECEFY, ECEFZ);
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getStationID() {
        return stationID;
    }

    public int getITRFyear() {
        return ITRFyear;
    }

    public boolean isGPS() {
        return GPS;
    }

    public boolean isGLONASS() {
        return GLONASS;
    }

    public boolean isGalileo() {
        return Galileo;
    }

    public boolean isReferenceStation() {
        return referenceStation;
    }

    public double getECEFX() {
        return ECEFX;
    }

    public boolean isOscillator() {
        return oscillator;
    }

    public boolean isReserverd() {
        return reserverd;
    }

    public double getECEFY() {
        return ECEFY;
    }

    public int getQuarterCycle() {
        return quarterCycle;
    }

    public double getECEFZ() {
        return ECEFZ;
    }


}