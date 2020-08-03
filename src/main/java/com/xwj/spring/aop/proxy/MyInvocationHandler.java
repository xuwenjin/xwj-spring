package com.xwj.spring.aop.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.xwj.spring.aop.ann.MyAfter;
import com.xwj.spring.aop.ann.MyAfterReturning;
import com.xwj.spring.aop.ann.MyAfterThrowing;
import com.xwj.spring.aop.ann.MyAround;
import com.xwj.spring.aop.ann.MyBefore;

/**
 * JDK动态代理
 */
public class MyInvocationHandler implements InvocationHandler {

	private Object target;// 被代理类
	private Object invoke;

	public Object bind(Object obj) { // 绑定具体实现类
		this.target = obj;

		// 通过反射机制获取代理对象
		Class<?> cls = obj.getClass();
		return Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), this);
	}

	/**
	 * 动态代理：实现了环绕通知、前置通知、后置通知等通知。
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 入参的类型的处理，返回被代理对象真正要执行的那个方法：
		Method declaredMethod = handleArgs(method);

		// 环绕通知：
		Boolean bool = false;
		if (null != declaredMethod.getAnnotation(MyAround.class)) {
			bool = true;
		}
		aroundInform(declaredMethod, bool, method, args);

		String methodName = declaredMethod.getName();

		try {
			if (null != declaredMethod.getAnnotation(MyBefore.class)) {
				System.out.println(methodName + ", 前置通知");
			}

			// 通过放射，真正执行被代理对象的方法：
			invoke = method.invoke(target, args);

			if (null != declaredMethod.getAnnotation(MyAfterReturning.class)) {
				System.out.println(methodName + ", 返回通知, 结果：" + invoke);
			}
		} catch (Exception e) {
			if (null != declaredMethod.getAnnotation(MyAfterThrowing.class)) {
				System.out.println(methodName + ", 异常通知：" + e);
			}
		} finally {
			if (null != declaredMethod.getAnnotation(MyAfter.class)) {
				System.out.println(methodName + ", 后置通知");
			}
		}

		return invoke;
	}

	/**
	 * 入参的类型的处理，这个方法很重要
	 * 
	 * @param method
	 *            被代理对象的接口中声明的被代理方法
	 * 
	 * @return 被代理对象真正要执行的那个方法
	 */
	public Method handleArgs(Method method) throws NoSuchMethodException, SecurityException {
		Class<?>[] parameterTypes = method.getParameterTypes();
		switch (parameterTypes.length) {
		case 1:
			// System.out.println("parameterTypes.length = 1 : " +
			// parameterTypes[0]);
			return target.getClass().getDeclaredMethod(method.getName(), parameterTypes[0]);
		case 2:
			// System.out.println("parameterTypes.length = 2 : " +
			// parameterTypes[0] + " ; " + parameterTypes[1]);
			return target.getClass().getDeclaredMethod(method.getName(), parameterTypes[0], parameterTypes[1]);
		case 3:
			// System.out.println("parameterTypes.length = 3 : " +
			// parameterTypes[0] + " ; " + parameterTypes[1] + " ; "
			// + parameterTypes[2]);
			return target.getClass().getDeclaredMethod(method.getName(), parameterTypes[0], parameterTypes[1],
					parameterTypes[2]);
		default:
			// System.out.println("parameterTypes.length = 0 : " +
			// parameterTypes.length);
			return target.getClass().getDeclaredMethod(method.getName());
		}
	}

	/**
	 * 环绕通知
	 * 
	 * @param declaredMethod
	 *            被代理对象的被代理方法
	 * @param bool
	 * @param method
	 *            被代理对象的接口中声明的被代理方法
	 * @param args
	 *            被代理方法的声明的入参
	 */
	private void aroundInform(Method declaredMethod, Boolean bool, Method method, Object[] args) {
		if (bool) {
			String methodName = declaredMethod.getName();
			try {
				System.out.println(methodName + ", 前置 ");

				invoke = method.invoke(target, args);

				System.out.println(methodName + ", 返回, 结果：" + invoke);
			} catch (Exception e) {
				System.out.println(methodName + ", 异常：" + e);
			} finally {
				System.out.println(methodName + ", 后置");
			}
		}
	}

}