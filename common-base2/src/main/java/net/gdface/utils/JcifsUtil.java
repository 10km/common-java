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
	 * @throws UnknownHostException 无法解析主机名
	 * @see NameServiceClient#getByName(String)
	 */
	public static String hostAddressOf(String host) throws UnknownHostException{
		return nsc.getByName(host).getHostAddress();
	}
	/**
	 * 根据{@code host}提供的主机名返回IP地址<br>
	 * 用于局域网内主机名解析为IP地址，解析失败返回{@code null}
	 * @param host address like "192.168.1.10"
	 * @return 
	 */
	public static String getAddressIfPossible(String host){
		try {
			return nsc.getByName(host).getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}
	/**
	 * 判断{@code host}是否为可解析的主机名
	 * @param host
	 * @return
	 */
	public static boolean isResolvableHost(String host){
		return getAddressIfPossible(host) != null;
	}
}
