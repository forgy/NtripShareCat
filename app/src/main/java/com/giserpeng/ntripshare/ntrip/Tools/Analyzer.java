package com.giserpeng.ntripshare.ntrip.Tools;

import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Analyzer {
    final static private Logger logger = LoggerFactory.getLogger(Analyzer.class.getName());
    final static private Timer timer = new Timer();

    private ReferenceStation referenceStation;
    private AnalyzeTasks tasks;
    private MessagePool messagePool = new MessagePool();
    private boolean isAnalyzed = false;

    public Analyzer(ReferenceStation referenceStation) {
        this.referenceStation = referenceStation;
        tasks = new AnalyzeTasks(referenceStation, messagePool);
        this.startAnalyzeTasks();
    }

    private void startAnalyzeTasks() {
        timer.schedule(tasks.carrier, 30_000, 220_000);
        timer.schedule(tasks.FormatDetails, 30_000, 220_000);
        timer.schedule(tasks.navSystems, 30_000, 220_000);
        timer.schedule(tasks.position, 30_000, 220_000);
        timer.schedule(tasks.positionMetaInfo, 60_000, 220_000);
        timer.schedule(tasks.rtcmVersion, 30_000, 220_000);
    }

    public void close() {
        tasks.carrier.cancel();
        tasks.FormatDetails.cancel();
        tasks.navSystems.cancel();
        tasks.position.cancel();
        tasks.positionMetaInfo.cancel();
        tasks.rtcmVersion.cancel();
    }

    //todo remove get array
    public void analyze(MessagePack messagePack) {
        for (Map.Entry<Integer, byte[]> entry : messagePack.getArray()) {
            messagePool.putData(entry.getKey(), entry.getValue());
        }
    }
}

class MessagePool {
    HashMap<Integer, Long> timeMarks = new HashMap<>();
    TreeMap<Integer, Integer> msgDelays = new TreeMap<>();
    HashMap<Integer, byte[]> bytePool = new HashMap<>();

    public void putData(Integer number, byte[] bytes) {
        //rewrite with "get" and "null" check
        if (bytePool.containsKey(number)) {
            long currentTime = System.currentTimeMillis();
            int delay = Math.round((currentTime - timeMarks.get(number)) / 1000f);
            bytePool.put(number, bytes);
            msgDelays.put(number, Math.max(delay, 1));
            timeMarks.put(number, currentTime);
        } else {
            bytePool.put(number, bytes);
            msgDelays.put(number, 0);
            timeMarks.put(number, System.currentTimeMillis());
        }
    }

    public byte[] getMessageBytes(int nmb) {
        return bytePool.get(nmb);
    }

    @Override
    public String toString() {
        if (bytePool.size() == 0)
            return "";

        StringBuilder response = new StringBuilder();

        for (Map.Entry<Integer, Integer> entry : msgDelays.entrySet()) {
            response.append("," + entry.getKey() + "(" + entry.getValue() + ")");
        }

        return response.substring(1);
    }
}