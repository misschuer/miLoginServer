package cc.mi.login;

import cc.mi.core.log.CustomLogger;
import cc.mi.core.net.ClientCore;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.net.LoginHandler;
import cc.mi.login.net.LoginToGateHandler;

public class Startup {
	static final CustomLogger logger = CustomLogger.getLogger(Startup.class);
	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						ClientCore.INSTANCE.start(ServerConfig.getGateIp(), ServerConfig.getGatePort(), new LoginToGateHandler());
						logger.devLog("连接网关服错误,系统将在1秒钟后重新连接");
						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "bootstrap-to-gate").start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						ClientCore.INSTANCE.start(ServerConfig.getCenterIp(), ServerConfig.getCenterPort(), new LoginHandler());
						logger.devLog("连接中心服错误,系统将在1秒钟后重新连接");
						Thread.sleep(1000);
					}
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
