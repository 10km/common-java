package net.gdface.cli;

import net.gdface.cli.CommonCliConstants;

/**
 * 常量定义
 * @author guyadong
 *
 */
public interface ThriftServiceConstants extends CommonCliConstants {
	public static final int DEFAULT_CONNECTION_LIMIT = 32;
	public static final int DEFAULT_IDLE_TIMEOUT = 60;
	public static final String DEFAULT_STATIC_METHOD = "getInstance";
	public static final String SERVICE_PORT_OPTION_LONG = "port";
	public static final String SERVICE_PORT_OPTION_DESC = "service port number,default: ";
	public static final String WORK_THREADS_OPTION_LONG = "threads";
	public static final String WORK_THREADS_OPTION_DESC = "work thread number,default: count of available processors";
	public static final String CONNECTION_LIMIT_OPTION_LONG = "connectionLimit";
	public static final String CONNECTION_LIMIT_OPTION_DESC = "an upper bound on the number of concurrent connections the server will accept.default:" + DEFAULT_CONNECTION_LIMIT;
	public static final String IDLE_CONNECTION_TIMEOUT_OPTION_LONG = "idleConnectionTimeout";
	public static final String IDLE_CONNECTION_TIMEOUT_OPTION_DESC = "Sets a timeout(seconds) period between receiving requests from a client connection. If the timeout is exceeded (no complete requests have arrived from the client within the timeout), the server will disconnect the idle client.default:" + DEFAULT_IDLE_TIMEOUT + " seconds";
	
}
