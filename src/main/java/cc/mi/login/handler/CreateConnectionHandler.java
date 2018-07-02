package cc.mi.login.handler;

import cc.mi.core.coder.Packet;
import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.server.ServerContext;
import io.netty.channel.Channel;

public class CreateConnectionHandler extends AbstractHandler {

	@Override
	public void handle(ServerContext player, Channel channel, Packet decoder) {
//		CreateConnection coder = (CreateConnection)decoder;
//		LoginSystemManager.putHostInfo(coder.getFd(), coder.getRemoteIp(), coder.getRemotePort());
//		System.out.printf("on_netgd_create_conn fd:%u, ip:%s, port:%u\n", coder.getFd(), coder.getRemoteIp(), coder.getRemotePort());	
	}

}
