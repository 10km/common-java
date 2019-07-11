package net.gdface.utils;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

/**
 * 组播数据接收处理器
 * @author guyadong
 *
 */
public class MultiCastDispatcher implements Runnable{
	public static final int DEFAULT_BUFFER_SIZE = 1024;
	private final InetAddress group;
	private final int port;
	private final Predicate<byte[]> processor;
	private final Predicate<Throwable> onerr;
	private Boolean stopListener = null;
	private  final byte[] message;
	private MulticastSocket multicastSocket;
	/**
	 * 构造方法
	 * @param group 组播地址
	 * @param port 端口号
	 * @param bufferSize 组播数据最大长度，根据此参数值分配数据接收缓冲区长度
	 * @param processor 数据处理器,返回false,则中止循环
	 * @param onerr 异常处理器,返回false,则中止循环，为{@code null}则使用默认值{@link Predicates#alwaysTrue}
	 */
	public MultiCastDispatcher(InetAddress group,int port,int bufferSize,
			Predicate<byte[]>processor,
			Predicate<Throwable> onerr){
		checkArgument(null != group,"group is null");
		checkArgument(group.isMulticastAddress(),"group %s is not a multicast address",group);
		this.group = group;
		this.port = port;
		this.processor = checkNotNull(processor,"processor is null");
		this.onerr = MoreObjects.firstNonNull(onerr, Predicates.<Throwable>alwaysTrue());
		this.message = new byte[bufferSize <= 0 ? DEFAULT_BUFFER_SIZE : bufferSize];
	}
	/**
	 * 构造方法
	 * @param bindaddr 组播IP地址
	 * @param port 端口号
	 * @param bufferSize
	 * @param processor
	 * @param onerr
	 * @throws UnknownHostException
	 * @see {@link #MultiCastDispatcher(InetAddress, int, int, Predicate, Predicate)}
	 */
	public MultiCastDispatcher(String bindaddr,int port,int bufferSize,
			Predicate<byte[]>processor,
			Predicate<Throwable> onerr) throws UnknownHostException{
		this(InetAddress.getByName(checkNotNull(Strings.emptyToNull(bindaddr),"bindaddr is null or empty")),
				port,bufferSize,processor,onerr);
	}
	/**
	 * 构造方法
	 * @param hostPort 组播地址和端口号(:号分隔) 如：244.12.12.12:4331,或[244.12.12.12:4331]
	 * @param bufferSize
	 * @param processor
	 * @param onerr
	 * @throws UnknownHostException
	 * @see {@link #MultiCastDispatcher(InetAddress, int, int, Predicate, Predicate)}
	 */
	public MultiCastDispatcher(String hostPort,int bufferSize,
			Predicate<byte[]>processor,
			Predicate<Throwable> onerr) throws UnknownHostException{
		this(HostAndPort.fromString(hostPort).getHost(),
				HostAndPort.fromString(hostPort).getPort(),
				bufferSize,processor,onerr);
	}
	/**
	 * socket初始化
	 * @return 当前对象
	 * @throws IOException 创建组播对象({@link MulticastSocket})时出错
	 */
	public MultiCastDispatcher init() throws IOException{
		if(null == multicastSocket){
			multicastSocket = new MulticastSocket(port);
			multicastSocket.joinGroup(group);
		}
		return this;
	}
	/**
	 * 循环接收group,port指定的组播地址发送的数据并交给{@link #processor}处理
	 */
	@Override
	public void run() {
		checkNotNull(multicastSocket,"multicastSocket is uninitizlied");
		DatagramPacket packet = new DatagramPacket(message, message.length);
		try {
			stopListener = Boolean.FALSE;
			while(!Boolean.TRUE .equals(stopListener)){
				try {
					multicastSocket.receive(packet);
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
			try {
				multicastSocket.leaveGroup(group);
			} catch (IOException e) {
				e.printStackTrace();
			}
			multicastSocket.close();
			multicastSocket = null;
			stopListener = null;
		}				
	}
	
	public boolean isRunning(){
		return Boolean.FALSE.equals(stopListener);
	}	
	public synchronized void close() {
		stopListener = Boolean.TRUE;
	}
}
