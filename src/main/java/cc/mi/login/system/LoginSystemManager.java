package cc.mi.login.system;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.util.Pair;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.generate.Opcodes;
import cc.mi.core.generate.msg.ServerRegIdentity;
import cc.mi.core.generate.msg.ServerRegOpcode;
import cc.mi.core.handler.Handler;
import cc.mi.core.task.SendToCenterTask;
import cc.mi.core.task.base.Task;
import cc.mi.login.handler.CreateCharHandler;
import cc.mi.login.handler.GetSessionHandler;
import cc.mi.login.loginAction.LoginQueueManager;
import io.netty.channel.Channel;

public class LoginSystemManager {
	// 单线程逻辑
	private static final ExecutorService executor;
	
	private static Channel centerChannel = null;
	
	// 句柄
	public static final Handler[] handlers = new Handler[1<<12];
		
	private static final List<Integer> opcodes;
	
	private static final Map<Integer, Pair<String, Short>> hostInfo = new HashMap<>();
	
	public static final Map<String, String> ip2Sessionkey = new HashMap<>();
	
	//登录队列
	public static final LoginQueueManager loginQueue = new LoginQueueManager();
	
	static {
		executor = Executors.newSingleThreadExecutor();
		opcodes = Arrays.asList(
			Opcodes.MSG_GETSESSION,
			Opcodes.MSG_CREATECONNECTION
		);
		
		handlers[Opcodes.MSG_GETSESSION] = new GetSessionHandler();
		handlers[Opcodes.MSG_CREATECHAR] = new CreateCharHandler();
	}
	
	public static Channel getCenterChannel() {
		return centerChannel;
	}
	
	public static void setCenterChannel(Channel channel) {
		if (centerChannel == null || !centerChannel.isActive()) {
			centerChannel = channel;
		}
	}
	
	// 提交客户端过来的任务
	public static void submitTask(Task task) {
		executor.submit(task);
	}
	
	public static void regToCenter() {
		ServerRegIdentity identity = new ServerRegIdentity();
		identity.setInternalDestFD(IdentityConst.IDENDITY_CENTER);
		identity.setIdentity(IdentityConst.IDENDITY_LOGIN);
		submitTask(new SendToCenterTask(centerChannel, identity));
		
		ServerRegOpcode reg = new ServerRegOpcode();
		reg.setInternalDestFD(IdentityConst.IDENDITY_CENTER);
		reg.setOpcodes(opcodes);
		submitTask(new SendToCenterTask(centerChannel, reg));
	}

	public static void putHostInfo(int fd, String ip, short port) {
		hostInfo.put(fd, new Pair<>(ip, port));
	}

	public static void removeHostInfo(int fd) {
		hostInfo.remove(fd);
	}
	
	public static String getHostInfoKey(int fd) {
		return hostInfo.get(fd).getKey();
	}
	
	public static short getHostInfoValue(int fd) {
		return hostInfo.get(fd).getValue();
	}
}
