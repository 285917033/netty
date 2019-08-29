package com.wbd.nio.decoder;

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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * DelimiterBasedFrameDecoder 客户端
 * @author zgh
 *
 */
public class DelimiterBasedFrameDecoderClient {

	public static void main(String[] args) {
	
		new DelimiterBasedFrameDecoderClient().connect("localhost", 8080);

	}
	
	public void connect(String host,int port) {
		
		//配置nio客户端线程组，
		
		EventLoopGroup socketGroup =  new NioEventLoopGroup();
		
		//创建client端启动辅助类
		Bootstrap client = new Bootstrap();
		
		client.group(socketGroup).option(ChannelOption.TCP_NODELAY, true).handler(new LoggingHandler(LogLevel.INFO)).handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, delimiter));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new ChannelHandlerAdapter() {
					
					int counter;
					String req = "hi zhu guang he  welcome to netty.$_";
					
					@Override
					public void channelActive(ChannelHandlerContext ctx) throws Exception {
						
						for(int i =0;i<10;i++) {
							ctx.writeAndFlush(Unpooled.copiedBuffer(req.getBytes()));
						}
					}
					
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						System.out.println("this is "+ ++counter+" the client recevie content is:["+msg+"]");
					}
					
					
					@Override
					public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
						ctx.flush();
					}
					
					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
					cause.printStackTrace();
					ctx.close();
					}
				});
				
			}
		}).channel(NioSocketChannel.class);
		
		try {
			ChannelFuture future = client.connect(host, port).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			socketGroup.shutdownGracefully();  //grace优雅
		}
		
	}

}
