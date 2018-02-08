package net.gdface.exception;

/**
 * 删除记录失败
 * 
 * @author guyadong
 *
 */
public class DeleteFailException extends BaseFaceException {

	public DeleteFailException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -8262750359150416117L;

	public DeleteFailException() {
	}

	public DeleteFailException(String s) {
		super(s);
	}

	public DeleteFailException(String s, Throwable cause) {
		super(s, cause);
	}

}
