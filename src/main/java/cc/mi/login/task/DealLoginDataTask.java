package cc.mi.login.task;

import cc.mi.core.coder.Coder;
import cc.mi.core.handler.Handler;
import cc.mi.core.server.ContextManager;
import cc.mi.core.task.base.AbstractCoderTask;
import cc.mi.login.system.SystemManager;
import io.netty.channel.Channel;

public class DealLoginDataTask extends AbstractCoderTask {
	private final Channel channel;
	public DealLoginDataTask(Channel channel, Coder coder) {
		super(coder);
		this.channel = channel;
	}
	
	@Override
	protected void doTask() {
		Handler handler = SystemManager.handlers[coder.getOpcode()];
		handler.handle(ContextManager.getContext(coder.getId()), this.channel, coder);
	}

}
