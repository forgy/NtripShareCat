package com.giserpeng.ntripshare.ntrip.Clients.Passwords;

import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SHA256 implements PasswordHandler {
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

        String userHash = sha256(fromUser);
        return fromDB.toLowerCase().equals(userHash);

    }

    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
