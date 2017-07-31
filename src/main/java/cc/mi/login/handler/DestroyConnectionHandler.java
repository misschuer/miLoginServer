package cc.mi.login.handler;

import cc.mi.core.coder.Coder;
import cc.mi.core.generate.msg.DestroyConnection;
import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.login.server.LoginContext;
import cc.mi.login.system.LoginSystemManager;
import io.netty.channel.Channel;

public class DestroyConnectionHandler extends AbstractHandler {
	
	@Override
	public void handle(ServerContext player, Channel channel, Coder decoder) {
		DestroyConnection dc = (DestroyConnection)decoder;
		LoginContext context = (LoginContext) ContextManager.getContext(dc.getFd());
		
		if (context != null) {
			System.out.printf("on_netgd_destory_conn fd:%u,ip:%s,port:%u\n", dc.getFd(),context.getRemoteIp(),context.getRemotePort());
			if (context.getAccount().isEmpty()) {
				context.onClosed();
			} else {
//				g_app->m_login_queue->PushAction(context->GetGuid(), context->GetFD(), CONTEXT_LOGIN_ACTION_CLOSE);
			}
		} else {
			System.out.printf("on_netgd_destory_conn but not found! fd:%u\n", dc.getFd());
		}
		LoginSystemManager.removeHostInfo(dc.getFd());
	}

}
