package cc.mi.login.server;

import cc.mi.core.coder.Coder;
import cc.mi.core.server.ServerContext;
import cc.mi.login.system.SystemManager;

public class LoginContext extends ServerContext {
	public LoginContext(int fd) {
		super(fd);
	}
	
	@Override
	protected void send(Coder coder) {
		SystemManager.getCenterChannel().writeAndFlush(coder);
	}
}
