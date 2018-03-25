package net.gdface.utils;

import java.io.PrintStream;

/**
 * 日志输出工具类
 * @author guyadong
 *
 */
public class SampleLog {

	public static void log(PrintStream printStream,String format, Object ... args){
		StackTraceElement stackTrace = Thread.currentThread() .getStackTrace()[2];
		printStream.printf("[%s.%s:%d]%s\n", 
				stackTrace.getClassName(),
				stackTrace.getMethodName(),
				stackTrace.getLineNumber(),
				String.format(format, args));
	}
	public static void log(String format, Object ... args){
		log(System.out,format,args);
	}
}
