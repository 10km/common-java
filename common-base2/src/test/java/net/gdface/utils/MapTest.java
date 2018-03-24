package net.gdface.utils;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.Test;

public class MapTest {

	@Test
	public void test() {
		HashMap<String, String> map = new LinkedHashMap<String,String>();
		map.put("young", "children");
		map.put("hello", null);
		map.put("alibaba", "ok");
		
		System.out.println(map.keySet());
		System.out.println(map.values());
		System.out.println(map.entrySet());
	}

}
