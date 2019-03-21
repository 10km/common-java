package net.gdface.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

public class BeanPropertyUtils {

	private static final boolean hasReadMethod(PropertyDescriptor propertyDescriptor) {
		Method m = propertyDescriptor.getReadMethod();
		// 过滤掉Object中的get方法
		if (m != null && m.getDeclaringClass() != Object.class) {
			return true;
		}
		return false;
	}

	private static final boolean hasWriteMethod(PropertyDescriptor propertyDescriptor) {
		Method m = propertyDescriptor.getWriteMethod();
		// 过滤掉Object中的get方法
		if (m != null && m.getDeclaringClass() != Object.class) {
			return true;
		}
		return false;
	}

	/**
	 * 获取beanClass中所有具有指定读写类型(rw)的属性
	 * @param beanClass
	 * @param rw 属性类型标记 <br>
	 * 					<li>0 所有属性</li>
	 * 					<li>1 读属性</li>
	 * 					<li>2 写属性</li>
	 * 					<li>3 读写属性</li>
	 * @param lenient 是否为宽容模式---允许返回类型不为void的setter方法
	 * @return 属性名与PropertyDescriptor映射的Map对象
	 */
	public static final Map<String, PropertyDescriptor> getProperties(Class<?> beanClass, int rw,boolean lenient) {
		try {
			Map<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
			if (beanClass != null) {
				BeanUtilsBean beanUtils = BeanUtilsBean.getInstance();
				PropertyUtilsBean propertyUtils = beanUtils.getPropertyUtils();
				PropertyDescriptor[] origDescriptors = propertyUtils.getPropertyDescriptors(beanClass);
				Boolean put;
				for (PropertyDescriptor pd : origDescriptors) {
					if(lenient){
						pd = LenientDecoratorOfDescriptor.toDecorator(pd);
					}
					put = false;
					switch (rw &= 3) {
					case 0:
						put = true;
						break;
					case 1:
						put = hasWriteMethod(pd);
						break;
					case 2:
						put = hasReadMethod(pd);
						break;
					case 3:
						put = hasReadMethod(pd) && hasWriteMethod(pd);
						break;
					}
					if (put) {
						properties.put(pd.getName(), pd);
					}
				}
			}
			return properties;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 获取beanClass中所有具有指定读写类型(rw)的属性
	 * @param beanClass
	 * @param rw 属性类型标记 <br>
	 * 					<li>0 所有属性</li>
	 * 					<li>1 读属性</li>
	 * 					<li>2 写属性</li>
	 * 					<li>3 读写属性</li>
	 * @return 属性名与PropertyDescriptor映射的Map对象
	 */
	public static final Map<String, PropertyDescriptor> getProperties(Class<?> beanClass, int rw) {
		return getProperties(beanClass, rw, false);
	}
	public static final <T>T copy(T from,T to){
		if(null==from||null==to)
			throw new NullPointerException();
		PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();
		try {
			propertyUtils.copyProperties(to, from);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return to;
	}
}
