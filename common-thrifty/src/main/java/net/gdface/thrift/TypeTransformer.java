package net.gdface.thrift;

import java.nio.ByteBuffer;

import com.google.common.base.Function;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.gdface.utils.BaseTypeTransformer;
import okio.ByteString;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftUtils.*;
/**
 * 类型转换工具类
 * @author guyadong
 *
 */
public class TypeTransformer extends BaseTypeTransformer{
	private final Function<byte[],ByteString> byteArray2ByteStringFun = new Function<byte[],ByteString>(){
		@Override
		public ByteString apply(byte[] input) {
			return null == input ? null : ByteString.of(input);
		}};
	private final Function<ByteString,byte[]> byteString2ByteArrayFun = new Function<ByteString,byte[]>(){
		@Override
		public byte[] apply(ByteString input) {
			return null == input ? null : input.toByteArray();
		}};
	private final Function<ByteBuffer,ByteString> byteBuffer2ByteStringFun = new Function<ByteBuffer,ByteString>(){
		@Override
		public ByteString apply(ByteBuffer input) {
			return null == input ? null : ByteString.of(input);
		}};
	private final Function<ByteString,ByteBuffer> byteString2ByteBufferFun = new Function<ByteString,ByteBuffer>(){
		@Override
		public ByteBuffer apply(ByteString input) {
			return null == input ? null : input.asByteBuffer();
		}};
	private final Table<Class<?>,Class<?>,Function<?,?>> transTable = HashBasedTable.create();
	private static TypeTransformer instance = new TypeTransformer();
	public static TypeTransformer getInstance() {
		return instance;
	}
	public static synchronized void setInstance(TypeTransformer instance) {
		TypeTransformer.instance = checkNotNull(instance,"instance is null");
	}
	protected TypeTransformer() {
		transTable.put(byte[].class,ByteString.class,byteArray2ByteStringFun);
		transTable.put(ByteString.class,byte[].class,byteString2ByteArrayFun);
		transTable.put(ByteBuffer.class,ByteString.class,byteBuffer2ByteStringFun);
		transTable.put(ByteString.class,ByteBuffer.class,byteString2ByteBufferFun);		
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <L,R>Function<L,R> getTransformer(Class<L>left,Class<R>right){
		Function<L, R> result = super.getTransformer(left,right);	
		if (null == result) {
			if(isThriftStruct(left) && isThriftyStruct(right)){
				// 添加 Swift到Thrifty对象的转换
				synchronized (this.transTable) {
					// double checking
					if (null == (result = (Function<L, R>) this.transTable.get(left, right))) {
						if(isThriftException(left) && isThriftyException(right)){
							result = new Swift2ThriftyExceptionTransformer(left,right);
						}else{
							checkArgument(!isThriftException(left) && !isThriftyException(right),
									"INVALID type pair %s TO %s",left.getName(),right.getName());
							result = new Swift2ThriftyStructTransformer(left,right);
						}
						setTransformer(left, right, result);
					}
				}
			}else if(isThriftyStruct(left) && isThriftStruct(right)){
				// 添加 Thrifty到Swift对象的转换
				synchronized (this.transTable) {
					// double checking
					if (null == (result = (Function<L, R>) this.transTable.get(left, right))) {
						result = new Thrifty2SwiftStructTransformer(left,right);
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
