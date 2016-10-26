package com.snail.dnslb4j.util;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;


public interface RequestSuccessCallback {

	public void onMessage(ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket);
}
