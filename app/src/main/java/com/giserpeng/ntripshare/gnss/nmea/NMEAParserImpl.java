package com.giserpeng.ntripshare.gnss.nmea;

import android.text.TextUtils;

import com.giserpeng.ntripshare.gnss.bean.GnssType;
import com.giserpeng.ntripshare.gnss.cons.Constants;
import com.giserpeng.ntripshare.gnss.listener.NMEAParserHandler;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-03-13 18:05
 */
public class NMEAParserImpl implements NMEAParser {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HHmmss", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("ddMMyy", Locale.US);

    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private NMEAParserHandler handler;

    public NMEAParserImpl(NMEAParserHandler handler) {
        this.handler = handler;
    }

    @Override
    public synchronized void parse(String nmea) {

        if (TextUtils.isEmpty(nmea))
            return;

        String[] data = formatData(nmea);
        if (data == null || data.length < 2) {
            handler.onUnrecognized(nmea);
            return;
        }

        int length = data.length;

        try {
            switch (data[0]) {
                case Constants.GPGGA:
                case Constants.GNGGA:
                    if (length == 15) {
                        parseGGA(data);
                    } else {
                        handler.onBadFormat(nmea);
                    }
                    break;
                case Constants.GPGSA:
                case Constants.GNGSA:
                    if (length == 18) {
                        parseGSA(data);
                    } else {
                        handler.onBadFormat(nmea);
                    }
                    break;
                case Constants.GPGST:
                case Constants.GNGST:
                    if (length == 9) {
                        parseGST(data);
                    } else {
                        handler.onBadFormat(nmea);
                    }
                    break;
                case Constants.GPRMC:
                case Constants.GNRMC:
                    if (length == 13) {
                        parseRMC(data);
                    } else {
                        handler.onBadFormat(nmea);
                    }
                    break;
                case Constants.GPGSV:
                case Constants.GLGSV:
                case Constants.GAGSV:
                case Constants.BDGSV:
                    if (length >= 8) {
                        parseGSV(data);
                    } else {
                        handler.onBadFormat(nmea);
                    }
                    break;
                case Constants.POWER:
                    parsePWE(data);
                    break;
                default:
                    handler.onUnrecognized(nmea);
                    break;
            }
        } catch (Exception e) {
            handler.onBadFormat(nmea);
        }
    }

    @Override
    public synchronized void parseGGA(String[] data) throws Exception {

        long time = TIME_FORMAT.parse(data[1]).getTime();
        double lat = toDegrees(data[2]);
        double lon = toDegrees(data[4]);
        int fix = parseInt(data[6], 0);
        int sat = parseInt(data[7], 0);
        float hdop = parseFloat(data[8], 0.0F);
        float alt = parseFloat(data[9], 0.0F);
        float geo = parseFloat(data[11], 0.0F);

        handler.onGGA(time, lat, lon, alt, geo, fix, sat, hdop);
    }

    @Override
    public synchronized void parseGSA(String[] data) throws Exception {

        int type = Integer.parseInt(data[2]);

        Set<Integer> pnrs = new HashSet<>();
        for (int i = 3; i <= 14; i++) {
            int pnr = parseInt(data[i], -1);
            if (pnr > -1) {
                pnrs.add(pnr);
            }
        }

        float pdop = parseFloat(data[15], -1.0F);
        float hdop = parseFloat(data[16], -1.0F);
        float vdop = parseFloat(data[17], -1.0F);

        handler.onGSA(type, pnrs, pdop, hdop, vdop);
    }

    @Override
    public synchronized void parseGST(String[] data) throws Exception {

        long time = TIME_FORMAT.parse(data[1]).getTime();

        float latDev = parseFloat(data[6], -1F);
        float lonDev = parseFloat(data[7], -1F);
        float altDev = parseFloat(data[8], -1F);

        handler.onGST(time, latDev, lonDev, altDev);
    }

    @Override
    public synchronized void parseGSV(String[] data) throws Exception {

        int type = GnssType.getType(data[0]);
        int index = Integer.parseInt(data[2]);
        int count = Integer.parseInt(data[3]);

        for (int i = 4; i < data.length; i = i + 4) {
            int pnr = Integer.parseInt(data[i]);
            float ele = Float.parseFloat(data[i + 1]);
            float azi = Float.parseFloat(data[i + 2]);
            int snr = Integer.parseInt(data[i + 3]);
            handler.onGSV(type, index, count, pnr, ele, azi, snr);
        }
    }

    @Override
    public synchronized void parseRMC(String[] data) throws Exception {

        long time = TIME_FORMAT.parse(data[1].substring(0, 6)).getTime();
        long date = DATE_FORMAT.parse(data[9]).getTime();
        double lat = toDegrees(data[3]);
        double lon = toDegrees(data[5]);
        float speed = Float.parseFloat(data[7]);
        float azimuth = Float.parseFloat(data[8]);

        handler.onRMC(time, date, lat, lon, speed, azimuth);
    }

    @Override
    public synchronized void parsePWE(String[] data) throws Exception {
        int level = Integer.parseInt(data[1]);
        handler.onPWE(level);
    }

    private static int parseInt(String number, int defValue) {
        if (TextUtils.isEmpty(number)) {
            return defValue;
        }
        try {
            return Integer.parseInt(number);
        } catch (Exception e) {
            return defValue;
        }
    }

    private static float parseFloat(String number, Float defValue) {
        if (TextUtils.isEmpty(number)) {
            return defValue;
        }
        try {
            BigDecimal decimal = new BigDecimal(number);
            decimal = decimal.setScale(2, BigDecimal.ROUND_HALF_UP);
            return decimal.floatValue();
        } catch (Exception e) {
            return defValue;
        }
    }

    private static String[] formatData(String nmea) {
        if (TextUtils.isEmpty(nmea)) return null;
        return nmea.substring(0, nmea.indexOf("*")).split(",", -1);
    }

    private static double toDegrees(String degrees) {
        if (TextUtils.isEmpty(degrees)) {
            return 0.0d;
        }

        BigDecimal sixty = new BigDecimal(60);
        BigDecimal hundred = new BigDecimal(100);
        BigDecimal decimal = new BigDecimal(degrees);
        decimal = decimal.divide(hundred, 10, BigDecimal.ROUND_HALF_UP);

        BigDecimal degreesInt = new BigDecimal(decimal.intValue());
        BigDecimal degreesDec = decimal.subtract(degreesInt)
                .multiply(hundred)
                .divide(sixty, 10, BigDecimal.ROUND_HALF_UP);

        return degreesInt.add(degreesDec).doubleValue();
    }
}