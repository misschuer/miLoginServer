package cc.mi.login.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.callback.InvokeCallback;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.GuidManager;
import cc.mi.core.server.OwnerDataSet;
import cc.mi.core.server.ServerObjectManager;

public class LoginObjectManager extends ServerObjectManager  {
	public static final LoginObjectManager INSTANCE = new LoginObjectManager();
	private final Map<String, Integer> playerRemovingHash = new HashMap<>();
	
	
	private LoginObjectManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN);
	}
	
	public LoginPlayer loadPlayer(String guid, final List<BinlogData> result) {
		this.getDataSetAllObject(guid, result);

		if (result.size() == 0) {
			return null;
		}

		for (BinlogData obj : result) {
			if (obj.getGuid().equals(guid)) {
				return (LoginPlayer)obj;
			}
		}
		
		return null;
	}
	
	public LoginPlayer findPlayer(String guid) {
		return (LoginPlayer)this.get(guid);
	}
	
	public void foreachPlayer(InvokeCallback<LoginPlayer> callback) {
		for (String guid : this.allOwnerDataSet.keySet()) {
			callback.invoke(this.findPlayer(guid));
		}
	}

	@Override
	protected BinlogData createBinlogData(String guid) {
		if (GuidManager.INSTANCE.isPlayerGuid(guid)) {
			return new LoginPlayer();
		}
		return new BinlogData(1 << 6, 1 << 6);
	}
	
	public boolean update(int diff) {
		Packet packet = this.getUpdatePacket();
		if (packet != null)
			LoginServerManager.getInstance().sendToCenter(packet);
		return true;
	}
	
	public void removePlayerData(final String ownerId) {
		OwnerDataSet ds = this.allOwnerDataSet.get(ownerId);
		if (ds == null) {
			return;
		}
		
		this.playerRemovingHash.put(ownerId, ds.size());
		final LoginObjectManager self = this;
		ds.foreach(new InvokeCallback<String>() {
			@Override
			public void invoke(String value) {
				self.removeCentreObject(value, new InvokeCallback<String>() {
					@Override
					public void invoke(String value) {
						if (self.playerIsRemoving(ownerId)) {
							int cnt = self.playerRemovingHash.get(ownerId) - 1;
							if (cnt <= 0) {
								self.playerRemovingHash.remove(ownerId);
								return;
							}
							self.playerRemovingHash.put(ownerId, cnt);
						}
					}
				});
			}
		});
	}
	
	public boolean playerIsRemoving(String ownerId) {
		return this.playerRemovingHash.containsKey(ownerId);
	}
}
