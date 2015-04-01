package com.raritan.tdz.user.dao;

import java.math.BigInteger;

import org.hibernate.Query;
import org.hibernate.Session;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.UserInfo;

/* 
 * Note: There are no mapping classes defined for User, UserPref etc. 
 * sql query result is mapped to UserInfo. Therefore I have to write
 * sql queries in the following case. 
 * 
 */

public class UserDAOImpl extends DaoImpl<UserInfo> implements UserDAO {
	
	@Override
	public Boolean getUserRequestByPassSetting(long userId) {
		Boolean rb = false;
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT up.requestbypass from users u ");
		sb.append("LEFT JOIN user_prefs up on up.user_id = u.id ");
		sb.append("where u.id=:userId");
		
		Session session =  this.getSession();	
		Query query = session.createSQLQuery( sb.toString() );
		query.setLong("userId", userId);

		rb = (Boolean)query.uniqueResult();
		
		return rb;
		
		/*
		Session session =  this.getSession();	
		Criteria criteria = session.createCriteria(UserInfo.class);
		criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
		criteria.add(Restrictions.eq("userId", userId));
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("requestBypass"), "requestBypass");
		criteria.setProjection(proList);
		
		
    	return (boolean)criteria.uniqueResult(); */
	}

	@Override
	public BigInteger getUserAccessLevelLkpValueCode(long userId) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT lksdata.lkp_value_code FROM users u ");
		sb.append("LEFT JOIN dct_lks_data lksdata ON lksdata.lks_id = u.accessLevelId ");
		sb.append("WHERE u.id=:userId");
		
		Session session =  this.getSession();	
		Query query = session.createSQLQuery( sb.toString() );
		query.setLong("userId", userId);

		return (BigInteger)query.uniqueResult();
	}

	@Override
	public String getLockRequestByPassButtonSetting() {
		String qryStr = "SELECT s.parameter FROM tblsettings s WHERE s.setting=:setting";
		
		Session session =  this.getSession();	
		Query query = session.createSQLQuery( qryStr);
		query.setString("setting", "LockRequestBypassButton");

		return (String)query.uniqueResult();
	}

	@Override
	public void setUserRequestByPassSetting(boolean requestBypass, long userId) {
		String qryStr = "UPDATE user_prefs SET requestbypass = :requestBypass WHERE user_id =:userId";
		
		Session session =  this.getSession();	
		Query query = session.createSQLQuery( qryStr );
		query.setBoolean("requestBypass", requestBypass);
		query.setLong("userId", userId);
		query.executeUpdate();
	}
	
}
