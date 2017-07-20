package cc.mi.login.server;

import java.util.Map;

import cc.mi.core.callback.Callback;
import cc.mi.core.coder.Coder;
import cc.mi.core.constance.OperateConst;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.core.server.SessionStatus;
import cc.mi.login.module.CharInfo;
import cc.mi.login.system.SystemManager;
import cc.mi.login.table.Account;

public class LoginContext extends ServerContext {
	private String account = "";
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
		SystemManager.getCenterChannel().writeAndFlush(coder);
	}

	/*获取sessionKey对象*/
	public boolean getSession(final Map<String, String> querys) {
		 //如果已经登录过了
	    if (!account.isEmpty() || player != null) {
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
					SystemManager.getCenterChannel(), 
					OperateConst.OPERATE_TYPE_LOGIN, 
					OperateConst.OPERATE_LOGIN_REASON_LOGINED_IN, 
					""
			);
			
			//通知已经登录的客户端
			LoginContext oldContext = (LoginContext) ContextManager.getContext(oldFd);
			if(oldContext != null) {
				oldContext.close(SystemManager.getCenterChannel(), OperateConst.OPERATE_CLOSE_REASON_OTHER_LOGINED, "", true);
				this.account = "";
			}
			
			return true;
		}

		final int fd = this.getFd();
		LoginDB.INSTANCE.modifyAccount(account, pid, sid, uid, isFCM, this.getRemoteIp(), platData, 0);
		LoginDB.INSTANCE.getCharList(account, new Callback<CharInfo>() {
			@Override
			public void invoke(CharInfo obj) {
//				auto *session = LogindContext::FindContext(fd);
//				if(!session) return;
//				string faction_name;
//				string queen_name;
//				uint8 icon = 0;
//				// 如果是被邀请的需要显示当前帮派信息
//				if (chars.size() == 0 && !invited.empty()) {
//					// 查询帮派信息
//					BinLogObject *factionInfo = (BinLogObject*)ObjMgr.Get(invited);
//					if (factionInfo) {
//						faction_name = factionInfo->GetStr(BINLOG_STRING_FIELD_NAME);
//						queen_name = factionInfo->GetStr(FACTION_STRING_FIELD_MANGER_NAME);
//						icon = factionInfo->GetByte(FACTION_INT_FIELD_FLAGS_ID, 0);
//					}
//				}
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
}
