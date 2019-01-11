package cc.mi.login.server;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.callback.InvokeCallback;
import cc.mi.core.callback.MatchCallback;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.constance.OperateConst;
import cc.mi.core.generate.Opcodes;
import cc.mi.core.generate.msg.QueuingMsg;
import cc.mi.core.generate.stru.BinlogInfo;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.ServerManager;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.core.server.SessionStatus;
import cc.mi.core.utils.ServerProcessBlock;
import cc.mi.core.utils.TimerTimestamp;
import cc.mi.core.utils.TimestampUtils;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.handler.CheckSessionHandler;
import cc.mi.login.handler.CreateCharHandler;
import cc.mi.login.handler.CreateConnectionHandler;
import cc.mi.login.handler.InnerServerConnListHandler;
import cc.mi.login.handler.PlayerLoginHandler;
import cc.mi.login.loginAction.LoginQueueManager;

public class LoginServerManager extends ServerManager {
	static final CustomLogger logger = CustomLogger.getLogger(LoginServerManager.class);
	
	private static LoginServerManager instance = new LoginServerManager();
	
	private final Queue<Integer> sessionQueue = new LinkedList<>();
	
	private final LoginQueueManager loginQueue = new LoginQueueManager();
	
	// 登录列表定时器
	private TimerTimestamp playerLoginQueueTimer;
	// 释放登出的玩家缓存定时器
	private TimerTimestamp playerFreeLogoutTimer;
	// 保存全局变量定时器
	private TimerTimestamp saveGlobalValueTimer;	//360s
	// 检测合服开始定时器 暂时不用
//	private TimerTimestamp checkMergeServerTimer;
	// 玩家备份定时器
	private TimerTimestamp playerBackupTimer;		//1800s
	// 保存玩家数据到db定时器
	private TimerTimestamp savePlayerToDbTimer;		//1s
	// 记录玩家在线人数定时器
	private TimerTimestamp onlineCountTimer;		//600s
	
	public static LoginServerManager getInstance() {
		return instance;
	}
	
	@Override
	protected void onOpcodeInit() {
		handlers.put(Opcodes.MSG_CREATECONNECTION, new CreateConnectionHandler());
		handlers.put(Opcodes.MSG_CHECKSESSION, new CheckSessionHandler());
		handlers.put(Opcodes.MSG_CREATECHAR, new CreateCharHandler());
		handlers.put(Opcodes.MSG_INNERSERVERCONNLIST, new InnerServerConnListHandler());
		handlers.put(Opcodes.MSG_PLAYERLOGIN, new PlayerLoginHandler());
		
		opcodes.addAll(handlers.keySet());
	}
	
	public LoginServerManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN);
	}
	
	@Override
	protected void onProcessInit() {
		this.process = new ServerProcessBlock() {
			@Override
			public void run(int diff) {
				instance.loadServerValue();
			}
		};
	}
	
	/**
	 * 进行帧刷新
	 */
	@Override
	protected void doWork(int diff) {
		// 初始化服务器
		this.doProcess(diff);
		// 执行定时器
		this.checkTimer();
		// 处理包信息
		this.dealPacket();
	}
	
	public void onBinlogDatasUpdated(List<BinlogInfo> binlogInfoList) {
		for (BinlogInfo binlogInfo : binlogInfoList) {
			LoginObjectManager.INSTANCE.parseBinlogInfo(binlogInfo);
		}
	}
	
	public void putObjects(String ownerId, final List<BinlogData> result, InvokeCallback<Boolean> abstractCallback) {
		LoginObjectManager.INSTANCE.putObjects(this.centerChannel, ownerId, result, abstractCallback);
	}
	
	public void putObject(String ownerId, BinlogData result, InvokeCallback<Boolean> callback) {
		LoginObjectManager.INSTANCE.putObject(this.centerChannel, ownerId, result, callback);
	}
	
	private void loadServerValue() {
		if (this.centerChannel == null) {
			return;
		}
		logger.devLog("login init");
		logger.devLog("load global value");
		
		List<BinlogData> globalList = LoginCache.INSTANCE.loadGlobalValue();
		this.putObjects(ObjectType.GLOBAL_VALUE_OWNER_STRING, globalList, null);
		
		List<BinlogData> factionList = LoginCache.INSTANCE.loadFactionValue();
		this.putObjects(ObjectType.FACTION_BINLOG_OWNER_STRING, factionList, null);
		
		this.onDataReady();
	}
	
	private void onDataReady() {
		this.process = new ServerProcessBlock() {
			@Override
			public void run(final int diff) {
				//登录队列
				loginQueue.update(diff);

				//地图管理器心跳
				LoginMapManager.INSTANCE.update(diff);
				
				//所有的player心跳
				ContextManager.INSTANCE.foreach(new InvokeCallback<ServerContext>() {
					@Override
					public void invoke(ServerContext value) {
						value.update(diff);
					}
				});
				
//				//是否是异步查询回调
//				//http服务
				
				//对象管理器
				LoginObjectManager.INSTANCE.update(diff);
			}
		};
		
		int now = TimestampUtils.now();
		this.playerLoginQueueTimer = new TimerTimestamp(now + 1);
		this.playerFreeLogoutTimer = new TimerTimestamp(now + 1);
		this.saveGlobalValueTimer  = new TimerTimestamp(now + ServerConfig.AUTO_SAVE_INTERVAL);
		this.playerBackupTimer     = new TimerTimestamp(now + ServerConfig.PLAYER_BACKUP_INTERVAL);
		this.savePlayerToDbTimer   = new TimerTimestamp(now + 1);
		this.onlineCountTimer      = new TimerTimestamp(now + ServerConfig.SAVE_ONLINE_COUNT_INTERVAL);
		
		this.startReady();
	}
	
	private void checkTimer() {
		if (this.playerLoginQueueTimer.isSuccess()) {
			this.dealLoginQueue();
			this.playerLoginQueueTimer.doNextAfterSeconds(1);
		}
		
		if (this.playerFreeLogoutTimer.isSuccess()) {
			LoginCache.INSTANCE.freeLogoutPlayer(false);
			this.playerFreeLogoutTimer.doNextAfterSeconds(1);
		}
		
		if (this.saveGlobalValueTimer.isSuccess()) {
			// TODO
			this.saveGlobalValueTimer.doNextAfterSeconds(ServerConfig.AUTO_SAVE_INTERVAL);
		}
		
		if (this.playerBackupTimer.isSuccess()) {
			// TODO
			this.playerBackupTimer.doNextAfterSeconds(ServerConfig.PLAYER_BACKUP_INTERVAL);
		}
		
		if (this.savePlayerToDbTimer.isSuccess()) {
			// TODO
			this.savePlayerToDbTimer.doNextAfterSeconds(1);
		}
		
		if (this.onlineCountTimer.isSuccess()) {
			// TODO
			this.onlineCountTimer.doNextAfterSeconds(ServerConfig.SAVE_ONLINE_COUNT_INTERVAL);
		}
	}
	
	public void pushSession(int fd) {
		sessionQueue.add(fd);
	}
	
	public void pushLogout(int fd, String guid) {
		loginQueue.pushAction(guid, fd, LoginActionEnum.CONTEXT_LOGIN_ACTION_CLOSE);
	}
	
	public int getLoginPlayerCount() {
		int cnt = ContextManager.INSTANCE.getLoginPlayers(new MatchCallback<ServerContext>() {
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
	
	public void dealLoginQueue() {
		if (!this.sessionQueue.isEmpty()) {
			int loginCount = this.getLoginPlayerCount();
			int passCount = ServerConfig.getMaxPlayerCount() - loginCount;
			logger.devLog("dealLoginQueue max {} , now {}, pass {}, queue {}", 
					ServerConfig.getMaxPlayerCount(), loginCount, passCount, sessionQueue.size());
			
			// 先计算能正常登录的
			while (passCount > 0 && !sessionQueue.isEmpty()) {
				int fd = sessionQueue.poll();
				LoginContext context = (LoginContext)ContextManager.INSTANCE.getContext(fd);
				if (context == null) {
					continue;
				}
				
				if (context.getGuid().isEmpty()) {
					logger.devLog("dealLoginQueue, guid empty, account %s", 
							context.getAccount());
					context.closeSession(OperateConst.OPERATE_CLOSE_REASON_LOGDIN_ONE18);
					continue;
				}
				
				if (context.getStatus() == SessionStatus.STATUS_TRANSFER || 
					context.getStatus() == SessionStatus.STATUS_LOGGEDIN) {
					logger.devLog("dealLoginQueue, but status err, {}", context.getGuid());
					continue;
				}
				loginQueue.pushAction(context.getGuid(), context.getFd(), LoginActionEnum.CONTEXT_LOGIN_ACTION_LOGIN);
				passCount --;
			}
			
			Iterator<Integer> iter = sessionQueue.iterator();
			// 需要等待的
			for (int index = 0; iter.hasNext(); index ++) {
				int fd = iter.next();
				ServerContext context = ContextManager.INSTANCE.getContext(fd);
				if (context == null) {
					iter.remove();
					index --;
					continue;
				}
				QueuingMsg packet = new QueuingMsg();
				packet.setBaseFd(fd);
				packet.setIndex(index);
				LoginServerManager.getInstance().sendToGate(packet);
			}
		}
	}
}
