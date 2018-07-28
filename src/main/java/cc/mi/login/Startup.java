package cc.mi.login;

import cc.mi.core.log.CustomLogger;
import cc.mi.core.net.ClientCore;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.net.LoginHandler;
import cc.mi.login.net.LoginToGateHandler;
import cc.mi.login.server.LoginCache;

public class Startup {
	static final CustomLogger logger = CustomLogger.getLogger(Startup.class);
	private static void start() throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		
		// 获取账号信息
		LoginCache.INSTANCE.loadAllAccountInfo();
		// 获取账号对应角色信息
		LoginCache.INSTANCE.loadAllAccountCharInfo();
		// 获取玩家名称信息
		LoginCache.INSTANCE.loadAllCharName();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						ClientCore.INSTANCE.start(ServerConfig.getGateIp(), ServerConfig.getGatePort(), new LoginToGateHandler());
					} catch (Exception e) {
					} finally {
						logger.devLog("连接网关服错误,系统将在1秒钟后重新连接");
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, "bootstrap-to-gate").start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						ClientCore.INSTANCE.start(ServerConfig.getCenterIp(), ServerConfig.getCenterPort(), new LoginHandler());
					} catch (Exception e) {
					} finally {
						logger.devLog("连接中心服错误,系统将在1秒钟后重新连接");
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}, "bootstrap-to-center").start();
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		start();
	}

}
