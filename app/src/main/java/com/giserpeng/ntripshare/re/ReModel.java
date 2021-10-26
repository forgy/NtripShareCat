package com.giserpeng.ntripshare.re;

import android.util.Log;

import com.giserpeng.ntripshare.util.EncryptUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReModel {
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    String key;
    Date EndTime;
    Date ReTime;
    String ServerIp;
    int ServerPort;
    int type = 0;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getCasterPort() {
        return CasterPort;
    }

    public void setCasterPort(int casterPort) {
        CasterPort = casterPort;
    }
    int CasterPort;
    String uuid;

    public ReModel(String key) {
        try {
            this.key = key;
            Log.i("ReModel", key);
            String data = EncryptUtil.decrypt(key);

            Log.i("ReModel", data);
            String[] datas = data.split("-");
            if (datas.length >= 5) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                EndTime = sdf.parse(datas[0]);
                ServerIp = datas[1];
                ServerPort = Integer.parseInt(datas[2]);
                CasterPort = Integer.parseInt(datas[3]);
                type = Integer.parseInt(datas[4]);
                uuid = datas[5];
            }
        } catch (Exception e) {
            Log.i("ReModel", e.getMessage());
        }
    }

    public static boolean CheckCode(String key) {
        Log.i("ReModel3", key);
        if (key == null || key.equals("")) {
            return false;
        }
        try {
            String data = EncryptUtil.decrypt(key);
            Log.i("ReModel4", data);
            String[] datas = data.split("-");
            Log.i("ReModel4", key);
            if (datas.length < 5) {
                Log.i("ReModel4", key);
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date EndTime = sdf.parse(datas[0]);
            if (EndTime.getTime() < System.currentTimeMillis()) {
                Log.i("ReModel4", datas[0]);
                return false;
            }
            Log.i("ReModel4", key);
            return true;
        } catch (Exception e) {
            Log.i("ReModel", e.getMessage());
            return false;
        }
    }

    public Date getEndTime() {
        return EndTime;
    }

    public void setEndTime(Date endTime) {
        EndTime = endTime;
    }

    public Date getReTime() {
        return ReTime;
    }

    public void setReTime(Date reTime) {
        ReTime = reTime;
    }

    public String getServerIp() {
        return ServerIp;
    }

    public void setServerIp(String serverIp) {
        ServerIp = serverIp;
    }

    public int getServerPort() {
        return ServerPort;
    }

    public void setServerPort(int serverPort) {
        ServerPort = serverPort;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
