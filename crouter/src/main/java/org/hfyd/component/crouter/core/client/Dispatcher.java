package org.hfyd.component.crouter.core.client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class Dispatcher {

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("CRouter-" + thread.getId());
            return thread;
        }
    };

    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(2, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>(), THREAD_FACTORY);

    void emit(Callable callable) {
        THREAD_POOL.submit(callable);
    }
}
