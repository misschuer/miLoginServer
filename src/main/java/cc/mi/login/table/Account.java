package cc.mi.login.table;

import cc.mi.login.db.annotation.AutoIncrement;
import cc.mi.login.db.annotation.Column;
import cc.mi.login.db.annotation.Table;

@Table(name="account", pks={"id"}, keys={"name"}, comment = "账号表")
public class Account {
	
	@AutoIncrement
	@Column(name="id", nullable = false, defaultValue="", comment = "序号自增id")
	private int id;
	
	@Column(name="pid", nullable = false, defaultValue="", comment = "平台id")
	private String pid;
	
	@Column(name="sid", nullable = false, defaultValue="", comment = "服务器id")
	private String sid;
	
	@Column(name="uid", nullable = false, defaultValue="", comment = "用户id")
	private String uid;
	
	@Column(name="name", nullable = false, defaultValue="", comment = "pid_sid_uid")
	private String name;
	
	@Column(name="lastLoginIp", nullable = false, defaultValue="", comment = "最近一次登录ip")
	private String lastLoginIp;
	
	@Column(name="isFcm", nullable = false, defaultValue="", comment = "是否防沉迷")
	private int isFcm;
	
	@Column(name="gmLv", nullable = false, defaultValue="", comment = "gm等级")
	private int gmLv;
	
	@Column(name="platData", nullable = false, length = 200, defaultValue="", comment = "平台数据")
	private String platData;
	
	public Account() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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
}
