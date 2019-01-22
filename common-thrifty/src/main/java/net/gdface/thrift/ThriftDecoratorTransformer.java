package net.gdface.thrift;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.*;

import java.lang.reflect.Constructor;

/**
 * 提供装饰对象{@link ThriftDecorator}和被装饰对象之间的类型转换
 * @author guyadong
 *
 * @param <L>
 * @param <R>
 */
public class ThriftDecoratorTransformer<L ,R extends ThriftDecorator<L> > {

	private final Constructor<R> constructor;

	public ThriftDecoratorTransformer(Class<L> left,Class<R> right) {
		checkArgument(null != left && null != right,"left or right is null");
		try {
			constructor = right.getConstructor(left);			
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/** 转换为thrift装饰对象{@link ThriftDecorator} */
	public final Function<L,R> toDecoratorFun = new Function<L,R>(){
		@Override
		public R apply(L input) {
			try {
				return null == input ? null	: constructor.newInstance(input);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new RuntimeException(e);
			}
		}};
	/** 转为被装饰对象 */
	public final  Function<R,L> toDelegateFun = new Function<R,L>(){
		@Override
		public L apply(R input) {
			return null == input ? null : input.delegate();
		}};
}
