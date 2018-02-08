package net.gdface.image;

import net.gdface.exception.BaseFaceException;

/**
 * 获取图像异常
 * 
 * @author guyadong
 *
 */
public class ImageErrorException extends BaseFaceException {

	private static final long serialVersionUID = 124609099246843533L;

	public ImageErrorException(Throwable cause) {
		super(cause);
	}

	public ImageErrorException() {
	}

	public ImageErrorException(String s) {
		super(s);
	}

	public ImageErrorException(String s, Throwable cause) {
		super(s, cause);
	}

}
