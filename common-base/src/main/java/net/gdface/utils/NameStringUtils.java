package net.gdface.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author guyadong
 *
 */
public final class NameStringUtils {
	private static final String[] JAVA_RESERVED_WORDS = new String[]{"null", "true", "false", "abstract", "double", "int", "strictfp",
			"boolean", "else", "interface", "super", "break", "extends", "long", "switch", "byte", "final", "native",
			"synchronized", "case", "finally", "new", "this", "catch", "float", "package", "throw", "char", "for",
			"private", "throws", "class", "goto", "protected", "transient", "const", "if", "public", "try", "continue",
			"implements", "return", "void", "default", "import", "short", "volatile", "do", "instanceof", "static",
			"while", "assert", "enum"};
	/** thrift IDL 关键字 */
	private static final String[] THRIFT_RESERVED_WORDS = new String[]{"bool","byte","i16","i32","i64","double",
			"binary","string","list","set","map","typedef","enum","struct","namespace","include","const","required","optional"
			};
	private static final Set<String> JAVA_RESERVED_KEYS = new HashSet<String>(Arrays.asList(JAVA_RESERVED_WORDS));
	private static final Set<String> THRIFT_RESERVED_KEYS = new HashSet<String>(Arrays.asList(THRIFT_RESERVED_WORDS));

	public static String pathFromPpackage(String pkg) {
		if (pkg == null) {
			return "";
		}
		return pkg.replace('.', '/');
	}
	public static String pathFromPackage(Package pkg) {
		return pathFromPpackage(null == pkg ? null : pkg.getName());
	}
	public static String convertName(String name, boolean wimpyCaps) {
		StringBuffer buffer = new StringBuffer(name.length());
		char[] list = name.toLowerCase().toCharArray();
		for (int i = 0; i < list.length; ++i) {
			if (i == 0 && !wimpyCaps) {
				buffer.append(Character.toUpperCase(list[i]));
				continue;
			}
			if (list[i] == '_' && i + 1 < list.length && i != 0) {
				buffer.append(Character.toUpperCase(list[++i]));
				continue;
			}
			buffer.append(list[i]);
		}
		return buffer.toString();
	}

	/**
	 * 字符串首字母大写
	 * @param name
	 * @return
	 */
	public static String firstUpperCase(String name){
		if(Judge.isEmpty(name)){
			return name;
		}
		char[] list = name.toCharArray();
		list[0] = Character.toUpperCase(list[0]);
		return new String(list);
	}
	public static boolean isJavaReserved(String name) {
		return null == name ? false : JAVA_RESERVED_KEYS.contains(name);
	}
	public static boolean isThriftReserved(String name) {
		return null == name ? false : THRIFT_RESERVED_KEYS.contains(name);
	}
}