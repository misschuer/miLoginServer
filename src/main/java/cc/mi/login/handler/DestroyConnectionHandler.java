package cc.mi.login.handler;

import cc.mi.core.generate.msg.DestroyConnection;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.login.server.LoginContext;
import cc.mi.login.server.LoginServerManager;
import io.netty.channel.Channel;

public class DestroyConnectionHandler extends HandlerImpl {
	
	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		DestroyConnection dc = (DestroyConnection)decoder;
		LoginContext context = (LoginContext) ContextManager.INSTANCE.removeContext(dc.getFd());
		
		if (context != null) {
			if (context.getAccount().isEmpty()) {
				context.onClosed();
			} else {
				LoginServerManager.getInstance().pushLogout(context.getFd(), context.getGuid());
			}
		} else {
			System.out.printf("on_netgd_destory_conn but not found! fd:%u\n", dc.getFd());
		}
//		LoginSystemManager.removeHostInfo(dc.getFd());
	}

}
