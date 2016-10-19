package com.snail.dnslb4j;

import com.snail.dnslb4j.util.BitSet;
import com.snail.dnslb4j.util.Cfg;
import com.snail.dnslb4j.util.Log;
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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.IDN;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import com.snail.dnslb4j.util.RequestSuccessCallback;
import com.snail.dnslb4j.util.RequestTimeoutCallback;
import java.util.ArrayList;
import java.util.Map;
import jodd.util.ThreadUtil;

/**
 *
 * @author pengmeng
 */
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
							request(buildQuery(Cfg.config("check_domain"), id), hostname, Integer.valueOf(port), timeout, null, (Channel ch, DatagramPacket requestPacket, Integer timeout1) -> {
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
								id = RNADOM.nextInt() & 0XFF;
							}
							request(buildQuery(Cfg.config("check_domain"), id), hostname, Integer.valueOf(port), timeout, null, (Channel ch, DatagramPacket requestPacket, Integer timeout1) -> {
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
							succcessCallback.onMessage(ctx1, packet1, newPacket);
						}
						ctx1.channel().close();
						ctx1.close();
					}
				});
			Channel ch = bootstrap.bind(0).sync().channel();
			Log.logger().debug("request to ->" + hostname + ":" + port + ".");
			ch.writeAndFlush(newPacket).sync();
			if (!ch.closeFuture().await(timeout)) {
				if (timeoutCallback != null) {
					timeoutCallback.onTimeout(ch, newPacket, timeout);
				}
				Log.logger().debug("request timeout (" + timeout + "ms)->" + hostname + ":" + port + ".");
			}
		} catch (InterruptedException ex) {
			Log.logger().error("DnsNodeManager.request", ex);
		} finally {
			group.shutdownGracefully();
		}
	}

	public static ByteBuf buildQuery(String domain, int id) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		DataOutputStream dos = new DataOutputStream(baos);
		BitSet bits = new BitSet();
		bits.set(8);
		try {
			dos.writeShort((short) id);
			dos.writeShort((short) bits.value());
			dos.writeShort(1);
			dos.writeShort(0);
			dos.writeShort(0);
			dos.writeShort(0);
			dos.flush();
			writeQuestion(baos, domain);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		return Unpooled.copiedBuffer(baos.toByteArray());
	}

	private static void writeDomain(OutputStream out, String domain) throws IOException {
		for (String s : domain.split("[.\u3002\uFF0E\uFF61]")) {
			byte[] buffer = IDN.toASCII(s).getBytes();
			out.write(buffer.length);
			out.write(buffer, 0, buffer.length); // ?
		}
		out.write(0);
	}

	private static void writeQuestion(OutputStream out, String domain) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		writeDomain(out, domain);
		dos.writeShort(1);
		dos.writeShort(1);
	}

	public static ArrayList<ConcurrentHashMap<String, String>> getNodeList() {

		ArrayList<ConcurrentHashMap<String, String>> list = new ArrayList<>();

		for (Map.Entry<String, ConcurrentHashMap> entry : backend.entrySet()) {
			String key = entry.getKey();
			ConcurrentHashMap value = entry.getValue();
			if (value.get("status").equals(STATUS_ONLINE)) {
				list.add(value);
			}
		}
		if (list.isEmpty()) {
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
			Log.logger().warn("using backend");
		}

		return list;
	}
}
