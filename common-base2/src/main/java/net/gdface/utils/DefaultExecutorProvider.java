package net.gdface.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.MoreObjects;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * 提供全局线程池对象<br>
 * 线程池对象在应用程序结束时自动退出
 * @author guyadong
 *
 */
public class  DefaultExecutorProvider{
    private static final int CACHE_COREPOOLSIZE = Runtime.getRuntime().availableProcessors();
    private static final int CACHE_MAXIMUMPOOLSIZE = Runtime.getRuntime().availableProcessors()*4;
    private static final long CACHE_KEEPALIVETIME = 60L;
    private static final int CACHE_QUEUECAPACITY = 1024;
    private static final String CACHE_NAMEFORMAT = "cached-pool-%d";
    private static final int CACHE_TIMER_COREPOOLSIZE = 1;
    private static final String TIMER_NAMEFORMAT = "timer-pool-%d";

    private static class Singleton{
        private static final DefaultExecutorProvider INSTANCE = new DefaultExecutorProvider();
    }
    /** 全局线程池(自动退出封装) */
    protected final ExecutorService globalExecutor = createExitingCachedPool();
    /** 定时任务线程池对象(自动退出封装) */
    protected final ScheduledExecutorService timerExecutor = createExitingScheduledPool();
    /** 根据配置文件指定的参数创建通用任务线程池对象 */
    protected static final ExecutorService createCachedPool(
            Integer corePoolSize,
            Integer maximumPoolSize,
            Long keepAliveTime,
            Integer queueCapacity,
            String nameFormat ){
        ExecutorService executor = MoreExecutors.getExitingExecutorService(
                new ThreadPoolExecutor(
                        MoreObjects.firstNonNull(corePoolSize, CACHE_COREPOOLSIZE), 
                        MoreObjects.firstNonNull(maximumPoolSize, CACHE_MAXIMUMPOOLSIZE),
                        MoreObjects.firstNonNull(keepAliveTime, CACHE_KEEPALIVETIME),
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>(MoreObjects.firstNonNull(queueCapacity,CACHE_QUEUECAPACITY)),
                        new ThreadFactoryBuilder()
                            .setNameFormat(MoreObjects.firstNonNull(nameFormat,CACHE_NAMEFORMAT))
                            .build())
                );
        return executor;
    }
    /** 根据配置文件指定的参数创建定时任务线程池对象 */
    protected static final ScheduledExecutorService createScheduledPool(Integer corePoolSize,String nameFormat){
        ScheduledExecutorService timerExecutor = MoreExecutors.getExitingScheduledExecutorService(
                new ScheduledThreadPoolExecutor(
                    MoreObjects.firstNonNull(corePoolSize, CACHE_TIMER_COREPOOLSIZE), 
                    new ThreadFactoryBuilder()
                        .setNameFormat(MoreObjects.firstNonNull(nameFormat,TIMER_NAMEFORMAT))
                        .build()));
        return timerExecutor;
    }
    /**
     * 创建一个自动退出封装的全局线程池,
     * 子类可以使用不同的参数调用{@link #createCachedPool(Integer, Integer, Long, Integer, String)}重写此方法
     * @return
     */
    protected ExecutorService createExitingCachedPool() {
        return createCachedPool(null,null,null,null,null);
    }
    /**
     * 创建一个自动退出封装的定时任务线程池,
     * 子类可以使用不同的参数调用{@link #createScheduledPool(Integer, String)}重写此方法
     * @return
     */
    protected ScheduledExecutorService createExitingScheduledPool() {
        return createScheduledPool(null,null);
    }
    protected DefaultExecutorProvider() {
    }
    /** 返回全局线程池 */
    public static ExecutorService getGlobalExceutor() {
        return Singleton.INSTANCE.globalExecutor;
    }
    /** 返回定时任务线程池 */
    public static ScheduledExecutorService getTimerExecutor() {
        return Singleton.INSTANCE.timerExecutor;
    }
}
