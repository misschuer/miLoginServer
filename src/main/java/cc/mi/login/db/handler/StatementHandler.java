package cc.mi.login.db.handler;

import java.sql.Statement;
import java.sql.SQLException;

public abstract class StatementHandler<T> {
	public abstract T handle(Statement st) throws SQLException;
}