package com.raritan.tdz.piq.home;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;


/**
 * A thread pool for handling certain PIQ related tasks in asynchronously. This important
 * for cases where we have long running operations that we don't want to block the 
 * Listen/Notify threads.
 * 
 * @author Andrew Cohen
 */
public class PIQAsyncTaskService implements ThreadFactory, RejectedExecutionHandler {

	private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final Logger log = Logger.getLogger("PIQSyncLogger");
    
    private final ScheduledThreadPoolExecutor scheduler;

    PIQAsyncTaskService(int coreThreads, int keepAliveSecs) {
		SecurityManager s = System.getSecurityManager();
        group = (s != null)? s.getThreadGroup() :
                             Thread.currentThread().getThreadGroup();
        namePrefix = "PIQ PDU Task Thread "+
                      poolNumber.getAndIncrement();
        
		scheduler = new ScheduledThreadPoolExecutor(coreThreads, (RejectedExecutionHandler)this);
		scheduler.setThreadFactory( this );
		scheduler.setKeepAliveTime(keepAliveSecs, TimeUnit.SECONDS);
		scheduler.allowCoreThreadTimeOut( true );
	}
    
    /**
     * Run a PIQ task in this thread pool.
     * @param task
     * @param delaySeconds Run task after delay
     */
    public void runDelayedTask(Runnable task, long delaySeconds) {
    	// Give the listen/notify thread time to complete the initial transaction
    	scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
    }
    
    /**
     * Run a PIQ task in this thread pool.
     * @param task
     * @param delaySeconds Run task after delay
     */
    public void runImmediateTask(Runnable task) {
    	scheduler.execute( task );
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.MIN_PRIORITY)
            t.setPriority(Thread.MIN_PRIORITY);
        return t;
    }

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		log.error("PIQ Async Task Service: Rejected task!");
	}
}
