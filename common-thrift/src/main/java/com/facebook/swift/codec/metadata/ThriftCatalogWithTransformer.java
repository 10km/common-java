package com.facebook.swift.codec.metadata;

import static com.facebook.swift.codec.metadata.DecoratorThriftStructMetadata.STRUCT_TRANSFORMER;

import java.lang.reflect.Type;

import com.facebook.swift.codec.metadata.MetadataErrors.Monitor;

/**
 * 重载{@link #getThriftStructMetadata(Type)}方法，
 * 将{@link ThriftStructMetadata}实例转换为{@link DecoratorThriftStructMetadata}实例
 * @author guyadong
 *
 */
public class ThriftCatalogWithTransformer extends ThriftCatalog {

	public ThriftCatalogWithTransformer() {
	}

	public ThriftCatalogWithTransformer(Monitor monitor) {
		super(monitor);
	}
	@Override
	public <T> ThriftStructMetadata getThriftStructMetadata(Type structType) {
		return STRUCT_TRANSFORMER.apply(super.getThriftStructMetadata(structType));
	}
}
