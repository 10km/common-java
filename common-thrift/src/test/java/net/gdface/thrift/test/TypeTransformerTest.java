package net.gdface.thrift.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import net.gdface.thrift.TypeTransformer;

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

}
