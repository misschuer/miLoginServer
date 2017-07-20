package cc.mi.login.server;

import org.apache.log4j.Logger;

import cc.mi.core.callback.Callback;
import cc.mi.core.task.base.AbstractAsyncTask;
import cc.mi.login.async.AsyncOperate;
import cc.mi.login.db.utils.DBUtils;
import cc.mi.login.module.CharInfo;
import cc.mi.login.table.Account;
import cc.mi.login.table.Chars;

public enum LoginDB {
	INSTANCE;
	
	private static final Logger logger = Logger.getLogger(LoginDB.class);
	
	private boolean isLocalDbIntegrated = false;
	
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
	public void getCharList(final String account, Callback<CharInfo> callback) {
		logger.info(String.format("account %s get char list", account));

		CharInfo info = LoginCache.INSTANCE.getCharInfo(account);

		//如果本地是完整库根本不需要再次去读数据库
		if (!this.isLocalDbIntegrated && info == null) {
			AsyncOperate.INSTANCE.submitTask(
				new AbstractAsyncTask<CharInfo>(callback) {
					@Override
					protected void doTask(Callback<CharInfo> callback) {
						Chars chars = DBUtils.fecthOne(Chars.class, "account", account);
						CharInfo info = null;
						if (chars != null) {
							info = new CharInfo();
							info.setGuid(chars.getGuid());
							info.setName(chars.getName());
							// 如果有属性可以再加
							LoginCache.INSTANCE.addAccountToChar(account, info);
							LoginCache.INSTANCE.saveAccountCharInfo(account, chars.getGuid());
						}
						callback.invoke(info);
					}
				}
			);
		} else {
			callback.invoke(info);
		}
	}
	
}
