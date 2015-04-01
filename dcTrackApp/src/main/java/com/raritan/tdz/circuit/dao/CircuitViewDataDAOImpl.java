package com.raritan.tdz.circuit.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.CircuitViewData;

/**
 * 
 * @author bunty
 *
 */
public class CircuitViewDataDAOImpl extends DaoImpl<CircuitViewData> implements
		CircuitViewDataDAO {

	@Override
	public CircuitViewData getCircuitViewData(Long circuitId, Long circuitType) {
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(type);
		criteria.add(Restrictions.eq("circuitId", circuitId.longValue()));
		criteria.add(Restrictions.eq("circuitType", circuitType.longValue()));
		
		return (CircuitViewData) criteria.uniqueResult();
		
	}

}
