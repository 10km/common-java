package com.facebook.swift.service;

public interface XHRHomepageProvider {
	/**
	 * @return 返回首页页面内容
	 * @throws Exception
	 */
	byte[] homepage() throws Exception;
}
