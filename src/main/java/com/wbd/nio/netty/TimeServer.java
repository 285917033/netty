package com.wbd.nio.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty server端
 * 
 * Future模式是多线程开发中非常常见的一种设计模式。它的核心思想是异步调用。当我们需要调用一个函数方法时。如果这个函数执行很慢,那么我们就要进行等待。但有时候,我们可能并不急着要结果。因此,我们可以让被调用者立即返回,让他在后台慢慢处理这个请求。对于调用者来说,则可以先处理一些其他任务,在真正需要数据的场合再去尝试获取需要的数据。
 * 
 * @author zgh
 *
 */
public class TimeServer {

	public void bind(int port) {
		// 配置服务器端的NIO线程组,bossGroup用与服务器端接受客户端的连接
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		// workerGroup 用来进行SocketChannel的网络读写
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		// serverBootStrap是netty用于启动nio服务端的辅助启动类，目的是降低服务端的开发复杂度
		ServerBootstrap serverBoostrap = new ServerBootstrap();
		// 将两个nio线程组当做参数传递到ServerBoostrap中，接着设置channel为NioServerSocketChannel,
		// 然后配置NioServerSocketChannel的tcp参数，最后绑定I/O事件的处理类ChildChannelHandler,
		serverBoostrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChildChannelHandler());

		// 绑定端口，同步等待成功
		try {
			ChannelFuture future = serverBoostrap.bind(port).sync();

			// 等待服务端监听端口关闭
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new TimeServerHandler());
		}

	}

}
