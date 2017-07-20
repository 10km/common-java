/**   
 * @Title: NotFoundBean.java 
 * @Package net.gdface.exception 
 * @Description: TODO 
 * @author guyadong   
 * @date 2014-10-8 下午7:28:31 
 * @version V1.0   
 */
package net.gdface.exception;

/**
 * 没有找到记录
 * 
 * @author guyadong
 *
 */
public class NotFoundBean extends FACEException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2556815862710085667L;

	public NotFoundBean(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	public NotFoundBean() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public NotFoundBean(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public NotFoundBean(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
