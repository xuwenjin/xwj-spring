package com.xwj.spring;

import java.util.Map;

import com.xwj.spring.aop.ISubject;
import com.xwj.spring.aop.ann.MyAspect;
import com.xwj.spring.aop.proxy.MyProxyUtil;
import com.xwj.spring.ioc.MyApplicationContext;

/**
 * 测试AOP
 */
public class AOPTest {

	static MyApplicationContext applicationContext;

	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String beanName = "subjectImpl";
		Object bean = applicationContext.getIocBean(beanName);
		ISubject subject = (ISubject) bean;
		subject.add(2, 3);
		subject.divide(10, 5);
	}

	static {
		try {
			applicationContext = new MyApplicationContext();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		init();
	}

	public static void init() {
		updateBeanFromBeanFactory();
	}

	/**
	 * 扫描BeanFactory，找出方法上有@Aspect注解的bean，为其创建代理类对象，并替代原bean。
	 */
	public static void updateBeanFromBeanFactory() {
		MyProxyUtil proxy = new MyProxyUtil();
		if (applicationContext != null) {
			for (Map.Entry<String, Object> entry : applicationContext.getBeanFactory().entrySet()) {
				if (null != entry.getValue().getClass().getDeclaredAnnotation(MyAspect.class)) {
					Object proxyBean = proxy.getProxyInstance(entry.getValue());

					// 将bean替换成bean的代理类
					applicationContext.updateBeanFactory(entry.getKey(), proxyBean);
				}
			}
		}
	}
}