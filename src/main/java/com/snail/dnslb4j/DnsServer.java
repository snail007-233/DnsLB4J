package com.snail.dnslb4j;

import com.snail.dnslb4j.util.Cfg;
import com.snail.dnslb4j.util.Log;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public final class DnsServer {
	
	public static void main(String[] args) throws Exception {
		if (args.length == 1) {
			Cfg.setPrePath(args[0]);
		} else {
			Cfg.setPrePath("development");
		}
		DnsNodeManager.init();
		io.netty.util.internal.logging.InternalLoggerFactory.setDefaultFactory(new io.netty.util.internal.logging.JdkLoggerFactory());
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			String hostname = Cfg.config("listen_ip");
			int port = Cfg.configInt("listen_port");
			Log.logger().info("listen on " + hostname + ":" + String.valueOf(port));
			Bootstrap b = new Bootstrap();
			b.group(group)
				.channel(NioDatagramChannel.class)
				.handler(new DnsServerHandler());
			b.bind(hostname, port).sync().channel().closeFuture().await();
		} finally {
			group.shutdownGracefully();
		}
	}
}
