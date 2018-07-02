package cc.mi.login.handler;

import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import io.netty.channel.Channel;

public class DestroyConnectionHandler extends AbstractHandler {
	
	@Override
	public void handle(ServerContext player, Channel channel, Packet decoder) {
//		DestroyConnection dc = (DestroyConnection)decoder;
//		LoginContext context = (LoginContext) ContextManager.getContext(dc.getFd());
//		
//		if (context != null) {
//			System.out.printf("on_netgd_destory_conn fd:%u,ip:%s,port:%u\n", dc.getFd(),context.getRemoteIp(),context.getRemotePort());
//			if (context.getAccount().isEmpty()) {
//				context.onClosed();
//			} else {
////				g_app->m_login_queue->PushAction(context->GetGuid(), context->GetFD(), CONTEXT_LOGIN_ACTION_CLOSE);
//			}
//		} else {
//			System.out.printf("on_netgd_destory_conn but not found! fd:%u\n", dc.getFd());
//		}
//		LoginSystemManager.removeHostInfo(dc.getFd());
	}

}
