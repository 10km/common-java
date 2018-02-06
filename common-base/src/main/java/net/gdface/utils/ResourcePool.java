package net.gdface.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 资源池管理对象<br>
 * {@link #apply()},{@link #free()}用于申请/释放资源,申请的资源对象不可跨线程调用,<br>
 * 通过重写{@link #isNestable()}方法决定是否允许嵌套调用
 * @author guyadong
 *
 * @param <R> 资源类型
 */
public class ResourcePool<R>{
	/** 资源队列 */
	protected final LinkedBlockingQueue<R> queue = new LinkedBlockingQueue<R>();
	/** 当前线程申请的资源对象 */
	private final ThreadLocal<R> tlsResource = new ThreadLocal<R>();
	/** 线程嵌套计数 */
	private final ThreadLocal<AtomicInteger> threadNestCount= new ThreadLocal<AtomicInteger>();
	private final boolean nestable = isNestable();
	protected ResourcePool() {
	}
	/**
	 * 构造方法
	 * @param resources 资源对象集合
	 * @throws IllegalArgumentException 包含{@code null}元素
	 */
	public ResourcePool(Collection<R> resources){
		for(R r:resources){
			if(null == r){
				throw new IllegalArgumentException("resources contains null element");
			}
			queue.add(r);
		}
	}
	@SafeVarargs
	public ResourcePool( R ...resources ){
		this(Arrays.asList(resources));
	}
	/**
	 * 从资源队列{@link #queue}中取出一个对象,保存到{@link #tlsResource}
	 * @return
	 * @throws InterruptedException
	 */
	private R getResource() throws InterruptedException{
		if(null != tlsResource.get()){
			// 资源状态异常
			throw new IllegalStateException("INVALID tlsResource state");
		}
		R r;
		if(queue.isEmpty() && null != (r = newResource())){
			queue.offer(r);
		}
		r = open(queue.take());
		tlsResource.set(r);
		return r;
	}
	/**
	 * 将{@link #tlsResource}中的资源对象重新加入资源队列{@link #queue},并清除TLS变量{@link #tlsResource}
	 */
	private void recycleResource(){
		R r = tlsResource.get();
		if(null == r){
			// 资源状态异常
			throw new IllegalStateException("INVALID tlsResource while recycle");
		}
		// 放回队例
		queue.offer(close(r));
		tlsResource.remove();
	}
	/**
	 * (阻塞式)申请当前线程使用的资源对象,不可跨线程使用
	 * @return
	 * @throws InterruptedException
	 */
	public final R applyChecked() throws InterruptedException{
		if(nestable){
			AtomicInteger count = threadNestCount.get();
			if(null == count){
				// 当前线程第一次申请资源
				count = new AtomicInteger(1);
				threadNestCount.set(count);
				return getResource();
			}else{
				// 嵌套调用时直接返回TLS变量
				if(null == this.tlsResource.get()){
					// 资源状态异常
					throw new IllegalStateException("INVALID tlsResource");
				}
				count.incrementAndGet();
				return this.tlsResource.get();
			}			
		}else{
			return getResource();
		}
	}
	/**
	 * (阻塞式)申请当前线程使用的资源对象,不可跨线程使用<br>
	 * {@link InterruptedException}封装到{@link RuntimeException}抛出
	 * @return
	 * @see #applyChecked()
	 */
	public final R apply(){
		try {
			return applyChecked();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 释放当前线程占用的资源对象，放回资源队列
	 */
	public final void free(){
		if(nestable){
			AtomicInteger count = threadNestCount.get();
			if(null == count){
				// 申请/释放没有成对调用
				throw new IllegalStateException("INVALID nestCount");
			}
			if( 0 == count.decrementAndGet()){
				threadNestCount.remove();
				recycleResource();
			}			
		}else{
			recycleResource();
		}
	}
	/** 是否允许嵌套 */
	protected boolean isNestable() {
		return false;
	}
	/**
	 * 创建一个新的资源对象
	 * @return
	 */
	protected R newResource(){
		return null;
	}
	/**
	 * 资源从队形从取出时调用,子类可重写此方法
	 * @param resource
	 * @return 返回 {@code resource
	 */
	protected R open(R resource){
		return resource;
	}
	/**
	 * 资源对象放回队列时调用,子类可重写此方法
	 * @param resource
	 * @return 返回 {@code resource}
	 */
	protected R close(R resource){
		return resource;
	}
	/**
	 * 以固定步长生成的一组数字为资源的资源池对象
	 * @author guyadong
	 *
	 */
	public static class IntResourcePool extends ResourcePool<Integer>{
		public IntResourcePool(Collection<Integer> resources) {
			super(resources);
		}
		public IntResourcePool(Integer... resources) {
			super(resources);
		}
		/**
		 * 构造方法<br>
		 * 起始为0,步长为1
		 * @param capacity
		 * @see #IntResourcePool(int, int, int)
		 */
		public IntResourcePool(int capacity){
			this(capacity,0, 1);
		}
		/**
		 * @param capacity 数组容量
		 * @param start 起始数字
		 * @param step 步长
		 * @throws IllegalArgumentException {@code capacity,step}<=0
		 */
		public IntResourcePool(int capacity,int start, int step){
			this(createList(capacity, start, step));
		}
		/**
		 * 用指定的参数构造一个数字列表
		 * @param capacity 数组容量
		 * @param start 起始数字
		 * @param step 步长
		 * @return
		 */
		private static List<Integer> createList(int capacity,int start, int step){
			if(capacity <= 0 ){
				throw new IllegalArgumentException(String.format("INVALID capacity:%d",capacity));
			}
			if(step <=0 ){
				throw new IllegalArgumentException("INVALID step:0");
			}
			ArrayList<Integer> list = new ArrayList<Integer>(capacity);
			for(int i = start ,endNu= start + capacity*step; i < endNu;i +=step ){
				list.add(i);
			}
			return list;
		}
	}	
}
