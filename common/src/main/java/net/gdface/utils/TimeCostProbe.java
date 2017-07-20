package net.gdface.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  耗时统计对象<br>
 *  用于程序执行耗时统计
 * @author guyadong
 *
 */
public class TimeCostProbe{
	/**
	 * 统计时间(毫秒)
	 */
	private AtomicLong timeCostMills=new AtomicLong(0);
	/**
	 * 统计次数
	 */
	private AtomicLong count=new AtomicLong(0);
	/**
	 * 统计位置
	 */
	private String location=null ;
	/**
	 * 获取当前方法名
	 * @return
	 */
	private String getMethodName(){
		StackTraceElement stack = Thread.currentThread().getStackTrace()[3];
		return stack.getClassName()+"."+stack.getMethodName();
	}
	/**
	 * 计时开始
	 * @return
	 */
	public TimeCostProbe begin() {
		location=getMethodName();
		timeCostMills.set(System.currentTimeMillis());
		return this;
	}
	/**
	 * 计时结束
	 * @return
	 */
	public TimeCostProbe end(){
		if(0==timeCostMills.get())
			throw new IllegalArgumentException("begin/end not match");
		if(!getMethodName().equals(location))
			throw new IllegalArgumentException("begin/end must be call in same method");
		timeCostMills.set(System.currentTimeMillis()-timeCostMills.get());
		count.set(1);
		return this;}		
	/**
	 * 计时结果相加
	 * @param mills
	 * @return
	 */
	public TimeCostProbe add(long mills){			
		timeCostMills.addAndGet(mills);
		count.incrementAndGet();
		return this;
	}
	public long getTimeCostMills() {
		return timeCostMills.get();
	}
	public long getTimeCost(TimeUnit unit) {
		return unit.convert(timeCostMills.get(), TimeUnit.MICROSECONDS);
	}
	/**
	 * 向输出设备打印耗时统计信息
	 * @see #log(String)
	 */
	public void print(){
		System.out.println(log(location));
	}
	/**
	 * 输出耗时统计信息
	 * @param name
	 * @return
	 */
	public String log(String name){
		if(0==count.get())
			throw new IllegalArgumentException("begin/end not match");
		return String.format("%s: average cost:%f seconds(sample count %d)\n",name,timeCostMills.get()/1000d/count.get(),count.get());
	}
	public String log(){
		return log(location);
	}
}