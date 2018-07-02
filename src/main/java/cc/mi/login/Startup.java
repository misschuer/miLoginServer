package cc.mi.login;

import cc.mi.core.net.ClientCore;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.net.LoginHandler;
import cc.mi.login.net.LoginToGateHandler;

public class Startup {

	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientCore.INSTANCE.start(ServerConfig.getGateIp(), ServerConfig.getGatePort(), new LoginToGateHandler());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "bootstrap-to-gate").start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ClientCore.INSTANCE.start(ServerConfig.getCenterIp(), ServerConfig.getCenterPort(), new LoginHandler());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "bootstrap-to-center").start();
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		start();
	}

}
