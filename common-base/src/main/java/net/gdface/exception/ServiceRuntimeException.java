package net.gdface.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
//<omit-j2cpp>
import net.gdface.utils.FaceUtilits;
//</omit-j2cpp>
/**
 * webservice调用产生的运行时异常<br>
 * 调用webservice方法时产生的所有{@link java.lang.RuntimeException}在抛出到webservice客户时被封装在{@link ServiceRuntimeException}中<br>
 * 调用 {@link #getServerStackTraceMessage()}可以获取服务器端的堆栈错误信息<br>
 * 调用{@link #printServerStackTrace()}输出服务器端的堆栈错误信息<br>
 * @author guyadong
 *
 */
public class ServiceRuntimeException extends Exception {
	/**
	 * 保存服务器端错误堆栈信息
	 */
	private String serverStackTraceMessage = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6222189909743043773L;

	/**
	 * 
	 */
	public ServiceRuntimeException() {
	}

	/**
	 * @param message
	 */
	public ServiceRuntimeException(String message) {
		super(message);
	}
	//<omit-j2cpp>
	/**
	 * @param message
	 * @param cause
	 */
	public ServiceRuntimeException(String message, Throwable cause) {
		super(message, FaceUtilits.stripThrowableShell(cause, RuntimeException.class));
		fillStackTraceMessage(getCause());
	}

	/**
	 * @param cause
	 */
	public ServiceRuntimeException(Throwable cause) {
		super(FaceUtilits.stripThrowableShell(cause, RuntimeException.class));
		fillStackTraceMessage(getCause());
	}

	/**
	 * 调用{@link #printStackTrace(PrintWriter)}将错误堆栈信息存入 {@link #serverStackTraceMessage}
	 * 
	 * @param cause
	 * @see #printStackTrace(PrintWriter)
	 */
	private void fillStackTraceMessage(Throwable cause) {
		if (null != cause) {
			StringWriter write = new StringWriter(256);
			PrintWriter pw = new PrintWriter(write);
			cause.printStackTrace(pw);
			serverStackTraceMessage = write.toString();
		}
	}

	/**
	 * 输出服务器端堆栈错误信息
	 * @see #printStackTrace()	 
	 */
	public void printServerStackTrace() {
		printServerStackTrace(System.err);
	}

	/**
	 * @param s
	 * @see #printServerStackTrace()
	 * @see #printStackTrace(PrintStream)
	 */
	public void printServerStackTrace(PrintStream s) {
		synchronized (s) {
			s.println(serverStackTraceMessage);
		}
	}

	/**
	 * @param s
	 * @see #printServerStackTrace()
	 * @see #printStackTrace(PrintWriter)
	 */
	public void printServerStackTrace(PrintWriter s) {
		synchronized (s) {
			s.println(serverStackTraceMessage);
		}
	}
	//</omit-j2cpp>
	/**
	 * 返回服务器端异常的堆栈信息
	 * @return serverStackTraceMessage
	 */
	public String getServerStackTraceMessage() {
		return serverStackTraceMessage;
	}
	/**
	 * @param serverStackTraceMessage
	 *            要设置的 serverStackTraceMessage
	 */
	public void setServerStackTraceMessage(String serverStackTraceMessage) {
		this.serverStackTraceMessage = serverStackTraceMessage;
	}
}
