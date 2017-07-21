/**   
* @Title: FException.java 
* @Package net.gdface.exception 
* @Description: 项目中所有异常的基类定义 
* @author guyadong   
* @date 2014-10-8 下午7:21:07 
* @version V1.0   
*/
package net.gdface.exception;

/**
 * 项目中所有异常的基类
 * @author guyadong
 *
 */
public class FACEException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -745959836580373067L;

	public FACEException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	public FACEException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public FACEException(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public FACEException(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
