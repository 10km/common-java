/**   
 * @Title: InsertFail.java 
 * @Package net.gdface.exception 
 * @Description: TODO 
 * @author guyadong   
 * @date 2014-10-8 下午7:28:05 
 * @version V1.0   
 */
package net.gdface.exception;

/**
 * 插入记录失败
 * 
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link InsertFailException}
 * 
 */
public class InsertFail extends FACEException {

	public InsertFail(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6281395139949032954L;

	/**
	 * 
	 */
	public InsertFail() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public InsertFail(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public InsertFail(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
