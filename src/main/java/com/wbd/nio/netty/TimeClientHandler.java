package com.wbd.nio.netty;

import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeClientHandler extends ChannelHandlerAdapter {
	private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());

	private  ByteBuf firstMessage;
	

	public TimeClientHandler() {
		byte[] req = "query time order".getBytes();
		//初始化bytebuf的长度
		firstMessage = Unpooled.buffer(req.length);
		//把需要发送的消息放入bytebuf中
		firstMessage.writeBytes(req);
	}
	
	/**
	 * 当客户端和服务端tcp建立成功之后，netty的nio线程会调用channelActive方法，发送查询时间的指令给服务端，调用
	 * channelhandlerContext的writandflush方法将请求消息发送给服务端
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(firstMessage);
	}
	
	
	/**
	 * 当服务端返回应答消息时， 客户端的channelRead方法被调用， 从netty的bytebuf中读取并打印应答消息
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//将服务器返回的应答消息进行转换
		ByteBuf byteBuf = (ByteBuf)msg;
		
		byte[] result = new byte[byteBuf.readableBytes()];
		
		byteBuf.readBytes(result);
		String body = new String(result,"utf-8");
		
		System.out.println("client out  now is :"+body);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		//释放资源
		logger.warning(cause.getMessage());
		ctx.close();
	}
}
