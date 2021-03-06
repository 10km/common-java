package net.gdface.thrift.exception.client;

import java.io.PrintStream;
import java.io.PrintWriter;

import com.google.common.base.Preconditions;

/**
 * Runtime exception wrap class<br>
 * all {@link RuntimeException} threw from service was wrapped to the exception<br>
 * catch the exception to retrieve detail error message from service<br>
 * retrieve service stack trace message by call {@link #getServiceStackTraceMessage()}<br>
 * get exception type by call {@link #getType()}
 * @author guyadong
 *
 */
public abstract class BaseServiceRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    protected int type;
    protected String message;
    protected String causeClass;
    protected String causeFields;
    protected String serviceStackTraceMessage;

    /**
     * @param cause
     */
    protected BaseServiceRuntimeException(Exception cause) {
        super(cause);
    }

    /**
     * print stack trace message from service to {@link System#err}
     * @see #printStackTrace()
     */
    public void printServiceStackTrace() {
        printServiceStackTrace(System.err);
    }

    /**
     * @param s
     * @see #printServiceStackTrace()
     * @see #printStackTrace(PrintStream)
     * @throws NullPointerException s is {@code null}
     */
    public void printServiceStackTrace(PrintStream s) {
        synchronized (Preconditions.checkNotNull(s)) {
            s.println(serviceStackTraceMessage);
        }
    }

    /**
     * @param s
     * @see #printServiceStackTrace()
     * @see #printStackTrace(PrintWriter)
     * @throws NullPointerException s is {@code null}
     */
    public void printServiceStackTrace(PrintWriter s) {
        synchronized (Preconditions.checkNotNull(s)) {
            s.println(serviceStackTraceMessage);
        }
    }
    /** return error message from service */
    @Override
    public String getMessage() {
        return message;
    }
    /** return cause exception class name */
    public String getCauseClass() {
        return causeClass;
    }
    /** return stack trace message from service */
    public String getServiceStackTraceMessage() {
        return serviceStackTraceMessage;
    }
    /** return exception type */
    public int getType() {
        return type;
    }
    /** 
     * return declared field values JSON string of cause <br>
     * user JSON parser deserialize to exception instance<br>
     * Example:
     * <pre>
     *   public Exception causeOf(ServiceRuntimeException exp) throws ClassNotFoundException{
     *       // user fastjson cat JSON string to target exception
     *       if(exp.getCauseFields().isEmpty()){
     *           return null;
     *       }
     *       JSONObject jsonObject = JSON.parseObject(exp.getCauseFields());
     *       // use simple name from getCauseClass()
     *       Class&lt;?&gt; targetClass = Class.forName(exp.getCauseClass().substring(exp.getCauseClass().lastIndexOf(&quot;.&quot;) + 1));
     *       return (Exception)TypeUtils.castToJavaBean(jsonObject, targetClass);
     *   }
     * </pre>
     */
    public String getCauseFields() {
        return causeFields;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append( getClass().getSimpleName());
        builder.append(" [type=");
        builder.append(type);
        builder.append(", message=");
        builder.append(message);
        builder.append(", causeClass=");
        builder.append(causeClass);
        builder.append(", causeFields=");
        builder.append(causeFields);
        builder.append(", serviceStackTraceMessage=");
        builder.append(serviceStackTraceMessage);
        builder.append("]");
        return builder.toString();
    }
}
