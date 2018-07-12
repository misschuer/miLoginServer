package cc.mi.login.server;

import java.util.List;

import cc.mi.core.binlog.data.BinlogModifier;
import cc.mi.core.server.ServerObjectManager;

public class LoginObjectManager extends ServerObjectManager  {
	public static final LoginObjectManager INSTANCE = new LoginObjectManager();
	
	private LoginObjectManager() {
		super();
	}
	
	public LoginPlayer loadPlayer(String guid, List<BinlogModifier> result) {
		this.getDataSetAllObject(guid, result);

		if (result.size() == 0) {
			return null;
		}

		for (BinlogModifier obj : result) {
			if (obj.getGuid() == guid) {
				return (LoginPlayer)obj;
			}
		}
		
		return null;
	}
}
