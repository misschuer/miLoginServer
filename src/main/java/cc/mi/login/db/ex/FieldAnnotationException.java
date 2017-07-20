package cc.mi.login.db.ex;

public class FieldAnnotationException extends RuntimeException {
	private static final long serialVersionUID = 7509149879489717853L;

	public FieldAnnotationException() {}

	public FieldAnnotationException(String message) {
		super(message);
	}

	public FieldAnnotationException(Throwable cause) {
		super(cause);
	}

	public FieldAnnotationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FieldAnnotationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
