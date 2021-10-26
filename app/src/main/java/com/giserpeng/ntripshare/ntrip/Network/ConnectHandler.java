package com.giserpeng.ntripshare.ntrip.Network;

import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Servers.ReferenceStation;
import com.giserpeng.ntripshare.ntrip.Tools.HttpRequestParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;

public class ConnectHandler implements IWork {
    private static Logger logger = LoggerFactory.getLogger(ConnectHandler.class);

    private Socket socket;
    private final NtripCaster caster;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final SelectionKey key;

    public ConnectHandler(SelectionKey key, NtripCaster caster) throws IOException {
        this.socket = new Socket((SocketChannel) key.channel());
        this.caster = caster;
        this.key = key;
        this.key.attach(this);
    }

    public void close() {
        logger.debug("Connection " + socket.socketId + " close");
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readSelf() throws IOException {
        buffer.clear();
        if (!socket.endOfStreamReached) {
            socket.read(buffer);
        } else {
            throw new IOException("Connection " + socket.socketId + " end of stream reached.");
        }
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        dataQueue.add(bytes);
    }

    Queue<byte[]> dataQueue = new ArrayDeque<>();

    @Override
    public void run() {
        try {
            if (dataQueue.peek() == null)
                return;

            String request = new String(dataQueue.poll());

            if (logger.isDebugEnabled()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Connection ");
                stringBuilder.append(socket.socketId);
                stringBuilder.append(" send request:\r\n");
                stringBuilder.append(request);
                logger.debug(stringBuilder.toString());
            }

            HttpRequestParser httpParser = new HttpRequestParser(request);

            //GET CONNECT
            if (httpParser.getParam("GET") != null) {
//                Client client = new Client(this.socket, httpParser, this.caster);
//                this.caster.clientAuthorizationProcessing(client);
//                this.key.attach(client);
            } else
                //OR SOURCE CONNECT
                if (httpParser.getParam("SOURCE") != null) {

                    ReferenceStation station = ReferenceStation.getStationByName(httpParser.getParam("SOURCE"));

                    //mb station not exists
                    if (station == null)
                        throw new IOException("MountPoint " + httpParser.getParam("SOURCE") + " is not exists.");

                    //mb password wrong
                    if (!station.checkPassword(httpParser.getParam("PASSWORD")))
                        throw new IOException("Connection " + socket.socketId + " Bad password.");

                    //mb station in time connect
                    station.setSocket(socket);

                    this.socket.sendOkMessage();
                    this.key.attach(station);
                }
        } catch (IOException e) {
            try {
                logger.error(e.getMessage());
                this.socket.sendBadMessageAndClose();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
    }
}