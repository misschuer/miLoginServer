package cc.mi.login.server;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.manager.ServerManager;

public class LoginServerManager extends ServerManager {
	private static LoginServerManager instance;
	
	public static LoginServerManager getInstance() {
		if (instance == null) {
			instance = new LoginServerManager();
		}
		return instance;
	}
	
	public LoginServerManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN);
	}
}
