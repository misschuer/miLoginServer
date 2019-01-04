package cc.mi.login.config;

import java.io.IOException;
import java.net.URL;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cc.mi.core.constance.NetConst;

public class ServerConfig {
	// 自动存储时间
	public static final int AUTO_SAVE_TIME = 360;
	
	private static final String ATTRIBUTE = "attribute";
	private static final String CENTER = "center";
	private static final String GATE = "gate";
	
	private static final String LOGIN_KEY = "loginKey";
	private static final String CHECK_SESSIONKEY_TIME = "checkSessionKeyTime";
	private static final String PLAYER_DATA_PATH = "playerDataPath";
	public static final String WANNENG_LOGIN_KEY = "dc829703a6039ff5262ec2d0d520444a";
	
	public static final String MAX_PLAYER_COUNT = "maxPlayerCount";
	public static final String IS_NORMAL_SERVER = "isNormalServer";
	
	private static String loginKey;
	private static String center_ip;
	private static int maxPlayerCount;
	private static int center_port;
	private static String gate_ip;
	private static int gate_port;
	private static String HDD_DATA_PATH = "";
	private static boolean checkSessionKeyTime = false;
	private static boolean normalServer = true;
	
	public static void loadConfig() throws NumberFormatException, Exception {
		Config cfg = new Config();
		URL url = ServerConfig.class.getResource("/config.ini");
		Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
        	// 加载配置文件  
        	ini.load(url);

        	Section section = ini.get(ATTRIBUTE);
        	loginKey = section.get(LOGIN_KEY);
        	checkSessionKeyTime = "TRUE".equals(section.get(CHECK_SESSIONKEY_TIME));
        	HDD_DATA_PATH = section.get(PLAYER_DATA_PATH);
        	maxPlayerCount = Integer.parseInt(section.get(MAX_PLAYER_COUNT));
        	normalServer = "TRUE".equals(section.get(IS_NORMAL_SERVER));
        	
        	Section section2 = ini.get(CENTER);
        	center_ip = section2.get(NetConst.IP);
        	center_port = Integer.parseInt(section2.get(NetConst.PORT));
        	
        	Section section3 = ini.get(GATE);
        	gate_ip = section3.get(NetConst.IP);
        	gate_port = Integer.parseInt(section3.get(NetConst.PORT));
        	
        } catch (IOException e) {
        	e.printStackTrace();
	    }  
	}
	
	public static String getLoginKey() {
		return loginKey;
	}
	
	public static String getCenterIp() {
		return center_ip;
	}
	
	public static int getCenterPort() {
		return center_port;
	}
	
	public static String getGateIp() {
		return gate_ip;
	}
	
	public static int getGatePort() {
		return gate_port;
	}
	
	public static boolean isCheckSessionKeyTime() {
		return checkSessionKeyTime;
	}

	public static String getHddDataPath() {
		return HDD_DATA_PATH;
	}

	public static int getMaxPlayerCount() {
		return maxPlayerCount;
	}

	public static boolean isNormalServer() {
		return normalServer;
	}
}
