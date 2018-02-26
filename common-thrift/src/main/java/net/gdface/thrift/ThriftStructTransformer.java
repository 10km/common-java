package net.gdface.thrift;

import static com.facebook.swift.codec.metadata.FieldKind.THRIFT_FIELD;
import static java.lang.String.format;
import static com.google.common.base.Preconditions.*;
import static com.facebook.swift.codec.metadata.DecoratorThriftStructMetadata.STRUCT_TRANSFORMER;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.protocol.TProtocolException;

import com.facebook.swift.codec.ThriftField.Requiredness;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.codec.metadata.ThriftConstructorInjection;
import com.facebook.swift.codec.metadata.ThriftExtraction;
import com.facebook.swift.codec.metadata.ThriftFieldExtractor;
import com.facebook.swift.codec.metadata.ThriftFieldInjection;
import com.facebook.swift.codec.metadata.ThriftFieldMetadata;
import com.facebook.swift.codec.metadata.ThriftInjection;
import com.facebook.swift.codec.metadata.ThriftMethodExtractor;
import com.facebook.swift.codec.metadata.ThriftMethodInjection;
import com.facebook.swift.codec.metadata.ThriftParameterInjection;
import com.facebook.swift.codec.metadata.ThriftStructMetadata;
import com.google.common.base.Function;
import com.google.common.base.Throwables;

/**
 * 有{@link com.facebook.swift.codec.ThriftStruct}注释的类型之间的转换
 * @author guyadong
 *
 * @param <L> 
 * @param <R>
 */
public class ThriftStructTransformer<L,R> implements Function<L,R>{
	private static final ThriftCatalog CATALOG = new ThriftCatalog() {
    	@Override
    	public <T> ThriftStructMetadata getThriftStructMetadata(Type structType) {
    		return STRUCT_TRANSFORMER.apply(super.getThriftStructMetadata(structType));
    	}
    };
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
			Map<Short, Object> data = getFiledValues(input,leftMetadata);
			return constructStruct(data,rightMetadata);
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
	public static boolean isThriftStruct(Type type){
		return type instanceof Class<?> 
			? ((Class<?>)type).getAnnotation(ThriftStruct.class) != null 
			: false;
	}
	@SuppressWarnings("unchecked")
    public static <T>T constructStruct(Map<Short, Object> data,ThriftStructMetadata metadata) 
    	throws Exception{
        // construct instance
        Object instance;
        {
            ThriftConstructorInjection constructor = metadata.getConstructorInjection().get();
            Object[] parametersValues = new Object[constructor.getParameters().size()];
            for (ThriftParameterInjection parameter : constructor.getParameters()) {
                Object value = data.get(parameter.getId());
                parametersValues[parameter.getParameterIndex()] = value;
            }

            try {
                instance = constructor.getConstructor().newInstance(parametersValues);
            } catch (InvocationTargetException e) {
                if (e.getTargetException() != null) {
                	Throwables.throwIfUnchecked(e.getTargetException());
                	throw new RuntimeException(e.getTargetException());
                }
                throw e;
            } 
        }

        // inject fields
        for (ThriftFieldMetadata fieldMetadata : metadata.getFields(THRIFT_FIELD)) {
            for (ThriftInjection injection : fieldMetadata.getInjections()) {
                if (injection instanceof ThriftFieldInjection) {
                    ThriftFieldInjection fieldInjection = (ThriftFieldInjection) injection;
                    Object value = data.get(fieldInjection.getId());
                    if (value != null) {
                        fieldInjection.getField().set(instance, value);
                    }
                }
            }
        }

        // inject methods
        for (ThriftMethodInjection methodInjection : metadata.getMethodInjections()) {
            boolean shouldInvoke = false;
            Object[] parametersValues = new Object[methodInjection.getParameters().size()];
            for (ThriftParameterInjection parameter : methodInjection.getParameters()) {
                Object value = data.get(parameter.getId());
                if (value != null) {
                    parametersValues[parameter.getParameterIndex()] = value;
                    shouldInvoke = true;
                }
            }

            if (shouldInvoke) {
                try {
                    methodInjection.getMethod().invoke(instance, parametersValues);
                }
                catch (InvocationTargetException e) {
                    if (e.getTargetException() != null) {
                    	Throwables.throwIfUnchecked(e.getTargetException());
                    	throw new RuntimeException(e.getTargetException());
                    }
                    throw e;
                }
            }
        }

        // builder method
        if (metadata.getBuilderMethod().isPresent()) {
            ThriftMethodInjection builderMethod = metadata.getBuilderMethod().get();
            Object[] parametersValues = new Object[builderMethod.getParameters().size()];
            for (ThriftParameterInjection parameter : builderMethod.getParameters()) {
                Object value = data.get(parameter.getId());
                parametersValues[parameter.getParameterIndex()] = value;
            }

            try {
                instance = builderMethod.getMethod().invoke(instance, parametersValues);
                if (instance == null) {
                    throw new IllegalArgumentException("Builder method returned a null instance");

                }
                if (!metadata.getStructClass().isInstance(instance)) {
                    throw new IllegalArgumentException(format("Builder method returned instance of type %s, but an instance of %s is required",
                            instance.getClass().getName(),
                            metadata.getStructClass().getName()));
                }
            }
            catch (InvocationTargetException e) {
                if (e.getTargetException() != null) {
                	Throwables.throwIfUnchecked(e.getTargetException());
                	throw new RuntimeException(e.getTargetException());
                }
                throw e;
            }
        }
        return (T) instance;
    }
	public static Object getFieldValue(Object instance, ThriftFieldMetadata field) throws Exception {
		try {
			if (field.getExtraction().isPresent()) {
				ThriftExtraction extraction = field.getExtraction().get();
				if (extraction instanceof ThriftFieldExtractor) {
					ThriftFieldExtractor thriftFieldExtractor = (ThriftFieldExtractor) extraction;
					return thriftFieldExtractor.getField().get(instance);
				} else if (extraction instanceof ThriftMethodExtractor) {
					ThriftMethodExtractor thriftMethodExtractor = (ThriftMethodExtractor) extraction;
					return thriftMethodExtractor.getMethod().invoke(instance);
				}
				throw new IllegalAccessException("Unsupported field extractor type " + extraction.getClass().getName());
			}
			throw new IllegalAccessException("No extraction present for " + field);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				Throwables.throwIfInstanceOf(e.getTargetException(), Exception.class);
			}
			throw e;
		}
	}

	public static Map<Short, Object> getFiledValues(Object instance, ThriftStructMetadata metadata) {
		checkArgument(null != instance && null != metadata, "instance,metadata must not be null");
		Collection<ThriftFieldMetadata> fields = metadata.getFields(THRIFT_FIELD);
		Map<Short, Object> data = new HashMap<>(fields.size());
		for (ThriftFieldMetadata field : metadata.getFields()) {
			try {
				Object value = getFieldValue(instance, field);
				if (value == null) {
					if (field.getRequiredness() == Requiredness.REQUIRED) {
						throw new TProtocolException("required field was not set");
					} else {
						continue;
					}
				}
				data.put(field.getId(), value);
			} catch (Exception e) {
				Throwables.throwIfUnchecked(e);
				throw new RuntimeException(e);
			}
		}
		return data;
	}
}