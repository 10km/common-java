/**   
 * @Title: ImageError.java 
 * @Package net.gdface.exception 
 * @Description: TODO 
 * @author guyadong   
 * @date 2014-10-8 下午7:27:34 
 * @version V1.0   
 */
package net.gdface.image;

import net.gdface.exception.FACEException;

/**
 * 获取图像异常
 * 
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link ImageErrorException}
 * 
 */
public class ImageError extends FACEException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 124609099246843533L;

	public ImageError(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	public ImageError() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public ImageError(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public ImageError(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
