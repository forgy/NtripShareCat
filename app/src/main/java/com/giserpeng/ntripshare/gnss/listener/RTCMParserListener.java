package com.giserpeng.ntripshare.gnss.listener;

import com.giserpeng.ntripshare.gnss.bean.GnssData;
import com.giserpeng.ntripshare.gnss.bean.ReferenceStation;

public interface RTCMParserListener {

    void onSARP(ReferenceStation station);

    void onGNSS(GnssData satellite);

}