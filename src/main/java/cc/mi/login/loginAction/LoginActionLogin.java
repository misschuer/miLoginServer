package cc.mi.login.loginAction;

import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.log.CustomLogger;
import cc.mi.core.loginAction.LoginActionBase;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.SessionStatus;
import cc.mi.login.server.LoginContext;

public class LoginActionLogin extends LoginActionBase {
	static final CustomLogger logger = CustomLogger.getLogger(LoginActionLogin.class);
	public LoginActionLogin(int fd, String guid) {
		super(fd, guid);
	}
	
	@Override
	public boolean update(int diff) {
		LoginContext context = (LoginContext) ContextManager.INSTANCE.getContext(this.getFd());
		if (context == null) {
			logger.devLog("LoginActionLogin.Update player not found, {} {} {}", 
					this.getGuid(), this.getFd(), this.getType());
			return false;	//玩家下线了
		}
// TODO:
//		//释放数据中，请等待
//		if(LogindApp::g_app->m_removing_player.find(context->GetGuid()) != LogindApp::g_app->m_removing_player.end())
//			return true;
//
		if (context.getStatus() == SessionStatus.STATUS_AUTHED) {
			context.playerLoadData();
			return true;
		} else if (context.getStatus() == SessionStatus.STATUS_TRANSFER) {
			return true; //已经在load数据了,等着吧
		} else if (context.getStatus() == SessionStatus.STATUS_TRANSFER2) {
			context.putData();
			return true;
		} else if (context.getStatus() == SessionStatus.STATUS_PUT) {
			return true;
		} else if (context.getStatus() == SessionStatus.STATUS_PUT_OK) {
			context.loginOK();
			return false;
		}
		
		return false;
	}

	@Override
	public LoginActionEnum getType() {
		return LoginActionEnum.CONTEXT_LOGIN_ACTION_LOGIN;
	}
}
