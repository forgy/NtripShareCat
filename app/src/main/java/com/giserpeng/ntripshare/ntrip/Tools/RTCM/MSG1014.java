package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

public class MSG1014 extends RTCM {
    private int messageNumber;
    private int NetworkID;
    private int SubnetworkID;
    private int NumberAuxiliaryStationsTransmitted;
    private int MasterStation;
    private int AuxiliaryReferenceStationID;
    private int AuxMasterDeltaLatitude;
    private int AuxMasterDeltaLongitude;
    private int AuxMasterDeltaHeight;

    public MSG1014(byte[] msg) {
        super.rawMsg = msg;
        super.setToBinaryBuffer(msg);

        messageNumber = toUnsignedInt(getBits(16, 12));
        NetworkID = toUnsignedInt(getBits(28, 8));
        SubnetworkID = toUnsignedInt(getBits(36, 4));
        NumberAuxiliaryStationsTransmitted = toUnsignedInt(getBits(40,5));
        MasterStation = toUnsignedInt(getBits(45, 12));
        AuxiliaryReferenceStationID = toUnsignedInt(getBits(57, 12));
        AuxMasterDeltaLatitude = toSignedInt(getBits(69, 20));
        AuxMasterDeltaLongitude = toSignedInt(getBits(89, 21));
        AuxMasterDeltaHeight = toSignedInt(getBits(110, 23));
    }
}
