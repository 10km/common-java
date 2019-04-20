package net.gdface.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import com.google.common.base.Predicates;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;

import com.google.common.base.Function;

/**
 * 网络管理工具类
 * @author guyadong
 * @since 1.0.7
 *
 */
public class NetworkUtil {
    public static final String DEFAULT_HOST = "localhost";
    public static enum Radix{
        /** 二进制 */BIN(2),
        /** 十进制 */DEC(10),
        /** 十六进制 */HEX(16);
        public final int value;
        Radix(int radix){
            this.value = radix;
        }
    }
    public static enum Filter implements Predicate<NetworkInterface>{
        /** 过滤器: 所有网卡 */ALL,
        /** 过滤器: 在线设备,see also {@link NetworkInterface#isUp()} */UP,
        /** 过滤器: 虚拟接口,see also {@link NetworkInterface#isVirtual()} */VIRTUAL,
        /** 过滤器:LOOPBACK, see also {@link NetworkInterface#isLoopback()} */LOOPBACK,
        /** 过滤器:物理网卡 */PHYICAL_ONLY;

        @Override
        public boolean apply(NetworkInterface input) {
            if(null == input ){
                return false;
            }
            try{
                switch(this){
                case UP:
                    return input.isUp();
                case VIRTUAL:
                    return input.isVirtual();
                case LOOPBACK:
                    return input.isLoopback();
                case PHYICAL_ONLY :{
                    byte[] hardwareAddress = input.getHardwareAddress();
                    return null != hardwareAddress 
                            && hardwareAddress.length > 0 
                            && !input.isVirtual() 
                            && !isVMMac(hardwareAddress);
                }
                case ALL:
                default :
                    return true;
                }
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
    }
    /**
     * 根据过滤器{@code filters}指定的条件(AND)返回网卡设备对象
     * @param filters
     * @return
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static Set<NetworkInterface> getNICs(Predicate<NetworkInterface> ...filters) {
        if(null == filters){
            filters = new Predicate[]{Filter.ALL};
        }
        try {
            Iterator<NetworkInterface> filtered = Iterators.filter(
                    Iterators.forEnumeration(NetworkInterface.getNetworkInterfaces()),
                    Predicates.and(filters));
            return ImmutableSet.copyOf(filtered);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } 
    }
    /**
     * 返回所有物理网卡
     * @return
     */
    public static Set<NetworkInterface> getPhysicalNICs() {
        return getNICs(Filter.PHYICAL_ONLY,Filter.UP);
    }
    /**
     * 将{@code byte[]} 转换为{@code radix}指定格式的字符串
     * 
     * @param source 
     * @param separator 分隔符
     * @param radix 进制基数
     * @return {@code source}为{@code null}时返回空字符串
     */
    public static final String format(byte[] source,String separator, final Radix radix) {
        if (null == source){
            return "";
        }
        if(null == separator){
            separator = "";
        }
        List<String> hex = Lists.transform(Bytes.asList(source),new Function<Byte,String>(){
            @Override
            public String apply(Byte input) {
            	return Integer.toString(input & 0xff, radix.value);
            }});
        return Joiner.on(separator).join(hex);
    }
    /** 
     * MAC地址格式(16进制)格式化{@code source}指定的字节数组 
     * @see #format(byte[], String, Radix)
     */
    public static final String formatMac(byte[] source,String separator) {
        return format(source,separator,Radix.HEX);
    }
    /** 
     * 以IP地址格式(点分位)格式化{@code source}指定的字节数组<br>
     * @see #format(byte[], String, Radix) 
     */
    public static final String formatIp(byte[] source) {
        return format(source,".",Radix.DEC);
    }
    /**
     * 返回指定{@code address}绑定的网卡的物理地址(MAC)
     * @param address
     * @return 指定的{@code address}没有绑定在任何网卡上返回{@code null}
     * @see NetworkInterface#getByInetAddress(InetAddress)
     * @see NetworkInterface#getHardwareAddress()
     */
    public static byte[] getMacAddress(InetAddress address) {
        try {
            NetworkInterface nic = NetworkInterface.getByInetAddress(address);
            return null == nic ? null  : nic.getHardwareAddress();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param nic 网卡对象
     * @param separator 格式化分隔符
     * @return 表示MAC地址的字符串
     */
    public static String getMacAddress(NetworkInterface nic,String separator) {
        try {
            return format(nic.getHardwareAddress(),separator, Radix.HEX);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 参见 {@link #getMacAddress(InetAddress)}
     * @param address
     * @param separator 格式化分隔符
     * @return 表示MAC地址的字符串
     */
    public static String getMacAddress(InetAddress address,String separator) {
        return format(getMacAddress(address),separator, Radix.HEX);        
    }
    private static byte invalidMacs[][] = {
            /** VMWare */{0x00, 0x05, 0x69},             
            /** VMWare */{0x00, 0x1C, 0x14},             
            /** VMWare */{0x00, 0x0C, 0x29},             
            /** VMWare */{0x00, 0x50, 0x56},             
            /** Virtualbox */{0x08, 0x00, 0x27},         
            /** Virtualbox */{0x0A, 0x00, 0x27},         
            /** Virtual-PC */{0x00, 0x03, (byte)0xFF},   
            /** Hyper-V */{0x00, 0x15, 0x5D}             
    };
    private static boolean isVMMac(byte[] mac) {
        if(null == mac) {
            return false;
        }
        
        for (byte[] invalid: invalidMacs){
            if (invalid[0] == mac[0] && invalid[1] == mac[1] && invalid[2] == mac[2]) {
                return true;
            }
        }
        return false;
    }
    /** 判断{@code host}是否为localhost */
    public static final boolean isLoopbackAddress(String host) {
        return "127.0.0.1".equals(host) 
                || "::1".equals(host) 
                || DEFAULT_HOST.equals(host);
    }
    /** 判断{@code address}是否为本机地址 */
    public static final boolean isLocalhost(InetAddress address) {
        try {
            return address.isLoopbackAddress() 
                    || InetAddress.getLocalHost().getHostAddress().equals( address.getHostAddress()) ;
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    /** 判断{@code address}是否为本机地址 */
    public static final boolean isLocalhost(String host) {
        try {
            return isLoopbackAddress(host) || isLocalhost(InetAddress.getByName(checkNotNull(host)));
        } catch (UnknownHostException e) {
            return false;
        }
    }
    /** 如果{@code host}为localhost转换为{@value #DEFAULT_HOST} */
    public  static final String convertHost(String host) {
        return isLoopbackAddress(host)? DEFAULT_HOST : host;
    }
    
    /** 遍历所有物理网上绑定的地址,判断{@code address}是否为本机网卡绑定的地址 */
    public static boolean selfBind(final InetAddress address){
        if(isLocalhost(address)){
            return true;
        }
        final Predicate<InetAddress> filter = new Predicate<InetAddress>(){
            @Override
            public boolean apply(InetAddress input) {
                return input.getHostAddress().equals(address.getHostAddress());
        }};
        return Iterators.tryFind(getPhysicalNICs().iterator(),new Predicate<NetworkInterface>(){
            @Override
            public boolean apply(NetworkInterface input) {
                return Iterators.tryFind(
                        Iterators.forEnumeration(input.getInetAddresses()), 
                        filter).isPresent();
            }}).isPresent();
    }
    /** see also {@link #selfBind(InetAddress)} */
    public static boolean selfBind(String host){
        try {
            return selfBind(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            return false;
        }
    }        
	/**
	 * 获取访问指定host的当前网卡物理地址
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException
	 */
	public static byte[] getCurrentMac(String host,int port) throws IOException {
		Socket socket = null;
		try {
			socket = new Socket(host,port);
			InetAddress address = socket.getLocalAddress();
			NetworkInterface nic = NetworkInterface.getByInetAddress(address);
			return nic.getHardwareAddress();
		} finally{
			if(socket != null){
				socket.close();
			}
		}
	}
	/**
	 * 向指定的url发送http请求
	 * @param url
	 * @param requestType 请求类型，see {@link HttpURLConnection#setRequestMethod(String)}
	 * @return 返回响应数据，请求失败返回{@code null}
	 */
	public static String sendHttpRequest(URL url,String requestType) {
	
	    HttpURLConnection con = null;  
	
	    BufferedReader buffer = null; 
	    StringBuffer resultBuffer = null;  
	
	    try {
	        //得到连接对象
	        con = (HttpURLConnection) url.openConnection(); 
	        //设置请求类型
	        con.setRequestMethod(requestType);  
	        //设置请求需要返回的数据类型和字符集类型
	        con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");  
	        //允许写出
	        con.setDoOutput(true);
	        //允许读入
	        con.setDoInput(true);
	        //不使用缓存
	        con.setUseCaches(false);
	        //得到响应码
	        int responseCode = con.getResponseCode();
	
	        if(responseCode == HttpURLConnection.HTTP_OK){
	            //得到响应流
	            InputStream inputStream = con.getInputStream();
	            //将响应流转换成字符串
	            resultBuffer = new StringBuffer();
	            String line;
	            buffer = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
	            while ((line = buffer.readLine()) != null) {
	                resultBuffer.append(line);
	            }
	            return resultBuffer.toString();
	        }
	
	    }catch(Exception e) {
	    }finally {
			if (con != null){
				con.disconnect();
			}
		}
	    return null;
	}
	/**
	 * 测试http连接是否可连接
	 * @param url 测试的url
	 * @param responseValidator 用于验证响应数据是否有效的验证器，
	 * 有效返回{@code true},否则返回{@code false},为{@code null}时,只要连接成功就返回{@code true}
	 * @return 连接成功返回{@code true}否则返回{@code false}
	 */
	public static boolean testHttpConnect(URL url,Predicate<String> responseValidator){
		String reponse = sendHttpRequest(url,"GET");
		responseValidator = MoreObjects.firstNonNull(responseValidator, Predicates.<String>alwaysTrue());
		return reponse == null ? false : responseValidator.apply(reponse);
	}
	/**
	 * 测试http连接是否可连接
	 * @param url 测试的url
	 * @param responseValidator
	 * @return 连接成功返回{@code true}否则返回{@code false}
	 * @see #testHttpConnect(URL, Predicate)
	 */
	public static boolean testHttpConnect(String url,Predicate<String> responseValidator){
		try {
			return testHttpConnect(new URL(url),responseValidator);
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 测试http连接是否可连接
	 * @param url 测试的url
	 * @param responseValidator
	 * @return 连接成功返回{@code true}否则返回{@code false}
	 * @see #testHttpConnect(URL, Predicate)
	 */
	public static boolean testHttpConnect(String host,int port,Predicate<String> responseValidator){
		try {
			return testHttpConnect(new URL("http",host,port,""), responseValidator);
		} catch (Exception e) {
			return false;
		}
	}
}