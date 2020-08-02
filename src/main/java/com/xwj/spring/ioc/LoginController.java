package com.xwj.spring.ioc;

import com.xwj.spring.ioc.ann.MyAutowired;
import com.xwj.spring.ioc.ann.MyController;
import com.xwj.spring.ioc.ann.MyValue;
import com.xwj.spring.ioc.service.LoginService;

@MyController
public class LoginController {

	@MyValue(value = "ioc.scan.pathTest")
	private String test;

	@MyAutowired(value = "test")
	private LoginService testLoginService;

	@MyAutowired(value = "loginServiceImpl")
	private LoginService defaultLoginService;

	public String loginTest() {
		return testLoginService.login();
	}

	public String loginDefault() {
		return defaultLoginService.login();
	}

}