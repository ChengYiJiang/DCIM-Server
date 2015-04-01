/**
 * 
 */
package com.raritan.tdz.item.home;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author prasanna
 *
 */
@Aspect
public class UnitTestSessionMgmtAspect {

	SessionFactory sf;
	SessionHolder origSessionHolder;
	
	public SessionFactory getSf() {
		return sf;
	}

	public void setSf(SessionFactory sf) {
		this.sf = sf;
	}

	@Before("execution(public * com.raritan.tdz.item.home.UnitTestItemDAO.*(..))")
	public void openTransaction(){
		origSessionHolder = getSessionObject();
		
		Session session = sf.openSession();
		
		setSessionObject(new SessionHolder(session));
	}
	
	@After("execution(public * com.raritan.tdz.item.home.UnitTestItemDAO.*(..))")
	public void closeTransaction(){
		if (getSessionObject() != null){
			Session s = getSessionObject().getSession();
			s.flush();
			SessionFactoryUtils.closeSession(s);
		}
		setSessionObject(origSessionHolder);
		
		try {
			sf.getCurrentSession();
		} catch (HibernateException e){
			//origSessionHolder.clear();
			// JB check this hibernate4 .. move error commenting //origSessionHolder.addSession(sf.openSession());
		}
	}
	
	private SessionHolder getSessionObject(){
		SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
		return holder;
	}
	
	private void setSessionObject(SessionHolder sessionHolder){
		TransactionSynchronizationManager.unbindResource(sf);
		if (sessionHolder != null)
			TransactionSynchronizationManager.bindResource(sf, sessionHolder);
	}
}
