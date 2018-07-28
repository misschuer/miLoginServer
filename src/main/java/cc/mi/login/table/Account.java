package cc.mi.login.table;

public class Account implements ContentParser {
	// 平台id
	private String pid;
	
	// 服务器id
	private String sid;
	
	// 玩家账号id
	private String uid;
	
	// 由 pid_sid_uid 组成
	private String name;
	
	// 最近一次登录ip
	private String lastLoginIp;
	
	// 是否防沉迷
	private int isFcm;
	
	// gm等级
	private int gmLv;
	
	// 平台数据
	private String platData;
	
	public Account() {}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public int getIsFcm() {
		return isFcm;
	}

	public void setIsFcm(int isFcm) {
		this.isFcm = isFcm;
	}

	public int getGmLv() {
		return gmLv;
	}

	public void setGmLv(int gmLv) {
		this.gmLv = gmLv;
	}

	public String getPlatData() {
		return platData;
	}

	public void setPlatData(String platData) {
		this.platData = platData;
	}

	@Override
	public void fromString(String str) {
		String[] params = str.split(" ");
		this.pid		= params[ 0 ];
		this.sid		= params[ 1 ];
		this.uid		= params[ 2 ];
		this.name		= params[ 3 ];
		this.lastLoginIp= params[ 4 ];
		this.isFcm		= Integer.parseInt(params[ 5 ]);
		this.gmLv		= Integer.parseInt(params[ 6 ]);
		this.platData	= params[ 7 ];
	}
	
	public String toString() {
		return String.format("%s %s %s %s %s %d %d %s", 
				this.pid, this.sid, this.uid, this.name, this.lastLoginIp, this.isFcm, this.gmLv, this.platData);
	}
}
