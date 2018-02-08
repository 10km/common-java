package net.gdface.exception;

/**
 * 插入记录失败
 * 
 * @author guyadong
 *
 */
public class InsertFailException extends BaseFaceException {

	public InsertFailException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -6281395139949032954L;

	public InsertFailException() {
	}

	public InsertFailException(String s) {
		super(s);
	}

public InsertFailException(String s, Throwable cause) {
		super(s, cause);
	}

}
