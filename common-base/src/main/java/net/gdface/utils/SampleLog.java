package net.gdface.utils;

import java.io.PrintStream;

/**
 * 简单日志输出工具类
 * @author guyadong
 *
 */
public class SampleLog {
	private static final String DELIM_STR = "{}";
	private static final Object[] EMPTY_ARGS = new Object[0];
	private static void log(PrintStream printStream,int level, String format, Object ... args){
		if(null == printStream || null == format){
			return;
		}
		if(null == args){
			args = EMPTY_ARGS;
		}
		StringBuilder buffer = new StringBuilder(format.length() + 64);
		int beginIndex = 0,endIndex=0,count=0;
		while((endIndex = format.indexOf(DELIM_STR, beginIndex))>=0){
			buffer.append(format.substring(beginIndex, endIndex));
			try{
				buffer.append(args[count++]);
			}catch(IndexOutOfBoundsException e){
				// 数组越界时对应占位符填null
				buffer.append("null");
			}
			beginIndex = endIndex + DELIM_STR.length();
		}
		buffer.append(format.substring(beginIndex,format.length()));
		Thread currentThread = Thread.currentThread();
		StackTraceElement stackTrace = currentThread.getStackTrace()[level];
		printStream.printf("[%s] (%s:%d) %s\n",
				currentThread.getName(),
				stackTrace.getFileName(),
				stackTrace.getLineNumber(),
				buffer.toString());
	}
	/**
	 * 向{@code printStream}输出日志信息<br>
	 * example:
	 * <pre>
	 * log("name : {},age:{}","tom",23);
	 * </pre>
	 * @param printStream
	 * @param format 格式字符串,采用{@code '{}'}为占位符,占位符个数要与{@code args}数组长度匹配
	 * @param args
	 */
	public static void log(PrintStream printStream,String format, Object ... args){
		log(printStream,3,format,args);	
	}
	/**
	 * 向控制台输出日志信息<br>
	 * @param format 格式字符串,采用{@code '{}'}为占位符
	 * @param args
	 * @see #log(PrintStream, String, Object...)
	 */
	public static void log(String format, Object ... args){
		log(System.out,3,format,args);
	}
}
