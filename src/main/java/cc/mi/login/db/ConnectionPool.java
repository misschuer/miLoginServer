package cc.mi.login.db;

import cc.mi.login.db.utils.DBUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 这个连接池必须先执行
 * @author misschuer
 *
 */
public final class ConnectionPool {
    /** 需要c3p0-config.xml文件配置 必须和放在和类相同路径下 */
    private static final String COMBOL_POOLED_CONFIG_NAME = "dbConfig";
    private static volatile ComboPooledDataSource ds = null;

    /**
     * 连接池进行赋值
     */
    public static void init() {
    	ds = new ComboPooledDataSource(COMBOL_POOLED_CONFIG_NAME);
    	DBUtils.setDS(ds);
    }
}