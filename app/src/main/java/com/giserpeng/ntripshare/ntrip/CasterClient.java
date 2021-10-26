package com.giserpeng.ntripshare.ntrip;

import android.util.Base64;

import com.giserpeng.ntripshare.ntrip.source.CasterSource;
import com.giserpeng.ntripshare.ntrip.source.NetworkSource;
import com.giserpeng.ntripshare.ntrip.source.NtripSource;
import com.giserpeng.ntripshare.ntrip.source.ServerInfo;
import com.giserpeng.ntripshare.ntrip.source.SourceType;
import com.giserpeng.ntripshare.ntrip.source.StreamSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.netty.handler.timeout.ReadTimeoutException;

public class CasterClient {

    private ServerInfo serverInfo;


    public CasterClient(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public List<NtripSource> getSourceTable() {
        List<NtripSource> resultList = new ArrayList<NtripSource>();

        Socket nsocket = null;
        InputStream nis = null;
        OutputStream nos = null;

        String hostName = serverInfo.getHost();
        int port = serverInfo.getPort();
        String username = serverInfo.getUsername();
        String password = serverInfo.getPassword();

        try {
            SocketAddress sockaddr = new InetSocketAddress(hostName, port);
            nsocket = new Socket();
            nsocket.connect(sockaddr, 10 * 1000); /* 10 second connection timeout */
            if (nsocket.isConnected()) {
                nsocket.setSoTimeout( 1000); /* 20 second timeout once data is flowing */
                nos = nsocket.getOutputStream();
                System.out.println("Invalid1 Line :getOutputStream " );
                StringBuilder sb = new StringBuilder();
                sb.append("GET / HTTP/1.0\r\n");
                sb.append("User-Agent: NTRIP Android/20190711\r\n");
                sb.append("Accept: */*\r\n");
                sb.append("Connection: close\r\n");
                if (username.length() > 0) {
                    sb.append("Authorization: Basic ").append(ToBase64(username + ":" + password));
                }
                sb.append("\r\n");

                List<String>  arr = new ArrayList<>();
                nos.write(sb.toString().getBytes());
                BufferedReader is=new BufferedReader(new InputStreamReader(nsocket.getInputStream()));
                String readline;
                while ((readline=is.readLine())!=null){
                    System.out.println("Invalid1 Line :readBytes "+ readline);
                    arr.add(readline);
                    if("ENDSOURCETABLE".equalsIgnoreCase(readline)){
                        break;
                    }
                }

                for (String line : arr) {
                    if (line.length() < 3) {
                        continue;
                    }
                    String typeStr = line.substring(0, 3);
                    SourceType st = SourceType.getSourceType(typeStr);
                    switch (st) {
                        case CAS:
                            resultList.add(new CasterSource(line));
                            break;
                        case NET:
                            resultList.add(new NetworkSource(line));
                            break;
                        case STR:
                            resultList.add(new StreamSource(line));
                            break;
                        default:
                            System.out.println("Invalid Line : " + line);
                            break;
                    }
                }
            }
        }
        catch (SocketTimeoutException ex) {
            System.out.println("Invalid1 Line : " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            System.out.println("Invalid1 Line : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (nis != null) {
                    nis.close();
                }
                if (nos != null) {
                    nos.close();
                }
                if (nsocket != null) {
                    nsocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return resultList;
    }


    private String ToBase64(String in) {
        return Base64.encodeToString(in.getBytes(), Base64.CRLF);
    }

}
