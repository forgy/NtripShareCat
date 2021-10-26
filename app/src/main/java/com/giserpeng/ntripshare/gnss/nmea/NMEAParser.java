package com.giserpeng.ntripshare.gnss.nmea;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-03-13 18:47
 */
public interface NMEAParser {

    void parse(String nmea);

    void parseGGA(String[] data) throws Exception;

    void parseGSA(String[] data) throws Exception;

    void parseGST(String[] data) throws Exception;

    void parseGSV(String[] data) throws Exception;

    void parseRMC(String[] data) throws Exception;

    void parsePWE(String[] data) throws Exception;


}
