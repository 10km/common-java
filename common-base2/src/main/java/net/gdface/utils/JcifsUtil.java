package net.gdface.utils;

import java.net.UnknownHostException;

import jcifs.NameServiceClient;
import jcifs.context.SingletonContext;
import jcifs.netbios.NameServiceClientImpl;

/**
 * 基于jcifs库的名字解析工具<br>
 * 使用该类的需要添加额外的依赖库:<br>
 * {@code  'eu.agno3.jcifs:jcifs-ng:2.1.2'}
 * @author guyadong
 *
 */
public class JcifsUtil {
	private final static NameServiceClient nsc = new NameServiceClientImpl(SingletonContext.getInstance());

	/**
	 * 根据{@code host}提供的主机名返回IP地址<br>
	 * 用于局域网内主机名解析为IP地址
	 * @param host
	 * @return host address like "192.168.1.10"
	 * @throws UnknownHostException
	 * @see NameServiceClient#getByName(String)
	 */
	public static String hostAddressOf(String host) throws UnknownHostException{
		return nsc.getByName(host).getHostAddress();
	}

}
