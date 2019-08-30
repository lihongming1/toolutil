package com.danke.scm.suitegoods.base.thread;

import java.util.concurrent.*;

/**
 * 线程池阻塞工具类
 */
public class ThreadPoolBlockUtil {

    private ThreadPoolBlockUtil() {
    }

    public static ThreadPoolExecutor build(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            MyBlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, handler);

        executor.prestartAllCoreThreads(); // 预启动所有核心线程

        return executor;
    }

    /**
     * 自定义阻塞队列，重写offer方法，实现阻塞功能
     *
     * @param <E>
     */
    static class MyBlockingQueue<E> extends ArrayBlockingQueue<E> {
        public MyBlockingQueue(int capacity) {
            super(capacity);
        }

        @Override
        public boolean offer(E e) {
            try {
                put(e);
                return true;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

}
