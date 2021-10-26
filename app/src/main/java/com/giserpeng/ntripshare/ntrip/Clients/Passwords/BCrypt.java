package com.giserpeng.ntripshare.ntrip.Clients.Passwords;

import java.util.logging.Level;
import java.util.logging.Logger;

import static at.favre.lib.crypto.bcrypt.BCrypt.*;

public class BCrypt implements PasswordHandler {
    public static final Logger log = Logger.getLogger(SHA256.class.getName());

    @Override
    public boolean Compare(String fromDB, String fromUser) {
        if (fromDB == null) {
            log.log(Level.WARNING, "Password fromDB is NULL");
            return false;
        }

        if (fromUser == null) {
            log.log(Level.WARNING, "User password is NULL");
            return false;
        }

        if (fromDB == "") {
            log.log(Level.WARNING, "Password fromDB is empty");
            return false;
        }

        Result rr = verifyer().verify(fromUser.getBytes(), fromDB.getBytes());

        if (rr.verified)
            return true;

        return false;
    }
}
