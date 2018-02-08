/**   
* @Title: DeleteImgFail.java 
* @Package net.gdface.exception 
* @Description: TODO 
* @author guyadong   
* @date 2014-10-8 下午7:27:05 
* @version V1.0   
*/
package net.gdface.exception;

/**删除图像文件失败
 * @author guyadong
 * @deprecated non-standard class name,instead use {@link DeleteImgFailException}
 * 
 */
public class DeleteImgFail extends FACEException {

	public DeleteImgFail(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4019289165841474369L;

	/**
	 * 
	 */
	public DeleteImgFail() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 */
	public DeleteImgFail(String s) {
		super(s);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param s
	 * @param cause
	 */
	public DeleteImgFail(String s, Throwable cause) {
		super(s, cause);
		// TODO Auto-generated constructor stub
	}

}
