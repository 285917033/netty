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
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 特殊字符解码器， DelimiterBasedFrameDecoder
 * 
 * @author zgh
 *
 */
public class DelimiterBasedFrameDecoderServer {

	public static void main(String[] args) {

		new DelimiterBasedFrameDecoderServer().bind(8080);
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
				
				//利用$_分隔符创建 缓冲对象bytebuf 
				ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
				
				//添加分隔符解码器
				
				//1024表示消息的最大长度，当达到这个长度时就通过分隔符进行查找，如果没有找到就抛出TooLongFrameException异常
				//防止由于异常码流缺失分隔符导致的内存溢出，这是netty解码器的可靠性保护，
				//第二个参数是分隔符缓冲对象
				ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
				ch.pipeline().addLast(new StringDecoder());
				ch.pipeline().addLast(new ChannelHandlerAdapter() {
					
					int counter=0;
					
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
					
						String body = (String) msg;
						
						System.out.println("this is "+ ++counter+" server receive client content:["+body+"]");
						
						body +="$_";
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
