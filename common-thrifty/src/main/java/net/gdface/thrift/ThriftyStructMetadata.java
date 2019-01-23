package net.gdface.thrift;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static net.gdface.thrift.TypeTransformer.getInstance;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.microsoft.thrifty.Struct;
import com.microsoft.thrifty.StructBuilder;
import com.microsoft.thrifty.ThriftField;

@Immutable
public class ThriftyStructMetadata {
    public static final LoadingCache<Class<?>,ThriftyStructMetadata> 
	STRUCTS_CACHE = 
		CacheBuilder.newBuilder().build(
				new CacheLoader<Class<?>,ThriftyStructMetadata>(){
					@Override
					public ThriftyStructMetadata load(Class<?> key) throws Exception {
						return new ThriftyStructMetadata(key);
					}});
	private final Class<?> structType;
	/** 字段ID对应的字段对象 */
	private final ImmutableMap<Short, Field> fields;
	/** builder对象所有字段的set方法 */
	private final ImmutableMap<Short, Method> buildSetters;
	private final Class<? extends StructBuilder<?>> builderClass;
	private final Method buildMethod;
	@SuppressWarnings("unchecked")
	private ThriftyStructMetadata(Class<?> structType ) {
		this.structType = checkNotNull(structType,"struct is null");
		checkArgument(Struct.class.isAssignableFrom(structType),
				"structType %s not immplement the %s",structType.getName(),Struct.class.getName());
		try {
			String className = structType.getName() + ".Builder";
			builderClass = (Class<? extends StructBuilder<?>>) Class.forName(className);
		} catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);       
        } 
		ImmutableMap.Builder<Short, Field> fieldBuilder = ImmutableMap.builder();
		ImmutableMap.Builder<Short, Method> methodBuilder = ImmutableMap.builder();

		for(Field field:structType.getDeclaredFields()){
			ThriftField thriftField;
			if((thriftField = field.getAnnotation(ThriftField.class)) != null){
				fieldBuilder.put(thriftField.fieldId(), field);
				try {
					Method method = builderClass.getMethod(field.getName(), field.getType());
					methodBuilder.put(thriftField.fieldId(), method);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				} 
			}
		}
		fields = fieldBuilder.build();
		buildSetters = methodBuilder.build();
		
		try {
			buildMethod = builderClass.getDeclaredMethod("build");
		} catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);       
        } 
	}
	public ImmutableMap<Short, Field> getFields() {
		return fields;
	}
	public Class<?> getStructType() {
		return structType;
	}
	@SuppressWarnings("unchecked")
	public <L,R>void setValue(Object instance,short id,L value){
		checkNotNull(instance,"instance is null");
		checkArgument(structType.isInstance(instance),"invalid value type,required %s",structType.getName());
		Field field = fields.get(id);
		checkNotNull(field,"invalid field id=%s for %s",Short.toString(id),structType.getName());
		Class<?> fieldType = field.getType();
		try {
			field.setAccessible(true);
			if(value == null){
				checkArgument(!fieldType.isPrimitive(),
						"primitive field %s of %s required not null value",field.getName(),structType.getName());
				field.set(instance,null);
			}
			else{
				field.set(instance, getInstance().to(value, (Class<L>) value.getClass(), fieldType));
			}
		} catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        } 
	}
	@SuppressWarnings("unchecked")
	public <V>V getValue(Object instance,short id){
		checkNotNull(instance,"instance is null");
		checkArgument(structType.isInstance(instance),"invalid value type,required %s",structType.getName());

		Field field = fields.get(id);
		checkNotNull(field,"invalid field id=%s for %s",Short.toString(id),structType.getName());
		try {
			return (V) field.get(instance);
		} catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);       
        } 
	}
	public ThriftField getAnnotation(short id){
		Field field = fields.get(id);
		checkNotNull(field,"invalid field id=%s for %s",Short.toString(id),structType.getName());
		return field.getAnnotation(ThriftField.class);
	}
	/**
	 * 根据字段值构造实例
	 * @param fieldValues
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T>T constructStruct(Map<Short, TypeValue> fieldValues){
		checkNotNull(fieldValues,"fieldValues is null");
		try {
			// new Builder()
			StructBuilder<T> builder = (StructBuilder<T>) builderClass.newInstance();
			for(Entry<Short, TypeValue> entry : fieldValues.entrySet()){
				Method method = buildSetters.get(entry.getKey());
				checkNotNull(method,"method is null");
				TypeValue typeValue= entry.getValue();
				// 调用Builder的设置方法设置字段值
				method.invoke(builder, getInstance().cast(
						typeValue.value,
						typeValue.type,
						fields.get(entry.getKey()).getGenericType()));
			}
			// build()
			return (T) buildMethod.invoke(builder);
		} catch (InvocationTargetException e){
			Throwables.throwIfUnchecked(e.getCause());
            throw new RuntimeException(e.getCause());  
		}catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);       
        }
	}
	/**
	 * 从 {@link com.microsoft.thrifty.Struct} 实例中返回所有字段值
	 * @param instance
	 * @return
	 */
	Map<Short, TypeValue> getFieldValues(Object instance){
		checkNotNull(instance,"instance is null");
		checkArgument(structType.isInstance(instance),"invalid value type,required %s",structType.getName());
		ImmutableMap.Builder<Short, TypeValue> builder = ImmutableMap.builder();
		for(Entry<Short, Field> entry:fields.entrySet()){
			Short id = entry.getKey();
			Field field = entry.getValue();
			builder.put(id, new TypeValue(field.getType(), getValue(instance, id)));
		}		
		return builder.build();
	}
}
