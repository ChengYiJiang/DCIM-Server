package com.raritan.tdz.searchplace.dao;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.searchplace.domain.PlaceSearch;

public class PlaceSearchDAOImpl  extends DaoImpl<PlaceSearch> implements PlaceSearchDAO{

	@Override
	public PlaceSearch loadPlaceSearch(Long id) {
		return loadPlaceSearch( id, false);
	}

	@Override
	public PlaceSearch loadPlaceSearch(Long id, boolean readOnly) {

		Session session = null;
		PlaceSearch retval = null;
		
		try{
			if( id != null && id > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(PlaceSearch.class);
				criteria.setFetchMode("item", FetchMode.JOIN);
				criteria.setFetchMode("destCabinet", FetchMode.JOIN);
				criteria.setFetchMode("destGroup", FetchMode.JOIN);
				criteria.setFetchMode("destType", FetchMode.JOIN);
				criteria.setFetchMode("destFunction", FetchMode.JOIN);
				criteria.add(Restrictions.eq("placeSearchId", id));
				criteria.setReadOnly(readOnly);
				retval = (PlaceSearch)criteria.uniqueResult();
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
	public PlaceSearch getPlaceSearch(Long id) {
		return this.read(id);
	}

}
