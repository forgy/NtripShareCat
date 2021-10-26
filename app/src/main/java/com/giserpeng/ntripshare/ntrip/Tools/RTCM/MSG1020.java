package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

public class MSG1020 extends RTCM {
    private int MessageNumber;
    private int SatelliteID;
    private int SatelliteChannel;
    private int almanacHealth; //almanac health (Cn word)
    private int almanacAvailabilityIndicator;
    private int P1;
    private int tk;
    private int MSB_of_Bn;
    private int P2;
    private int tb;
    private int xn_f; //first derivative
    private int xn;
    private int xn_s; //second derivative
    private int yn_f; //first derivative
    private int yn;
    private int yn_s; //second derivative
    private int zn_f; //first derivative
    private int zn;
    private int zn_s; //second derivative
    private int P3;
    private int Gamma_n;
    private int M_P;
    private int M_ln; // (third string)
    private int Tn;
    private int M_dTn;
    private int En;
    private int M_P4;
    private int M_FT;
    private int M_NT;
    private int M_M;
    private int Availability_Additional_Data;
    private int NA; // N^A
    private int Tc;
    private int M_N4;
    private int M_Tgps;
    private int M_ln_fi; // (fifth string)
    private int reserved;

    public MSG1020(byte[] msg) {
        MessageNumber = toUnsignedInt(getBits(16, 12));
        SatelliteID = toUnsignedInt(getBits(28, 6));
        SatelliteChannel = toUnsignedInt(getBits(34, 5));
        almanacHealth = toUnsignedInt(getBits(39, 1));
        almanacAvailabilityIndicator = toUnsignedInt(getBits(40, 1));
        P1 = toUnsignedInt(getBits(41, 2));
        tk = toUnsignedInt(getBits(43, 12));
        MSB_of_Bn = toUnsignedInt(getBits(55, 1));
        P2 = toUnsignedInt(getBits(56, 1));
        tb = toUnsignedInt(getBits(57, 7));
        xn_f = toIntS(getBits(64, 24));
        xn = toIntS(getBits(88, 27));
        xn_s = toIntS(getBits(115, 5));
        yn_f = toIntS(getBits(120, 24));
        yn = toIntS(getBits(144, 27));
        yn_s = toIntS(getBits(171, 5));
        zn_f = toIntS(getBits(176, 24));
        zn = toIntS(getBits(200, 27));
        zn_s = toIntS(getBits(227, 5));
        P3 = toUnsignedInt(getBits(232, 1));
        Gamma_n = toIntS(getBits(233, 11));
        M_P = toUnsignedInt(getBits(244, 2));
        M_ln = toUnsignedInt(getBits(246, 1));
        Tn = toIntS(getBits(247, 22));
        M_dTn = toIntS(getBits(269, 5));
        En = toUnsignedInt(getBits(274, 5));
        M_P4 = toUnsignedInt(getBits(279, 1));
        M_FT = toUnsignedInt(getBits(280, 4));
        M_NT = toUnsignedInt(getBits(284, 11));
        M_M = toUnsignedInt(getBits(295, 2));
        Availability_Additional_Data = toUnsignedInt(getBits(297, 1));
        NA = toUnsignedInt(getBits(298, 11));
        Tc = toIntS(getBits(309, 32));
        M_N4 = toUnsignedInt(getBits(341, 5));
        M_Tgps = toIntS(getBits(346, 22));
        M_ln_fi = toUnsignedInt(getBits(368, 1));
        reserved = toUnsignedInt(getBits(369, 7));
    }

    public int getMessageNumber() {
        return MessageNumber;
    }

    public int getSatelliteID() {
        return SatelliteID;
    }

    public int getSatelliteChannel() {
        return SatelliteChannel;
    }

    public int getAlmanacHealth() {
        return almanacHealth;
    }

    public int getAlmanacAvailabilityIndicator() {
        return almanacAvailabilityIndicator;
    }

    public int getP1() {
        return P1;
    }

    public int getTk() {
        return tk;
    }

    public int getMSB_of_Bn() {
        return MSB_of_Bn;
    }

    public int getP2() {
        return P2;
    }

    public int getTb() {
        return tb;
    }

    public int getXn_f() {
        return xn_f;
    }

    public int getXn() {
        return xn;
    }

    public int getXn_s() {
        return xn_s;
    }

    public int getYn_f() {
        return yn_f;
    }

    public int getYn() {
        return yn;
    }

    public int getYn_s() {
        return yn_s;
    }

    public int getZn_f() {
        return zn_f;
    }

    public int getZn() {
        return zn;
    }

    public int getZn_s() {
        return zn_s;
    }

    public int getP3() {
        return P3;
    }

    public int getGamma_n() {
        return Gamma_n;
    }

    public int getM_P() {
        return M_P;
    }

    public int getM_ln() {
        return M_ln;
    }

    public int getTn() {
        return Tn;
    }

    public int getM_dTn() {
        return M_dTn;
    }

    public int getEn() {
        return En;
    }

    public int getM_P4() {
        return M_P4;
    }

    public int getM_FT() {
        return M_FT;
    }

    public int getM_NT() {
        return M_NT;
    }

    public int getM_M() {
        return M_M;
    }

    public int getAvailability_Additional_Data() {
        return Availability_Additional_Data;
    }

    public int getNA() {
        return NA;
    }

    public int getTc() {
        return Tc;
    }

    public int getM_N4() {
        return M_N4;
    }

    public int getM_Tgps() {
        return M_Tgps;
    }

    public int getM_ln_fi() {
        return M_ln_fi;
    }

    public int getReserved() {
        return reserved;
    }
}
