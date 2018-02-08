package net.gdface.exception;

/**
 * 图像存储失败
 * 
 * @author guyadong
 *
 */
public class SaveImgFailException extends BaseFaceException {

	public SaveImgFailException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = -8000938520880615505L;

	public SaveImgFailException() {
	}

	public SaveImgFailException(String s) {
		super(s);
	}

	public SaveImgFailException(String s, Throwable cause) {
		super(s, cause);
	}

}
