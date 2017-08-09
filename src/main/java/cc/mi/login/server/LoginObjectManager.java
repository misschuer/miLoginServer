package cc.mi.login.server;

import java.util.LinkedList;
import java.util.List;

import cc.mi.core.binlog.data.GuidObject;
import cc.mi.core.server.ServerObjectManager;

public class LoginObjectManager extends ServerObjectManager  {
	public static final LoginObjectManager INSTANCE = new LoginObjectManager();
	
	private LoginObjectManager() {
		super();
	}
	
	public LoginPlayer loadPlayer(String guid, List<GuidObject> result) {
		this.getDataSetAllObject(guid, result);

		if (result.size() == 0) {
			return null;
		}

		for (GuidObject obj : result) {
			if (obj.getGuid() == guid) {
				return (LoginPlayer)obj;
			}
		}
		
		return null;
	}
}
