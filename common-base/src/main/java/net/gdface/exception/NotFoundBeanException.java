package net.gdface.exception;

/**
 * 没有找到记录
 * 
 * @author guyadong
 *
 */
public class NotFoundBeanException extends BaseFaceException {

	private static final long serialVersionUID = 2556815862710085667L;

	public NotFoundBeanException(Throwable cause) {
		super(cause);
	}

	public NotFoundBeanException() {
	}

	public NotFoundBeanException(String s) {
		super(s);
	}

	public NotFoundBeanException(String s, Throwable cause) {
		super(s, cause);
	}

}
