package cc.mi.login.table;

import cc.mi.core.utils.TimestampUtils;

/**
 * 玩家副本信息
 * @author gy
 *
 */
public class PlayerInstInfo {
	private final String guid;
	private final int instId;
	private final int mapId;
	private int expire;
	private float x;
	private float y;
	
	public PlayerInstInfo(
		String guid,
		int instId,
		int mapId) {
		
		this.guid = guid;
		this.instId = instId;
		this.mapId = mapId;
	}
	
	public boolean isExpired() {
		return this.expire >= TimestampUtils.now();
	}

	public String getGuid() {
		return guid;
	}

	public int getExpire() {
		return expire;
	}

	public int getInstId() {
		return instId;
	}

	public int getMapId() {
		return mapId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setExpire(int expire) {
		this.expire = expire;
	}
	
	
}
