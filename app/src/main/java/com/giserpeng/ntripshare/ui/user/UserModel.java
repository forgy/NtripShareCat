package com.giserpeng.ntripshare.ui.user;

import java.util.Date;

public class UserModel {
    private String userName;
    private String password;
    private Date endTime;
//    private Date loginTime;
    private boolean onLine;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

//    public Date getLoginTime() {
//        return loginTime;
//    }
//
//    public void setLoginTime(Date loginTime) {
//        this.loginTime = loginTime;
//    }
}
