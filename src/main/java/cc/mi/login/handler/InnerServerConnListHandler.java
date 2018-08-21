package cc.mi.login.handler;

import java.util.List;

import cc.mi.core.generate.msg.InnerServerConnList;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import cc.mi.login.server.LoginServerManager;
import io.netty.channel.Channel;

public class InnerServerConnListHandler extends HandlerImpl {

	@Override
	public void handle(ServerContext nil, Channel channel, Packet decoder) {
		InnerServerConnList isc = (InnerServerConnList) decoder;
		
		int appConn    = isc.getAppConn();
		int loginConn  = isc.getLoginConn();
		int recordConn = isc.getRecordConn();
		List<Integer> sceneConns = isc.getSceneConns();
		
		LoginServerManager.getInstance().getConnList().setAppConn(appConn);
		LoginServerManager.getInstance().getConnList().setLoginConn(loginConn);
		LoginServerManager.getInstance().getConnList().setRecordConn(recordConn);
		LoginServerManager.getInstance().getConnList().setSceneConns(sceneConns);
	}

}
