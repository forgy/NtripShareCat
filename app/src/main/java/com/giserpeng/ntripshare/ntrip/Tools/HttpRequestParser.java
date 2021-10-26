package com.giserpeng.ntripshare.ntrip.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;

public class HttpRequestParser {
    private String requestLine;
    private final Hashtable<String, String> requestHeaders;
    private final StringBuffer messageBody;

    public HttpRequestParser(String request) throws IOException {
        requestHeaders = new Hashtable<>();
        messageBody = new StringBuffer();
        parseRequest(request);
    }

    private void parseRequest(String request) throws IOException, IndexOutOfBoundsException {
        BufferedReader reader = new BufferedReader(new StringReader(request));

        setRequestLine(reader.readLine());

        String header = reader.readLine();
        while (header.length() > 0) {
            appendHeaderParameter(header);
            header = reader.readLine();
        }

        String bodyLine = reader.readLine();
        while (bodyLine != null) {
            appendMessageBody(bodyLine);
            bodyLine = reader.readLine();
        }

        String[] splitLine = requestLine.split(" ");

        if (requestLine.matches("GET [\\S]+ HTTP[\\S]+")) {
            requestHeaders.put("GET", splitLine[1].replaceAll("/", ""));
        }

        if (requestLine.matches("SOURCE [\\S]+ [\\S]+")) {
            requestHeaders.put("SOURCE", splitLine[2]);
            requestHeaders.put("PASSWORD", splitLine[1]);
        }
    }

    private void setRequestLine(String requestLine) {
        if (requestLine == null || requestLine.length() == 0) {
            this.requestLine = "";
        }
        this.requestLine = requestLine;
    }

    private void appendHeaderParameter(String header) {
        int idx = header.indexOf(":");
        if (idx == -1) {
            return;
        }
        requestHeaders.put(header.substring(0, idx), header.substring(idx + 1));
    }

    private void appendMessageBody(String bodyLine) {
        messageBody.append(bodyLine).append("\r\n");
    }

    public String getParam(String headerName) {
        return requestHeaders.get(headerName);
    }

}

