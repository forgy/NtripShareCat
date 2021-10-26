package com.giserpeng.ntripshare.ntrip.Network;

import com.giserpeng.ntripshare.protocol.ProxyMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import io.netty.channel.Channel;

public class SocketProxy {
    private static Logger logger = LoggerFactory.getLogger(SocketProxy.class.getName());
    private static long prevSocketId = 0;
    public String userID;

    private static final byte[] OK_MESSAGE = "ICY 200 OK\r\n".getBytes();
    private static final byte[] BAD_MESSAGE = "ERROR - Bad Password\r\n".getBytes();

    public Channel socketChannel = null;

    public boolean endOfStreamReached = false;

    public SocketProxy(Channel socketChannel,String userID) {
        this.socketChannel = socketChannel;
        this.userID = userID;
    }

    public void sendOkMessage() throws IOException {
        this.write(ByteBuffer.wrap(OK_MESSAGE));
        logger.debug("Connection " + this.userID + " response: ICY 200 OK");
        if (logger.isDebugEnabled()) {
            logger.debug("Connection " + this.userID + " response: ICY 200 OK");
        }
    }

    public void sendBadMessageAndClose() throws IOException {
        this.write(ByteBuffer.wrap(BAD_MESSAGE));
        this.close();
        if (logger.isDebugEnabled()) {
            logger.debug("Connection " + this.userID + " response: ERROR - Bad Password");
        }
    }


    public int write(ByteBuffer byteBuffer) throws IOException {
        int len = byteBuffer.limit() - byteBuffer.position();
        byte[] bytes1 = new byte[len];
        byteBuffer.get(bytes1);

        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.P_TYPE_TRANSFER);
        proxyMessage.setUri(userID);
        proxyMessage.setData(bytes1);
        socketChannel.writeAndFlush(proxyMessage);

        return bytes1.length;
    }

    public void close() throws IOException {
        this.socketChannel.close();
    }

    public boolean isRegistered() {
        return this.socketChannel.isRegistered();
    }
}