package net.gdface.image;

/**
 * 没有找到{@link LazyImageFactory}实例
 * @author guyadong
 *
 */
public class NotFoundLazyImageFactoryException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NotFoundLazyImageFactoryException(Throwable cause) {
		super(cause);
	}

	public NotFoundLazyImageFactoryException() {
	}

	public NotFoundLazyImageFactoryException(String s) {
		super(s);
	}

	public NotFoundLazyImageFactoryException(String s, Throwable cause) {
		super(s, cause);
	}

}
