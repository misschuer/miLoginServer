package cc.mi.login.server;

import java.util.HashMap;
import java.util.Map;

import cc.mi.core.log.CustomLogger;
import cc.mi.core.server.SessionStatus;
import cc.mi.core.xlsxData.MapTemplate;
import cc.mi.login.table.MapInstInfo;
import cc.mi.login.table.PlayerInstInfo;

public enum LoginMapManager {
	INSTANCE;
	
	static final CustomLogger logger = CustomLogger.getLogger(LoginMapManager.class);

	private final int ZHUCHENG_DITU_ID = 1;
	private final float ZHUCHENG_FUHUO_X = 100;
	private final float ZHUCHENG_FUHUO_Y = 100;
	
	private int sceneCollapseTime;
	private int sceneCnt;
	
	private final Map<String, PlayerInstInfo> playerInstInfoHash = new HashMap<>();
	private final Map<Integer, MapInstInfo> mapInstInfoHash = new HashMap<>();
	private LoginMapManager() {}
	
	
//	void MapManager::Update(uint32 diff)
//	{
//		//轮询场景服状态，为崩掉的场景服重新传送玩家
//		UpdateScenedStatus();
//	}

	public void updateTeleport(LoginContext context) {
		if(context.getStatus() != SessionStatus.STATUS_LOGGEDIN) {
			return;
		}

		//如果等待场景服准备好，还是先等一下吧
		if (sceneCollapseTime > 0) {
			return;
		}
		
		LoginPlayer player = context.getPlayer();
		if (player == null) {
			return;	
		}

		//既然没有场景服存在，也没心跳的必要了
		if (sceneCnt == 0) {
			return;
		}

		if (context.getTeleMapId() == 0 && player.getTeleportMapID() > 0) {
			context.setTeleMapId(player.getTeleportMapID());
			context.setTeleExt(player.getTeleportExt());
			context.setTeleLineNo(player.getTeleportLineNo());

			if (player.getMapId() <= 0) {
				logger.errorLog("UpdateTeleport:player mapid {} is not valid, to mapid = {}, to instanceid = {}, teleportMapId = {}", 
						player.getMapId(), context.getTeleMapId(), context.getTeleInstId(), player.getTeleportMapID());
				return;
			}
			
			MapTemplate mt = MapTemplate.getTemplate(context.getTeleMapId());
			if (mt == null || !mt.isValidPosition((int)player.getTeleportPosX(), (int)player.getTeleportPosY())) {
				//如果待传送的目的地不是有效的则传回主城
				logger.errorLog("UpdateTeleport:player to mapid = {}, to instanceid = {}, pos({}, {})", 
						context.getTeleMapId(), context.getTeleInstId(), player.getTeleportPosX(), player.getTeleportPosY());
				context.setTeleMapId(0);
				player.setTeleportInfo(ZHUCHENG_DITU_ID, ZHUCHENG_FUHUO_X, ZHUCHENG_FUHUO_Y, 0, "");
//				if (player->IsKuafuPlayer())
//					session->GoBackToGameSvr();			//跨服玩家直接传回到游戏服	
				return;
			}
			context.setTeleInstId(player.getTeleportInstanceId());
			player.setTeleportInstanceId(0);
			logger.devLog("UpdateTeleport: player {} will teleport to mapid {} instanceId {} ext {} pos({}, {})", 
					context.getGuid(), context.getTeleMapId(), context.getTeleInstId(), context.getTeleExt(), player.getTeleportPosX(), player.getTeleportPosY());
			
			//执行以下代码后calljoinmap,场景服会主动要玩家数据,并且清空玩家传送信息
			teleportTo(context);
		}
		
		//玩家身上的GetTeleportMapID为0的时候，肯定是传送成功了。
		//玩家身上的GetTeleportMapID不等于session->to_mapid的时候，则证明有新的传送需求了。
		//玩家身上的GetTeleportLineNo不等于session->to_line_no，则证明有新的传送需求了。
		//玩家身上的GetTeleportGuid不等于session->to_teleport_id，则证明有新的传送需求了。
		else if (context.getTeleMapId() > 0
			&& (
					context.getTeleMapId() != player.getTeleportMapID() 
					|| context.getTeleLineNo() != player.getTeleportLineNo() 
					|| context.getTeleExt() != player.getTeleportExt())) {
			//检测到传送成功了
			logger.devLog("UpdateTeleport: player {} teleport to mapid {} ext {} success!",
					context.getGuid(), context.getTeleMapId(), context.getTeleInstId());
			context.setTeleMapId(0);
		}	
	}

	public void teleportTo(LoginContext context) {
		
		logger.devLog("teleportTo {} begin", context.getGuid());
		LoginPlayer player = context.getPlayer();
		if (player.getMapId() == 0) {
			//玩家当前地图id为0，估计是数据乱了
			logger.errorLog("releportTo player {} curmapid is 0!", context.getGuid());
			return;
		}
		
		MapTemplate toMt = MapTemplate.getTemplate(context.getTeleMapId());
		MapTemplate frMt = MapTemplate.getTemplate(player.getMapId());
		// 从副本到主城
		if (frMt.isInstance() && !toMt.isInstance()) {
			delRecordInstance(player);
		}

		//加入地图并返回到新实例
		playerJoin(player);
		logger.devLog("teleportTo end");
	}
//
//	void MapManager::Call_Join_Map(uint32 index, logind_player *player)
//	{
//		tea_pdebug("MapManager::Call_Join_Map player %s call join map begin", player->GetGuid().c_str());
//		uint32 m_scened_conn = GetScenedConn(index);
//		tea_pdebug("MapManager::Call_Join_Map m_scened_conn=%u", m_scened_conn);
//		if(!m_scened_conn)
//		{
//			if(player->GetSession()){
//				player->GetSession()->Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE20,"");
//			}
//			return;
//		}
//		if (!player->GetSession())
//		{
//			tea_perror("Call_Join_Map 玩家连接已经断开");
//			return;
//		}
//
//		//通知场景服，玩家加入地图	
//		player->SetTeleportSign(--m_teleport_callback_index);
//		//ASSERT(m_teleport_callback_index < 0);
//		WorldPacket pkt_scened(INTERNAL_OPT_JOIN_MAP);
//		pkt_scened << m_scened_conn << player->GetSession()->GetFD() << player->guid() << player->GetTeleportMapID() << GetInstanceID(index)
//			<< player->GetTeleportPosX()<< player->GetTeleportPosY() << m_teleport_callback_index;
//		LogindApp::g_app->SendToScened(pkt_scened, m_scened_conn);
//		//传送完毕，设置FD
//		player->SetScenedFD(m_scened_conn);
//
//		//场景服计数加1
//		DoAddScenedPlayer(m_scened_conn);
//		//ServerList.ScenedAddPlayer(m_scened_conn);
//		tea_pdebug("MapManager::Call_Join_Map player %s call join map end", player->GetGuid().c_str());
//	}
//
//	void MapManager::AddPlayer(uint32 index, logind_player *player)
//	{
//		//加入玩家信息
//		BinLogObject *binlog = dynamic_cast<BinLogObject*>(ObjMgr.Get(GetInstancePlayerInfoID(GetInstanceID(index))));
//		if(!binlog)
//		{
//			uint32 m_scened_conn = GetScenedConn(index);
//			tea_perror("MapManager::AddPlayer GetInstancePlayerInfoID==NULL  %s , scened fd %u", player->GetGuid().c_str(), m_scened_conn);
//			//把这个场景服关闭
//			WorldPacket pkt(INTERNAL_OPT_CLOSE_SCENED);
//			LogindApp::g_app->SendToScened(pkt, m_scened_conn);
//			//把这个玩家关掉
//			if(player->GetSession()){
//				player->GetSession()->Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE21,"");
//			}
//			return;
//		}
//		bool b = false;
//		uint32 str_s = binlog->length_str();
//		for (uint32 i = MAP_INSTANCE_PLAYER_INFO_START_FIELD; i < str_s; i++)
//		{
//			if(binlog->GetStr(i).empty())
//			{
//				binlog->SetStr(i, player->GetGuid());
//				b = true;
//				break;
//			}
//		}
//		if(!b)//放到最后
//		{
//			uint32 i = MAP_INSTANCE_PLAYER_INFO_START_FIELD;
//			if(str_s > i)
//				i = str_s;
//			binlog->SetStr(i, player->GetGuid());
//		}
//
//		//加入地图
//		Call_Join_Map(index, player);	
//	}
//
//	int32 MapManager::DelPlayer(logind_player *player)
//	{
//		//由于存在网络同步问题，所以不信任玩家身上的instanceid
//		int32 index = ForEach([&](uint32 index){
//			uint32 instance_id = GetInstanceID(index);
//			if(instance_id == 0)
//				return false;
//			BinLogObject *binlog = dynamic_cast<BinLogObject*>(ObjMgr.Get(GetInstancePlayerInfoID(instance_id)));
//			if(!binlog)
//			{
//				tea_perror("MapManager::DelPlayer instance %u binlog not find", instance_id);
//				return false;
//			}
//			bool result = false;
//			uint32 str_s = binlog->length_str();
//			for (uint32 i = MAP_INSTANCE_PLAYER_INFO_START_FIELD; i < str_s; i++)
//			{
//				if(binlog->GetStr(i) == player->GetGuid())
//				{
//					binlog->SetStr(i, "");
//					result = true;
//					break;
//				}
//			}
//			return result;
//		});
//		//场景服计数减1
//		if(index >= 0)
//		{
//			uint32 mapid = GetMapID(index);
//			uint32 instid = GetInstanceID(index);
//			uint32 fd = GetScenedConn(index);
//			tea_pdebug("player leave map %s %u %u.", player->guid().c_str(), mapid, instid);
//			DoSubScenedPlayer(fd);
//			WorldPacket pkt(INTERNAL_OPT_LEAVE_MAP);
//			pkt << player->guid() << player->GetSession()->GetFD();
//			LogindApp::g_app->SendToScened(pkt, fd);
//		}
//		else
//		{
//			//找不到有两种情况
//			//1.玩家登录传送，这种没办法
//			//2.ObjectTypeMapPlayerInfo丢失
//			//解决方案是，所有场景服都发一次离开场景服
//			//风险是场景服人数统计会失效，不用修复了
//			WorldPacket pkt(INTERNAL_OPT_LEAVE_MAP);
//			pkt << player->guid() << 0;
//			ServerList.ForEachScened([&pkt](uint32 fd){
//				LogindApp::g_app->SendToScened(pkt, fd);
//				return false;
//			});
//		}
//		return index;
//	}

	private boolean playerJoin(LoginPlayer player) {
		
//		uint32 mapid = player->GetTeleportMapID();
//		uint32 instanceid = player->GetSession()->to_instanceid;
//		uint32 lineno = player->GetTeleportLineNo();
//		tea_pdebug("player %s join map %u begin, instanceid %u, lineno %u", player->GetGuid().c_str(), mapid, instanceid, lineno);
//		const MapTemplate *mt = MapTemplate::GetMapTempalte(mapid);	
//		ASSERT(mt);		//传送心跳里已经验证过了，这里要是找不到就直接断言吧
//		mt = MapTemplate::GetMapTempalte(mt->GetParentMapid());
//		if (!mt)
//		{
//			tea_perror("player %s join map %u, Parent MapTemplate not find", player->GetGuid().c_str(), mapid);
//			return false;
//		}
//		
//		int32 index = 0;
//		if(instanceid)
//			index = findInstance(instanceid);				//如果是加入已有实例
//		else
//			index = HandleGetInstance(player, mt, lineno, mapid);
//
//		if (index >= 0)
//		{
//			//旧实例删除对象
//			DelPlayer(player);
//			AddPlayer(index, player);
//			tea_pdebug("player %s join map %u end", player->GetGuid().c_str(), mapid);
//			return true;
//		}
//		else
//		{
//			//创建实例失败
//			Call_operation_failed(player->GetSession()->m_delegate_sendpkt,OPRATE_TYPE_LOGIN,OPRATE_RESULT_SCENED_CLOSE,"");
//			player->GetSession()->Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE22,"");
//			tea_perror("player:%s teleport fail! mapid:%u,instid:%u,lineno:%u", player->GetGuid().c_str(), mapid, instanceid, lineno);
//			return false;
//		}
		return false;
	}
	
	private boolean isInstanceOfflineReenter(int mapId) {
		// TODO: 查看配表是否能够重进
		return false;
	}

	//玩家登录时的逻辑
	public void playerLogin(LoginPlayer player) {
		
		int instanceid = 0;
		PlayerInstInfo playerInstInfo = playerInstInfoHash.get(player.getGuid());

		//如果传送没有成功，玩家数据保留下来，修改数据后重新发起传送
		if (player.getMapId() < 0 && player.getTeleportMapID() > 0) {
			player.setMapId(player.getTeleportMapID());
			player.setMapLineNo(player.getTeleportLineNo());
			player.setPosition(player.getTeleportPosX(), player.getTeleportPosY());
		}

		//先确认副本信息里有没有
		if (playerInstInfo != null
			&& playerInstInfo.isExpired()
			&& this.findInstance(playerInstInfo.getInstId()) >= 0	//如果找不到该实例,也是无效的
			&& this.isInstanceOfflineReenter(playerInstInfo.getMapId()) // 如果副本不可重新进的话
			) {
			
			playerInstInfo.setExpire(0);
			instanceid = playerInstInfo.getInstId();
			player.setMapId(playerInstInfo.getMapId());
			player.setPosition(playerInstInfo.getX(), playerInstInfo.getY());
			
		} else {
			
			player.relocateDBPosition();
			
			//如果从数据库load出来是副本地图,则置为主城,一般出现这种情况都是异常数据
			MapTemplate mt = MapTemplate.getTemplate(player.getMapId());
			if (mt == null || mt.isInstance()) {
				player.setMapId(ZHUCHENG_DITU_ID);
				player.setPosition(ZHUCHENG_FUHUO_X, ZHUCHENG_FUHUO_Y);
			}

			//跨服回来的玩家 TODO:
//			if (!player->IsKuafuPlayer() && player->GetFlags(PLAYER_APPD_INT_FIELD_FLAGS_FROM_KUAFU)) {
//				int map_id;
//				float x,y;
//				player->GetToDBPosition(map_id, x, y);
//				if (map_id > 0 && !DoIsKuafuMapID(map_id))
//				{
//					player->SetMapId(map_id);
//					player->SetPosition(x,y);				
//				}			
//			}
		}
		
		//传送,如果是pk服的话则设成跨地图id和坐标
//		boolean kuafu = player->IsKuafuPlayer()
		boolean kuafu = false;
		if (kuafu) {
			//游戏服
			player.setTeleportInfo(
					player.getMapId(), 
					player.getPositionX(), 
					player.getPositionY(), 
					0, 
					player.getTeleportExt());
		} else {
			//pk服 根据跨服类型（m_kuafutype）来选择地图id TODO:
//			LogindContext *context = player->GetSession();
//			ASSERT(context);
//			if (!DoSelectKuafuMapid(player, context->m_warid, context->m_kuafutype, context->m_number, context->m_kuafu_reserve, context->m_kuafu_reserve_str))
//			{//没有找到对应的地图id和坐标，则把玩家传回到游戏服
//				context->GoBackToGameSvr();
//				return;
//			}		
		}
		
		player.setTeleportInstanceId(instanceid);
	}

//	//玩家登出时的逻辑
//	void MapManager::PlayerLogout(logind_player *player)
//	{
//		//已经加入地图的等待场景服释放这个实例
//		int index = DelPlayer(player);
//		if(index >= 0 && !player->IsKuafuPlayer())
//		{//游戏服才走这个逻辑
//			int map_id = GetMapID(index);
//			if(map_id > 0)
//			{
//				const MapTemplate *mt = MapTemplate::GetMapTempalte(map_id);
//				if(mt)
//				{
//					//如果是在副本内,为他保留10分钟,但如果是新手村的话则不保留 TODO:考虑一下本来就是要刷新的情况
//					if(mt->IsInstance() && map_id != BORN_MAP)
//					{
//						RecordInstance(player);	
//						//player->RelocateDBPosition();
//					}
//				}
//			}
//		}
//	}
//
//	//记录玩家副本记录
//	void MapManager::RecordInstance(logind_player *player)
//	{
//		const MapTemplate *mt = MapTemplate::GetMapTempalte(player->GetMapId());
//		if (!DoIsRecordIntanceInfo(player, player->GetMapId(), mt->GetMapBaseInfo().is_instance, mt->GetMapBaseInfo().instance_type))
//			return;
//
//		if (!player->GetInstanceId())
//		{
//			return;
//		}
//
//		PlayerInstanceInfo info = {player->GetGuid(),
//			uint32(time(NULL)+INSTANCE_DEL_TIME),
//			player->GetInstanceId(),(int)player->GetMapId(),
//			player->GetPositionX(),player->GetPositionY()
//		};
//		m_playerInstInfo[info.guid] = info;
//	}

	//删除玩家副本记录信息
	private void delRecordInstance(LoginPlayer player) {
		playerInstInfoHash.remove(player.getGuid());
	}
	
//
//	//根据地图模板的类型的副本类型进行控制
//	int32 MapManager::HandleGetInstance(logind_player *player, const MapTemplate *mt,uint32 lineno, uint32 mapid)
//	{
//		uint16 inst_type = mt->GetMapBaseInfo().instance_type; //副本类型见枚举MapInstanceType
//		uint32 parent_map = mt->GetParentMapid();
//
//		//判断一下传送ID
//		bool need_general_id = false;
//		DoIsNeedGeneralid(parent_map, need_general_id);
//		if(need_general_id)
//		{
//			if(player->GetTeleportGuid().empty())
//			{
//				tea_pwarn("MapManager::HandleGetInstance player->GetTeleportGuid().empty() %s", player->guid().c_str());
//				return -1;
//			}
//		}
//
//		//找不到就创建
//		int32 index;
//		string general_id;			//如果不需要generalid那就应该传空
//		if (need_general_id)
//			general_id = player->GetTeleportGuid();
//		DoFindOrCreateMap(mapid, inst_type, general_id, lineno, index);
//		return index;
//	}
//
//	//创建新的地图实例
//	int32 MapManager::CreateInstance(uint32 mapid, const string &general_id,uint32 lineno)
//	{
//		const MapTemplate *mt = MapTemplate::GetMapTempalte(mapid);
//		uint16 inst_typ = mt->GetMapBaseInfo().instance_type;
//		uint32 parent_id = mt->GetParentMapid();
//		//uint32 instance_id = GetNewInstanceID();
//		uint32 instance_id = 0;
//		uint32 conn = 0;
//		if(parent_id != mapid)
//		{
//			int32 index = findInstance(parent_id, general_id, lineno);
//			if(index < 0)
//			{
//				index = CreateInstance(parent_id, general_id, lineno);
//			}
//			ASSERT(index >= 0);
//			instance_id = GetInstanceID(index);
//			conn = GetScenedConn(findInstance(parent_id, general_id, lineno));
//		}
//		else
//		{
//			instance_id = GetNewInstanceID();
//			conn = DoGetScenedFDByType(inst_typ, mapid);
//		}
//
//		tea_pdebug("MapManager::CreateInstance inst_typ = %u, parent_id = %u, instance_id = %u, conn = %u begin"
//			, inst_typ, parent_id, instance_id, conn);
//		
//		if (conn == 0)
//		{
//			tea_pdebug("MapManager::CreateInstance find scened fail, mapid %u, gengeral_id %s, lineno %u", mapid, general_id.c_str(), lineno);
//			return -1;
//		}
//		
//		//开始增加地图信息
//		//找个空位
//		int32 result = ForEach([&](uint32 index){
//			if(GetInstanceID(index) == 0)
//			{
//				SetMapInstanceInfo(index, instance_id, mapid, lineno, conn, general_id);
//				return true;
//			}
//			return false;
//		});
//		//没找到空位，放到后面
//		if(result < 0)
//		{
//			result = length_uint32() / MAX_MAP_INSTANCE_INT_TYPE;
//			SetMapInstanceInfo(result, instance_id, mapid, lineno, conn, general_id);
//		}
//		//创建该地图的玩家信息binlog
//		const char *guid = GetInstancePlayerInfoID(instance_id);
//		BinLogObject *blog = new BinLogObject(core_obj::SYNC_SLAVE | core_obj::SYNC_LOCAL);
//		blog->SetBinlogMaxSize(core_obj::SyncEventRecorder::MAX_BINLOG_SIZE_UNLIME);
//		blog->SetGuid(guid);
//		blog->SetOwner(MAP_INSTANCE_PLAYER_INFO_OWNER_STRING);
//		ObjMgr.CallPutObject(blog,[guid](bool){
//			ObjMgr.InsertObjOwner(guid);
//		});
//		//通知场景服创建地图实例
//		WorldPacket pkt_scened(INTERNAL_OPT_ADD_MAP_INSTANCE);
//		pkt_scened << instance_id << mapid << lineno << general_id;
//		LogindApp::g_app->SendToScened(pkt_scened, conn);
//
//		//创建地图标记
//		if (DoIsWorldMapID(mapid)) {
//			MapManager::lineCreated(mapid, lineno, instance_id);
//		}
//
//		tea_pdebug("MapManager::CreateInstance end");
//		return result;
//	}
//
//	//关闭某一地图
//	bool MapManager::TryClose(uint32 instance_id)
//	{
//		if(instance_id == 0)
//			return false;
//
//		//为了给下面的打印用，从循环里挪出来了
//		int32 index = findInstance(instance_id);
//		if(index < 0)
//			return true;
//		int mapid = GetMapID(index);
//		uint32 lineNo = GetLineNo(index);
//		uint32 scened_conn = GetScenedConn(index);
//
//		//看看还有没有玩家在里面
//		string player_info_id = GetInstancePlayerInfoID(instance_id);
//		BinLogObject *binlog = dynamic_cast<BinLogObject*>(ObjMgr.Get(player_info_id));
//		if(!binlog)
//		{
//			tea_perror("MapManager::TryClose GetInstancePlayerInfoID==NULL  %s", player_info_id.c_str());
//			return false;
//		}
//		uint32 str_s = binlog->length_str();
//		for (uint32 i = MAP_INSTANCE_PLAYER_INFO_START_FIELD; i < str_s; i++)
//		{
//			if(!binlog->GetStr(i).empty())
//			{
//				tea_pinfo("MapManager::TryClose has player %s  %s, mapid = %d, conn = %u"
//					, binlog->GetStr(i).c_str(), player_info_id.c_str(), mapid, scened_conn);
//				return false;
//			}
//		}
//
//		bool call_del = false;
//		while(true)
//		{
//			//如果场景服连接还活着，关闭吧
//			if(!call_del && scened_conn)
//			{
//				CallDelMapInstance(index);
//				call_del = true;
//				//请掉这个地图内的玩家数据
//				ObjMgr.CallRemoveObject(player_info_id);
//			}
//			//清空地图信息
//			SetMapInstanceInfo(index, 0, 0, 0, 0, "");
//
//			index = findInstance(instance_id);
//			if(index < 0)
//				break;
//		}
//		ASSERT(call_del);
//
//		// 看下删不删
//		if (DoIsWorldMapID(mapid)) {
//			MapManager::lineRemoved(mapid, lineNo);
//		}
//
//		tea_pdebug("MapManager::TryClose instance_id = %u, end", instance_id);
//		return true;
//	}
//
//	void MapManager::CallDelMapInstance(uint32 index)
//	{
//		WorldPacket pkt_scened(INTERNAL_OPT_DEL_MAP_INSTANCE);
//		pkt_scened << GetMapID(index)<< GetInstanceID(index);
//		uint32 m_scened_conn = GetScenedConn(index);
//		LogindApp::g_app->SendToScened(pkt_scened, m_scened_conn);
//	}
//
//	// 场景服异常终止处理 
//	bool MapManager::OnScenedClosed(int32 scened_id)
//	{
//		tea_pinfo("scened %u close", scened_id);
//		//让玩家重新传送
//		bool result = true;
//		ForEach([&](uint32 index){
//			if (GetScenedConn(index) == scened_id)
//			{
//				ClearInstanceAndTelePlayer(scened_id, index);
//			}
//			return false;//要遍历所有地图信息
//		});
//		return result;
//	}
//
//	void MapManager::Close()
//	{
//		ForEach([&](uint32 index){
//			if(GetInstanceID(index))
//			{
//				//todo
//			}
//			return false;
//		});
//		
//	}

	//根据实例ID进行查找地图实例
	private int findInstance(int instanceid) {
		for (MapInstInfo mapInstInfo: mapInstInfoHash.values()) {
			if (mapInstInfo.getInstId() == instanceid) {
				return mapInstInfo.getIndx();
			}
		}
		
		return -1;
	}

	//查找地图实例
	private int findInstance(int mapId, final String ext, int lineNo) {
		for (MapInstInfo mapInstInfo: mapInstInfoHash.values()) {
			if (mapInstInfo.getInstId() > 0
				&& mapInstInfo.getParentId() == mapId
				&& ext.equals(mapInstInfo.getExt())
				&& mapInstInfo.getLineNo() == lineNo) {
				return mapInstInfo.getIndx();
			}
		}
		
		return -1;
	}

	//根据general_id查找
	private int findInstance(final String ext) {
		if (ext == null || ext.isEmpty()) {
			return -1;
		}
		
		for (MapInstInfo mapInstInfo: mapInstInfoHash.values()) {
			if (mapInstInfo.getInstId() > 0
				&& ext.equals(mapInstInfo.getExt())) {
				return mapInstInfo.getIndx();
			}
		}
		
		return -1;
	}

//	//登录服重启修复
//	void MapManager::Restart()
//	{
//
//	}
//
//	//地图包路由
//	void MapManager::HandleMapPkt(packet *pkt)
//	{
//		uint32 to_instance_id, to_map_id, src_instance_id, src_map_id;
//		string to_general_id, src_general_id;
//		*pkt >> to_instance_id >> to_map_id >> to_general_id >> src_instance_id >> src_map_id >> src_general_id;
//		uint32 lineo = 0;
//		const MapTemplate *mt = MapTemplate::GetMapTempalte(to_map_id);
//		ASSERT(mt);
//
//		int index;
//		DoFindOrCreateMap(mt->GetMapBaseInfo().mapid, mt->GetMapBaseInfo().instance_type, to_general_id, lineo, index);
//		ASSERT(index >= 0);
//
//		to_instance_id = GetInstanceID(index);
//		uint32 fd = GetScenedConn(index);
//		ASSERT(to_instance_id);
//		memcpy(pkt->content, &to_instance_id, sizeof(to_instance_id));
//		WorldPacket _pkt(*pkt);
//		LogindApp::g_app->SendToScened(_pkt, fd);
//	}
//
//	//检查场景服状态，为崩溃的场景服玩家重新传送
//	void MapManager::UpdateScenedStatus()
//	{
//		//当前帧场景服数量
//		DoGetScenedSize(m_scened_size);
//
//		//当前没有场景服，那也没有传送的必要了
//		if(m_scened_size == 0)
//			return;
//		//如果是满场景服，看看是不是等待超时了
//		bool scened_ready = m_scened_size >= g_Config.max_secend_count;
//		if(!scened_ready)
//		{
//			time_t t = time(NULL);
//			if(m_scened_collapse_time && t - m_scened_collapse_time >= g_Config.scened_collapse_time_out)
//			{
//				//tea_pwarn("MapManager::UpdateScenedStatus() timeout.");
//				scened_ready = true;
//			}
//			if(m_scened_collapse_time == 0)
//				m_scened_collapse_time = t;
//		}
//		if(!scened_ready)
//			return;
//
//		m_scened_collapse_time = 0;
//		//看一下地图实例所在的场景服是不是挂了，如果挂了就摧毁他！
//		//每帧最多修复100个地图就好。。
//		for (int i = 0; i < 100; i++)
//		{
//			//每个foreach修复一张地图，否则迭代关系会乱掉
//			if(ForEach([this](uint32 index){
//					uint32 fd = GetScenedConn(index);
//					if(fd && !ServerList.HasScenedFd(fd))
//					{
//						OnScenedClosed(fd);
//						return true;
//					}
//					return false;
//				}) == -1)
//				break;
//		}
//	}
//
//	//重新传送玩家
//	void MapManager::ReTelePlayer(logind_player *player, uint32 scened_id)
//	{
//		if(!player->GetSession() || player->GetSession()->GetStatus() != STATUS_LOGGEDIN)
//		{
//			tea_pdebug("scened %u coolapse,but player %s is outline", scened_id, player->guid().c_str());
//			return;
//		}
//		tea_pdebug("scened %u coolapse, player %s start tele", scened_id, player->guid().c_str());
//
//		Call_operation_failed(player->GetSession()->m_delegate_sendpkt,OPRATE_TYPE_LOGIN ,OPRATE_RESULT_SCENED_ERROR,"");
//		player->SetTeleportInfo(player->GetMapId(), player->GetPositionX(), player->GetPositionY(), 0, player->GetTeleportGuid());
//	}
//
//	void MapManager::ClearInstanceAndTelePlayer(uint32 scened_id, uint32 index)
//	{
//		uint32 instance_id = GetInstanceID(index);
//		const char *player_info_id = GetInstancePlayerInfoID(instance_id);
//		BinLogObject *binlog = dynamic_cast<BinLogObject*>(ObjMgr.Get(player_info_id));
//		if(!binlog)
//		{
//			tea_perror("MapManager::ClearInstanceAndTelePlayer GetInstancePlayerInfoID==NULL  %s", player_info_id);
//			ObjMgr.ForEachPlayer([&](logind_player *player){
//				if(player->GetInstanceId() == instance_id)
//				{
//					ReTelePlayer(player,scened_id);
//				}
//			});
//		}
//		else
//		{
//			uint32 str_s = binlog->length_str();
//			tea_pdebug("scened %u coolapse, instance %u reset", scened_id, instance_id);
//			//遍历图内所有玩家，修改他们的传送下标
//			//当玩家的update再次跳到传送的时候，就会给他换个场景服了
//			for (uint32 i = MAP_INSTANCE_PLAYER_INFO_START_FIELD; i < str_s; i++)
//			{
//				string player_guid = binlog->GetStr(i);
//				if(player_guid.empty())
//				{
//					continue;
//				}
//				logind_player *player = LogindContext::FindPlayer(player_guid);
//				if(!player)
//				{
//					tea_pdebug("scened %u coolapse,but player %s not find", scened_id, player_guid.c_str());
//					continue;
//				}
//				ReTelePlayer(player, scened_id);
//			}
//		}
//		//清除这个地图数据
//		SetMapInstanceInfo(index, 0, 0, 0, 0, "");
//		//请掉这个地图内的玩家数据
//		ObjMgr.CallRemoveObject(player_info_id);
//	}
//
//	//场景服全部崩光了以后，清理所有的地图信息。
//	void MapManager::ClearMapInstance()
//	{
//
//	}
//
//	//计算玩家数量
//	uint32 MapManager::PlayerCount(uint32 index)
//	{
//		if(index == -1)
//			return 0;
//
//		string player_info_id = GetInstancePlayerInfoID(GetInstanceID(index));
//		BinLogObject *binlog = dynamic_cast<BinLogObject*>(ObjMgr.Get(player_info_id));
//		if(!binlog)
//		{
//			return 0;
//		}
//		uint32 str_s = binlog->length_str();
//		uint32 count = 0;
//		for (uint32 i = MAP_INSTANCE_PLAYER_INFO_START_FIELD; i < str_s; i++)
//		{
//			if(!binlog->GetStr(i).empty())
//			{
//				++count;
//			}
//		}
//
//		return count;
//	}
}
