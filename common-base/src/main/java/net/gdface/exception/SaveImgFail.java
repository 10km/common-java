/**   
 * @Title: SaveImgFail.java 
 * @Package net.gdface.exception 
 * @Description: TODO 
 * @author guyadong   
 * @date 2014-10-8 下午7:29:20 
 * @version V1.0   
 */
package net.gdface.exception;

/**
 * 图像存储失败
 * 
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link SaveImgFailException}
 *
 */
public class SaveImgFail extends FACEException {

	public SaveImgFail(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8000938520880615505L;

	/**
	 * 
	 */
	public SaveImgFail() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public SaveImgFail(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public SaveImgFail(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
