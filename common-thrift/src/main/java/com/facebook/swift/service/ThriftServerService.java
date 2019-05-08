package com.facebook.swift.service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import org.apache.thrift.protocol.TJSONProtocol;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty3.handler.codec.http.cors.Netty3CorsConfig;
import org.jboss.netty3.handler.codec.http.cors.Netty3CorsConfigBuilder;
import org.jboss.netty3.handler.codec.http.cors.Netty3CorsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import static com.google.common.base.Preconditions.*;
import static org.jboss.netty.handler.codec.http.HttpMethod.*;

import com.facebook.nifty.codec.ThriftFrameCodecFactory;
import com.facebook.nifty.core.NettyServerTransport;
import com.facebook.nifty.core.NiftyTimer;
import com.facebook.nifty.duplex.TDuplexProtocolFactory;
import com.facebook.swift.codec.ThriftCodecManager;
import com.facebook.swift.service.ThriftEventHandler;
import com.facebook.swift.service.ThriftServer;
import com.facebook.swift.service.ThriftServerConfig;
import com.facebook.swift.service.ThriftService;
import com.facebook.swift.service.ThriftServiceProcessor;
import com.facebook.swift.service.metadata.ThriftServiceMetadata;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.MoreExecutors;

import net.gdface.utils.ReflectionUtils;

/**
 * 创建thrift服务实例{@link ThriftServer},封装为{@link com.google.common.util.concurrent.Service}
 * @author guyadong
 *
 */
public class ThriftServerService extends AbstractIdleService{
    private static final Logger logger = LoggerFactory.getLogger(ThriftServerService.class);
    public static final String HTTP_TRANSPORT = "http";
    public static final String JSON_PROTOCOL = "json";
    /**
     * 在{@link ThriftServer#DEFAULT_PROTOCOL_FACTORIES}基础上增加'json'支持
     */
    public static final ImmutableMap<String,TDuplexProtocolFactory> DEFAULT_PROTOCOL_FACTORIES = 
    		ImmutableMap.<String, TDuplexProtocolFactory>builder()
	    		.putAll(ThriftServer.DEFAULT_PROTOCOL_FACTORIES)
	    		.put(JSON_PROTOCOL, TDuplexProtocolFactory.fromSingleFactory(new TJSONProtocol.Factory()))
	    		.build();
    /**
     * 在{@link ThriftServer#DEFAULT_FRAME_CODEC_FACTORIES}基础上增加'http'支持
     */
    public static final ImmutableMap<String,ThriftFrameCodecFactory> DEFAULT_FRAME_CODEC_FACTORIES = 
    		ImmutableMap.<String, ThriftFrameCodecFactory>builder()
	    		.putAll(ThriftServer.DEFAULT_FRAME_CODEC_FACTORIES)
	    		.put(HTTP_TRANSPORT, (ThriftFrameCodecFactory) new ThriftHttpCodecFactory())
	    		.build();

	public static class Builder {
		private List<?> services = ImmutableList.of();
		private ThriftServerConfig thriftServerConfig= new ThriftServerConfig();
		private List<ThriftEventHandler> eventHandlers = ImmutableList.of();
		private Builder() {
		}

		public Builder withServices(Object... services) {
			return withServices(ImmutableList.copyOf(services));
		}

		public Builder withServices(List<?> services) {
			this.services = checkNotNull(services);
			return this;
		}
		public Builder setEventHandlers(List<ThriftEventHandler> eventHandlers){
			this.eventHandlers = checkNotNull(eventHandlers);
			return this;
		}
		public Builder setEventHandlers(ThriftEventHandler...eventHandlers){
			return setEventHandlers(ImmutableList.copyOf(eventHandlers));
		}
		/**
		 * 设置服务端口
		 * @param servicePort
		 * @return
		 * @see ThriftServerConfig#setPort(int)
		 */
		public Builder setServerPort(int servicePort) {
			this.thriftServerConfig.setPort(servicePort);
			return this;
		}

		/**
		 * 设置服务器配置参数对象
		 * @param thriftServerConfig
		 * @return
		 */
		public Builder setThriftServerConfig(ThriftServerConfig thriftServerConfig) {
			this.thriftServerConfig = checkNotNull(thriftServerConfig,"thriftServerConfig is null");
			return this;
		}

		/**
		 * 根据参数构造 {@link ThriftServerService}实例
		 * @return
		 */
		public ThriftServerService build() {
			return new ThriftServerService(services, eventHandlers, thriftServerConfig);
		}
		/**
		 * 根据参数构造 {@link ThriftServerService}子类实例
		 * @param subServiceClass
		 * @return
		 */
		public <T extends ThriftServerService> T build(Class<T> subServiceClass) {
			try {
				Constructor<T> constructor= checkNotNull(subServiceClass,"subServiceClass is null")
						.getDeclaredConstructor(List.class,List.class,ThriftServerConfig.class);
				return constructor.newInstance(services,eventHandlers,thriftServerConfig);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new RuntimeException(e);
			}
		}
	}

	public static final Builder bulider() {
		return new Builder();
	}

	protected final ThriftServer thriftServer;
	protected final ThriftServiceProcessor processor;
	protected final ThriftServerConfig thriftServerConfig;
	protected final String serviceName;
	/**
	 * 构造函数<br>
	 * @param services 服务对象列表
	 * @param eventHandlers 事件侦听器列表
	 * @param thriftServerConfig 服务配置对象
	 * @see ThriftServiceProcessor#ThriftServiceProcessor(ThriftCodecManager, List, List)
	 * @see ThriftServer#ThriftServer(com.facebook.nifty.processor.NiftyProcessor, ThriftServerConfig)
	 */
	public ThriftServerService(final List<?> services, 
			List<ThriftEventHandler> eventHandlers, 
			ThriftServerConfig thriftServerConfig) {
		checkArgument(null != services && !services.isEmpty());
		this.thriftServerConfig = checkNotNull(thriftServerConfig,"thriftServerConfig is null");
		int port = this.thriftServerConfig.getPort();
		checkArgument(port > 0 && port < 65535,  "INVALID service port %d", port);

		this.processor = new ThriftServiceProcessorCustom(
				new ThriftCodecManager(), 
				checkNotNull(eventHandlers,"eventHandlers is null"),
				services);
		this.thriftServer =  new ThriftServer(processor,
				thriftServerConfig,
				new NiftyTimer("thrift"),
				DEFAULT_FRAME_CODEC_FACTORIES, DEFAULT_PROTOCOL_FACTORIES, 
				ThriftServer.DEFAULT_WORKER_EXECUTORS, 
				ThriftServer.DEFAULT_SECURITY_FACTORY);
		addCorsHandlerIfHttp();

		String serviceList = Joiner.on(",").join(Lists.transform(services, new Function<Object,String>(){
			@Override
			public String apply(Object input) {
				return getServiceName(input);
			}}));
		this.serviceName = String.format("%s(T:%s,P:%s)", 
				serviceList,thriftServerConfig.getTransportName(),
				thriftServerConfig.getProtocolName());
		// Arrange to stop the server at shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutDown();
			}
		});
		addListener(new Listener(){
			@Override
			public void starting() {
				logThriftServerConfig(ThriftServerService.this.thriftServerConfig);
			}			
		}, MoreExecutors.directExecutor());
	}


	/**
	 * 添加CORS Handler和XHR编解码器
	 */
	protected void addCorsHandlerIfHttp(){
		if(HTTP_TRANSPORT.equals(thriftServerConfig.getTransportName())){
			try {
				// 反射获取私有的成员NettyServerTransport
				final NettyServerTransport nettyServerTransport = ReflectionUtils.valueOfField(thriftServer, "transport");
				// 反射获取私有的成员ChannelPipelineFactory
				Field pipelineFactory = NettyServerTransport.class.getDeclaredField("pipelineFactory");
				{
					Field modifiersField = Field.class.getDeclaredField("modifiers");
					modifiersField.setAccessible(true); //Field 的 modifiers 是私有的
					modifiersField.setInt(pipelineFactory, pipelineFactory.getModifiers() & ~Modifier.FINAL);
				}
				pipelineFactory.setAccessible(true);
				final ChannelPipelineFactory channelPipelineFactory = (ChannelPipelineFactory) pipelineFactory.get(nettyServerTransport);
				final Netty3CorsConfig corsConfig = Netty3CorsConfigBuilder.forAnyOrigin()
					.allowedRequestMethods(POST,GET,OPTIONS)
					.allowedRequestHeaders("Origin","Content-Type","Accept","application","x-requested-with")
					.build();
				ChannelPipelineFactory factoryWithCORS = new ChannelPipelineFactory(){

					@Override
					public ChannelPipeline getPipeline() throws Exception {
						// 修改 ChannelPipeline,在frameCodec后(顺序)增加CORS handler,XHR编解码器
						ChannelPipeline cp = channelPipelineFactory.getPipeline();
//						cp.remove("idleTimeoutHandler");
//						cp.remove("idleDisconnectHandler");
//						final ThriftServerDef def = ReflectionUtils.valueOfField(nettyServerTransport, "def");
//						final NettyServerConfig nettyServerConfig = ReflectionUtils.valueOfField(nettyServerTransport, "nettyServerConfig");
//		                if (def.getClientIdleTimeout() != null) {
//		                    // Add handlers to detect idle client connections and disconnect them
//		                    cp.addBefore("authHandler","idleTimeoutHandler", 
//		                    		new IdleStateHandler(nettyServerConfig.getTimer(),
//		                                                                          0,
//		                                                                          0,
//		                                                                          0,
//		                                                                          TimeUnit.MILLISECONDS));
//		                    cp.addBefore("authHandler","idleDisconnectHandler", new IdleDisconnectHandler());
//		                }
						cp.addAfter("frameCodec", "thriftServerXHRCodec", new ThriftServerXHRCodec());
						cp.addAfter("frameCodec", "cors", new Netty3CorsHandler(corsConfig));
						return cp;
					}};
				// 修改nettyServerTransport的私有常量pipelineFactory
				pipelineFactory.set(nettyServerTransport, factoryWithCORS);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new RuntimeException(e);
			}
		}
	}
	/** 
	 * 返回注释{@link ThriftService}定义的服务名称
	 * @see  {@link ThriftServiceMetadata#getThriftServiceAnnotation(Class)}
	 */
	private static final String getServiceName(Class<?> serviceClass){
		ThriftService thriftService = ThriftServiceMetadata.getThriftServiceAnnotation(
				checkNotNull(serviceClass,"serviceClass is null"));
		return Strings.isNullOrEmpty(thriftService.value())
				? serviceClass.getSimpleName()
				: thriftService.value();
	}
	/** @see #getServiceName(Class) */
	private static final String getServiceName(Object serviceInstance){
		return getServiceName(serviceInstance.getClass());
	}
	@Override
	protected String serviceName() {
		return this.serviceName;
	}

	@Override
	protected final void startUp() throws Exception {
		thriftServer.start();
		logger.info("{} service is running(服务启动)",serviceName());
	}
	@Override
	protected final void shutDown() {
		logger.info(" {} service shutdown(服务关闭) ",	serviceName());
		thriftServer.close();
	}
	/** log 输出{@code config}中的关键参数 */
	public static final void logThriftServerConfig(ThriftServerConfig config){
		logger.info("RPC Service Parameters(服务运行参数):");
		logger.info("port: {}", config.getPort());
		logger.info("connectionLimit: {}", config.getConnectionLimit());
		logger.info("workerThreads: {}", config.getWorkerThreads());
		logger.info("idleConnectionTimeout: {}", config.getIdleConnectionTimeout());
	}


	/**
	 * @return thriftServerConfig
	 */
	public ThriftServerConfig getThriftServerConfig() {
		return thriftServerConfig;
	}
}
