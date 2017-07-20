package cc.mi.login.table;

import cc.mi.login.db.annotation.AutoIncrement;
import cc.mi.login.db.annotation.Column;
import cc.mi.login.db.annotation.Table;

@Table(name="chars", pks={"id"}, keys={"name", "guid"}, comment = "角色表")
public class Chars {
	
	@AutoIncrement
	@Column(name="id", nullable = false, defaultValue="", comment = "序号自增id")
	private int id;
	
	@Column(name="guid", nullable = false, defaultValue="", comment = "角色guid")
	private String guid;
	
	@Column(name="account", nullable = false, defaultValue="", comment = "角色所属账号")
	private String account;
	
	@Column(name="account", nullable = false, defaultValue="", comment = "角色名称")
	private String name;
	
	@Column(name="createData", nullable = false, defaultValue="", comment = "创建时间戳")
	private int createData;
	
	@Column(name="createIp", nullable = false, defaultValue="", comment = "注册ip")
	private String createIp;
	
	@Column(name="loginDays", nullable = false, defaultValue="", comment = "登录天数")
	private int loginDays;
	
	@Column(name="serverName", nullable = false, defaultValue="", comment = "服务器名称")
	private String serverName;
	
	@Column(name="data", nullable = false, length = 21844, defaultValue="", comment = "整形数据")
	private String data;
	
	@Column(name="dataStr", nullable = false, length = 21844, defaultValue="", comment = "字符串型数据")
	private String dataStr;
	
	public Chars() {}

	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getGuid() {
		return guid;
	}


	public void setGuid(String guid) {
		this.guid = guid;
	}


	public String getAccount() {
		return account;
	}


	public void setAccount(String account) {
		this.account = account;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getCreateData() {
		return createData;
	}


	public void setCreateData(int createData) {
		this.createData = createData;
	}


	public String getCreateIp() {
		return createIp;
	}


	public void setCreateIp(String createIp) {
		this.createIp = createIp;
	}


	public int getLoginDays() {
		return loginDays;
	}


	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}


	public String getServerName() {
		return serverName;
	}


	public void setServerName(String serverName) {
		this.serverName = serverName;
	}


	public String getData() {
		return data;
	}


	public void setData(String data) {
		this.data = data;
	}


	public String getDataStr() {
		return dataStr;
	}


	public void setDataStr(String dataStr) {
		this.dataStr = dataStr;
	}
}
