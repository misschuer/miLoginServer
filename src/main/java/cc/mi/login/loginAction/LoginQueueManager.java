package cc.mi.login.loginAction;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.loginAction.ContextLoginManager;
import cc.mi.core.loginAction.LoginActionBase;
import cc.mi.core.server.ContextManager;
import cc.mi.login.server.LoginContext;

public class LoginQueueManager extends ContextLoginManager {
	private static final Logger logger = Logger.getLogger(LoginQueueManager.class);
	private Queue<LoginContext> offlinecontext = new LinkedList<>();
	
	public void pushAction(String guid, int fd, LoginActionEnum actionType) {
		logger.debug(String.format("LoginQueueManager.PushAction %s %u %s", guid, fd, actionType));
		LoginContext context = null;
		if (fd > 0) {
			context = (LoginContext) ContextManager.getContext(fd);
		} else {
			context = getOfflineContext(guid);
		}
		
		if (context.getAccount().isEmpty()) {
			logger.debug("LoginQueueManager session account is nil");
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
		this.offlinecontext.add(context);
	}
	
	public LoginContext getOfflineContext(String guid) {
		for (LoginContext context : this.offlinecontext) {
			if (context.getGuid() == guid) {
				return context;
			}
		}
		
		return null;
	}
	
	public void removeOfflineContext(LoginContext context) {
		Iterator<LoginContext> iter = this.offlinecontext.iterator();
		
		for (;iter.hasNext();) {
			LoginContext element = iter.next();
			if (element == context) {
				iter.remove();
				return;
			}
		}
	}
}
