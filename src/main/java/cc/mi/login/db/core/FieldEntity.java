package cc.mi.login.db.core;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import cc.mi.login.db.adaptor.Adaptor;
import cc.mi.login.db.annotation.AutoIncrement;
import cc.mi.login.db.annotation.Column;

public final class FieldEntity {
	private final Field field;
	private final Column column;
	
	private boolean autoIncrement;
	private Adaptor valueAdaptor;
	
	public FieldEntity(Field field) {
		this.field = field;
		this.column = field.getAnnotation(Column.class);
		this.autoIncrement = field.isAnnotationPresent(AutoIncrement.class);
		this.adaptor();
	}

	public Column getColumn() {
		return column;
	}
	
	public boolean isAutoIncrement() {
		return autoIncrement;
	}
	
	public String getColumnName() {
		return column.name();
	}
	
	public String getDefault() {
		return column.defaultValue();
	}

	public Field getField() {
		return field;
	}
	
	public String getFieldName() {
		return field.getName();
	}

	public Type getType() {
		return this.field.getGenericType();
	}
	
	private void adaptor() {
		this.field.setAccessible(true);
		this.valueAdaptor = Lookout.lookout(field.getGenericType());
		if (this.valueAdaptor == null)
			throw new RuntimeException("未定义类型:" + field.getGenericType());
	}

	public Adaptor getValueAdaptor() {
		return valueAdaptor;
	}
}
