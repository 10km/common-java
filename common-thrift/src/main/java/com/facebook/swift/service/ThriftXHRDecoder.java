package com.facebook.swift.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.UpstreamMessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

import com.facebook.nifty.core.ThriftMessage;
import com.facebook.nifty.core.ThriftTransportType;

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

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if(e.getMessage() instanceof HttpRequest){
			HttpRequest request = (HttpRequest)e.getMessage();
			if(request.getContent().readable() && HttpMethod.POST.equals(request.getMethod())){
				ThriftMessage thriftMessage = new ThriftMessage(request.getContent(),ThriftTransportType.UNFRAMED);
				ctx.sendUpstream(new UpstreamMessageEvent(ctx.getChannel(), thriftMessage, e.getRemoteAddress()));
				return;
			}
		}
		super.messageReceived(ctx, e);
	}
}
