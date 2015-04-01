
package com.raritan.tdz.searchplace.dao;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.searchplace.domain.PlaceSearchPortPower;

public class PlaceSearchPortPowerDAOImpl extends DaoImpl<PlaceSearchPortPower> implements PlaceSearchPortPowerDAO{

	@Override
	public PlaceSearchPortPower loadPlaceSearchPortPower(Long id) {
		return loadPlaceSearchPortPower( id, false);
	}

	@Override
	public PlaceSearchPortPower loadPlaceSearchPortPower(Long id, boolean readOnly) {

		Session session = null;
		PlaceSearchPortPower retval = null;
		
		try{
			if( id != null && id > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(PlaceSearchPortPower.class);
				criteria.setFetchMode("placeSearch", FetchMode.JOIN);
				criteria.setFetchMode("classLookup", FetchMode.JOIN);
				criteria.setFetchMode("colorLookup", FetchMode.JOIN);
				criteria.setFetchMode("connectorLookup", FetchMode.JOIN);
				criteria.setFetchMode("phaseLookup", FetchMode.JOIN);
				criteria.setFetchMode("voltsLookup", FetchMode.JOIN);				
				criteria.add(Restrictions.eq("placeSearchPortPowerId", id));
				criteria.setReadOnly(readOnly);
				retval = (PlaceSearchPortPower)criteria.uniqueResult();
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
	public PlaceSearchPortPower getPlaceSearchPortPower(Long id) {
		return this.read(id);
	}

}
