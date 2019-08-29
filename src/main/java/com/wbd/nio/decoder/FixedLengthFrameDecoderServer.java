package com.wbd.nio.decoder;

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
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 固定长度解码器， FixedLengthFrameDecoder
 * 
 * @author zgh
 *
 */
public class FixedLengthFrameDecoderServer {

	public static void main(String[] args) {

		new FixedLengthFrameDecoderServer().bind(8080);
	}

	public void bind(int port) {
		
		//用来接收socket的nio线程组
		EventLoopGroup acceptSocket = new NioEventLoopGroup();
		
		//用来对socketChannel io进行读写
		
		EventLoopGroup socketGroup = new NioEventLoopGroup();
		
		//创建服务端启动辅助类
		ServerBootstrap server = new ServerBootstrap();
		
		//设置server 辅助启动类
		
		server.group(acceptSocket, socketGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 100).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

			//为channel添加解码器
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				
			
				ch.pipeline().addLast(new FixedLengthFrameDecoder(25));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new ChannelHandlerAdapter() {
					
					int counter=0;
					
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					
						String body = (String) msg;
						
						System.out.println("this is "+ ++counter+" server receive client content:["+body+"]");
						
						
						ByteBuf resp = Unpooled.copiedBuffer(body.getBytes());
						
						ctx.writeAndFlush(resp);
					}
					
					
					@Override
					public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
						
						cause.printStackTrace();
						ctx.close();
					}
					
				});
				
			}
		});
		
		try {
			ChannelFuture future = server.bind(port).sync();
			future.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			acceptSocket.shutdownGracefully();
			socketGroup.shutdownGracefully();
		}
		
	}

}
