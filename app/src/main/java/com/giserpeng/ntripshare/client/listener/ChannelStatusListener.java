package com.giserpeng.ntripshare.client.listener;

import io.netty.channel.ChannelHandlerContext;

public interface ChannelStatusListener {

    void channelInactive(ChannelHandlerContext ctx);
    void channelActive(ChannelHandlerContext ctx);
}
