package net.gdface.image;


/**
 * 不支持的图像格式
 * @author guyadong
 *
 */
public class UnsupportedFormatException extends ImageErrorException {

	private static final long serialVersionUID = 5515183993603303378L;

	public UnsupportedFormatException(Throwable cause) {
		super(cause);
	}

	public UnsupportedFormatException() {
	}

	public UnsupportedFormatException(String s) {
		super(s);
	}

	public UnsupportedFormatException(String s, Throwable cause) {
		super(s, cause);
	}

}
