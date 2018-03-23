package net.gdface.utils;

import static com.google.common.base.Preconditions.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * @author guyadong
 *
 */
public class ReflectionUtils {

	public ReflectionUtils() {
	}
	public static <T> T getInstance(Class<T> superClass,String clazzName,String staticMethodName,Class<?>[] parameterTypes,Object[] ctorArgs) 
			throws ClassNotFoundException, NoSuchMethodException{
		checkArgument(null != superClass && !Strings.isNullOrEmpty(clazzName));
		Class<? extends T> clazz = getInstanceClass(superClass,clazzName);
		
		parameterTypes = MoreObjects.firstNonNull(parameterTypes, new Class<?>[0]);
		ctorArgs = MoreObjects.firstNonNull(ctorArgs, new Object[0]);
		if(!Strings.isNullOrEmpty(staticMethodName)){
			return getInstanceByStaticMethod(superClass,clazz,staticMethodName);
		}else if(null !=parameterTypes && null !=ctorArgs){
			return getInstanceByConstructor(superClass,clazz,parameterTypes,ctorArgs);
		}
		throw new IllegalArgumentException("fail to get instance of " + superClass.getName());
	}
	public static <T> T getInstanceByConstructor(Class<T> superClass,Class<? extends T> clazz,Class<?>[] parameterTypes,Object[] ctorArgs) 
			throws NoSuchMethodException{
		checkArgument(null != superClass && null != clazz);
		checkArgument(clazz != superClass 
				&& superClass.isAssignableFrom(clazz),"%s not a implemenation of %s",clazz.getName(),superClass.getSimpleName());
		checkArgument(!Modifier.isAbstract(clazz.getModifiers()),"%s is abstract class",clazz.getName());
		parameterTypes = MoreObjects.firstNonNull(parameterTypes, new Class<?>[0]);
		ctorArgs = MoreObjects.firstNonNull(ctorArgs, new Object[0]);
		checkArgument(parameterTypes.length == ctorArgs.length);
		try{
			Constructor<? extends T> ctor = clazz.getConstructor(parameterTypes);
			try {
				return ctor.newInstance(ctorArgs);
			} catch (Exception e1) {
				Throwables.throwIfUnchecked(e1);
				throw new RuntimeException(e1);
			}
		} finally{}
	}
	@SuppressWarnings("unchecked")
	public static <T> Class<? extends T> getInstanceClass(Class<T> superClass,String clazzName) 
			throws ClassNotFoundException{
		checkArgument(null != superClass && !Strings.isNullOrEmpty(clazzName));
		Class<?> clazz = (Class<?>) Class.forName(clazzName);
		checkArgument(!clazz.isInterface() && superClass.isAssignableFrom(clazz),"%s not a implemenation or subclass of %s",clazz.getName(),superClass.getSimpleName());
		return  (Class<? extends T>)clazz;
	}
	@SuppressWarnings("unchecked")
	public static <T> T getInstanceByStaticMethod(Class<T> superClass,Class<? extends T> clazz,String staticMethodName) 
			throws NoSuchMethodException{
		checkArgument(null != superClass && null != clazz && !Strings.isNullOrEmpty(staticMethodName));
		checkArgument(clazz != superClass 
				&& superClass.isAssignableFrom(clazz),"%s not a implemenation of %s",clazz.getName(),superClass.getSimpleName());		
		try {
			Method method = clazz.getMethod(staticMethodName);
			checkArgument(Modifier.isStatic(method.getModifiers()),"%s is not a static method",method.toString());
			checkArgument(superClass.isAssignableFrom(method.getReturnType()),"unexpect return type %s",method.getReturnType().toString());
			return (T) method.invoke(null);
		} catch(NoSuchMethodException e1){
			throw e1;
		}catch (Exception e1) {
			Throwables.throwIfUnchecked(e1);
			throw new RuntimeException(e1);
		}
	}
}
