package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MSG1001 extends RTCM {

    private int messageNumber;
    private int stationID;
    private double TOW;
    private boolean synchronous;
    private int signalsProcessed; //No. of GPS Satellite Signals Processed
    private boolean smoothingIndicator;
    private int smoothingInterval;
    private final double LightMilliSecond = 299792.458;
    String[] smoothing = new String[]{
            "No smoothing",
            "< 30 s",
            "30-60 s",
            "1-2 min",
            "2-4 min",
            "4-8 min",
            ">8 min",
            "Unlimited smoothing interval"
    };

    Sat1001[] listSatellites;

    public Sat1001[] getListSatellites() {
        return listSatellites;
    }

    public MSG1001(byte[] msg) {

        super.rawMsg = msg;

        super.setToBinaryBuffer(msg);

        messageNumber = Integer.parseUnsignedInt(binaryBuffer.substring(16, 28), 2);//1005 1006
        stationID = Integer.parseUnsignedInt(binaryBuffer.substring(28, 40), 2);
        TOW = toUnsignedInt(binaryBuffer.substring(40, 70)) / 1000.0d;
        synchronous = binaryBuffer.charAt(70) == RTCM.BIT1;
        signalsProcessed = toUnsignedInt(binaryBuffer.substring(71, 76));
        smoothingIndicator = binaryBuffer.charAt(76) == RTCM.BIT1;
        smoothingInterval = toUnsignedInt(binaryBuffer.substring(77, 80));

        listSatellites = new Sat1001[signalsProcessed];

        for (int i = 0; i < signalsProcessed; i++) {
            int shift = i * 58;

            Sat1001 s = new Sat1001();

            s.setID(toUnsignedInt(getBits(80 + shift, 6)));
            s.setCodeL1(toUnsignedInt(getBits(86 + shift, 1)));
            s.setL1Psr(toUnsignedInt(getBits(87 + shift, 24)));
            s.setL1Phr_L1Psr(toSignedInt(getBits(111 + shift, 20)));
            s.setLockL1(toUnsignedInt(getBits(131 + shift, 7)));

            listSatellites[i] = s;
        }
    }

    public class Sat1001 {
        //Psr - PseudoRange
        //Phr - PhaseRange

        String[] L1Indicator = new String[]{
                "C/A Code",
                "P(Y) Code Direct"
        };

        String[] L2Indicator = new String[]{
                "C/A or L2C code",
                "P(Y) code direct",
                "P(Y) code cross-correlated",
                "Correlated P/Y"
        };

        private int ID;
        private int CodeL1;
        private int L1Psr;
        private int L1Phr_L1Psr;
        private int L1Phr;
        private int LockL1;


        @Override
        public String toString() {

            String response = "";
            response += customFormat("##", ID) + "\t|\t";
            response += L1Indicator[CodeL1] + "\t|\t";
            //response += L1Psr + "\t|\t";
            response += new BigDecimal(L1Phr_L1Psr / 2000d).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";
            response += LockL1 + "\t|\t";

            return response;
        }

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public int getCodeL1() {
            return CodeL1;
        }

        public void setCodeL1(int codeL1) {
            CodeL1 = codeL1;
        }

        public int getL1Psr() {
            return L1Psr;
        }

        public void setL1Psr(int l1Psr) {
            L1Psr = l1Psr;
        }

        public int getL1Phr_L1Psr() {
            return L1Phr_L1Psr;
        }

        public void setL1Phr_L1Psr(int l1Phr_L1Psr) {
            this.L1Phr_L1Psr = l1Phr_L1Psr;
        }

        public int getLockL1() {
            return LockL1;
        }

        public void setLockL1(int lockL1) {
            LockL1 = lockL1;
        }
    }
}
