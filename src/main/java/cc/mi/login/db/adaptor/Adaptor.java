package cc.mi.login.db.adaptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import cc.mi.login.db.core.FieldEntity;

/**
 * 值适配器
 * @author misschuer
 */
public enum Adaptor {
	Object {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getObject(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ps.setObject(index, ce.getField().get(entity));
		}
	}, 
	INTEGER {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getInt(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			int value = (int)ce.getField().get(entity);
			if (value == 0 && !"".equals(ce.getDefault().trim())) {
				value = Integer.parseInt(ce.getDefault());
			}
			ps.setInt(index, value);
		}
	}, 
	LONG {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getLong(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			long value = (long)ce.getField().get(entity);
			if (value == 0L && !"".equals(ce.getDefault().trim())) {
				value = Long.parseLong(ce.getDefault());
			}
			ps.setLong(index, value);
		}
	}, 
	FLOAT {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getFloat(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			float value = (float)ce.getField().get(entity);
			if (value == 0f && !"".equals(ce.getDefault().trim())) {
				value = Float.parseFloat(ce.getDefault());
			}
			ps.setFloat(index, value);
		}
	}, 
	DOUBLE {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getDouble(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			double value = (double)ce.getField().get(entity);
			if (value == 0d && !"".equals(ce.getDefault().trim())) {
				value = Double.parseDouble(ce.getDefault());
			}
			ps.setDouble(index, value);
		}
	}, 
	STRING {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getString(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			String value = (String)ce.getField().get(entity);
			if (value == null) {
				value = ce.getDefault();
			}
			ps.setString(index, value);
		}
	}, 
	ATOMIC_INTEGER {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getInt(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			AtomicInteger ai = (AtomicInteger)ce.getField().get(entity);
			ps.setInt(index, ai.get());
		}
	},
	BOOLEAN {
		@Override
		public void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ce.getField().set(entity, rs.getBoolean(ce.getColumnName()));
		}
		
		@Override
		public void set(PreparedStatement ps, FieldEntity ce, Object entity,
				int index) throws IllegalArgumentException, IllegalAccessException, SQLException {
			ps.setBoolean(index, (boolean) ce.getField().get(entity));
		}
	};
	
	
	public abstract void get(ResultSet rs, FieldEntity ce, Object entity) throws IllegalArgumentException, IllegalAccessException, SQLException;
	public abstract void set(PreparedStatement ps, FieldEntity ce, Object entity, int index) throws IllegalArgumentException, IllegalAccessException, SQLException;
}
