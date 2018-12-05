package cc.mi.login.loginAction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.loginAction.ContextLoginManager;
import cc.mi.core.loginAction.LoginActionBase;
import cc.mi.core.server.ContextManager;
import cc.mi.login.server.LoginContext;

public class LoginQueueManager extends ContextLoginManager {
	private static final CustomLogger logger = CustomLogger.getLogger(LoginQueueManager.class);
	private Map<String, LoginContext> offlinecontext = new HashMap<>();
	
	public void pushAction(String guid, int fd, LoginActionEnum actionType) {
		logger.devLog("LoginQueueManager.PushAction {} {} {}", guid, fd, actionType);
		LoginContext context = null;
		if (fd > 0) {
			context = (LoginContext) ContextManager.INSTANCE.getContext(fd);
		} else {
			context = getOfflineContext(guid);
		}
		
		if (context.getAccount().isEmpty()) {
			logger.devLog("LoginQueueManager session account is nil");
			return;
		}

		LoginActionBase action = null;
		if (actionType == LoginActionEnum.CONTEXT_LOGIN_ACTION_LOGIN) {
			action = new LoginActionLogin(context.getFd(), context.getAccount());
		} else if (actionType == LoginActionEnum.CONTEXT_LOGIN_ACTION_CLOSE) {
			action = new LoginActionClose(context.getFd(), context.getAccount());
		}
		
		Queue<LoginActionBase> queue = null;
		if (this.actionHash.containsKey(guid)) {
			queue = this.actionHash.get(guid);
		} else {
			queue = new LinkedList<>();
			this.actionHash.put(guid, queue);
		}
		
		queue.add(action);
	}
	
	public void pushOfflineContext(LoginContext context) {
		this.offlinecontext.put(context.getGuid(), context);
	}
	
	public LoginContext getOfflineContext(String guid) {
		return this.offlinecontext.get(guid);
	}
	
	public void removeOfflineContext(String guid) {
		this.offlinecontext.remove(guid);
	}
}
