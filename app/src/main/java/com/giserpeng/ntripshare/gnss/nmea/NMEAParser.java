package com.giserpeng.ntripshare.gnss.nmea;


public interface NMEAParser {

    void parse(String nmea);

    void parseGGA(String[] data) throws Exception;

    void parseGSA(String[] data) throws Exception;

    void parseGST(String[] data) throws Exception;

    void parseGSV(String[] data) throws Exception;

    void parseRMC(String[] data) throws Exception;

    void parsePWE(String[] data) throws Exception;


}
