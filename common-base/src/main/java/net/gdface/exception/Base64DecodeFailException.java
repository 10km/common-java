package net.gdface.exception;

/**
 * Base64解码失败
 * 
 * @author guyadong
 *
 */
public class Base64DecodeFailException extends BaseFaceException {

	public Base64DecodeFailException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -5085018474030608520L;

	public Base64DecodeFailException() {
	}

	public Base64DecodeFailException(String s) {
		super(s);
	}

	public Base64DecodeFailException(String s, Throwable cause) {
		super(s, cause);
	}

}
