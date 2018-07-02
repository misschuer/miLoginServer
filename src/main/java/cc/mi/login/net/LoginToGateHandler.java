package cc.mi.login.net;

import cc.mi.core.coder.Packet;
import cc.mi.core.handler.ChannelHandlerGenerator;
import cc.mi.core.log.CustomLogger;
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
	static final CustomLogger logger = CustomLogger.getLogger(LoginToGateHandler.class);
	public void channelActive(final ChannelHandlerContext ctx) {
		logger.devLog("connect to {} success", ctx.channel().remoteAddress());
		LoginServerManager.getInstance().onGateConnected(ctx.channel());
	}
	
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Packet coder) throws Exception {
		
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.devLog("warning!!!! connect to {} fail", ctx.channel().remoteAddress());
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
