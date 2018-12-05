package cc.mi.login.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.mi.core.algorithm.IndexTree;
import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.constance.InstanceConst;
import cc.mi.core.constance.MapTypeConst;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.generate.msg.CreateMap;
import cc.mi.core.generate.msg.DeleteMap;
import cc.mi.core.generate.msg.JoinMapMsg;
import cc.mi.core.generate.msg.PlayerLeaveMap;
import cc.mi.core.impl.Tick;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.MapTemplateManager;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.GuidManager;
import cc.mi.core.server.SessionStatus;
import cc.mi.core.utils.TimestampUtils;
import cc.mi.core.xlsxData.MapTemplate;
import cc.mi.login.table.MapInstInfo;
import cc.mi.login.table.PlayerInstInfo;

public class LoginMapManager implements Tick {
	
	static final CustomLogger logger = CustomLogger.getLogger(LoginMapManager.class);
	public static final LoginMapManager INSTANCE = new LoginMapManager();

	private final int ZHUCHENG_DITU_ID = 1;
	private final float ZHUCHENG_FUHUO_X = 100;
	private final float ZHUCHENG_FUHUO_Y = 100;
	
	private int sceneCollapseTime;
	private int sceneCnt;
	
	private byte teleportCallbackIndex = 0;
	
	private final Map<String, PlayerInstInfo> playerInstInfoHash = new HashMap<>();
	// 地图实例信息
	private final Map<Integer, MapInstInfo> mapInstInfoHash = new HashMap<>();
	// 副本里面要进入玩家guid
	private final Map<Integer, Set<String>> instPlayerInHash = new HashMap<>();
	
	// 地图下标索引信息
	private final IndexTree indexTree = new IndexTree(5000);
	
	private LoginMapManager() {}
	

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
			
			MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(context.getTeleMapId());
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
					|| !context.getTeleExt().equals(player.getTeleportExt()))) {
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
		
		MapTemplate toMt = MapTemplateManager.INSTANCE.getTemplate(context.getTeleMapId());
		MapTemplate frMt = MapTemplateManager.INSTANCE.getTemplate(player.getMapId());
		// 从副本到主城
		if (frMt.isInstance() && !toMt.isInstance()) {
			delRecordInstance(player);
		}

		//加入地图并返回到新实例
		playerJoin(context);
		logger.devLog("teleportTo end");
	}

	private void callJoinMap(int index, LoginContext context) {
		
		logger.devLog("callJoinMap {} call join map begin", context.getGuid());
		MapInstInfo mapInstInfo = this.mapInstInfoHash.get(index);
		int scenedConn = mapInstInfo.getSceneConn();
		logger.devLog("callJoinMap scenedConn = {}", scenedConn);
		if (scenedConn <= 0) {
			//TODO: 关闭的type
			context.closeSession(0);
			return;
		}

		//通知场景服，玩家加入地图
		LoginPlayer player = context.getPlayer();
		player.setTeleportSign(++ teleportCallbackIndex);
		JoinMapMsg jmm = new JoinMapMsg();
		jmm.setFd(context.getFd());
		jmm.setInstId(mapInstInfo.getInstId());
		jmm.setOwnerId(player.getGuid());
		jmm.setSign(teleportCallbackIndex);
		jmm.setTeleMapId(player.getTeleportMapID());
		jmm.setX(player.getTeleportPosX());
		jmm.setY(player.getTeleportPosY());
		jmm.setBaseFd(scenedConn);
		
		LoginServerManager.getInstance().sendToCenter(jmm);
		//传送完毕，设置场景服FD
		player.setSceneFd(scenedConn);

//		//场景服计数加1
//		DoAddScenedPlayer(m_scened_conn);
		logger.devLog("callJoinMap player {} call join map end", player.getGuid());
	}

	protected void addPlayer(int index, LoginContext context) {

		MapInstInfo mapInstInfo = this.mapInstInfoHash.get(index);
		int instId = mapInstInfo.getInstId();
		if (!this.instPlayerInHash.containsKey(instId)) {
			//加入玩家信息
			int scenedConn = mapInstInfo.getSceneConn();
			logger.devLog("addPlayer GetInstancePlayerInfoID==NULL  {} , scened fd {}", context.getGuid(), scenedConn);
//			//把这个场景服关闭
//			WorldPacket pkt(INTERNAL_OPT_CLOSE_SCENED);
//			LogindApp::g_app->SendToScened(pkt, m_scened_conn);
//			//把这个玩家关掉
//			if(player->GetSession()){
//				player->GetSession()->Close(PLAYER_CLOSE_OPERTE_LOGDIN_ONE21,"");
//			}
			return;
		}
		
		this.instPlayerInHash.get(instId).add(context.getGuid());
		//加入地图
		this.callJoinMap(index, context);	
	}
	
	
	private int getMapInstInfoIndex(String guid) {
		
		for (MapInstInfo mapInstInfo: mapInstInfoHash.values()) {
			int instId = mapInstInfo.getInstId();
			if (this.instPlayerInHash.containsKey(instId)) {
				if (this.instPlayerInHash.get(instId).contains(guid)) {
					return mapInstInfo.getIndx();
				}
			}
		}
		return -1;
	}

	private int delPlayer(LoginContext context) {
		//TODO:由于存在网络同步问题，所以不信任玩家身上的instanceid
		int index = this.getMapInstInfoIndex(context.getGuid());
		//场景服计数减1
		if (index >= 0) {
			MapInstInfo instInfo = this.mapInstInfoHash.get(index);
			int mapid	= instInfo.getParentId();
			int instid	= instInfo.getInstId();
			int sceneFd = instInfo.getSceneConn();
			logger.devLog("player leave map {} {} {}", context.getGuid(), mapid, instid);
//TODO:			DoSubScenedPlayer(fd);
			PlayerLeaveMap packet = new PlayerLeaveMap();
			packet.setGuid(context.getGuid());
			packet.setClientFd(context.getFd());
			packet.setBaseFd(sceneFd);
			LoginServerManager.getInstance().sendToCenter(packet);
		} else {
			//找不到有两种情况
			//1.玩家登录传送，这种没办法
			//2.ObjectTypeMapPlayerInfo丢失
			//解决方案是，所有场景服都发一次离开场景服
			//风险是场景服人数统计会失效，不用修复了
			List<Integer> sceneList = LoginServerManager.getInstance().getConnList().getSceneConns();
			for (int sceneFd : sceneList) {
				PlayerLeaveMap packet = new PlayerLeaveMap();
				packet.setGuid(context.getGuid());
				packet.setClientFd(context.getFd());
				packet.setBaseFd(sceneFd);
				LoginServerManager.getInstance().sendToCenter(packet);
			}
		}
		return index;
	}

	private boolean playerJoin(LoginContext context) {
		
		LoginPlayer player = context.getPlayer();
		int mapId  = player.getTeleportMapID();
		int instId = context.getTeleInstId();
		int lineNo = player.getTeleportLineNo();
		logger.devLog("player {} join map {} begin, instanceid {}, lineno {}", player.getGuid(), mapId, instId, lineNo);
		
		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
		if (mt == null || !MapTemplateManager.INSTANCE.containsTemplate(mapId)) {
			logger.devLog("player {} join map {}, Parent MapTemplate not find", player.getGuid(), mapId);
			return false;
		}
		
		int index = 0;
		if (instId > 0) {
			index = this.findInstance(instId);				//如果是加入已有实例
		} else {
			index = this.handleGetInstance(player, mt, lineNo, mapId);
		}
		
		if (index > 0) {
			//旧实例删除对象
			this.delPlayer(context);
			this.addPlayer(index, context);
			logger.devLog("player {} join map {} end", player.getGuid(), mapId);
			return true;
		}
		//创建实例失败
		context.closeSession(0);
//			Call_operation_failed(player->GetSession()->m_delegate_sendpkt,OPRATE_TYPE_LOGIN,OPRATE_RESULT_SCENED_CLOSE,"");
		logger.devLog("player:{} teleport fail! mapid:{},instid:{},lineno:{}", player.getGuid(), mapId, instId, lineNo);
		return false;
	}
	
	private boolean isInstanceOfflineReenter(int mapId) {
		// TODO: 查看配表是否能够重进
		return false;
	}

	//玩家登录时的逻辑
	public void playerLogin(LoginContext context) {
		
		LoginPlayer player = context.getPlayer();
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
			MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(player.getMapId());
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

	//玩家登出时的逻辑
	public void playerLogout(LoginContext context) {
		//已经加入地图的等待场景服释放这个实例
		int index = this.delPlayer(context);
		boolean kuafu = false; //player->IsKuafuPlayer()
		
		if (index >= 0 && !kuafu) {
			//游戏服才走这个逻辑
			int mapId = this.mapInstInfoHash.get(index).getParentId();
			if (mapId > 0) {
				MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
				if (mt != null) {
					//如果是在副本内,为他保留10分钟,但如果是新手村的话则不保留 TODO:考虑一下本来就是要刷新的情况
					if (mt.isInstance()) {
						this.recordInstance(context);	
					}
				}
			}
		}
	}

	//记录玩家副本记录
	private void recordInstance(LoginContext context) {
		
		LoginPlayer player = context.getPlayer();
//		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(player.getMapId());
//		if (!DoIsRecordIntanceInfo(player, player->GetMapId(), mt->GetMapBaseInfo().is_instance, mt->GetMapBaseInfo().instance_type))
//			return;

		if (player.getInstanceId() <= 0) {
			return;
		}
		PlayerInstInfo playerInstInfo = new PlayerInstInfo(player.getGuid(), player.getInstanceId(), player.getMapId());
		playerInstInfo.setExpire(TimestampUtils.now() + InstanceConst.INSTANCE_DEL_TIME);
		playerInstInfo.setX(player.getPositionX());
		playerInstInfo.setY(player.getPositionY());
		this.playerInstInfoHash.put(player.getGuid(), playerInstInfo);
	}

	//删除玩家副本记录信息
	private void delRecordInstance(LoginPlayer player) {
		playerInstInfoHash.remove(player.getGuid());
	}

	//根据地图模板的类型的副本类型进行控制
	int handleGetInstance(LoginPlayer player, MapTemplate mt, int lineNo, int mapId) {
		
		int instType = mt.getBaseInfo().getType(); //副本类型见枚举MapTypeConst
		int parentMap = mt.getBaseInfo().getParentId();

		//判断一下传送ID
		boolean needGeneral = this.isNeedGeneral(parentMap);
		String ext = null;		//如果不需要generalid那就应该传空
		if (needGeneral) {
			ext = player.getTeleportExt();
			if (ext == null || ext.isEmpty()) {
				logger.devLog("HandleGetInstance teleport ext {} is empty", player.getTeleportExt());
				return -1;
			}
		}

		//找不到就创建
		int index = findOrCreateMap(mapId, instType, ext, lineNo);
		return index;
	}
	
	private boolean isNeedGeneral(int mapId) {
		//TODO:
		return false;
	}
	
	private void setMapInstanceInfo(int index, MapInstInfo mapInstInfo) {
		this.mapInstInfoHash.put(index, mapInstInfo);
	}
	
	private void removeMapInstanceInfo(int index) {
		this.mapInstInfoHash.remove(index);
	}
	
	private int getWantedLineNo(int mapId, int lineNo) {
		return 1;
	}
	
	private int findOrCreateMap(int mapId, int instType, String ext, int lineNo) {
		
		if (instType == MapTypeConst.MAP_TYPE_INSTANCE) {
			return this.createInstance(mapId, ext, lineNo);
		}
		
		//找一下真正该去的分线号
		int lineNoTrue = this.getWantedLineNo(mapId, lineNo);
		
		//遍历实例查找该地图
		int index = this.findInstance(mapId, ext, lineNoTrue);
		logger.devLog("---------FindOrCreateMap {} {} '{}' {} result = {}", mapId, instType, ext, lineNoTrue, index);
		if (index < 0) {
			index = this.createInstance(mapId, ext, lineNoTrue);	//如果没有就创建一个新的实例
		}
		return index;
	}

	//创建新的地图实例
	private int createInstance(int mapId, String ext, int lineNo) {
		
		MapTemplate mt = MapTemplateManager.INSTANCE.getTemplate(mapId);
		int instType = mt.getBaseInfo().getType();
		int parentId = mt.getBaseInfo().getParentId();
		
		int instId = 0;
		int conn = 0;
		
		if (parentId != mapId) {
			int index = this.findInstance(parentId, ext, lineNo);
			if(index < 0) {
				index = this.createInstance(parentId, ext, lineNo);
			}
			instId = this.getInstanceID(index);
			conn = this.getScenedConn(this.findInstance(mapId, ext, lineNo));
		} else {
			instId = this.getNewInstanceID();
			conn = this.getScenedFDByMapID(mapId);
		}

		logger.devLog("createInstance instType = {}, parentId = {}, instId = {}, conn = {} begin"
			, instType, parentId, instId, conn);
		
		if (conn == 0) {
			logger.devLog("createInstance find scened fail, mapid {}, gengeral_id {}, lineno {}", mapId, ext, lineNo);
			return -1;
		}
		
		//开始增加地图信息
		//找个空位
		int result = this.indexTree.newIndex();
		if (result < 0) {
			throw new RuntimeException("mapinstinfo index < 0");
		}
		MapInstInfo mapInstInfo = new MapInstInfo(result, instId, parentId, lineNo, conn, ext);
		this.setMapInstanceInfo(result, mapInstInfo);
		if (!this.instPlayerInHash.containsKey(instId)) {
			this.instPlayerInHash.put(instId, new HashSet<>());
		}
		//通知场景服创建地图实例
		CreateMap cm = new CreateMap();
		cm.setBaseFd(conn);
		cm.setInstId(instId);
		cm.setMapId(mapId);
		cm.setLineNo(lineNo);
		cm.setExt(ext);
		LoginServerManager.getInstance().sendToCenter(cm);

//		//创建地图标记
//		if (DoIsWorldMapID(mapid)) {
//			MapManager::lineCreated(mapid, lineno, instance_id);
//		}
//
		logger.devLog("createInstance end");
		return result;
	}
	
	private int getScenedFDByMapID(int mapId) {
		return LoginServerManager.getInstance().getConnList().getSceneConns().get(0);
	}

	//关闭某一地图
	public boolean tryClose(int instId) {
		
		if (instId == 0) {
			return false;
		}
		//为了给下面的打印用，从循环里挪出来了
		int index = this.findInstance(instId);
		if(index < 0) {
			return true;
		}
		
		MapInstInfo mapInstInfo = this.mapInstInfoHash.get(index);
		int sceneFd = mapInstInfo.getSceneConn();

		//看看还有没有玩家在里面
		if (!this.instPlayerInHash.containsKey(instId)) {
			logger.devLog("tryClose instPlayerInHash is null instId = {}", instId);
			return false;
		}
		
		boolean callDel = false;
		while(true) {
			//如果场景服连接还活着，关闭吧
			if(!callDel && sceneFd > 0) {
				this.callDelMapInstance(index);
				callDel = true;
				//请掉这个地图内的玩家数据
				this.instPlayerInHash.remove(instId);
			}
			//清空地图信息
			this.removeMapInstanceInfo(index);

			index = this.findInstance(instId);
			if (index < 0) {
				break;
			}
		}

		// 看下删不删
//		if (DoIsWorldMapID(mapid)) {
//			MapManager::lineRemoved(mapid, lineNo);
//		}
		
		logger.devLog("tryClose instId = {} end", instId);
		return true;
	}

	private void callDelMapInstance(int index) {
		MapInstInfo mapInstInfo = this.mapInstInfoHash.get(index);
		DeleteMap packet = new DeleteMap();
		packet.setBaseFd(mapInstInfo.getSceneConn());
		packet.setInstId(mapInstInfo.getInstId());
		packet.setMapId(mapInstInfo.getParentId());
		LoginServerManager.getInstance().sendToCenter(packet);
	}

	// 场景服异常终止处理 
	public boolean onScenedClosed(int scenedId) {
		logger.devLog("scened {} close", scenedId);
		//让玩家重新传送
		for (MapInstInfo mapInstInfo: mapInstInfoHash.values()) {
			if (mapInstInfo.getSceneConn() == scenedId) {
				this.clearInstanceAndTelePlayer(scenedId, mapInstInfo.getIndx());
			}
		}
		
		return true;
	}
	
	public void close() {
//		ForEach([&](uint32 index){
//			if(GetInstanceID(index))
//			{
//				//todo
//			}
//			return false;
//		});
		
	}
	
	private int getScenedConn(int index) {
		return mapInstInfoHash.get(index).getSceneConn();
	}
	
	private int getNewInstanceID() {
		return GuidManager.INSTANCE.newIndex(ObjectType.MAP);
	}
	
	private int getInstanceID(int index) {
		if (!mapInstInfoHash.containsKey(index)) {
			return -1;
		}
		return mapInstInfoHash.get(index).getInstId();
	}

	//根据实例ID进行查找地图实例
	protected int findInstance(int instanceid) {
		for (MapInstInfo mapInstInfo: mapInstInfoHash.values()) {
			if (mapInstInfo.getInstId() == instanceid) {
				return mapInstInfo.getIndx();
			}
		}
		
		return -1;
	}

	//查找地图实例
	protected int findInstance(int mapId, final String ext, int lineNo) {
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
	protected int findInstance(final String ext) {
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

	//重新传送玩家
	private void reTelePlayer(LoginPlayer player, int scenedId) {
		logger.devLog("scened {} coolapse, player {} start tele", scenedId, player.getGuid());
		int fd = player.getFd();
		LoginContext context = (LoginContext)ContextManager.INSTANCE.getContext(fd);
		if (context == null || context.getStatus() != SessionStatus.STATUS_LOGGEDIN) {
			logger.devLog("scened {} coolapse,but player %s is outline", scenedId, player.getGuid());
			return;
		}
//		Call_operation_failed(player->GetSession()->m_delegate_sendpkt,OPRATE_TYPE_LOGIN ,OPRATE_RESULT_SCENED_ERROR,"");
		player.setTeleportInfo(player.getMapId(), player.getPositionX(), player.getPositionY(), 0, player.getTeleportExt());
	}

	public void clearInstanceAndTelePlayer(int scenedId, int index) {
		
		MapInstInfo mapInstInfo = this.mapInstInfoHash.get(index);
		int instanceId = mapInstInfo.getInstId();
		Set<String> set = this.instPlayerInHash.get(instanceId);
		
		if (set == null) {
			logger.devLog("clearInstanceAndTelePlayer GetInstancePlayerInfoID");
			LoginObjectManager.INSTANCE.foreachPlayer(new AbstractCallback<LoginPlayer>() {
				@Override
				public void invoke(LoginPlayer player) {
					LoginMapManager.INSTANCE.reTelePlayer(player, scenedId);
				}
			});
		} else {
			
			for (String guid : set) {
				LoginPlayer player = LoginObjectManager.INSTANCE.findPlayer(guid);
				if (player == null) {
					logger.devLog("scened {} coolapse,but player %s not find", scenedId, guid);
					continue;
				}
				this.reTelePlayer(player, scenedId);
			}
		}
		
		//清除这个地图数据
		this.removeMapInstanceInfo(index);
		//请掉这个地图内的玩家数据
		this.instPlayerInHash.remove(instanceId);
	}
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


	@Override
	public boolean update(int diff) {
//		UpdateScenedStatus();
		return false;
	}
}
