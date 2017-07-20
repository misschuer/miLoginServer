package cc.mi.login.db.handler;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class PrepareStatementHandler<T> {
	public abstract T handle(PreparedStatement ps) throws SQLException;
}

