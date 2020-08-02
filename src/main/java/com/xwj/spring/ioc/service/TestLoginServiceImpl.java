package com.xwj.spring.ioc.service;

import com.xwj.spring.ioc.ann.MyService;

@MyService(value = "test")
public class TestLoginServiceImpl implements LoginService {

	@Override
	public String login() {
		return "测试多态情况下依赖注入";
	}
}
