package com.xwj.spring.ioc.service;

import com.xwj.spring.ioc.ann.MyService;

@MyService
public class LoginServiceImpl implements LoginService {

	@Override
	public String login() {
		return "测试默认beanName注入";
	}
}