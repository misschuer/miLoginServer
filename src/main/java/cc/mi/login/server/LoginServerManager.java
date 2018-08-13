package cc.mi.login.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cc.mi.core.binlog.data.BinlogData;
import cc.mi.core.callback.AbstractCallback;
import cc.mi.core.callback.Callback;
import cc.mi.core.constance.IdentityConst;
import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.constance.OperateConst;
import cc.mi.core.generate.Opcodes;
import cc.mi.core.handler.Handler;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.ServerManager;
import cc.mi.core.packet.Packet;
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
import cc.mi.login.loginAction.LoginQueueManager;

public class LoginServerManager extends ServerManager {
	static final CustomLogger logger = CustomLogger.getLogger(LoginServerManager.class);
	
	private static LoginServerManager instance;
	
	// 消息收到以后的回调
	private static final Map<Integer, Handler> handlers = new HashMap<>();
	private static final List<Integer> opcodes;
	
	// 帧刷新
	private final ScheduledExecutorService excutor = Executors.newScheduledThreadPool(1);
	// 消息包队列
	private final Queue<Packet> packetQueue = new LinkedList<>();
	// 当前帧刷新执行的代码逻辑
	protected ServerProcessBlock process;
	// 最后一次执行帧刷新的时间戳
	protected long timestamp = 0;
	
	private final Queue<Integer> sessionQueue = new LinkedList<>();
	private final LoginQueueManager loginQueue = new LoginQueueManager();
	
	// 登录列表定时器
	private TimerTimestamp playerLoginQueueTimer;
	
	static {
		handlers.put(Opcodes.MSG_CREATECONNECTION, new CreateConnectionHandler());
		handlers.put(Opcodes.MSG_CHECKSESSION, new CheckSessionHandler());
		handlers.put(Opcodes.MSG_CREATECHAR, new CreateCharHandler());
		
		opcodes = new LinkedList<>();
		opcodes.addAll(handlers.keySet());
	}
	
	public static LoginServerManager getInstance() {
		if (instance == null) {
			instance = new LoginServerManager();
			instance.process = new ServerProcessBlock() {
				@Override
				public void run(int diff) {
					instance.loadServerValue();
				}
			};
		}
		return instance;
	}
	
	public LoginServerManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN, opcodes);
		excutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				long prev = instance.timestamp;
				long now = System.currentTimeMillis();
				int diff = 0;
				if (prev > 0) diff = (int) (now - prev);
				instance.timestamp = now;
				if (diff < 0 || diff > 1000) {
					logger.warnLog("too heavy logical that execute");
				}
				try {
					instance.doWork(diff);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, 1000, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 进行帧刷新
	 */
	private void doWork(int diff) {
		// 初始化服务器
		this.doProcess(diff);
		// 执行定时器
		this.checkTimer();
		// 处理包信息
		this.dealPacket();
	}
	
	public void putObjects(String ownerId, final List<BinlogData> result, AbstractCallback<Boolean> abstractCallback) {
		LoginObjectManager.INSTANCE.putObjects(this.centerChannel, ownerId, result, abstractCallback);
	}
	
	public void putObject(String ownerId, BinlogData result, Callback<Boolean> callback) {
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
			public void run(int diff) {
//				//登录队列
//				m_login_queue->Update(diff);
//
//				//地图管理器心跳
//				if(MapMgr)
//					MapMgr->Update(diff);
//
//				//所有的player心跳
//				for (auto it = context_map_.begin();it != context_map_.end();++it)
//				{
//					it->second->Update(diff);
//				}
//				 
//				//是否是异步查询回调
//				if(m_db_access_mgr)
//					m_db_access_mgr->UpdateAsync();
//				
//				//http服务
//				if(m_http ) 
//					m_http->Update();
//				else
//					m_http = new HttpHandler;
			}
		};
		this.playerLoginQueueTimer = new TimerTimestamp(TimestampUtils.now() + 1);
		this.startReady();
	}
	
	private void doProcess(int diff) {
		if (this.process != null) {
			this.process.run(diff);
		}
	}
	
	private void checkTimer() {
		if (this.playerLoginQueueTimer.isSuccess()) {
			this.playerLoginQueueTimer.doNextAfterSeconds(1);
			this.dealLoginQueue();
		}
	}
	
	private void dealPacket() {
		while (!packetQueue.isEmpty()) {
			Packet packet = packetQueue.poll();
			this.invokeHandler(packet);
		}
	}
	
	private void invokeHandler(Packet packet) {
		int opcode = packet.getOpcode();
		Handler handle = handlers.get(opcode);
		if (handle != null) {
			handle.handle(null, this.centerChannel, packet);
		}
	}
	
	public void pushPacket(Packet packet) {
		synchronized (this) {
			packetQueue.add(packet);
		}
	}
	
	public void pushSession(int fd) {
		sessionQueue.add(fd);
	}
	
	public int getLoginPlayerCount() {
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
	
	public void dealLoginQueue() {
		if (!this.sessionQueue.isEmpty()) {
			int loginCount = this.getLoginPlayerCount();
			int passCount = ServerConfig.getMaxPlayerCount() - loginCount;
			logger.devLog("dealLoginQueue max {} , now {}, pass {}, queue {}", 
					ServerConfig.getMaxPlayerCount(), loginCount, passCount, sessionQueue.size());
			
			// 先计算能正常登录的
			while (passCount > 0 && !sessionQueue.isEmpty()) {
				int fd = sessionQueue.poll();
				LoginContext context = (LoginContext)ContextManager.getContext(fd);
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
				ServerContext context = ContextManager.getContext(fd);
				if (context == null) {
					iter.remove();
					index --;
					continue;
				}
				System.out.println(index);
//TODO:	通知客户单当前排第几位			Call_login_queue_index(context->m_delegate_sendpkt, index);
			}
		}
	}
}
