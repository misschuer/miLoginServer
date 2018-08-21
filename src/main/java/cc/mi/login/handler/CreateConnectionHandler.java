package cc.mi.login.handler;

import cc.mi.core.generate.msg.CreateConnection;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import io.netty.channel.Channel;

public class CreateConnectionHandler extends HandlerImpl {
	
	static final CustomLogger logger = CustomLogger.getLogger(CreateConnectionHandler.class);
	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		CreateConnection packet = (CreateConnection)decoder;
		logger.devLog("fd = {} ip = {} port = {}", packet.getFd(), packet.getRemoteIp(), packet.getRemotePort());
	}

}
