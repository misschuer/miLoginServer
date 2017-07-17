package cc.mi.login.system;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.mi.core.constance.IdentityConst;
import cc.mi.core.generate.msg.ServerRegIdentity;
import cc.mi.core.generate.msg.ServerRegOpcode;
import cc.mi.core.task.SendToCenterTask;
import cc.mi.core.task.base.Task;
import io.netty.channel.Channel;

public class SystemManager {
	// 单线程逻辑
	private static final ExecutorService executor;
	
	private static Channel centerChannel = null;
		
	private static final List<Integer> opcodes;
	
	static {
		executor = Executors.newSingleThreadExecutor();
		opcodes = Arrays.asList(
				
		);
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
}
