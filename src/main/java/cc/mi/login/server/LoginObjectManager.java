package cc.mi.login.server;

import java.util.List;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.server.ServerObjectManager;

public class LoginObjectManager extends ServerObjectManager  {
	public static final LoginObjectManager INSTANCE = new LoginObjectManager();
	
	private LoginObjectManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN);
	}
	
	public LoginPlayer loadPlayer(String guid, final List<BinlogData> result) {
		this.getDataSetAllObject(guid, result);

		if (result.size() == 0) {
			return null;
		}

		for (BinlogData obj : result) {
			if (obj.getGuid() == guid) {
				return (LoginPlayer)obj;
			}
		}
		
		return null;
	}
	
	public LoginPlayer findPlayer(String guid) {
		return (LoginPlayer)this.get(guid);
	}
}
