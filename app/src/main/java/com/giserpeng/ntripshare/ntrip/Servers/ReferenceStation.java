package com.giserpeng.ntripshare.ntrip.Servers;

import android.util.Log;

import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.Models.ReferenceStationModel;
import com.giserpeng.ntripshare.ntrip.Network.IWork;
import com.giserpeng.ntripshare.ntrip.Network.Socket;
import com.giserpeng.ntripshare.ntrip.Tools.Analyzer;
import com.giserpeng.ntripshare.ntrip.Tools.Decoders.IDecoder;
import com.giserpeng.ntripshare.ntrip.Tools.Decoders.RAW;
import com.giserpeng.ntripshare.ntrip.Tools.Decoders.RTCM_3X;
import com.giserpeng.ntripshare.ntrip.Tools.GPSPosition;
import com.giserpeng.ntripshare.ntrip.Tools.MessagePack;
import com.giserpeng.ntripshare.ntrip.Tools.NMEA;
import com.giserpeng.ntripshare.ui.net.NetPointModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * All reference station from the database, contains in memory and waiting for connect base receiver.
 * After successful connect, setSocket() method is called.
 */
public class ReferenceStation extends ReferenceStationUpdater implements IWork {
    final static private Logger logger = LoggerFactory.getLogger(ReferenceStation.class.getName());

    Map<String, Client> subscribers = new ConcurrentHashMap<String, Client>();

    //    private final CopyOnWriteArrayList<Client> subscribers = new CopyOnWriteArrayList<>();
    private final ByteBuffer buffer = ByteBuffer.allocate(32768);

    private Socket socket;
    private Analyzer analyzer;

    private long connectTimeMark;
    private long acceptBytes;

    private GPSPosition currentPosition;

    public boolean available = true;

    public void setNetMode(boolean netMode) {
        dataBuffer.clear();
        dataBufferNetMode.clear();
        netPointModelHashMap.clear();
        NetMode = netMode;
    }

    public void removeNetPoint(String id) {
        netPointModelHashMap.remove(id);
    }

    private boolean NetMode = false;
    //accepted bytes pack
    private final Queue<byte[]> dataBuffer = new ArrayDeque<>();
    private final Map<String, Queue<byte[]>> dataBufferNetMode = new HashMap<>();
    private final Map<String, NetPointModel> netPointModelHashMap = new HashMap<>();

    //The decoder can change.
    private IDecoder decoder = new RTCM_3X();

    public ReferenceStation(ReferenceStationModel model) {
        this.model = model;
        refStations.put(model.getName(), this);
    }

    /**
     * 获取上报坐标
     *
     * @return
     */
    public GPSPosition getCurrentPosition() {
        double centLat = 0;
        double centerLon = 0;
        int num = 0;
        if (subscribers.size() > 0) {
            if (subscribers.size() == 1) {
                for (Client client : subscribers.values()) {
                    if (client.getPosition() != null) {
                        if (currentPosition == null) {
                            return client.getPosition();
                        }
                        float dis = distance(currentPosition.lat, currentPosition.lon, client.getPosition().lat, client.getPosition().lon);
                        if (dis > 10000) {
                            currentPosition.lat = (float) centLat / num;
                            currentPosition.lon = (float) centerLon / num;
                        } else {
                            double dx = client.getPosition().lat - currentPosition.lat;
                            double dY = client.getPosition().lon - currentPosition.lon;
                            currentPosition.lat += (float) dx / 200;
                            currentPosition.lon += (float) dY / 200;
                        }
                    }
                }
                return currentPosition;
            }
            for (Client client : subscribers.values()) {
                GPSPosition clientPosition = client.getPosition();
                if (currentPosition == null) {
                    currentPosition = clientPosition;
                    return currentPosition;
                }
                if (clientPosition != null) {
                    centLat += clientPosition.lat;
                    centerLon += clientPosition.lon;
                    num++;
                }
            }
            if (num != 0) {

                if (currentPosition == null) {
                    currentPosition = new GPSPosition();
                    currentPosition.lat = (float) centLat / num;
                    currentPosition.lon = (float) centerLon / num;
                    return currentPosition;
                }
                double dx = centLat / num - currentPosition.lat;
                double dY = centerLon / num - currentPosition.lon;
                float dis = distance(currentPosition.lat, currentPosition.lon, centLat / num, centerLon / num);
                if (dis > 10000) {
                    currentPosition.lat = (float) centLat / num;
                    currentPosition.lon = (float) centerLon / num;
                } else {
                    currentPosition.lat += (float) dx / 200;
                    currentPosition.lon += (float) dY / 200;
                }
            }
        }
        return currentPosition;
    }


    /**
     * 获取已登录客户端
     *
     * @return
     */
    public List<Client> getClient() {
        List<Client> list = new ArrayList<>();
        list.addAll(subscribers.values());
        return list;
    }

    public Client getClientByUserId(String id) {
        if (subscribers.containsKey(id)) {
            return subscribers.get(id);
        }

        return null;
    }

    /*
    删除下线
     */
    public void removeClientByUserId(String id) {
        if (subscribers != null) {
            if (subscribers.containsKey(id)) {
                subscribers.remove(id);
            }
        }
    }

    public void clearDeadClient(int time) {
        try {
            Iterator<Map.Entry<String, Client>> entries = subscribers.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Client> entry = entries.next();
                Client value = entry.getValue();
                if ((value.getPosition() != null && value.getPosition().dataTime < (System.currentTimeMillis() - time))
                        || (value.getPosition() == null && (value.getConnectionTime().getTime() < (System.currentTimeMillis() - time)))) {
                    Log.i("setPosition" + value.getPosition().dataTime, System.currentTimeMillis() + "-" + time);
                    removeClientByUserId(value.getUserId());
                }
            }
        } catch (Exception e) {

        }

    }

    public int getClientNum() {
        return subscribers.size();
    }

    public void setSocket(Socket socket) throws IOException {
        if (this.socket == null || !this.socket.isRegistered()) {
            this.decoder = new RTCM_3X();
            this.analyzer = new Analyzer(this);
            this.acceptBytes = 0;
            this.connectTimeMark = System.currentTimeMillis();
            this.socket = socket;
            this.available = true;
            logger.info("Connection " + socket.socketId + " (" + model.getName() + ") was logged in.");
        } else {
            throw new IOException("Connection " + socket.socketId + " " + model.getName() + " socket already taken.");
        }
    }

    public boolean checkLogin(String userName) {
        for (Client client : subscribers.values()) {
            if (userName.equalsIgnoreCase(client.getClientUserName())) {
                return true;
            }
        }
        return false;
    }

    public Client checkLoginClient(String userName) {
        for (Client client : subscribers.values()) {
            if (userName.equalsIgnoreCase(client.getClientUserName())) {
                return client;
            }
        }
        return null;
    }

    public void offline(String userName) {
        Iterator<Map.Entry<String, Client>> entries = subscribers.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Client> entry = entries.next();
            Client value = entry.getValue();
            if (userName.equalsIgnoreCase(value.getClientUserName())) {
                removeClientByUserId(value.getUserId());
            }
        }
    }


    /**
     * Close connection.
     */
    @Override
    public void close() {
        try {
            this.available = false;
            this.analyzer.close();
//            this.socket.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void readSelf() throws IOException {
//        int count = 0;
//        this.buffer.clear();

//        if (!this.socket.endOfStreamReached) {
//            count = this.socket.read(buffer);
//        } else {
//            throw new IOException("Connection " + socket.socketId + " RefSt: " + model.getName() + " end of stream reached.");
//        }
//
//        logger.info("Connection " + socket.socketId + " RefSt: " + model.getName() + " accept " + count);

//        buffer.flip();
//        byte[] bytes = new byte[buffer.remaining()];
//        buffer.get(bytes);
//        dataBuffer.add(bytes);
    }

    public void pushData(byte[] data) {
        dataBuffer.add(data);
    }


    public void pushData(NetPointModel netPointModel, byte[] data) {
        if (NetMode) {
            if (dataBufferNetMode.containsKey(netPointModel.getUuid())) {
                dataBufferNetMode.get(netPointModel.getUuid()).add(data);
            } else {
                Queue<byte[]> dataBuffer2 = new ArrayDeque<>();
                dataBuffer2.add(data);
                dataBufferNetMode.put(netPointModel.getUuid(), dataBuffer2);
            }
            if (!netPointModelHashMap.containsKey(netPointModel.getUuid())) {
                netPointModelHashMap.put(netPointModel.getUuid(), netPointModel);
            } else {
                netPointModelHashMap.get(netPointModel.getUuid()).setLat(netPointModel.getLat());
                netPointModelHashMap.get(netPointModel.getUuid()).setLon(netPointModel.getLon());
            }
        }
//        dataBuffer.add(data);
    }

    @Override
    public void run() {
        if (!NetMode) {
            Log.i("pushData", "0");
            if (dataBuffer.peek() == null)
                return;
            try {
                Log.i("pushData", "1");
                byte[] bytes = dataBuffer.poll();
                MessagePack messagePack = decoder.separate(ByteBuffer.wrap(bytes));
                logger.info(model.getName() + " decode: " + messagePack.toString());
                if (analyzer != null) {
                    analyzer.analyze(messagePack);
                }
                sendMessageToClients(messagePack);
//                sendMessageToClients(ByteBuffer.wrap(bytes));
            } catch (IllegalArgumentException e) {
                logger.info(model.getName() + " error " + decoder.getType());
                if (decoder instanceof RTCM_3X) {
                    decoder = new RAW();
                    super.rawDataType();
                    logger.info(model.getName() + " set new decoder " + decoder.getType());
                }
            }
        } else {
            Log.i("pushData", "2 " + netPointModelHashMap.size());
            Iterator<Map.Entry<String, NetPointModel>> entries = netPointModelHashMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, NetPointModel> entry = entries.next();
                NetPointModel value = entry.getValue();
                try {
                    Log.i("pushData", "4");
                    Queue<byte[]> dataBuffer2 = dataBufferNetMode.get(value.getUuid());
                    if (dataBuffer2.peek() == null)
                        continue;
                    try {
                        Log.i("pushData", "5");
                        byte[] bytes = dataBuffer2.poll();
                        MessagePack messagePack = decoder.separate(ByteBuffer.wrap(bytes));

                        if (analyzer != null) {
                            analyzer.analyze(messagePack);
                        }
                        sendMessageToClients(value, messagePack);
//                        sendMessageToClients(value, ByteBuffer.wrap(bytes));
                    } catch (IllegalArgumentException e) {
                        logger.info(model.getName() + " error " + decoder.getType());
//                        if (decoder instanceof RTCM_3X) {
//                            decoder = new RAW();
//                            super.rawDataType();
//                            logger.info(model.getName() + " set new decoder " + decoder.getType());
//                        }
                    }
                } catch (Exception e) {

                }
            }
        }

    }

    private void sendMessageToClients(ByteBuffer localBuffer) {
//        ByteBuffer localBuffer = messagePack.getFullBytes();
        Log.i("pushData", "sendMessageToClients" + subscribers.size());
        for (Client client : subscribers.values()) {
            localBuffer.flip();
            try {
                client.write(localBuffer);
            } catch (Exception e) {
                client.close();
            }
        }
    }

    private void sendMessageToClients(NetPointModel netPointModel, ByteBuffer localBuffer) {
        Log.i("pushData", "sendMessageToClients");
//        ByteBuffer localBuffer = messagePack.getFullBytes();

        for (Client client : subscribers.values()) {
            if (netPointModel.getUuid().equalsIgnoreCase(getNearestNodeID(client))) {
                Log.i("pushData", "getNearestNodeID");
                localBuffer.flip();
                try {
                    client.write(localBuffer);
                    Log.i("sendMessageToClients", "getNearestNodeID" + localBuffer.array().length);
                } catch (Exception e) {
                    client.close();
                }
            }
        }
    }


    private void sendMessageToClients(MessagePack messagePack) {
        ByteBuffer localBuffer = messagePack.getFullBytes();
        Log.i("pushData", "sendMessageToClients" + subscribers.size());
        for (Client client : subscribers.values()) {
            localBuffer.flip();
            try {
                client.write(localBuffer);
            } catch (Exception e) {
                client.close();
            }
        }
    }

    private void sendMessageToClients(NetPointModel netPointModel, MessagePack messagePack) {
        Log.i("pushData", "sendMessageToClients");
        ByteBuffer localBuffer = messagePack.getFullBytes();

        for (Client client : subscribers.values()) {
            if (netPointModel.getUuid().equalsIgnoreCase(getNearestNodeID(client))) {
                Log.i("pushData", "getNearestNodeID");
                localBuffer.flip();
                try {
                    client.write(localBuffer);
                    Log.i("sendMessageToClients", "getNearestNodeID" + localBuffer.array().length);
                } catch (Exception e) {
                    client.close();
                }
            }
        }
    }

    public String getNearestNodeID(Client client) {
        double dis = Integer.MAX_VALUE;
        String re = null;
        Iterator<Map.Entry<String, NetPointModel>> entries = netPointModelHashMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, NetPointModel> entry = entries.next();
            NetPointModel value = entry.getValue();
            double dis2 = Math.sqrt((value.getLat() - client.getPosition().lat) * (value.getLat() - client.getPosition().lat)
                    + (value.getLon() - client.getPosition().lon) * (value.getLon() - client.getPosition().lon));
            if (dis2 < dis) {
                dis = dis2;
                re = value.getUuid();
            }
        }
        Log.i("pushData", "getNearestNodeID" + re);
        return re;
    }

    public NetPointModel getNearestNode(Client client) {
        double dis = Integer.MAX_VALUE;
        NetPointModel re = null;
        Iterator<Map.Entry<String, NetPointModel>> entries = netPointModelHashMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, NetPointModel> entry = entries.next();
            NetPointModel value = entry.getValue();
            double dis2 = Math.sqrt((value.getLat() - client.getPosition().lat) * (value.getLat() - client.getPosition().lat)
                    + (value.getLon() - client.getPosition().lon) * (value.getLon() - client.getPosition().lon));
            if (dis2 < dis) {
                dis = dis2;
                re = value;
            }
        }
        Log.i("pushData", "getNearestNodeID" + re);
        return re;
    }

    /**
     * Distance from reference station to client position.
     * Used for get nearest reference station for client receiver.
     *
     * @return float
     * @throws NullPointerException
     */
    public float distance(double lat, double lon, double lat1, double lon1) throws NullPointerException {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat - lat1);
        double dLng = Math.toRadians(lon - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lat1)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) (earthRadius * c);
    }

    public void clearClient() {
        try {
            subscribers.clear();
        } catch (Exception e) {

        }

    }

    /**
     * Check password reference stations.
     *
     * @param password
     * @return boolean
     */
    public boolean checkPassword(String password) {
        return model.getPassword().equals(password);
    }

    public void addClient(Client client) {
        subscribers.put(client.getUserId(), client);
    }

    public void removeClient(Client client) {
        subscribers.remove(client.getUserId());
    }

    public ReferenceStationModel getModel() {
        return this.model;
    }

    public String getName() {
        return model.getName();
    }

    public int getId() {
        return model.getId();
    }

}