package cc.mi.login.server;

import java.util.LinkedList;
import java.util.List;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.manager.ServerManager;
import io.netty.channel.Channel;

public class LoginServerManager extends ServerManager {
	private static LoginServerManager instance;
	
	private final List<Channel> channelList = new LinkedList<>();
	
	public static LoginServerManager getInstance() {
		if (instance == null) {
			instance = new LoginServerManager();
		}
		return instance;
	}
	
	public LoginServerManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN);
	}
	
	public void addChannel(Channel channel) {
		channelList.add(channel);
	}
}
