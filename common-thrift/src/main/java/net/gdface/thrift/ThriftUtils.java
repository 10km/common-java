package net.gdface.thrift;

import static com.facebook.swift.codec.metadata.DecoratorThriftStructMetadata.STRUCT_TRANSFORMER;
import static com.facebook.swift.codec.metadata.FieldKind.THRIFT_FIELD;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

/**
 * thrift工具
 * @author guyadong
 *
 */
public class ThriftUtils {

	public static final ThriftCatalog CATALOG = new ThriftCatalog() {
		@Override
		public <T> ThriftStructMetadata getThriftStructMetadata(Type structType) {
			return STRUCT_TRANSFORMER.apply(super.getThriftStructMetadata(structType));
		}
	};
	public static final Set<Class<?>> THRIFT_BUILTIN_KNOWNTYPES = 
	ImmutableSet.of(
			boolean.class,
			byte.class,
			double.class,
			short.class,
			int.class,
			long.class,
			String.class,
			ByteBuffer.class,
			void.class,
			Boolean.class,
			Byte.class,
			Short.class,
			Integer.class,
			Long.class,
			Double.class);
	public static final Map<Class<?>,Class<?>> CAST_TYPES = 
	ImmutableMap.<Class<?>,Class<?>>builder()
	.put(byte[].class,ByteBuffer.class)
	.put(Date.class,Long.class)
	.put(java.sql.Date.class,Long.class)
	.put(java.sql.Time.class,Long.class)
	.put(float.class,double.class)
	.put(Float.class,Double.class)
	.build();

	public ThriftUtils() {
	}

	/**
	 * 构造{@code metadata}指定类型的实例并填充字段
	 * @param data
	 * @param metadata
	 * @return
	 * @throws Exception
	 */
	public static <T>T constructStruct(Map<Short, Object> data,ThriftStructMetadata metadata) 
		throws Exception{
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
		return fillStructField(data,metadata,instance);
	}
	/**
	 * 填充{@code instance}实例的字段
	 * @param data
	 * @param metadata
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T>T fillStructField(Map<Short, Object> data,ThriftStructMetadata metadata,Object instance) 
		throws Exception{
		checkArgument(null != instance,"instance is null");
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

	/**
	 * 获取{@code field}指定的字段值
	 * @param instance
	 * @param field
	 * @return
	 * @throws Exception
	 */
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

	/**
	 * 根据{@code metadata}类型数据获取{@code instance}实例所有的字段值
	 * @param instance
	 * @param metadata
	 * @return 字段值映射表
	 */
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

	public static boolean isThriftStruct(Type type){
		return type instanceof Class<?> 
			? ((Class<?>)type).isAnnotationPresent(ThriftStruct.class) 
			: false;
	}

	public static boolean isThriftDecorator(Type type){
		return type instanceof Class<?> 
				? ThriftDecorator.class.isAssignableFrom((Class<?>)type) 
				: false;
	}

	public static boolean isPrimitiveArray(Type type){
		if(type instanceof Class<?>){
			Class<?> clazz = (Class<?>)type;
			return clazz.isArray() && clazz.getComponentType().isPrimitive();
		}
		return false;
	}

	public static boolean isThriftBuildinType(Type type){		
		return THRIFT_BUILTIN_KNOWNTYPES.contains(type);
	}

	public static boolean isPrimitivefloat(Type type){
		return type == float.class;
	}

	public static boolean isCastType(Type type){
		return  CAST_TYPES.containsKey(type);
	}

	public static boolean isException(Type type){
		return null == type 
				? false 
				: Exception.class.isAssignableFrom(TypeToken.of(type).getRawType());
	}
	public static <T>Constructor<T> getConstructor(Class<T> clazz,Class<?>...parameterTypes){
		try {
			return clazz.getConstructor(parameterTypes);
		} catch (NoSuchMethodException e) {
			return null;
		} 
	}
	public static <T> boolean hasConstructor(Class<T> clazz,Class<?>...parameterTypes){
		return getConstructor(clazz,parameterTypes) != null;
	}
	public static boolean isThriftException(Type type){
		return isException(type) && isThriftStruct(type);
	}
	public static boolean isThriftException(Type left, Type right){
		return isThriftException(left) && isThriftException(right);
	}

	public static boolean needTransformer(Type type){
		return !isThriftBuildinType(type) && ! isPrimitivefloat(type);
	}
	public static interface Action{
		void doClass(Class<?> type);
	}
	public static void traverseTypes(Type type,Action action){
		checkArgument(null !=action,"action is null");
		if(type instanceof Class<?>){
			action.doClass((Class<?>) type);
		}else if( type instanceof ParameterizedType){
			ParameterizedType paramType = (ParameterizedType)type;
			Type rawType = paramType.getRawType();
			Type[] typeArgs = paramType.getActualTypeArguments();
			traverseTypes(rawType,action);
			for(Type arg:typeArgs){
				traverseTypes(arg,action);
			}
		}else if (type instanceof GenericArrayType) {
			traverseTypes(((GenericArrayType) type).getGenericComponentType(),action);
		} else if (type instanceof TypeVariable) {
			for (Type t : ((TypeVariable<?>) type).getBounds()) {
				traverseTypes(t,action);
			}
		} else if (type instanceof WildcardType) {
			for (Type t : ((WildcardType) type).getLowerBounds()) {
				traverseTypes(t,action);
			}
			for (Type t : ((WildcardType) type).getUpperBounds()) {
				traverseTypes(t,action);
			}
		} else{
			throw new IllegalArgumentException(String.format("not allow type %s", type.toString()));
		}
	}
}
