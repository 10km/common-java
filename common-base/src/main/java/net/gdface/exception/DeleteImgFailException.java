package net.gdface.exception;

/**
 * 删除图像文件失败
 * @author guyadong
 *
 */
public class DeleteImgFailException extends BaseFaceException {

	public DeleteImgFailException(Throwable cause) {
		super(cause);
	}

	private static final long serialVersionUID = 4019289165841474369L;

	public DeleteImgFailException() {
	}

	public DeleteImgFailException(String s) {
		super(s);
	}

	public DeleteImgFailException(String s, Throwable cause) {
		super(s, cause);
	}

}
