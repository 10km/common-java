package com.facebook.swift.codec.metadata;

import java.util.Collection;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

/**
 * {@link ThriftStructMetadata}的代理类<br>
 * 重载所有{@link ThriftFieldMetadata}相关方法
 * @author guyadong
 *
 */
@Immutable
public class DecoratorThriftStructMetadata extends ThriftStructMetadata {
    /** {@link DecoratorThriftStructMetadata}缓存对象,
     * 保存每个{@link ThriftStructMetadata}对应的{@link DecoratorThriftStructMetadata}实例 
     */
    private static final LoadingCache<ThriftStructMetadata,DecoratorThriftStructMetadata> 
    	STRUCTS_CACHE = 
    		CacheBuilder.newBuilder().build(
    				new CacheLoader<ThriftStructMetadata,DecoratorThriftStructMetadata>(){
						@Override
						public DecoratorThriftStructMetadata load(ThriftStructMetadata key) throws Exception {
							return new DecoratorThriftStructMetadata(key);
						}});
    /**  将{@link ThriftStructMetadata}转换为 {@link DecoratorThriftStructMetadata}对象 */
    public static final Function<ThriftStructMetadata,ThriftStructMetadata> 
    	STRUCT_TRANSFORMER = new Function<ThriftStructMetadata,ThriftStructMetadata>(){
    		@Nullable
			@Override
			public ThriftStructMetadata apply(@Nullable ThriftStructMetadata input) {
				return null == input || input instanceof DecoratorThriftStructMetadata
						? input
						: STRUCTS_CACHE.getUnchecked(input);
			}};
	    /** 
	     * {@link DecoratorThriftFieldMetadata}缓存对象,
	     * 保存每个{@link ThriftFieldMetadata}对应的{@link DecoratorThriftFieldMetadata}实例 
	     */
	    private final LoadingCache<ThriftFieldMetadata,DecoratorThriftFieldMetadata> 
	    	FIELDS_CACHE = 
	    		CacheBuilder.newBuilder().build(
	    				new CacheLoader<ThriftFieldMetadata,DecoratorThriftFieldMetadata>(){
							@Override
							public DecoratorThriftFieldMetadata load(ThriftFieldMetadata key) throws Exception {
								return new DecoratorThriftFieldMetadata(key);
							}});
	    /**  将{@link ThriftFieldMetadata}转换为 {@link DecoratorThriftFieldMetadata}对象 */
		private  final Function<ThriftFieldMetadata,ThriftFieldMetadata> 
			FIELD_TRANSFORMER = 
				new Function<ThriftFieldMetadata,ThriftFieldMetadata>(){
					@Nullable
					@Override
					public ThriftFieldMetadata apply(@Nullable ThriftFieldMetadata input) {
					    return null == input || input instanceof DecoratorThriftFieldMetadata  
					    		? input 
					    		: FIELDS_CACHE.getUnchecked(input);
					}};
	private DecoratorThriftStructMetadata(ThriftStructMetadata input){
		super(input.getStructName(), 
				input.getStructType(), 
				input.getBuilderType(), 
				input.getMetadataType(), 
				input.getBuilderMethod(), 
				input.getDocumentation(), 
				ImmutableList.copyOf(input.getFields()),
				input.getConstructorInjection(), 
				input.getMethodInjections());
	}
	@Override
	public ThriftFieldMetadata getField(int id) {
		return FIELD_TRANSFORMER.apply(super.getField(id));
	}

	@Override
	public Collection<ThriftFieldMetadata> getFields() {
		return Collections2.transform(super.getFields(), FIELD_TRANSFORMER);
	}

	@Override
	public Collection<ThriftFieldMetadata> getFields(FieldKind type) {
		return Collections2.transform(super.getFields(type), FIELD_TRANSFORMER);
	}

}
