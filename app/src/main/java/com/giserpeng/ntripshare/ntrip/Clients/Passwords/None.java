package com.giserpeng.ntripshare.ntrip.Clients.Passwords;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class None implements PasswordHandler {
    final static private Logger logger = LoggerFactory.getLogger(None.class.getName());

    @Override
    public boolean Compare(String fromDB, String fromUser) {
        if (fromDB == null) {
            return false;
        }

        return fromDB.equals(fromUser);
    }
}
