package com.snail.dnslb4j;

import com.snail.dnslb4j.dns.Packet;
import com.snail.dnslb4j.dns.Record;
import com.snail.dnslb4j.util.Cache;
import com.snail.dnslb4j.util.DnsNodeManager;
import com.snail.dnslb4j.util.Cfg;
import com.snail.dnslb4j.util.Log;
import com.snail.dnslb4j.util.Misc;
import com.snail.dnslb4j.util.NodeList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.util.concurrent.ConcurrentHashMap;

public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private ChannelHandlerContext ctx0 = null;
	private DatagramPacket packet0 = null;

	@Override

	public void messageReceived(ChannelHandlerContext ctx0, DatagramPacket packet0) {
		this.ctx0 = ctx0;
		this.packet0 = packet0.copy();
		Packet requestPacket = new Packet(Misc.byteBuf2bytes(packet0.copy().content()));
		Log.logger().info("revceived from <-" + packet0.sender().getAddress().getHostAddress() + ":" + packet0.sender().getPort()
			+ "[" + requestPacket.queryType() + ":" + requestPacket.queryClass() + "] " + requestPacket.queryDomain());
		if (requestPacket.queryType() == Packet.QUERY_TYPE_A) {
			String cacheKey = requestPacket.queryDomain();
			String ip = Cache.get(cacheKey);
			if (!ip.isEmpty()) {
				Log.logger().info("reply to -> " + packet0.sender() + " [ from cache ] (" + requestPacket.queryDomain() + "->" + ip + ")");
				Packet cachePacket = new Packet().setAnswer();
				cachePacket.id(requestPacket.id())
					.queryDomain(requestPacket.queryDomain())
					.answer(ip, Cfg.configInt("ttl_min_seconds"));
				DatagramPacket responsePacket = new DatagramPacket(Misc.bytes2ByteBuf(cachePacket.getBytes()), packet0.sender());
				ctx0.writeAndFlush(responsePacket);
				return;
			}
		}
		dispatch();
	}

	public void dispatch() {
		Integer timeout = Cfg.configInt("check_timeout");
		ConcurrentHashMap<String, Boolean> reply = new ConcurrentHashMap();
		reply.put("reply", Boolean.FALSE);
		NodeList nodeList = DnsNodeManager.getNodeList();
		nodeList.getList().stream().forEach((ConcurrentHashMap<String, String> dnsServer) -> {
			String hostname = dnsServer.get("hostname");
			Integer port = Integer.valueOf(dnsServer.get("port"));
			DatagramPacket srcPacket = packet0.copy();
			Thread t = new Thread(() -> {
				DnsNodeManager.request(srcPacket.content(), hostname, port, timeout, (ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket) -> {
					if (!reply.get("reply")) {
						reply.put("reply", Boolean.TRUE);
						DatagramPacket newPacket = new DatagramPacket(responsePacket.content().copy(), srcPacket.sender());
						ctx0.writeAndFlush(newPacket);
						Packet responsePacket1 = new Packet(Misc.byteBuf2bytes(responsePacket.copy().content()));
						Record record = null;
						for (Record record0 : responsePacket1.answers()) {
							if (record0.isA()) {
								record = record0;
							}
						}
						Log.logger().info("reply to ->" + srcPacket.sender() + " " + responsePacket1.queryDomain() + " -> " + (record != null ? record.value : ""));
						if (record != null) {
							boolean cacheBackend = nodeList.getTYPE() == NodeList.TYPE_BACKEND && Cfg.configBoolean("cache_backend");
							boolean cacheBackup = nodeList.getTYPE() == NodeList.TYPE_BACKUP && Cfg.configBoolean("cache_backup");
							if (cacheBackend || cacheBackup) {
								String cacheKey = responsePacket1.queryDomain();
								int ttl = record.ttl;
								int minTtl = Cfg.configInt("ttl_min_seconds");
								ttl = ttl > minTtl ? minTtl : minTtl;
								Cache.set(cacheKey, record.value, ttl);
							}
						}
					}
				}, null);
			});
			t.start();
		});
	}
}
