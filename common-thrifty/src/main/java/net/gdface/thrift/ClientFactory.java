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
     * 构造{@code interfaceClass}实例
     * @param destClass
     * @return 返回 {@code destClass }实例
     */
    public<O> O  build(final Class<O> destClass){
        try {
            return destClass.getDeclaredConstructor(ClientFactory.class).newInstance(ClientFactory.this);
        } catch (Exception e) {
        	Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
    public <V> void addCallback(
            final ListenableFuture<V> future,
            final FutureCallback<? super V> callback) {
    	ThriftUtils.addCallback(future, callback, getExecutor());
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
