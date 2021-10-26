package com.giserpeng.ntripshare.ntrip.source;

public class MountInfo extends ServerInfo {

    private String mountPoint;

    public MountInfo(String ip, int port, String username, String password) {
        super(ip, port, username, password);
    }

    public MountInfo(String ip, int port, String username, String password, String mountPoint) {
        super(ip, port, username, password);
        this.mountPoint = mountPoint;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

}
