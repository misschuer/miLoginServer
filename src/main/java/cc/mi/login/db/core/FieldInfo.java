package cc.mi.login.db.core;

import cc.mi.login.db.adaptor.Adaptor;

public final class FieldInfo {
	private final String name;
	private Adaptor valueAdaptor;
	private final boolean nullable;
	private final boolean isPrimary;
	private final boolean isAutoIncrement;
	private final String defaultValue;
	
//	String field 	= rs.getString(1);	// account_id
//	String type 	= rs.getString(2);	// int(11)
//	String nils 	= rs.getString(3);	// NO / YES
//	String keys 	= rs.getString(4);	// PRI / ""
//	String deft		= rs.getString(5);	// ""
//	String extra 	= rs.getString(6);	// auto_increment / ""
	
	public FieldInfo(String field, String type, String nils, String keys, String deft, String extra) {
		this.name = field;
		this.nullable = nils.equalsIgnoreCase("YES");
		this.isPrimary = keys.equalsIgnoreCase("PRI");
		this.defaultValue = deft;
		this.isAutoIncrement = extra.equalsIgnoreCase("auto_increment");
		this.parseType(type);
	}

	private void parseType(String type) {
		if (type.startsWith("int")) {
			valueAdaptor = Adaptor.INTEGER;
		}
		else if (type.startsWith("double")) {
			valueAdaptor = Adaptor.DOUBLE;
		}
		else if (type.startsWith("float")) {
			valueAdaptor = Adaptor.FLOAT;
		}
		else if (type.startsWith("varchar")) {
			valueAdaptor = Adaptor.STRING;
		}
	}
	
	public String getName() {
		return name;
	}

	public Adaptor getValueAdaptor() {
		return valueAdaptor;
	}

	public boolean isNullable() {
		return nullable;
	}

	public boolean isPrimary() {
		return isPrimary;
	}

	public boolean isAutoIncrement() {
		return isAutoIncrement;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

}
