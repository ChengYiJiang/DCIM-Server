package com.raritan.tdz.location.dao;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.DataCenterLocationDetails;

public class LocationDAOImpl extends DaoImpl<DataCenterLocationDetails> implements LocationDAO{

	@Override
	public DataCenterLocationDetails loadLocation(Long id){
		return loadLocation( id, false);
	}
	
	@Override
	public DataCenterLocationDetails loadLocation( Long id, boolean readOnly){
	
		Session session = null;
		DataCenterLocationDetails retval = null;
		
		try{
			if( id != null && id > 0 ){
				session = this.getNewSession();
				Criteria criteria = session.createCriteria(DataCenterLocationDetails.class);
				criteria.setFetchMode("parentLocation", FetchMode.JOIN);
				criteria.add(Restrictions.eq("dataCenterLocationId", id));
				criteria.setReadOnly(readOnly);
				retval = (DataCenterLocationDetails)criteria.uniqueResult();
			}
		}
		finally{
			if( session != null ){
				session.close();
			}
		}
		return retval;
	}
	
	private void projectCode(Criteria c) {
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("code"), "code");
		c.setProjection(proList);
		
	}

	@Override
	public String getLocationCode(Long locationId) {
		
		Session session = this.getSession();
		
		Criteria criteria = session.createCriteria(DataCenterLocationDetails.class);
		criteria.add(Restrictions.eq("dataCenterLocationId", locationId));
		
		projectCode(criteria);
		
		return (String) criteria.uniqueResult();
	}

	@Override
	public DataCenterLocationDetails getLocation(Long id){
		return this.read(id);
	}

	@Override
	public String getPiqHostByLocationId(long locationId) {
        Query query = this.getSession().getNamedQuery("getPiqHostByLocation");
        query.setLong("locationId", locationId);
		return (String) query.uniqueResult();
	}

	@Override
	public String getPiqSettingByLocationId(long locationId,long lkpValueCode) {
        Query query = this.getSession().getNamedQuery("getPiqSettingByLocation");
        query.setLong("locationId", locationId);
        query.setLong("lkpValueCode", lkpValueCode);
		return (String) query.uniqueResult();
	}
	
	@Override
	public Long createLocation(DataCenterLocationDetails location) {
		Session s = this.getSession();
		// HACK: since trigger deletes locale when location is deleted, we can use
		// cascadType.ALL for locale field, and it will not save automatically.
		// save locale before saving location.
		s.save(location.getDcLocaleDetails());
		return create(location);		
	}

	@Override
	public List<Long> getLocationIdByPIQHost(String piqHost) {
		Query query = this.getSession().getNamedQuery("getLocationByPIQHost");
	    query.setParameter("piqHost", piqHost);
		return query.list();
	}

	@Transactional
	@Override
	public Long getLocationIdByCode(String locationCode) {
		Query query = this.getSession().getNamedQuery("getLocationIdByCode");
		query.setParameter("locationCode", locationCode);
		return (Long) query.uniqueResult();
	}

	@Override
	public void deleteLocationAndItems(Long locationId, String userName) {
		
		Query query = this.getSession().getNamedQuery("dcDeleteLocationsAndAllReference");
		query.setParameter("locationId", locationId.intValue());
		query.setParameter("user", (null != userName) ? userName.toCharArray() : new String().toCharArray());
		query.uniqueResult();
		
	}
	
	
	@Override
	public void unmapLocationWithPIQ(Long locationId) {
		Session session = this.getSession();

		Query query = session.getNamedQuery("clearLocationPiqAssociation");
		query.setParameter("locationId", locationId.intValue());
		
		query.executeUpdate();
		
		// When location is unmapped, the external key is reset in the PIQ and therefore the dctrack
		// will start deleting the items, therefore reset the external key as well
		Query queryItemInLoc = session.getNamedQuery("clearItemInLocationPiqAssociation");
		queryItemInLoc.setParameter("locationId", locationId.intValue());
		
		queryItemInLoc.executeUpdate();

	}

	
	
}
