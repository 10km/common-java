package net.gdface.thrift;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HostAndPort;
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
                    CLIENT_MANAGER.close();
                }});
        }
    }    
    private static final Cache<Class<?>, ThriftClient<?>> THRIFT_CLIENT_CACHE = CacheBuilder.newBuilder().softValues().build();
    private static final Cache<Class<?>, Object> CLIENT_CACHE = CacheBuilder.newBuilder().softValues().build();
    /** 接口类 -- 实例资源池缓存 */
    private static final Cache<Class<?>,GenericObjectPool<?>> INSTANCE_POOL_CACHE = CacheBuilder.newBuilder().softValues().build();;
	private ThriftClientManager clientManager; 
    private ThriftClientConfig thriftClientConfig = new ThriftClientConfig();
    private HostAndPort hostAndPort;
    private volatile NiftyClientConnector<? extends NiftyClientChannel> connector;
    private String clientName = ThriftClientManager.DEFAULT_NAME;
    private volatile GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    protected ClientFactory() {
    }

    public ClientFactory setManager(ThriftClientManager clientManager){
        this.clientManager = clientManager;
        return this;
    }
    public ClientFactory setThriftClientConfig(ThriftClientConfig thriftClientConfig) {
        this.thriftClientConfig = thriftClientConfig;
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
    public ClientFactory setPoolConfig(GenericObjectPoolConfig poolConfig) {
    	if(null != poolConfig){
    		this.poolConfig = poolConfig;
    	}
		return this;
	}

	private HostAndPort getHostAndPort(){
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
			return getChannel(p).getNettyChannel().isOpen();
		}

		@Override
		public void activateObject(PooledObject<T> p) throws Exception {
		}

		@Override
		public void passivateObject(PooledObject<T> p) throws Exception {
		}
    	
    }
	public static ClientFactory builder() {
		return new ClientFactory();
	}
    /**
     * 构造{@code interfaceClass}实例
     * @param interfaceClass
     * @param destClass
     * @return 返回 {@code destClass }实例
     */
    @SuppressWarnings("unchecked")
    public<I,O> O  build(Class<I> interfaceClass,final Class<O> destClass){
        try {
            return (O) CLIENT_CACHE.get(interfaceClass, new Callable<Object>(){
                @Override
                public Object call() throws Exception {
                    return destClass.getDeclaredConstructor(ClientFactory.class).newInstance(ClientFactory.this);
                }});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
}
