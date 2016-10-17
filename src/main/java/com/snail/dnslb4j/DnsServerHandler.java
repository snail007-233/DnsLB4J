package com.snail.dnslb4j;

import com.snail.dnslb4j.util.Cfg;
import com.snail.dnslb4j.util.Log;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;

public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private ChannelHandlerContext ctx0 = null;
	private DatagramPacket packet0 = null;

	@Override

	public void messageReceived(ChannelHandlerContext ctx0, DatagramPacket packet0) throws InterruptedException {
		this.ctx0 = ctx0;
		this.packet0 = packet0;
		Log.logger().info("request from client <-" + packet0.sender().getAddress().getHostAddress() + ":" + packet0.sender().getPort());
		dispatch();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	public void dispatch() {
		Integer timeout = Cfg.configInt("check_timeout");
		ConcurrentHashMap<String, Boolean> reply = new ConcurrentHashMap();
		reply.put("reply", Boolean.FALSE);
		DnsNodeManager.getNodeList().stream().forEach(dnsServer -> {
			String hostname = dnsServer.get("hostname");
			Integer port = Integer.valueOf(dnsServer.get("port"));
			DatagramPacket srcPacket = packet0.copy();
			Thread t = new Thread(() -> {
				DnsNodeManager.request(srcPacket.content(), hostname, port, timeout, (ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket) -> {
					if (!reply.get("reply")) {
						reply.put("reply", Boolean.TRUE);
						DatagramPacket newPacket = new DatagramPacket(responsePacket.content().copy(), srcPacket.sender());
						ctx0.writeAndFlush(newPacket);
						Log.logger().info("response to client ->" + srcPacket.sender());
					}
				}, null);
			});
			t.start();
		});
	}
}
