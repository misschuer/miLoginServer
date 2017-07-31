package cc.mi.login.loginAction;

import cc.mi.core.constance.LoginActionEnum;
import cc.mi.core.loginAction.LoginActionBase;

public class LoginActionClose extends LoginActionBase {
	private boolean isClosed;
	
	public LoginActionClose(int fd, String guid) {
		super(fd, guid);
	}
	
	@Override
	public boolean update(int diff) {
//		if(m_close)
//		{
//			//能进这里，肯定是pk服
//			ASSERT(LogindApp::g_app->IsPKServer());
//			return ObjMgr.Get(m_guid) != nullptr;
//		}
//
//		CommonContext *context = nullptr;
//		if(m_fd)
//		{
//			context = CommonContext::FindContext(m_fd);
//			if(!context)
//			{
//				tea_pdebug("LoginActionClose::Update player not found, %s %u %u", m_guid.c_str(), m_fd, GetType());
//				return false;	//玩家下线了
//			}
//		}
//		else
//		{
//			context = LogindApp::g_app->m_login_queue->FindOfflineContext(m_guid);
//			LogindApp::g_app->m_login_queue->RemoveOfflineContext(context);
//		}
//		context->OnClosed();
//		safe_delete(context);
//
//		//如果是pk服，必须等到对象缓存被释放才能完成下线
//		if(LogindApp::g_app->IsPKServer())
//		{
//			m_close = true;
//			return true;
//		}
		return false;
	}

	@Override
	public LoginActionEnum getType() {
		return LoginActionEnum.CONTEXT_LOGIN_ACTION_CLOSE;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

}
