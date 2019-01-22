package net.gdface.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

/**
 * 对象类型相关工具
 * @author guyadong
 *
 */
public class TypeUtils {

	public TypeUtils() {
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

}
