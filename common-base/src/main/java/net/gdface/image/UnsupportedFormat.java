/**   
* @Title: UnsupportedFormat.java 
* @Package net.gdface.image 
* @Description: TODO 
* @author guyadong   
* @date 2015年5月27日 上午11:13:16 
* @version V1.0   
*/
package net.gdface.image;


/**
 * 不支持的图像格式
 * @author guyadong
 *
 */
public class UnsupportedFormat extends ImageError {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5515183993603303378L;

	/**
	 * @param cause
	 */
	public UnsupportedFormat(Throwable cause) {
		super(cause);
		// TODO 自动生成的构造函数存根
	}

	/**
	 * 
	 */
	public UnsupportedFormat() {
		// TODO 自动生成的构造函数存根
	}

	/**
	 * @param s
	 */
	public UnsupportedFormat(String s) {
		super(s);
		// TODO 自动生成的构造函数存根
	}

	/**
	 * @param s
	 * @param cause
	 */
	public UnsupportedFormat(String s, Throwable cause) {
		super(s, cause);
		// TODO 自动生成的构造函数存根
	}

}
