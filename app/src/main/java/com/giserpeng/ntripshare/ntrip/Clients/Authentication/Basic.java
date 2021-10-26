package com.giserpeng.ntripshare.ntrip.Clients.Authentication;

import android.util.Base64;
import android.util.Log;

import com.giserpeng.ntripshare.ntrip.Clients.Client;

import java.util.logging.Logger;

public class Basic implements Authenticator {
    public static final Logger log = Logger.getLogger(Basic.class.getName());

    @Override
    public boolean authentication(Client client) {
        String basicAuth = client.getHttpHeader("Authorization");
        Log.i("authentication",basicAuth);
        if (basicAuth == null)
            return false;
        if (!basicAuth.matches(" Basic [\\S]+"))
            return false;
        basicAuth = basicAuth.trim();
        String[] accPass = basicAuthorizationDecode(basicAuth.split(" ")[1]);
        client.setClientUserName(accPass[0]);
        client.setClientPassword(accPass[1]);
        return client.getCaster().checkAuth (client.getClientUserName(),client.getClientPassword());
    }

    @Override
    public String toString() {
        return "Basic";
    }

    String[] basicAuthorizationDecode(String src) {
        String temp = src.replaceAll("Basic", "").trim();
        byte[] decode = Base64.decode(temp,Base64.DEFAULT);
        String result = new String(decode);

        String[] response = result.split(":");

        if (response.length != 2)
            throw new SecurityException("Illegal authorization format");

        return response;
    }
}
