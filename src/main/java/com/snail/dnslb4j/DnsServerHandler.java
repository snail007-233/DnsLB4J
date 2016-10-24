package com.snail.dnslb4j;

import com.snail.dnslb4j.dns.DnsMessage;
import com.snail.dnslb4j.dns.query.ParseException;
import com.snail.dnslb4j.dns.query.Parser;
import com.snail.dnslb4j.dns.query.Question;
import com.snail.dnslb4j.dns.response.Record;
import com.snail.dnslb4j.util.Cache;
import com.snail.dnslb4j.util.DnsNodeManager;
import com.snail.dnslb4j.util.Cfg;
import com.snail.dnslb4j.util.Log;
import com.snail.dnslb4j.util.Misc;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import jodd.exception.ExceptionUtil;

public class DnsServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	private ChannelHandlerContext ctx0 = null;
	private DatagramPacket packet0 = null;

	@Override

	public void messageReceived(ChannelHandlerContext ctx0, DatagramPacket packet0) throws InterruptedException, IOException, ParseException {
		this.ctx0 = ctx0;
		this.packet0 = packet0.copy();
		Question question = Question.parse(new Parser(Misc.byteBuf2bytes(packet0.copy().content()), 12));
		Log.logger().info("revceived from <-" + packet0.sender().getAddress().getHostAddress() + ":" + packet0.sender().getPort()
			+ "[" + question.getQueryType() + ":" + question.getQueryClass() + "] " + question.getDomainName());
//		if (question.getQueryType().toString().equals("A")) {
//			String cacheKey = question.getDomainName().toString();
//			String ip = Cache.get(cacheKey);
//			if (!ip.isEmpty()) {
//				Parser parser = new Parser(Misc.byteBuf2bytes(packet0.copy().content()), 0);
//				int id = parser.parseUnsignedShort();
//				Log.logger().debug("reply to -> " + packet0.sender() + " [ from cache ]");
//				DatagramPacket responsePacket = DnsMessage.buildResponse(ip, id);
//				ctx0.writeAndFlush(responsePacket);
//			}
//		}
		Log.logger().debug("request packet :" +Misc.bytes2BitString(Misc.byteBuf2bytes(packet0.copy().content())));
		dispatch();
	}

	public void dispatch() {
		Integer timeout = Cfg.configInt("check_timeout");
		ConcurrentHashMap<String, Boolean> reply = new ConcurrentHashMap();
		reply.put("reply", Boolean.FALSE);
		DnsNodeManager.getNodeList().stream().forEach((ConcurrentHashMap<String, String> dnsServer) -> {
			String hostname = dnsServer.get("hostname");
			Integer port = Integer.valueOf(dnsServer.get("port"));
			DatagramPacket srcPacket = packet0.copy();
			Thread t = new Thread(() -> {
				DnsNodeManager.request(srcPacket.content(), hostname, port, timeout, (ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket) -> {
					if (!reply.get("reply")) {
						reply.put("reply", Boolean.TRUE);
						DatagramPacket newPacket = new DatagramPacket(responsePacket.content().copy(), srcPacket.sender());
						ctx0.writeAndFlush(newPacket);
						Question question;
						try {
							byte[] rawRequestBytes = Misc.byteBuf2bytes(requestPacket.copy().content().copy());
							byte[] rawResponseBytes = Misc.byteBuf2bytes(responsePacket.copy().content().copy());
							question = Question.parse(new Parser(rawRequestBytes, 12));
							Record[] records = DnsMessage.parseResponse(rawResponseBytes);
							StringBuilder sb = new StringBuilder();
							String ip = "";
							for (Record record : records) {
								sb.append(record.value + " (" + record.type + ") , ");
								if (record.isA()) {
									ip = record.value;
								}
							}
							Log.logger().debug("reply packet :" + Misc.bytes2BitString(Misc.byteBuf2bytes(responsePacket.copy().content())));

							Log.logger().info("reply to ->" + srcPacket.sender() + " " + question.getDomainName().toString() + " -> " + sb.toString());
							if (!ip.isEmpty()) {
								String cacheKey = question.getDomainName().toString();
								Cache.set(cacheKey, ip);
							}
						} catch (ParseException | IOException ex) {
							Log.logger().info("reply to ->" + srcPacket.sender());
							Log.logger().warn(ExceptionUtil.exceptionChainToString(ex));
						}
					}
				}, null);
			});
			t.start();
		});
	}
}
