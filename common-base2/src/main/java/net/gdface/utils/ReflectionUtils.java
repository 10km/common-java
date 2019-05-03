package net.gdface.utils;

import static com.google.common.base.Preconditions.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ForwardingMap;

/**
 * @author guyadong
 *
 */
public class ReflectionUtils {
	public static final String PROP_CLASSNAME = "className";
	public static final String PROP_STATICMETHODNAME = "staticMethodName";
	public static final String PROP_PARAMETERTYPES = "parameterTypes";
	public static final String PROP_CONSTRUCTORARGS = "constructorArgs";
	public static final String PROP_CONSTRUCTORPARAMS = "constructorParams";
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	public ReflectionUtils() {
	}
	private static class ParameterMap extends ForwardingMap<String,Object>{
		private final Map<String, Object> delegate;
		public ParameterMap(Map<String, Object> delegate) {
			super();
			if(null == delegate){
				this.delegate = Collections.emptyMap();
			}else{
				this.delegate = delegate;
			}
		}
		@Override
		protected Map<String, Object> delegate() {
			return delegate;
		}
		@SuppressWarnings("unchecked")
		public final <T> T of(String key,T defaultValue){
			checkArgument(null != key);
			Object value = delegate.get(key);
			try{
				return null == value ? defaultValue: (T) value;
			}catch(ClassCastException e){
				throw new IllegalArgumentException("invalid parameter: " + key + ",caused by " + e.getMessage());
			}
		}
		public final <T> T of(String key){
			return of(key,null);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> getInstanceClass(Class<T> superClass,String instanceClassName) 
			throws ClassNotFoundException{
		checkArgument(null != superClass && !Strings.isNullOrEmpty(instanceClassName));
		Class<?> instanceClass = (Class<?>) Class.forName(instanceClassName);
		checkInstanceClass(superClass,instanceClass);
		return  (Class<? extends T>)instanceClass;
	}
	public static <T> T getInstance(Class<T> superClass,Map<String,Object> params) 
			throws NoSuchMethodException, ClassNotFoundException{
		ParameterMap paramMap = new ParameterMap(params);
		String clazzName = paramMap.of(PROP_CLASSNAME);
		Class<? extends T> instanceClass = getInstanceClass(superClass,clazzName);
		String staticMethodName =paramMap.of(PROP_STATICMETHODNAME);
		if(null != staticMethodName){
			try{
				return getInstanceByStaticMethod(superClass,instanceClass,staticMethodName);
			}catch(NoSuchMethodException e){
				// 找不到静态方法则尝试用构造方法创建实例
			}
		}
		if(paramMap.containsKey(PROP_CONSTRUCTORPARAMS)){
			LinkedHashMap<Class<?>,Object> constructorParams = paramMap.of(PROP_CONSTRUCTORPARAMS);
			return getInstanceByConstructor(superClass,instanceClass,constructorParams);
		}else{
			Class<?>[] parameterTypes = paramMap.of(PROP_PARAMETERTYPES);
			Object[] ctorArgs = paramMap.of(PROP_CONSTRUCTORARGS);
			return getInstanceByConstructor(superClass,instanceClass,parameterTypes,ctorArgs);
		}
	}
	private static void checkInstanceClass(Class<?> superClass,Class<?> instanceClass){
		checkArgument(null != superClass && null != instanceClass);
		checkArgument(!instanceClass.isInterface() && superClass.isAssignableFrom(instanceClass),
				"%s not a implemenation of %s",instanceClass.getName(),superClass.getSimpleName());
		checkArgument(!Modifier.isAbstract(instanceClass.getModifiers()),
				"%s is abstract class",instanceClass.getName());
		checkArgument(Modifier.isStatic(instanceClass.getModifiers()) || null == instanceClass.getDeclaringClass(),
				"%s is not static class",instanceClass.getName());
	}
	public static <T> T getInstanceByConstructor(Class<T> superClass,Class<? extends T> instanceClass,Class<?>[] parameterTypes,Object[] constructorArgs) 
			throws NoSuchMethodException{
		checkInstanceClass(superClass,instanceClass);
		parameterTypes = MoreObjects.firstNonNull(parameterTypes, EMPTY_CLASS_ARRAY);
		constructorArgs = MoreObjects.firstNonNull(constructorArgs, EMPTY_OBJECT_ARRAY);
		checkArgument(parameterTypes.length == constructorArgs.length);
		try{
			Constructor<? extends T> ctor = instanceClass.getConstructor(parameterTypes);
			try {
				return ctor.newInstance(constructorArgs);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new RuntimeException(e);
			}
		} finally{}
	}
	public static <T> T getInstanceByConstructor(Class<T> superClass,Class<? extends T> instanceClass,LinkedHashMap<Class<?>,Object> constructorParams) 
			throws NoSuchMethodException{
		checkInstanceClass(superClass,instanceClass);
		constructorParams = MoreObjects.firstNonNull(constructorParams, new LinkedHashMap<Class<?>,Object>());
		Class<?>[] parameterTypes = constructorParams.keySet().toArray(new Class<?>[constructorParams.size()]);
		Object[] initargs = constructorParams.values().toArray(new Object[constructorParams.size()]);
		Constructor<? extends T> ctor = instanceClass.getConstructor(parameterTypes);
		try {
			return ctor.newInstance(initargs);
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	@SuppressWarnings("unchecked")
	public static <T> T getInstanceByStaticMethod(Class<T> superClass,Class<? extends T> instanceClass,String staticMethodName) 
			throws NoSuchMethodException{
		checkArgument(!Strings.isNullOrEmpty(staticMethodName));
		checkInstanceClass(superClass,instanceClass);
		try {
			Method method = instanceClass.getMethod(staticMethodName);
			checkArgument(Modifier.isStatic(method.getModifiers()),"%s is not a static method",method.toString());
			checkArgument(superClass.isAssignableFrom(method.getReturnType()),"unexpect return type %s",method.getReturnType().toString());
			try{
				return (T) method.invoke(null);
			}catch(ClassCastException e){
				throw new IllegalArgumentException(
						String.format("invalid return type of static method %s caused by %s",method.toString(),e.getMessage()));
			}
		} catch(NoSuchMethodException e){
			throw e;
		}catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 反射获取{@code object}的私有成员
	 * @param object
	 * @param name
	 * @return 成员对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T valueOfField(Object object,String name){
		try {
			Field field = checkNotNull(object,"object is null").getClass().getDeclaredField(checkNotNull(name,"name is null"));
			field.setAccessible(true);
			return (T) field.get(object);
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
}
