package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MSG1012 extends RTCM {

    private int messageNumber;
    private int stationID;
    private double epochTime;
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

    Sat1012[] listSatellites;

    public Sat1012[] getListSatellites() {
        return listSatellites;
    }

    public MSG1012(byte[] msg) {

        super.rawMsg = msg;

        super.setToBinaryBuffer(msg);

        messageNumber = toUnsignedInt(getBits(16, 12));
        stationID = toUnsignedInt(getBits(28, 12));
        epochTime = toUnsignedInt(binaryBuffer.substring(40, 27)) / 1000.0d;
        synchronous = binaryBuffer.charAt(67) == RTCM.BIT1;
        signalsProcessed = toUnsignedInt(binaryBuffer.substring(68, 5));
        smoothingIndicator = binaryBuffer.charAt(73) == RTCM.BIT1;
        smoothingInterval = toUnsignedInt(binaryBuffer.substring(74, 3));

        listSatellites = new Sat1012[signalsProcessed];

        for (int i = 0; i < signalsProcessed; i++) {
            int shift = i * 130;

            Sat1012 s = new Sat1012();

            s.setID(toUnsignedInt(getBits(77 + shift, 6)));
            s.setCodeL1(toUnsignedInt(getBits(83 + shift, 1)));
            s.setChannelNumber(toUnsignedInt(getBits(84,5)));
            s.setL1Psr(toUnsignedInt(getBits(89 + shift, 25)));
            s.setL1Phr_L1Psr(toSignedInt(getBits(114 + shift, 20)));
            s.setLockL1(toUnsignedInt(getBits(134 + shift, 7)));
            s.setAmbL1(toUnsignedInt(getBits(141 + shift, 7)));
            s.setSNRL1(toUnsignedInt(getBits(148 + shift, 8)));
            s.setCodeL2(toUnsignedInt(getBits(156 + shift, 2)));
            s.setL2Psr_L1Psr(toSignedInt(getBits(158 + shift, 14)));
            s.setL2Phr_L1Psr(toSignedInt(getBits(172 + shift, 20)));
            s.setLockL2(toUnsignedInt(getBits(192 + shift, 7)));
            s.setSNRL2(toUnsignedInt(getBits(199 + shift, 8)));

            listSatellites[i] = s;
        }
    }

    public class Sat1012 {
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
        private int ChannelNumber;
        private int L1Psr;
        private int L1Phr_L1Psr;
        private int LockL1;
        private int AmbL1;
        private int SNRL1;
        private int CodeL2;
        private int L2Psr_L1Psr;
        private int L2Phr_L1Psr;
        private int LockL2;
        private int SNRL2;

        @Override
        public String toString() {

            String response = "";
            response += customFormat("##", ID) + "\t|\t";
            response += L1Indicator[CodeL1] + "\t|\t";
            //response += L1Psr + "\t|\t";
            response += new BigDecimal(L1Phr_L1Psr / 2000d).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";
            response += LockL1 + "\t|\t";
            response += new BigDecimal(AmbL1 * LightMilliSecond).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";
            response += new BigDecimal(SNRL1 / 4.0d).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";
            response += L2Indicator[CodeL2] + "\t|\t";
            response += new BigDecimal(L2Psr_L1Psr * 0.02d).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";
            response += new BigDecimal(L2Phr_L1Psr / 2000.0d).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";
            response += LockL2 + "\t|\t";
            response += new BigDecimal(SNRL2 / 4.0d).setScale(2, RoundingMode.HALF_EVEN) + "\t|\t";

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

        public int getAmbL1() {
            return AmbL1;
        }

        public void setAmbL1(int ambL1) {
            AmbL1 = ambL1;
        }

        public int getSNRL1() {
            return SNRL1;
        }

        public void setSNRL1(int SNRL1) {
            this.SNRL1 = SNRL1;
        }

        public int getCodeL2() {
            return CodeL2;
        }

        public void setCodeL2(int codeL2) {
            CodeL2 = codeL2;
        }

        public int getL2Psr_L1Psr() {
            return L2Psr_L1Psr;
        }

        public void setL2Psr_L1Psr(int l2Psr_L1Psr) {
            this.L2Psr_L1Psr = l2Psr_L1Psr;
        }

        public int getL2Phr_L1Psr() {
            return L2Phr_L1Psr;
        }

        public void setL2Phr_L1Psr(int l2Phr_L1Psr) {
            this.L2Phr_L1Psr = l2Phr_L1Psr;
        }

        public int getLockL2() {
            return LockL2;
        }

        public void setLockL2(int lockL2) {
            LockL2 = lockL2;
        }

        public int getSNRL2() {
            return SNRL2;
        }

        public void setSNRL2(int SNRL2) {
            this.SNRL2 = SNRL2;
        }

        public int getChannelNumber() {
            return ChannelNumber;
        }

        public void setChannelNumber(int channelNumber) {
            ChannelNumber = channelNumber;
        }
    }
}
