package com.raritan.tdz.reservation.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ReservationDetails;

/**
 * reservation details DAO
 * @author bunty
 *
 */
public interface ReservationDetailsDAO extends Dao<ReservationDetails> {

	/**
	 * get list of reservation number for a given parent item id
	 * @param parentItemId
	 * @return
	 */
	public List<String> getReservationNumber(Long parentItemId);
	
}
