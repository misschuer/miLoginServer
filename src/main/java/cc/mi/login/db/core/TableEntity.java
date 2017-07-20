package cc.mi.login.db.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cc.mi.login.db.annotation.AutoIncrement;
import cc.mi.login.db.annotation.Column;
import cc.mi.login.db.annotation.Table;
import cc.mi.login.db.ex.FieldAnnotationException;

public final class TableEntity<T> {
	private final Class<T> clazz;
	private final List<FieldEntity> fieldList;
	private final List<FieldEntity> pkList;
	private final List<FieldEntity> keyList;
	
	private FieldEntity autoField;
	private String tableName;
	private String comment;
	
	public TableEntity(Class<T> clazz) {
		this.clazz = clazz;
		fieldList = new ArrayList<>();
		pkList = new ArrayList<>();
		keyList = new ArrayList<>();
		
		init();
	}
	
	private void init() {
		ensureAnnotation(this.clazz, preparePrintName(this.clazz), Table.class);
		
		Table tableAnno = this.clazz.getAnnotation(Table.class);
		tableName 		= tableAnno.name();
		comment 		= tableAnno.comment();
		String[] pks 	= tableAnno.pks();
		String[] keys   = tableAnno.keys();
		
		Map<String, Boolean> pkMap = new HashMap<>();
		for (String pk : pks) {
			pkMap.put(pk, true);
		}
		
		Map<String, Boolean> keyMap = new HashMap<>();
		for (String key : keys) {
			keyMap.put(key, true);
		}
		
		
		Field[] fields = this.clazz.getDeclaredFields();
		for (Field field : fields) {
			ensureAnnotation(field, preparePrintName(field), Column.class);
			
			FieldEntity fieldEntity = new FieldEntity(field);
			fieldList.add(fieldEntity);
			
			// 数据库字段只允许出现字母数字
			if (!fieldEntity.getColumnName().matches("[a-z]+\\w*")) {
				throw new RuntimeException(String.format("必须以小写字母开头且只包含字母，下划线，数字。 错误位置> class:%s, Field:%s", clazz.getName(), field.getName()));
			}
			
			if (pkMap.containsKey(fieldEntity.getColumnName())) {
				pkList.add(fieldEntity);
			}
			
			if (keyMap.containsKey(fieldEntity.getColumnName())) {
				keyList.add(fieldEntity);
			}
			
			if (field.isAnnotationPresent(AutoIncrement.class)) {
				ensureAutoIncreOnce(field);
				autoField = fieldEntity;
			}
		}
	}
	
	public List<FieldEntity> alterColumns(Map<String, FieldInfo> hash) {
		List<FieldEntity> alters = new LinkedList<>();
		for (FieldEntity fieldEntity : this.fieldList) {
			if (!hash.containsKey(fieldEntity.getColumnName())) {
				alters.add(fieldEntity);
			}
			hash.remove(fieldEntity.getColumnName());
		}
		
		if (hash.size() > 0) {
			String[] fields = new String[hash.size()];
			int index = 0;
			for (String name : hash.keySet()) {
				fields[index++] = name;
			}
			throw new RuntimeException("表字段比实体类字段多:" + Arrays.toString(fields));
		}
		
		return alters;
	}
	
	private void ensureAnnotation(AnnotatedElement element, String printName, Class<? extends Annotation> annotation) {
		if (!element.isAnnotationPresent(annotation))
			throw new FieldAnnotationException(String.format("%s lack of annotation %s", printName, annotation.getName()));
	}
	
	private String preparePrintName(Field field) {
		return String.format("Field:[%s]", field.getName());
	}
	
	private String preparePrintName(Class<?> clazz) {
		return String.format("Class:[%s]", clazz.getCanonicalName());
	}
	
	private void ensureAutoIncreOnce(Field field) {
		if (autoField != null)
			throw new FieldAnnotationException(String.format("field:[%s] and field:[%s] both AutoIncrement", autoField.getField().getName(), field.getName()));
	}
	
	public Class<T> getClazz() {
		return clazz;
	}

	public String getTableName() {
		return tableName;
	}

	public List<FieldEntity> getFieldList() {
		return fieldList;
	}

	public String getComment() {
		return comment;
	}

	public boolean isGenerated() {
		return this.autoField != null;
	}

	public List<FieldEntity> getPkList() {
		return pkList;
	}

	public List<FieldEntity> getKeyList() {
		return keyList;
	}
	
	public List<FieldEntity> getFieldListWithoutPks() {
		List<FieldEntity> ret = new LinkedList<>();
		for (FieldEntity fieldEntity : this.fieldList) {
			if (!this.pkList.contains(fieldEntity))
				ret.add(fieldEntity);
		}
		return ret;
	}
}
