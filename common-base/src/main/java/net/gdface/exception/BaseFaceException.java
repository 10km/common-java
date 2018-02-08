package net.gdface.exception;

/**
 * 项目中所有异常的基类
 * @author guyadong
 *
 */
public class BaseFaceException extends Exception {

	private static final long serialVersionUID = -745959836580373067L;

	public BaseFaceException(Throwable cause) {
		super(cause);
	}

	public BaseFaceException() {
	}

	public BaseFaceException(String s) {
		super(s);
	}

	public BaseFaceException(String s, Throwable cause) {
		super(s, cause);
	}

}
