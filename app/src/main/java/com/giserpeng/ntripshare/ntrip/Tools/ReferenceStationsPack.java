package com.giserpeng.ntripshare.ntrip.Tools;

import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ReferenceStationsPack {
    final static private Logger logger = LoggerFactory.getLogger(ReferenceStationsPack.class.getName());

    private HashMap<Integer, ReferenceStation> array = new HashMap<>();

    public void parseStationsId(String ids) {
        for (String id : ids.split(",")) {
            int intId = Integer.parseInt(id);
            array.put(intId, ReferenceStation.getStationById(Integer.parseInt(id)));
        }
    }


}
