package net.gdface.thrift;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftUtils.*;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.facebook.swift.codec.metadata.ThriftStructMetadata;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * 有{@link com.facebook.swift.codec.ThriftStruct}注释的异常类型之间的转换
 * @author guyadong
 *
 * @param <L> thrift 生成的client异常类型
 * @param <R> 实现 {@link ThriftDecorator}的装饰类异常类型
 */
public class ThriftExceptionTransformer<L extends Exception,R extends ThriftDecorator<? extends Exception>> 
			implements Function<L,R>{
	private final ThriftStructMetadata leftMetadata;
	private final ThriftStructMetadata rightMetadata;
	public ThriftExceptionTransformer(Class<L> left, Class<R> right) {
		checkArgument(isThriftException(left,right),"left and right must not be Exception with @ThriftStruct annotation"	);
		this.leftMetadata= CATALOG.getThriftStructMetadata(left);
		this.rightMetadata= CATALOG.getThriftStructMetadata(right);
	}
	@Override
	public R apply(L input) {
		if(null == input){
			return null;
		}
		try {
			Map<Short, Object> data = ThriftUtils.getFiledValues(input,leftMetadata);
			String message = ((Exception)input).getMessage();
			Constructor<R> constructor = getStringConstructor();
			if(null == constructor || Strings.isNullOrEmpty(message)){
				// 默认构造方法创建实例
				return constructStruct(data,rightMetadata);
			}else{
				// 调用String参数的构造方法创建实例
				R instance = constructor.newInstance(message);
				return fillStructField(data,rightMetadata,instance);
			}
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
	/**
	 * 获取String参数的构造方法,没有则返回{@code null}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Constructor<R> getStringConstructor(){
		return ThriftUtils.getConstructor((Class<R>)rightMetadata.getStructClass(),String.class); 
	}
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder()
				.append("ThriftExceptionTransformer [leftClass=")
				.append(leftMetadata.getStructType())
				.append(", rightClass=")
				.append(rightMetadata.getStructType())
				.append("]");
		return builder.toString();
	}
}