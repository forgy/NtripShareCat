package com.giserpeng.ntripshare.gnss.rtcm;

import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMHeader;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMSatellite;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMSignal;


public interface RTCMParser {

    void parseRTCM(byte[] bytes);

    void parseSARP(byte[] data);

    void parseMSM4(byte[] data);

    void parseGNSS(MSMHeader msmHeader, MSMSatellite msmSatellite, MSMSignal msmSignal);
}
