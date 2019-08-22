package com.wbd.nio.netty.half;

import io.netty.bootstrap.Bootstrap;
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
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 采用netty 解决tcp粘包问题， LineBasedFrameDecoder ,StringDecoder
 * 
 * @author zgh
 *
 */
public class TimeClientHalf {

	public static void main(String[] args) {

		new TimeClientHalf().connection("127.0.0.1", 8080);
	}

	public void connection(String host, int port) {

		// 配置客户端nio线程组， 客户端用来处理读取IO

		EventLoopGroup group = new NioEventLoopGroup();

		// 客户端的netty的启动类
		Bootstrap start = new Bootstrap();

		start.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {

					@Override
					protected void initChannel(SocketChannel ch) throws Exception {

						// 添加linebasedframedecoder,Stringdecoder

						ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
						ch.pipeline().addLast(new StringDecoder());
						ch.pipeline().addLast(new ChannelHandlerAdapter() {

							int counter;
							
							


							//客户端连接服务端成功之后， 调用该方法， 想服务端发送消息
							@Override
							public void channelActive(ChannelHandlerContext ctx) throws Exception {
								
								
								byte[] req = ("query time order"+System.getProperty("line.separator")).getBytes();

								ByteBuf message = null;
								for(int i=0;i<100;i++) {
									message = Unpooled.buffer(req.length);
									message.writeBytes(req);
									ctx.writeAndFlush(message);
								}
								
							}

							//服务器向客户端发送应答后调用该方法， 客户端读取信息，进行处理
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

								
								String body = (String) msg;
								
								System.out.println("now is :"+body+"; the counter is:"+ ++counter);
								
							}

							@Override
							public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
								cause.printStackTrace();
								ctx.close();
							}

						});
					}
				});

		// 发起异步连接操作
		try {
			ChannelFuture future = start.connect(host, port).sync();
			
			System.out.println("client start....");
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

}
