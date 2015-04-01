package com.raritan.tdz.reservation.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.ReservationDetails;

/**
 * reservation details DAO implementation
 * @author bunty
 *
 */
public class ReservationDetailsDAOImpl extends DaoImpl<ReservationDetails> implements ReservationDetailsDAO {

	@Override
	public List<String> getReservationNumber(Long parentItemId) {
		
		Session session = this.getSession();
		
		Criteria c = session.createCriteria(type);
		c.createAlias("parentItem", "parentItem");
		c.createAlias("reservation", "reservation");
		
		c.add(Restrictions.eq("parentItem.itemId", parentItemId));
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("reservation.reservationNo"), "reservationNumber");
		c.setProjection(proList);
		
		@SuppressWarnings("unchecked")
		List<String> reservationNumbers = c.list();
		
		return reservationNumbers;
	}

}
