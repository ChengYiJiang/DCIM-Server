package com.raritan.tdz.circuit.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.util.ExceptionContext;

/**
 * A semaphore based locking mechanism used to prohibit simultaneous edits to the same circuit.
 * Since saving a circuit is not an atomic operation with respect to database transactions,
 * this ensures that simultaneous edits to the same circuit do not corrupt data integrity.
 * The last edit to get the lock and do its work will ultimately be the final state of the circuit.
 * 
 * @author Andrew Cohen
 */
public class CircuitEditLock implements Runnable {
	private Logger log = Logger.getLogger("CircuitEditLock");
	private ConcurrentMap<String, Lock> lockMap;
	private boolean enabled;
	private long maxLockIdleMillis;
	private ScheduledThreadPoolExecutor cleanupExec;
	private ResourceBundleMessageSource messageSource;
	
	public CircuitEditLock() {
		enabled = false;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		if (enabled) {
			if (lockMap == null) {
				lockMap = new ConcurrentHashMap<String, Lock>();
			}
		}
	}

	public void setMaxLockIdleMinutes(int maxLockIdleMinutes) {
		this.maxLockIdleMillis = maxLockIdleMinutes * 60000;
	}
	
	public void setCleanupIntervalMinutes(int cleanupIntervalMinutes) {
		
		if (enabled) {
			// Schedule cleanup thread
			if (cleanupExec == null) {
				cleanupExec = new ScheduledThreadPoolExecutor(1);
				cleanupExec.setMaximumPoolSize( 1 );
				cleanupExec.setKeepAliveTime(5, TimeUnit.SECONDS);
				cleanupExec.allowCoreThreadTimeOut(true);
				cleanupExec.scheduleWithFixedDelay(this, cleanupIntervalMinutes, cleanupIntervalMinutes, TimeUnit.MINUTES);	
			}
		}
	}
	
	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Cleans up idle locks.
	 */
	public void cleanup() {
		if (!enabled) return;
		final int origSize = lockMap.size();
		if (origSize == 0) return;
		final long currentTime = System.currentTimeMillis();
		
		for (String lockId : lockMap.keySet()) {
			Lock lock = lockMap.get( lockId );
			
			if (lock != null) {
				long idle = currentTime - lock.lastRequested;
				if (idle > maxLockIdleMillis) {
					lockMap.remove( lockId );
					lock.sem.drainPermits();
					if (log.isDebugEnabled()) {
						log.debug("Removed idle lock " + lockId);
					}
				}
			}
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Cleaned up " + (lockMap.size() - origSize) + " circuit edit lock(s)");
		}
	}
	
	public void run() {
		if (!enabled) return;
		Thread.currentThread().setName("Circuit Lock Cleanup Thread");
		cleanup();
	}
	
	/**
	 * Acquire a lock for editing an existing circuit.
	 * If another thread is modifying the circuit, it will wait until the operation is complete.
	 * @param circuit
	 * @return
	 */
	Lock acquireLock(CircuitDTO circuit) throws InterruptedException, BusinessValidationException {
		if (!enabled) return null;
		Lock lock = null;
		String lockId = getLockId( circuit );
		
		if (lockId != null) {
			
			synchronized (lockMap) {
				lock = lockMap.get( lockId );
				if (lock == null) {
					lock = new Lock( circuit.isStatusInstalled(), lockId );
					lockMap.put(lockId, lock);
				}
			}
					
			if (log.isDebugEnabled()) {
				log.debug("Before lock acquire for " + lockId + ", availablePermits = " + 
						lock.sem.availablePermits() + ", queued threads " + lock.sem.getQueueLength());
			}
			
			lock.acquire(); // Blocks until other threads are finished modifying this circuit
			
			if (log.isDebugEnabled()) {
				log.debug("Acquired lock for " + lockId);
			}
		}
		
		return lock;
	}
	
	/**
	 * Releases the edit lock for the associated circuit.
	 * @param circuit
	 * @return
	 */
	boolean releaseLock( Lock lock ) {
		if (!enabled) return false;
		
		if (lock != null) {
			lock.release();
			if (log.isDebugEnabled()) {
				log.debug("Released lock for " + lock.lockId);
			}
			return true;
		}
		
		return false;
	}
	
	private String getLockId(CircuitDTO circuit) {
		if (circuit == null) return null;
		CircuitUID circuitUID = circuit.getCircuitUID();
		if (circuitUID == null) return null; // No lock for a new circuit
		return circuitUID.toString();
	}
	
	/**
	 * A Lock wrapping a semaphore with a last accessed time.
	 * Last accessed time determines when idle locks can be removed from the map.
	 * @author Andrew Cohen
	 */
	public class Lock {
		private Semaphore sem;
		private long lastRequested;
		private boolean installedCircuit;
		private String lockId;
		
		private Lock(boolean installedCircuit, String lockId) {
			this.lockId = lockId;
			this.installedCircuit = installedCircuit;
			this.sem = new Semaphore(1, true);
			this.lastRequested = System.currentTimeMillis();
		}
		
		private void acquire() throws InterruptedException, BusinessValidationException {
			lastRequested = System.currentTimeMillis();
			
			// If installed circuit edit in progress, warn the user don't allow the edit
			// because we don't want two proposed circuits!
			if (installedCircuit && (sem.availablePermits() == 0)) {
				String msg = messageSource.getMessage("circuit.editInstalledWithExistingProposed", null, null);
				BusinessValidationException be = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				be.addValidationError( msg );
				throw be;
			}
			
			sem.acquire();
		}
		
		private void release() {
			sem.release();
		}
		
		public String getLockId() {
			return lockId;
		}
	}
}
