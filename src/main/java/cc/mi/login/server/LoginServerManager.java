package cc.mi.login.server;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.constance.ServerState;
import cc.mi.core.generate.Opcodes;
import cc.mi.core.handler.Handler;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.manager.ServerManager;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.utils.ServerProcessBlock;
import cc.mi.login.handler.CheckSessionHandler;
import cc.mi.login.handler.CreateConnectionHandler;

public class LoginServerManager extends ServerManager {
	static final CustomLogger logger = CustomLogger.getLogger(LoginServerManager.class);
	
	private static LoginServerManager instance;
	
	// 帧刷新
	private static final ScheduledExecutorService excutor = Executors.newScheduledThreadPool(1);
	// 消息包队列
	private static final Queue<Packet> packetQueue = new LinkedList<>();
	// 消息收到以后的回调
	private static final Map<Integer, Handler> handlers = new HashMap<>();
	private static final List<Integer> opcodes;
	
	// 当前帧刷新执行的代码逻辑
	protected ServerProcessBlock process;
	
	// 最后一次执行帧刷新的时间戳
	protected long timestamp = 0;
	
	static {
		handlers.put(Opcodes.MSG_CREATECONNECTION, new CreateConnectionHandler());
		handlers.put(Opcodes.MSG_CHECKSESSION, new CheckSessionHandler());
		
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
				instance.doWork(diff);
			}
		}, 1000, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 进行帧刷新
	 */
	private void doWork(int diff) {
		// 初始化服务器
		this.initServer(diff);
		// 处理包信息
		this.dealPacket();
	}
	
	private void loadServerValue() {
		logger.devLog("login init");
		logger.debugLog("load global value");
		
//		vector<GuidObject*> vec;
//		g_app->m_cache->LoadGlobalValue();
//		g_app->m_db_access_mgr->Load_GlobalValue();
//		tea_pinfo("load global value end");
//
//		tea_pinfo("load league value begin");
//		g_app->m_cache->LoadFractionValue();
//		tea_pinfo("load league value end");
//
//		tea_pinfo("load faction data begin");
//		g_app->m_cache->LoadFractionData();
//		tea_pinfo("load faction data end");
		
//		g_app->OnDataReady();
		
		
	}
	
	private void onDataReady() {
		this.process = new ServerProcessBlock() {
			@Override
			public void run(int diff) {
				
			}
		};
		// TODO: 发送消息给中心服 通知初始化完成
	}
	
	private void initServer(int diff) {
		if (this.process != null) {
			this.process.run(diff);
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
		//TODO: 检测包的频率(这里需要么?)
		synchronized (this) {
			packetQueue.add(packet);
		}
	}
	
	public void closeSession(int fd, int reasonType) {
		ContextManager.closeSession(this.gateChannel, fd, reasonType);
	}
	
	
}
