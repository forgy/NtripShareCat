package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MSG1004 extends RTCM {

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

    Sat1004[] listSatellites;

    public Sat1004[] getListSatellites() {
        return listSatellites;
    }

    public MSG1004(byte[] msg) {

        super.rawMsg = msg;

        super.setToBinaryBuffer(msg);

        messageNumber = Integer.parseUnsignedInt(binaryBuffer.substring(16, 28), 2);//1005 1006
        stationID = Integer.parseUnsignedInt(binaryBuffer.substring(28, 40), 2);
        TOW = toUnsignedInt(binaryBuffer.substring(40, 70)) / 1000.0d;
        synchronous = binaryBuffer.charAt(70) == RTCM.BIT1;
        signalsProcessed = toUnsignedInt(binaryBuffer.substring(71, 76));
        smoothingIndicator = binaryBuffer.charAt(76) == RTCM.BIT1;
        smoothingInterval = toUnsignedInt(binaryBuffer.substring(77, 80));

        listSatellites = new Sat1004[signalsProcessed];

        for (int i = 0; i < signalsProcessed; i++) {
            int shift = i * 125;

            Sat1004 s = new Sat1004();

            s.setID(toUnsignedInt(getBits(80 + shift, 6)));
            s.setCodeL1(toUnsignedInt(getBits(86 + shift, 1)));
            s.setL1Psr(toUnsignedInt(getBits(87 + shift, 24)));
            s.setL1Phr_L1Psr(toSignedInt(getBits(111 + shift, 20)));
            s.setLockL1(toUnsignedInt(getBits(131 + shift, 7)));
            s.setAmbL1(toUnsignedInt(getBits(138 + shift, 8)));
            s.setSNRL1(toUnsignedInt(getBits(146 + shift, 8)));
            s.setCodeL2(toUnsignedInt(getBits(154 + shift, 2)));
            s.setL2Psr_L1Psr(toSignedInt(getBits(156 + shift, 14)));
            s.setL2Phr_L1Psr(toSignedInt(getBits(170 + shift, 20)));
            s.setLockL2(toUnsignedInt(getBits(190 + shift, 7)));
            s.setSNRL2(toUnsignedInt(getBits(197 + shift, 8)));

            listSatellites[i] = s;
        }
    }

    public class Sat1004 {
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
        private BigDecimal L1Psr;
        private BigDecimal L1Phr_L1Psr;
        private int LockL1;
        private BigDecimal AmbL1;
        private double SNRL1;
        private int CodeL2;
        private BigDecimal L2Psr_L1Psr;
        private BigDecimal L2Phr_L1Psr;
        private int LockL2;
        private double SNRL2;

        @Override
        public String toString() {

            String response = "";
            response += customFormat("##", ID) + "\t|\t";
            response += L1Indicator[CodeL1] + "\t|\t";
            response += L1Psr + "\t|\t";
            response += L1Phr_L1Psr + "\t|\t";
            response += LockL1 + "\t|\t";
            response += AmbL1 + "\t|\t";
            response += SNRL1 + "\t|\t";
            response += L2Indicator[CodeL2] + "\t|\t";
            response += L2Psr_L1Psr + "\t|\t";
            response += L2Phr_L1Psr + "\t|\t";
            response += LockL2 + "\t|\t";
            response += SNRL2 + "\t|\t";

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

        public BigDecimal getL1Psr() {
            return L1Psr;
        }

        public void setL1Psr(int l1Psr) {
            L1Psr = new BigDecimal(l1Psr * 0.02d).setScale(3, RoundingMode.HALF_EVEN);
        }

        public BigDecimal getL1Phr_L1Psr() {
            return L1Phr_L1Psr;
        }

        public void setL1Phr_L1Psr(int l1Phr_L1Psr) {
            L1Phr_L1Psr = new BigDecimal(l1Phr_L1Psr * 0.0005).setScale(3, RoundingMode.HALF_EVEN);
        }

        public int getLockL1() {
            return LockL1;
        }

        public void setLockL1(int lockL1) {
            LockL1 = lockL1;
        }

        public BigDecimal getAmbL1() {
            return AmbL1;
        }

        public void setAmbL1(int ambL1) {
            AmbL1 = new BigDecimal(ambL1 * 299792.458d).setScale(3, RoundingMode.HALF_EVEN);
        }

        public double getSNRL1() {
            return SNRL1;
        }

        public void setSNRL1(int SNRL1) {
            this.SNRL1 = SNRL1 * 0.25d;
        }

        public int getCodeL2() {
            return CodeL2;
        }

        public void setCodeL2(int codeL2) {
            CodeL2 = codeL2;
        }

        public BigDecimal getL2Psr_L1Psr() {
            return L2Psr_L1Psr;
        }

        public void setL2Psr_L1Psr(int l2Psr_L1Psr) {
            L2Psr_L1Psr = new BigDecimal(l2Psr_L1Psr * 0.02d).setScale(3, RoundingMode.HALF_EVEN);
        }

        public BigDecimal getL2Phr_L1Psr() {
            return L2Phr_L1Psr;
        }

        public void setL2Phr_L1Psr(int l2Phr_L1Psr) {
            L2Phr_L1Psr = new BigDecimal(l2Phr_L1Psr * 0.0005d).setScale(3, RoundingMode.HALF_EVEN);
        }

        public int getLockL2() {
            return LockL2;
        }

        public void setLockL2(int lockL2) {
            LockL2 = lockL2;
        }

        public double getSNRL2() {
            return SNRL2;
        }

        public void setSNRL2(int SNRL2) {
            this.SNRL2 = SNRL2 * 0.25d;
        }
    }
}
