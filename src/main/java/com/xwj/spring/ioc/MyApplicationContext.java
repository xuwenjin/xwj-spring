package com.xwj.spring.ioc;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.xwj.spring.ioc.ann.MyAutowired;
import com.xwj.spring.ioc.ann.MyController;
import com.xwj.spring.ioc.ann.MyService;
import com.xwj.spring.ioc.ann.MyValue;
import com.xwj.spring.utils.MyArrayUtils;

/**
 * IOC工具类
 */
public class MyApplicationContext {

	/**
	 * 类集合--存放所有的全限制类名
	 */
	private Set<String> classSet = new HashSet<>();

	/**
	 * IOC容器 如： String(beanName) --> Object(bean实例)
	 */
	private Map<String, Object> beanFactory = new ConcurrentHashMap<>(32);

	public MyApplicationContext() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		// 初始化数据
		this.classLoader();
	}

	/**
	 * 从IOC容器中获取对象
	 */
	public Object getIocBean(String beanName) {
		if (beanFactory != null) {
			return beanFactory.get(lowerCaseFirst(beanName));
		} else {
			return null;
		}
	}

	/**
	 * 获取beanFactory
	 */
	public Map<String, Object> getBeanFactory() {
		return beanFactory;
	}

	/**
	 * 更新beanFactory
	 */
	public void updateBeanFactory(String beanName, Object bean) {
		if (StringUtils.isNotBlank(beanName)) {
			beanFactory.put(beanName, bean);
		}
	}

	/**
	 * 类加载器
	 */
	private void classLoader() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		// 加载配置文件所有配置信息
		new ConfigurationUtils(null);

		// 获取扫描包路径
		String classScanPath = (String) ConfigurationUtils.properties.get("ioc.scan.path");
		if (StringUtils.isNotEmpty(classScanPath)) {
			classScanPath = classScanPath.replace(".", "/");
		} else {
			throw new RuntimeException("请配置项目包扫描路径 ioc.scan.path");
		}

		// 获取项目中全部的代码文件中带有MyService注解的
		this.getPackageClassFile(classScanPath);

		for (String className : classSet) {
			addServiceToIoc(Class.forName(className));
		}

		// 获取带有MyService注解类的所有的带MyAutowired注解的属性并对其进行实例化
		Set<String> beanKeySet = beanFactory.keySet();
		for (String beanName : beanKeySet) {
			addAutowiredToField(beanFactory.get(beanName));
		}
	}

	/**
	 * 将bean信息放入beanFactory
	 */
	private void addServiceToIoc(Class<?> classZ) throws IllegalAccessException, InstantiationException {
		if (classZ.getAnnotation(MyController.class) != null || classZ.getAnnotation(MyService.class) != null) {
			Object instance = classZ.newInstance();
			String beanName = lowerCaseFirst(classZ.getSimpleName());

			// 将当前类交由IOC管理
			if (classZ.getAnnotation(MyController.class) != null) {
				beanFactory.put(beanName, instance);
				System.out.println("控制反转访问控制层:" + beanName);
			} else if (classZ.getAnnotation(MyService.class) != null) {
				MyService myService = (MyService) classZ.getAnnotation(MyService.class);
				// 如果MyService注解配置了值，只用配置的值，否则使用类名首字母小写
				beanName = StringUtils.isEmpty(myService.value()) ? beanName : lowerCaseFirst(myService.value());
				beanFactory.put(beanName, instance);
				System.out.println("控制反转服务层:" + beanName);
			}
		}
	}

	/**
	 * 依赖注入(实例化带有@MyAutowired的类、对有@MyValue的属性赋值)
	 */
	private void addAutowiredToField(Object obj)
			throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		Field[] fields = obj.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(MyAutowired.class) != null) {
				field.setAccessible(true);
				MyAutowired myAutowired = field.getAnnotation(MyAutowired.class);
				String autowiredValue = myAutowired.value();

				Class<?> fieldClass = field.getType();
				// 接口不能被实例化，需要对接口进行特殊处理获取其子类，获取所有实现类
				if (fieldClass.isInterface()) {
					// 如果有指定获取子类名
					if (StringUtils.isNotEmpty(autowiredValue)) {
						field.set(obj, beanFactory.get(autowiredValue));
					} else {
						// 当注入接口时，属性的名字与接口实现类名一致则直接从容器中获取
						Object objByName = beanFactory.get(field.getName());
						if (objByName != null) {
							field.set(obj, objByName);
							// 递归依赖注入
							addAutowiredToField(field.getType());
						} else {
							// 注入接口时，如果属性名称与接口实现类名不一致的情况下
							List<Object> list = findSuperInterfaceByIoc(field.getType());
							if (list != null && list.size() > 0) {
								if (list.size() > 1) {
									throw new RuntimeException(
											obj.getClass() + "  注入接口 " + field.getType() + "   失败，请在注解中指定需要注入的具体实现类");
								} else {
									field.set(obj, list.get(0));
									// 递归依赖注入
									addAutowiredToField(field.getType());
								}
							} else {
								throw new RuntimeException("当前类" + obj.getClass() + "  不能注入接口 "
										+ field.getType().getClass() + "  ， 接口没有实现类不能被实例化");
							}
						}
					}
				} else {
					String beanName = StringUtils.isEmpty(autowiredValue) ? lowerCaseFirst(field.getName())
							: lowerCaseFirst(autowiredValue);
					Object beanObj = beanFactory.get(beanName);
					field.set(obj, beanObj == null ? field.getType().newInstance() : beanObj);
					System.out.println("依赖注入" + field.getName());
				}
				addAutowiredToField(field.getType());
			}
			if (field.getAnnotation(MyValue.class) != null) {
				field.setAccessible(true);
				MyValue value = field.getAnnotation(MyValue.class);
				field.set(obj, StringUtils.isNotEmpty(value.value())
						? ConfigurationUtils.getPropertiesByKey(value.value()) : null);
				System.out.println("注入配置文件  " + obj.getClass() + " 加载配置属性" + value.value());
			}
		}
	}

	/**
	 * 判断需要注入的接口所有的实现类
	 */
	private List<Object> findSuperInterfaceByIoc(Class<?> classz) {
		Set<String> beanNameList = beanFactory.keySet();
		ArrayList<Object> objectArrayList = new ArrayList<>();
		for (String beanName : beanNameList) {
			Object obj = beanFactory.get(beanName);
			Class<?>[] interfaces = obj.getClass().getInterfaces();
			if (MyArrayUtils.useArrayUtils(interfaces, classz)) {
				objectArrayList.add(obj);
			}
		}
		return objectArrayList;
	}

	/**
	 * 扫描项目根目录中所有的class文件
	 */
	private void getPackageClassFile(String packageName) {
		URL url = this.getClass().getClassLoader().getResource(packageName);
		File file = new File(url.getFile());
		if (file.exists() && file.isDirectory()) {
			File[] files = file.listFiles();
			for (File fileSon : files) {
				if (fileSon.isDirectory()) {
					// 递归扫描
					getPackageClassFile(packageName + "/" + fileSon.getName());
				} else {
					// 是文件并且是以 .class结尾
					if (fileSon.getName().endsWith(".class")) {
						System.out.println("正在加载: " + packageName.replace("/", ".") + "." + fileSon.getName());
						classSet.add(packageName.replace("/", ".") + "." + fileSon.getName().replace(".class", ""));
					}
				}
			}
		} else {
			throw new RuntimeException("没有找到需要扫描的文件目录");
		}
	}

	/**
	 * 将字符串首字母变成大写(通过ASCII编码前移)
	 */
	protected static String upperCaseFirst(String str) {
		if (!StringUtils.isEmpty(str)) {
			char[] cs = str.toCharArray();
			if (Character.isLowerCase(cs[0])) {
				cs[0] += 32;
			}
			return String.valueOf(cs);
		}
		return null;
	}

	/**
	 * 将字符串首字母变成小写(通过ASCII编码前移)
	 */
	public static String lowerCaseFirst(String name) {
		if (!StringUtils.isEmpty(name)) {
			char[] cs = name.toCharArray();
			if (Character.isUpperCase(cs[0])) {
				cs[0] += 32;
			}
			return String.valueOf(cs);
		}
		return null;
	}

}
