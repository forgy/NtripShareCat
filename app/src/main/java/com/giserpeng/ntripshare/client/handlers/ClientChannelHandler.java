package com.giserpeng.ntripshare.client.handlers;

import android.util.Log;

import com.giserpeng.ntripshare.client.ClientChannelMannager;
import com.giserpeng.ntripshare.client.ProxyClientContainer;
import com.giserpeng.ntripshare.client.listener.ChannelStatusListener;
import com.giserpeng.ntripshare.client.listener.ProxyChannelBorrowListener;
import com.giserpeng.ntripshare.ntrip.Clients.Client;
import com.giserpeng.ntripshare.ntrip.Servers.NtripCaster;
import com.giserpeng.ntripshare.ntrip.Tools.HttpRequestParser;
import com.giserpeng.ntripshare.protocol.Constants;
import com.giserpeng.ntripshare.protocol.ProxyMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengfei
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<ProxyMessage> {

    private static Logger logger = LoggerFactory.getLogger(ClientChannelHandler.class);
    private Bootstrap bootstrap;
    private Bootstrap proxyBootstrap;

    private ChannelStatusListener channelStatusListener;

    public ClientChannelHandler(Bootstrap bootstrap, Bootstrap proxyBootstrap, ChannelStatusListener channelStatusListener) {
        this.bootstrap = bootstrap;
        this.proxyBootstrap = proxyBootstrap;
        this.channelStatusListener = channelStatusListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ProxyMessage proxyMessage) throws Exception {
        logger.debug("recieved proxy message, type is {}", proxyMessage.getType());
        switch (proxyMessage.getType()) {
            case ProxyMessage.C_TYPE_AUTH:
                ProxyClientContainer.getInstance().channelActive(ctx);
                break;
            case ProxyMessage.TYPE_CONNECT:
                handleConnectMessage(ctx, proxyMessage);
                break;
            case ProxyMessage.TYPE_DISCONNECT:
                handleDisconnectMessage(ctx, proxyMessage);
                break;
            case ProxyMessage.P_TYPE_TRANSFER:
                handleTransferMessage(ctx, proxyMessage);
                break;
            default:
                break;
        }
    }

    private void handleTransferMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        final Channel cmdChannel = ctx.channel();
        final String userId = proxyMessage.getUri();
        Log.i("ClientChannelHandler", userId + "handleTransferMessage");
        try {
            Client client = NtripCaster.getInstance().getReferenceStation().getClientByUserId(userId);
            if (client == null) {
                String request = new String(proxyMessage.getData());
                Log.i("ClientChannelHandler", userId + request);
                HttpRequestParser httpParser = new HttpRequestParser(request);
                client = new Client(ClientChannelMannager.getRealServerChannel(userId), httpParser, NtripCaster.getInstance(), userId);
                NtripCaster.getInstance().clientAuthorizationProcessing(client);
            } else {
                client.read(proxyMessage.getData());
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
           Channel channel =  ClientChannelMannager.getRealServerChannel(userId);
           if(channel!= null){
               ProxyMessage proxyMessage2 = new ProxyMessage();
               proxyMessage2.setType(ProxyMessage.P_TYPE_TRANSFER);
               proxyMessage2.setUri(userId);
               proxyMessage2.setData("ERROR - Bad Password\r\n".getBytes());
               channel.writeAndFlush(proxyMessage);
               ProxyMessage proxyMessage3 = new ProxyMessage();
               proxyMessage3.setType(ProxyMessage.TYPE_DISCONNECT);
               proxyMessage3.setUri(userId);
               channel.writeAndFlush(proxyMessage);
           }
        }
//        Channel realServerChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
//        if (realServerChannel != null) {
//            ByteBuf buf = ctx.alloc().buffer(proxyMessage.getData().length);
//            buf.writeBytes(proxyMessage.getData());
//            logger.debug("write data to real server, {}", realServerChannel);
//            realServerChannel.writeAndFlush(buf);
//        }
    }

    private void handleDisconnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        final Channel cmdChannel = ctx.channel();
        final String userId = proxyMessage.getUri();
        Log.i("ClientChannelHandler", userId + "handleDisconnectMessage");
        NtripCaster.getInstance().getReferenceStation().removeClientByUserId(userId);
//        ProxyMessage proxyMessage2 = new ProxyMessage();
//        proxyMessage2.setType(ProxyMessage.TYPE_DISCONNECT);
//        proxyMessage2.setUri(userId);
//        cmdChannel.writeAndFlush(proxyMessage2);
//        Channel realServerChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
//        logger.debug("handleDisconnectMessage, {}", realServerChannel);
//        if (realServerChannel != null) {
//            ctx.channel().attr(Constants.NEXT_CHANNEL).remove();

            ClientChannelMannager.returnProxyChanel(ClientChannelMannager.getRealServerChannel(userId));
//            realServerChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//        }
    }

    private void handleConnectMessage(ChannelHandlerContext ctx, ProxyMessage proxyMessage) {
        final Channel cmdChannel = ctx.channel();
        final String userId = proxyMessage.getUri();
        Log.i("ClientChannelHandler", userId + "handleConnectMessage");
        ProxyMessage proxyMessage2 = new ProxyMessage();
        proxyMessage2.setType(ProxyMessage.TYPE_DISCONNECT);
        proxyMessage2.setUri(userId + "@" + ProxyClientContainer.getInstance().Key);
        cmdChannel.writeAndFlush(proxyMessage2);
        try{
            ClientChannelMannager.borrowProxyChanel(ProxyClientContainer.getInstance().ServerIP, ProxyClientContainer.getInstance().ServerPort,
                    proxyBootstrap, new ProxyChannelBorrowListener() {
                        @Override
                        public void success(Channel channel) {
                            // 远程绑定
                            ProxyMessage proxyMessage = new ProxyMessage();
                            proxyMessage.setType(ProxyMessage.TYPE_CONNECT);
                            proxyMessage.setUri(userId + "@" + ProxyClientContainer.getInstance().Key);
                            channel.writeAndFlush(proxyMessage);
                            ClientChannelMannager.addRealServerChannel(userId, channel);
                            ClientChannelMannager.setRealServerChannelUserId(channel, userId);
                            Log.i("ClientChannelHandler", userId + "handleConnectMessage success");
                        }

                        @Override
                        public void error(Throwable cause) {
                            ProxyMessage proxyMessage = new ProxyMessage();
                            proxyMessage.setType(ProxyMessage.TYPE_DISCONNECT);
                            proxyMessage.setUri(userId);
                            cmdChannel.writeAndFlush(proxyMessage);
                            Log.i("ClientChannelHandler", userId + "handleConnectMessage error");
                        }
                    });
        }catch (Exception e){

        }

    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        Channel realServerChannel = ctx.channel().attr(Constants.NEXT_CHANNEL).get();
        if (realServerChannel != null) {
            realServerChannel.config().setOption(ChannelOption.AUTO_READ, ctx.channel().isWritable());
        }

        super.channelWritabilityChanged(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 控制连接
        if (ClientChannelMannager.getCmdChannel() == ctx.channel()) {
            ClientChannelMannager.setCmdChannel(null);
            ClientChannelMannager.clearRealServerChannels();
            channelStatusListener.channelInactive(ctx);
        } else {
            // 数据传输连接
            Channel realServerChannel = ctx.channel();
            if (realServerChannel != null && realServerChannel.isActive()) {
                realServerChannel.close();
            }
            try{
                String userid = ClientChannelMannager.getRealServerChannelUserId(realServerChannel);
                if(userid == null){
                    NtripCaster.getInstance().getReferenceStation().removeClientByUserId(userid);
                }
            }catch (Exception e){
            }
        }

        ClientChannelMannager.removeProxyChanel(ctx.channel());
        super.channelInactive(ctx);
        ProxyClientContainer.getInstance().channelInactive(null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("exception caught", cause);
        super.exceptionCaught(ctx, cause);
    }

}