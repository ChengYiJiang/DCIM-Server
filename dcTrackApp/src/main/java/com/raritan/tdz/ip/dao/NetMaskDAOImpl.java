package com.raritan.tdz.ip.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.ip.domain.NetMask;

public class NetMaskDAOImpl extends DaoImpl<NetMask>  implements NetMaskDAO  {

	@Override
	public List<NetMask> getByMask(String mask) {
		List<NetMask> retval = null;
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("NetMask.getNetMaskByMask");
		query.setParameter("mask", mask);

		retval = (List<NetMask>) query.list();
	
		return retval;

	}

	@Override
	public List<NetMask> getById(Long id) {
		NetMask retval = null;
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("NetMask.getNetMaskById");
		query.setParameter("id", id.longValue());

		List<NetMask> list = (List<NetMask>) query.list();
	
		return list;
	}

	@Override
	public List<NetMask> getByCidr(Long cidr) {
		List<NetMask> retval = null;
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("NetMask.getNetMaskByCidr");
		query.setParameter("cidr", cidr);

		retval = (List<NetMask>) query.list();
	
		return retval;

	}

	@Override
	public List<NetMask> getAll() {
		List<NetMask> retval = null;
		
		Session session = this.getSession();
		Query query = session.getNamedQuery("NetMask.getAllNetMasks");

		retval = (List<NetMask>) query.list();
	
		return retval;
		
	}
}
