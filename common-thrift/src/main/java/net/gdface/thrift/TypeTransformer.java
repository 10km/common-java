package net.gdface.thrift;

import com.google.common.base.Function;
import net.gdface.utils.BaseTypeTransformer;

import static com.google.common.base.Preconditions.*;
import static net.gdface.thrift.ThriftUtils.*;
/**
 * 类型转换工具类
 * @author guyadong
 *
 */
public class TypeTransformer extends BaseTypeTransformer{
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
			if(isThriftStruct(left) && isThriftStruct(right)){
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
