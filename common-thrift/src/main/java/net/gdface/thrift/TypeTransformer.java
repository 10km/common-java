package net.gdface.thrift;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;

import net.gdface.utils.BaseTypeTransformer;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftUtils.*;
/**
 * 类型转换工具类
 * @author guyadong
 *
 */
public class TypeTransformer extends BaseTypeTransformer{
	private final Table<Class<?>,Class<?>,Function<?,?>> transTable = HashBasedTable.create();
	private static TypeTransformer instance = new TypeTransformer();
	public static TypeTransformer getInstance() {
		return instance;
	}
	public static synchronized void setInstance(TypeTransformer instance) {
		TypeTransformer.instance = checkNotNull(instance,"instance is null");
	}
	protected TypeTransformer() {
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <L,R>Function<L,R> getTransformer(Class<L>left,Class<R>right){
		Function<L, R> result = super.getTransformer(left,right);	
		if (null == result) {
			if (Enum.class.isAssignableFrom(left) && Enum.class.isAssignableFrom(left)) {
				// 添加枚举类型转换
				synchronized (this.transTable) {
					// double checking
					if (null == (result = (Function<L, R>) this.transTable.get(left, right))) {
						result = new EnumTransformer((Class<? extends Enum<?>>) left,
								(Class<? extends Enum<?>>) right);
						setTransformer(left, right, (Function<L,R>)result);
					}
				}
			}else if(isThriftStruct(left) && isThriftStruct(right)){
				// 添加 ThriftStuct对象之间的转换
				synchronized (this.transTable) {
					// double checking
					if (null == (result = (Function<L, R>) this.transTable.get(left, right))) {
						if(isThriftException(left,right)){
							result = new ThriftExceptionTransformer(left,right);
						}else{
							result = new ThriftStructTransformer(left,right);
						}
						setTransformer(left, right, result);
					}
				}
			}else if(isThriftDecoratorPair(left,right) ){
				// 添加thrift decorator对象和被装饰对象之间的转换	
				updateThriftDecoatorTransformer(right,(Class<? extends ThriftDecorator>)left);
				result = (Function<L, R>) this.transTable.get(left, right);
			}else if(isThriftDecoratorPair(right,left) ){
				// 添加thrift decorator对象和被装饰对象之间的转换	
				updateThriftDecoatorTransformer(left,(Class<? extends ThriftDecorator>)right);
				result = (Function<L, R>) this.transTable.get(left, right);
			}else if(isThriftClientPair(left,right)){
				// 添加原始对象和thrift decorator对象之间的转换	
				updateThriftClientTypeTransformer(left,right);
				result = (Function<L, R>) this.transTable.get(left, right);
			}else if(isThriftClientPair(right,left)){
				// 添加原始对象和thrift decorator对象之间的转换	
				updateThriftClientTypeTransformer(right,left);
				result = (Function<L, R>) this.transTable.get(left, right);
			}
		}
		return result;
	}
	private <L,R extends ThriftDecorator<L>>void updateThriftDecoatorTransformer(Class<L>left,Class<R>right){
		synchronized (this.transTable) {
			// double checking
			if (null == this.transTable.get(left, right)) {
				ThriftDecoratorTransformer<L,R> trans = new ThriftDecoratorTransformer<L,R>(left,right);
				setTransformer(left, right, trans.toDecoratorFun);
				setTransformer(right,left, trans.toDelegateFun);
			}
		}
	}
	private <L,M extends ThriftDecorator<L>,R>void updateThriftClientTypeTransformer(Class<L>left,Class<R>right){
		synchronized (this.transTable) {
			// double checking
			if (null == this.transTable.get(left, right)) {
				ThriftClientTypeTransformer<L, M, R> trans = new ThriftClientTypeTransformer<L,M,R>(left,right);
				setTransformer(left, right, trans.toThriftClientTypeFun);
				setTransformer(right,left, trans.toOriginalTypeFun);
			}
		}
	}
	/**
	 * 将{@code value}转换为{@code right}指定的类型
	 * @param value
	 * @param left {@code value}的原类型
	 * @param right 目标类型
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <L,R>R cast(Object value,Type left,Type right){
		if(null == left){
			return null;
		}
		TypeToken<?> leftToken = TypeToken.of(checkNotNull(left));
		TypeToken<?> rightToken = TypeToken.of(checkNotNull(right));
		if(left.equals(right)){
			return (R) value;
		}
		// object to object
		if((((Class<L>)left).isAssignableFrom(value.getClass()) 
				&& left instanceof Class<?>)
				&& (right instanceof Class<?>)){
			return to((L)value,(Class<L>)left, (Class<R>)right);
		}
		// list to array
		if(List.class.isAssignableFrom(leftToken.getRawType()) && null != rightToken.getComponentType()){
			Class<?> componentType = rightToken.getComponentType().getRawType();
			if(componentType == int.class){
				return (R) tointArray((List<Integer>)value, Integer.class, int.class);
			}
			if(componentType == long.class){
				return (R) tolongArray((List<Long>)value, Long.class, long.class);
			}
			if(componentType == double.class){
				return (R) todoubleArray((List<Double>)value, Double.class, double.class);
			}
			if(componentType == float.class){
				return (R) tofloatArray((List<Double>)value, Double.class, float.class);
			}
			if(componentType == short.class){
				return (R) toshortArray((List<Short>)value, Short.class, short.class);
			}
			if(componentType == boolean.class){
				return (R) tobooleanArray((List<Boolean>)value, Boolean.class, boolean.class);
			}
			Type elementType = ((ParameterizedType)left).getActualTypeArguments()[0];
			if(elementType instanceof Class<?>){
				return (R) toArray((List<L>)value,(Class<L>)elementType,(Class<R>)componentType);
			}
			throw new UnsupportedOperationException(String.format("unsupported cast %s to %s",left,right));
		}
		// list to list
		if(List.class.isAssignableFrom(leftToken.getRawType()) && List.class == rightToken.getRawType()){
			checkArgument(value instanceof List);
			Type leftElementType = ((ParameterizedType)left).getActualTypeArguments()[0];
			Type rightElementType = ((ParameterizedType)right).getActualTypeArguments()[0];
			if(leftElementType instanceof Class<?>&& rightElementType instanceof Class<?>){
				return (R) to((List<L>)value,(Class<L>)leftElementType,(Class<R>)rightElementType);
			}
			throw new UnsupportedOperationException(String.format("unsupported cast %s to %s",left,right));
		}
		// set to set
		if(Set.class.isAssignableFrom(leftToken.getRawType()) && Set.class == rightToken.getRawType()){
			checkArgument(value instanceof Set);
			Type leftElementType = ((ParameterizedType)left).getActualTypeArguments()[0];
			Type rightElementType = ((ParameterizedType)right).getActualTypeArguments()[0];
			if(leftElementType instanceof Class<?>&& rightElementType instanceof Class<?>){
				return (R) to((Set<L>)value,(Class<L>)leftElementType,(Class<R>)rightElementType);
			}
			throw new UnsupportedOperationException(String.format("unsupported cast %s to %s",left,right));
		}
		// map to map
		if(Map.class.isAssignableFrom(leftToken.getRawType()) && Map.class == rightToken.getRawType()){
			Type k1Type = ((ParameterizedType)left).getActualTypeArguments()[0];
			Type v1Type = ((ParameterizedType)left).getActualTypeArguments()[1];
			Type k2Type = ((ParameterizedType)right).getActualTypeArguments()[0];
			Type v2Type = ((ParameterizedType)right).getActualTypeArguments()[1];
			if(k1Type instanceof Class<?> 
				&& v1Type instanceof Class<?> 
				&& k2Type instanceof Class<?> 
				&& v2Type instanceof Class<?>){
				to((Map)value,(Class<?>)k1Type,(Class<?>)v1Type,(Class<?>)k2Type,(Class<?>)v2Type);
			}
			throw new UnsupportedOperationException(String.format("unsupported cast %s to %s",left,right));
		}
		// array to list
		if(leftToken.isArray() && List.class == rightToken.getRawType()){
			checkArgument(value.getClass().isArray());
			Type componentType = leftToken.getComponentType().getType();
			if(componentType == int.class){
				return (R) to((int[])value, int.class, Integer.class);
			}
			if(componentType == long.class){
				return (R) to((long[])value, long.class, Long.class);
			}
			if(componentType == double.class){
				return (R) to((double[])value, double.class, Double.class);
			}
			if(componentType == float.class){
				return (R) to((float[])value, float.class, Double.class);
			}
			if(componentType == short.class){
				return (R) to((short[])value, short.class, Short.class);
			}
			if(componentType == boolean.class){
				return (R) to((boolean[])value, boolean.class, Boolean.class);
			}
			Type rightElementType = ((ParameterizedType)right).getActualTypeArguments()[0];
			if((leftToken.getComponentType().getType()) instanceof Class<?> 
				&& rightElementType instanceof Class<?>){
				return (R) to((L[])value,(Class<L>)componentType,(Class<R>)rightElementType);
			}
			throw new UnsupportedOperationException(String.format("unsupported cast %s to %s",left,right));
		}
		throw new UnsupportedOperationException(String.format("unsupported cast %s to %s",left,right));
	}
	/**
	 * 将{@code value}转换为{@code right}指定的类型
	 * @param value
	 * @param left {@code value}的原类型
	 * @param right 目标类型
	 * @return
	 */
    /**
     * 原始类型和thrift client 类型之间的转换器,需要2将完成转换，如果{@code L->M->R}
     * @author guyadong
     *
     * @param <L> 原始类型
     * @param <M>中间类型deorator
     * @param <R> thrift client 类型
     */
    public class ThriftClientTypeTransformer<L,M extends ThriftDecorator<L>,R>  {
		private final Function<L, M> left2Middle;
		private final Function<M, R> middle2Right;
		private final Function<M, L> middle2Left;
		private final Function<R, M> right2Middle;
    	public ThriftClientTypeTransformer(Class<L> left, Class<R> right) {
    		checkArgument(null != left && null != right,"left,middle,right must not be null");
    		Class<M> middClass = getMiddleClassChecked(left,right);
    		left2Middle = getTransformerChecked(left,middClass);
    		middle2Right = getTransformerChecked(middClass,right);
    		middle2Left = getTransformerChecked(middClass,left);
    		right2Middle = getTransformerChecked(right,middClass);
    	}
    	public final Function<L,R> toThriftClientTypeFun = new Function<L,R>(){
			@Override
			public R apply(L input) {
				M m = left2Middle.apply(input);
				return middle2Right.apply(m);
			}};
    	public final Function<R,L> toOriginalTypeFun = new Function<R,L>(){
			@Override
			public L apply(R input) {
				M m = right2Middle.apply(input);
				return middle2Left.apply(m);
			}};
    }
}
