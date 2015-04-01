package com.raritan.tdz.user.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.cmn.Users;

/**
 * DAO layer for "users" table
 * @author bunty
 *
 */
public class UsersDAOImpl extends DaoImpl<Users> implements UsersDAO {

	@Override
	public Users getUser(String userId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(type);
		criteria.add(Restrictions.eq("userId", userId));
		
		return (Users) criteria.uniqueResult();
		
	}

}
