package com.wbd.nio.netty.half;

import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
/**
 * 半包演示,粘包
 * @author zgh
 *
 */
public class TimeClientHandler extends ChannelHandlerAdapter {
	private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());


	
	private int counter;
	
	private byte[] req;
	
	public TimeClientHandler() {
		 req = ("query time order"+System.getProperty("line.separator")).getBytes();
		
		
	}
	
	/**
	 * 当客户端和服务端tcp建立成功之后，netty的nio线程会调用channelActive方法，发送查询时间的指令给服务端，调用
	 * channelhandlerContext的writandflush方法将请求消息发送给服务端
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		
		ByteBuf message = null;
		
		for(int i=0;i<100;i++) {
			message = Unpooled.buffer(req.length);
			message.writeBytes(req);
			ctx.writeAndFlush(message); //每发送一条就刷新一次，保证每条信息被写人到socketchannle中
		}
		
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
		
		System.out.println("client out  now is :"+body+"; the counter is :"+ ++counter);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		//释放资源
		logger.warning(cause.getMessage());
		ctx.close();
	}
}
