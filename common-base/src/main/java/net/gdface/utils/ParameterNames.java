package net.gdface.utils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.gdface.utils.Assert;

import org.apache.bytecode.ChainedParamReader;

/**
 * 获取构造函数或方法的参数名<br>
 * 当不能获取参数名的情况下,
 * {@link returnFakeNameIfFail}为{@code false}时返回{@code null},否则返回返回arg,arg2...格式的替代名<br>
 * {@link returnFakeNameIfFail}默认为{@code true}
 * @author guyadong
 *
 */
public class ParameterNames {
	private final Map<Class<?>, ChainedParamReader> readers = new HashMap<Class<?>, ChainedParamReader>();
	private final Class<?> clazz;
	/** 当获取无法参数名时是否返回arg,arg2...格式的替代名字 */
	private boolean returnFakeNameIfFail = true;
	public ParameterNames setReturnFakeNameIfFail(boolean returnFakeNameIfFail) {
		this.returnFakeNameIfFail = returnFakeNameIfFail;
		return this;
	}

	/**
	 * @param clazz 要构造函数或方法的参数名的类,为{@code null}时所有getParameterNames方法返回{@code null}
	 */
	public ParameterNames(Class<?> clazz) {
		this.clazz = clazz;
		if(null != clazz){
			try {
				Class<?> c = clazz;
				do {
					readers.put(c, new ChainedParamReader(c));
				} while (null != (c = c.getSuperclass()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 获取构造函数或方法的参数名
	 * @param reader
	 * @param member 构造函数或方法对象
	 * @return
	 */
	private final String[] getParameterNames(ChainedParamReader reader, Member member) {
		String [] parameterNames = null;
		int paramCount ;
		if (member instanceof Method){
			parameterNames = reader.getParameterNames((Method) member);
			paramCount = ((Method) member).getParameterTypes().length;
		} else if (member instanceof Constructor){
			parameterNames = reader.getParameterNames((Constructor<?>) member);
			paramCount = ((Constructor<?>) member).getParameterTypes().length;
		} else {
			throw new IllegalArgumentException("member type must be Method or Constructor");
		}
		if(this.returnFakeNameIfFail){
			if (null == parameterNames) {
				parameterNames = new String[paramCount];
				for (int i = 0; i < parameterNames.length; i++)
					parameterNames[i] = String.format("arg%d", i);
			}
		}
		return parameterNames;
	}

	/**
	 * 获取构造函数或方法的参数名
	 * @param member 构造函数或方法对象
	 * @return
	 * @see #getParameterNames(ChainedParamReader, Member)
	 */
	public final String[] getParameterNames(Member member) {
		if(null == clazz){
			return null;
		}
		Assert.notNull(member, "member");
		Class<?> declaringClass = member.getDeclaringClass();
		ChainedParamReader reader;
		if (null == (reader = readers.get(declaringClass))) {
			throw new IllegalArgumentException(String.format("%s is not member of %s", member.toString(),
					declaringClass.getName()));
		}
		return getParameterNames(reader, member);
	}
	
	/**
	 * 获取构造函数或方法的参数名<br>
	 * {@code name}为{@code null}时,获取构造函数的参数名
	 * @param name 方法名
	 * @param parameterTypes 构造函数或方法的参数类型
	 * @return
	 * @throws NoSuchMethodException
	 * @see #getParameterNames(String, Class)
	 */
	public final String[] getParameterNames(String name, Class<?>[] parameterTypes) throws NoSuchMethodException {
		if(null == clazz){
			return null;
		}
		try {
			Member member = null == name ? clazz.getConstructor(parameterTypes) : clazz.getMethod(name, parameterTypes);
			return getParameterNames(member);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(e);
		}
	}
	/**
	 * {@link #getParameterNames(String, Class[])}不显式抛出异常版本
	 * @param name
	 * @param parameterTypes
	 * @return
	 */
	public final String[] getParameterNamesUnchecked(String name, Class<?>[] parameterTypes)  {
		try {
			return getParameterNames(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
