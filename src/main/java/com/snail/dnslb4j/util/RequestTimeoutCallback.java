/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.snail.dnslb4j.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

/**
 *
 * @author pengmeng
 */
public interface RequestTimeoutCallback {

	public void onTimeout(Channel ch, DatagramPacket requestPacket,Integer timeout);
}
