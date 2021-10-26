package com.giserpeng.ntripshare.gnss;

import com.giserpeng.ntripshare.gnss.bean.GnssLocation;
import com.giserpeng.ntripshare.gnss.listener.NMEAParserListener;
import com.giserpeng.ntripshare.gnss.listener.RTCMParserListener;
import com.giserpeng.ntripshare.gnss.nmea.NMEAManager;
import com.giserpeng.ntripshare.gnss.rtcm.RTCMManager;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-08 10:59
 */
public class GnssService {

    private NMEAManager mNMEAManager;
    private RTCMManager mRTCMManager;

    private static class SingletonHolder {
        private static GnssService INSTANCE = new GnssService();
    }

    public static GnssService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private GnssService() {
        mNMEAManager = new NMEAManager();
        mRTCMManager = new RTCMManager();
    }

    public void setNMEAParserListener(NMEAParserListener listener) {
        this.mNMEAManager.setNMEAParserListener(listener);
    }

    public void setRTCMParserListener(RTCMParserListener listener) {
        this.mRTCMManager.setRTCMParserListener(listener);
    }

    public GnssLocation getLastGnssLocation() {
        return mNMEAManager.getLastGnssLocation();
    }

    public String getGGA() {
        return mNMEAManager.getGga();
    }

    public void parseNMEA(String nmea) {
        this.mNMEAManager.parseNMEA(nmea);
    }

    public void parseRTCM(byte[] data) {
        this.mRTCMManager.parseRTCM(data);
    }

    public void clear() {
        mNMEAManager.resetAll();
    }
}