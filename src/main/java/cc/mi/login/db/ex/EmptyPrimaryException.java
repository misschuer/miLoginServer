package cc.mi.login.db.ex;

public class EmptyPrimaryException extends RuntimeException {
	private static final long serialVersionUID = -848474804174604407L;
	public EmptyPrimaryException() {
	}

	public EmptyPrimaryException(String message) {
		super(message);
	}

	public EmptyPrimaryException(Throwable cause) {
		super(cause);
	}

	public EmptyPrimaryException(String message, Throwable cause) {
		super(message, cause);
	}

	public EmptyPrimaryException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
