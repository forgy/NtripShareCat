package com.giserpeng.ntripshare.ntrip;

import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class NtripClient implements Runnable {
    private String nProtocol = "";
    private String nServer = "";
    private int nPort = 2101;

    private String nMountpoint = "";
    private String nUsername = "";
    private String nPassword = "";

    private Socket nsocket;
    private InputStream nis;
    private OutputStream nos;

    private NtripClientListener ncl;

    public NtripClient(String pProtocol, String pServer, int pPort, String pMountpoint, String pUsername, String pPassword, NtripClientListener ncl) {
        nProtocol = pProtocol;
        nServer = pServer;
        nPort = pPort;
        nMountpoint = pMountpoint;
        nUsername = pUsername;
        nPassword = pPassword;

        this.ncl = ncl;
    }

    public void run() {
        try {
            //Log.i(NTAG, "Creating socket");
            SocketAddress sockaddr = new InetSocketAddress(nServer, nPort);
            nsocket = new Socket();
            nsocket.connect(sockaddr, 10 * 1000); // 10 second connection timeout
            if (nsocket.isConnected()) {
                nsocket.setSoTimeout(20 * 1000); // 20 second timeout once data is flowing
                nis = nsocket.getInputStream();
                nos = nsocket.getOutputStream();
                //Log.i(NTAG, "Socket created, streams assigned");

                if (nProtocol.equals("ntripv1")) {
                    // Build request message
                    //Log.i(NTAG, "This is a NTRIP connection");
//                    String requestmsg = "GET /" + nMountpoint + " HTTP/1.0\r\n";
//                    requestmsg += "User-Agent: NTRIP JavaNTRIPClient/20190711\r\n";
//                    requestmsg += "Accept: */*\r\n";
//                    requestmsg += "Connection: close\r\n";
//                    if (nUsername.length() > 0) {
//                        requestmsg += "Authorization: Basic " + ToBase64(nUsername + ":" + nPassword);
//                    }
//                    requestmsg += "\r\n";
                    StringBuilder sb = new StringBuilder();
                    sb.append( "GET /" + nMountpoint + " HTTP/1.0\r\n");
                    sb.append("User-Agent: NTRIP CasterTest/20190711\r\n");
                    sb.append("Accept: */*\r\n");
                    sb.append("Connection: close\r\n");
                    if (nUsername.length() > 0) {
                        sb.append("Authorization: Basic ").append(ToBase64(nUsername + ":" + nPassword));
                    }
                    sb.append("\r\n");
                    nos.write(sb.toString().getBytes());
                    //Log.i("Request", requestmsg);
                } else {
                    //Log.i(NTAG, "This is a raw TCP/IP connection");
                }

                //Log.i(NTAG, "Waiting for inital data...");
                byte[] buffer = new byte[4096];
                int read = nis.read(buffer, 0, 4096); // This is blocking
                while (read != -1) {
                    byte[] tempdata = new byte[read];
                    System.arraycopy(buffer, 0, tempdata, 0, read);
                    // Log.i(NTAG, "Got data: " + new String(tempdata));
                    if (ncl != null) {
                        ncl.onDataReceived(tempdata);
                    }
                    read = nis.read(buffer, 0, 4096); // This is blocking
                }
            }
        } catch (SocketTimeoutException ex) {
            if (ncl != null) {
                ncl.onTimeoutOccurred();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                nis.close();
                nos.close();
                nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.i(NTAG, "Finished");
            if (ncl != null) {
                ncl.onTransactionCompleted();
            }
        }
    }

    private String ToBase64(String in) {
        return Base64.encodeToString(in.getBytes(), Base64.CRLF);
    }
}
