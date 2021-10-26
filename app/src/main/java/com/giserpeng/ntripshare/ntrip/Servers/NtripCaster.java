package com.giserpeng.ntripshare.ntrip.Servers;

import android.util.Log;

import com.giserpeng.ntripshare.R;
import com.giserpeng.ntripshare.ShareApplication;
import com.giserpeng.ntripshare.ntrip.Clients.Authentication.Authenticator;
import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.Models.MountPointModel;
import com.giserpeng.ntripshare.ntrip.Models.NtripCasterModel;
import com.giserpeng.ntripshare.ntrip.Network.NetworkProcessor;
import com.giserpeng.ntripshare.ui.user.UserModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NtripCaster extends NtripCasterUpdater {
    private static Logger logger = LoggerFactory.getLogger(NtripCaster.class.getName());
    private int maxUserNum;
    private String mountPoint = "NtripShare";
Map<String, Date> loginDateMap = new ConcurrentHashMap<String, Date>();

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public boolean isMultiUser() {
        return multiUser;
    }

    public void setMultiUser(boolean multiUser) {
        this.multiUser = multiUser;
    }

    private boolean multiUser;
    private static final HashMap<Integer, NtripCaster> ntripCasters = new HashMap<>();
    private static NtripCaster instances = null;

    public static NtripCaster getInstance() {
        return instances;
    }

    private List<UserModel> userModels = new ArrayList<>();

    public void setUserModel(List<UserModel> list) {
        if (list != null) {
            userModels.clear();
            userModels.addAll(list);
        }
    }

    public boolean checkAuth(String username, String password) {
        for (UserModel userModel : userModels) {
            if (userModel.getUserName().equalsIgnoreCase(username)
                    && userModel.getPassword().equalsIgnoreCase(password)
                    && System.currentTimeMillis() < userModel.getEndTime().getTime()) {
                loginDateMap.put(username,new Date());
                return true;
            }
        }
        return false;
    }

    public Date getLoginTime(String userName) {
        if(loginDateMap.containsKey(userName)){
            return loginDateMap.get(userName);
        }

        return null;
    }

    private ICasterEvent casterEvent;

    public void setCasterEvent(ICasterEvent casterEvent) {
        this.casterEvent = casterEvent;
    }

    public void setMaxUserNum(int maxUserNum) {
        this.maxUserNum = maxUserNum;
    }

    public NtripCaster() {
        instances = this;
        NtripCasterModel casterModel = new NtripCasterModel();
        casterModel.setId(0);
        casterModel.setPort(27000);
        HashMap<String, MountPointModel> mountPointModelHashMap = createMountPointModel(mountPoint);
        super.model = casterModel;
        super.mountPoints = mountPointModelHashMap;
        ntripCasters.put(model.getId(), this);

        logger.info("NtripCaster :" + model.getPort() + " has been initiated!");
        if (casterEvent != null) {
            casterEvent.onMessage(ShareApplication.INSTANCE.getString(R.string.caster_run) + "...");
        }
    }

    private void showLog(String msg) {
        if (casterEvent != null) {
            casterEvent.onUserMessage(msg);
        }
    }

    public ReferenceStation getReferenceStation() {
        if (mountPoints.size() == 0) {
            return null;
        }
        return ((MountPointModel) mountPoints.values().toArray()[0]).getReferenceStation();
    }

    public void updateMountPointName(String name) {
        logger.info("updateMountPointName :" + name + mountPoints.size());
        if (mountPoints.size() > 0) {
            MountPointModel mountPointModel = ((MountPointModel) mountPoints.values().toArray()[0]);
            String olDName = mountPointModel.getMountpoint();
            if (!olDName.equalsIgnoreCase(name)) {
                mountPointModel.setMountpoint(name);
                mountPoints.put(name, mountPointModel);
                mountPoints.remove(olDName);
            }
        }
    }

    public String getMountPointName() {
        if (mountPoints.size() > 0) {
            logger.info("updateMountPointName :" + mountPoints.size());
            MountPointModel mountPointModel = ((MountPointModel) mountPoints.values().toArray()[0]);
            String olDName = mountPointModel.getMountpoint();
            logger.info("updateMountPointName :" + olDName);
            return olDName;
        }
        logger.info("updateMountPointName :" + mountPoints.size());
        return "";
    }


    @Override
    public void close() {
        try {
            NtripCaster.ntripCasters.remove(model.getId());
//            selectionKey.cancel();
//            serverChannel.close();
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
    }


    private HashMap<String, MountPointModel> createMountPointModel(String name) {
        MountPointModel mountPointModel = new MountPointModel();
        mountPointModel.setMountpoint(name);
        mountPointModel.setIdentifier(name);
        mountPointModel.setFormat("RTCM 3.2");
        mountPointModel.setFormatDetails("1074(1),1084(1),1124(1),1005(5),1007(5),1033(5)");
        mountPointModel.setCarrier(2);
        mountPointModel.setNavSystem("GNSS");
        mountPointModel.setNetwork("EagleGnss");
        mountPointModel.setCountry("CHN");
        mountPointModel.setLatitude(0.0);
        mountPointModel.setLongitude(0.0);
        mountPointModel.setNmea(true);
        mountPointModel.setSolution(true);
        mountPointModel.setGenerator("NRS1.180703");
        mountPointModel.setCompression("none");
        mountPointModel.setAuthenticator("Basic");
        mountPointModel.setFee(false);
        mountPointModel.setBitrate(19200);
        mountPointModel.setMisc("");
        mountPointModel.initStationPool();
        HashMap<String, MountPointModel> mountPointModelHashMap = new HashMap<>();
        mountPointModelHashMap.put(name, mountPointModel);
        return mountPointModelHashMap;
    }


    private byte[] sourceTable() {
        String header = "SOURCETABLE 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "Connection: close\r\n";

        StringBuilder body = new StringBuilder();
        for (MountPointModel mountPoint : super.mountPoints.values()) {
            body.append(mountPoint.toString());
        }

        body.append("ENDSOURCETABLE\r\n");
        String bodyString = body.toString();
        header += "Content-Length: " + bodyString.getBytes().length + "\r\n\n";
        return (header + bodyString).getBytes();
    }

    /**
     * This method will be call on get request and after NMEA message from a client.
     *
     * @param client
     * @throws IOException
     */
    public void clientAuthorizationProcessing(Client client) throws IOException {
        logger.debug("Connection " + client.getUserId());
        if (client.isAuth()) {
            return;
        }

        MountPointModel requestedMountPoint = super.mountPoints.get(client.getHttpHeader("GET"));
        logger.debug("Connection " + client.getUserId() + " requested mountpoint " + client.getHttpHeader("GET"));
        showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName()  + ShareApplication.INSTANCE.getString(R.string.request_connect) + "," + client.getHttpHeader("GET") + "...");
        if (requestedMountPoint == null) {
            client.write(ByteBuffer.wrap(sourceTable()));
            client.close();
            logger.debug("Caster " + model.getPort() + ": MountPoint " + client.getHttpHeader("GET") + " is not exists!");
            showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.mountpoint_close) + "...");
            return;
        }

        client.setMountPoint(requestedMountPoint);
        Authenticator authenticator = requestedMountPoint.getAuthenticator();
        logger.debug("Caster " + model.getPort() + ": MountPoint " + requestedMountPoint.getMountpoint() + " authenticator " + authenticator);
        showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId() +" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth) + requestedMountPoint.getMountpoint() + "...");
        if (!authenticator.authentication(client)) {
//            if (deiviceID.equalsIgnoreCase(client.getClientUserName())) {
//                client.sendOkMessage();
//                client.setAuth(true);
//            } else {
                logger.info("Caster " + model.getPort() + ": MountPoint " + requestedMountPoint.getMountpoint() + " bad password!");
                showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth_pass) + "...");
                client.sendBadMessageAndClose();
                casterEvent.onClientAuthenticationFail(client);
                return;
        } else {
            if (!isMultiUser()) {
                if (getReferenceStation().getClientNum() >= maxUserNum + 1) {
                    showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth_usermax) + "...");
                    client.sendBadMessageAndClose();
                    casterEvent.onClientAuthenticationFail(client);
                    return;
                }
            } else {
                Client cli= getReferenceStation().checkLoginClient(client.getClientUserName());
                if(cli != null && cli.getPosition() != null && cli.getPosition().dataTime > System.currentTimeMillis() - 11000){
                    showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth_login) + "...");
                    client.sendBadMessageAndClose();
                    casterEvent.onClientAuthenticationFail(client);
                    return;
                }
                if(cli != null){
                    getReferenceStation().offline(client.getClientUserName());
                }
            }
            showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth_ok) + requestedMountPoint.getMountpoint() + "...");
            client.sendOkMessage();
            client.setAuth(true);
            if (casterEvent != null) {
                casterEvent.onClientAuthenticationSuccess(client);
            }
        }

        if (requestedMountPoint.isNmea()) {
            Log.i("NtripCaster", "requestedMountPoint.isNmea()");
            try {
                client.subscribe(requestedMountPoint.getNearestReferenceStation(client));
            } catch (IllegalStateException e) {
                showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth_fail) + requestedMountPoint.getMountpoint() + "...");
                logger.debug("Error authentication");
            }
        } else {
            Log.i("NtripCaster", "requestedMountPoint.isNmea()not");
            ReferenceStation ref = requestedMountPoint.getReferenceStation();
            logger.debug(ref.model.getName());
            client.subscribe(ref);
            showLog(ShareApplication.INSTANCE.getString(R.string.connect) + " " + client.getUserId()+" - "+client.getClientUserName() + ShareApplication.INSTANCE.getString(R.string.auth_ok) + requestedMountPoint.getMountpoint() + "，" + ShareApplication.INSTANCE.getString(R.string.begin_data) + "...");
        }
    }
}
