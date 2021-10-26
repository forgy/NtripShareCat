package com.giserpeng.ntripshare.gnss.listener;

import com.giserpeng.ntripshare.gnss.bean.GnssData;
import com.giserpeng.ntripshare.gnss.bean.ReferenceStation;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-03 15:16
 */
public interface RTCMParserHandler {

    void onSARP(ReferenceStation station);

    void onGNSS(GnssData satellite);

    void onException(Exception e);

}