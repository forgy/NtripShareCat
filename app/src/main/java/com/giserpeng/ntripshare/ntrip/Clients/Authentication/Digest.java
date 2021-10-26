package com.giserpeng.ntripshare.ntrip.Clients.Authentication;

import com.giserpeng.ntripshare.ntrip.Clients.Client;

public class Digest implements Authenticator {

    @Override
    public boolean authentication(Client client) {
        return false;
    }

    @Override
    public String toString() {
        return "Digest";
    }
}
