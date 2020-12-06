package com.xwj.spring.circlebean;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xwj.spring.circlebean.service.AService;
import com.xwj.spring.circlebean.service.BService;

/**
 * 简易版解决spring bean循环依赖问题
 * 
 * 实际上spring容器是是用三级缓存实现循环依赖的：
 * 
 * 第一级缓存：singletonObjects       ---->  在bean对象创建完毕后会放入一级缓存
 * 
 * 第二级缓存：earlySingletonObjects  ---->  在bean对象被放入三级缓存后，以后的操作如果要进入缓存查询，就会将三级缓存中的bean对象移动到二级缓存，此时放入三级缓存的bean对象会被移除
 * 
 * 第三级缓存：singletonFactory       ---->  bean对象在被实例化后会放入第三级缓存(此时的bean不是完整的)
 * 
 * 三级缓存其实就是三个Map，它们在特定的时间、特定的场合被使用，以解决特定的问题。
 * 
 * 查询bean时，是先查询第一级缓存，查不到再查询第二级缓存，再查不到就查询第三级缓存
 * 
 */
public class SimpleCircleBean {

	/**放置创建好的bean Map*/
	private static Map<String, Object> cacheBeanMap = new ConcurrentHashMap<>();

	/**
	 * 测试，判断注入后，是否为同一个bean
	 */
	public static void main(String[] args) throws Exception {
		System.out.println(getBean(BService.class).getaService() == getBean(AService.class));
		System.out.println(getBean(AService.class).getbService() == getBean(BService.class));

		System.out.println(getBean(AService.class).hello());
		System.out.println(getBean(BService.class).hello());
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> beanClass) throws Exception {
		// 本文用类名小写 简单代替bean的命名规则
		String beanName = beanClass.getSimpleName().toLowerCase();

		// 如果已经是一个bean，则直接返回
		if (cacheBeanMap.containsKey(beanName)) {
			return (T) cacheBeanMap.get(beanName);
		}

		// 将对象本身实例化
		Object object = beanClass.getDeclaredConstructor().newInstance();

		// 放入缓存
		cacheBeanMap.put(beanName, object);

		// 把所有字段当成需要注入的bean，创建并注入到当前bean中
		Field[] fieldArr = object.getClass().getDeclaredFields();
		for (Field field : fieldArr) {
			field.setAccessible(true);

			// 获取需要注入字段的class
			Class<?> fieldClass = field.getType();

			// 如果是基本类型，直接跳过
			if (!fieldClass.isPrimitive()) {
				String fieldBeanName = fieldClass.getSimpleName().toLowerCase();

				// 如果需要注入的bean，已经在缓存Map中，那么把缓存Map中的值注入到该field即可
				// 如果缓存没有 继续创建
				field.set(object, cacheBeanMap.containsKey(fieldBeanName) ? cacheBeanMap.get(fieldBeanName) : getBean(fieldClass));
			}
		}

		// 属性填充完成，返回
		return (T) object;
	}

}
