package com.giserpeng.ntripshare.ntrip.Servers;

import com.giserpeng.ntripshare.ntrip.Models.ReferenceStationModel;
import com.giserpeng.ntripshare.ntrip.Spatial.PointLla;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;


public abstract class ReferenceStationUpdater {
    final static private Logger logger = LoggerFactory.getLogger(ReferenceStationUpdater.class.getName());
    protected static Map<String, ReferenceStation> refStations = new HashMap<>();

    public static ReferenceStation getStationByName(String name) {
        return refStations.get(name);
    }

    public static ReferenceStation getStationById(int id) {
        for (ReferenceStation station : refStations.values()) {
            if (station.getId() == id)
                return station;
        }
        return null;
    }

    protected void rawDataType() {
        model.setFormat("RAW");
        model.setLla(new PointLla(0, 0));
        model.setCountry("");
        model.setIdentifier("");
        model.setNav_system("");
    }

    protected static Timer timer = new Timer();
    protected ReferenceStationModel model;
}
