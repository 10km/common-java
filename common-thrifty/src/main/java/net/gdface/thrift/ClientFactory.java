// ______________________________________________________
// Generated by sql2java - https://github.com/10km/sql2java-2-6-7 (custom branch) 
// modified by guyadong from
// sql2java original version https://sourceforge.net/projects/sql2java/ 
// JDBC driver used at code generation time: com.mysql.jdbc.Driver
// template: client.factory.vm
// ______________________________________________________
package net.gdface.thrift;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.microsoft.thrifty.protocol.BinaryProtocol;
import com.microsoft.thrifty.protocol.Protocol;
import com.microsoft.thrifty.service.AsyncClientBase;
import com.microsoft.thrifty.transport.SocketTransport;

import static com.google.common.net.HostAndPort.fromParts;
import static com.google.common.net.HostAndPort.fromString;
import static com.google.common.base.Preconditions.*;

/**
 * Factory class for creating client instance of IFaceLog<br>
 * Example:<br>
 * <pre>
 * // get a asynchronous instance
 * IFaceLogClientAsync client = ClientFactory.builder()
 * .setHostAndPort("127.0.0.1",9090)
 * .setTimeout(10,TimeUnit.SECONDS)
 * .build();
 * </pre>
 * @author guyadong
 *
 */
public class ClientFactory {
    private HostAndPort hostAndPort;
    private long readTimeout;
    private long connectTimeout;
	private Executor executor = MoreExecutors.directExecutor();
    /**
     * 接口实例代理函数对象,
     * 如果提供了该函数对象(不为null),则在创建接口实例时调用该函数，
     * 将thrift/swift创建的接口实例转为代理实例,
     */
    private Function<Object, Object> decorator = null;
    protected ClientFactory() {
    }

    /**
     * set all timeout arguments
     * @param time
     * @param unit
     * @return
     * @see #setConnectTimeout(long time,TimeUnit unit)
     * @see #setReadTimeout(long time,TimeUnit unit)
     */
    public ClientFactory setTimeout(long time,TimeUnit unit){
        setConnectTimeout(time,unit);
        setReadTimeout(time,unit);
        return this;
    }
    public ClientFactory setConnectTimeout(long connectTimeout,TimeUnit unit) {
        this.connectTimeout = unit.toMillis(connectTimeout);
        return this;
    }
    public ClientFactory setReadTimeout(long readTimeout,TimeUnit unit) {
        this.readTimeout = unit.toMillis(readTimeout);
        return this;
    }
    public ClientFactory setHostAndPort(HostAndPort hostAndPort) {
        if(null == this.hostAndPort){
            synchronized(this){
                if(null == this.hostAndPort){
                    this.hostAndPort = checkNotNull(hostAndPort,"hostAndPort must not be null");
                }
            }
        }else{
            throw new IllegalStateException("the memeber hostAndPort be initialized always");
        }
        return this;
    }
    public ClientFactory setHostAndPort(String host,int port) {
        return setHostAndPort(fromParts(host, port));
    }
    public ClientFactory setHostAndPort(String host) {
        return setHostAndPort(fromString(host));
    }
	public synchronized ClientFactory setExecutor(Executor executor) {
		this.executor = checkNotNull(executor,"executor is null");
		return this;
	}

	public Executor getExecutor() {
		return executor;
	}
	public HostAndPort getHostAndPort(){
        return checkNotNull(this.hostAndPort,"hostAndPort is null");
    }
	@SuppressWarnings("unchecked")
	public <T> ClientFactory setDecorator(Function<T, T> decorator) {
		this.decorator = (Function<Object, Object>) decorator;
		return this;
	}
    /**
     * @param stubClass
     * @param closeListener
     * @return instance of {@link net.gdface.facelog.client.thrift.IFaceLogClient}
     */
    public <T> T applyInstance(Class<T> stubClass,AsyncClientBase.Listener closeListener) {
        try {
            SocketTransport transport = 
                    new SocketTransport.Builder(hostAndPort.getHost(),hostAndPort.getPort())
                        .connectTimeout((int) connectTimeout)
                        .readTimeout((int) readTimeout).build();
            transport.connect();
            Protocol protocol = new BinaryProtocol(transport);
            /* force set private field 'strictWrite' to true */
            Field field = BinaryProtocol.class.getDeclaredField("strictWrite");
            field.setAccessible(true);
            field.set(protocol, true);
            return (T)stubClass.getConstructor(Protocol.class,AsyncClientBase.Listener.class).newInstance(protocol,closeListener);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
    public static ClientFactory builder() {
        return new ClientFactory();
    }
    /**
     * 构造{@code interfaceClass}实例<br>
     * thriftyImplClass或interfaceClass为null时,假设destClass为thrifty 异步实例类型
     * @param <I> 接口类
     * @param <O> 返回实例类型
     * @param <T> 基于thrifty实现接口类的实例类型
     * @param interfaceClass
     * @param thriftyImplClass
     * @param destClass 返回的实例类型，如果interfaceClass和thriftyImplClass为null,必须有参数为{@link ClientFactory}的构造函数
     * 否则必须有参数类型为interfaceClass的构造函数
     * @return 返回 {@code destClass }实例
     */
    @SuppressWarnings("unchecked")
	public<I,T extends I,O> O  build(Class<I> interfaceClass, Class<T> thriftyImplClass,Class<O> destClass){
        try {
        	checkNotNull(destClass,"destClass is null");
        	if(interfaceClass == null || thriftyImplClass == null){
        		// destClass 为异步模式实例
                return destClass.getDeclaredConstructor(ClientFactory.class).newInstance(this); 
        	}
			T instance =thriftyImplClass.getDeclaredConstructor(ClientFactory.class).newInstance(this);
        	if(decorator !=null){
        		instance = (T) decorator.apply(instance);
        	}
            return destClass.getDeclaredConstructor(interfaceClass).newInstance(instance);
        } catch (Exception e) {
        	Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
	/**
	 * 构造{@code interfaceClass}实例<br>
	 * {@link #build(Class, Class, Class)}的简化版本，当thriftImplClass只实现了一个接口时，自动推断接口类型
	 * <b>NOTE</b>从1.1.17版本以后，该方法第一个参数为thrift实现的服务接口的类，不再是服务接口类
	 * @param thriftyImplClass
	 * @param destClass
	 * @return
	 * @see #build(Class, Class, Class)
	 */
	@SuppressWarnings("unchecked")
	public<I,O,T extends I> O  build(Class<T> thriftyImplClass,Class<O> destClass){
		checkArgument(thriftyImplClass != null);
		checkArgument(!thriftyImplClass.isInterface(),
				"%s must not be a interface,must a class implemented service interface",thriftyImplClass.getName());
		checkArgument(thriftyImplClass.getInterfaces().length ==1,
				"can't determines interface class from %s",thriftyImplClass.getName());
		Class<I> interfaceClass = (Class<I>) thriftyImplClass.getInterfaces()[0];
		return build(interfaceClass,thriftyImplClass,destClass);
    }
	/**
	 * 创建异步实例 
	 * @param destClass 返回的实例类型，必须有参数为{@link ClientFactory}的构造函数
	 * @return
	 * @see #build(Class, Class, Class)
	 */
	public<O> O  buildAsync(Class<O> destClass){
		return build(null,null,destClass);
    }
	/**
	 * 测试thrifty服务连接<br>
	 * {@code timeoutMills}>0时设置连接超时参数
	 * @param host 主机名
	 * @param port 端口号
	 * @param timeoutMills 指定连接超时(毫秒)
	 * @return 连接成功返回{@code true},否则返回{@code false}
	 */
	public static final boolean testConnect(String host,int port,long timeoutMills){
			ClientFactory clientFactory = ClientFactory.builder().setHostAndPort(host,port);
			if(timeoutMills > 0){
				clientFactory.setTimeout(timeoutMills, TimeUnit.MILLISECONDS);
			}
			return clientFactory.testConnect();
	}
	/**
	 * 测试指定连接是否有效
	 * @return 连接有效返回{@code true},否则返回{@code false}
	 */
	public boolean testConnect(){
        try {
            SocketTransport transport = 
                    new SocketTransport.Builder(hostAndPort.getHost(),hostAndPort.getPort())
                        .connectTimeout((int) connectTimeout)
                        .readTimeout((int) readTimeout).build();
            transport.connect();
            transport.close();
            return true;
        } catch (Exception e) {
        }
        return false;
	}
    public <V> void addCallback(
            final ListenableFuture<V> future,
            final FutureCallback<? super V> callback) {
    	ThriftUtils.addCallback(future, callback, getExecutor());
    }
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ClientFactory [hostAndPort=");
		builder.append(hostAndPort);
		builder.append(", readTimeout=");
		builder.append(readTimeout);
		builder.append(", connectTimeout=");
		builder.append(connectTimeout);
		builder.append(", executor=");
		builder.append(executor);
		builder.append("]");
		return builder.toString();
	}
	public class ListenableFutureDecorator<A,V> implements ListenableFuture<V>{
        private final ListenableFuture<V> future;
        public ListenableFutureDecorator(ListenableFuture<V> future) {
            this.future = checkNotNull(future,"future is null");
        }
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return future.get();
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            future.addListener(listener, executor);            
        }        
    }
}
