package net.gdface.thrift;

import java.lang.reflect.Array;
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

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftStructTransformer.isThriftStruct;
/**
 * 类型转换工具类
 * @author guyadong
 *
 */
public class TypeTransformer {
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
	private final Table<Class<?>,Class<?>,Function<?,?>> transTable = HashBasedTable.create();

	public TypeTransformer() {
		transTable.put(byte[].class,ByteBuffer.class,byteArray2ByteBufferFun);
		transTable.put(ByteBuffer.class,byte[].class,byteBuffer2ByteArrayFun);
		transTable.put(Float.class,Double.class,float2DoubleFun);
		transTable.put(Double.class,Float.class,double2FloatFun);
		transTable.put(float.class,double.class,float2DoubleFun);
		transTable.put(double.class,float.class,double2FloatFun);
		transTable.put(Date.class, Long.class, date2LongFunction);
		transTable.put(java.sql.Date.class, Long.class, date2LongFunction);
		transTable.put(java.sql.Time.class,Long.class,date2LongFunction);
		transTable.put(Long.class,Date.class,long2DateFun);
		transTable.put(Long.class,java.sql.Date.class,long2SqlDateFun);
		transTable.put(Long.class,java.sql.Time.class,long2SqlTimeFun);
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
	}
	public <L,R>TypeTransformer setTransformer(Class<L> left, Class<R> right, Function<L, R> trans){
		checkArgument(null != left && null != right && null != trans,"left, right, trans must not be null");
		synchronized (this.transTable) {
			transTable.put(left,right,trans);
			return this;
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <L,R>Function<L,R> getTransformer(Class<L>left,Class<R>right){
		checkArgument(null != left && null != right,"left, right must not be null");
		if(right.isAssignableFrom(left)){
			return (Function<L,R>)this.sameTypeFun;
		}
		Function<L, R> result = (Function<L, R>) this.transTable.get(left, right);
		if (null == result) {
			if (Enum.class.isAssignableFrom(left) && Enum.class.isAssignableFrom(left)) {
				synchronized (this.transTable) {
					// double checking
					if (null == (result = (Function<L, R>) this.transTable.get(left, right))) {
						EnumTransformer<?, ?> trans = new EnumTransformer((Class<? extends Enum<?>>) left,
								(Class<? extends Enum<?>>) right);
						setTransformer(left, right, (Function<L,R>)trans);
					}
				}
			}else if(isThriftStruct(left) && isThriftStruct(right)){
				synchronized (this.transTable) {
					// double checking
					if (null == (result = (Function<L, R>) this.transTable.get(left, right))) {
						ThriftStructTransformer trans = new ThriftStructTransformer(left,right);
						setTransformer(left, right, (Function<L,R>)trans);
					}
				}
			}
		}
		return result;
	}
	public Function<?,?> getTransformerChecked(Class<?>left,Class<?>right){
		return checkNotNull(getTransformer(left,right),"not found transformer for %s to %s",left,right);
	}

	@SuppressWarnings("unchecked")
	public <L,R> R to (L value,Class<L>left,Class<R> right){
		if(null == value){
			return null;
		}
		if(checkNotNull(right,"right is null").isInstance(value)){
			return (R) value;
		}
		return ((Function<L,R>)this.getTransformerChecked(left, right)).apply(value);
	}
	@SuppressWarnings("unchecked")
	public <L,R>List<R> to(List<L> input,Class<L> left, Class<R> right){
		return null == input 
				? null 
				: Lists.transform(input, (Function<L,R>)this.getTransformerChecked(left, right));
	}
	@SuppressWarnings("unchecked")
	public <L,R> Set<R> to(Set<L> input,Class<L> left, Class<R> right){
		return null == input 
				? null 
				: Sets.newHashSet(Iterables.transform(input, (Function<L,R>)this.getTransformerChecked(left, right)));
	}
	@SuppressWarnings("unchecked")
	public <L,R> List<R> toList(L[] input,Class<L> left,Class<R> right){
		if(null == input){
			return null;
		}
		checkArgument(null != left && null != right,"left or right is null");
		List<L> l = Arrays.asList(input);
		if(left == right){
			return (List<R>) l;
		}
		return to(l,left,right);
	}

	public List<Integer> to(int[] input,Class<Integer>left,Class<Integer> right){
		return intArray2List.apply(input);
	}
	public List<Long> to(long[] input,Class<Long>left,Class<Long> right){
		return longArray2List.apply(input);
	}
	public List<Double> to(double[] input,Class<Double>left,Class<Double> right){
		return doubleArray2List.apply(input);
	}
	public List<Double> to(float[] input,Class<Float>left,Class<Double> right){
		return floatArray2List.apply(input);
	}
	public List<Short> to(short[] input,Class<Short>left,Class<Short> right){
		return shortArray2List.apply(input);
	}
	public List<Boolean> to(boolean[] input,Class<Boolean>left,Class<Boolean> right){
		return booleanArray2List.apply(input);
	}
	@SuppressWarnings("unchecked")
	public <L,R> R[] toArray(List<L> input,Class<L> left,Class<R> right){
		if(null == input){
			return null;
		}
		checkArgument(null != left && null != right,"left or right is null");
		List<R> r ;
		if(left == right){
			r = (List<R>) input;
		}else{
			r =  to(input,left,right);
		}		
		return null == r 
				? null
				: r.toArray((R[]) Array.newInstance(checkNotNull(right,"right is null"), r.size()));
	}
	public  int[] tointArray(List<Integer> input,Class<Integer> left,Class<Integer> right){
		return list2intArray.apply(input);
	}
	public  long[] tolongArray(List<Long> input,Class<Long> left,Class<Long> right){
		return list2longArray.apply(input);
	}
	public double[] todoubleArray(List<Double>input,Class<Double> left,Class<Double> right){
		return list2doubleArray.apply(input);	
	}
	public float[] tofloatArray(List<Double>input,Class<Double> left,Class<Float> right){
		return list2floatArray.apply(input);
	}
	public short[] toshortArray(List<Short>input,Class<Short> left,Class<Short> right){
		return list2shortArray.apply(input);	
	}
	public boolean[] tobooleanArray(List<Boolean>input,Class<Boolean> left,Class<Boolean> right){
		return list2booleanArray.apply(input);
	}
	@SuppressWarnings("unchecked")
	public <K1,V1,K2,V2> Map<K2,V2> to(Map<K1,V1>input,Class<K1>k1,Class<V1>v1,Class<K2> k2,Class<V2> v2){
		checkArgument(null != k1 && null != v1 && null != k2 && null != v2,"k1,v1,k2,v2 must not be null");
		if(k1 == k2){
			if(v1 == v2){
				return (Map<K2, V2>) input;
			}
			return (Map<K2, V2>) Maps.transformValues(input, (Function<V1,V2>)this.getTransformerChecked(v1, v2));
		}else{
			Map<K2, V1> k2v1 = transform(input,(Function<K1,K2>)this.getTransformerChecked(k1, k2));
			if(v1 == v2){
				return (Map<K2, V2>) k2v1;
			}else{
				return Maps.transformValues(k2v1, (Function<V1,V2>)this.getTransformerChecked(v1, v2));
			}
		}		
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
    public static void main(String[] args){
		TypeTransformer trans = new TypeTransformer();
		List<Long> result = trans.to(Arrays.asList(new Date()),Date.class, Long.class);
		for(Long element:result){
			System.out.println(element);
		}
		System.out.println(trans.to(Arrays.asList(0.7f),Float.class, Double.class));
	}
}