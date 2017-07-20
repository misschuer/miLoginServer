package cc.mi.login.db.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cc.mi.login.db.adaptor.Adaptor;

public final class MapperFactory {
	private MapperFactory() {}

	/**
	 * 数据库的数据映射到类中
	 * @param clazz
	 * @param rs
	 * @return
	 */
	public static <T> T parserTableEntity(TableEntity<T> clazz, ResultSet rs) {
		T mapper = null;
		try {
			mapper = clazz.getClazz().newInstance();
			for (FieldEntity fieldEntity : clazz.getFieldList()) {
				Adaptor adapter = fieldEntity.getValueAdaptor();
				adapter.get(rs, fieldEntity, mapper);
			}
		} catch (InstantiationException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
		return mapper;
	}
	
	/**
	 * insert时类中的数据存入PreparedStatement
	 * @param ps
	 * @param mapper
	 */
	public static <T> void parserInsertMapper(PreparedStatement ps, T mapper) {
		try {
			TableEntity<T> entity = Lookout.lookout(mapper);
			int index = 0;
			for (FieldEntity fieldEntity : entity.getFieldList()) {
				++ index;
				Adaptor adapter = fieldEntity.getValueAdaptor();
				adapter.set(ps, fieldEntity, mapper, index);
			}
		} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * update时类中的数据存入PreparedStatement
	 * @param ps
	 * @param mapper
	 */
	public static <T> void parserUpdateMapper(PreparedStatement ps, T mapper) {
		int index = 0;
		try {
			TableEntity<T> entity = Lookout.lookout(mapper);
			for (FieldEntity fieldEntity : entity.getFieldListWithoutPks()) {
				++ index;
				Adaptor adapter = fieldEntity.getValueAdaptor();
				adapter.set(ps, fieldEntity, mapper, index);
			}
		} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
		parserDeleteMapper(ps, mapper, index);
	}
	
	/**
	 * delete时类中的数据存入PreparedStatement
	 * @param ps
	 * @param mapper
	 */
	public static <T> void parserDeleteMapper(PreparedStatement ps, T mapper, int index) {
		try {
			TableEntity<T> entity = Lookout.lookout(mapper);
			for (FieldEntity fieldEntity : entity.getPkList()) {
				++ index;
				Adaptor adapter = fieldEntity.getValueAdaptor();
				adapter.set(ps, fieldEntity, mapper, index);
			}
		} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
	}
}