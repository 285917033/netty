package com.wbd.nio.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty client
 * 
 * @author zgh
 *
 */
public class TimeClient {

	public void connection(String host, int port) {
		// 配置客户端nio线程组

		EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
		// 新建启动辅助类
		Bootstrap bootStrap = new Bootstrap();
		// 设置辅助类的基本信息
		bootStrap.group(eventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new TimeClientHandler());

					}
				});

		// 发起异步连接操作，连接服务端
		try {
			ChannelFuture future = bootStrap.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			eventLoopGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) {

		new TimeClient().connection("localhost", 8080);
	}

}
