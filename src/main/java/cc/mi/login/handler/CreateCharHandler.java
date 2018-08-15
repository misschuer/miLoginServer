package cc.mi.login.handler;

import cc.mi.core.constance.ObjectType;
import cc.mi.core.generate.msg.CreateChar;
import cc.mi.core.generate.stru.CharInfo;
import cc.mi.core.handler.HandlerImpl;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ContextManager;
import cc.mi.core.server.GuidManager;
import cc.mi.core.server.ServerContext;
import cc.mi.core.server.SessionStatus;
import cc.mi.login.server.LoginCache;
import cc.mi.login.server.LoginContext;
import cc.mi.login.server.LoginPlayer;
import cc.mi.login.server.LoginServerManager;
import cc.mi.login.table.Account;
import io.netty.channel.Channel;

public class CreateCharHandler extends HandlerImpl {

	@Override
	public void handle(ServerContext player, Channel channel, Packet decoder) {
		CreateChar createChar = (CreateChar)decoder;
		int fd = createChar.getFD();
		LoginContext context = (LoginContext)ContextManager.getContext(fd);
	
//		//TODO: 参数验证
//		if (coder.getCharData().getGender() < 1 || coder.getCharData().getGender() > 6) {
//			context.callOperationResult(
//					LoginSystemManager.getCenterChannel(), 
//					OperateConst.OPERATE_TYPE_LOGIN, 
//					OperateConst.OPERATE_LOGIN_REASON_GENDER_ILLEGAL, 
//					""
//			);
//			return;
//		}
		
		//账号信息
		Account accountInfo = LoginCache.INSTANCE.getAccount(context.getAccount());	
		// 名字验证
		String realName = context.checkNameAndGetRealName(createChar.getCharData().getName());
		if ("".equals(realName)) {
			return;
		}
		String guid = GuidManager.INSTANCE.makeNewGuid(ObjectType.PLAYER, context.getFromServerName());
		LoginPlayer newPlayer = new LoginPlayer();
		newPlayer.setGuid(guid);
		newPlayer.setOwner(guid);
		newPlayer.setAccount(context.getAccount());
		newPlayer.setCreateLoginIp(context.getRemoteIp());
		newPlayer.setPlatData(accountInfo.getPlatData());
		//初始化新角色属性
		newPlayer.initNewPlayer(realName, createChar.getCharData().getModelId(), accountInfo.getIsFcm() == 1);
		// 保存角色名字信息
		LoginCache.INSTANCE.addCharNameAndSave(guid, realName);
		//记一下日志
		
////FIXME:			WriteCreateRole(m_account, new_player->GetGuid(), new_player->GetName().c_str(), m_remote_ip);
		
		//这样才会存，不然永远不存
		newPlayer.setDbHashCode(newPlayer.hashCode());
//		//保存新角色
		CharInfo charInfo = new CharInfo();
		charInfo.setAccountName(accountInfo.getName());
		charInfo.setGuid(guid);
		charInfo.setLevel(0);
		charInfo.setModelId(createChar.getCharData().getModelId());
		charInfo.setName(realName);
		LoginCache.INSTANCE.addAccountToCharAndSave(accountInfo.getName(), charInfo);
		// 记录日志
////			//g_LOG.AddHtBaiscInfo(new_player->GetGuid(), m_account, info->name, m_remote_ip);
////	
		context.addTempBinlogDataToLogin(newPlayer);
		context.setGuid(guid);
		context.setStatus(SessionStatus.STATUS_TRANSFER2);
		LoginServerManager.getInstance().pushSession(context.getFd());
	}

}
