package net.gdface.utils;

import java.io.PrintStream;

/**
 * 日志输出工具类
 * @author guyadong
 *
 */
public class SampleLog {

	private static void log(PrintStream printStream,int index, String format, Object ... args){
		StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[index];
		printStream.printf("[%s](%s:%d) %s\n",
				Thread.currentThread().getName(),
				stackTrace.getFileName(),
				stackTrace.getLineNumber(),
				String.format(format, args));
	}
	public static void log(PrintStream printStream,String format, Object ... args){
		log(printStream,3,format,args);
	}
	public static void log(String format, Object ... args){
		log(System.out,3,format,args);
	}
}
