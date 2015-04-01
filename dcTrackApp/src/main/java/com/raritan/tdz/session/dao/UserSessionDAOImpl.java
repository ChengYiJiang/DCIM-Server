/**
 * 
 */
package com.raritan.tdz.session.dao;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.DctrackUserUUIDMappings;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author prasanna
 *
 */
public class UserSessionDAOImpl extends DaoImpl<UserInfo> implements UserSessionDAO{
	private final Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	@Transactional
	public UserInfo getUserInfo(String sessionUUID) throws DataAccessException {
		UserInfo userInfo = null;
		
		Session session = getSession();

		try {
			Criteria criteria = session.createCriteria(DctrackUserUUIDMappings.class);
			criteria.add(Restrictions.eq("uuidSessionKeyString", sessionUUID));
			DctrackUserUUIDMappings uuid = (DctrackUserUUIDMappings)criteria.uniqueResult();
			
			if (uuid != null) {
				//Load user information
				Query q = session.getNamedQuery("UserInfoList");
				q.setString("sessionId", sessionUUID);

				for(Object u:q.list()){             //get the first user records. Could be multiple if user
					userInfo = (UserInfo)u;         //belong to more than one group
					break;
				}
				
				if(userInfo == null){
					userInfo = new UserInfo();
					userInfo.setSessionId(uuid.getUuidSessionKeyString());
				}
				
				
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
				
				session.clear();
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
