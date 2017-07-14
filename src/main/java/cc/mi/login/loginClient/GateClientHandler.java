package cc.mi.login.loginClient;

import cc.mi.core.coder.Coder;
import cc.mi.login.system.SystemManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class GateClientHandler extends SimpleChannelInboundHandler<Coder> {
	
	public void channelActive(final ChannelHandlerContext ctx) {
		System.out.println("connect to center success");
		SystemManager.setCenterChannel(ctx.channel());
	}
	
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
	}
	
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Coder coder) throws Exception {
		
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("gate client inactive");
		ctx.fireChannelInactive();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		throwable.printStackTrace();
		ctx.close();
	}
}
