package com.giserpeng.ntripshare.ntrip.Servers;

import com.giserpeng.ntripshare.ntrip.Clients.Client;

public interface ICasterEvent {
    public  void onMessage(String msg);
    public  void onUserMessage(String msg);
    public  void onClientAuthenticationFail(Client client);
    public  void onClientAuthenticationSuccess(Client client);
}
