/**   
* @Title: Assert.java 
* @Package net.gdface.utils 
* @Description: TODO 
* @author guyadong   
* @date 2015年4月23日 上午9:48:17 
* @version V1.0   
*/
package net.gdface.utils;

import java.nio.Buffer;
import java.util.Collection;
import java.util.Map;

/**断言类
 * @author guyadong
 *
 */
public class Assert {
	private static final String getLocation(){
		StackTraceElement stack = Thread.currentThread() .getStackTrace()[3];
		return String.format("[%s.%s:%d]\n",stack.getClassName(),stack.getMethodName(),stack.getLineNumber());
	}
	public static final <T>void isTrue(boolean t,String expression){
		if (!t)
			throw new IllegalArgumentException(String.format("%s:experssion '%s' must be true",getLocation(),expression));
	}
	public static final <T>void isTrue(boolean t,String expression,String msg){
		if (!t)
			throw new IllegalArgumentException(String.format("%s:experssion '%s' must be true,%s",getLocation(),expression,msg));
	}
	public static final <T>void notNull(T t,String arg){
		if (null == t)
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null",getLocation(),arg));		
	}
	public static final <T>void notNull(T t,String arg,String msg){
		if (null == t)
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null,%s",getLocation(),arg,msg));		
	}
	public static final <T>void notEmpty(T[] t,String arg){
		if (null == t||0==t.length)
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty",getLocation(),arg));		
	}
	public static final <T>void notNullElement(T[] t,String arg){
		notEmpty(t,arg);
		for(int i=0;i<t.length;i++){			
			if (null == t[i]) {
				throw new IllegalArgumentException(String.format("%s:%s[%d] is null ",getLocation(),arg,i));
			}
		}
	}
	
	public static final void notEmptyElement(String[] t,String arg){
		notEmpty(t,arg);
		for(int i=0;i<t.length;i++){			
			if (null == t[i] || 0 == t[i].length()) {
				throw new IllegalArgumentException(String.format("%s:%s[%d] is null or empty",getLocation(),arg,i));
			}
		}
	}
	public static final void notEmptyElement(Buffer[] t,String arg){
		notEmpty(t,arg);
		for(int i=0;i<t.length;i++){			
			if (null == t[i] || !t[i].hasRemaining()) {
				throw new IllegalArgumentException(String.format("%s:%s[%d] is null or empty",getLocation(),arg,i));
			}
		}
	}
	public static final void notEmptyElement(Collection<Buffer> t,String arg){
		notEmpty(t,arg);
		for(Buffer buffer:t){			
			if (null == buffer || !buffer.hasRemaining()) {
				throw new IllegalArgumentException(String.format("%s:%s have null or empty element",getLocation(),arg));
			}
		}
	}
	public static final <T extends Collection<?>>void notEmpty(T t,String arg){
		if (null == t || t.isEmpty())
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty",getLocation(),arg));		
	}
	public static final <T extends Map<?,?>>void notEmpty(T t,String arg){
		if (null == t|| t.isEmpty())
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty",getLocation(),arg));		
	}
	public static final void notEmpty(byte[] t,String arg){
		if (null == t||0==t.length)
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty",getLocation(),arg));		
	}
	public static final void notEmpty(String t,String arg){
		if (null == t||0==t.length())
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty",getLocation(),arg));		
	}

	public static final void notEmpty(String t,String arg,String msg){
		if (null == t||0==t.length())
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty,%s",getLocation(),arg,msg));		
	}
	public static final void notEmpty(Buffer t,String arg){
		if (null == t || !t.hasRemaining())
			throw new IllegalArgumentException(String.format("%s:the argument %s must not be null or empty",getLocation(),arg));
	}

	public static final void assertValidCode(byte[] code1, byte[] code2){
		notEmpty(code1, "code1");
		notEmpty(code2, "code2");
		if(code1.length!=code2.length){
			throw new IllegalArgumentException(String.format("%s:INVALID CODE code1(%dbytes),code2(%dbytes)",getLocation(),code1.length,code2.length));
		}
	}

}
