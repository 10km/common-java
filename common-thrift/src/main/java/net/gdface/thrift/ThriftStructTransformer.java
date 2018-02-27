package net.gdface.thrift;

import static com.google.common.base.Preconditions.*;
import java.util.Map;

import com.facebook.swift.codec.metadata.ThriftStructMetadata;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import static net.gdface.thrift.ThriftUtils.CATALOG;
/**
 * 有{@link com.facebook.swift.codec.ThriftStruct}注释的类型之间的转换
 * @author guyadong
 *
 * @param <L> 
 * @param <R>
 */
public class ThriftStructTransformer<L,R> implements Function<L,R>{
	private final ThriftStructMetadata leftMetadata;
	private final ThriftStructMetadata rightMetadata;
	public ThriftStructTransformer(Class<L> left, Class<R> right) {
		this.leftMetadata= CATALOG.getThriftStructMetadata(checkNotNull(left,"left is null"));
		this.rightMetadata= CATALOG.getThriftStructMetadata(checkNotNull(right,"right is null"));
	}
	@Override
	public R apply(L input) {
		if(null == input){
			return null;
		}
		try {
			Map<Short, Object> data = ThriftUtils.getFiledValues(input,leftMetadata);
			return ThriftUtils.constructStruct(data,rightMetadata);
		} catch (Exception e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder()
				.append("ThriftStructTransformer [leftClass=")
				.append(leftMetadata.getStructType())
				.append(", rightClass=")
				.append(rightMetadata.getStructType())
				.append("]");
		return builder.toString();
	}
}