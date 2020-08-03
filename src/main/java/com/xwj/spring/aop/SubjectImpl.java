package com.xwj.spring.aop;

import com.xwj.spring.aop.ann.MyAfter;
import com.xwj.spring.aop.ann.MyAfterReturning;
import com.xwj.spring.aop.ann.MyAfterThrowing;
import com.xwj.spring.aop.ann.MyAround;
import com.xwj.spring.aop.ann.MyAspect;
import com.xwj.spring.aop.ann.MyBefore;
import com.xwj.spring.ioc.ann.MyService;

@MyAspect // 切面注解类，加了该注解就表示被注解的类的实例需要做动态代理。
@MyService // 自定义注解类，有该注解就表示被注解类交由自定义IOC容器管理了。
public class SubjectImpl implements ISubject {

	@MyAfter
	@MyAfterReturning
	@MyBefore
	@MyAfterThrowing
	@Override
	public int add(int a, int b) {
		System.out.println("--> a + b = " + (a + b));
		return a + b;
	}

	@MyAround
	@Override
	public int divide(int a, int b) {
		return a / b;
	}

}