package com.giserpeng.ntripshare.ntrip.Clients.Passwords;

public interface PasswordHandler {
    boolean Compare(String fromDB, String fromUser);
}
