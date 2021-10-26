package com.giserpeng.ntripshare.gnss.rtcm;

import android.util.Log;

import com.giserpeng.ntripshare.gnss.bean.GnssData;
import com.giserpeng.ntripshare.gnss.bean.GnssType;
import com.giserpeng.ntripshare.gnss.bean.ReferenceStation;
import com.giserpeng.ntripshare.gnss.bean.SatelliteData;
import com.giserpeng.ntripshare.gnss.bean.SignalData;
import com.giserpeng.ntripshare.gnss.cons.Constants;
import com.giserpeng.ntripshare.gnss.listener.RTCMParserHandler;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMHeader;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMSatellite;
import com.giserpeng.ntripshare.gnss.rtcm.msm.MSMSignal;
import com.giserpeng.ntripshare.gnss.utils.BitUtils;
import com.giserpeng.ntripshare.gnss.utils.RTKUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Company: Copyright (c) 太昌电子 2018</p>
 *
 * @author liuhanling
 * @date 2019-04-01 18:13
 */
public class RTCMParserImpl implements RTCMParser {

    private static final String TAG = "RTCMParser";

    private static final int MSG_PRE = (byte) 0XD3;
    private static final int PRE_LEN = 3;
    private static final int CRC_LEN = 3;

    private GnssData mGnssData;
    private RTCMParserHandler mRTCMParserHandler;

    public RTCMParserImpl(RTCMParserHandler listener) {
        this.mRTCMParserHandler = listener;
    }

    @Override
    public void parseRTCM(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }

        Log.d(TAG, "RTCM length:" + bytes.length);

        mGnssData = new GnssData();
        boolean flag = false;

        try {
            int i = 0;
            while (i < bytes.length) {
                if (bytes[i] == MSG_PRE) {
                    int len = (int) BitUtils.bytesDecodeR(bytes, i * 8 + 14, 10);
                    byte[] data = Arrays.copyOfRange(bytes, i + PRE_LEN, i + PRE_LEN + len);
                    int messageNumber = (int) BitUtils.bytesDecodeR(data, 0, 12);

                    Log.d(TAG, "MSG: i: " + i + ", number:" + messageNumber + ", len: " + len);
                    switch (messageNumber) {
                        case Constants.ARP_1005:
                        case Constants.ARP_1006:
                            parseSARP(data);
                            break;
                        case Constants.MSM4_GPS:
                        case Constants.MSM4_GLO:
                        case Constants.MSM4_GAL:
                        case Constants.MSM4_BDS:
                            flag = true;
                            parseMSM4(data);
                            break;
                        default:
                            break;
                    }
                    i += PRE_LEN + CRC_LEN + len;
                } else {
                    i++;
                }
            }

            if (mRTCMParserHandler != null && flag) {
                mRTCMParserHandler.onGNSS(mGnssData);
            }
        } catch (Exception e) {
            if (mRTCMParserHandler != null) {
                mRTCMParserHandler.onException(e);
            }
        }
    }

    @Override
    public void parseSARP(byte[] bytes) {

        int messageNumber = (int) BitUtils.bytesDecodeR(bytes, 0, 12);
        int referenceStationId = (int) BitUtils.bytesDecodeR(bytes, 12, 12);
        int itrf = (int) BitUtils.bytesDecodeR(bytes, 24, 6);
        int gpsIndicator = (int) BitUtils.bytesDecodeR(bytes, 30, 1);
        int gloIndicator = (int) BitUtils.bytesDecodeR(bytes, 31, 1);
        int galIndicator = (int) BitUtils.bytesDecodeR(bytes, 32, 1);
        int referenceStationIndicator = (int) BitUtils.bytesDecodeR(bytes, 33, 1);
        double ecefX = BitUtils.bytesDouble(bytes, 34, 38);
        int receiverOscillatorIndicator = (int) BitUtils.bytesDecodeR(bytes, 72, 1);
        double ecefY = BitUtils.bytesDouble(bytes, 74, 38);
        int quarterCycleIndicator = (int) BitUtils.bytesDecodeR(bytes, 112, 2);
        double ecefZ = BitUtils.bytesDouble(bytes, 114, 38);

        ReferenceStation station = new ReferenceStation();
        station.setMessageNumber(messageNumber);
        station.setReferenceStationId(referenceStationId);
        station.setItrf(itrf);
        station.setGpsIndicator(gpsIndicator);
        station.setGloIndicator(gloIndicator);
        station.setGalIndicator(galIndicator);
        station.setReceiverOscillatorIndicator(referenceStationIndicator);
        station.setEcefX(ecefX);
        station.setEcefY(ecefY);
        station.setEcefZ(ecefZ);
        station.setReceiverOscillatorIndicator(receiverOscillatorIndicator);
        station.setQuarterCycleIndicator(quarterCycleIndicator);

        if (messageNumber == Constants.ARP_1006) {
            int heigth = (int) BitUtils.bytesDecodeR(bytes, 152, 16);
            station.setHeight(heigth);
        }

        double[] points = RTKUtils.ecef2pos(ecefX * 0.0001, ecefY * 0.0001, ecefZ * 0.0001);

        station.setLat(points[0]);
        station.setLon(points[1]);
        station.setAlt(points[2]);

        Log.d(TAG, "SARP: " + station.toString());

        if (mRTCMParserHandler != null) {
            mRTCMParserHandler.onSARP(station);
        }
    }

    @Override
    public void parseMSM4(byte[] bytes) {
        
        // 解析MSMHeader
        int messageNumber = (int) BitUtils.bytesDecodeR(bytes, 0, 12);
        int referenceStationId = (int) BitUtils.bytesDecodeR(bytes, 12, 12);
        int epochTime = (int) BitUtils.bytesDecodeR(bytes, 24, 30);
        int multipleMessageFlag = (int) BitUtils.bytesDecodeR(bytes, 54, 1);
        int iods = (int) BitUtils.bytesDecodeR(bytes, 55, 3);
        int receiverClockMark = (int) BitUtils.bytesDecodeR(bytes, 65, 2);
        int externalClockMark = (int) BitUtils.bytesDecodeR(bytes, 67, 2);
        int smoothMark = (int) BitUtils.bytesDecodeR(bytes, 69, 1);
        int smoothInterval = (int) BitUtils.bytesDecodeR(bytes, 70, 3);
        long satelliteMask = BitUtils.bytesDecodeR(bytes, 73, 64);
        long signalMask = BitUtils.bytesDecodeR(bytes, 137, 32);

        List<Integer> satelliteList = getSatelliteAndSignalMaskMap(satelliteMask, 64);
        List<Integer> signalList = getSatelliteAndSignalMaskMap(signalMask, 32);
        int satelliteCount = satelliteList.size();
        int signalCount = signalList.size();
        int cellCount = satelliteCount * signalCount;
        long cellMask = BitUtils.bytesDecodeR(bytes, 169, cellCount);
        int validCellCount = getSatelliteAndSignalMaskMap(cellMask, cellCount).size();
        int headerLength = 169 + cellCount;

        MSMHeader msmHeader = new MSMHeader();
        msmHeader.setMessageNumber(messageNumber);
        msmHeader.setReferenceStationId(referenceStationId);
        msmHeader.setEpochTime(epochTime);
        msmHeader.setMultipleMessageFlag(multipleMessageFlag);
        msmHeader.setIods(iods);
        msmHeader.setClockSteeringIndicator(receiverClockMark);
        msmHeader.setExternalClockIndicator(externalClockMark);
        msmHeader.setSmoothIndicator(smoothMark);
        msmHeader.setSmoothInterval(smoothInterval);
        msmHeader.setSatelliteMask(satelliteMask);
        msmHeader.setSignalMask(signalMask);
        msmHeader.setCellMask(cellMask);
        msmHeader.setSatelliteList(satelliteList);
        msmHeader.setSignalList(signalList);
        msmHeader.setSatelliteCount(satelliteCount);
        msmHeader.setSignalCount(signalCount);
        msmHeader.setCellCount(cellCount);
        msmHeader.setValidCellCount(validCellCount);
        msmHeader.setHeaderLength(headerLength);

        Log.d(TAG, "MsmHeader: length=" + headerLength);
        Log.d(TAG, "MsmHeader: " + msmHeader.toString());

        // 解析MSMSatellite
        MSMSatellite msmSatellite = new MSMSatellite();
        for (int i = 0; i < satelliteCount; i++) {
            int millisecondsRoughRange = (int) BitUtils.bytesDecodeR(bytes, headerLength + (i * 8), 8);
            int dotMillisecondsRoughRange = (int) BitUtils.bytesDecodeR(bytes, headerLength + (i * 18), 10);
            msmSatellite.addMillisecondsRoughRange(millisecondsRoughRange);
            msmSatellite.addDotMillisecondsRoughRange(dotMillisecondsRoughRange);
        }

        Log.d(TAG, "MSMSatellite: length=" + msmSatellite.getSatelliteLength());
        Log.d(TAG, "MSMSatellite: " + msmSatellite.toString());

        // 解析MSMSignal
        MSMSignal msmSignal = new MSMSignal();
        int finePseudoRangeStart = headerLength + msmSatellite.getSatelliteLength();
        int finePhaseRangeStart = finePseudoRangeStart + (validCellCount * 15);
        int phaseRangeLockTimeStart = finePhaseRangeStart + (validCellCount * 22);
        int halfCycleAmbiguityStart = phaseRangeLockTimeStart + (validCellCount * 4);
        int snrStart = halfCycleAmbiguityStart + validCellCount;

        for (int i = 0; i < validCellCount; i++) {
            int finePseudoRange = (int) BitUtils.bytesDecodeR(bytes, finePseudoRangeStart + (i * 15), 15);
            int finePhaseRange = (int) BitUtils.bytesDecodeR(bytes, finePhaseRangeStart + (i * 22), 22);
            int phaseRangeLockTime = (int) BitUtils.bytesDecodeR(bytes, phaseRangeLockTimeStart + (i * 4), 4);
            int halfCycleAmbiguity = (int) BitUtils.bytesDecodeR(bytes, halfCycleAmbiguityStart + i, 1);
            int snr = (int) BitUtils.bytesDecodeR(bytes, snrStart + (i * 6), 6);

            msmSignal.addFinePseudorange(finePseudoRange);
            msmSignal.addFinePhaseRange(finePhaseRange);
            msmSignal.addPhaseRangeLockTimeIndicator(phaseRangeLockTime);
            msmSignal.addHalfCycleAmbiguityIndicator(halfCycleAmbiguity);
            msmSignal.addSnr(snr);
        }

        Log.d(TAG, "MSMSignal: length=" + 48 * validCellCount);
        Log.d(TAG, "MSMSignal: " + msmSignal.toString());

        parseGNSS(msmHeader, msmSatellite, msmSignal);
    }

    @Override
    public void parseGNSS(MSMHeader msmHeader, MSMSatellite msmSatellite, MSMSignal msmSignal) {



        List<Integer> satelliteList = msmHeader.getSatelliteList();
        int satelliteCount = satelliteList.size();
        int signalCount = msmHeader.getSignalList().size();
        int validCellIndex = 0;

        for (int i = 0; i < satelliteCount; i++) {

            SatelliteData satellite = new SatelliteData();
            satellite.setSatelliteSystem(GnssType.getSatlliteSystem(msmHeader.getMessageNumber()));
            satellite.setPrn(satelliteList.get(i));

            for (int j = 0; j < signalCount; j++) {
                boolean isValid = msmHeader.isValidCell(i * signalCount + j);
                if (isValid) {

                    String frequencyBand = msmHeader.getFrequencyBand()[j];
                    int finePseudoRange = msmSignal.getFinePseudoRangeList().get(validCellIndex);
                    double speedLight = 299792458;
                    double pseudoRange = (speedLight / 1000)
                            * (msmSatellite.getMillisecondsRoughRangeList().get(i)
                            + msmSatellite.getDotMillisecondsRoungRangeList().get(i) / 1024
                            + Math.pow(2, -24) * finePseudoRange);

                    int finePhaseRange = msmSignal.getFinePhaseRangeList().get(validCellIndex);
                    double phaseRange = (speedLight / 1000)
                            * (msmSatellite.getMillisecondsRoughRangeList().get(i)
                            + msmSatellite.getDotMillisecondsRoungRangeList().get(i) / 1024
                            + Math.pow(2, -29) * finePhaseRange);

                    int cnr = msmSignal.getSnrList().get(validCellIndex);

                    SignalData signal = new SignalData();
                    signal.setFrequencyBand(frequencyBand);
                    signal.setPseudoRange(pseudoRange);
                    signal.setPhaseRange(phaseRange);
                    signal.setSnr(cnr);

                    satellite.addSignalData(signal);
                    validCellIndex++;
                }
            }

            mGnssData.addSatelliteData(satellite);
        }

        Log.d(TAG, "GnssData: " + mGnssData.toString());
    }

    private static ArrayList<Integer> getSatelliteAndSignalMaskMap(long mask, int size) {
        ArrayList<Integer> maskList = new ArrayList<>();
        for (int i = 0; i < size; i++, mask = mask >> 1) {
            if ((mask & 1) == 1) {
                int value = size - i;
                maskList.add(value);
            }
        }
        Collections.sort(maskList);
        return maskList;
    }
}