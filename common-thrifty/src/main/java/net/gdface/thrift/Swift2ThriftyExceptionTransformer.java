package net.gdface.thrift;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftUtils.*;
import static net.gdface.thrift.ThriftyStructMetadata.STRUCTS_CACHE;

import java.util.Map;

import com.facebook.swift.codec.metadata.ThriftStructMetadata;
import com.google.common.base.Function;

/**
 * 有{@link com.facebook.swift.codec.ThriftStruct}注释的异常类型之间的转换
 * @author guyadong
 *
 * @param <L> thrift 生成的client异常类型
 * @param <R> 实现 {@link ThriftDecorator}的装饰类异常类型
 */
public class Swift2ThriftyExceptionTransformer<L extends Exception,R extends ThriftDecorator<? extends Exception>> 
			implements Function<L,R>{
	private final ThriftStructMetadata leftMetadata;
	private final ThriftyStructMetadata rightMetadata;
	public Swift2ThriftyExceptionTransformer(Class<L> left, Class<R> right) {
		checkArgument(isThriftException(left) && isThriftyException(right),
				"left must be Exception with @com.facebook.swift.codec.ThriftStruct annotation,right must be Exception implement Struct interface"	);
		this.leftMetadata = CATALOG.getThriftStructMetadata(left);
		this.rightMetadata = STRUCTS_CACHE.getUnchecked(checkNotNull(right,"right is null"));
	}
	@Override
	public R apply(L input) {
		if(null == input){
			return null;
		}
		Map<Short, TypeValue> data = getFieldValues(input,leftMetadata);
		return rightMetadata.construct(data);

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