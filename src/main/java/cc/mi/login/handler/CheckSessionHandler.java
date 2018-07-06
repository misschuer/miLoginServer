package cc.mi.login.handler;

import java.util.HashMap;
import java.util.Map;

import cc.mi.core.generate.msg.CheckSession;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.login.server.LoginContext;
import io.netty.channel.Channel;

public class CheckSessionHandler extends HandlerImpl {
	
	final static CustomLogger logger = CustomLogger.getLogger(CheckSessionHandler.class);
	
	@Override
	public void handle(ServerContext player, Channel channel, Packet decoder) {
		CheckSession packet = (CheckSession) decoder;
		int fd = packet.getFD();
		
		
		// 解析packet.getSessionkey()
		Map<String, String> params = new HashMap<String, String>();
		
		// 判断重复checksession
		LoginContext loginContext = (LoginContext) ContextManager.getContext(fd);
		if (loginContext != null) {
			logger.warnLog("CheckSessionHandler duplicate for fd = {}", fd);
			return;
		}
		
		// 新进来的
		if (loginContext == null) {
			loginContext = new LoginContext(fd);
			ContextManager.pushContext(loginContext);
		}
		
		if (loginContext.checkSession(params)) {
			// 看看需要进行什么操作
		} else {
			loginContext.closeSession(0);
		}
	}

}
