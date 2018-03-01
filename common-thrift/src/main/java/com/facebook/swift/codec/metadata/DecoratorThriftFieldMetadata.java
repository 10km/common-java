package com.facebook.swift.codec.metadata;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Type;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import com.facebook.swift.codec.ThriftField.Requiredness;
import com.facebook.swift.codec.metadata.ThriftFieldMetadata;
import com.facebook.swift.codec.metadata.ThriftInjection;
import com.facebook.swift.codec.metadata.ThriftParameterInjection;
import com.google.common.primitives.Primitives;

/**
 * {@link ThriftFieldMetadata}的代理类，
 * 重载{@link #getRequiredness()}方法，根据参数类型对返回值进行修改
 * @author guyadong
 *
 */
@Immutable
public class DecoratorThriftFieldMetadata extends ThriftFieldMetadata {
	private static final Logger logger = Logger.getLogger(DecoratorThriftFieldMetadata.class.getName());
    private static Boolean primitiveOptional = null;
    private final Type javaType;
	DecoratorThriftFieldMetadata(ThriftFieldMetadata input){
        super(
                input.getId(),
                input.getRequiredness(),
                input.getThriftType(),
                input.getName(),
                input.getType(),
                input.getInjections(),
                input.getConstructorInjection(),
                input.getMethodInjection(),
                input.getExtraction(),
                input.getCoercion());
		// 获取field的类型
		List<ThriftInjection> injections = getInjections();
		checkState(injections.size()>0,"invalid size of injections");
		ThriftInjection injection = injections.get(0);		
		if(injection instanceof ThriftParameterInjection){
			javaType = ((ThriftParameterInjection)injection).getJavaType();
		}else if(injection instanceof ThriftFieldInjection){
			javaType = ((ThriftFieldInjection)injection).getField().getType();
		}else{
			javaType = null;
			// 对于不支持的数据类型无法获取field类型，输出警告
			logger.warning(
					String.format("UNSUPPORED TYPE %s，can't get Java Type. "
							+ "(不识别的ThriftInjection实例类型，无法实现requiredness转义)",
					null == injection? null : injection.getClass().getName()));
		}
	}
	/** 重载方法,实现 requiredness 转义 */
	@Override
	public Requiredness getRequiredness() {
		Requiredness requiredness = super.getRequiredness();
		checkState(Requiredness.UNSPECIFIED != requiredness);
		// 当为primitive类型时，Requiredness 为REQUIRED
		// 当为primitive类型的Object封装类型时(Long,Integer,Boolean)，Requiredness为OPTIONAL
		if( !Boolean.FALSE.equals(primitiveOptional)
				&& javaType instanceof Class<?>
				&& requiredness == Requiredness.NONE){
			Class<?> parameterClass = (Class<?>)javaType;
			if(parameterClass.isPrimitive()){
				requiredness = Requiredness.REQUIRED;
				// logger.info(String.format("%s %s", parameterClass.getSimpleName(),requiredness));
			}else if(Primitives.isWrapperType(parameterClass)){
				requiredness = Requiredness.OPTIONAL;
				// logger.info(String.format("%s %s", parameterClass.getSimpleName(),requiredness));
			}
		}
		return requiredness;
	}
    /**
	 * 设置optional标记<br>
	 * 指定{@link #getRequiredness}方法调用时是否对primitive类型及其封装类型(Integer,Long)参数的返回值进行替换<br>
	 * 默认值:{@code true}<br>
	 * 该方法只能被调用一次
	 * @param optional
	 * @see #getRequiredness()
	 * @throws IllegalStateException 方法已经被调用
	 */
	public static synchronized void setPrimitiveOptional(boolean optional) {
		checkState(null == DecoratorThriftFieldMetadata.primitiveOptional,"primitiveOptional is initialized already.");
		DecoratorThriftFieldMetadata.primitiveOptional = optional;
	}
}
