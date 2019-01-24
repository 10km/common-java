package net.gdface.thrift;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftyStructMetadata.STRUCTS_CACHE;

import java.util.Map;
import com.facebook.swift.codec.metadata.ThriftStructMetadata;
import com.google.common.base.Function;
import com.microsoft.thrifty.Struct;

import static net.gdface.thrift.ThriftUtils.*;
/**
 * 有{@link com.facebook.swift.codec.ThriftStruct}注释的类型之间的转换
 * @author guyadong
 *
 * @param <L> 
 * @param <R>
 */
public class Swift2ThriftyStructTransformer<L,R extends Struct> implements Function<L,R>{
	private final ThriftStructMetadata leftMetadata;
	private final ThriftyStructMetadata rightMetadata;
	public Swift2ThriftyStructTransformer(Class<L> left, Class<R> right) {
		this.leftMetadata = CATALOG.getThriftStructMetadata(checkNotNull(left,"left is null"));
		this.rightMetadata = STRUCTS_CACHE.getUnchecked(checkNotNull(right,"right is null"));
	}
	@Override
	public R apply(L input) {
		if(null == input){
			return null;
		}
		Map<String, TypeValue> data = getFieldValues(input,leftMetadata);
		return rightMetadata.constructStruct(data);
	}
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder()
				.append("Swift2ThriftyStructTransformer [leftClass=")
				.append(leftMetadata.getStructType())
				.append(", rightClass=")
				.append(rightMetadata.getStructType())
				.append("]");
		return builder.toString();
	}
}