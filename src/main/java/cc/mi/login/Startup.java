package cc.mi.login;

import cc.mi.core.net.ClientCore;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.net.LoginHandler;

public class Startup {

	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		ClientCore.start(ServerConfig.getIp(), ServerConfig.getPort(), new LoginHandler());
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		start();
	}

}
