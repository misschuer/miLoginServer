package cc.mi.login.db.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import cc.mi.login.db.annotation.Table;
import cc.mi.login.db.command.FieldCommand;
import cc.mi.login.db.command.TableCommand;
import cc.mi.login.db.core.FieldEntity;
import cc.mi.login.db.core.FieldInfo;
import cc.mi.login.db.core.Lookout;
import cc.mi.login.db.core.MapperFactory;
import cc.mi.login.db.core.TableEntity;
import cc.mi.login.db.handler.PrepareStatementHandler;
import cc.mi.login.db.handler.StatementHandler;

/**
 * 静态初始化的sql语句效率是动态的200+倍
 * @author mi
 */
public class DBUtils {
	private static final Logger logger = Logger.getLogger(DBUtils.class);
	private static ComboPooledDataSource ds;
	private static final String DB_NAME = "gy";
	private static final String SHOW_TABLES = 
			"SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?";
	private static final String SHOW_COLUMNS = "SHOW COLUMNS FROM `%s`";
	private static final String COUNT = "SELECT COUNT(*) FROM `%s`";
	private static final String MAX = "SELECT MAX(`%s`) FROM `%s`";

	/**
	 * 表的记录数
	 * @param clazz
	 * @return
	 */
	public static <T> Integer count(final Class<T> clazz) {
		return DBUtils.execute(new StatementHandler<Integer>() {
			@Override
			public Integer handle(Statement st) throws SQLException {
				String sql = String.format(COUNT, clazz.getAnnotation(Table.class).name());
				logger.info("execute sql:" + sql);
				try (ResultSet rs = st.executeQuery(sql)) {
					return rs.next() ? rs.getInt(1) : 0;
				} catch (Exception e) {
					throw e;
				}
			}
		});
	}
	
	/**
	 * 获得某个字段的最大值
	 * @param clazz
	 * @param key
	 * @return
	 */
	public static <T> Object max(final Class<T> clazz, final String key) {
		return DBUtils.execute(new StatementHandler<Object>() {
			@Override
			public Object handle(Statement st) throws SQLException {
				String sql = String.format(MAX, key, clazz.getAnnotation(Table.class).name());
				logger.info("execute sql:" + sql);
				try (ResultSet rs = st.executeQuery(sql)) {
					return rs.next() ? rs.getObject(1) : null;
				} catch (Exception e) {
					throw e;
				}
			}
		});
	}
	
	/**
	 * 查找数据库中的所有表名
	 * @param conn
	 */
	public static List<String> showTables() {
		return DBUtils.execute(new PrepareStatementHandler<List<String>>() {
			@Override
			public List<String> handle(PreparedStatement ps) throws SQLException {
				ps.setString(1, DB_NAME);
				List<String> tables = new LinkedList<>();
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String tableName = rs.getString(1);
						tables.add(tableName);
					}
					return tables;
				} catch (Exception e) {
					tables.clear();
					throw e;
				}
			}
		}, SHOW_TABLES);
	}
	
	/**
	 * table is exist
	 * @param tableName
	 * @return
	 */
	public static boolean isExist(final String tableName) {
		return DBUtils.execute(new StatementHandler<Boolean>() {
			@Override
			public Boolean handle(Statement st) throws SQLException {
				String sql = String.format("show tables like '%s' ", tableName);
				logger.info("execute sql:" + sql);
				try (ResultSet rs = st.executeQuery(sql)) {
					return rs.next() ? true : false;
				} catch (Exception e) {
					throw e;
				}
			}
		});
	}
	
	/**
	 * 查询数据库对应表中的字段
	 * @param tableName
	 * @return
	 */
	public static Map<String, FieldInfo> showColumns(String tableName) {
		return DBUtils.execute(new PrepareStatementHandler<Map<String, FieldInfo>>() {
			@Override
			public Map<String, FieldInfo> handle(PreparedStatement ps) throws SQLException {
				try (ResultSet rs = ps.executeQuery()) {
					Map<String, FieldInfo> hash = new HashMap<>();
					while (rs.next()) {
						String field 	= rs.getString(1);
						String type 	= rs.getString(2);
						String nils 	= rs.getString(3);
						String keys 	= rs.getString(4);
						String deft		= rs.getString(5);
						String extra 	= rs.getString(6);
						FieldInfo fieldInfo = new FieldInfo(field, type, nils, keys, deft, extra);
						hash.put(field, fieldInfo);
					}
					return hash;
				} catch (Exception e) {
					throw e;
				}
			}
		}, String.format(SHOW_COLUMNS, tableName));
	}
	
	/**
	 * 生成列
	 * @param tableName
	 * @param fieldEntity
	 */
	public static void alterTable(String tableName, FieldEntity fieldEntity) {
		DBUtils.execute(new PrepareStatementHandler<Void>() {
			@Override
			public Void handle(PreparedStatement ps) throws SQLException {
				try {
					ps.execute();
					return null;
				} catch(Exception e) {
					throw e;
				}
			}
		}, FieldCommand.createAddColumnSql(tableName, fieldEntity));
	}
	
	/**
	 * 生成表
	 * @param tableEntity
	 */
	public static <T> void createTable(TableEntity<T> tableEntity) {
		DBUtils.execute(new PrepareStatementHandler<Void>() {
			@Override
			public Void handle(PreparedStatement ps) throws SQLException {
				try {
					ps.execute();
					return null;
				} catch(Exception e) {
					throw e;
				}
			}
		}, TableCommand.createTableSql(tableEntity));
	}
	
	/**
	 * 查询一个
	 * @param clazz
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> T fecthOne(Class<T> clazz, String key, final Object value) {
		final TableEntity<T> entity = new TableEntity<T>(clazz);
		// 动态生成 TableCommand.createSelectSql(entity, key) + " LIMIT 1"
		String sql = MappingSQLPretreatment.getSql(clazz, SQLOperateType.TYPE_SELECT);
		// 加LIMIT 1 提高效率
		sql = TableCommand.joinSelectConditon(sql, key) + " LIMIT 1";
		
		return DBUtils.execute(new PrepareStatementHandler<T>() {
			@Override
			public T handle(PreparedStatement ps) throws SQLException {
				ps.setObject(1, value);
				try (ResultSet rs = ps.executeQuery()) {
					return rs.next() ? MapperFactory.parserTableEntity(entity, rs) : null;
				} catch (Exception e) {
					throw e;
				}
			}
		}, sql);
	}
	
	/**
	 * 查询多个
	 * @param clazz
	 * @param key
	 * @param value
	 * @return
	 */
	public static <T> List<T> fecthMany(Class<T> clazz, String key, final Object value) {
		final TableEntity<T> entity = new TableEntity<T>(clazz);
		// 动态生成 TableCommand.createSelectSql(entity, key)
		String sql = MappingSQLPretreatment.getSql(clazz, SQLOperateType.TYPE_SELECT);
		sql = TableCommand.joinSelectConditon(sql, key);
				
		return DBUtils.execute(new PrepareStatementHandler<List<T>>() {
			@Override
			public List<T> handle(PreparedStatement ps) throws SQLException {
				ps.setObject(1, value);
				try (ResultSet rs = ps.executeQuery()) {
					List<T> ret = new ArrayList<T>();
					while (rs.next()) {
						ret.add(MapperFactory.parserTableEntity(entity, rs));
					}
					return ret;
				} catch (Exception e) {
					throw e;
				}
			}
		}, sql);
	}
	
	public static <T> List<T> fetchMany(Class<T> clazz) {
		final TableEntity<T> entity = new TableEntity<T>(clazz);
		// 动态生成 TableCommand.createSelectAllSql(entity, key)
		String sql = MappingSQLPretreatment.getSql(clazz, SQLOperateType.TYPE_SELECT);
		
		return DBUtils.execute(new PrepareStatementHandler<List<T>>() {
			@Override
			public List<T> handle(PreparedStatement ps) throws SQLException {
				try (ResultSet rs = ps.executeQuery()) {
					List<T> ret = new ArrayList<T>();
					while (rs.next()) {
						ret.add(MapperFactory.parserTableEntity(entity, rs));
					}
					return ret;
				} catch (Exception e) {
					throw e;
				}
			}
		}, sql);
	}
	
	/**
	 * 插入数据 默认都以
	 * @param mapper
	 * @return
	 */
	public static <T> Integer insert(final T mapper) {
		TableEntity<T> entity = Lookout.lookout(mapper);
		// 动态生成 TableCommand.createInsertSql(entity, key)
		String sql = MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_INSERT);
				
		return DBUtils.execute(new PrepareStatementHandler<Integer>() {
			@Override
			public Integer handle(PreparedStatement ps) throws SQLException {
				MapperFactory.parserInsertMapper(ps, mapper);
				ps.execute();
				try (ResultSet rs = ps.getGeneratedKeys()) {
					if (rs.next()) {
						// ubuntu
						return rs.getInt(1);
						// centOS
//						return ((Long)rs.getObject(1)).intValue();
					}
					return -1;
				} catch(Exception e) {
					throw e;
				}
			}
		}, sql);
	}
	
	/**
	 * 更新数据
	 * @param mapper
	 * @return
	 */
	public static <T> Void update(final T mapper) {
		TableEntity<T> entity = Lookout.lookout(mapper);
		// 动态生成 TableCommand.createUpdateSql(entity, key)
		String sql = MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_UPDATE);
				
		return DBUtils.execute(new PrepareStatementHandler<Void>() {
			@Override
			public Void handle(PreparedStatement ps) throws SQLException {
				MapperFactory.parserUpdateMapper(ps, mapper);
				ps.executeUpdate();
				return null;
			}
		}, sql);
	}
	
	/**
	 * 删除数据
	 * @param mapper
	 * @return
	 */
	public static <T> Void delete(final T mapper) {
		TableEntity<T> entity = Lookout.lookout(mapper);
		// 动态生成 TableCommand.createDeleteSql(entity, key)
		String sql = MappingSQLPretreatment.getSql(entity.getClazz(), SQLOperateType.TYPE_DELETE);
				
		return DBUtils.execute(new PrepareStatementHandler<Void>() {
			@Override
			public Void handle(PreparedStatement ps) throws SQLException {
				MapperFactory.parserDeleteMapper(ps, mapper, 0);
				ps.executeUpdate();
				return null;
			}
		}, sql);
	}
	
	/**
	 * 自定义sql
	 * @param sql
	 * @return
	 */
	public static <T> List<List<Object>> customFetch(final String sql) {
		return DBUtils.execute(new StatementHandler<List<List<Object>>>() {
			@Override
			public List<List<Object>> handle(Statement st) throws SQLException {
				List<List<Object>> ret = new ArrayList<>();
				try (ResultSet rs = st.executeQuery(sql)) {
					while (rs.next()) {
						int length = rs.getMetaData().getColumnCount();
						List<Object> sub = new ArrayList<>(length);
						for (int i = 1; i <= length; ++ i) {
							sub.add(rs.getObject(i));
						}
						ret.add(sub);
					}
					return ret;
				} catch (Exception e) {
					throw e;
				}
			}
		});
	}
	
	/**
	 * 执行preparestatement
	 * @param handler
	 * @param sql
	 */
	private static <T> T execute(PrepareStatementHandler<T> handler, String sql) {
		try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			logger.info("execute sql:" + sql);
			return handler.handle(ps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 执行statement
	 * @param handler
	 * @return
	 */
	private static <T> T execute(StatementHandler<T> handler) {
		try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
			return handler.handle(st);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * check out a connection
	 * @return
	 * @throws SQLException
	 */
	private static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
	
	/**
	 * IOC set connectionpools
	 * @param ds
	 */
	public static void setDS(ComboPooledDataSource ds) {
		DBUtils.ds = ds;
		logger.info("ComboPooledDataSource setted");
	}
}
