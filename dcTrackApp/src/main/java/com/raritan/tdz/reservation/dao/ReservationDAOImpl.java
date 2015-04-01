package com.raritan.tdz.reservation.dao;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.Reservations;

public class ReservationDAOImpl extends DaoImpl<Reservations> implements ReservationDAO {

	@Override
	public Reservations loadReservation(Long id) {
		return loadReservation(id, true);
	}

	@Override
	public Reservations loadReservation(Long id, boolean readOnly) {
		Session session = null;
		Reservations retval = null;
	
		try{
			if( id != null && id > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(Reservations.class);
				criteria.setFetchMode("statusLookup", FetchMode.JOIN);
				criteria.add(Restrictions.eq("reservationId", id));
				criteria.setReadOnly(readOnly);
				retval = (Reservations)criteria.uniqueResult();
			}
		}
		finally{
			if( session != null ){
				session.close();
			}
		}
		return retval;	
	}

	@Override
	public Reservations getReservation(Long id) {
		return this.read(id);
	}

	@Override
	public void delete(Long id) {
		Reservations object = this.read(id);
		
		if(object != null){
			this.delete(object);
		}
	}

}
