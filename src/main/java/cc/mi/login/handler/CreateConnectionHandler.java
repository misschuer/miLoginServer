package cc.mi.login.handler;

import cc.mi.core.coder.Coder;
import cc.mi.core.generate.msg.CreateConnection;
import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.server.ServerContext;
import cc.mi.login.system.SystemManager;
import io.netty.channel.Channel;

public class CreateConnectionHandler extends AbstractHandler {

	@Override
	public void handle(ServerContext player, Channel channel, Coder decoder) {
		CreateConnection coder = (CreateConnection)decoder;
		SystemManager.putHostInfo(coder.getFd(), coder.getRemoteIp(), coder.getRemotePort());
		System.out.printf("on_netgd_create_conn fd:%u, ip:%s, port:%u\n", coder.getFd(), coder.getRemoteIp(), coder.getRemotePort());	
	}

}
