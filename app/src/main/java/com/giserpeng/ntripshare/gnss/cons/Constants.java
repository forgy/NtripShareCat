package com.giserpeng.ntripshare.gnss.cons;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-11 15:44
 */
public class Constants {

    public static final String GPGGA = "$GPGGA";
    public static final String GNGGA = "$GNGGA";

    public static final String GPGSA = "$GPGSA";
    public static final String GNGSA = "$GNGSA";

    public static final String GPGST = "$GPGST";
    public static final String GNGST = "$GNGST";

    public static final String GPRMC = "$GPRMC";
    public static final String GNRMC = "$GNRMC";

    public static final String GPGSV = "$GPGSV";
    public static final String GLGSV = "$GLGSV";
    public static final String GAGSV = "$GAGSV";
    public static final String BDGSV = "$BDGSV";

    // 额外属性
    public static final String POWER = "$POWER";

    public static final String GPS = "GPS";
    public static final String GLO = "GLO";
    public static final String GAL = "GAL";
    public static final String BDS = "BDS";

    public static final int ARP_1005 = 1005;
    public static final int ARP_1006 = 1006;
    public static final int MSM4_GPS = 1074;
    public static final int MSM4_GLO = 1084;
    public static final int MSM4_GAL = 1094;
    public static final int MSM4_BDS = 1124;
}
