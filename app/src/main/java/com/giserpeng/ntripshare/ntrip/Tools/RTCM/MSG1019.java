package com.giserpeng.ntripshare.ntrip.Tools.RTCM;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MSG1019 extends RTCM {
    private static final BigDecimal PI = new BigDecimal(Math.PI);
    private int messageNumber;
    private int SatelliteID;
    private int WeekNumber;
    private int SV_ACCURACY;
    private int CODE_ON_L2;
    private BigDecimal IDOT;
    private int IODE;
    private int toc;
    private BigDecimal af2;
    private BigDecimal af1;
    private BigDecimal af0;
    private int IODC;
    private BigDecimal Crs;
    private BigDecimal DELTAn;
    private BigDecimal M0;
    private BigDecimal Cuc;
    private BigDecimal e;
    private BigDecimal Cus;
    private BigDecimal sqA; //(A)^1/2
    private BigDecimal toe;
    private BigDecimal Cic;
    private BigDecimal OMEGA0;
    private BigDecimal Cis;
    private BigDecimal i0;
    private BigDecimal Crc;
    private BigDecimal Argument_of_Perigee;
    private BigDecimal OMEGADOT; //Rate of Right Ascension
    private BigDecimal tGD;
    private int SV_HEALTH;
    private int L2_P_data_flag;
    private int Fit_Interval;

    public MSG1019(byte[] msg) {
        super.rawMsg = msg;
        super.setToBinaryBuffer(msg);

        messageNumber = toUnsignedInt(getBits(16, 12));
        SatelliteID = toUnsignedInt(getBits(28, 6));
        WeekNumber = toUnsignedInt(getBits(34, 10));
        SV_ACCURACY = toUnsignedInt(getBits(44, 4));
        CODE_ON_L2 = toUnsignedInt(getBits(48, 2));
        setIDOT(toSignedInt(getBits(50, 14)));
        IODE = toUnsignedInt(getBits(64, 8));
        toc = toUnsignedInt(getBits(72, 16));
        setAf2(toSignedInt(getBits(88, 8)));
        setAf1(toSignedInt(getBits(96, 16)));
        setAf0(toSignedInt(getBits(112, 22)));
        IODC = toUnsignedInt(getBits(134, 10));
        setCrs(toSignedInt(getBits(144, 16)));
        setDELTAn(toSignedInt(getBits(160, 16)));
        setM0(toSignedInt(getBits(176, 32)));
        setCuc(toSignedInt(getBits(208, 16)));
        setE(toUnsignedLong(getBits(224, 32)));
        setCus(toSignedInt(getBits(256, 16)));
        setSqA(toUnsignedLong(getBits(272, 32))); //(A)^1/2
        setToe(toUnsignedInt(getBits(304, 16)));
        setCic(toSignedInt(getBits(320, 16)));
        setOMEGA0(toSignedInt(getBits(336, 32)));
        setCis(toSignedInt(getBits(368, 16)));
        setI0(toSignedInt(getBits(384, 32)));
        setCrc(toSignedInt(getBits(416, 16)));
        setArgument_of_Perigee(toSignedInt(getBits(432, 32)));
        setOMEGADOT(toSignedInt(getBits(464, 24))); //Rate of Right Ascension
        settGD(toSignedInt(getBits(488, 8)));
        SV_HEALTH = toUnsignedInt(getBits(496, 6));
        L2_P_data_flag = toUnsignedInt(getBits(502, 1));
        Fit_Interval = toUnsignedInt(getBits(503, 1));
    }

    @Override
    public String toString() {
        return "MSG1019{" +
                "messageNumber=" + messageNumber + "\n" +
                ", SatelliteID=" + SatelliteID + "\n" +
                ", WeekNumber=" + WeekNumber + "\n" +
                ", SV_ACCURACY=" + SV_ACCURACY + "\n" +
                ", CODE_ON_L2=" + CODE_ON_L2 + "\n" +
                ", IDOT=" + IDOT + "\n" +
                ", IODE=" + IODE + "\n" +
                ", toc=" + toc + "\n" +
                ", af2=" + af2 + "\n" +
                ", af1=" + af1 + "\n" +
                ", af0=" + af0 + "\n" +
                ", IODC=" + IODC + "\n" +
                ", Crs=" + Crs + "\n" +
                ", DELTAn=" + DELTAn + "\n" +
                ", M0=" + M0 + "\n" +
                ", Cuc=" + Cuc + "\n" +
                ", e=" + e + "\n" +
                ", Cus=" + Cus + "\n" +
                ", sqA=" + sqA + "\n" +
                ", toe=" + toe + "\n" +
                ", Cic=" + Cic + "\n" +
                ", OMEGA0=" + OMEGA0 + "\n" +
                ", Cis=" + Cis + "\n" +
                ", i0=" + i0 + "\n" +
                ", Crc=" + Crc + "\n" +
                ", Argument_of_Perigee=" + Argument_of_Perigee + "\n" +
                ", OMEGADOT=" + OMEGADOT + "\n" +
                ", tGD=" + tGD + "\n" +
                ", SV_HEALTH=" + SV_HEALTH + "\n" +
                ", L2_P_data_flag=" + L2_P_data_flag + "\n" +
                ", Fit_Interval=" + Fit_Interval + "\n" +
                '}';
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getSatelliteID() {
        return SatelliteID;
    }

    public int getWeekNumber() {
        return WeekNumber;
    }

    public int getSV_ACCURACY() {
        return SV_ACCURACY;
    }

    public int getCODE_ON_L2() {
        return CODE_ON_L2;
    }

    public BigDecimal getIDOT() {
        return IDOT;
    }

    public int getIODE() {
        return IODE;
    }

    public int getToc() {
        return toc;
    }

    public BigDecimal getAf2() {
        return af2;
    }

    public BigDecimal getAf1() {
        return af1;
    }

    public BigDecimal getAf0() {
        return af0;
    }

    public int getIODC() {
        return IODC;
    }

    public BigDecimal getCrs() {
        return Crs;
    }

    public BigDecimal getDELTAn() {
        return DELTAn;
    }

    public BigDecimal getM0() {
        return M0;
    }

    public BigDecimal getCuc() {
        return Cuc;
    }

    public BigDecimal getE() {
        return e;
    }

    public BigDecimal getCus() {
        return Cus;
    }

    public BigDecimal getSqA() {
        return sqA;
    }

    public BigDecimal getToe() {
        return toe;
    }

    public BigDecimal getCic() {
        return Cic;
    }

    public BigDecimal getOMEGA0() {
        return OMEGA0;
    }

    public BigDecimal getCis() {
        return Cis;
    }

    public BigDecimal getI0() {
        return i0;
    }

    public BigDecimal getCrc() {
        return Crc;
    }

    public BigDecimal getArgument_of_Perigee() {
        return Argument_of_Perigee;
    }

    public BigDecimal getOMEGADOT() {
        return OMEGADOT;
    }

    public BigDecimal gettGD() {
        return tGD;
    }

    public int getSV_HEALTH() {
        return SV_HEALTH;
    }

    public int getL2_P_data_flag() {
        return L2_P_data_flag;
    }

    public int getFit_Interval() {
        return Fit_Interval;
    }

    public void setMessageNumber(int messageNumber) {
        this.messageNumber = messageNumber;
    }

    public void setSatelliteID(int satelliteID) {
        SatelliteID = satelliteID;
    }

    public void setWeekNumber(int weekNumber) {
        WeekNumber = weekNumber;
    }

    public void setSV_ACCURACY(int SV_ACCURACY) {
        this.SV_ACCURACY = SV_ACCURACY;
    }

    public void setCODE_ON_L2(int CODE_ON_L2) {
        this.CODE_ON_L2 = CODE_ON_L2;
    }

    public void setIDOT(int IDOT) {
        this.IDOT = new BigDecimal(IDOT).divide(new BigDecimal(2).pow(43)).multiply(PI).setScale(12, RoundingMode.HALF_EVEN);
    }

    public void setIODE(int IODE) {
        this.IODE = IODE;
    }

    public void setToc(int toc) {
        this.toc = toc;
    }

    public void setAf2(int af2) {
        this.af2 = new BigDecimal(af2).divide(new BigDecimal(2).pow(55));
    }

    public void setAf1(int af1) {
        this.af1 = new BigDecimal(af1).divide(new BigDecimal(2).pow(43));
    }

    public void setAf0(int af0) {//.multiply(new BigDecimal("2e-31"));
        this.af0 = new BigDecimal(af0).divide(new BigDecimal(2).pow(31));
    }

    public void setIODC(int IODC) {
        this.IODC = IODC;
    }

    public void setCrs(int crs) {
        Crs = new BigDecimal(crs).divide(new BigDecimal(2).pow(5));
    }

    public void setDELTAn(int DELTAn) {
        this.DELTAn = new BigDecimal(DELTAn).divide(new BigDecimal(2).pow(43)).multiply(PI).setScale(12, RoundingMode.HALF_EVEN);
    }

    public void setM0(int m0) {
        M0 = new BigDecimal(m0).divide(new BigDecimal(2).pow(31)).multiply(PI).setScale(12, RoundingMode.HALF_EVEN);
    }

    public void setCuc(int cuc) {
        Cuc = new BigDecimal(cuc).divide(new BigDecimal(2).pow(29));
    }

    public void setE(long e) {
        this.e = new BigDecimal(e).divide(new BigDecimal(2).pow(33));
    }

    public void setCus(int cus) {
        Cus = new BigDecimal(cus).divide(new BigDecimal(2).pow(29));
    }

    public void setSqA(long sqA) {
        this.sqA = new BigDecimal(sqA).divide(new BigDecimal(2).pow(19));
    }

    public void setToe(int toe) {
        this.toe = new BigDecimal(toe).multiply(new BigDecimal(16));
    }

    public void setCic(int cic) {
        Cic = new BigDecimal(cic).divide(new BigDecimal(2).pow(29));
    }

    public void setOMEGA0(int OMEGA0) {
        this.OMEGA0 = new BigDecimal(OMEGA0).divide(new BigDecimal(2).pow(31)).multiply(PI).setScale(12, RoundingMode.HALF_EVEN);
    }

    public void setCis(int cis) {
        Cis = new BigDecimal(cis).divide(new BigDecimal(2).pow(29));
    }

    public void setI0(int i0) {
        this.i0 = new BigDecimal(i0).divide(new BigDecimal(2).pow(31)).multiply(PI).setScale(12, RoundingMode.HALF_EVEN);
    }

    public void setCrc(int crc) {
        Crc = new BigDecimal(crc).divide(new BigDecimal(32));
    }

    public void setArgument_of_Perigee(int argument_of_Perigee) {
        Argument_of_Perigee = new BigDecimal(argument_of_Perigee).divide(new BigDecimal(2).pow(31));
    }

    public void setOMEGADOT(int OMEGADOT) {
        this.OMEGADOT = new BigDecimal(OMEGADOT).divide(new BigDecimal(2).pow(43)).multiply(PI).setScale(12, RoundingMode.HALF_EVEN);
    }

    public void settGD(int tGD) {
        this.tGD = new BigDecimal(tGD).divide(new BigDecimal(2).pow(31));
    }

    public void setSV_HEALTH(int SV_HEALTH) {
        this.SV_HEALTH = SV_HEALTH;
    }

    public void setL2_P_data_flag(int l2_P_data_flag) {
        L2_P_data_flag = l2_P_data_flag;
    }

    public void setFit_Interval(int fit_Interval) {
        Fit_Interval = fit_Interval;
    }
}