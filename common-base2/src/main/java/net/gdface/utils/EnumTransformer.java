package net.gdface.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Function;

/**
 * 枚举类型转换
 * @author guyadong
 *
 * @param <L>
 * @param <R>
 */
public class EnumTransformer<L extends Enum<L>,R extends Enum<R>> implements Function<L,R>{
	private final Class<R> right;
	public EnumTransformer(Class<L> left, Class<R> right) {
		checkNotNull(left,"left is null");
		this.right = checkNotNull(right,"right is null");
	}
	@Override
	public R apply(L input) {
		return null == input ? null : Enum.valueOf(right, input.name());
	}    	
}