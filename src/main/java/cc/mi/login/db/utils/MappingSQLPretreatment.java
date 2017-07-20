package cc.mi.login.db.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingSQLPretreatment {
	private static final Map<Class<?>, List<String>> sqlHash = new HashMap<>();

// 没什么用, 底层都是自己写的除非底层写错了
//	private static final int TYPES = 4;
//	private static final String TABLE_NAME_REGEX = "`[a-zA-Z]+`";
//	private static final String TABLE_COLUMNS_REGEX = "[\\`\\w\\,\\ ]+";
//	private static final String TABLE_CONDITION_AND_VALUE_REGEX = "[\\`\\w\\,\\ \\?\\=]+";
//	private static final String TABLE_INSERT_VALUES_REGEX = "[\\?\\,\\ ]+";
//	
//	private static final List<String> regexList = Arrays.asList(
////			INSERT INTO `account` (`userid`, `account`, `password`) VALUES (?, ?, ?)
////			UPDATE `account` SET `account` = ?, `password` = ? WHERE `userid` = ?
////			SELECT `userid`, `account`, `password` FROM `account` where `id` = ?, `position` = ?
////			DELETE FROM `account` WHERE `userid` = ?
//			"INSERT INTO `[a-zA-Z]+` ([\\`\\w\\,\\ ]+) VALUES ([\\?\\,\\ ]+)",
//			"UPDATE `[a-zA-Z]+` SET [\\`\\w\\,\\ \\?\\=]+ WHERE [\\`\\w\\?\\,\\ \\=]+",
//			"SELECT [\\`\\w\\,\\ ]+ FROM `[a-zA-Z]+` WHERE [\\`\\w\\?\\,\\ \\=]+",
//			"DELETE FROM `[a-zA-Z]+` WHERE [\\`\\w\\?\\,\\ \\=]+"
//	);
	
//	if (sqlList.size() != TYPES) {
//		throw new RuntimeException(String.format("sql语句初始化长度有误, length=%d, need %d", sqlList.size(), TYPES));
//	}
//	for (int i = 0; i < sqlList.size(); ++ i) {
//		String regex = regexList.get(i);
//		String sql   =   sqlList.get(i);
//		if (!sql.matches(regex)) {
//			throw new RuntimeException(String.format("sql语句初始化有误, 不满足正则匹配:%s, value:%s", regex, sql));
//		}
//	}
	
	public static void put(Class<?> clazz, List<String> sqlList) {
		sqlHash.put(clazz, sqlList);
	}
	
	public static String getSql(Class<?> clazz, int sqlOperateType) {
		return sqlHash.get(clazz).get(sqlOperateType);
	}
}
