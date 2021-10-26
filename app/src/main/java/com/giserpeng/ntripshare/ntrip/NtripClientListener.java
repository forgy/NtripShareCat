package com.giserpeng.ntripshare.ntrip;

public interface NtripClientListener {

    void onDataReceived(byte[] data);

    void onTimeoutOccurred();

    void onTransactionCompleted();

}
