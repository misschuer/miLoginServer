package cc.mi.login.handler;

import cc.mi.core.generate.msg.CheckSession;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import io.netty.channel.Channel;

public class CheckSessionHandler extends HandlerImpl {

	@Override
	public void handle(ServerContext player, Channel channel, Packet decoder) {
		CheckSession packet = (CheckSession) decoder;
		
	}

}
