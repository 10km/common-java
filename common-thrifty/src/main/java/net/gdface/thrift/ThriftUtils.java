package net.gdface.thrift;

import static com.facebook.swift.codec.metadata.FieldKind.THRIFT_FIELD;
import static com.google.common.base.Preconditions.*;
import static java.lang.String.format;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

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
import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.thrifty.Struct;
import com.microsoft.thrifty.ThriftException;

/**
 * thrift工具
 * @author guyadong
 *
 */
public class ThriftUtils {
	public static final ThriftCatalog CATALOG = new ThriftCatalogWithTransformer();
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
	public static <T>T constructStruct(Map<String, TypeValue> data,ThriftStructMetadata metadata) 
		throws Exception{
		T instance;
	    {
	        ThriftConstructorInjection constructor = metadata.getConstructorInjection().get();
	        Type[] dstTypes = constructor.getConstructor().getGenericParameterTypes();
	        Type[] srcTypes = new Type[constructor.getParameters().size()];
	        Object[] parametersValues = new Object[constructor.getParameters().size()];
	        checkState(dstTypes.length == parametersValues.length);
	        for (ThriftParameterInjection parameter : constructor.getParameters()) {
	        	TypeValue value = data.get(parameter.getId());
	            parametersValues[parameter.getParameterIndex()] = value.value;
	            srcTypes[parameter.getParameterIndex()] = value.type;
	        }
	        for(int i =0;i<dstTypes.length;++i){
	        	parametersValues[i] = TypeTransformer.getInstance().cast(parametersValues[i], srcTypes[i], dstTypes[i]);
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
	public static <T>T fillStructField(Map<String, TypeValue> data,ThriftStructMetadata metadata,T instance) 
		throws Exception{
		checkArgument(null != instance,"instance is null");
		// inject fields
	    for (ThriftFieldMetadata fieldMetadata : metadata.getFields(THRIFT_FIELD)) {
	        for (ThriftInjection injection : fieldMetadata.getInjections()) {
	            if (injection instanceof ThriftFieldInjection) {
	                ThriftFieldInjection fieldInjection = (ThriftFieldInjection) injection;	                
	                TypeValue value = data.get(fieldInjection.getName());
	                if (value != null) {
	                	Field f = fieldInjection.getField();
	                    f.set(instance, TypeTransformer.getInstance().cast(value.value,value.type,f.getGenericType()));
	                }
	            }
	        }
	    }
	
	    // inject methods
	    for (ThriftMethodInjection methodInjection : metadata.getMethodInjections()) {
	        boolean shouldInvoke = false;
	        Object[] parametersValues = new Object[methodInjection.getParameters().size()];
	        Type[] srcTypes = new Type[methodInjection.getParameters().size()];
	        for (ThriftParameterInjection parameter : methodInjection.getParameters()) {
	        	TypeValue value = data.get(parameter.getName());
	            if (value != null) {
	                parametersValues[parameter.getParameterIndex()] = value.value;
	                srcTypes[parameter.getParameterIndex()] = value.type;
	                shouldInvoke = true;
	            }
	        }
	
	        if (shouldInvoke) {
	            try {	            	
	            	Method method = methodInjection.getMethod();
	            	Type[] parameterTypes = method.getGenericParameterTypes();
	            	for(int i = 0 ;i<parametersValues.length;++i){
	            		parametersValues[i]=TypeTransformer.getInstance().cast(parametersValues[i], srcTypes[i], parameterTypes[i]);
	            	}
	            	method.invoke(instance, parametersValues);
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
	            Object value = data.get(parameter.getName());
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
	public static TypeValue getFieldValue(Object instance, ThriftFieldMetadata field) throws Exception {
		try {
			if (field.getExtraction().isPresent()) {
				ThriftExtraction extraction = field.getExtraction().get();
				if (extraction instanceof ThriftFieldExtractor) {
					ThriftFieldExtractor thriftFieldExtractor = (ThriftFieldExtractor) extraction;
					Field f = thriftFieldExtractor.getField();
					return new TypeValue(f.getGenericType(),f.get(instance));
				} else if (extraction instanceof ThriftMethodExtractor) {
					ThriftMethodExtractor thriftMethodExtractor = (ThriftMethodExtractor) extraction;
					Method method = thriftMethodExtractor.getMethod();
					return new TypeValue(method.getGenericReturnType(),method.invoke(instance));
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
	public static Map<String, TypeValue> getFieldValues(Object instance, ThriftStructMetadata metadata) {
		checkArgument(null != instance && null != metadata && metadata.getStructClass().isInstance(instance), 
				"instance,metadata must not be null");
		
		Collection<ThriftFieldMetadata> fields = metadata.getFields(THRIFT_FIELD);
		Map<String, TypeValue> data = new HashMap<>(fields.size());
		for (ThriftFieldMetadata field : fields) {
			try {
	            // is the field readable?
	            if (field.isWriteOnly()) {
	                continue;
	            }
				TypeValue value = getFieldValue(instance, field);
				if (value.value == null) {
					if (field.getRequiredness() == Requiredness.REQUIRED) {
						throw new RuntimeException("required field was not set");
					} else {
						continue;
					}
				}
				data.put(field.getName(), value);
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
	public static boolean isThriftyStruct(Type type){
		return type instanceof Class<?> 
			? Struct.class.isAssignableFrom((Class<?>)type)
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

	public static boolean isPrimitivefloat(Type type){
		return type == float.class;
	}

	public static boolean isException(Type type){
		return null == type 
				? false 
				: Exception.class.isAssignableFrom(TypeToken.of(type).getRawType());
	}
	public static boolean isThriftException(Type type){
		return isException(type) && isThriftStruct(type);
	}
	public static boolean isThriftyException(Type type){
		return isException(type) && isThriftyStruct(type);
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
		if(isThriftyStruct(right)){
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
	/** 避免{@code null}抛出异常 */
	public static <T> T returnNull(ThriftException e){
		if(e.kind == ThriftException.Kind.MISSING_RESULT  ){
            return null;
        }
	    throw e;
	}
	/** 避免{@code null}抛出异常 
	 * @throws Throwable */
	public static <T> T returnNull(Throwable e) throws Throwable{
		if(e instanceof ThriftException){
			return returnNull((ThriftException)e);
		}
	    throw e;
	}
	public static<V> void addCallback(
			final ListenableFuture<V> future,
			final FutureCallback<? super V> callback,Executor executor) {
		checkArgument(null != callback,"callback is null");
		checkArgument(null != executor,"executor is null");
		Runnable callbackListener =
				new Runnable() {
			@Override
			public void run() {
				V value;
				try {
					value = Futures.getDone(future);
				} catch (ExecutionException e) {
					try{
						// value is null
						value = returnNull(e.getCause()); 
					}catch(Throwable t){
						callback.onFailure(t);
						return;
					}                    
				} catch (Throwable e) {
					callback.onFailure(e);
					return;
				}
				callback.onSuccess(value);
			}
		};
		future.addListener(callbackListener, executor);
	}
}
