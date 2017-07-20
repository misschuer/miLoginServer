package cc.mi.login.db.check;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cc.mi.core.utils.ClasspathLoader;
import cc.mi.login.db.annotation.Table;
import cc.mi.login.db.command.TableCommand;
import cc.mi.login.db.core.FieldEntity;
import cc.mi.login.db.core.FieldInfo;
import cc.mi.login.db.core.TableEntity;
import cc.mi.login.db.utils.DBUtils;
import cc.mi.login.db.utils.MappingSQLPretreatment;
import cc.mi.login.db.utils.SQLOperateType;

public final class TableChecker {
	private TableChecker(){}
	
	/**
	 * 进程开启时
	 * 映射类成员变量和数据库字段同步
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void tableSync() throws ClassNotFoundException, IOException {
		List<Class<?>> clazzList = ClasspathLoader.list("cc.mi.login.table", Table.class);
		for (Class<?> clazz : clazzList) {
			TableEntity<?> entity = new TableEntity<>(clazz);
			boolean isTableExist = DBUtils.isExist(entity.getTableName());
			if (isTableExist) {
				Map<String, FieldInfo> hash = DBUtils.showColumns(entity.getTableName());
				List<FieldEntity> alters = entity.alterColumns(hash);
				for (FieldEntity fieldEntity : alters) {
					DBUtils.alterTable(entity.getTableName(), fieldEntity);
				}
			}
			else {
				DBUtils.createTable(entity);
			}
			// 预处理sql语句
			pretreatment(entity);
		}
	}
	
	private static void pretreatment(TableEntity<?> entity) {
		List<String> sqlList = Arrays.asList(
			TableCommand.createInsertSql(entity),
			TableCommand.createUpdateSql(entity),
			TableCommand.createSelectAllSql(entity),
			TableCommand.createDeleteSql(entity)
		);
		MappingSQLPretreatment.put(entity.getClazz(), sqlList);
	}
	
	public static void showSql() throws ClassNotFoundException, IOException {
		List<Class<?>> clazzList = ClasspathLoader.list("cc.mi.logical.mappin", Table.class);
		TableEntity<?> once = null;
		for (Class<?> clazz : clazzList) {
			TableEntity<?> entity = new TableEntity<>(clazz);
//			showOperateType(entity);
			// 预处理sql语句
			pretreatment(entity);
			once = entity;
		}
		
		long x1 = withDynamic(once);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long x2 = withStatic(once);
		System.out.println("一共相差" + (x1 * 1.0 / x2) + "倍");
	}
	
	private static long withDynamic(TableEntity<?> entity) {
		long a = System.nanoTime();
		for (int i = 0; i < 10000; ++ i) {
			TableCommand.createInsertSql(entity);
			TableCommand.createUpdateSql(entity);
			TableCommand.createSelectAllSql(entity);
			TableCommand.createDeleteSql(entity);
		}
		long b = System.nanoTime();
		System.out.println(TableCommand.createInsertSql(entity));
		System.out.println(TableCommand.createUpdateSql(entity));
		System.out.println(TableCommand.createSelectAllSql(entity));
		System.out.println(TableCommand.createDeleteSql(entity));
		DecimalFormat df1 = new DecimalFormat("#,###");
		System.out.println(df1.format(b-a) + "ns with dynamic");
		
		return b-a;
	}
	
	private static long withStatic(TableEntity<?> entity) {
		long a = System.nanoTime();
		for (int i = 0; i < 10000; ++ i) {
			MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_INSERT);
			MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_UPDATE);
			MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_SELECT);
			MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_DELETE);
		}
		long b = System.nanoTime();
		System.out.println(MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_INSERT));
		System.out.println(MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_UPDATE));
		System.out.println(MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_SELECT));
		System.out.println(MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_DELETE));
		DecimalFormat df1 = new DecimalFormat("#,###");
		System.out.println(df1.format(b-a) + "ns with static");
		
		return b-a;
	}
	
	protected static void showOperateType(TableEntity<?> entity) {
		System.out.println(TableCommand.createInsertSql(entity));
		System.out.println(TableCommand.createUpdateSql(entity));
		System.out.println(TableCommand.createSelectAllSql(entity));
		System.out.println(TableCommand.createDeleteSql(entity));
	}
}
