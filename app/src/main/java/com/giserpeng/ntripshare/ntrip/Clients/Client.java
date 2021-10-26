package com.giserpeng.ntripshare.ntrip.Clients;
import android.util.Log;

import com.giserpeng.ntripshare.ntrip.Models.MountPointModel;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.ntrip.Tools.GPSPosition;
import com.giserpeng.ntripshare.ntrip.Tools.HttpRequestParser;
import com.giserpeng.ntripshare.ntrip.Tools.NMEA;
import com.giserpeng.ntripshare.protocol.ProxyMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

import io.netty.channel.Channel;

public class Client {
    final static private Logger logger = LoggerFactory.getLogger(Client.class.getName());

    private ReferenceStation referenceStation;
    private MountPointModel mountPoint;

    public NtripCaster getCaster() {
        return caster;
    }

    private final NtripCaster caster;
    private final Channel socket;
    private GPSPosition position;
    private final HttpRequestParser httpRequest;
    private final ByteBuffer input = ByteBuffer.allocate(1024);

    private final Date connectionTime = new  Date();
    private long bytesReceive = 0;
    private long bytesSent = 0;
    private boolean isAuth = false;

    private static final byte[] OK_MESSAGE = "ICY 200 OK\r\n".getBytes();
    private static final byte[] BAD_MESSAGE = "ERROR - Bad Password\r\n".getBytes();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public Date getConnectionTime() {
        return connectionTime;
    }

    private String ClientUserName = "";

    public void setClientUserName(String clientUserName) {
        ClientUserName = clientUserName;
        Log.i("setClientUserName",clientUserName);
    }

    public void setClientPassword(String clientPassword) {
        ClientPassword = clientPassword;
    }

    private String ClientPassword = "";

    public String getClientUserName(){
        return  ClientUserName;
    }

    public String getClientPassword(){
        return  ClientPassword;
    }


    public Client(Channel socket, HttpRequestParser httpRequest, NtripCaster caster, String userId) {
        this.socket = socket;
        this.httpRequest = httpRequest;
        this.caster = caster;
        this.userId = userId;
    }

    public void subscribe(ReferenceStation referenceStation) {
        if (referenceStation == null)
            return;

        this.referenceStation = referenceStation;
        this.referenceStation.addClient(this);
    }

    public void close() {
        try {
            if (this.referenceStation != null)
                this.referenceStation.removeClient(this);

            if (socket != null) {
                ProxyMessage proxyMessage = new ProxyMessage();
                proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
                proxyMessage.setUri(userId);
                socket.writeAndFlush(proxyMessage);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void read(byte[] data) {
        String line = new String(data);

        logger.debug("Connection " + userId + " accept nmea from client -> " + line);
        this.setPosition(line);

        try {
            caster.clientAuthorizationProcessing(this);
        } catch (IOException e) {
            logger.error("Connection " + userId + "Re-authorization error", e);
            this.close();
        }
    }

    /* getters and setters */
    public void setMountPoint(MountPointModel mountPoint) {
        this.mountPoint = mountPoint;
    }

    public void setPosition(String nmea_str) {
        this.position = new NMEA().parse(nmea_str);
        Log.i("setPosition" + position.dataTime ,System.currentTimeMillis() + "-");
    }

    public GPSPosition getPosition() {
        return position;
    }

    public String getHttpHeader(String key) {
        return httpRequest.getParam(key);
    }

    public void sendOkMessage() throws IOException {
        this.write(OK_MESSAGE);
        logger.debug("Connection " + this.userId + " response: ICY 200 OK");
        if (logger.isDebugEnabled()) {
            logger.debug("Connection " + this.userId + " response: ICY 200 OK");
        }
    }

    public void sendBadMessageAndClose() throws IOException {
        this.write(BAD_MESSAGE);
        this.close();
        if (logger.isDebugEnabled()) {
            logger.debug("Connection " + this.userId + " response: ERROR - Bad Password");
        }
    }

    public int write(byte[] bytes1){
        Log.i("write",userId+ "bytes1"+ bytes1.length);
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.P_TYPE_TRANSFER);
        proxyMessage.setUri(userId);
        proxyMessage.setData(bytes1);
        socket.writeAndFlush(proxyMessage);
        return bytes1.length;
    }

    public int write(ByteBuffer byteBuffer){

        int len = byteBuffer.limit() - byteBuffer.position();
        byte[] bytes1 = new byte[len];
        byteBuffer.get(bytes1);
        Log.i("write",userId+ "bytes1"+ bytes1.length);

        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.P_TYPE_TRANSFER);
        proxyMessage.setUri(userId);
        proxyMessage.setData(bytes1);
        socket.writeAndFlush(proxyMessage);
        return bytes1.length;
    }
}
