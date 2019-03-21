package net.gdface.utils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * 重写{@link #getWriteMethod()}方法，允许返回类型不为void的write方法
 * @author guyadong
 *
 */
public class LenientDecoratorOfDescriptor extends PropertyDescriptor {
	private final PropertyDescriptor descriptor;

	private LenientDecoratorOfDescriptor(PropertyDescriptor descriptor) throws IntrospectionException {
		super(descriptor.getName(), 
				classOf(descriptor),
				readMethodNameOf(descriptor),
				writeMethodNameOf(descriptor));
		this.descriptor = descriptor;
	}
	private static String readMethodNameOf(PropertyDescriptor descriptor){
		Method m = descriptor.getReadMethod();
		return null == m ? null : m.getName();
	}
	private static String writeMethodNameOf(PropertyDescriptor descriptor){
		Method m = descriptor.getWriteMethod();
		return null == m ? null : m.getName();
	}
	private static Class<?> classOf(PropertyDescriptor descriptor){
		try {
			Method m =  (Method) internalFindMethod(descriptor.getClass(),"getClass0");
			m.setAccessible(true);
			return (Class<?>) m.invoke(descriptor);
		} catch (NullPointerException e) {
			throw e;
		}catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    private static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
    @Override
	public synchronized Method getWriteMethod() {
		Method writeMethod = super.getWriteMethod();
		if (writeMethod == null) {
			Class<?> cls = classOf(this);
			String writeMethodName = "set" + capitalize(getName());

			Class<?> type = getPropertyType();
			Class<?>[] args = (type == null) ? null : new Class[] { type };
			try {
				writeMethod = cls.getMethod(writeMethodName, args);
				setWriteMethod(writeMethod);
			} catch (NoSuchMethodException e) {
			} catch (IntrospectionException ex) {
				// fall through
			}
		}
		return writeMethod;
	}
	public PropertyDescriptor origin() {
		return descriptor;
	}
	public static LenientDecoratorOfDescriptor toDecorator(PropertyDescriptor descriptor){
		if(descriptor instanceof LenientDecoratorOfDescriptor){
			return (LenientDecoratorOfDescriptor)descriptor;
		}
		try {
			return new LenientDecoratorOfDescriptor(descriptor);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}
    /**
     * Internal support for finding a target methodName with a given
     * parameter list on a given class.
     */
    private static Method internalFindMethod(Class<?> start, String methodName,Class<?> ...args) {

        Method method = null;

        for (Class<?> cl = start; cl != null && cl.getDeclaringClass() != Object.class && method == null; cl = cl.getSuperclass()) {
        	try {
        		method = cl.getDeclaredMethod(methodName, args);
        	} catch (NoSuchMethodException e) {
        	} 
        }
        return method;
    }

}
