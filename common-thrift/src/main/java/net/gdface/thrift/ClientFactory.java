package net.gdface.thrift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.swift.service.ThriftClient;
import com.facebook.swift.service.ThriftClientConfig;
import com.facebook.swift.service.ThriftClientManager;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import static com.google.common.net.HostAndPort.fromParts;
import static com.google.common.net.HostAndPort.fromString;
import static com.google.common.base.Preconditions.*;

import io.airlift.units.Duration;
/**
 * Factory class for creating client instance <br>
 * Example:<br>
 * <pre>
 * // get a FaceApi synchronized instance
 * FaceApi client = ClientFactory.builder()
 * .setHostAndPort("127.0.0.1",9090)
 * .setTimeout(10,TimeUnit.SECONDS)
 * .build(FaceApi.class,FaceApiThriftClient.class);
 * </pre>
 * @author guyadong
 *
 */
public class ClientFactory {

    private static class Singleton{
        private static final ThriftClientManager CLIENT_MANAGER = new ThriftClientManager();    
        static{
        	Runtime.getRuntime().addShutdownHook(new Thread(){
        		@Override
        		public void run() {
        			try {
        				CLIENT_MANAGER.close();
        			} catch (Exception e) {
        				e.printStackTrace();
        			}

        		}});
        }
    }    
    private static final Cache<Class<?>, ThriftClient<?>> THRIFT_CLIENT_CACHE = CacheBuilder.newBuilder().softValues().build();
    private static final Cache<Class<?>, Object> CLIENT_CACHE = CacheBuilder.newBuilder().softValues().build();
    /** 接口类 -- 实例资源池缓存 */
    private static final Cache<Class<?>,GenericObjectPool<?>> INSTANCE_POOL_CACHE = CacheBuilder.newBuilder().softValues().build();
	private ThriftClientManager clientManager; 
    private ThriftClientConfig thriftClientConfig = new ThriftClientConfig();
    private HostAndPort hostAndPort;
    private volatile NiftyClientConnector<? extends NiftyClientChannel> connector;
    private String clientName = ThriftClientManager.DEFAULT_NAME;
    private GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    private Executor executor = MoreExecutors.directExecutor();
    /**
     * 接口实例代理函数对象,
     * 如果提供了该函数对象(不为null),则在创建接口实例时调用该函数，
     * 将thrift/swift创建的接口实例转为代理实例,
     */
    private Function<Object, Object> decorator = null;
    protected ClientFactory() {
    }

    public ClientFactory setManager(ThriftClientManager clientManager){
        this.clientManager = clientManager;
        return this;
    }
    public ClientFactory setThriftClientConfig(ThriftClientConfig thriftClientConfig) {
    	if(thriftClientConfig != null){
    		this.thriftClientConfig = thriftClientConfig;
    	}
        return this;
    }
    /**
     * set all timeout arguments
     * @param time
     * @param unit
     * @return
     * @see #setConnectTimeout(Duration)
     * @see #setReceiveTimeout(Duration)
     * @see #setReadTimeout(Duration)
     * @see #setWriteTimeout(Duration)
     */
    public ClientFactory setTimeout(Duration timeout){
        setConnectTimeout(timeout);
        setReceiveTimeout(timeout);
        setReadTimeout(timeout);
        setWriteTimeout(timeout);
        return this;
    }
    public ClientFactory setTimeout(long time,TimeUnit unit){
        return setTimeout(new Duration(time,unit));
    }
    public ClientFactory setConnectTimeout(Duration connectTimeout) {
        thriftClientConfig.setConnectTimeout(connectTimeout);
        return this;
    }
    public ClientFactory setReceiveTimeout(Duration receiveTimeout) {
        thriftClientConfig.setReceiveTimeout(receiveTimeout);
        return this;
    }
    public ClientFactory setReadTimeout(Duration readTimeout) {
        thriftClientConfig.setReadTimeout(readTimeout);
        return this;
    }
    public ClientFactory setWriteTimeout(Duration writeTimeout) {
        thriftClientConfig.setWriteTimeout(writeTimeout);
        return this;
    }
    public ClientFactory setSocksProxy(HostAndPort socksProxy) {
        thriftClientConfig.setSocksProxy(socksProxy);
        return this;
    }
    public ClientFactory setMaxFrameSize(int maxFrameSize) {
        thriftClientConfig.setMaxFrameSize(maxFrameSize);
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
    public ClientFactory setConnector(NiftyClientConnector<? extends NiftyClientChannel> connector) {
        this.connector = connector;
        return this;
    }
    public ClientFactory setClientName(String clientName) {
        this.clientName = clientName;
        return this;
    }
    /**
     * 设置资源池配置参数
     * @param poolConfig
     * @return
     */
    public synchronized ClientFactory setPoolConfig(GenericObjectPoolConfig poolConfig) {
   		this.poolConfig = checkNotNull(poolConfig,"poolConfig is null");
		return this;
	}

	public synchronized ClientFactory setExecutor(Executor executor) {
		this.executor = checkNotNull(executor,"executor is null");
		return this;
	}

	public Executor getExecutor() {
		return executor;
	}

	@SuppressWarnings("unchecked")
	public <T> ClientFactory setDecorator(Function<T, T> decorator) {
		this.decorator = (Function<Object, Object>) decorator;
		return this;
	}
	public HostAndPort getHostAndPort(){
        return checkNotNull(this.hostAndPort,"hostAndPort is null");
    }
    private NiftyClientConnector<? extends NiftyClientChannel> getConnector(){
        if(null == this.connector){
        	synchronized(this){
        		if(null == this.connector){
        			this.connector = new FramedClientConnector(this.getHostAndPort());
        		}        		
        	}
        }
        return this.connector;
    }
    private ThriftClientManager getClientManager(){
        if(null == this.clientManager){
        	synchronized(this){
        		if(null == this.clientManager){
        			this.clientManager = Singleton.CLIENT_MANAGER;
        		}
        	}
        }
        return this.clientManager;
    }

    @SuppressWarnings("unchecked")
	private <T>ThriftClient<T> getThriftClient(final Class<T> interfaceClass) {
        try {
        		return (ThriftClient<T>) THRIFT_CLIENT_CACHE.get(checkNotNull(interfaceClass,"interfaceClass is null"), new Callable<ThriftClient<?>>(){
					@Override
					public ThriftClient<?> call() throws Exception {
						return new ThriftClient<T>(
		    			        getClientManager(),
		    			        interfaceClass,
		    			        thriftClientConfig,
		    			        clientName);
					}});
		} catch (Exception e) {
        	Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
	@SuppressWarnings("unchecked")
	private <T> GenericObjectPool<T> getObjectPool(final Class<T> interfaceClass){
		try{
			return (GenericObjectPool<T>) INSTANCE_POOL_CACHE.get(checkNotNull(interfaceClass,"interfaceClass is null"), 
					new Callable<GenericObjectPool<T>>(){
				@Override
				public GenericObjectPool<T> call() throws Exception {
					return new GenericObjectPool<T>(new ClientInstanceFactory<T>(interfaceClass),poolConfig);
				}});
		} catch (Exception e) {
        	Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
	}
    /**
	 * 返回{@code instance}对应的资源池对象
	 * @param instance
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> GenericObjectPool<T> getObjectPoolByInstance(T instance){
		checkArgument(null != instance,"intance is null");
		List<GenericObjectPool<?>> found = new ArrayList<GenericObjectPool<?>>(1);
		for(Class<?> clazz : instance.getClass().getInterfaces()){
			GenericObjectPool<?> pool = INSTANCE_POOL_CACHE.getIfPresent(clazz) ;
			if(null !=pool){
				found.add(pool);
			}
		}
		checkState(found.size() ==1,"%s is not valid instance of thrift client",instance.getClass().getName());
		return (GenericObjectPool<T>) found.get(0);
	}

	public <T>void closeObjectPool(Class<T> interfaceClass){
		if(null != interfaceClass){
			GenericObjectPool<?> pool = INSTANCE_POOL_CACHE.getIfPresent(interfaceClass);
			if(null != pool){
				pool.close();
			}
		}
	}

	/**
	 * thrift client 实例不是线程安全的，只可单线程独占使用，所以每次调用实例时要向资源池{@link GenericObjectPool}
     * 申请一个{@code interfaceClass}的实例,用完后调用{@link #releaseInstance(Object)}归还,其他线程才可重复使用。
     * @param interfaceClass 接口类，不可为{@code null}
     * @return
     */
    public <T>T applyInstance(Class<T> interfaceClass) {
        try {
        	return getObjectPool(interfaceClass).borrowObject();
		} catch (Exception e) {
        	Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 释放{@code instance}实例使用权,必须和{@link #applyInstance(Class)}配对使用
     * @param instance 接口实例
     */
    public <T>void releaseInstance(T instance){
    	if(null != instance){
    		getObjectPoolByInstance(instance).returnObject(instance);
    	}
    }
    private class ClientInstanceFactory<T> implements PooledObjectFactory<T>{
    	private final Class<T> interfaceClass;

		private ClientInstanceFactory(Class<T> interfaceClass) {
			checkArgument(null != interfaceClass && interfaceClass.isInterface());
			this.interfaceClass = interfaceClass;
		}
		private NiftyClientChannel getChannel(PooledObject<T>p){
	    	return (NiftyClientChannel) getClientManager().getRequestChannel(p.getObject());
		}
		@Override
		public PooledObject<T> makeObject() throws Exception {
			T obj = getThriftClient(interfaceClass).open(getClientManager().createChannel(getConnector()).get());
			return new DefaultPooledObject<T>(obj);
		}

		@Override
		public void destroyObject(PooledObject<T> p) throws Exception {
	    	getChannel(p).close();
		}

		@Override
		public boolean validateObject(PooledObject<T> p) {
			return getChannel(p).getNettyChannel().isConnected();
		}

		@Override
		public void activateObject(PooledObject<T> p) throws Exception {
			// socket连接长时间空闲会被自动关闭,
			// 为确保borrowObject方法返回的实例有效,在这里要检查对象是否被关闭
			// 否则返回的对象可能因为长时间空闲连接被关闭而在使用时导致连接关闭异常
			if(!validateObject(p)){
				throw new IllegalStateException();
			}
		}

		@Override
		public void passivateObject(PooledObject<T> p) throws Exception {
		}
    	
    }
	public static ClientFactory builder() {
		return new ClientFactory();
	}
    /**
     * 构造{@code interfaceClass}实例<br>
     * thriftyImplClass为null时,假设destClass为thrifty 异步实例类型
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
    public<I,T extends I,O> O  build(final Class<I> interfaceClass,final Class<T> thriftyImplClass,final Class<O> destClass){
        try {
            return (O) CLIENT_CACHE.get(interfaceClass, new Callable<Object>(){
                @Override
                public Object call() throws Exception {
                	if(thriftyImplClass == null){
                		// destClass 为异步模式实例
                        return destClass.getDeclaredConstructor(ClientFactory.class).newInstance(ClientFactory.this); 
                	}
        			T instance =thriftyImplClass.getDeclaredConstructor(ClientFactory.class).newInstance(ClientFactory.this);
                	if(decorator !=null){
                		instance = (T) decorator.apply(instance);
                	}
                    return destClass.getDeclaredConstructor(interfaceClass).newInstance(instance);
                }});
        } catch (ExecutionException e) {
        	Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());
		}
    }
	/**
	 * 构造{@code interfaceClass}实例<br>
	 * {@link #build(Class, Class, Class)}的简化版本，当thriftImplClass只实现了一个接口时，自动推断接口类型
	 * @param thriftyImplClass
	 * @param destClass
	 * @return
	 * @see #build(Class, Class, Class)
	 */
	@SuppressWarnings("unchecked")
	public<I,O,T extends I> O  build(Class<T> thriftyImplClass,Class<O> destClass){
		checkArgument(thriftyImplClass != null);
		checkArgument(thriftyImplClass.getInterfaces().length ==1,
				"can't determines interface class from %s",thriftyImplClass.getName());
		Class<I> interfaceClass = (Class<I>) thriftyImplClass.getInterfaces()[0];
		return build(interfaceClass,thriftyImplClass,destClass);
    }

	/**
	 * 测试thrift服务连接<br>
	 * {@code timeoutMills}>0时设置连接超时参数
	 * @param host 主机名
	 * @param port 端口号
	 * @param timeoutMills 指定连接超时(毫秒),<=0使用默认值
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
	 * 测试当前连接是否有效
	 * @return 连接有效返回{@code true},否则返回{@code false}
	 */
	public boolean testConnect(){
		try {
			NiftyClientChannel channel = getClientManager().createChannel(getConnector()).get();
			channel.close();
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
		builder.append(", clientName=");
		builder.append(clientName);
		builder.append("]");
		return builder.toString();
	}
	/**
	 * 将{@code future} 封装为{@link ListenableFutureDecorator}实例
	 * @param async thrift 异步接口实例
	 * @param future 异步返回结果实例
	 * @return {@link ListenableFutureDecorator}实例
	 */
	@SuppressWarnings("unchecked")
	public <A,V>ListenableFutureDecorator<A,V>wrap(A async,ListenableFuture<V> future){
		if(future instanceof ListenableFutureDecorator){
			return (ListenableFutureDecorator<A,V>)future;
		}
		return new ListenableFutureDecorator<A, V>(async,future);	
	}
    /**
     * {@link ListenableFuture}接口的装饰类，
     * 用于确保异步调用结束时释放异步接口实例,参见{@link ClientFactory#releaseInstance(Object)}
     * @author guyadong
     *
     * @param <A> thrift 异步接口类型
     * @param <V> 方法返回值类型
     */
    public class ListenableFutureDecorator<A,V> implements ListenableFuture<V>{
    	private final A async;
    	private final ListenableFuture<V> future;
    	/** 确保 {@link #releaseAsync()}方法只被调用一次的标志字段 */
    	private final AtomicBoolean released = new AtomicBoolean(false);
		public ListenableFutureDecorator(A async, ListenableFuture<V> future) {
			this.async = checkNotNull(async,"async is null");
			this.future = checkNotNull(future,"future is null");
		}
		private void releaseAsync(){
			if(released.compareAndSet(false, true)){
				releaseInstance(async);	
			}
		}
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return future.cancel(mayInterruptIfRunning);
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			try{
				return future.get();
			}finally{
				releaseAsync();				
			}
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			try{
				return future.get(timeout, unit);
			}finally{
				releaseAsync();
			}
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
    static{
		// JVM 结束时自动清除资源池中所有对象
    	Runtime.getRuntime().addShutdownHook(new Thread(){

    		@Override
    		public void run() {
    			try {
    				for(GenericObjectPool<?> pool:INSTANCE_POOL_CACHE.asMap().values()){
    					pool.close();
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}

    		}			
    	});
    }
}
