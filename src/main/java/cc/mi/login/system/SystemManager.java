package cc.mi.login.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.mi.core.coder.Coder;
import cc.mi.core.task.Task;
import io.netty.channel.Channel;

public class SystemManager {
	// 单线程逻辑
	private static final ExecutorService executor;
	
	private static Channel centerChannel = null;
		
	static {
		executor = Executors.newSingleThreadExecutor();
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
	
	public static void sendToClient(Coder coder) {
		centerChannel.writeAndFlush(coder);
	}
}
