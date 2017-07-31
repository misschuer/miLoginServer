package cc.mi.login.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.coder.Coder;
import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.constance.OperateConst;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.core.server.SessionStatus;
import cc.mi.login.module.CharInfo;
import cc.mi.login.system.LoginSystemManager;
import cc.mi.login.table.Account;

public class LoginContext extends ServerContext {
	private static final Logger logger = Logger.getLogger(LoginContext.class);
	private static final int MAX_PLAYER_COUNT = 2000;
	private static final Queue<Integer> sessionQueue = new LinkedList<>();
	
	private String account;
	private String fromServerName;
	private LoginPlayer player;
	private boolean isFCM = false;
	private boolean hasPlatdata;			//登录sessionkey中是否携带平台信息
	private boolean hasFcm;					//登录sessionkey中是否携带防沉迷信息
	
	private String watcherGuid = "";		//观察者guid
	private String generalId = "";			//地图实例对应唯一id
	
	public LoginContext(int fd) {
		super(fd);
	}
	
	@Override
	protected void send(Coder coder) {
		LoginSystemManager.getCenterChannel().writeAndFlush(coder);
	}

	/*获取sessionKey对象*/
	public boolean getSession(final Map<String, String> querys) {
		 //如果已经登录过了
	    if (account != null && !account.isEmpty() || player != null) {
//	    	tea_perror("account[%s] re get session", m_account.c_str());	
	    	return false;
	    }

		//解析URL
		String pid = querys.get("pid");
		String sid = querys.get("sid");
		String uid = querys.get("uid");
		String fcm = querys.get("indulge");
		String platData = querys.get("platdata");
		
		isFCM = fcm == "y";
		if (!fcm.isEmpty()) {
			this.hasFcm = true;
		}
		if (!platData.isEmpty()) {
			hasPlatdata = true;
		}
		watcherGuid = querys.get("watcher");
		//是观察者
		if (!watcherGuid.isEmpty()) {
			//发内部协议到场景服,给这个连接下发对应的地图数据等
			this.generalId = querys.get("generalid");
			if(!sendToScenedAddMapWatcher())
				return false;
		}

		//TODO: 帐户名称，并且判断一下是否超长
		this.account = String.format("%s_%s_%s", pid, sid, uid);
		
		this.fromServerName = pid + "_" + sid;

//		tea_pdebug("account %s getsession!", this.account);
		//状态变成验证通过	
		this.setStatus(SessionStatus.STATUS_AUTHED);
		
		Integer oldFd = ContextManager.getSessionFd(this.account);
		//当前已经在线 或者 在session表里面找到成员也认为是已经有角色在线
		// 顶号的逻辑是先让原来的下掉, 再去
		if (oldFd != null) {
			//发条错误信息
//			tea_pwarn("LogindContext::Get_Session %s, but char online", m_account.c_str());
		
			//通知角色已经登录
			this.callOperationResult(
					LoginSystemManager.getCenterChannel(), 
					OperateConst.OPERATE_TYPE_LOGIN, 
					OperateConst.OPERATE_LOGIN_REASON_LOGINED_IN, 
					""
			);
			
			//通知已经登录的客户端
			LoginContext oldContext = (LoginContext) ContextManager.getContext(oldFd);
			if(oldContext != null) {
				oldContext.close(LoginSystemManager.getCenterChannel(), OperateConst.OPERATE_CLOSE_REASON_OTHER_LOGINED, "", true);
				this.account = null;
			}
			
			return true;
		}

		final int fd = this.getFd();
		LoginDB.INSTANCE.modifyAccount(account, pid, sid, uid, isFCM, this.getRemoteIp(), platData, 0);
		LoginDB.INSTANCE.getCharList(account, new AbstractCallback<CharInfo>() {
			@Override
			public void invoke(CharInfo obj) {
				List<CharInfo> chars = new ArrayList<>();
				if (obj != null) {
					chars.add(obj);
				}
//				Call_chars_list(session->m_delegate_sendpkt, chars, faction_name.c_str(), queen_name.c_str(), icon);
			}
		});

		ContextManager.putSession(account, fd);

		//为客户端监听其最新的模块信息
//TODO:		ObjMgr.CallAddWatch(fd_, GLOBAL_CLIENT_GAME_CONFIG);	

		return true;
	}
	
	public void onClosed() {
//		tea_pdebug("player %s %u logout. begin", m_lguid.c_str(), fd_);
//		if(m_player)
//		{
//			tea_pdebug("player %s %u logout. remove player", m_lguid.c_str(), fd_);
//			ASSERT(GetStatus() == STATUS_LOGGEDIN);
//			//通知其他服务器玩家下线
//			WorldPacket pkt(INTERNAL_OPT_PLAYER_LOGOUT);
//			pkt << fd_ << m_player->GetGuid();
//			//通知应用服
//			if(!LogindApp::g_app->SendToAppd(pkt))
//			{
//				tea_pdebug("player %s %u logout. appd collapse", m_lguid.c_str(), fd_);
//			}
//			//通知日志服
//			if(!LogindApp::g_app->SendToPoliced(pkt))
//			{
//				tea_pdebug("player %s %u logout. policed collapse", m_lguid.c_str(), fd_);
//			}
//
//			//从地图管理器退出
//			MapMgr->PlayerLogout(m_player);
//
//			//玩家离开记录玩家退出时间
//			m_player->SetLastLogoutTime((uint32)time(NULL));
//			// 只有在冒险世界下线才需要设置
//			if (m_player->GetPickRiskRewardFlag() == 1) {
//				m_player->SetPickRiskRewardFlag(0);
//				m_player->SetLastLeaveRiskTime((uint32)time(NULL));
//			}
//
//			//玩家下线做点什么
//			DoPlayerLogout(m_player);
//
//			//数据对象的session置空
//			m_player->SetSession(NULL);
//
//			//进入等待移除列表
//			g_Cache.AddLogoutPlayer(m_lguid);
//		}
//		
//		//从账号映射表删除
//		if (fd_ && fd_ == LogindContext::FindSessionID(m_account))
//		{
//			LogindContext::SessionMaps.erase(m_account);
//		}
//
//		//保存一下登出LOG
//		SavePlayerLogoutLog();
//		//从app中移除自己
//		LogindApp::g_app->RomoveContext(this);
//		tea_pdebug("player %s %u logout. end", m_lguid.c_str(), fd_);
	}
	
	public boolean sendToScenedAddMapWatcher() {
		if (this.generalId.isEmpty()) {
			return false;
		}

//		int32 index = MapMgr->FindInstance(m_general_id);
//		if (index < 0)
//			return false;		//没有找到
//		uint32 scened_conn = MapMgr->GetScenedConn(index);
//		if (!scened_conn)
//			return false;	
//
//		WorldPacket spkt(INTERNAL_OPT_ADD_MAP_WATHER);
//		spkt << GetFD() << m_watcher_guid << m_general_id;
//		LogindApp::g_app->SendToScened(spkt, scened_conn);
		return true;
	}
	
	/*检查名称*/
	public String checkNameAndGetRealName(String name) {
		//账号信息
		Account accountInfo = LoginCache.INSTANCE.getAccount(this.getAccount());
		////在创建角色的用户名中加入平台ID,服务器ID并且
		String charName = name;
		//没加区服之前的校验
		short checkReslut = checkName1(charName);
		if(checkReslut != OperateConst.OPERATE_LOGIN_REASON_SUCCESS) {
			this.callOperationResult(
					LoginSystemManager.getCenterChannel(), 
					OperateConst.OPERATE_TYPE_LOGIN, 
					checkReslut, 
					""
			);
			return null;
		}
		////按照规则拼结用户名
		charName = accountInfo.getPid();
		charName += ',';
		charName += accountInfo.getSid();
		charName += ',';
		charName += name;
		//加了区服以后的校验
		checkReslut = checkName2(charName);
		if(checkReslut == OperateConst.OPERATE_LOGIN_REASON_DB_RESULT_ERROR) {
			//数据库异常无法创建
			return null;
		}
		if(checkReslut != OperateConst.OPERATE_LOGIN_REASON_SUCCESS) {
			this.callOperationResult(
					LoginSystemManager.getCenterChannel(), 
					OperateConst.OPERATE_TYPE_LOGIN, 
					checkReslut, 
					""
			);
			return null;
		}
		return charName;
	}
	
	private short checkName1(String name) {
//TODO:		屏蔽字
//		if (Pingbi((char*)name.c_str()))
//		{
//			tea_pdebug("error: has pingbi !!!");
//			return OPRATE_RESULT_NAME_HAS_PINGBI;
//		}

		if ("".equals(name)) {
			logger.info("error: name is null !!!");
			return OperateConst.OPERATE_LOGIN_REASON_NAME_ILLEGAL;
		}

//TODO: 不可创建字
//		auto& vec = g_Config.g_cant_make_name;
//		for (auto it = vec.begin(); it != vec.end(); ++it)
//		{
//			if(name.find(*it) != string::npos)
//			{
//				return OperateConst.OPERATE_LOGIN_REASON_NAME_ILLEGAL;
//			}
//		}
		return OperateConst.OPERATE_LOGIN_REASON_SUCCESS;
	}

	private short checkName2(String name) {
		
		if (name.length() >= 50) {
			logger.info("error:name is too long!!!");
			return OperateConst.OPERATE_LOGIN_REASON_NAME_TOO_LONG;
		}

		String guid = LoginCache.INSTANCE.findGuidByCharName(name);
		if (guid == null) {
			logger.info(String.format("g_DAL.FindGuidByName(name, has_err); %s ", name));
			return OperateConst.OPERATE_LOGIN_REASON_DB_RESULT_ERROR;
		}

		if (!guid.isEmpty()) {
			logger.info(String.format("error: name repeat : %s", name));
			return OperateConst.OPERATE_LOGIN_REASON_NAME_REPEAT;		
		}

		return OperateConst.OPERATE_LOGIN_REASON_SUCCESS;
	}
	
	
	public static void pushSession(int fd) {
		sessionQueue.add(fd);
	}
	
	public static int getLoginPlayerCount() {
		int cnt = ContextManager.getLoginPlayers(new AbstractCallback<ServerContext>() {
			@Override
			public boolean isMatched(ServerContext obj) {
				if (obj.getStatus() == SessionStatus.STATUS_TRANSFER || obj.getStatus() == SessionStatus.STATUS_LOGGEDIN) {
					return true;
				}
				return false;
			}
		});
		return cnt;
	}
	
	public static void dealLoginQueue() {
		if (!LoginContext.sessionQueue.isEmpty()) {
			int loginCount = LoginContext.getLoginPlayerCount();
			int passCount = MAX_PLAYER_COUNT - loginCount;
			logger.debug(String.format("dealLoginQueue max %u , now %u, pass %u, queue %u", 
					MAX_PLAYER_COUNT, loginCount, passCount, sessionQueue.size()));
			
			// 先计算能正常登录的
			while (passCount > 0 && !sessionQueue.isEmpty()) {
				int fd = sessionQueue.poll();
				ServerContext context = ContextManager.getContext(fd);
				if (context == null) {
					continue;
				}
				
				if (context.getGuid().isEmpty()) {
					logger.debug(String.format("dealLoginQueue, guid empty, account %s", ((LoginContext)context).getAccount()));
					context.close(LoginSystemManager.getCenterChannel(), OperateConst.OPERATE_CLOSE_REASON_LOGDIN_ONE18, "");
					continue;
				}
				
				if (context.getStatus() == SessionStatus.STATUS_TRANSFER || 
					context.getStatus() == SessionStatus.STATUS_LOGGEDIN) {
					logger.debug(String.format("dealLoginQueue ,but status err, %s", context.getGuid()));
					continue;
				}
				LoginSystemManager.loginQueue.pushAction(context.getGuid(), context.getFd(), LoginActionEnum.CONTEXT_LOGIN_ACTION_LOGIN);
				passCount --;
			}
			
			Iterator<Integer> iter = sessionQueue.iterator();
			// 需要等待的
			for (int index = 0; iter.hasNext(); index ++) {
				int fd = iter.next();
				ServerContext context = ContextManager.getContext(fd);
				if (context == null) {
					iter.remove();
					index --;
					continue;
				}
				System.out.println(index);
//TODO:				Call_login_queue_index(context->m_delegate_sendpkt, index);
			}
		}
	}
	
	
	public void playerLoadData() {
//		SetStatus(STATUS_TRANSFER);
//		logind_player *player;
//		//先从硬盘load，没有再从数据库load
//		if(ObjMgr.LoadPlayer(m_lguid, &player, m_temp_vec)
//			|| g_Cache.LoadPlayer(m_lguid, &player, m_temp_vec))
//		{
//			ASSERT(player);
//			g_Cache.DelLogoutPlayer(m_lguid);
//			SetStatus(STATUS_TRANSFER2);
//			return;
//		}
//		else
//		{
//			safe_delete(player);
//			for(auto it:m_temp_vec)
//				safe_delete(it);
//			m_temp_vec.clear();
//		}
//
//		uint32 fd = fd_;
//		string guid = m_lguid;
//		static map<uint32, vector<GuidObject*>> obj_vec_map;
//		ASSERT(obj_vec_map.find(fd_) == obj_vec_map.end());
//
//		g_DAL.LoadPlayer(m_account, m_lguid, obj_vec_map[fd_], [this, fd, guid](bool load_succeed, vector<GuidObject*> &vec){
//			auto session = LogindContext::FindContext(fd);
//			if(!session)
//			{
//				for (auto it:vec)
//				{
//					safe_delete(it);
//				}
//				vec.clear();
//				return;
//			}
//			if(!load_succeed)
//			{
//				session->Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE59, "");
//				return;
//			}
//
//			for (auto it:vec)
//			{
//				session->m_temp_vec.push_back(it);
//			}
//			SetStatus(STATUS_TRANSFER2);
//		});
	}
	

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public LoginPlayer getPlayer() {
		return player;
	}

	public void setPlayer(LoginPlayer player) {
		this.player = player;
	}

	public boolean isFCM() {
		return isFCM;
	}

	public void setFCM(boolean isFCM) {
		this.isFCM = isFCM;
	}

	public boolean isHasPlatdata() {
		return hasPlatdata;
	}

	public void setHasPlatdata(boolean hasPlatdata) {
		this.hasPlatdata = hasPlatdata;
	}

	public boolean isHasFcm() {
		return hasFcm;
	}

	public void setHasFcm(boolean hasFcm) {
		this.hasFcm = hasFcm;
	}

	public String getWatcherGuid() {
		return watcherGuid;
	}

	public void setWatcherGuid(String watcherGuid) {
		this.watcherGuid = watcherGuid;
	}

	public String getGeneralId() {
		return generalId;
	}

	public void setGeneralId(String generalId) {
		this.generalId = generalId;
	}

	public String getFromServerName() {
		return fromServerName;
	}

	public void setFromServerName(String fromServerName) {
		this.fromServerName = fromServerName;
	}
}
