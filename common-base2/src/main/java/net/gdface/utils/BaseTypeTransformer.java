package net.gdface.utils;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.google.common.reflect.TypeToken;

import static com.google.common.base.Preconditions.*;
/**
 * 类型转换工具类
 * @author guyadong
 *
 */
public class BaseTypeTransformer {
	/**
	 * 返回buffer中所有字节(position~limit),不改变buffer状态
	 * @param buffer
	 * @return buffer 为 null 时返回 null 
	 */
	public static final byte[] getBytesInBuffer(ByteBuffer buffer){
		if(null == buffer){
			return null;
		}
		int pos = buffer.position();
		try{
			byte[] bytes = new byte[buffer.remaining()];
			buffer.get(bytes);
			return bytes;
		}finally{
			buffer.position(pos);
		}
	}
	@SuppressWarnings("rawtypes")
	private final Function sameTypeFun = new Function(){
		@Override
		public Object apply(Object input) {
			return input;
		}};
	private final Function<Boolean,Boolean> booleanTypeFun = new Function<Boolean,Boolean>(){
		@Override
		public Boolean apply(Boolean input) {
			return checkNotNull(input,"input is null").booleanValue();
		}};
	private final Function<Character,Character> charTypeFun = new Function<Character,Character>(){
		@Override
		public Character apply(Character input) {
			return checkNotNull(input,"input is null").charValue();
		}};		
	private final Function<Byte,Byte> byteTypeFun = new Function<Byte,Byte>(){
	@Override
	public Byte apply(Byte input) {
		return checkNotNull(input,"input is null").byteValue();
	}};	
	private final Function<Short,Short> shortTypeFun = new Function<Short,Short>(){
		@Override
		public Short apply(Short input) {
			return checkNotNull(input,"input is null").shortValue();
		}};		
	private final Function<Integer,Integer> intTypeFun = new Function<Integer,Integer>(){
		@Override
		public Integer apply(Integer input) {
			return checkNotNull(input,"input is null").intValue();
		}};
	private final Function<Long,Long> longTypeFun = new Function<Long,Long>(){
		@Override
		public Long apply(Long input) {
			return checkNotNull(input,"input is null").longValue();
		}};
	private final Function<Float,Float> floatTypeFun = new Function<Float,Float>(){
		@Override
		public Float apply(Float input) {
			return checkNotNull(input,"input is null").floatValue();
		}};
	private final Function<Double,Double> doubleTypeFun = new Function<Double,Double>(){
		@Override
		public Double apply(Double input) {
			Double.class.isPrimitive();
			return checkNotNull(input,"input is null").doubleValue();
		}};
	private final Function<Void,Void> voidTypeFun = new Function<Void,Void>(){
		@Override
		public Void apply(Void input) {
			return null;
		}};
	private final Function<byte[],ByteBuffer> byteArray2ByteBufferFun = new Function<byte[],ByteBuffer>(){
		@Override
		public ByteBuffer apply(byte[] input) {
			return null == input ? null :ByteBuffer.wrap(input);
		}};
	private final Function<ByteBuffer,byte[]> byteBuffer2ByteArrayFun = new Function<ByteBuffer,byte[]>(){
		@Override
		public byte[] apply(ByteBuffer input) {
			return getBytesInBuffer(input);
		}};
	private final Function<Float,Double> float2DoubleFun = new Function<Float,Double>(){
		@Override
		public Double apply(Float input) {
			return null == input ? null : input.doubleValue();
		}};
	private final Function<Double,Float> double2FloatFun = new Function<Double,Float>(){
		@Override
		public Float apply(Double input) {
			return null == input ? null : input.floatValue();
		}};
	private final Function<Date,Long> date2LongFunction = new Function<Date,Long>(){
		@Override
		public Long apply(Date input) {
			return null == input ? null : input.getTime();
		}};
	private final Function<Long,Date> long2DateFun = new Function<Long,Date>(){
		@Override
		public Date apply(Long input) {
			return null == input ? null : new Date(input);
		}};
	private final Function<Long,java.sql.Date> long2SqlDateFun = new Function<Long,java.sql.Date>(){
		@Override
		public java.sql.Date apply(Long input) {
			return null == input ? null : new java.sql.Date(input);
		}};
	private final Function<Long,java.sql.Time> long2SqlTimeFun = new Function<Long,java.sql.Time>(){
		@Override
		public java.sql.Time apply(Long input) {
			return null == input ? null : new java.sql.Time(input);
		}};
	private final Function<URL,String> url2StringFun = new Function<URL,String>(){
		@Override
		public String apply(URL input) {
			return null == input ? null : input.toString();
		}};
	private final Function<URI,String> uri2StringFun = new Function<URI,String>(){
		@Override
		public String apply(URI input) {
			return null == input ? null : input.toString();
		}};
	private final Function<String,URL> string2UrlFun = new Function<String,URL>(){
		@Override
		public URL apply(String input) {
			try {
				return null == input ? null : new URL(input);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}};
	private final Function<String,URI> string2UriFun = new Function<String,URI>(){
		@Override
		public URI apply(String input) {
			return null == input ? null : URI.create(input);
		}};
	private final Function<int[],List<Integer>> intArray2List = new Function<int[],List<Integer>>(){
		@Override
		public List<Integer> apply(int[] input) {
			return null == input ? null : Ints.asList(input);
		}};
	private final Function<long[],List<Long>> longArray2List = new Function<long[],List<Long>>(){
		@Override
		public List<Long> apply(long[] input) {
			return null == input ? null : Longs.asList(input);
		}};
	private final Function<double[],List<Double>> doubleArray2List = new Function<double[],List<Double>>(){
		@Override
		public List<Double> apply(double[] input) {
			return null == input ? null : Doubles.asList(input);
		}};
	private final Function<float[],List<Double>> floatArray2List = new Function<float[],List<Double>>(){
		@Override
		public List<Double> apply(float[] input) {			
			return null == input 
					? null 
					: Lists.transform(Floats.asList(input),(Function<Float,Double>)float2DoubleFun);
		}};
	private final Function<short[],List<Short>> shortArray2List = new Function<short[],List<Short>>(){
		@Override
		public List<Short> apply(short[] input) {
			return null == input ? null : Shorts.asList(input);
		}};
	private final Function<boolean[],List<Boolean>> booleanArray2List = new Function<boolean[],List<Boolean>>(){
		@Override
		public List<Boolean> apply(boolean[] input) {
			return null == input ? null : Booleans.asList(input);
		}};
	private final Function<List<Integer>,int[]> list2intArray = new Function<List<Integer>,int[]>(){
		@Override
		public int[] apply(List<Integer> input) {
			return null == input ? null : Ints.toArray(input);
		}};
	private final Function<List<Long>,long[]> list2longArray = new Function<List<Long>,long[]>(){
		@Override
		public long[] apply(List<Long> input) {
			return null == input ? null : Longs.toArray(input);
		}};
	private final Function<List<Double>,double[]> list2doubleArray = new Function<List<Double>,double[]>(){
		@Override
		public double[] apply(List<Double> input) {
			return null == input ? null : Doubles.toArray(input);
		}};
	private final Function<List<Double>,float[]> list2floatArray = new Function<List<Double>,float[]>(){
		@Override
		public float[] apply(List<Double> input) {
			return null == input ? null : Floats.toArray(input);
		}};
	private final Function<List<Short>,short[]> list2shortArray = new Function<List<Short>,short[]>(){
		@Override
		public short[] apply(List<Short> input) {
			return null == input ? null : Shorts.toArray(input);
		}};
	private final Function<List<Boolean>,boolean[]> list2booleanArray = new Function<List<Boolean>,boolean[]>(){
		@Override
		public boolean[] apply(List<Boolean> input) {
			return null == input ? null : Booleans.toArray(input);
		}};
	protected final Table<Class<?>,Class<?>,Function<?,?>> transTable = HashBasedTable.create();
	public BaseTypeTransformer() {
		transTable.put(byte[].class,ByteBuffer.class,byteArray2ByteBufferFun);
		transTable.put(ByteBuffer.class,byte[].class,byteBuffer2ByteArrayFun);
		transTable.put(Float.class,Double.class,float2DoubleFun);
		transTable.put(Double.class,Float.class,double2FloatFun);
		transTable.put(float.class,double.class,float2DoubleFun);
		transTable.put(float.class,Double.class,float2DoubleFun);
		transTable.put(double.class,float.class,double2FloatFun);
		transTable.put(double.class,Float.class,double2FloatFun);
		transTable.put(Date.class, Long.class, date2LongFunction);
		transTable.put(java.sql.Date.class, Long.class, date2LongFunction);
		transTable.put(java.sql.Time.class,Long.class,date2LongFunction);
		transTable.put(Long.class,Date.class,long2DateFun);
		transTable.put(Long.class,java.sql.Date.class,long2SqlDateFun);
		transTable.put(Long.class,java.sql.Time.class,long2SqlTimeFun);
		transTable.put(URL.class,String.class,url2StringFun);
		transTable.put(URI.class,String.class,uri2StringFun);
		transTable.put(String.class,URL.class,string2UrlFun);
		transTable.put(String.class,URI.class,string2UriFun);
		transTable.put(int[].class,List.class,intArray2List);
		transTable.put(long[].class,List.class,longArray2List);
		transTable.put(double[].class,List.class,doubleArray2List);
		transTable.put(float[].class,List.class,floatArray2List);
		transTable.put(short[].class,List.class,shortArray2List);
		transTable.put(boolean[].class,List.class,booleanArray2List);
		transTable.put(List.class,int[].class,list2intArray);
		transTable.put(List.class,long[].class,list2longArray);
		transTable.put(List.class,double[].class,list2doubleArray);
		transTable.put(List.class,float[].class,list2floatArray);
		transTable.put(List.class,short[].class,list2shortArray);
		transTable.put(List.class,boolean[].class,list2booleanArray);
		transTable.put(Boolean.class, boolean.class, booleanTypeFun);
		transTable.put(Character.class, Character.class, charTypeFun);
		transTable.put(Byte.class, byte.class, byteTypeFun);
		transTable.put(Short.class, short.class, shortTypeFun);
		transTable.put(Integer.class, int.class, intTypeFun);
		transTable.put(Long.class, long.class, longTypeFun);
		transTable.put(Float.class, float.class, floatTypeFun);
		transTable.put(Double.class, double.class, doubleTypeFun);
		transTable.put(Void.class, void.class, voidTypeFun);
	}
	/**
	 * 设置{@code left -> right}的转换器，参数不可为{@code null}
	 * @param left
	 * @param right
	 * @param trans 转换器对象
	 * @return
	 */
	public <L,R>BaseTypeTransformer setTransformer(Class<L> left, Class<R> right, Function<L, R> trans){
		checkArgument(null != left && null != right && null != trans,"left, right, trans must not be null");
		synchronized (this.transTable) {
			transTable.put(left,right,trans);
			return this;
		}
	}
	/**
	 * 返回{@code left TO right}指定的转换器，参数不可为{@code null}
	 * @param left
	 * @param right
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <L,R>Function<L,R> getTransformer(Class<L>left,Class<R>right){
		checkArgument(null != left && null != right,"left, right must not be null");
		if(right.isAssignableFrom(left)){
			// 相同类型转换
			return (Function<L,R>)this.sameTypeFun;
		}
		Function<L, R> result = (Function<L, R>) this.transTable.get(left, right);
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
			}
		}
		return result;
	}
	public <L,R>Function<L,R> getTransformerChecked(Class<L>left,Class<R>right){
		return checkNotNull(getTransformer(left,right),"not found transformer for %s to %s",left,right);
	}

	/**
	 * 将{@code value}转换为{@code right}指定的类型
	 * @param value
	 * @param left {@code value}的原类型
	 * @param right 目标类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <L,R> R to (L value,Class<L>left,Class<R> right){
		if(null == value){
			return null;
		}
		checkArgument(null != right,"right is null");
		if(right.isInstance(value)){
			return (R) value;
		}
		return this.getTransformerChecked(left, right).apply(value);
	}
	/**
	 * 将 List 中的元素转换为{@code right}指定的类型
	 * @param input
	 * @param left 列表元素原类型
	 * @param right 列表元素目标类型
	 * @return
	 */
	public <L,R>List<R> to(List<L> input,Class<L> left, Class<R> right){
		return null == input 
				? null 
				: Lists.transform(input, this.getTransformerChecked(left, right));
	}
	/**
	 * 将 Set 中的元素转换为{@code right}指定的类型
	 * @param input
	 * @param left 集合元素原类型
	 * @param right 集合元素目标类型
	 * @return
	 */
	public <L,R> Set<R> to(Set<L> input,Class<L> left, Class<R> right){
		return null == input 
				? null 
				: Sets.newHashSet(Iterables.transform(input, this.getTransformerChecked(left, right)));
	}
	/**
	 * 将 数组 中的元素转换为{@code right}指定的类型
	 * @param input
	 * @param left 数组元素原类型
	 * @param right 数组元素目标类型
	 * @return
	 */
	public <L,R> List<R> to(L[] input,Class<L> left,Class<R> right){
		if(null == input){
			return null;
		}
		return to(Arrays.asList(input),left,right);
	}
	/**
	 * 将 int[] 转换为列表
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public List<Integer> to(int[] input,Class<Integer>left,Class<Integer> right){
		return intArray2List.apply(input);
	}
	/**
	 * 将 long[] 转换为列表
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public List<Long> to(long[] input,Class<Long>left,Class<Long> right){
		return longArray2List.apply(input);
	}
	/**
	 * 将 double[] 转换为列表
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public List<Double> to(double[] input,Class<Double>left,Class<Double> right){
		return doubleArray2List.apply(input);
	}
	/**
	 * 将 float[] 转换为Double列表
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public List<Double> to(float[] input,Class<Float>left,Class<Double> right){
		return floatArray2List.apply(input);
	}
	/**
	 * 将 short[] 转换为列表
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public List<Short> to(short[] input,Class<Short>left,Class<Short> right){
		return shortArray2List.apply(input);
	}
	/**
	 * 将 boolean[] 转换为列表
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public List<Boolean> to(boolean[] input,Class<Boolean>left,Class<Boolean> right){
		return booleanArray2List.apply(input);
	}
	/**
	 * 将 列表 转换为数组
	 * @param input
	 * @param left 列表元素类型
	 * @param right 数组元素类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <L,R> R[] toArray(List<L> input,Class<L> left,Class<R> right){
		if(null == input){
			return null;
		}
		List<R> r = to(input,left,right);
		return r.toArray((R[]) Array.newInstance(right, r.size()));
	}
	/**
	 * 将 Integer 列表 转换为数组
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public  int[] tointArray(List<Integer> input,Class<Integer> left,Class<Integer> right){
		return list2intArray.apply(input);
	}
	/**
	 * 将 Long 列表 转换为数组
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public  long[] tolongArray(List<Long> input,Class<Long> left,Class<Long> right){
		return list2longArray.apply(input);
	}
	/**
	 * 将 Double 列表 转换为数组
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public double[] todoubleArray(List<Double>input,Class<Double> left,Class<Double> right){
		return list2doubleArray.apply(input);	
	}
	/**
	 * 将 Double 列表 转换为float数组
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public float[] tofloatArray(List<Double>input,Class<Double> left,Class<Float> right){
		return list2floatArray.apply(input);
	}
	/**
	 * 将 Short 列表 转换为数组
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public short[] toshortArray(List<Short>input,Class<Short> left,Class<Short> right){
		return list2shortArray.apply(input);	
	}
	/**
	 * 将 Boolean 列表 转换为数组
	 * @param input
	 * @param left 
	 * @param right 
	 * @return
	 */
	public boolean[] tobooleanArray(List<Boolean>input,Class<Boolean> left,Class<Boolean> right){
		return list2booleanArray.apply(input);
	}
	/**
	 * 将{@code Map<K1,V1>}转换为{@code Map<K2,V2>}
	 * @param input
	 * @param k1
	 * @param v1
	 * @param k2
	 * @param v2
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <K1,V1,K2,V2> Map<K2,V2> to(Map<K1,V1>input,Class<K1>k1,Class<V1>v1,Class<K2> k2,Class<V2> v2){
		checkArgument(null != k1 && null != v1 && null != k2 && null != v2,"k1,v1,k2,v2 must not be null");
		if(k1 == k2){
			if(v1 == v2){
				return (Map<K2, V2>) input;
			}
			return (Map<K2, V2>) Maps.transformValues(input, this.getTransformerChecked(v1, v2));
		}else{
			Map<K2, V1> k2v1 = transform(input,this.getTransformerChecked(k1, k2));
			if(v1 == v2){
				return (Map<K2, V2>) k2v1;
			}else{
				return Maps.transformValues(k2v1, this.getTransformerChecked(v1, v2));
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
	public <L, R> R cast(Object value, Type left, Type right) {
		TypeToken<?> leftToken = TypeToken.of(checkNotNull(left));
		TypeToken<?> rightToken = TypeToken.of(checkNotNull(right));
		if(null == value){
			// right为primitive类型时 value不可为null,否则抛出异常
			checkArgument(! ((right instanceof Class<?>) && ((Class<?>)right).isPrimitive()),"cant cast null to primitive type %s",right);
			return null;
		}
		// object to object
		if( left instanceof Class<?> && right instanceof Class<?>){
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
     * convert {@code Map<K1,V>} to {@code Map<K2,V>}   
     * @return {@linkplain ImmutableMap}
     */
    private static final <K1,K2,V>Map<K2,V> transform(Map<K1,V>fromMap,final Function<K1,K2>transformer){
        checkNotNull(fromMap,"fromMap is null");
        checkNotNull(transformer,"transformer is null");
        // 新的Map对象Key类型已经是K2了，只是Value类型成了Entry
        ImmutableMap<K2, Entry<K1, V>> k2Entry = Maps.uniqueIndex(fromMap.entrySet(), new Function<Entry<K1, V>,K2>(){
            @Override
            public K2 apply(Entry<K1, V> input) {               
                return transformer.apply(input.getKey());
            }});
        // 再做一次转换将Entry<K1, V>转换成V,这个过程并没有创建新的Map,只是创建了k2Entry的代理对象
        Map<K2, V> k2V = Maps.transformEntries(k2Entry, new EntryTransformer<K2,Entry<K1,V>,V>(){
            @Override
            public V transformEntry(K2 key, Entry<K1, V> value) {
                return value.getValue();
            }});
        return k2V;
    }
}
