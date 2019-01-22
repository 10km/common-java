package net.gdface.thrift.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.facebook.swift.codec.metadata.ThriftCatalog;
import com.facebook.swift.codec.metadata.ThriftStructMetadata;

import net.gdface.thrift.TypeTransformer;
import net.gdface.thrift.exception.ServiceRuntimeException;

public class TypeTransformerTest {

	@Test
	public void test() {
		TypeTransformer trans = TypeTransformer.getInstance();
		List<Long> result = trans.to(Arrays.asList(new Date()),Date.class, Long.class);
		for(Long element:result){
			System.out.println(element);
		}
		System.out.println(trans.to(Arrays.asList(0.7f),Float.class, Double.class));
	}
	@Test
	public void test2(){
		ThriftCatalog catalog = new ThriftCatalog();
		ThriftStructMetadata metadata = catalog.getThriftStructMetadata(ServiceRuntimeException.class);
		System.out.println(metadata);
	}
}
