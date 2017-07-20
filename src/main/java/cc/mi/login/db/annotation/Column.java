package cc.mi.login.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/** 名字 */
	public String name() 			default "";
	/** varchar类型的长度 */
	public int length() 			default 64;
	/** double类型的长度 */
	public int precision() 			default 15;
	/** double类型的保留小数位 */
	public int scale() 				default 5;
	/** 是否为空 */
	public boolean nullable() 		default false;
	/** 默认值 */
	public String defaultValue()	default "";
	/** 注释 */
	public String comment() 		default "";
}
