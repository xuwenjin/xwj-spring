package com.xwj.spring.circlebean.service;

public class BService {

	private AService aService;

	public AService getaService() {
		return aService;
	}

	public void setaService(AService aService) {
		this.aService = aService;
	}

	public String hello() {
		return "B";
	}

}