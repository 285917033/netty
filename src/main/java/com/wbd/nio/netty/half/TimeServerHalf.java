package com.wbd.nio.netty.half;

import java.util.Date;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 采用netty 解决tcp粘包问题， LineBasedFrameDecoder ,StringDecoder
 * 
 * @author zgh
 *
 */
public class TimeServerHalf {

	public static void main(String[] args) {

		new TimeServerHalf().bind(8080);
	}

	public void bind(int port) {

		// 创建两个NIO线程组，一个用来接受socket,一个用来读取网络IO
		EventLoopGroup bossGroup = new NioEventLoopGroup();

		EventLoopGroup workerGroup = new NioEventLoopGroup();

		// 创建netty 启动辅助类
		ServerBootstrap bootstrap = new ServerBootstrap();

		// 设置bootstrap
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {

						// 添加linebasedFrameDecoder解码器
						ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
						// 添加StringDecoder解码器
						ch.pipeline().addLast(new StringDecoder());

						ch.pipeline().addLast(new ChannelHandlerAdapter() {

							int counter;

							// 读取socketchannel的信息
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

								String body = (String) msg;

								System.out.println("server accept order:" + body + ";the counter is:" + ++counter);

								String currentTime = "query time order".equals(body)
										? new Date(System.currentTimeMillis()).toString()
										: "bad order";
										
										currentTime = currentTime+System.getProperty("line.separator");
										//将内容转换成ByteBuf  然后发送到socketchannel中
										ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
										
										ctx.writeAndFlush(resp);
							}

							@Override
							public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
								ctx.close();
							}

						});
					}
				});

		try {
			ChannelFuture f = bootstrap.bind(port).sync();
			System.out.println("服务器启动成功...");
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

}
