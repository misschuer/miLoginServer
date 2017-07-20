package cc.mi.login;

import cc.mi.core.serverClient.ServerClient;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.loginClient.LoginClientHandler;

public class Startup {

	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		ServerClient.start(ServerConfig.getIp(), ServerConfig.getPort(), new LoginClientHandler());
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		start();
	}

}
