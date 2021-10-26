package com.giserpeng.ntripshare.ntrip.Clients.Authentication;

import com.giserpeng.ntripshare.ntrip.Clients.Client;

public interface Authenticator {
    boolean authentication(Client client);
    String toString();
}
