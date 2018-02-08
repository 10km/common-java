/**   
 * @Title: Base64DecodeFail.java 
 * @Package net.gdface.exception 
 * @Description: TODO 
 * @author guyadong   
 * @date 2014-10-8 下午7:26:02 
 * @version V1.0   
 */
package net.gdface.exception;

/**
 * Base64解码失败
 * 
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link Base64DecodeFailException}
 *
 */
public class Base64DecodeFail extends FACEException {

	public Base64DecodeFail(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5085018474030608520L;

	/**
	 * 
	 */
	public Base64DecodeFail() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public Base64DecodeFail(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public Base64DecodeFail(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
