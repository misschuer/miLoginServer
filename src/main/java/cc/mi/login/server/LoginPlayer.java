package cc.mi.login.server;

import cc.mi.core.constance.PlayerEnumFields;
import cc.mi.core.server.PlayerBase;
import cc.mi.core.utils.RandomUtils;
import cc.mi.core.utils.TimestampUtils;

public class LoginPlayer extends PlayerBase {
	private static final int BORN_MAP	= 1001; //出生地图
	private static final int BORN_X		= 143;	//出生坐标x
	private static final int BORN_Y		= 113;	//出生坐标y
	private static final int BORN_OR	= 2;	//出生朝向
	private static final int BORN_LV	= 1;	//出生等级
	
	private int autoSaveTimestamp = 0;
	
	private int fd;

	public LoginPlayer() {
		super(PlayerEnumFields.PLAYER_INT_FIELDS_SIZE, PlayerEnumFields.PLAYER_STR_FIELDS_SIZE);
	}
	
	public void initNewPlayer (String name, byte gender, boolean isFcm) {
		//创建基本属性
		this.setName(name);
		this.setOwner(this.getGuid());
		this.setGender(gender);
		this.setLevel(BORN_LV);
		this.setCreateTime(TimestampUtils.now());
		
		//坐标朝向
		//新手村稍微随机一下，不然相当不带劲啊
		float bornX = BORN_X + RandomUtils.randomRange(-8, 8);
		float bornY = BORN_Y + RandomUtils.randomRange(-8, 8);
		this.setMapId(BORN_MAP);
		this.relocate(bornX, bornY, BORN_OR);

//		SetDouble(PLAYER_FIELD_MOVESPEED, INIT_MOVE_SPEED);			//移动速度
//
//		//防沉迷
//		SetFCMLoginTime(is_FCM ? 0 : -1);
	}

	public int getFd() {
		return fd;
	}

	public void setFd(int fd) {
		this.fd = fd;
	}

	public int getAutoSaveTimestamp() {
		return autoSaveTimestamp;
	}

	public void setAutoSaveTimestamp(int autoSaveTimestamp) {
		this.autoSaveTimestamp = autoSaveTimestamp;
	}
}
