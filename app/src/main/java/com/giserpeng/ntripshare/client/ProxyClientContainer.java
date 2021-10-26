package com.giserpeng.ntripshare.client;

import android.util.Log;

import com.giserpeng.ntripshare.client.handlers.ClientChannelHandler;
import com.giserpeng.ntripshare.client.handlers.RealServerChannelHandler;
import com.giserpeng.ntripshare.client.listener.ChannelStatusListener;
import com.giserpeng.ntripshare.common.container.Container;
import com.giserpeng.ntripshare.protocol.IdleCheckHandler;
import com.giserpeng.ntripshare.protocol.ProxyMessage;
import com.giserpeng.ntripshare.protocol.ProxyMessageDecoder;
import com.giserpeng.ntripshare.protocol.ProxyMessageEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;


public class ProxyClientContainer implements Container, ChannelStatusListener {

    private static Logger logger = LoggerFactory.getLogger(ProxyClientContainer.class);

    private static final int MAX_FRAME_LENGTH = 1024 * 1024;

    private static final int LENGTH_FIELD_OFFSET = 0;

    private static final int LENGTH_FIELD_LENGTH = 4;

    private static final int INITIAL_BYTES_TO_STRIP = 0;

    private static final int LENGTH_ADJUSTMENT = 0;

    private NioEventLoopGroup workerGroup;

    private Bootstrap bootstrap;

    private Bootstrap realServerBootstrap;

    public String ServerIP;
    public String Key;
    public int ServerPort;
    public String deviceCode;

//    private SSLContext sslContext;

//    private long sleepTimeMill = 1000;

    private static ProxyClientContainer instances = null;

    public boolean isConnect() {
        return isConnect;
    }

    private boolean isConnect = false;

    private OnProxyEvent onProxyEvent;
    private Timer timer;
//    int first = 1;

    public static ProxyClientContainer getInstance() {
        return instances;
    }

    public ProxyClientContainer(String ServerIP, String Key, int ServerPort, String deviceCode) {
        instances = this;
        this.ServerIP = ServerIP;
        this.Key = Key;
        this.ServerPort = ServerPort;
        this.deviceCode = deviceCode;
        workerGroup = new NioEventLoopGroup();
        realServerBootstrap = new Bootstrap();
        realServerBootstrap.group(workerGroup);
        realServerBootstrap.channel(NioSocketChannel.class);
        realServerBootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RealServerChannelHandler());
            }
        });

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ProxyMessageDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP));
                ch.pipeline().addLast(new ProxyMessageEncoder());
                ch.pipeline().addLast(new IdleCheckHandler(IdleCheckHandler.READ_IDLE_TIME, IdleCheckHandler.WRITE_IDLE_TIME, 0));
                ch.pipeline().addLast(new ClientChannelHandler(realServerBootstrap, bootstrap, ProxyClientContainer.this));
            }
        });
        timer = new Timer();
    }

    @Override
    public void start() {
//        connectProxyServer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isConnect) {
                    connectProxyServer();
                    if (onProxyEvent != null) {
                        onProxyEvent.onReConnect();
                    }
                }
            }
        }, 0, 2000);
    }

//    private ChannelHandler createSslHandler(SSLContext sslContext) {
//        SSLEngine sslEngine = sslContext.createSSLEngine();
//        sslEngine.setUseClientMode(true);
//        return new SslHandler(sslEngine);
//    }

    private void connectProxyServer() {
        bootstrap.connect(ServerIP, ServerPort).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // 连接成功，向服务器发送客户端认证信息（clientKey）
                    ClientChannelMannager.setCmdChannel(future.channel());
                    ProxyMessage proxyMessage = new ProxyMessage();
                    proxyMessage.setType(ProxyMessage.C_TYPE_AUTH);
                    proxyMessage.setUri(Key);
                    future.channel().writeAndFlush(proxyMessage);
//                    channelActive();
                } else {
//                    Log.e("connect proxy server failed", future.cause().getMessage());
//                    // 连接失败，发起重连
//                    reconnectWait();
//                    connectProxyServer();
//                    isConnect = false;
//                    if(onProxyEvent != null){
//                        onProxyEvent.onReConnect();
//                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
        isConnect = false;
        timer.cancel();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Log.e("connect proxy server failed", "");
        if (isConnect) {
            if (onProxyEvent != null) {
                onProxyEvent.onDisConnected();
            }
        }
        isConnect = false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        isConnect = true;
        if (onProxyEvent != null) {
            onProxyEvent.onConnected();
        }
    }

//    private void reconnectWait() {
//        try {
//            if (sleepTimeMill > 60000) {
//                sleepTimeMill = 5000;
//            }
//            synchronized (this) {
//                sleepTimeMill = sleepTimeMill * 2;
//                wait(sleepTimeMill);
//            }
//        } catch (InterruptedException e) {
//        }
//    }

    public void setOnProxyEvent(OnProxyEvent onProxyEvent) {
        this.onProxyEvent = onProxyEvent;
    }

    public interface OnProxyEvent {
        public void onConnected();

        public void onDisConnected();

        public void onReConnect();
    }
}
