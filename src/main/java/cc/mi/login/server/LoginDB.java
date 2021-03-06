package cc.mi.login.server;

import cc.mi.core.callback.InvokeCallback;
import cc.mi.core.generate.stru.CharInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.login.table.Account;

public enum LoginDB {
	INSTANCE;
	
	static final CustomLogger logger = CustomLogger.getLogger(LoginDB.class);
	
	private LoginDB() {}
	
	public Account modifyAccount(
			String account, 
			String pid, 
			String sid, 
			String uid, 
			boolean isFCM,
			String ip,
			String platData,
			int gmlevel) {
		
		int fcm = isFCM ? 1 : 0;
		//验证是否变化,则更新
		boolean hasChanged = false;
		//尝试从本地缓存取
		Account info = LoginCache.INSTANCE.getAccount(account);
		
		//创建一个新的帐户信息指针	
		if (info == null) {
			info = new Account();
			info.setGmLv(gmlevel >= 0 ? gmlevel : 0);
			info.setLastLoginIp(ip);
			info.setName(account);
			info.setPid(pid);
			info.setSid(sid);
			info.setUid(uid);
			info.setPlatData(platData);
			info.setIsFcm(fcm);
			
			LoginCache.INSTANCE.addAccountAndSave(info);
			hasChanged = true;
		}
		
		//////////////////////////////////////////////////////////////////////////	
		if (fcm != info.getIsFcm()) {
			info.setIsFcm(fcm);
			hasChanged = true;
		}
		
		if (!platData.isEmpty() && !platData.equals(info.getPlatData())) {
			info.setPlatData(platData);
			hasChanged = true;
		}
		
		if (gmlevel >= 0 && gmlevel != info.getGmLv()) {
			info.setGmLv(gmlevel);
			hasChanged = true;
		}

		if (hasChanged) {
//TODO:			//更新并插入数据库
//			Map result,wheres,values;
//			//result["id"] = id;
//			wheres["name"] = pid+'_'+sid+'_'+uid;
//			wheres["server_name"] = GetServerNameFromAccount(account);
//
//			result["pid"] = pid;
//			result["sid"] = sid;
//			result["s_uid"] = uid;
//			result["is_fcm"] = toString(is_FCM);
//			result["last_ip"] = ip;		
//			result["i_gm_level"] = toString(0);	
//			result["platdata"] = platdata;
//
//			if(Load(GetServerNS("account"),wheres,values) == MONGO_RES_SUCCESS)
//			{
//				AsyncUpdate(GetServerNS("account"),wheres,result);
//			}
//			else
//			{
//				result["name"] = pid+'_'+sid+'_'+uid;
//				result["server_name"] = GetServerNameFromAccount(account);
//				uint32 now_time = (uint32)time(NULL);
//				result["u_create_date"] = toString(now_time);
//				AsyncInsert(GetServerNS("account"),result);
//			}
		}
		
		return info;
	}
	
	//根据帐号获取角色列表
	public void getCharList(final String account, InvokeCallback<CharInfo> callback) {
		logger.devLog("account {} get char list", account);
		
		CharInfo info = LoginCache.INSTANCE.getCharInfo(account);
		callback.invoke(info);
	}

	public String getServerNameFromCharName(String name) {
		
		String serverName = "";
		
		//倒数第一个,号的后面存储着玩家真正的名称
		int pos = name.indexOf(',');
		if (pos == -1) {
			return "";
		}
		
		//第一个逗号分割着pid
		serverName += name.substring(0, pos);	
		serverName += '_';

		//第二个逗号存着sid
		int pos2 = name.indexOf(',', pos+1);
		if(pos2 == -1) {
			return "";
		}
		
		serverName += name.substring(pos+1, pos2-pos-1);
		return serverName;
	}
}
