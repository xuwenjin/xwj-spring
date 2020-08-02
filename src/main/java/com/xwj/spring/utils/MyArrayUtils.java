package com.xwj.spring.utils;

import org.apache.commons.lang3.ArrayUtils;

/**
 * 工具类
 */
public class MyArrayUtils {

	/**
	 * 判断数组中是否包含元素
	 */
	public static boolean useArrayUtils(String[] arr, String targetValue) {
		return ArrayUtils.contains(arr, targetValue);
	}

	/**
	 * 判断数组中是否包含元素
	 */
	public static boolean useArrayUtils(Class<?>[] arr, Class<?> targetValue) {
		return ArrayUtils.contains(arr, targetValue);
	}

}
