package net.gdface.image;


/**
 * 非图像数据
 * @author guyadong
 */
public class NotImageException extends ImageErrorException {

	private static final long serialVersionUID = -2929925579607787681L;

	public NotImageException(Throwable cause) {
		super(cause);
	}

	public NotImageException() {
	}

	public NotImageException(String s) {
		super(s);
	}

	public NotImageException(String s, Throwable cause) {
		super(s, cause);
	}

}
