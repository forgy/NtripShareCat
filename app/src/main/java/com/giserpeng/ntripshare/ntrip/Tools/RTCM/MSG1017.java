package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

public class MSG1017 extends RTCM {
    private int messageNumber;
    private int NetworkID;
    private int SubnetworkID;
    private int TOW; //GPS Epoch Time (GPS TOW)
    private boolean MultipleMessageIndicator;
    private int MasterStationID;
    private int AuxiliaryStationID;

    private GPS[] list;


    public MSG1017(byte[] msg) {
        super.rawMsg = msg;
        super.setToBinaryBuffer(msg);

        messageNumber = toUnsignedInt(getBits(16, 12));
        NetworkID = toUnsignedInt(getBits(28, 8));
        SubnetworkID = toUnsignedInt(getBits(36, 4));
        TOW = toUnsignedInt(getBits(40, 23));
        MultipleMessageIndicator = binaryBuffer.charAt(63) == BIT1;
        MasterStationID = toUnsignedInt(getBits(64, 12));
        AuxiliaryStationID = toUnsignedInt(getBits(76, 12));
        int gpsCounter = toUnsignedInt(getBits(88, 4));
        for (int i = 0; i < gpsCounter; i++) {
            int shift = i * 53;
            GPS g = new GPS();

            g.setID(toUnsignedInt(getBits(92, 6)));
            g.setAmbiguityStatusFlag(toUnsignedInt(getBits(98, 2)));
            g.setNonSyncCount(toUnsignedInt(getBits(100, 3)));
            g.setGeometricCarrierPhaseCorrectionDifference(toUnsignedInt(getBits(103, 17)));
            g.setIODE(toUnsignedInt(getBits(120, 8)));
            g.setIonosphericCarrierPhaseCorrectionDifference(toSignedInt(getBits(128, 17)));

        }
    }

    public class GPS {
        private int ID;
        private int AmbiguityStatusFlag;
        private int NonSyncCount;
        private int GeometricCarrierPhaseCorrectionDifference;
        private int IODE;
        private int IonosphericCarrierPhaseCorrectionDifference;

        public int getID() {
            return ID;
        }

        public void setID(int ID) {
            this.ID = ID;
        }

        public int getAmbiguityStatusFlag() {
            return AmbiguityStatusFlag;
        }

        public void setAmbiguityStatusFlag(int ambiguityStatusFlag) {
            AmbiguityStatusFlag = ambiguityStatusFlag;
        }

        public int getNonSyncCount() {
            return NonSyncCount;
        }

        public void setNonSyncCount(int nonSyncCount) {
            NonSyncCount = nonSyncCount;
        }

        public int getGeometricCarrierPhaseCorrectionDifference() {
            return GeometricCarrierPhaseCorrectionDifference;
        }

        public void setGeometricCarrierPhaseCorrectionDifference(int geometricCarrierPhaseCorrectionDifference) {
            GeometricCarrierPhaseCorrectionDifference = geometricCarrierPhaseCorrectionDifference;
        }

        public int getIODE() {
            return IODE;
        }

        public void setIODE(int IODE) {
            this.IODE = IODE;
        }

        public int getIonosphericCarrierPhaseCorrectionDifference() {
            return IonosphericCarrierPhaseCorrectionDifference;
        }

        public void setIonosphericCarrierPhaseCorrectionDifference(int ionosphericCarrierPhaseCorrectionDifference) {
            IonosphericCarrierPhaseCorrectionDifference = ionosphericCarrierPhaseCorrectionDifference;
        }
    }
}
