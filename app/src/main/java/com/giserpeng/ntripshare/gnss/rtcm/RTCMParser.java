package com.giserpeng.ntripshare.gnss.rtcm;

import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMHeader;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMSatellite;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMSignal;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-01 18:12
 */
public interface RTCMParser {

    void parseRTCM(byte[] bytes);

    void parseSARP(byte[] data);

    void parseMSM4(byte[] data);

    void parseGNSS(MSMHeader msmHeader, MSMSatellite msmSatellite, MSMSignal msmSignal);
}
