package com.xwj.spring;

import com.xwj.spring.ioc.LoginController;
import com.xwj.spring.ioc.MyApplicationContext;

/**
 * 测试IOC
 */
public class IOCTest {

	public static void main(String[] args) throws Exception {
		// 1、初始化Bean(IOC容器)
		MyApplicationContext applicationContext = new MyApplicationContext();
		applicationContext.getBeanFactory().forEach((key, value) -> {
			System.out.println(key + "-->" + value);
		});

		// 2、从容器中获取bean
		LoginController loginController = (LoginController) applicationContext.getIocBean("loginController");

		// 3、调用bena的方法
		String loginTest = loginController.loginTest();
		System.out.println(loginTest);
		String loginDefault = loginController.loginDefault();
		System.out.println(loginDefault);
	}

}