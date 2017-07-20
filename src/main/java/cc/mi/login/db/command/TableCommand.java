package cc.mi.login.db.command;

import java.util.Arrays;
import java.util.List;

import cc.mi.login.db.core.FieldEntity;
import cc.mi.login.db.core.TableEntity;
import cc.mi.login.db.ex.EmptyPrimaryException;

public final class TableCommand {
	private static final String TABLE_INDEX = ", " + System.lineSeparator() + "	INDEX `%sIndex` (%s)";
	private static final String CREATE_TABLE = "CREATE TABLE `%s` (" + System.lineSeparator()
			+ "%s"
			+ "	PRIMARY KEY (%s)"
			+ "%s"
			+ ")"+ System.lineSeparator()
			+ "COMMENT = '%s'"+ System.lineSeparator()
			+ "COLLATE = 'utf8_general_ci'"+ System.lineSeparator()
			+ "ENGINE=InnoDB";
	private static final String SELECT = "SELECT %s FROM `%s` WHERE %s";
	private static final String SELECT_ALL = "SELECT %s FROM `%s`";
	private static final String INSERT = "INSERT INTO `%s` (%s) VALUES (%s)";
	private static final String UPDATE = "UPDATE `%s` SET %s WHERE %s";
	private static final String DELETE = "DELETE FROM `%s` WHERE %s";
	
	/**
	 * 创建表
	 * @param entity
	 * @return
	 */
	public static <T> String createTableSql(TableEntity<T> entity) {
		return String.format(CREATE_TABLE, 
				entity.getTableName(), createFields(entity), createPrimaryKey(entity), 
				createTableIndex(entity), entity.getComment());
	}
	
	/**
	 * 自定义条件查询
	 * @param entity
	 * @param key
	 * @return
	 */
	public static <T> String createSelectSql(TableEntity<T> entity, String key) {
		FieldEntity cond = matchFieldEntity(entity, key);
		return String.format(SELECT, selectFields(entity), entity.getTableName(), createPrepareValue(cond));
	}
	
	/**
	 * 给查询语句添加条件
	 * @param selectSql
	 * @param key
	 * @return
	 */
	public static String joinSelectConditon(String selectSql, String key) {
		return selectSql + " WHERE " + key + " = ?";
	}
	
	/**
	 * 按主键查询
	 * @param entity
	 * @return
	 */
	public static <T> String createSelectSql(TableEntity<T> entity) {
		return String.format(SELECT, selectFields(entity), entity.getTableName(), whereConditon(entity));
	}
	
	/**
	 * 查询所有
	 * @param entity
	 * @return
	 */
	public static <T> String createSelectAllSql(TableEntity<T> entity) {
		return String.format(SELECT_ALL, selectFields(entity), entity.getTableName());
	}
	
	public static <T> FieldEntity matchFieldEntity(TableEntity<T> entity, String key) {
		for (FieldEntity fieldEntity : entity.getFieldList()) {
			if (fieldEntity.getColumnName().equals(key)) {
				return fieldEntity;
			}
		}
		return null;
	}
	
	/**
	 * 插入
	 * @param entity
	 * @return
	 */
	public static <T> String createInsertSql(TableEntity<T> entity) {
		return String.format(INSERT, entity.getTableName(), selectFields(entity), valueFields(entity));
	}
	
	/**
	 * 更新
	 * @param entity
	 * @return
	 */
	public static <T> String createUpdateSql(TableEntity<T> entity) {
		return String.format(UPDATE, entity.getTableName(), updateFields(entity), whereConditon(entity));
	}
	
	/**
	 * 删除
	 * @param entity
	 * @return
	 */
	public static <T> String createDeleteSql(TableEntity<T> entity) {
		return String.format(DELETE, entity.getTableName(), whereConditon(entity));
	}
	
	
	private static <T> String createFields(TableEntity<T> entity) {
		String str = "";
		for (FieldEntity fieldEntity : entity.getFieldList()) {
			str += FieldCommand.createFieldSql(fieldEntity) + ", " + System.lineSeparator();
		}
		return str;
	}
	
	private static <T> String createPrimaryKey(TableEntity<T> entity) {
		if (entity.getPkList().size() == 0)
			throw new EmptyPrimaryException("请检查是否设置了主键");
		return join(entity.getPkList());
	}

	private static <T> String createTableIndex(TableEntity<T> entity) {
		if (entity.getKeyList().size() == 0)
			return System.lineSeparator() + "";
		return String.format(TABLE_INDEX, entity.getTableName(), join(entity.getKeyList())) + System.lineSeparator();
	}
	
	private static String join(List<FieldEntity> entityList) {
		return join(entityList, "");
	}
	
	private static String join(List<FieldEntity> entityList, String combine) {
		return join(entityList, ", ", combine);
	}
	
	private static String join(List<FieldEntity> entityList, String sep, String combine) {
		String str = "";
		for (int i = 0;i < entityList.size(); ++ i) {
			if (i > 0) {
				str += sep;
			}
			str += String.format("`%s`%s", entityList.get(i).getColumnName(), combine);
		}
		return str;
	}
	
	private static String join(Object[] arrays, String sep) {
		String str = "";
		for (int i = 0;i < arrays.length; ++ i) {
			if (i > 0) {
				str += sep;
			}
			String fmt = "?".equals(arrays[ i ]) ? "%s" : "`%s`";
			str += String.format(fmt, arrays[ i ]);
		}
		return str;
	}
	
	private static <T> String updateFields(TableEntity<T> entity) {
		return join(entity.getFieldListWithoutPks(), " = ?");
	}
	
	private static <T> String whereConditon(TableEntity<T> entity) {
		return join(entity.getPkList(), " = ?");
	}
	
	private static <T> String valueFields(TableEntity<T> entity) {
		String[] sigs = new String[entity.getFieldList().size()];
		Arrays.fill(sigs, "?");
		return join(sigs, ", ");
	}
	
	private static String createPrepareValue(FieldEntity cond) {
		return String.format("`%s` = ?", cond.getColumnName());
	}
	
	private static <T> String selectFields(TableEntity<T> entity) {
		return join(entity.getFieldList());
	}
	
	private TableCommand() {}
}
