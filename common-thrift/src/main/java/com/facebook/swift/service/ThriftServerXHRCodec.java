package com.facebook.swift.service;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;

/**
 * A combination of {@link ThriftXHRDecoder} and {@link ThriftXHREncoder}
 * which enables easier server side HTTP implementation.
 * @see org.jboss.netty.handler.codec.http.HttpServerCodec
 * @author guyadong
 *
 */
public class ThriftServerXHRCodec implements ChannelUpstreamHandler,ChannelDownstreamHandler{
    private final ThriftXHRDecoder decoder = new ThriftXHRDecoder();
    private final ThriftXHREncoder encoder = new ThriftXHREncoder(decoder);
	public ThriftServerXHRCodec() {
	}

	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		 decoder.handleUpstream(ctx, e);
	}

	@Override
	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		encoder.handleDownstream(ctx, e);
	}

}
