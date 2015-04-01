package com.raritan.tdz.ip.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.ip.domain.IPTeaming;

public class IPTeamingDAOImpl extends DaoImpl<IPTeaming> implements IPTeamingDAO  {

	@Override
	public List<IPTeaming> getTeamsForIp(Long ipId) {

		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllTeamsForIp");
		query.setParameter("ipId", ipId);
		@SuppressWarnings("unchecked")
		List<IPTeaming> list = (List<IPTeaming>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();
		return list;
	}

	
	@Override
	public List<IPTeaming> getTeamsForIpAddress(String ipAddress, Long locationId) {

		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllTeamsForIpAddress");
		query.setParameter("ipAddress", ipAddress);
		query.setParameter("locationId", locationId);
		
		@SuppressWarnings("unchecked")
		List<IPTeaming> list = (List<IPTeaming>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();
		return list;
	}
	
	@Override
	public List<IPTeaming> getIpTeamsForItem(Long ipId, Long itemId) {
		Session session = this.getSession();
		Query query = session.getNamedQuery("IPAddress.findAllIpTeamsForItem");
		query.setParameter("ipId", ipId);
		query.setParameter("itemId", itemId);
		@SuppressWarnings("unchecked")
		List<IPTeaming> list = (List<IPTeaming>) query
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();
		return list;
	}

	@Override
	public IPTeaming getTeamForIpAndDataPort(Long ipId, Long dataPort) {

		IPTeaming retval = null;
		if( ipId != null && dataPort != null ){
			Session session = this.getSession();
			Query query = session.getNamedQuery("IPAddress.findTeamForIpAndDataPort");
			query.setParameter("ipId", ipId);
			query.setParameter("dataPortId", dataPort);
			@SuppressWarnings("unchecked")
			List<IPTeaming> list = (List<IPTeaming>) query
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
			.list();
			if( list.size() > 0 ){
				retval = list.get(0);
			}
		}
		return retval;
	}

	@Override
	public List<IPTeaming> getTeamsForDataPort(Long dataPort) {
		List<IPTeaming> retval = new ArrayList<IPTeaming>();
		if( dataPort != null ){
			Session session = this.getSession();
			Query query = session.getNamedQuery("IPAddress.findTeamForDataPort");
			query.setParameter("dataPortId", dataPort);
			@SuppressWarnings("unchecked")
			List<IPTeaming> list  = (List<IPTeaming>) query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			retval = list;	
		}
		return retval;
	}
	
	@Override
	public List<IPTeaming> getTeamsForItem(Long itemId) {
		List<IPTeaming> retval = new ArrayList<IPTeaming>();
		if( itemId != null ){
			Session session = this.getSession();
			Query query = session.getNamedQuery("IPAddress.findTeamForItem");
			query.setParameter("itemId", itemId);
			@SuppressWarnings("unchecked")
			List<IPTeaming> list  = (List<IPTeaming>) query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			retval = list;	
		}
		return retval;
	}
	
}
