package net.gdface.thrift.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import com.google.common.base.Preconditions;

/**
 * Runtime exception wrap class<br>
 * all {@link RuntimeException} threw from service was wrapped to the object<br>
 * @author guyadong
 *
 */
@ThriftStruct
public final class ServiceRuntimeException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    private int type = 0;
    private String message;
    private String causeClass;
    /** Lazy Initialization field */
    private volatile String serviceStackTraceMessage;
    private String causeFields;

    public ServiceRuntimeException(String message) {
		this(message,null);
	}
	public ServiceRuntimeException() {
		this(null,null);	
	}
	public ServiceRuntimeException(Throwable cause) {
        this(null,cause);
    }
    public ServiceRuntimeException(String message, Throwable cause) {
        super(message,stripRuntimeShell(cause));
        if(null != message){
            this.message = message;
        }else if(null != getCause()){
            this.message = getCause().getMessage();
            if(null == this.message){
                this.message = getCause().toString();
            }
        }
        this.causeClass = null == getCause()? null : getCause().getClass().getName();
        this.causeFields = null == getCause()? null : getCause().toString();
        if(getCause() instanceof ServiceRuntimeException){
            this.causeFields = ((ServiceRuntimeException)getCause()).jsonOfDeclaredFields();    
        }
    }
    /**
     * @param type exception type
     * @param cause
     */
    public ServiceRuntimeException(int type,Throwable cause) {
        this(cause);
        this.type = type;
    }
    /** return a JSON string of declared fields,subclass override it */
    protected String jsonOfDeclaredFields(){
        return "";
    }
    /**
     * return cause wrapped by {@link RuntimeException}<br>
     * @param e
     * @return
     */
    private static final Throwable stripRuntimeShell(Throwable e){
        if(null != e && null !=e.getCause() && e.getClass() == RuntimeException.class){
            return stripRuntimeShell(e.getCause());
        }
        return e;
    }
    /**
     * save error message to {@link #serviceStackTraceMessage} by calling {@link #printStackTrace(PrintWriter)} 
     * @param cause
     * @see #printStackTrace(PrintWriter)
     */
    private void fillStackTraceMessage(Throwable cause) {
        if (null != cause) {
            StringWriter write = new StringWriter(256);
            PrintWriter pw = new PrintWriter(write);
            cause.printStackTrace(pw);
            serviceStackTraceMessage = write.toString();
        }
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
    @ThriftField(1)
    public String getMessage() {
        return message;
    }
    @ThriftField
    public void setMessage(String message) {
        this.message = message;
    }
    /** return cause exception class name */
    @ThriftField(2)
    public String getCauseClass() {
        return causeClass;
    }
    @ThriftField
    public void setCauseClass(String causeClass) {
        this.causeClass = causeClass;
    }
    /** return stack trace message from service */
    @ThriftField(3)
    public String getServiceStackTraceMessage() {
        // Double-checked locking
        if(null == serviceStackTraceMessage){
            synchronized(this){
                if(null == serviceStackTraceMessage){
                    fillStackTraceMessage(null == getCause()? this : getCause());
                }
            }
        }
        return serviceStackTraceMessage;
    }
    @ThriftField
    public void setServiceStackTraceMessage(String serviceStackTraceMessage) {
        this.serviceStackTraceMessage = serviceStackTraceMessage;
    }
    /** 
     * return JSON string of declared field values if cause is subclass of this class 
     * and override {@code jsonOfDeclaredFields} method, otherwise return empty string
     * @see #jsonOfDeclaredFields()
     */
    @ThriftField(4)
    public String getCauseFields() {
        return causeFields;
    }
    @ThriftField
    public void setCauseFields(String causeFields) {
        this.causeFields = causeFields;
    }
    /** return exception type */
    @ThriftField(5)
    public int getType() {
        return type;
    }
    @ThriftField
    public void setType(int type) {
        this.type = type;
    }
}