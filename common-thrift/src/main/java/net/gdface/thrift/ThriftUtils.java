package net.gdface.thrift;

import static com.facebook.swift.codec.metadata.FieldKind.THRIFT_FIELD;
import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TProtocolException;

import com.facebook.swift.codec.ThriftField.Requiredness;
import com.facebook.swift.codec.ThriftStruct;
import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.codec.metadata.ThriftCatalogWithTransformer;
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
import com.facebook.swift.service.RuntimeTApplicationException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

/**
 * thrift工具
 * @author guyadong
 *
 */
public class ThriftUtils {

	public static final ThriftCatalog CATALOG = new ThriftCatalogWithTransformer();
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
	public static final String DECORATOR_PKG_SUFFIX="decorator";
	public static final String CLIENT_SUFFIX="client";
	public static final String DECORATOR_CLIENT_PKG_SUFFIX= DECORATOR_PKG_SUFFIX + "." + CLIENT_SUFFIX;
	public ThriftUtils() {
	}

	/**
	 * 构造{@code metadata}指定类型的实例并填充字段
	 * 参见 {@link com.facebook.swift.codec.internal.reflection.ReflectionThriftStructCodec#constructStruct(Map<Short, Object>)}
	 * @param data
	 * @param metadata
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T>T constructStruct(Map<Short, Object> data,ThriftStructMetadata metadata) 
		throws Exception{
		T instance;
	    {
	        ThriftConstructorInjection constructor = metadata.getConstructorInjection().get();
	        Object[] parametersValues = new Object[constructor.getParameters().size()];
	        for (ThriftParameterInjection parameter : constructor.getParameters()) {
	            Object value = data.get(parameter.getId());
	            parametersValues[parameter.getParameterIndex()] = value;
	        }
	
	        try {
	            instance = (T) constructor.getConstructor().newInstance(parametersValues);
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
	 * 填充{@code instance}实例的字段<br>
	 * 参见 {@link com.facebook.swift.codec.internal.reflection.ReflectionThriftStructCodec#constructStruct(Map<Short, Object>)}
	 * @param data
	 * @param metadata
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T>T fillStructField(Map<Short, Object> data,ThriftStructMetadata metadata,T instance) 
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
	            instance = (T) builderMethod.getMethod().invoke(instance, parametersValues);
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
	 * 获取{@code field}指定的字段值<br>
	 * 
	 * @param instance
	 * @param field
	 * @return
	 * @throws Exception
	 * @see {@link com.facebook.swift.codec.internal.reflection.AbstractReflectionThriftCodec#getFieldValue(Object, ThriftFieldMetadata)}
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
	 * 参见 {@link com.facebook.swift.codec.internal.reflection.ReflectionThriftStructCodec#write(Object, org.apache.thrift.protocol.TProtocol)}
	 * @param instance
	 * @param metadata
	 * @return 字段值映射表
	 */
	public static Map<Short, Object> getFiledValues(Object instance, ThriftStructMetadata metadata) {
		checkArgument(null != instance && null != metadata && metadata.getStructClass().isInstance(instance), 
				"instance,metadata must not be null");
		
		Collection<ThriftFieldMetadata> fields = metadata.getFields(THRIFT_FIELD);
		Map<Short, Object> data = new HashMap<>(fields.size());
		for (ThriftFieldMetadata field : fields) {
			try {
	            // is the field readable?
	            if (field.isWriteOnly()) {
	                continue;
	            }
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
	@SuppressWarnings("serial")
	public static <K, V> TypeToken<Map<K, V>> mapToken(TypeToken<K> keyToken, TypeToken<V> valueToken) {
		  return new TypeToken<Map<K, V>>() {}
		    .where(new TypeParameter<K>() {}, keyToken)
		    .where(new TypeParameter<V>() {}, valueToken);
	}
	@SuppressWarnings("serial")
	public static <T> TypeToken<List<T>> listToken(TypeToken<T> keyToken) {
		  return new TypeToken<List<T>>() {}
		    .where(new TypeParameter<T>() {}, keyToken);
	}
	@SuppressWarnings("serial")
	public static <T> TypeToken<Set<T>> setToken(TypeToken<T> keyToken) {
		  return new TypeToken<Set<T>>() {}
		    .where(new TypeParameter<T>() {}, keyToken);
	}

	/**
	 * @param type
	 * @return
	 * @see #getDecoratorType(Type)
	 */
	public static boolean hasDecoratorType(Type type){
		return getDecoratorType(type) !=null;
	}
	/**
	 * @param type
	 * @return
	 * @see #getDecoratorType(Class)
	 */
	@SuppressWarnings("unchecked")
	public static <T>Class<? extends ThriftDecorator<T>> getDecoratorType(Type type){
		if(!isThriftStruct(type)){
			return getDecoratorType((Class<T>)type);
		}
		return null;
	}
	/**
	 * 返回{@code clazz}对应的装饰类
	 * @param clazz
	 * @return 如果没有装饰类则返回{@code null}
	 */
	@SuppressWarnings("unchecked")
	public static <T,D extends ThriftDecorator<T>>Class<D> getDecoratorType(Class<T> clazz){
		if(!isThriftStruct(clazz)){
			String decoratorClazzName = clazz.getPackage().getName() 
					+ "."
					+ DECORATOR_CLIENT_PKG_SUFFIX 
					+ "." 
					+ clazz.getSimpleName();
			try {
				Class<?> decoratorClazz = Class.forName(decoratorClazzName);
				checkState(isThriftDecoratorPair(decoratorClazz,clazz),
						"%s must immplement %s",
						decoratorClazz.getName(),
						ThriftDecorator.class.getName());
				return (Class<D>) decoratorClazz;
			} catch (ClassNotFoundException e) {
			}
		}
		return null;
	}

	/**
	 * 判断 {@code left & right}之间是否为装饰类和被装饰类关系
	 * @param left 装饰类
	 * @param right 补装饰类
	 * @return
	 */
	public static <L,R>boolean isThriftDecoratorPair(Class<L>left,Class<R>right){
		try {
			return isThriftDecorator(left) 
					&& left.getMethod("delegate").getReturnType() == right;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 判断 {@code right}是否为{@code left}对应的client端存根类型
	 * @param left
	 * @param right
	 * @return
	 */
	public static <L,R>boolean isThriftClientPair(Class<L>left,Class<R>right){
		return getMiddleClass(left,right)!=null;
	}
	/**
	 * 返回 {@code left & right}之间的decorator类型
	 * @param left 原始类型
	 * @param right {@code left}对应的client端存根类型
	 * @return
	 */
	public static <L,M extends ThriftDecorator<L>,R>Class<M> getMiddleClass(Class<L>left,Class<R>right){
		if(isThriftStruct(right)){
			Class<M> decoratorClass = getDecoratorType(left);
			if(null != decoratorClass && decoratorClass.getSimpleName().equals(left.getSimpleName())){
				return decoratorClass;
			}			
		}
		return null;
	}
	/**
	 * @param left
	 * @param right
	 * @return
	 * @see #getMiddleClass(Class, Class)
	 */
	@SuppressWarnings("unchecked")
	public static <L,M extends ThriftDecorator<L>,R>Class<M> getMiddleClassChecked(Class<L>left,Class<R>right){
		return (Class<M>) checkNotNull(
						getMiddleClass(left,right),
						"NOT FOUND decorator class for %s",
						left.getName());
	}
	public static final String ISLOCAL_METHOD_NAME = "isLocal";
	public static boolean isIsLocalMethod(Method method){
		if(null == method){
			return false;
		}
		return method.getName().equals(ISLOCAL_METHOD_NAME)
				&& method.getParameterTypes().length == 0 
				&& method.getExceptionTypes().length == 0
				&& method.getReturnType() == boolean.class;
	}

	/** 避免{@code null}抛出异常 */
	public static <T> T returnNull(RuntimeTApplicationException e){
	    Throwable cause = e.getCause();
	    if (cause instanceof TApplicationException  
	            && ((TApplicationException) cause).getType() == TApplicationException.MISSING_RESULT){
	        return null;
	    }
	    throw e;
	}
}
