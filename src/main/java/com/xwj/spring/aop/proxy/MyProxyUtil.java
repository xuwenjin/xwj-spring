package com.xwj.spring.aop.proxy;

public class MyProxyUtil {

	/**
	 * 动态的创建一个代理类的对象.
	 */
	public Object getProxyInstance(Object obj) {
		MyInvocationHandler handler = new MyInvocationHandler();
		return handler.bind(obj);
	}

}