package net.gdface.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 服务基本配置参数
 * @author guyadong
 *
 */
public abstract class ThriftServiceConfig extends BaseAppConfig implements ThriftServiceConstants {

	/**
	 * 服务端口号
	 */
	private int servicePort;
	private int workThreads;
	private int connectionLimit;
	private int idleConnectionTimeout;
	/**
	 *  默认服务端口号
	 * @param defaultPort
	 */
	public ThriftServiceConfig(int defaultPort) {
		if(defaultPort<=0){
			throw new IllegalArgumentException(String.format("invalid defaultPort %d",defaultPort));
		}
		options.addOption(Option.builder().longOpt(SERVICE_PORT_OPTION_LONG)
				.desc(SERVICE_PORT_OPTION_DESC + defaultPort).numberOfArgs(1).type(Number.class).build());
		options.addOption(Option.builder().longOpt(WORK_THREADS_OPTION_LONG)
				.desc(WORK_THREADS_OPTION_DESC).numberOfArgs(1).type(Number.class).build());
		options.addOption(Option.builder().longOpt(CONNECTION_LIMIT_OPTION_LONG)
				.desc(CONNECTION_LIMIT_OPTION_DESC).numberOfArgs(1).type(Number.class).build());
		options.addOption(Option.builder().longOpt(IDLE_CONNECTION_TIMEOUT_OPTION_LONG)
				.desc(IDLE_CONNECTION_TIMEOUT_OPTION_DESC).numberOfArgs(1).type(Number.class).build());

		defaultValue.setProperty(SERVICE_PORT_OPTION_LONG, defaultPort);
		defaultValue.setProperty(WORK_THREADS_OPTION_LONG, Runtime.getRuntime().availableProcessors());
		defaultValue.setProperty(CONNECTION_LIMIT_OPTION_LONG, DEFAULT_CONNECTION_LIMIT);
		defaultValue.setProperty(IDLE_CONNECTION_TIMEOUT_OPTION_LONG, DEFAULT_IDLE_TIMEOUT);
	}
	@Override
	public void loadConfig(Options options, CommandLine cmd) throws ParseException {
		super.loadConfig(options, cmd);
		this.servicePort = ((Number)getProperty(SERVICE_PORT_OPTION_LONG)).intValue(); 
		this.workThreads = ((Number)getProperty(WORK_THREADS_OPTION_LONG)).intValue(); 
		this.connectionLimit = ((Number)getProperty(CONNECTION_LIMIT_OPTION_LONG)).intValue(); 		
		this.idleConnectionTimeout = ((Number)getProperty(IDLE_CONNECTION_TIMEOUT_OPTION_LONG)).intValue();
	}
	/**
	 * @return 服务端口号
	 */
	public int getServicePort() {
		return servicePort;
	}

	/**
	 * @return 工作线程数量
	 */
	public int getWorkThreads() {
		return workThreads;
	}

	/**
	 * @return 连接上限
	 */
	public int getConnectionLimit() {
		return connectionLimit;
	}

	/**
	 * 
	 * @return 空间连接超时(秒)
	 */
	public int getIdleConnectionTimeout() {
		return idleConnectionTimeout;
	}
	
}
