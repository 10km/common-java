package com.facebook.swift.service;

/**
 * 用于SPI(Service Provider Interface)方式返回首页数据接口
 * @author guyadong
 *
 */
public interface XHRHomepageProvider {
	/**
	 * @return 返回首页页面内容
	 * @throws Exception
	 */
	byte[] homepage() throws Exception;
}
