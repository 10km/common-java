/**   
 * @Title: DeleteFail.java 
 * @Package net.gdface.exception 
 * @Description: TODO 
 * @author guyadong   
 * @date 2014-10-8 下午7:26:36 
 * @version V1.0   
 */
package net.gdface.exception;

/**
 * 删除记录失败
 * 
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link DeleteFailException}
 *
 */
public class DeleteFail extends FACEException {

	public DeleteFail(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8262750359150416117L;

	/**
	 * 
	 */
	public DeleteFail() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public DeleteFail(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public DeleteFail(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
