package net.gdface.utils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.*;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
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
            	switch (radix) {
				case HEX:
					return String.format("%02x", input & 0xff);
				default:
	            	return Integer.toString(input & 0xff, radix.value);
				}
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
	 * 连接测试返回状态
	 * @author guyadong
	 *
	 */
	public static enum ConnectStatus{		
		/** 可连接,http响应有效 */CONNECTABLE,
		/** 可连接,http响应无效 */INVALID_RESPONE,
		/** 连接失败 */FAIL
	}
	/**
	 * 测试http连接是否可连接<br>
	 * 连接失败返回{@link ConnectStatus#FAIL},
	 * 建立连接后用
	 * {@code responseValidator}验证响应数据，{@code responseValidator}返回{@code true}则连接有效返回{@link ConnectStatus#CONNECTABLE},
	 * {@code responseValidator}返回{@code false}则连接无效返回{@link ConnectStatus#INVALID_RESPONE} ,
	 * 
	 * @param url 测试的url
	 * @param responseValidator 用于验证响应数据是否有效的验证器,
	 * 为{@code null}时,只要连接成功就返回{@link ConnectStatus#CONNECTABLE}
	 * @return 连接状态{@link ConnectStatus}
	 */
	public static ConnectStatus testHttpConnect(URL url,Predicate<String> responseValidator){
		String reponse = sendHttpRequest(url,"GET");
		responseValidator = MoreObjects.firstNonNull(responseValidator, Predicates.<String>alwaysTrue());
		return reponse == null 
				? ConnectStatus.FAIL : 
					(responseValidator.apply(reponse) 
						? ConnectStatus.CONNECTABLE : ConnectStatus.INVALID_RESPONE);
	}
	/**
	 * 测试http连接是否可连接
	 * @param url 测试的url
	 * @param responseValidator
	 * @return 连接状态{@link ConnectStatus}
	 * @see #testHttpConnect(URL, Predicate)
	 */
	public static ConnectStatus testHttpConnect(String url,Predicate<String> responseValidator){
		try {
			return testHttpConnect(new URL(url),responseValidator);
		} catch (Exception e) {
			return ConnectStatus.FAIL;
		}
	}
	/**
	 * 测试http连接是否可连接
	 * @param url 测试的url
	 * @param responseValidator
	 * @return 连接状态{@link ConnectStatus}
	 * @see #testHttpConnect(URL, Predicate)
	 */
	public static ConnectStatus testHttpConnect(String host,int port,Predicate<String> responseValidator){
		try {
			return testHttpConnect(new URL("http",host,port,""), responseValidator);
		} catch (Exception e) {
			return ConnectStatus.FAIL;
		}
	}
	/**
	 * 测试http连接是否可连接
	 * @param url
	 * @param responseValidator
	 * @return 连接成功{@link ConnectStatus#CONNECTABLE}返回{@code true},
	 * 连接失败{@link ConnectStatus#FAIL}返回{@code false}
	 * 响应无效{@link ConnectStatus#INVALID_RESPONE}抛出异常
	 * @see #testHttpConnect(URL, Predicate)
	 * @throws IllegalStateException 连接响应无效
	 */
	public static boolean testHttpConnectChecked(URL url,Predicate<String> responseValidator){
		ConnectStatus status = testHttpConnect(url,responseValidator);
		checkState(status != ConnectStatus.INVALID_RESPONE,"INVALID INVALID_RESPONE from %s",url);
		return status == ConnectStatus.CONNECTABLE;
	}
	/**
	 * 测试http连接是否可连接
	 * @param url
	 * @param responseValidator
	 * @return
	 * @see #testHttpConnectChecked(URL, Predicate)
	 * @throws IllegalStateException 连接响应无效,连接状态为 {@link ConnectStatus#INVALID_RESPONE}时
	 */
	public static boolean testHttpConnectChecked(String url,Predicate<String> responseValidator){
		try {
			return testHttpConnectChecked(new URL(url),responseValidator);
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * 测试http连接是否可连接
	 * @param host
	 * @param port
	 * @param responseValidator
	 * @return
	 * @see #testHttpConnectChecked(URL, Predicate)
	 * @throws IllegalStateException 连接响应无效,连接状态为 {@link ConnectStatus#INVALID_RESPONE}时
	 */
	public static boolean testHttpConnectChecked(String host,int port,Predicate<String> responseValidator){
		try {
			return testHttpConnectChecked(new URL("http",host,port,""), responseValidator);
		} catch (Exception e) {
			return false;
		}
	}
	/** 
	 * 验证MAC地址有效性
	 * @throws IllegalArgumentException MAC地址无效
	  */
	public static final byte[] validateMac(byte[]mac){
		checkArgument(null != mac && 6 == mac.length ,"INVAILD MAC address");
		return mac;
	}
	/**
	 * 向指定的组播地址和端口发送组播数据
	 * @param group
	 * @param port
	 * @param message
	 * @param ttl 
	 * @throws IOException
	 */
	public static void sendMultiCast(InetAddress group,int port,byte[] message, Integer ttl) throws IOException{
		checkArgument(null != group,"group is null");
		checkArgument(group.isMulticastAddress(),"group %s is not a multicast address",group);
		checkArgument(message != null && message.length > 0,"message is null or empty");

		MulticastSocket ms = new MulticastSocket();
		try {
			if(ttl != null){
				ms.setTimeToLive(ttl);
			}
			ms.send(new DatagramPacket(message, message.length,group,port));
		} finally {
			ms.close();
		}
	}
	/**
	 * 向指定的组播地址和端口发送组播数据
	 * @param bindaddr 组播IP地址
	 * @param port 端口
	 * @param message
	 * @throws IOException
	 */
	public static void sendMultiCast(String bindaddr,int port,byte[] message) throws IOException{
		checkArgument(!Strings.isNullOrEmpty(bindaddr),"bindaddr is null or empty");
		sendMultiCast(InetAddress.getByName(bindaddr),port,message, null);
	}
	/**
	 * 向指定的组播地址和端口发送组播数据
	 * @param hostPort 组播地址和端口号(:号分隔) 如：244.12.12.12:4331,或[244.12.12.12:4331]
	 * @param message
	 * @throws IOException
	 */
	public static void sendMultiCast(String hostPort,byte[] message) throws IOException{
		HostAndPort hostAndPort = HostAndPort.fromString(hostPort);
		sendMultiCast(InetAddress.getByName(hostAndPort.getHost()),hostAndPort.getPort(),message, null);
	}
	/**
	 * 从指定的组播地址和端口接收组播数据
	 * @param group 组播地址
	 * @param port 端口号
	 * @param buffer 数据接收缓冲区
	 * @return 收到的数据长度
	 * @throws IOException
	 */
	public static int recevieMultiCast(InetAddress group,int port,byte[] buffer) throws IOException{
		checkArgument(null != group,"group is null");
		checkArgument(group.isMulticastAddress(),"group %s is not a multicast address",group);
		checkArgument(buffer != null && buffer.length > 0,"message is null or empty");

		MulticastSocket ms = new MulticastSocket(port);
		ms.joinGroup(group);
		try {			
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			ms.receive(packet);
			return packet.getLength();
		} finally {
			ms.leaveGroup(group);
			ms.close();
		}
	}
	/**
	 * 循环接收group,port指定的组播地址发送的数据并交给fun处理
	 * @param group 组播地址
	 * @param port 端口号
	 * @param bufferSize 组播数据最大长度，根据此参数值分配数据接收缓冲区长度
	 * @param processor 数据处理器,返回false,则中止循环
	 * @param onerr 异常处理器,返回false,则中止循环，为{@code null}则使用默认值{@link Predicates#alwaysTrue}
	 * @param stop 中止标记,调用者可通过些标记异步控制循环结束,可为{@code null}
	 * @throws IOException 网络IO异常
	 */
	public static void recevieMultiCastLoop(InetAddress group,int port,int bufferSize,
			Predicate<byte[]>processor,
			Predicate<Throwable> onerr,
			AtomicBoolean stop) throws IOException{
		checkArgument(null != group,"group is null");
		checkArgument(group.isMulticastAddress(),"group %s is not a multicast address",group);
		onerr = MoreObjects.firstNonNull(onerr, Predicates.<Throwable>alwaysTrue());
		if(stop == null){
			stop = new AtomicBoolean(false);
		}
		byte[] message = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(message, message.length);
		MulticastSocket ms = new MulticastSocket(port);
		ms.joinGroup(group);
		try {			
			while(!stop.get()){
				try {
					ms.receive(packet);
					byte[] recevied = new byte[packet.getLength()];
					System.arraycopy(message, 0, recevied, 0, packet.getLength());
					if(!processor.apply(recevied)){
						break;
					}
				} catch (Exception e) {
					if(!onerr.apply(e)){
						break;
					}
				}
			}
		} finally {
			ms.leaveGroup(group);
			ms.close();
		}
	}
	/**
	 * @param bindaddr 组播IP地址
	 * @param port 端口号
	 * @param bufferSize 
	 * @param processor
	 * @param onerr
	 * @param stop
	 * @throws IOException
	 * @see #recevieMultiCastLoop(InetAddress, int, int, Predicate, Predicate, AtomicBoolean)
	 */
	public static void recevieMultiCastLoop(String bindaddr,int port,int bufferSize,
			Predicate<byte[]>processor,
			Predicate<Throwable> onerr,
			AtomicBoolean stop) throws IOException{
		checkArgument(!Strings.isNullOrEmpty(bindaddr),"bindaddr is null or empty");
		recevieMultiCastLoop(InetAddress.getByName(bindaddr),port,bufferSize,processor,onerr,stop);
	}
	/**
	 * @param hostPort 组播地址和端口号(:号分隔) 如：244.12.12.12:4331,或[244.12.12.12:4331]
	 * @param bufferSize
	 * @param processor
	 * @param onerr
	 * @param stop
	 * @throws IOException
	 * @see #recevieMultiCastLoop(InetAddress, int, int, Predicate, Predicate, AtomicBoolean)
	 */
	public static void recevieMultiCastLoop(String hostPort,int bufferSize,
			Predicate<byte[]>processor,
			Predicate<Throwable> onerr,
			AtomicBoolean stop) throws IOException{
		HostAndPort hostAndPort = HostAndPort.fromString(hostPort);
		recevieMultiCastLoop(InetAddress.getByName(hostAndPort.getHost()),hostAndPort.getPort(),bufferSize,processor,onerr,stop);		
	}
}