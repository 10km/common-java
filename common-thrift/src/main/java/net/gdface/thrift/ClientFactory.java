// ______________________________________________________
// Generated by sql2java - https://github.com/10km/sql2java-2-6-7 (custom branch) 
// modified by guyadong from
// sql2java original version https://sourceforge.net/projects/sql2java/ 
// JDBC driver used at code generation time: com.mysql.jdbc.Driver
// template: client.factory.vm
// ______________________________________________________
package net.gdface.thrift;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

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
 * Factory class for creating client instance of IFaceLog<br>
 * Example:<br>
 * <pre>
 * // get a IFaceLog synchronized instance
 * IFaceLog client = ClientFactory.builder()
 * .setHostAndPort("127.0.0.1",9090)
 * .setTimeout(10,TimeUnit.SECONDS)
 * .getThriftClient(IFaceLog.class);
 * </pre>
 * @author guyadong
 *
 */
public class ClientFactory {
	private static final Logger logger = Logger.getLogger(ClientFactory.class.getSimpleName());

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
	private ThriftClientManager clientManager; 
    private ThriftClientConfig thriftClientConfig = new ThriftClientConfig();
    private HostAndPort hostAndPort;
    private volatile NiftyClientConnector<? extends NiftyClientChannel> connector;
    private String clientName = ThriftClientManager.DEFAULT_NAME;
    private volatile GenericObjectPoolConfig channelPoolConfig = new GenericObjectPoolConfig();
    private volatile GenericObjectPool<NiftyClientChannel> channelPool;

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
     * 设置channel pool 配置参数
     * @param channelPoolConfig
     * @return
     */
    public ClientFactory setChannelPoolConfig(GenericObjectPoolConfig channelPoolConfig) {
    	if(null != channelPoolConfig){
    		this.channelPoolConfig = channelPoolConfig;
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

    private GenericObjectPool<NiftyClientChannel> getChannelPool() {
    	if(null == channelPool){
    		synchronized(this){
    			if(null == channelPool){
    				channelPool = new GenericObjectPool<NiftyClientChannel>(new ThriftClientPoolFactory(),channelPoolConfig);
    			}
    		}
    	}
		return channelPool;
	}

	@SuppressWarnings("unchecked")
	private <T>ThriftClient<T> getThriftClient(final Class<T> interfaceClass) {
        try {
        		return (ThriftClient<T>) THRIFT_CLIENT_CACHE.get(interfaceClass, new Callable<ThriftClient<?>>(){
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
    /**
     * 返回{@code interfaceClass}的实例
     * @param interfaceClass
     * @return
     */
    public <T>T applyInstance(Class<T> interfaceClass) {
        try {
			return getThriftClient(interfaceClass).open(getChannelPool().borrowObject());
		} catch (Exception e) {
        	Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 释放{@code instance}实例,必须于{@link #applyInstance(Class)}配对使用
     * 向资源池归还{@link NiftyClientChannel}
     * @param instance
     */
    public <T>void releaseInstance(T instance){
    	NiftyClientChannel channel = (NiftyClientChannel) getClientManager().getRequestChannel(instance);
   		getChannelPool().returnObject(channel);
    }
    private class ThriftClientPoolFactory implements PooledObjectFactory<NiftyClientChannel> {

		@Override
		public PooledObject<NiftyClientChannel> makeObject() throws Exception {
			return new DefaultPooledObject<NiftyClientChannel>(getClientManager().createChannel(getConnector()).get());
		}

		@Override
		public void destroyObject(PooledObject<NiftyClientChannel> p) throws Exception {
			logger.info("destroyObject");
			p.getObject().close();
		}

		@Override
		public boolean validateObject(PooledObject<NiftyClientChannel> p) {
			
			return p.getObject().getNettyChannel().isOpen();
		}

		@Override
		public void activateObject(PooledObject<NiftyClientChannel> p) throws Exception {
		}

		@Override
		public void passivateObject(PooledObject<NiftyClientChannel> p) throws Exception {
		}
    }
	public static ClientFactory builder() {
		return new ClientFactory();
	}
}
