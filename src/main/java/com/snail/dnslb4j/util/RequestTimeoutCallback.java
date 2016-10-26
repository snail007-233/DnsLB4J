package com.snail.dnslb4j.util;

import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramPacket;

public interface RequestTimeoutCallback {

	public void onTimeout(Channel ch, DatagramPacket requestPacket,Integer timeout);
}
