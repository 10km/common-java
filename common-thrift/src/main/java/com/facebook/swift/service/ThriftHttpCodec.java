package com.facebook.swift.service;

import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpServerCodec;

import com.facebook.nifty.codec.ThriftFrameCodec;

/**
 * {@link HttpClientCodec}本身已经实现了{@link ThriftFrameCodec}接口的所有方法,
 * 只是因为{@link ThriftFrameCodec}是nifty定义的接口，所以{@link HttpClientCodec}不是{@link ThriftFrameCodec}实例，
 * {@link ThriftHttpCodec}的作用就是将{@link HttpClientCodec}封装成{@link ThriftFrameCodec}实例
 * 
 * @author guyadong
 *
 */
public class ThriftHttpCodec extends HttpServerCodec implements ThriftFrameCodec {

}
