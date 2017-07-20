package cc.mi.login.db.utils;

import java.util.Map;

public final class Format {

	private Format() {}

	public static String format(String fmt, Map<String, Object> kvargs) {
		for (String key : kvargs.keySet()) {
			fmt = fmt.replaceAll("{" + key + "}", kvargs.get(key).toString());
		}
		return fmt;
	}
}
