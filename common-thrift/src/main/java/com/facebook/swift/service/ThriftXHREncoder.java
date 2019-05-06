package com.facebook.swift.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import com.facebook.nifty.core.ThriftMessage;
import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * XHR(XML Http Request)编码器<br>
 * 将{@link com.facebook.nifty.core.NiftyDispatcher}输出的
 * {@link ThriftMessage}响应数据转为{@link DownstreamMessageEvent},
 * 
 * @author guyadong
 *
 */
public class ThriftXHREncoder extends SimpleChannelDownstreamHandler {
	private static final Supplier<HttpRequest> DEFAULT_SUPPLIER = new Supplier<HttpRequest>() {

		@Override
		public HttpRequest get() {
			// 如果没有获取 HttpRequest 则使用用HTTP 1.1为Response的版本号
			return new DefaultHttpRequest(HTTP_1_1,HttpMethod.GET, "/");
		}
	};
	private final Supplier<HttpRequest> currentRequest;
	/**
	 * @param currentRequest 返回当前HTTP请求的{@link Supplier}实例,为{@code null}则使用默认实例{@link #DEFAULT_SUPPLIER}
	 */
	public ThriftXHREncoder(Supplier<HttpRequest> currentRequest) {
		super();
		this.currentRequest = MoreObjects.firstNonNull(currentRequest,DEFAULT_SUPPLIER);
	}
	public ThriftXHREncoder(){
		this(null);
	}
	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if(e.getMessage() instanceof ThriftMessage){
			ThriftMessage thriftMessage = (ThriftMessage)e.getMessage();
			if(thriftMessage.getBuffer().readable()){
				switch (thriftMessage.getTransportType()) {
				case UNFRAMED:
					HttpRequest request = MoreObjects.firstNonNull(currentRequest.get(),DEFAULT_SUPPLIER.get());
                    DefaultHttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), OK);
                    // 将ThriftMessage中的数据装入HttpResponse
                    response.setContent(thriftMessage.getBuffer());
					ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), 
							Channels.future(ctx.getChannel()), response, e.getRemoteAddress()));
					return;
				default:
	                throw new UnsupportedOperationException(
	                		thriftMessage.getTransportType().name() + " transport is not supported");
				}
			}
		}
		super.writeRequested(ctx, e);
	}

}
