// ______________________________________________________
// Generated by sql2java - https://github.com/10km/sql2java-2-6-7 (custom branch) 
// modified by guyadong from
// sql2java original version https://sourceforge.net/projects/sql2java/ 
// JDBC driver used at code generation time: com.mysql.jdbc.Driver
// template: service.runtime.exception.java.vm
// ______________________________________________________
package net.gdface.thrift.exception;

import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;

/**
 * Runtime exception wrap class<br>
 * all {@link RuntimeException} threw from service was wrapped to the object<br>
 * @author guyadong
 *
 */
@ThriftStruct
public final class ServiceRuntimeException extends BaseServiceException{
    private static final long serialVersionUID = 1L;
    private int type = 0;

    public ServiceRuntimeException() {
    }
    /**
     * @param cause
     */
    public ServiceRuntimeException(Throwable cause) {
        super(cause);
    }
    /**
     * @param type exception type
     * @param cause
     */
    public ServiceRuntimeException(int type,Throwable cause) {
        super(cause);
        this.type = type;
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