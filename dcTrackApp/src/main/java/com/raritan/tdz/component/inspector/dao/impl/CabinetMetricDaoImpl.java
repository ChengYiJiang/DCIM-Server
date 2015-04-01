package com.raritan.tdz.component.inspector.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.component.inspector.dao.CabinetMetricDao;
import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.domain.HstCabinetUsage;
import com.raritan.tdz.domain.HstPortsData;
import com.raritan.tdz.domain.HstPortsPower;
import com.raritan.tdz.dto.PortInterface;

/**
 * Implementation of CabinetMetricDao.
 */
@Transactional
public class CabinetMetricDaoImpl implements CabinetMetricDao {
    private static Logger log = Logger.getLogger(CabinetMetricDaoImpl.class);

    private SessionFactory sessionFactory;
  
    /**
     * @param sessionFactory
     */
    public CabinetMetricDaoImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
	public double getCabinetBudgetedPower(Long cabinetId) {
        Session session = sessionFactory.getCurrentSession();
        
		Query query = session.getNamedQuery("getSumPSWattsBudgetForCabinetId");
		query.setLong("cabinetId",  cabinetId );

        Object result = query.uniqueResult();
        return (result == null) ? 0d : (double) result;
	}

    @Override
    public List<?> getCabinetListBudgetedPower(String locationCode) {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.getNamedQuery("getSumPSWattsBudgetByCabinetForLocation");
        query.setString("locationCode", locationCode);

        return query.list();
    }

    @Override
	public double getCabinetHeatOutput(Long cabinetId) {
		double heatFactor = 1000.0; //default
		
		double watts = getCabinetBudgetedPower(cabinetId);
		
		return (watts / heatFactor); // unit = kW
	}
	
    @Override
    public double getCabinetTotalWeight(Long cabinetId) {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.getNamedQuery("getCabinetTotalWeight");
        query.setLong("cabinetId", cabinetId);

        Object result = query.uniqueResult();
        return (result == null) ? 0d : (double) result;
    }

    @Override
	public long getCabinetItemCount(Long cabinetId) {
        Session session = sessionFactory.getCurrentSession();

        Query query = session.getNamedQuery("getCabinetItemCount");
        query.setLong("cabinetId", cabinetId);

        Long result = (Long)query.uniqueResult();

        return result;
	}

    @Override
	public int getCabinetTotalAvailableRUs(Long cabinetId) {
		HstCabinetUsage cabinet = getHstCabinetUsage(cabinetId);
		
		return (cabinet == null ? 0 : cabinet.getFreeRu());		
	}

    @Override
	public int getCabinetTotalRUs(Long cabinetId) {
		HstCabinetUsage cabinet = getHstCabinetUsage(cabinetId);
		
		return (cabinet == null ? 0 : cabinet.getTotalRu());		
	}

    @Override
	public int getCabinetTotalUsedRUs(Long cabinetId) {
		HstCabinetUsage cabinet = getHstCabinetUsage(cabinetId);
		
		if(cabinet == null) return 0;
		
		return cabinet.getTotalRu() - cabinet.getFreeRu();		
	}
	
    @Override
	public int getCabinetLargestContigRU(Long cabinetId) {
		HstCabinetUsage cabinet = getHstCabinetUsage(cabinetId);
		
		return (cabinet == null ? 0 : cabinet.getLargestRu());		
	}

    @Override
    public CabinetMetricDto getCabinetRUInfo(Long cabinetId) {
        CabinetMetricDto dto = new CabinetMetricDto();
        HstCabinetUsage cabinet = getHstCabinetUsage(cabinetId);

        if (cabinet == null) {
            dto.setTotalRUs(0);
            dto.setAvailableRUs(0);
            dto.setLargestContiguousRUs(0);
        } else {
            dto.setTotalRUs(cabinet.getTotalRu());
            dto.setAvailableRUs(cabinet.getFreeRu());
            dto.setLargestContiguousRUs(cabinet.getLargestRu());
        }

        return dto;
    }

    @Override
	public HstCabinetUsage getHstCabinetUsage(Long cabinetId) {
		Session session = sessionFactory.getCurrentSession();
		
		Criteria criteria = session.createCriteria(HstCabinetUsage.class);
		criteria.createAlias("cabinetItem", "cabinet");
		criteria.add(Restrictions.eq("cabinet.itemId", cabinetId));
		criteria.add(Restrictions.eq("latest", true));
		criteria.setReadOnly(true);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		for(Object r:criteria.list()){
			return (HstCabinetUsage)r;
		}
		
		return null;		
		
	}
	
    @Override
	public List<HstPortsData> getHstPortData(Long cabinetId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(HstPortsData.class);
		criteria.createAlias("hstPortsDataDetail", "detail");
		criteria.add(Restrictions.eq("cabinetId", cabinetId));
		criteria.add(Restrictions.eq("latest", true));
		criteria.setReadOnly(true);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return criteria.list();
	}
	
    @Override
	public List<HstPortsPower> getHstPortPower(Long cabinetId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(HstPortsPower.class);
		criteria.createAlias("hstPortsPowerDetail", "detail");
		criteria.add(Restrictions.eq("cabinetId", cabinetId));
		criteria.add(Restrictions.eq("latest", true));
		criteria.setReadOnly(true);
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		return criteria.list();
	}

    @Override
	public HashMap<Long, Float> getItemsEffectivePower(List<Long> itemIds) {
        Session session = sessionFactory.getCurrentSession();
        HashMap<Long, Float> itemMap = new HashMap<Long, Float>();
        
		Query query = session.getNamedQuery("getSumPSWattsBudgetForItemList");
		query.setParameterList("itemIds",  itemIds );
		
        @SuppressWarnings("unchecked")
        List<Object[]> results = (List<Object[]>) query.list();

		for ( Object[] result : results ) {
			Long itemId = (Long) result[0];
			Float budgetedPower = ((Double) result[1]).floatValue();
			
			itemMap.put(itemId, budgetedPower);
		}
        
        return itemMap;
	}
	
}