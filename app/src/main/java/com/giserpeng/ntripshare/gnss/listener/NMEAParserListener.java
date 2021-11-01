package com.giserpeng.ntripshare.gnss.listener;


import com.giserpeng.ntripshare.gnss.bean.GnssLocation;
import com.giserpeng.ntripshare.gnss.bean.GnssSatellite;

import java.util.List;

public interface NMEAParserListener {

    void onLocation(GnssLocation location);

    void onSatellites(List<GnssSatellite> satellites);

    void onPower(int level);
}
