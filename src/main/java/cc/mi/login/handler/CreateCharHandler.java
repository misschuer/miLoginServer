package cc.mi.login.handler;

import cc.mi.core.coder.Coder;
import cc.mi.core.constance.ObjectType;
import cc.mi.core.constance.OperateConst;
import cc.mi.core.constance.PlayerEnumFields;
import cc.mi.core.generate.msg.CreateChar;
import cc.mi.core.handler.AbstractHandler;
import cc.mi.core.server.GuidManager;
import cc.mi.core.server.ServerContext;
import cc.mi.login.server.LoginCache;
import cc.mi.login.server.LoginContext;
import cc.mi.login.server.LoginPlayer;
import cc.mi.login.system.SystemManager;
import cc.mi.login.table.Account;
import io.netty.channel.Channel;

public class CreateCharHandler extends AbstractHandler {

	@Override
	public void handle(ServerContext player, Channel channel, Coder decoder) {
		
		LoginContext context = (LoginContext)player;
		CreateChar coder = (CreateChar)decoder;
		
		//TODO: 等具体的配置
		if (coder.getCharData().getGender() < 1 || coder.getCharData().getGender() > 6) {
			context.callOperationResult(
					SystemManager.getCenterChannel(), 
					OperateConst.OPERATE_TYPE_LOGIN, 
					OperateConst.OPERATE_LOGIN_REASON_GENDER_ILLEGAL, 
					""
			);
			return;
		}

		//账号信息

		Account accountInfo = LoginCache.INSTANCE.getAccount(context.getAccount());
		
		String realName = context.checkNameAndGetRealName(coder.getCharData().getName());
		if ("".equals(realName)) {
			return;
		}
		
		String guid = GuidManager.INSTANCE.makeNewGuid(ObjectType.PLAYER, context.getFromServerName());
		LoginPlayer newPlayer = new LoginPlayer(PlayerEnumFields.PLAYER_INT_FIELDS_SIZE, PlayerEnumFields.PLAYER_STR_FIELDS_SIZE);
		newPlayer.setGuid(guid);
		newPlayer.setOwner(guid);
		newPlayer.setAccount(context.getAccount());
		newPlayer.setCreateLoginIp(context.getRemoteIp());
		newPlayer.setPlatData(accountInfo.getPlatData());
		//初始化新角色属性
		newPlayer.initNewPlayer(realName, coder.getCharData().getGender(), accountInfo.getIsFcm() == 1);
//			
//			/**
//			new_player->SetStr(PLAYER_STRING_FIELD_INVITE_FACTION_GUID, info->inviteGuid);
//			string invited = string(info->inviteGuid);
//			// 是女性角色且不是邀请的
//			if ((info->gender & 1) == 0 && invited.empty()) {
//				string faction_name = string(info->faction_name);
//				if (!faction_name.empty()) {
//					new_player->SetStr(PLAYER_STRING_FIELD_CREATE_FACTION_NAME, faction_name);
//					new_player->SetUInt32(PLAYER_INT_FIELD_CREATE_ICON, 1);
//				}
//			}
//			*/
//			//记一下日志
//			//g_DAL.InsertGuidByName(guid,charName);	
//			WriteCreateRole(m_account, new_player->GetGuid(), new_player->GetName().c_str(), m_remote_ip);
//			//腾讯日志
//			if(LogindApp::g_app->GetPlatformID() == PLATFORM_QQ)
//				WriteTXUserLog(m_account,new_player->GetGuid(), new_player->GetName().c_str(),1,GetPlatInfo(accountInfo->platdata,"pf"),(uint32)time(NULL));
//			//这样才会存，不然永远不存
//			new_player->m_db_hashcode = new_player->GetHashCode();
//
//			//保存新角色	
//			string data,data_str;
//			new_player->ToString(data,data_str);
//			g_DAL.AddChars(new_player->GetGuid(), m_account, charName, m_remote_ip, data, data_str);
//			g_Cache.AddAccountToChar(m_account, new_player->GetGuid());
//			g_Cache.SaveAccountCharInfo(m_account, new_player->GetGuid());
//			//g_LOG.AddHtBaiscInfo(new_player->GetGuid(), m_account, info->name, m_remote_ip);
//
//			//put到中心服，并回调
//			m_temp_vec.push_back(new_player);
//
//			m_lguid = guid;
//			SetStatus(STATUS_TRANSFER2);
//			LogindContext::SessionQues.push_back(fd_);
	}

}
