package com.giserpeng.ntripshare.ntrip.Clients.Authentication;

import com.giserpeng.ntripshare.ntrip.Clients.Client;

public class None implements Authenticator {
    @Override
    public boolean authentication(Client client) {
        return true;
    }

    @Override
    public String toString() {
        return "None";
    }
}
