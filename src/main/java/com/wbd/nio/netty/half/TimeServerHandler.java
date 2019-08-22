package com.wbd.nio.netty.half;

import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 半包演示，粘包
 *  用来处理网络io事件读写操作，例如，记录日志， 对消息进行编码等等，一般我们只关注channeread，和exceptionCaught方法
 * 
 * @author zgh
 *
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

	private int counter;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// 将msg转换为netty的bytebuf, 通过它的readablebytes方法可以获取缓冲区可读的字节数， 更加可读的字节数创建byte数组
		// 通过它的readBytes方法将缓冲区的字节数组复制到新建的byte数组中
		ByteBuf buf = (ByteBuf) msg; // msg为读取到SocketChannel的内容
		byte[] req = new byte[buf.readableBytes()];
		buf.readBytes(req); // 将buf中读取到的内容放入新建的byte数组中
		String body = new String(req, "utf-8").substring(0, req.length - System.getProperty("line.separator").length());
		System.out.println("服务端读取客户端的内容为：" + body+";the count is :"+ ++counter);
		String currentTime = "query time order".equals(body) ? new Date(System.currentTimeMillis()).toString()
				: "bad order";
		currentTime = currentTime+System.getProperty("line.separator");
		// 服务器给客户端的响应
		ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());

		ctx.write(resp);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// flush方法作用是将消息发送到队列的写入到socketchannel中，发送给对方，从性能上考虑，为了防止频繁唤醒selector进行消息发送，
		// netty的write方法并不会直接将消息写入socketchannel中，调用write方法只是把待发送的消息发送到缓冲数组中，
		// 再通过调用flush方法
		// 将发送到缓冲区的消息全部写到socketchannel中
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.close();
	}
}
