package cc.mi.login.service;

import cc.mi.core.generate.stru.CharInfo;

public enum CharInfoService {
	INSTANCE;
	
	public String toContentString(CharInfo charInfo) {
		return String.format("%s %s %s %d %d", 
				charInfo.getAccountName(), 
				charInfo.getGuid(), 
				charInfo.getName(), 
				charInfo.getModelId(), 
				charInfo.getLevel());
	}
	
	public CharInfo fromString(String content) {
		CharInfo charInfo = new CharInfo();
		String[] params = content.split(" ");
		
		charInfo.setAccountName(params[ 0 ]);
		charInfo.setGuid(params[ 1 ]);
		charInfo.setName(params[ 2 ]);
		charInfo.setModelId(Byte.parseByte(params[ 3 ]));
		charInfo.setLevel(Integer.parseInt(params[ 4 ]));
		
		return charInfo;
	}
}
