package com.snail.dnslb4j.util;

import com.snail.dnslb4j.dns.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Map;
import jodd.util.ThreadUtil;

public class DnsNodeManager {

	private static ConcurrentHashMap<String, ConcurrentHashMap> backend = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, ConcurrentHashMap> backup = new ConcurrentHashMap<>();
	private static final Random RNADOM = new Random();
	private static final String STATUS_ONLINE = "1";
	private static final String STATUS_OFFLINE = "2";

	public static void init() {
		Cfg.getBackendDns().stream().forEach((ConcurrentHashMap<String, String> node) -> {
			String hostname = node.get("hostname");
			String port = node.get("port");
			String key = hostname + ":" + port;
			Integer timeout = Cfg.configInt("check_timeout");
			node.put("status", STATUS_ONLINE);
			backend.put(key, node);
			//后端健康检查
			Thread t = new Thread(() -> {
				while (true) {
					try {
						ArrayList errorCount = new ArrayList();
						for (int i = 0; i < Cfg.configInt("check_count"); i++) {
							int id;
							synchronized (RNADOM) {
								id = RNADOM.nextInt() & 0XFF;
							}
							request(Unpooled.copiedBuffer(new Packet().setQuery().id(id).queryDomain(Cfg.config("check_domain")).getBytes()), hostname, Integer.valueOf(port), timeout, (ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket) -> {
								backend.get(key).put("status", STATUS_ONLINE);
							}, (Channel ch, DatagramPacket requestPacket, Integer timeout1) -> {
								errorCount.add(1);
							});
							ThreadUtil.sleep(Cfg.configInt("check_interval"));
						}

						Log.logger().debug("backend checked " + key);

						if (errorCount.size() >= Cfg.configInt("error_count")) {
							backend.get(key).put("status", STATUS_OFFLINE);
							Log.logger().warn("backend offline " + key);
						} else {
							backend.get(key).put("status", STATUS_ONLINE);
							Log.logger().debug("backend online " + key);
						}
					} catch (Exception e) {
					}
					ThreadUtil.sleep(Cfg.configInt("check_interval"));
				}
			});
			t.start();
		});
		Cfg.getBackupDns().stream().forEach((ConcurrentHashMap<String, String> node) -> {
			String hostname = node.get("hostname");
			String port = node.get("port");
			String key = hostname + ":" + port;
			Integer timeout = Cfg.configInt("check_timeout");
			node.put("status", STATUS_ONLINE);
			backup.put(key, node);
			//并发查询
			Thread t = new Thread(() -> {
				while (true) {
					try {
						ArrayList errorCount = new ArrayList();
						for (int i = 0; i < Cfg.configInt("check_count"); i++) {
							int id;
							synchronized (RNADOM) {
								id = RNADOM.nextInt() & 0xFF;
							}
							byte[] packet = new Packet().setQuery().id(id).queryDomain(Cfg.config("check_domain")).getBytes();
							request(Unpooled.copiedBuffer(packet), hostname, Integer.valueOf(port), timeout, (ChannelHandlerContext ctx1, DatagramPacket responsePacket, DatagramPacket requestPacket) -> {
								backup.get(key).put("status", STATUS_ONLINE);
							}, (Channel ch, DatagramPacket requestPacket, Integer timeout1) -> {
								errorCount.add(1);
							});
							ThreadUtil.sleep(Cfg.configInt("check_interval"));
						}

						Log.logger().debug("backup checked " + key);

						if (errorCount.size() >= Cfg.configInt("error_count")) {
							backup.get(key).put("status", STATUS_OFFLINE);
							Log.logger().warn("backup offline " + key);
						} else {
							backup.get(key).put("status", STATUS_ONLINE);
							Log.logger().debug("backup online " + key);
						}
					} catch (Exception e) {
					}
					ThreadUtil.sleep(Cfg.configInt("check_interval"));
				}
			});
			t.start();
		});
	}

	public static void request(ByteBuf packet, String hostname, int port, int timeout, RequestSuccessCallback succcessCallback, RequestTimeoutCallback timeoutCallback) {
		DatagramPacket newPacket = new DatagramPacket(packet, new InetSocketAddress(hostname, port));
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		try {
			bootstrap.group(group)
				.channel(NioDatagramChannel.class)
				.handler(new SimpleChannelInboundHandler<DatagramPacket>() {
					@Override
					protected void messageReceived(ChannelHandlerContext ctx1, DatagramPacket packet1) throws Exception {
						Log.logger().debug("revceived from <-" + packet1.sender().getAddress().getHostAddress() + ":" + packet1.sender().getPort());
						if (succcessCallback != null) {
							succcessCallback.onMessage(ctx1, packet1, newPacket.copy());
						}
						ctx1.channel().close();
						ctx1.close();
					}
				});
			Channel ch = bootstrap.bind(0).sync().channel();
			Log.logger().debug("request to ->" + hostname + ":" + port + ".");
			ch.writeAndFlush(newPacket.copy());//.sync();
			if (!ch.closeFuture().await(timeout)) {
				if (timeoutCallback != null) {
					timeoutCallback.onTimeout(ch, newPacket.copy(), timeout);
				}
				Log.logger().debug("request timeout (" + timeout + "ms)->" + hostname + ":" + port + ".");
			}
		} catch (InterruptedException ex) {
			Log.logger().error("DnsNodeManager.request", ex);
		} finally {
			group.shutdownGracefully();
		}
	}

	public static NodeList getNodeList() {

		ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<>();
		int type = 0;
		for (Map.Entry<String, ConcurrentHashMap> entry : backend.entrySet()) {
			String key = entry.getKey();
			ConcurrentHashMap value = entry.getValue();
			if (value.get("status").equals(STATUS_ONLINE)) {
				list.add(value);
			}
		}
		if (list.isEmpty()) {
			type = NodeList.TYPE_BACKUP;
			Log.logger().warn("using backup");
			for (Map.Entry<String, ConcurrentHashMap> entry : backup.entrySet()) {
				String key = entry.getKey();
				ConcurrentHashMap value = entry.getValue();
				if (value.get("status").equals(STATUS_ONLINE)) {
					list.add(value);
				}
			}
			if (list.isEmpty()) {
				Log.logger().error("backup unavailable");
			}
		} else {
			type=NodeList.TYPE_BACKEND;
			Log.logger().warn("using backend");
		}

		return new NodeList(list, type);
	}

}
