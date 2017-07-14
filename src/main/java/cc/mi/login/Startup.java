package cc.mi.login;

import java.io.IOException;
import java.net.URL;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cc.mi.login.loginClient.LoginClient;

public class Startup {
	private static final String LOGIN_CLIENT = "loginClient";
	private static final String IP = "ip";
	private static final String PORT = "port";
	
	private static void loadConfig() throws NumberFormatException, Exception {
		Config cfg = new Config();
		URL url = Startup.class.getResource("/config.ini");
		Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
        	// 加载配置文件  
        	ini.load(url);

        	Section section = ini.get(LOGIN_CLIENT);
        	LoginClient.start(section.get(IP), Integer.parseInt(section.get(PORT)));
        	
        } catch (IOException e) {
        	e.printStackTrace();
	    }  
	}

	public static void main(String[] args) throws NumberFormatException, Exception {
		loadConfig();
	}

}
