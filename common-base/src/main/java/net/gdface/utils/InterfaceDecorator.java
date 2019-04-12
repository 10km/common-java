package net.gdface.utils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 实现接口<I>实例<T>的代理类 <br>
 * 应用层可以根据需要继承此类重写{@link #invoke(Object, Method, Object[])}方法
 * 
 * @author guyadong
 *
 * @param <I> 接口类型
 * @param <T> 接口实现类型
 */
public class InterfaceDecorator<I,T> implements InvocationHandler,Delegator<T>{
	private final Class<I> interfaceClass;
	protected final T delegate;

	/**
	 * 构造方法
	 * 
	 * @param interfaceClass 接口类
	 * @param delegate 实现接口的类
	 */
	public InterfaceDecorator(Class<I> interfaceClass, T delegate) {
		Assert.notNull(interfaceClass, "interfaceClass");
		Assert.notNull(delegate, "delegate");
		Assert.isTrue(interfaceClass.isInterface() && interfaceClass.isInstance(delegate), 
				"interfaceClass.isInstance(delegate)", "delegate must implement interfaceClass ");
		this.interfaceClass = interfaceClass;
		this.delegate = delegate;
	}
	/**
	 * 简化版构造函数<br>
	 * 当delegate只实现了一个接口时，自动推断接口类型
	 * @param delegate
	 */
	@SuppressWarnings("unchecked")
	public InterfaceDecorator(T delegate) {
		Assert.notNull(delegate, "delegate");
		if(delegate.getClass().getInterfaces().length !=1){
			throw new IllegalArgumentException(
				String.format("can't determines interface class from %s", delegate.getClass().getName()));
		}
		this.interfaceClass = (Class<I>) delegate.getClass().getInterfaces()[0];
		this.delegate = delegate;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return method.invoke(delegate, args);
	}

	/**
	 * 返回代理的接口类
	 * @return
	 */
	public final Class<I> getInterfaceClass() {
		return interfaceClass;
	}

	/**
	 * 根据当前对象创建新的接口实例{@link Proxy}
	 * @return
	 */
	public final I proxyInstance(){
		return interfaceClass.cast(Proxy.newProxyInstance(
				interfaceClass.getClassLoader(),
				new Class<?>[]{ interfaceClass},
				this));
	}
	@Override
	public T delegate() {
		// TODO 自动生成的方法存根
		return delegate;
	}
}
