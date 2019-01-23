package net.gdface.thrift;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftyStructMetadata.STRUCTS_CACHE;

import java.util.Map;
import com.facebook.swift.codec.metadata.ThriftStructMetadata;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.microsoft.thrifty.Struct;

import static net.gdface.thrift.ThriftUtils.*;
/**
 * 有{@link com.facebook.swift.codec.ThriftStruct}注释的类型之间的转换
 * @author guyadong
 *
 * @param <L> 
 * @param <R>
 */
public class Thrifty2SwiftStructTransformer<L extends Struct,R> implements Function<L,R>{
	private final ThriftyStructMetadata leftMetadata;
	private final ThriftStructMetadata rightMetadata;
	public Thrifty2SwiftStructTransformer(Class<L> left, Class<R> right) {
		this.leftMetadata = STRUCTS_CACHE.getUnchecked(checkNotNull(left,"right is null"));
		this.rightMetadata = CATALOG.getThriftStructMetadata(checkNotNull(right,"left is null"));
	}
	@Override
	public R apply(L input) {
		if(null == input){
			return null;
		}
		try {
			Map<Short, TypeValue> data = leftMetadata.getFieldValues(input);
			return constructStruct(data, rightMetadata);
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder()
				.append("Thrifty2SwiftStructTransformer [leftClass=")
				.append(leftMetadata.getStructType())
				.append(", rightClass=")
				.append(rightMetadata.getStructType())
				.append("]");
		return builder.toString();
	}
}