package cc.mi.login.db.command;

import cc.mi.login.db.adaptor.Adaptor;
import cc.mi.login.db.annotation.Column;
import cc.mi.login.db.core.FieldEntity;

public final class FieldCommand {
	//ALTER  TABLE  `account`  ADD  `ip` varchar(23)  NOT NULL  DEFAULT "" COMMENT "";
	private static final String ADD_COLUMN = "ALTER TABLE `%s` ADD `%s` %s %s %s COMMENT '%s'";
	// `ip` varchar(23)  NOT NULL [DEFAULT ""/AUTO_INCREMENT] COMMENT "";
	private static final String CREATE_COLUMN = "	`%s` %s %s %s COMMENT '%s'";
	
	private FieldCommand(){}
	
	/**
	 * @param tableName
	 * @return
	 */
	public static String createAddColumnSql(String tableName, FieldEntity entity) {
		String typeSqled = sqledType(entity);
		String nil = nilOption(entity);
		String extra = extraOption(entity);
		String comment = entity.getColumn().comment();
		return String.format(ADD_COLUMN, tableName, entity.getColumnName(), typeSqled, nil, extra, comment);
	}
	
	/**
	 * @return
	 */
	public static String createFieldSql(FieldEntity entity) {
		String typeSqled = sqledType(entity);
		String nil = nilOption(entity);
		String extra = extraOption(entity);
		String comment = entity.getColumn().comment();
		return String.format(CREATE_COLUMN, entity.getColumnName(), typeSqled, nil, extra, comment);
	}
	
	private static String extraOption(FieldEntity entity) {
		return entity.isAutoIncrement() ? "AUTO_INCREMENT" : String.format("DEFAULT '%s'", entity.getColumn().defaultValue());
	}
	
	private static String nilOption(FieldEntity entity) {
		return entity.getColumn().nullable() ?  "NULL": "NOT NULL";
	}
	
	
	private static String sqledType(FieldEntity entity) {
		Adaptor valueAdaptor = entity.getValueAdaptor();
		Column column = entity.getColumn();
		String str = "";
		
		if (valueAdaptor == Adaptor.BOOLEAN) {
			str = "TINYINT(4)";
		}
		else if (valueAdaptor == Adaptor.INTEGER) {
			str = "INT(11)";
		}
		else if (valueAdaptor == Adaptor.LONG) {
			str = "BIGINT(20)";
		}
		else if (valueAdaptor == Adaptor.FLOAT) {
			str = String.format("FLOAT(%d, %d)", column.precision(), column.scale());
		}
		else if (valueAdaptor == Adaptor.DOUBLE) {
			str = String.format("DOUBLE(%d, %d)", column.precision(), column.scale());
		}
		else if (valueAdaptor == Adaptor.STRING) {
			str = String.format("VARCHAR(%d)", column.length());
		}
		return str;
	}
}
