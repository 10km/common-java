package net.gdface.utils;

import static org.junit.Assert.*;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import net.gdface.utils.ParmTest.EnumTest;
import net.gdface.utils.ParmTest.Sub;
import net.gdface.utils.ParmTest.Sub2;


public class ParameterizedTypeTest {

	@Test
	public void test() {
		try {
			Type returnType = java.util.Map.class.getMethod("entrySet").getGenericReturnType();
			System.out.println(returnType);
			ParameterizedTypeImpl paramType = new ParameterizedTypeImpl((ParameterizedType) returnType);
			System.out.println(paramType.toString());
			System.out.println(paramType.equals(returnType));
		} catch (NoSuchMethodException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	@Test
	public void test2() {
		{
			System.out.println(Double[].class.toString());		
			System.out.println(Sub.class.toString());
			System.out.println(Sub.class.getDeclaringClass());
			System.out.println(Sub.class.getSimpleName());
			ParmTest<List<Float>> parent = new ParmTest<List<Float>>();
			ParmTest<List<Float>>.Sub<Integer> t = parent.new Sub<Integer>(){};
			ParmTest.Sub.class.getModifiers();
			System.out.println("ParmTest.Sub isStatic " + Modifier.isStatic(ParmTest.Sub.class.getModifiers()));
			System.out.println("ParmTest isStatic " + Modifier.isStatic(ParmTest.class.getModifiers()));
			System.out.println("ParmTest DeclaringClass " + ParmTest.class.getDeclaringClass());

			Type type = t.getClass().getGenericSuperclass();
			ParameterizedTypeImpl paramType = new ParameterizedTypeImpl((ParameterizedType) type);
			System.out.println(type.toString());
			System.out.println(paramType.toString());
		}
		{			
			Sub2<Long> t = new Sub2<Long>(){};
			Type type = t.getClass().getGenericSuperclass();
			ParameterizedTypeImpl paramType = new ParameterizedTypeImpl((ParameterizedType) type);
			System.out.println(type.toString());
			System.out.println(paramType.toString());

		}
		{
	        Class<?> cls = Character.Subset.class.getEnclosingClass();
	        System.out.println("class name  : " + cls.toString());
	        System.out.println("class name  : " + Character.Subset.class.getDeclaringClass().toString());
		}
		{
			System.out.println(EnumTest.class.getDeclaringClass());
			System.out.println(Modifier.isStatic(EnumTest.class.getModifiers()));
			System.out.println(Modifier.isStatic(ParmTest.class.getModifiers()));
		}
	}

}
