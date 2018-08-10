package cc.mi.login.server;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.constance.OperateConst;
import cc.mi.core.generate.msg.SendCharInfo;
import cc.mi.core.generate.stru.CharInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.core.server.SessionStatus;
import cc.mi.login.table.Account;

public class LoginContext extends ServerContext {
	static final CustomLogger logger = CustomLogger.getLogger(LoginContext.class);
	
	private String account = null;
	protected String fromServerName = null;
	private LoginPlayer player = null;
//	private boolean isFCM = false;
//	private boolean hasPlatdata;			//登录sessionkey中是否携带平台信息
//	private boolean hasFcm;					//登录sessionkey中是否携带防沉迷信息
//	
//	private String watcherGuid = "";		//观察者guid
//	private String generalId = "";			//地图实例对应唯一id
	
	private int teleMapId;
	private int teleInstId;
	private String teleExt;
	private int teleLineNo;
	
	private List<BinlogData> tempList = new LinkedList<>();

	public LoginContext(int fd) {
		super(fd);
	}
	
	public boolean checkSession(final Map<String, String> params) {
		// 如果已经登陆过了
		if (this.account != null && !this.account.isEmpty() || this.player != null) {
			logger.errorLog("checkSession repeat for fd = {}", this.getFd());
			return false;
		}

		String platId   = params.get("pid"); // 平台id
		String serverId = params.get("sid"); // 服务器id
		String username = params.get("uid"); // 玩家自己的账号

//TODO:
//		watcherGuid = querys.get("watcher");
//		//是观察者
//		if (!watcherGuid.isEmpty()) {
//			//发内部协议到场景服,给这个连接下发对应的地图数据等
//			this.generalId = querys.get("generalid");
//			if(!sendToScenedAddMapWatcher())
//				return false;
//		}
		
		this.account = String.format("%s_%s_%s", platId, serverId, username);
		this.fromServerName = String.format("%s_%s", platId, serverId);
		
		// 验证通过
		this.setStatus(SessionStatus.STATUS_AUTHED);
		
		Integer oldFd = ContextManager.getSessionFd(this.account);
		//当前已经在线 或者 在session表里面找到成员也认为是已经有角色在线
		// 顶号的逻辑是先让原来的下掉, 再重新发登录请求
		if (oldFd != null) {
			//通知角色正在顶号登录(让当前玩家重新checksession)
			this.operationResult(OperateConst.OPERATE_LOGIN_REASON_LOGINED_IN, "");
			this.account = null;
			
			//通知已经登录的客户端 下线
			LoginContext oldContext = (LoginContext) ContextManager.getContext(oldFd);
			if(oldContext != null) {
				oldContext.closeSession(OperateConst.OPERATE_CLOSE_REASON_OTHER_LOGINED);
			}
			
			return true;
		}
		
		boolean isFCM = false;	//TODO: 要通过客户端传过来
		String platData = "";	//TODO: 要通过客户端传过来
		// 修改账号登录信息
		LoginDB.INSTANCE.modifyAccount(account, platId, serverId, username, isFCM, this.getRemoteIp(), platData, 0);

		// 拉角色列表
		LoginDB.INSTANCE.getCharList(account, new AbstractCallback<CharInfo>() {
			@Override
			public void invoke(CharInfo obj) {
				List<CharInfo> chars = new ArrayList<>();
				if (obj != null) {
					chars.add(obj);
				}
				SendCharInfo sci = new SendCharInfo();
				sci.setChars(chars);
			}
		});
		
		// 记录已经验证的号
		ContextManager.putSession(account, this.getFd());
		// 给客户端发送服务器信息
		LoginServerManager.getInstance().addWatchAndCall(this.getFd(), ObjectType.GLOBAL_CLIENT_GAME_CONFIG);
		
		return true;
	}

//	public void onClosed() {
////		tea_pdebug("player %s %u logout. begin", m_lguid.c_str(), fd_);
////		if(m_player)
////		{
////			tea_pdebug("player %s %u logout. remove player", m_lguid.c_str(), fd_);
////			ASSERT(GetStatus() == STATUS_LOGGEDIN);
////			//通知其他服务器玩家下线
////			WorldPacket pkt(INTERNAL_OPT_PLAYER_LOGOUT);
////			pkt << fd_ << m_player->GetGuid();
////			//通知应用服
////			if(!LogindApp::g_app->SendToAppd(pkt))
////			{
////				tea_pdebug("player %s %u logout. appd collapse", m_lguid.c_str(), fd_);
////			}
////			//通知日志服
////			if(!LogindApp::g_app->SendToPoliced(pkt))
////			{
////				tea_pdebug("player %s %u logout. policed collapse", m_lguid.c_str(), fd_);
////			}
////
////			//从地图管理器退出
////			MapMgr->PlayerLogout(m_player);
////
////			//玩家离开记录玩家退出时间
////			m_player->SetLastLogoutTime((uint32)time(NULL));
////			// 只有在冒险世界下线才需要设置
////			if (m_player->GetPickRiskRewardFlag() == 1) {
////				m_player->SetPickRiskRewardFlag(0);
////				m_player->SetLastLeaveRiskTime((uint32)time(NULL));
////			}
////
////			//玩家下线做点什么
////			DoPlayerLogout(m_player);
////
////			//数据对象的session置空
////			m_player->SetSession(NULL);
////
////			//进入等待移除列表
////			g_Cache.AddLogoutPlayer(m_lguid);
////		}
////		
////		//从账号映射表删除
////		if (fd_ && fd_ == LogindContext::FindSessionID(m_account))
////		{
////			LogindContext::SessionMaps.erase(m_account);
////		}
////
////		//保存一下登出LOG
////		SavePlayerLogoutLog();
////		//从app中移除自己
////		LogindApp::g_app->RomoveContext(this);
////		tea_pdebug("player %s %u logout. end", m_lguid.c_str(), fd_);
//	}
//	
//	public boolean sendToScenedAddMapWatcher() {
//		if (this.generalId.isEmpty()) {
//			return false;
//		}
//
////		int32 index = MapMgr->FindInstance(m_general_id);
////		if (index < 0)
////			return false;		//没有找到
////		uint32 scened_conn = MapMgr->GetScenedConn(index);
////		if (!scened_conn)
////			return false;	
////
////		WorldPacket spkt(INTERNAL_OPT_ADD_MAP_WATHER);
////		spkt << GetFD() << m_watcher_guid << m_general_id;
////		LogindApp::g_app->SendToScened(spkt, scened_conn);
//		return true;
//	}
//	
	/*检查名称*/
	public String checkNameAndGetRealName(String name) {
		//账号信息
		Account accountInfo = LoginCache.INSTANCE.getAccount(this.getAccount());
		////在创建角色的用户名中加入平台ID,服务器ID并且
		String charName = name;
//		//没加区服之前的校验
//		short checkReslut = checkName1(charName);
//		if(checkReslut != OperateConst.OPERATE_LOGIN_REASON_SUCCESS) {
//			this.callOperationResult(
//					LoginSystemManager.getCenterChannel(), 
//					OperateConst.OPERATE_TYPE_LOGIN, 
//					checkReslut, 
//					""
//			);
//			return null;
//		}
		////按照规则拼结用户名
		charName = accountInfo.getPid();
		charName += ',';
		charName += accountInfo.getSid();
		charName += ',';
		charName += name;
//		//加了区服以后的校验
//		checkReslut = checkName2(charName);
//		if(checkReslut == OperateConst.OPERATE_LOGIN_REASON_DB_RESULT_ERROR) {
//			//数据库异常无法创建
//			return null;
//		}
//		if(checkReslut != OperateConst.OPERATE_LOGIN_REASON_SUCCESS) {
//			this.callOperationResult(
//					LoginSystemManager.getCenterChannel(), 
//					OperateConst.OPERATE_TYPE_LOGIN, 
//					checkReslut, 
//					""
//			);
//			return null;
//		}
		return charName;
	}

//	private short checkName1(String name) {
////TODO:		屏蔽字
////		if (Pingbi((char*)name.c_str()))
////		{
////			tea_pdebug("error: has pingbi !!!");
////			return OPRATE_RESULT_NAME_HAS_PINGBI;
////		}
//
//		if ("".equals(name)) {
//			logger.info("error: name is null !!!");
//			return OperateConst.OPERATE_LOGIN_REASON_NAME_ILLEGAL;
//		}
//
////TODO: 不可创建字
////		auto& vec = g_Config.g_cant_make_name;
////		for (auto it = vec.begin(); it != vec.end(); ++it)
////		{
////			if(name.find(*it) != string::npos)
////			{
////				return OperateConst.OPERATE_LOGIN_REASON_NAME_ILLEGAL;
////			}
////		}
//		return OperateConst.OPERATE_LOGIN_REASON_SUCCESS;
//	}
//
//	private short checkName2(String name) {
//		
//		if (name.length() >= 50) {
//			logger.info("error:name is too long!!!");
//			return OperateConst.OPERATE_LOGIN_REASON_NAME_TOO_LONG;
//		}
//
//		String guid = LoginCache.INSTANCE.findGuidByCharName(name);
//		if (guid == null) {
//			logger.info(String.format("g_DAL.FindGuidByName(name, has_err); %s ", name));
//			return OperateConst.OPERATE_LOGIN_REASON_DB_RESULT_ERROR;
//		}
//
//		if (!guid.isEmpty()) {
//			logger.info(String.format("error: name repeat : %s", name));
//			return OperateConst.OPERATE_LOGIN_REASON_NAME_REPEAT;		
//		}
//
//		return OperateConst.OPERATE_LOGIN_REASON_SUCCESS;
//	}
//	
	
	
	public void playerLoadData() {
		this.setStatus(SessionStatus.STATUS_TRANSFER);
		tempList.clear();
		//先从硬盘load，没有再从数据库load
		if (LoginObjectManager.INSTANCE.loadPlayer(this.getGuid(), tempList) != null
			|| LoginCache.INSTANCE.loadHddPlayer(this.getGuid(), tempList) != null) {
			
			LoginCache.INSTANCE.delLogoutPlayer(this.getGuid());
			this.setStatus(SessionStatus.STATUS_TRANSFER2);
			return;
		}

		logger.errorLog("playerLoadData error no data exist");
	}
	
	public void putData() {
		final String guid = this.getGuid();
		final int fd = this.getFd();
		//缓存里有，不用put了
		if (LoginObjectManager.INSTANCE.get(guid) != null) {
			this.setStatus(SessionStatus.STATUS_PUT_OK);
			return;
		}
		
		//到这里就不是缓存里取得玩家了
		//向中心服提交玩家对象
		this.setStatus(SessionStatus.STATUS_PUT);
		final LoginContext self = this;
		LoginServerManager.getInstance().putObjects(this.getGuid(), this.tempList, new AbstractCallback<Boolean>() {
			@Override
			public void invoke(Boolean value) {
				LoginContext context = (LoginContext)ContextManager.getContext(fd);
				if(!value) {
					logger.devLog("player login call puts fail {}, fd {}", guid, fd);
					if(context != null) {
						context.closeSession(OperateConst.OPERATE_CLOSE_REASON_LOGDIN_ONE54);
					}
					return;
				}
				// 向中心服添加观察对象
				LoginServerManager.getInstance().addTagWatchAndCall(guid);

				if(context == null) {
					logger.devLog("player login call puts ok, but logout, {} {}", guid, fd);
					return;
				}

				logger.devLog("LogindContext::Login player login call puts ok, {} {}", guid, fd);
				self.setStatus(SessionStatus.STATUS_PUT_OK);
			}
		});
	}
	
	public void loginOK() {
		logger.devLog("LogindContext::LoginOK player {}, fd {}", this.getGuid(), this.getFd());
		LoginPlayer player = LoginObjectManager.INSTANCE.findPlayer(this.getGuid());

// TODO: 一些数据的处理
//		if(!IsKuafuPlayer())
//		{
//			//初始化一下数据
//			if(!p->InitDatabase())
//			{
//				tea_perror("LogindContext::Login player guid ==  %s,not InitDatabase ", m_lguid.c_str());
//				Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE9,"");
//				return ;
//			}
//
//			//看看有没有玩家数据需要修复或者升级的
//			bool repait_data = false;
//			DoRepairPlayerData(m_account, m_temp_vec, repait_data);
//			if(!repait_data)
//			{
//				tea_pinfo("repair player account %s  %s  err, ", m_account.c_str(), m_lguid.c_str());
//				Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE10,"");
//				return;
//
//			}
//		}

		//关联m_player
		this.setPlayer(player);
		player.setFd(this.getFd());
		// 这个是否可能会引起类循环引用
//		m_player->SetSession(this);

		//跨服的玩家
//		if(IsKuafuPlayer())
		boolean isKuafu = false;
		if (isKuafu) {	
//			m_player->SaveDBKuafuInfo(m_warid, m_kuafutype, (uint16)m_number, m_backsvrname);
		}
		else
		{	
			//情况跨服信息
//			m_player->ClearDBKuafuInfo();

			//重设账号信息
//			Account accountInfo = LoginCache.INSTANCE.getAccount(account);

//			//重新刷新一下防沉迷的信息
//			if(m_bHasfcm && !accountInfo->is_FCM)
//				m_player->SetFCMLoginTime(-1);
//			//从非防沉迷到防沉迷的
//			else if(m_bHasfcm && accountInfo->is_FCM && m_player->GetFCMLoginTime() == (uint32)-1)
//				m_player->SetFCMLoginTime(0);
//			//计算处理防沉迷相关时间
//			m_player->CalculFCMLogoutTime();	
			//设置一下RMB充值等级
//			m_player->GetSession()->SetPayLevel();	
//			if(m_bHasplatdata && m_player->GetPlatData() != accountInfo->platdata)
//				m_player->SetPlatData(accountInfo->platdata);
//			if(g_Config.gm_open)
//				m_player->SetGmNum(3);

			//设置下是否托管
//			if(m_player->GetFlags(PLAYER_APPD_INT_FIELD_FLAGS_IS_HOSTING_LOGIN) != m_is_hosting)
//			{
//				if(m_is_hosting)
//					m_player->SetFlags(PLAYER_APPD_INT_FIELD_FLAGS_IS_HOSTING_LOGIN);
//				else
//					m_player->UnSetFlags(PLAYER_APPD_INT_FIELD_FLAGS_IS_HOSTING_LOGIN);
//			}

			//设置页游手游在线情况
//			if (!m_player->GetFlags(PLAYER_APPD_INT_FIELD_FLAGS_YEYOU_ONLINE))
//				m_player->SetFlags(PLAYER_APPD_INT_FIELD_FLAGS_YEYOU_ONLINE);

//			bool is_phone_online = MobileContext::FindSessionID(m_account) != 0 ? true: false;
//			if (is_phone_online)
//			{//手游在线
//				if (!m_player->GetFlags(PLAYER_APPD_INT_FIELD_FLAGS_PHONE_ONLINE))
//					m_player->SetFlags(PLAYER_APPD_INT_FIELD_FLAGS_PHONE_ONLINE);
//			}
//			else
//			{//手游不在线
//				if (m_player->GetFlags(PLAYER_APPD_INT_FIELD_FLAGS_PHONE_ONLINE))
//					m_player->UnSetFlags(PLAYER_APPD_INT_FIELD_FLAGS_PHONE_ONLINE);
//			}
		}

		//通知其他服务器为该玩家建立会话信息	
		this.noticeOtherInnerServerToCreateConnection();
		
		//客户端对玩家对象的监听(由于客户端的先后顺序 玩家数据先发)
		LoginServerManager.getInstance().addTagWatchAndCall(this.getFd(), this.getGuid());

		//登录完成，准备传送
//		LoginMapManager.INSTANCE
//		MapMgr->PlayerLogin(m_player);
		//登录完毕，状态置一下
		this.setStatus(SessionStatus.STATUS_LOGGEDIN);

		//发个登录登录服完毕的包给客户端
//		Call_join_or_leave_server(m_delegate_sendpkt, 0, SERVER_TYPE_LOGIND, getpid(), LogindApp::g_app->Get_Connection_ID(), uint32(time(nullptr)));

		//保存一下登录记录
//		SavePlayerLoginLog();

		//监听帮派的
//		string factionId = p->GetFactionId();
//		if (!factionId.empty()) {
//			ObjMgr.CallAddWatch(fd_, factionId);
//		}

		//发送世界变量
//		ObjMgr.CallAddWatch(fd_, GLOBAL_OBJ_GUID);
//		ObjMgr.CallAddWatch(fd_, GLOBAL_GAME_CONFIG);
//		ObjMgr.CallAddWatch(fd_, GLOBAL_RIGHT_FLOAT_GUID);
	}

	private void noticeOtherInnerServerToCreateConnection() {
		if (this.player == null) {
			return;
		}

//		WorldPacket pkt(INTERNAL_OPT_PLAYER_LOGIN);
//		pkt << fd_ << m_player->GetGuid() << uint8(CONTEXT_TYPE_YEYOU);
//		
//		//通知中心服建立连接
//		LogindApp::g_app->SendToCentd(pkt);
//		//通知应用服建立连接
//		LogindApp::g_app->SendToAppd(pkt);
//		//通知日志服建立连接
//		LogindApp::g_app->SendToPoliced(pkt);
	}
	
	public String getAccount() {
		return account;
	}
//
//	public void setAccount(String account) {
//		this.account = account;
//	}

	public LoginPlayer getPlayer() {
		return player;
	}

	public void setPlayer(LoginPlayer player) {
		this.player = player;
	}
//
//	public boolean isFCM() {
//		return isFCM;
//	}
//
//	public void setFCM(boolean isFCM) {
//		this.isFCM = isFCM;
//	}
//
//	public boolean isHasPlatdata() {
//		return hasPlatdata;
//	}
//
//	public void setHasPlatdata(boolean hasPlatdata) {
//		this.hasPlatdata = hasPlatdata;
//	}
//
//	public boolean isHasFcm() {
//		return hasFcm;
//	}
//
//	public void setHasFcm(boolean hasFcm) {
//		this.hasFcm = hasFcm;
//	}
//
//	public String getWatcherGuid() {
//		return watcherGuid;
//	}
//
//	public void setWatcherGuid(String watcherGuid) {
//		this.watcherGuid = watcherGuid;
//	}
//
//	public String getGeneralId() {
//		return generalId;
//	}
//
//	public void setGeneralId(String generalId) {
//		this.generalId = generalId;
//	}

	public String getFromServerName() {
		return fromServerName;
	}
//
//	public void setFromServerName(String fromServerName) {
//		this.fromServerName = fromServerName;
//	}

	@Override
	protected void sendToGate(Packet coder) {
		LoginServerManager.getInstance().sendToGate(coder);
	}

	@Override
	protected void sendToCenter(Packet coder) {
		LoginServerManager.getInstance().sendToCenter(coder);
	}

	@Override
	protected void operationResult(short type, String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeSession(int type) {
		LoginServerManager.getInstance().closeSession(this.getFd(), type);
	}
	
	public void addTempBinlogDataToLogin(BinlogData binlogData) {
		this.tempList.add(binlogData);
	}

	public int getTeleMapId() {
		return teleMapId;
	}

	public void setTeleMapId(int teleMapId) {
		this.teleMapId = teleMapId;
	}

	public int getTeleInstId() {
		return teleInstId;
	}

	public void setTeleInstId(int teleInstId) {
		this.teleInstId = teleInstId;
	}

	public String getTeleExt() {
		return teleExt;
	}

	public void setTeleExt(String teleExt) {
		this.teleExt = teleExt;
	}

	public int getTeleLineNo() {
		return teleLineNo;
	}

	public void setTeleLineNo(int teleLineNo) {
		this.teleLineNo = teleLineNo;
	}
}
