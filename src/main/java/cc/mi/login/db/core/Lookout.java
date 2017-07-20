package cc.mi.login.db.core;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import cc.mi.login.db.adaptor.Adaptor;

public final class Lookout {
	private static final Map<Class<?>, Adaptor> hash;
	static {
		hash = new HashMap<>();
		
		hash.put(int.class, Adaptor.INTEGER);
		hash.put(Integer.class, Adaptor.INTEGER);
		
		hash.put(long.class, Adaptor.LONG);
		hash.put(Long.class, Adaptor.LONG);
		
		hash.put(float.class, Adaptor.FLOAT);
		hash.put(Float.class, Adaptor.FLOAT);
		
		hash.put(double.class, Adaptor.DOUBLE);
		hash.put(Double.class, Adaptor.DOUBLE);
		
		hash.put(boolean.class, Adaptor.BOOLEAN);
		hash.put(Boolean.class, Adaptor.BOOLEAN);
		
		hash.put(String.class, Adaptor.STRING);
		
		hash.put(AtomicInteger.class, Adaptor.ATOMIC_INTEGER);
	}
	
	/**
	 * 获得对象的适配器
	 * @param type
	 * @return
	 */
	public static Adaptor lookout(Type type) {
		return hash.get(type);
	}

	/**
	 * 找到映射类对应的解析类
	 * @param mapper
	 * @return
	 */
	public static <T> TableEntity<T> lookout(T mapper) {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) mapper.getClass();
		TableEntity<T> entity = new TableEntity<T>(clazz);
		return entity;
	}
	
	private Lookout() {}
}
