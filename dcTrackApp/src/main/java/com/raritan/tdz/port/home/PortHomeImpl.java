package com.raritan.tdz.port.home;

 import java.sql.Timestamp;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Implementation for extended PortHome interface.
 * @author Andrew Cohen
 */
public class PortHomeImpl extends com.raritan.tdz.home.PortHomeImpl implements PortHome {

	private SessionFactory sessionFactory;
	
	public PortHomeImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
		this.sessionFactory = sessionFactory;
	}
	
	@Transactional
	@Override
	public long savePowerPort(PowerPort powerPort) throws DataAccessException{
		Session session = null;

		long id = 0L;
		
		try{
			
			session = this.sessionFactory.getCurrentSession();
			if (powerPort != null){
				if (powerPort.getPortId() > 0){
					session.merge(powerPort);
					id = powerPort.getPortId();
				} else {
				
					powerPort.setCreationDate(new Timestamp(System.currentTimeMillis()));
					
					id = (Long)session.save(powerPort);
				}
			}
			
		}catch(HibernateException e){
			
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			 
		}catch(org.springframework.dao.DataAccessException e){
			
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return id;
	}

	@Override
	public void lockPort(IPortInfo port) throws DataAccessException {
		if (port instanceof DataPort) {
			lockDataPort((DataPort)port, true);
		}
		else {
			lockPowerPort((PowerPort)port, true);
		}
	}
	
	public void unlockPort(IPortInfo port) throws DataAccessException {
		if (port instanceof DataPort) {
			lockDataPort((DataPort)port, false);
		}
		else {
			lockPowerPort((PowerPort)port, false);
		}
	}

	//
	// Private methods
	//
	private void lockDataPort(DataPort dataPort, boolean lockFlag) throws DataAccessException {
		Timestamp updateDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session.createQuery("update DataPort set used = :used, updateDate = :updateDate where portId = :portId and used != :used");
			q.setLong("portId", dataPort.getPortId());
			q.setBoolean("used", lockFlag);
			q.setTimestamp("updateDate", updateDate);
			q.executeUpdate();
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
		}
	}
	
	private void lockPowerPort(PowerPort powerPort, boolean lockFlag) throws DataAccessException {
		Timestamp updateDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session.createQuery("update PowerPort set used = :used, updateDate = :updateDate where portId = :portId and used != :used");
			q.setLong("portId", powerPort.getPortId());
			q.setBoolean("used", lockFlag);
			q.setTimestamp("updateDate", updateDate);
			q.executeUpdate();
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
		}
	}
}
