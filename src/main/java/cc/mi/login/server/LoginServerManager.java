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
import cc.mi.core.generate.Opcodes;
import cc.mi.core.generate.msg.CloseSession;
import cc.mi.core.handler.Handler;
import cc.mi.core.manager.ServerManager;
import cc.mi.core.packet.Packet;
import cc.mi.login.handler.CheckSessionHandler;
import cc.mi.login.handler.CreateConnectionHandler;

public class LoginServerManager extends ServerManager {
	private static LoginServerManager instance;
	
	// 帧刷新
	private static final ScheduledExecutorService excutor = Executors.newScheduledThreadPool(1);
	// 消息包队列
	private static final Queue<Packet> packetQueue = new LinkedList<>();
	// 消息收到以后的回调
	private static final Map<Integer, Handler> handlers = new HashMap<>();
	private static final List<Integer> opcodes;
	
	static {
		handlers.put(Opcodes.MSG_CREATECONNECTION, new CreateConnectionHandler());
		handlers.put(Opcodes.MSG_CHECKSESSION, new CheckSessionHandler());
		
		opcodes = new LinkedList<>();
		opcodes.addAll(handlers.keySet());
	}
	
	public static LoginServerManager getInstance() {
		if (instance == null) {
			instance = new LoginServerManager();
		}
		return instance;
	}
	
	public LoginServerManager() {
		super(IdentityConst.SERVER_TYPE_LOGIN, opcodes);
		excutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				instance.doWork();
			}
		}, 1000, 100, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 进行帧刷新
	 */
	private void doWork() {
		
		// 处理包信息
		this.dealPacket();
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
	
	
	public void closeSession(int fd) {
		CloseSession cs = new CloseSession();
		cs.setFd(fd);
		this.gateChannel.writeAndFlush(cs);
	}
}
