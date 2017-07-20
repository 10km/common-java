/**   
 * @Title: ShareLock.java 
 * @Package net.gdface.worker 
 * @Description: TODO 
 * @author guyadong   
 * @date 2015年6月10日 上午9:00:50 
 * @version V1.0   
 */
package net.gdface.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 共享锁<br>
 * 实现固定数目 {@link #maxShareCount} 的资源共享锁,限制并发线程数目.<br>
 * 同一个线程内嵌套加锁解锁,不会重复计数
 * @see Lock 
 * @see AbstractQueuedSynchronizer 
 * @author guyadong
 *
 */
public class ShareLock implements Lock {
	private static final class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = -3340303865224708218L;
		/**
		 * 线程加锁计数
		 */
		private final ThreadLocal<Integer> threadLockCount=new ThreadLocal<Integer>();
		Sync(int count) {
			if (count <= 0) {
				throw new IllegalArgumentException("maxShareCount must large than zero.");
			}
			setState(count);
		}
		@Override
		public int tryAcquireShared(int acquireCount) {
			Integer tlc=threadLockCount.get();
			if (null == tlc) {
				for (;;) {
					int current = getState();
					int newCount = current - acquireCount;
					if (newCount < 0) {
						return newCount;
					}else if (compareAndSetState(current, newCount)) {
						threadLockCount.set(acquireCount);
						return newCount;
					}
				}
			}else{
				tlc+=acquireCount;
				return getState();
			}
		}
		@Override
		public boolean tryReleaseShared(int releaseCount) {
			Integer tlc = threadLockCount.get();
			if(null == tlc || tlc <= 0)
				throw new IllegalStateException("Error threadLockCount");
			if ((tlc -= releaseCount) > 0) {
				return true;
			} else {
				if(tlc!=0)
					throw new IllegalStateException("Error threadLockCount");
				for (;;) {
					int current = getState();
					int newCount = current + releaseCount;
					if (compareAndSetState(current, newCount)) {
						threadLockCount.set(null);
						return true;
					}
				}
			}
		}
	}
	/**
	 * 可用资源计数
	 */
	private final int maxShareCount;
	/**
	 * 同步对象
	 */
	private final Sync sync;
	/**
	 * @param maxShareCount 最大可用资源计数
	 */
	public ShareLock(int maxShareCount) {
		this.maxShareCount = maxShareCount;
		this.sync = new Sync(this.maxShareCount);
	}
	@Override
	public void lock() {
		sync.acquireShared(1);
	}
	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}
	@Override
	public Condition newCondition() {
		//不支持该方法
		throw new UnsupportedOperationException();
	}
	@Override
	public boolean tryLock() {
		return sync.tryAcquireShared(1) >= 0;
	}
	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireSharedNanos(1, unit.toNanos(time));
	}

	@Override
	public void unlock() {
		sync.releaseShared(1);
	}
	/**
	 * @return maxShareCount
	 */
	public int getMaxShareCount() {
		return maxShareCount;
	}
}