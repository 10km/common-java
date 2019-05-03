package com.facebook.swift.service;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DownstreamMessageEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.facebook.nifty.core.ThriftMessage;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * XHR(XML Http Request)编码器<br>
 * 将{@link com.facebook.nifty.core.NiftyDispatcher}输出的
 * {@link ThriftMessage}响应数据转为{@link DownstreamMessageEvent},
 * 
 * @author guyadong
 *
 */
public class ThriftXHREncoder extends SimpleChannelDownstreamHandler {

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if(e.getMessage() instanceof ThriftMessage){
			ThriftMessage thriftMessage = (ThriftMessage)e.getMessage();
			if(thriftMessage.getBuffer().readable()){
				switch (thriftMessage.getTransportType()) {
				case UNFRAMED:
                    DefaultHttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
                    response.setContent(thriftMessage.getBuffer());
					ctx.sendDownstream(new DownstreamMessageEvent(ctx.getChannel(), 
							Channels.future(ctx.getChannel()), response, e.getRemoteAddress()));
					return;
				default:
	                throw new UnsupportedOperationException(
	                		thriftMessage.getTransportType().name() +" transport is not supported");
				}
			}
		}
		super.writeRequested(ctx, e);
	}

}
