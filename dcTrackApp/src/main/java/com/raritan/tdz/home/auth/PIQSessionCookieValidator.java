package com.raritan.tdz.home.auth;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.DctrackUserUUIDMappings;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Authentication service implementation which validates a
 * pre-authenticated PIQ session key.
 * @author Andrew Cohen
 *
 */
public class PIQSessionCookieValidator implements AuthHome {
	private SessionFactory sessionFactory;
	private static Logger logger = Logger.getLogger(PIQSessionCookieValidator.class);

	public PIQSessionCookieValidator(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public UserInfo authenticate(String username, String password)
			throws DataAccessException {
		UserInfo userInfo = null;
		
		// We only look at the password since that is the session ID
		String sessionUUID = password;
		
		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria criteria = session.createCriteria(DctrackUserUUIDMappings.class);
			criteria.add(Restrictions.eq("uuidSessionKeyString", sessionUUID));
			DctrackUserUUIDMappings uuid = (DctrackUserUUIDMappings)criteria.uniqueResult();
			
			if (uuid != null) {
				userInfo = new UserInfo();
				userInfo.setSessionId(uuid.getUuidSessionKeyString());
				
				Integer timeoutMins = (Integer)session.createSQLQuery("select session_timeout from rails_options").uniqueResult();
				if (timeoutMins == null) {
					logger.warn("No session_timeout value found in rails_options table! Will set default timeout to 1 day.");
					timeoutMins = 1440;
				}
				
				// Set user session timeout in milliseconds
				userInfo.setSessionTimeout(timeoutMins * 60 * 1000);
				// TODO: May eventually want to add more information to the user bean 
				
				if (logger.isDebugEnabled()) {
					logger.debug("SessionHome.isAuthenticated('"+sessionUUID+"') : true");
				}
			}
			else {
				userInfo = null;
				if (logger.isDebugEnabled()) {
					logger.debug("SessionHome.isAuthenticated('"+sessionUUID+"') : false");
				}
			}
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext("Exception in trying to fetch Cookie", this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext("Exception in trying to fetch Cookie", this.getClass(), e));
		}

		return userInfo;
	}
}
