package cc.mi.login.db;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;

/**
 * 数据操作管理
 * FIXME 不成熟 还不能用
 * @author gongyuan
 */
public abstract class DBStatedMultiObject<T extends DBState> {
	static final Logger logger = Logger.getLogger(DBStatedMultiObject.class);
	final int userId = -1; //FIXME 需要处理
	final int capacity;
	final Queue<Integer> pq; 
	final Map<Integer, T> mainHash;
	
	/**
	 * 是否容量已满
	 * @return
	 */
	public boolean isFull() {
		return pq.isEmpty();
	}
	
	public Iterator<T> iterator() {
		return mainHash.values().iterator();
	}
	
	public void init(Map<Integer, T> initHash) {
		for (int key : initHash.keySet()) {
			mainHash.put(key, initHash.get(key));
		}
		initHash.clear();
	}
	
	/**
	 * @param capacity 数据最大容量
	 */
	public DBStatedMultiObject(int capacity) {
		this.capacity = capacity;
		pq = new PriorityQueue<>(capacity);
		for (int i = 0; i < capacity; ++ i) {
			pq.add(i);
		}
		mainHash = new HashMap<>(capacity);
	}
	
	/**
	 * key位置的数据是否存在
	 * @param key
	 * @return
	 */
	public boolean containsKey(int key) {
		return mainHash.containsKey(key);
	}
	
	/**
	 * 得到key位置的数据
	 * @param key
	 * @return
	 */
	public T get(int key) {
		return mainHash.get(key);
	}
	
	/**
	 * 增加数据
	 * @param key
	 * @param value
	 */
	public int addAndReturnKey(T value) {
		int key = pq.poll();
		mainHash.put(key, value);
		value.notifyInsert();
		return key;
	}
	
	/**
	 * 删除数据
	 * @param key
	 * @return
	 */
	public void remove(int userId, int key) {
		ensure(key);
		pq.add(key);
//		T value = mainHash.remove(key);
		// 进行数据库删除操作
//TODO:		AsyncThreadGroupSwitch.deleteDB(userId, value);
	}
	
	public void clear() {
		pq.clear();
		mainHash.clear();
	}
	
	private void ensure(int key) {
		if (key < 0 || key >= this.capacity)
			throw new CapacityExceededException(String.format("key=%d不为容量内位置", key));
	}
	
	/**
	 * 获得空槽的位置
	 * @return
	 */
	public int getEmptyKey() {
		int key = -1;
		if (!pq.isEmpty()) {
			key = pq.peek();
		}
		return key;
	}
	
	/**
	 * 存数据库
	 */
	public void save() {
		Iterator<Integer> iterMain = mainHash.keySet().iterator();
		for (;iterMain.hasNext();) {
//			int key = iterMain.next();
//			T value = mainHash.get(key);
//TODO:			AsyncThreadGroupSwitch.executeDB(userId, value);
		}
	}
	
	static class EmptyObjectException extends RuntimeException {
		private static final long serialVersionUID = -5779479072867495920L;

		public EmptyObjectException() {}
		
		public EmptyObjectException(String message) {
			super(message);
		}
	}
	
	static class CapacityExceededException extends RuntimeException {
		private static final long serialVersionUID = -7072918271033638837L;

		public CapacityExceededException() {}
		
		public CapacityExceededException(String message) {
			super(message);
		}
	}
}