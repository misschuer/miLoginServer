package cc.mi.login.net;

import cc.mi.core.handler.ChannelHandlerGenerator;
import cc.mi.core.packet.Packet;
import cc.mi.login.server.LoginServerManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 网关服不会主动发消息到这里
 * @author gy
 *
 */
public class LoginToGateHandler extends SimpleChannelInboundHandler<Packet> implements ChannelHandlerGenerator {
	public void channelActive(final ChannelHandlerContext ctx) {
		LoginServerManager.getInstance().onGateConnected(ctx.channel());
	}
	
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Packet coder) throws Exception {
		//TODO: 这里应该不会有, 我们只做从本服到网关服的单向通信
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		LoginServerManager.getInstance().onGateDisconnected(ctx.channel());
		ctx.fireChannelInactive();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		throwable.printStackTrace();
		ctx.close();
	}

	@Override
	public ChannelHandler newChannelHandler() {
		return new LoginToGateHandler();
	}
}
