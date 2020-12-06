package com.xwj.spring.circlebean.service;

public class AService {

	private BService bService;

	public BService getbService() {
		return bService;
	}

	public void setbService(BService bService) {
		this.bService = bService;
	}

	public String hello() {
		return "A";
	}

}