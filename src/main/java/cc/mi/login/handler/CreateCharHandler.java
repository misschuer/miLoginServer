package cc.mi.login.handler;

import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.packet.Packet;
import cc.mi.core.server.ServerContext;
import io.netty.channel.Channel;

public class CreateCharHandler extends AbstractHandler {

	@Override
	public void handle(ServerContext player, Channel channel, Packet decoder) {
		
//		LoginContext context = (LoginContext)player;
//		CreateChar coder = (CreateChar)decoder;
//		
//		//TODO: 等具体的配置
//		if (coder.getCharData().getGender() < 1 || coder.getCharData().getGender() > 6) {
//			context.callOperationResult(
//					LoginSystemManager.getCenterChannel(), 
//					OperateConst.OPERATE_TYPE_LOGIN, 
//					OperateConst.OPERATE_LOGIN_REASON_GENDER_ILLEGAL, 
//					""
//			);
//			return;
//		}
//
//		//账号信息
//
//		Account accountInfo = LoginCache.INSTANCE.getAccount(context.getAccount());
//		
//		String realName = context.checkNameAndGetRealName(coder.getCharData().getName());
//		if ("".equals(realName)) {
//			return;
//		}
//		
//		String guid = GuidManager.INSTANCE.makeNewGuid(ObjectType.PLAYER, context.getFromServerName());
//		LoginPlayer newPlayer = new LoginPlayer(PlayerEnumFields.PLAYER_INT_FIELDS_SIZE, PlayerEnumFields.PLAYER_STR_FIELDS_SIZE);
//		newPlayer.setGuid(guid);
//		newPlayer.setOwner(guid);
//		newPlayer.setAccount(context.getAccount());
//		newPlayer.setCreateLoginIp(context.getRemoteIp());
//		newPlayer.setPlatData(accountInfo.getPlatData());
//		//初始化新角色属性
//		newPlayer.initNewPlayer(realName, coder.getCharData().getGender(), accountInfo.getIsFcm() == 1);
//		
//		//记一下日志
//		LoginCache.INSTANCE.addCharName(guid, realName);
//		
////FIXME:			WriteCreateRole(m_account, new_player->GetGuid(), new_player->GetName().c_str(), m_remote_ip);
//
////			//腾讯日志
////			if(LogindApp::g_app->GetPlatformID() == PLATFORM_QQ)
////				WriteTXUserLog(m_account,new_player->GetGuid(), new_player->GetName().c_str(),1,GetPlatInfo(accountInfo->platdata,"pf"),(uint32)time(NULL));
//		//这样才会存，不然永远不存
//		newPlayer.setDbHashCode(newPlayer.hashCode());
//
//		//保存新角色
//		
////			g_DAL.AddChars(new_player->GetGuid(), m_account, charName, m_remote_ip, data, data_str);
//		LoginCache.INSTANCE.addAccountToChar(context.getAccount(), guid);
//		LoginCache.INSTANCE.saveAccountCharInfo(context.getAccount(), guid);
////			//g_LOG.AddHtBaiscInfo(new_player->GetGuid(), m_account, info->name, m_remote_ip);
////
////			//put到中心服，并回调
////			m_temp_vec.push_back(new_player);
//		context.setGuid(guid);
//		context.setStatus(SessionStatus.STATUS_TRANSFER2);
//		LoginContext.pushSession(context.getFd());
	}

}
