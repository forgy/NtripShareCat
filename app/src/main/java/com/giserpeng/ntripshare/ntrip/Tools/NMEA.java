package com.giserpeng.ntripshare.ntrip.Tools;

import android.util.Log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
/*
   from = https://gist.github.com/javisantana/1326141
 */

public class NMEA {
    final static private Logger logger = LoggerFactory.getLogger(NMEA.class.getName());

    interface SentenceParser {
        boolean parse(String[] tokens, GPSPosition position);
    }

    static float Latitude2Decimal(String lat, String NS) {
        float med = Float.parseFloat(lat.substring(2)) / 60.0f;
        med += Float.parseFloat(lat.substring(0, 2));
        if (NS.startsWith("S")) {
            med = -med;
        }
        return med;
    }

    static float Longitude2Decimal(String lon, String WE) {
        float med = Float.parseFloat(lon.substring(3)) / 60.0f;
        med += Float.parseFloat(lon.substring(0, 3));
        if (WE.startsWith("W")) {
            med = -med;
        }
        return med;
    }

    class GPGGA implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[2], tokens[3]);
            position.lon = Longitude2Decimal(tokens[4], tokens[5]);
            position.quality = Integer.parseInt(tokens[6]);
            position.altitude = Float.parseFloat(tokens[9]);
            return true;
        }
    }

    class GPGGL implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.lat = Latitude2Decimal(tokens[1], tokens[2]);
            position.lon = Longitude2Decimal(tokens[3], tokens[4]);
            position.time = Float.parseFloat(tokens[5]);
            return true;
        }
    }

    class GPRMC implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[3], tokens[4]);
            position.lon = Longitude2Decimal(tokens[5], tokens[6]);
            position.velocity = Float.parseFloat(tokens[7]);
            position.dir = Float.parseFloat(tokens[8]);
            return true;
        }
    }

    class GPVTG implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.dir = Float.parseFloat(tokens[3]);
            return true;
        }
    }

    class GPRMZ implements SentenceParser {
        public boolean parse(String[] tokens, GPSPosition position) {
            position.altitude = Float.parseFloat(tokens[1]);
            return true;
        }
    }


    GPSPosition position = new GPSPosition();

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<String, SentenceParser>();

    public NMEA() {
        sentenceParsers.put("GPGGA", new GPGGA());
        sentenceParsers.put("GPGGL", new GPGGL());
        sentenceParsers.put("GPRMC", new GPRMC());
        sentenceParsers.put("GPRMZ", new GPRMZ());
        sentenceParsers.put("GPVTG", new GPVTG());
        sentenceParsers.put("BDGGA", new GPGGA());
        sentenceParsers.put("GNGGA", new GPGGA());
    }

    public GPSPosition parse(String line) {
        Log.i("NMEA",line);
        if (line.startsWith("$")) {
            String nmea = line.substring(1);
            String[] tokens = nmea.split(",");
            String type = tokens[0];
            if (sentenceParsers.containsKey(type)) {
                sentenceParsers.get(type).parse(tokens, position);
            }
            position.updatefix();
            position.time = System.currentTimeMillis();
        }

        return position;
    }
}
