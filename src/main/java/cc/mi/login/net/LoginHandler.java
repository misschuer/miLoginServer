package cc.mi.login.net;

import cc.mi.core.coder.Coder;
import cc.mi.login.system.SystemManager;
import cc.mi.login.task.DealLoginDataTask;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class LoginHandler extends SimpleChannelInboundHandler<Coder> {
	
	public void channelActive(final ChannelHandlerContext ctx) {
		System.out.println("connect to center success");
		SystemManager.setCenterChannel(ctx.channel());
		SystemManager.regToCenter();
	}
	
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
	}
	
	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final Coder coder) throws Exception {
		SystemManager.submitTask(new DealLoginDataTask(ctx.channel(), coder));
	}
	
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("login client inactive");
		ctx.fireChannelInactive();
	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		throwable.printStackTrace();
		ctx.close();
	}
}
