/**   
* @Title: NotImage.java 
* @Package net.gdface.image 
* @Description: TODO 
* @author guyadong   
* @date 2015年5月27日 上午11:09:56 
* @version V1.0   
*/
package net.gdface.image;


/**
 * 非图像数据
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link NotImageException}
 * 
 */
public class NotImage extends ImageError {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2929925579607787681L;

	/**
	 * @param cause
	 */
	public NotImage(Throwable cause) {
		super(cause);
		// TODO 自动生成的构造函数存根
	}

	/**
	 * 
	 */
	public NotImage() {
		// TODO 自动生成的构造函数存根
	}

	/**
	 * @param s
	 */
	public NotImage(String s) {
		super(s);
		// TODO 自动生成的构造函数存根
	}

	/**
	 * @param s
	 * @param cause
	 */
	public NotImage(String s, Throwable cause) {
		super(s, cause);
		// TODO 自动生成的构造函数存根
	}

}
