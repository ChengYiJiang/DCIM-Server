
package com.raritan.tdz.searchplace.dao;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.searchplace.domain.PlaceSearchPortData;

public class PlaceSearchPortDataDAOImpl extends DaoImpl<PlaceSearchPortData> implements PlaceSearchPortDataDAO{

	@Override
	public PlaceSearchPortData loadPlaceSearchPortData(Long id) {
		return loadPlaceSearchPortData( id, false);
	}

	@Override
	public PlaceSearchPortData loadPlaceSearchPortData(Long id, boolean readOnly) {

		Session session = null;
		PlaceSearchPortData retval = null;
		
		try{
			if( id != null && id > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(PlaceSearchPortData.class);
				criteria.setFetchMode("placeSearch", FetchMode.JOIN);
				criteria.setFetchMode("classLookup", FetchMode.JOIN);
				criteria.setFetchMode("colorLookup", FetchMode.JOIN);
				criteria.setFetchMode("connectorLookup", FetchMode.JOIN);
				criteria.setFetchMode("mediaLookup", FetchMode.JOIN);
				criteria.setFetchMode("vlanLookup", FetchMode.JOIN);				
				criteria.add(Restrictions.eq("placeSearchPortDataId", id));
				criteria.setReadOnly(readOnly);
				retval = (PlaceSearchPortData)criteria.uniqueResult();
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
	public PlaceSearchPortData getPlaceSearchPortData(Long id) {
		return this.read(id);
	}

}
