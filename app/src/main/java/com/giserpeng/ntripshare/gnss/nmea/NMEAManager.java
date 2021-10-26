package com.giserpeng.ntripshare.gnss.nmea;

import android.util.Log;

import com.giserpeng.ntripshare.gnss.bean.GnssLocation;
import com.giserpeng.ntripshare.gnss.bean.GnssSatellite;
import com.giserpeng.ntripshare.gnss.listener.NMEAParserHandler;
import com.giserpeng.ntripshare.gnss.listener.NMEAParserListener;
import com.giserpeng.ntripshare.gnss.utils.GeoUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-03-14 15:22
 */
public class NMEAManager {

    private static final String TAG = "NMEAParser";

    private NMEAParser mGnssParser;
    private Set<Integer> fixSatellites = new HashSet<>();
    private List<GnssSatellite> gnssSatellites = new ArrayList<>();
    private GnssLocation location;
    private GnssLocation lastGnssLocation;
    private NMEAParserListener mGnssListener;

    private boolean flagGGA = false;
    private boolean flagGST = false;
    private long lastTime;

    private String nmea = null;
    private String gga = null;

    public NMEAManager() {
        this.mGnssParser = new NMEAParserImpl(mGnssHandler);
    }

    public void setNMEAParserListener(NMEAParserListener listener) {
        this.mGnssListener = listener;
    }

    public void parseNMEA(String nmea) {
        this.nmea = nmea;
        mGnssParser.parse(nmea);
    }

    public String getGga() {
        return gga;
    }

    public GnssLocation getLastGnssLocation() {
        return lastGnssLocation;
    }

    public void setLastGnssLocation(GnssLocation lastGnssLocation) {
        this.lastGnssLocation = lastGnssLocation;
    }

    public void resetAll() {
        resetLocation();
        resetSatellites();
        lastGnssLocation = null;
        mGnssListener = null;
    }

    private void newLocation(long time) {
        if (location == null || time != lastTime) {
            location = new GnssLocation();
            lastTime = time;
        }
    }

    private void resetLocation() {
        flagGGA = false;
        flagGST = false;
        lastTime = 0;
        location = null;
    }

    private void updateLocation() {
        if (flagGGA && flagGST) {
            lastGnssLocation = location.clone();
            lastGnssLocation = GeoUtils.gps84_To_Gcj02(lastGnssLocation);
            lastGnssLocation.setUtc(System.currentTimeMillis());
            mGnssListener.onLocation(lastGnssLocation);
        } else {
            mGnssListener.onLocation(null);
        }
        resetLocation();
    }

    private void updateSatellites() {
        List<GnssSatellite> satellites = new ArrayList<>();
        for (GnssSatellite state : gnssSatellites) {
            state.setUsedInFix(fixSatellites.contains(state.getPrn()));
            satellites.add(state);
        }
        mGnssListener.onSatellites(satellites);
        resetSatellites();
    }

    private void resetSatellites() {
        gnssSatellites.clear();
        fixSatellites.clear();
    }

    private void updateLevel(int level) {
        if (mGnssListener != null) {
            mGnssListener.onPower(level);
        }
    }

    /**
     * NMEA解析处理
     */
    private NMEAParserHandler mGnssHandler = new NMEAParserHandler() {
        @Override
        public void onGGA(long time, double latitude, double longitude, float altitude, float geo, int status, int satellites, float hdop) {
            Log.d(TAG, "onGGA: time=" + time + ", latitude=" + latitude + ", longitude=" + longitude  + ", altitude=" + altitude+ ", status=" + status + ", satellites=" + satellites + ", hdop=" + hdop);
            newLocation(time);

            location.setLat(latitude);
            location.setLon(longitude);
            location.setAlt(altitude);
            location.setFix(status);
            location.setSat(satellites);
            location.setGeo(geo);

            gga = nmea;
            flagGGA = true;
        }

        @Override
        public void onRMC(long time, long date, double latitude, double longitude, float speed, float azimuth) {
            Log.d(TAG, "onRMC: time=" + time + ", latitude=" + latitude + ", longitude=" + longitude + ", speed=" + speed + ", azimuth=" + azimuth);
            newLocation(time);

            location.setUtc(time + date);
            location.setSpeed(speed);
            location.setBearing(azimuth);
        }

        @Override
        public void onGST(long time, float latDev, float lonDev, float altDev) {
            Log.d(TAG, "onGST: time=" + time + ", acc=" + (float) Math.sqrt(latDev * latDev + lonDev * lonDev));
            newLocation(time);

            double acc = Math.sqrt(latDev * latDev + lonDev * lonDev);
            BigDecimal decimal = new BigDecimal(String.valueOf(acc));
            decimal = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);

            location.setAcc(decimal.floatValue());

            flagGST = true;
        }

        @Override
        public void onGSA(int status, Set<Integer> prns, float pdop, float hdop, float vdop) {
            Log.d(TAG, "onGSA: " + ", pdop=" + pdop + ", hdop=" + hdop + ", vdop=" + vdop);
            fixSatellites.addAll(prns);
        }

        @Override
        public void onGSV(int type, int index, int count, int prn, float elevation, float azimuth, int snr) {
            Log.d(TAG, "onGSV: index=" + index + ", count=" + count + ", prn=" + prn + ", elevation=" + elevation + ", azimuth=" + azimuth + ", snr=" + snr);

            GnssSatellite satellite = new GnssSatellite();
            satellite.setType(type);
            satellite.setPrn(prn);
            satellite.setAzimuth(azimuth);
            satellite.setElevation(elevation);
            satellite.setSnr(snr);

            gnssSatellites.add(satellite);
        }

        @Override
        public void onPWE(int level) {
            updateLocation();
            updateSatellites();
            updateLevel(level);
        }

        @Override
        public void onUnrecognized(String nmea) {
            Log.d(TAG, "onUnrecognized" + nmea);
        }

        @Override
        public void onBadFormat(String nmea) {
            Log.d(TAG, "onBadFormat" + nmea);
        }
    };
}