package net.gdface.thrift;

/**
 * thrift装饰者模式接口
 * @author guyadong
 *
 */
public interface ThriftDecorator<T> {
	/**
	 * 返回代理对象
	 * @return
	 */
	T delegate();
}
