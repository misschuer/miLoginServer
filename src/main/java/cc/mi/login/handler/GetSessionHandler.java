package cc.mi.login.handler;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import cc.mi.core.coder.Coder;
import cc.mi.core.generate.msg.GetSession;
import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.ServerContext;
import cc.mi.core.server.SessionStatus;
import cc.mi.login.config.ServerConfig;
import cc.mi.login.server.LoginContext;
import cc.mi.login.system.SystemManager;
import io.netty.channel.Channel;

public class GetSessionHandler extends AbstractHandler {

	@Override
	public void handle(ServerContext player, Channel channel, Coder decoder) {
		GetSession coder = (GetSession)decoder;
		
		int fd = coder.getFd();
		String sessionKey = coder.getSessionkey();
		
		//在这里就已经验证session的有效性并解析出一个map交给GetSession
		Map<String, String> querys = new HashMap<>();
		if (!parseSessionKey(querys, sessionKey)) {
			//这个还是蛮正常的，因为会出现一台机器多个平台的情况，平台密钥不会相同，所以tea_pdebug就好
			//tea_pdebug("LogindApp::on_create_conn_get_session ParseSessionKey failed");
			ContextManager.closeSession(SystemManager.getCenterChannel(), fd);
			return;
		}
		 
		int gaptime = 900;		//页游默认为15分钟
		if ("y".equals(querys.get("mobile"))) {		
			gaptime = 86400;	//手游默认为24小时
		}
		
		//验证一下session_key的时效性
		if (!checkSessionKeyTimeout(fd, sessionKey, Integer.parseInt(querys.get("time")), gaptime)) {
			//tea_pwarn("LogindApp::on_create_conn_get_session CheckSessionKeyTimeout failed");
			ContextManager.closeSession(SystemManager.getCenterChannel(), fd);
			return;
		}
	
		//如果连接不存在,则根据解开的session结果进行创建对应的session实例
		LoginContext loginContext = (LoginContext) ContextManager.getContext(fd);
		if (loginContext != null && loginContext.getStatus() == SessionStatus.STATUS_NEVER) {
//			tea_pwarn("LogindApp::on_create_conn_get_session create_sesstion duplicate!");
			return;
		}
		
		//为顶号服务的
		if (loginContext == null) {
			loginContext = new LoginContext(fd);
			String remoteIp = querys.get("remote_ip");
			if (remoteIp == null || "".equals(remoteIp)) {
				loginContext.setRemoteIp(SystemManager.getHostInfoKey(fd));
			} else {
				loginContext.setRemoteIp(remoteIp);
			}
			loginContext.setRemotePort(SystemManager.getHostInfoValue(fd));
			ContextManager.pushContext(loginContext);
		}	
	
		//要么成功,要么失败
		if(loginContext.getSession(querys)) {
			SystemManager.ip2Sessionkey.put(loginContext.getRemoteIp(), sessionKey);
		} else {
			loginContext.close(SystemManager.getCenterChannel(), (short) 0, "");
		}
	}
	
	private boolean parseURLParams(String params, Map<String, String> querys) {
		if (params.length() < 3) {
			// 至少a=b
			return false;
		}
		
		querys.clear();
		String[] pairs = params.split("[&]");
		for (String pair : pairs) {
			int eq = pair.indexOf("=");
			if (eq < 0) return false;
			querys.put(pair.substring(0, eq), pair.substring(eq+1));
		}
		
		return true;
	}
	
	private String md5Crypt(String s) {
	    char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};  
	    try {
	        byte[] btInput = s.getBytes();
	        // 获得MD5摘要算法的 MessageDigest 对象
	        MessageDigest mdInst = MessageDigest.getInstance("MD5");
	        // 使用指定的字节更新摘要
	        mdInst.update(btInput);
	        // 获得密文
	        byte[] md = mdInst.digest();
	        // 把密文转换成十六进制的字符串形式 
	        int j = md.length;
	        char str[] = new char[j << 1];
	        int k = 0;
	        for (int i = 0; i < j; i++) {
	            byte byte0 = md[ i ];
	            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
	            str[k++] = hexDigits[byte0 & 0xf];
	        }
	        return new String(str);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}  
	
	private boolean parseSessionKey(Map<String, String> querys, String sessionkey) {
		/*必须以?开头*/
		if (sessionkey.charAt(0) != '?') {
			return false;
		}
		
		Map<String, String> tmpQuerys = new HashMap<>();
		if (!parseURLParams(sessionkey.substring(1), tmpQuerys)) {
			return false;
		}
		String auth = tmpQuerys.get("auth");	//auth数据的校验码，用来校验数据是否恶意修改。
		String sign = tmpQuerys.get("sign");	//传送数据，包括用户帐号等信息。数据通过base64编码。
		
		//auth以base64编码,解码
		String tmp = "";
		try {
			byte[] bytes = Base64.getDecoder().decode(auth.getBytes("UTF-8"));
			tmp = new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if (!parseURLParams(tmp, querys)) {
			return false;
		}

		//验证key的有效性
		String authTemp = auth + ServerConfig.getLoginKey();
		String md5Str = md5Crypt(authTemp);
		if (!sign.equals(md5Str)) {
			authTemp = auth + ServerConfig.WANNENG_LOGIN_KEY;
			md5Str = md5Crypt(authTemp);
			if (!sign.equals(md5Str)) {
				System.out.printf("parseSessionKey warn sign:%s md5_str:%s\n", sign, md5Str);
				return false;
			}
		}

		//非空选项控制
		if (querys.get("pid").isEmpty() || querys.get("sid").isEmpty() || querys.get("uid").isEmpty()) {
			System.out.printf("pid:%s sid:%s uid:%s failed\n", querys.get("pid"), querys.get("sid"), querys.get("uid"));	
			return false;
		}

		return true;
	}
	
	//传入session验证是否已经过期
	boolean checkSessionKeyTimeout(int fd, final String sessionKey, int keyTime, int gaptime) {
		//校验时间
		if (!ServerConfig.isCheckSessionKeyTime()) {
			return true;
		}

		//本连接的IP
		String thisIp = SystemManager.getHostInfoKey(fd);
		if (thisIp == null || thisIp.isEmpty()) {
//			tea_pwarn("CheckSessionKeyTimeout this_ip is empty!");		
			return false;
		}

		//如果上次的IP跟本次没有变化则不过期
		if (sessionKey.equals(SystemManager.ip2Sessionkey.get(thisIp))) {
			return true;
		}
		
		//验证时效性,允许误差
		int now = (int) (System.currentTimeMillis() / 1000);
		//误差操作过了且不是断线重连上来的
		if(keyTime < now - gaptime || keyTime > now + gaptime) {
//			tea_pwarn("CheckSessionKeyTimeout key_time is faild");		
			return false;
		}

		return true;
	}
	
	public static void main(String[] args) throws NumberFormatException, Exception {
		ServerConfig.loadConfig();
		String sessionKey = "?auth=cGlkPTImc2lkPTEwMDMmdWlkPWMwMDA2JnRpbWU9MTUwMDM0NzQxMyZpbmR1bGdlPW4mbW9iaWxlPW4mcmVtb3RlX2lwPQ==&sign=50de923f8279b74d506286ee7824b21f";
		Map<String, String> querys = new HashMap<>();
		if (!new GetSessionHandler().parseSessionKey(querys, sessionKey)) {
			return;
		}
		System.out.println("parse success");
	}

}
