package com.facebook.swift.service;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.facebook.nifty.core.ThriftMessage;
import com.facebook.nifty.core.ThriftTransportType;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;

/**
 * XHR(XML Http Request)解码器<br>
 * 将{@link HttpRequest}请求的内容数据(content)转为{@link ThriftMessage},
 * 提供给{@link com.facebook.nifty.core.NiftyDispatcher}
 * @author guyadong
 *
 */
public class ThriftXHRDecoder extends SimpleChannelUpstreamHandler {
	
	public ThriftXHRDecoder() {
	}
	private static volatile byte[] homepage;
	/**
	 * SPI(Service Provider Interface)机制加载 {@link XHRHomepageProvider}实例获取页面数据,没有找到返回默认页面数据
	 * @return homepage页面数据
	 * @throws Exception 
	 */
	private static byte[] loadHomepageByXHRHomepageProvider() throws Exception {		
		ServiceLoader<XHRHomepageProvider> providers = ServiceLoader.load(XHRHomepageProvider.class);
		Iterator<XHRHomepageProvider> itor = providers.iterator();
		if(itor.hasNext()){
			XHRHomepageProvider provider = itor.next();
			try{
				byte [] homepage = provider.homepage();
				if(homepage != null){
					return homepage;
				}
			} catch (Exception e) {
				// 读取异常时使用默认页面数据
			}
		}
		// 返回默认HOMEPAGE数据
		return Resources.toByteArray(ThriftXHRDecoder.class.getResource("/xhr_homepage.html"));	
	
	}
	private static byte[] loadHomepage(){
		if(null == homepage){
			synchronized (ThriftXHRDecoder.class) {
				if(null == homepage){
					try {
						homepage = loadHomepageByXHRHomepageProvider();						
					} catch (Exception e) {
						Throwables.throwIfUnchecked(e);
						throw new RuntimeException(e);
					}
				}
			}
		}
		return homepage;
	}
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if(e.getMessage() instanceof HttpRequest){
			HttpRequest request = (HttpRequest)e.getMessage();
			if(request.getContent().readable()){
				if(HttpMethod.POST.equals(request.getMethod())){
					ThriftMessage thriftMessage = new ThriftMessage(request.getContent(),ThriftTransportType.UNFRAMED);
					ctx.sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), thriftMessage, e.getRemoteAddress()));
					return;
				}
			}else{
				// 空请求(没有内容)时输出首页
		        HttpResponse response = new DefaultHttpResponse(request.getProtocolVersion(), OK);
		        ChannelBuffer content = ChannelBuffers.wrappedBuffer(loadHomepage());
		        response.setContent(content);
		        ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
				return ;
			}
		}
		super.messageReceived(ctx, e);
	}
}
