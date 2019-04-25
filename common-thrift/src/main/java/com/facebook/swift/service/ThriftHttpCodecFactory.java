package com.facebook.swift.service;

import org.apache.thrift.protocol.TProtocolFactory;
import org.jboss.netty.channel.ChannelHandler;

import com.facebook.nifty.codec.ThriftFrameCodecFactory;

public class ThriftHttpCodecFactory implements ThriftFrameCodecFactory {

	@Override
	public ChannelHandler create(int maxFrameSize, TProtocolFactory defaultProtocolFactory) {
		return new ThriftHttpCodec();
	}

}
