package cc.mi.login.handler;

import cc.mi.core.generate.msg.PlayerLogin;
import cc.mi.core.generate.stru.CharInfo;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.login.server.LoginCache;
import cc.mi.login.server.LoginContext;
import cc.mi.login.server.LoginServerManager;
import io.netty.channel.Channel;

public class PlayerLoginHandler extends HandlerImpl {

	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		PlayerLogin packet = (PlayerLogin)decoder;
		
		int fd = packet.getBaseFd();
		LoginContext context = (LoginContext)ContextManager.getContext(fd);
		
		// TODO:判断是否被封号
		
		// 账号是否存在
		if (!LoginCache.INSTANCE.isAccountExist(context.getAccount())) {
			// TODO:操作失败的提示 
			context.closeSession(0);
			return;
		}
		
		CharInfo info = LoginCache.INSTANCE.getCharInfo(context.getAccount());
		if (!info.getGuid().equals(packet.getGuid())) {
			// TODO:操作失败的提示 
			context.closeSession(0);
			return;
		}
		
		context.setGuid(packet.getGuid());
		LoginServerManager.getInstance().pushSession(fd);
	}

}
