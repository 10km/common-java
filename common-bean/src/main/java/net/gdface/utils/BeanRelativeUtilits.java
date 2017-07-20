package net.gdface.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
public class BeanRelativeUtilits {

	public static class FieldComparator implements Comparator<Object> {
		private static final Map<String, FieldComparator> comparators = new HashMap<String, FieldComparator>();
		private final String name;
	
		private FieldComparator(String name) {
			Assert.notEmpty(name, "name");
			this.name = name;
		}
	
		public static final FieldComparator getComparator(String name) {
			FieldComparator comparator=comparators.get(name);
			if (null != comparator )
				return comparator;
			comparator = new FieldComparator(name);
			comparators.put(name, comparator);
			return comparator;
		}
		public static final void clearComparators() {
			comparators.clear();
		}
		@Override
		public int compare(Object o1, Object o2) {
			Object v1 = getPropertyFrom(o1, name);
			Object v2 = getPropertyFrom(o2, name);
			if (v1 == v2)
				return 0;
			if (v1 == null)
				return 1;
			if (v2 == null)
				return -1;
			try {
				Method compareTo = v1.getClass().getMethod("compareTo", v2.getClass());
				return (Integer) compareTo.invoke(v1, v2);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static final <T> T getPropertyFrom(Object obj,String name){
		BeanUtilsBean beanUtils = BeanUtilsBean.getInstance();
		PropertyUtilsBean propertyUtils = beanUtils.getPropertyUtils();
		try {
			return (T) propertyUtils.getProperty(obj, name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	public static final <T>List<T> sortByField(Collection<T> collection,String fieldName) {
		Assert.notNull(collection, "collection");
		ArrayList<T> list = new ArrayList<T>(collection);
		Collections.sort(list, FieldComparator.getComparator(fieldName));
		return list;
	}

	public static final <T> T[] sortByField(T[] array,String fieldName) {
		Assert.notNull(array, "array");
		T[] newArray = Arrays.copyOf(array, array.length);
		Arrays.sort(newArray, FieldComparator.getComparator(fieldName));
		return newArray;
	}
}
