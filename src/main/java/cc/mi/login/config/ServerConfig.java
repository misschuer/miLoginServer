package cc.mi.login.config;

import java.io.IOException;
import java.net.URL;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cc.mi.core.constance.NetConst;

public class ServerConfig {
	private static final String LOGIN_CLIENT = "loginClient";
	private static final String LOGIN_KEY = "loginKey";
	private static final String CHECK_SESSIONKEY_TIME = "checkSessionKeyTime";
	private static final String PLAYER_DATA_PATH = "playerDataPath";
	
	public static final String WANNENG_LOGIN_KEY = "dc829703a6039ff5262ec2d0d520444a";
	
	private static String loginKey;
	private static String ip;
	private static int port;
	private static String HDD_DATA_PATH = "";
	private static boolean checkSessionKeyTime = false;
	
	public static void loadConfig() throws NumberFormatException, Exception {
		Config cfg = new Config();
		URL url = ServerConfig.class.getResource("/config.ini");
		Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
        	// 加载配置文件  
        	ini.load(url);

        	Section section = ini.get(LOGIN_CLIENT);
        	loginKey = section.get(LOGIN_KEY);
        	ip = section.get(NetConst.IP);
        	port = Integer.parseInt(section.get(NetConst.PORT));
        	checkSessionKeyTime = "TRUE".equals(section.get(CHECK_SESSIONKEY_TIME));
        	HDD_DATA_PATH = section.get(PLAYER_DATA_PATH);
        } catch (IOException e) {
        	e.printStackTrace();
	    }  
	}
	
	public static String getLoginKey() {
		return loginKey;
	}
	
	public static String getIp() {
		return ip;
	}
	
	public static int getPort() {
		return port;
	}
	
	public static boolean isCheckSessionKeyTime() {
		return checkSessionKeyTime;
	}

	public static String getHddDataPath() {
		return HDD_DATA_PATH;
	}
}
