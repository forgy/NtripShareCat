package com.giserpeng.ntripshare.gnss.listener;

import com.giserpeng.ntripshare.gnss.bean.GnssData;
import com.giserpeng.ntripshare.gnss.bean.ReferenceStation;


public interface RTCMParserHandler {

    void onSARP(ReferenceStation station);

    void onGNSS(GnssData satellite);

    void onException(Exception e);

}