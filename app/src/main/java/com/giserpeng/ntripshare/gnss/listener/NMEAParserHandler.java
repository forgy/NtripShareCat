package com.giserpeng.ntripshare.gnss.listener;

import java.util.Set;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-03-14 16:40
 */
public interface NMEAParserHandler {

    void onGGA(long time, double latitude, double longitude, float altitude, float height, int status, int satellites, float hdop);

    void onRMC(long time, long date, double latitude, double longitude, float speed, float azimuth);

    void onGST(long time, float latDev, float lonDev, float altDev);

    void onGSA(int type, Set<Integer> prns, float pdop, float hdop, float vdop);

    void onGSV(int type, int index, int count, int prn, float elevation, float azimuth, int snr);

    void onPWE(int level);

    void onUnrecognized(String nmea);

    void onBadFormat(String nmea);
}