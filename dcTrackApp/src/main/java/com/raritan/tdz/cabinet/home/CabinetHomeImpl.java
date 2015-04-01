package com.raritan.tdz.cabinet.home;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.move.home.MoveHome;

/**
 * Cabinet home implementation.
 * 
 * @author Andrew Cohen
 */
public class CabinetHomeImpl implements CabinetHome {

	private SessionFactory sessionFactory;
	
	@Autowired
	private MoveHome moveHome;
	
	public CabinetHomeImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<String> getCabinetRowLabels(Long locationId) 
	{
		Session session = sessionFactory.getCurrentSession();                
        String HQL_QUERY = "select distinct c.rowLabel from CabinetItem as c inner join c.dataCenterLocation as loc ";
        HQL_QUERY += " where loc.dataCenterLocationId = " + locationId + " and c.rowLabel is not null order by c.rowLabel";
        
		return session.createQuery(HQL_QUERY).list();
	}

	@Transactional(readOnly = true)
	@Override
	public List<Integer> getCabinetPositionInRows(Long locationId, String rowLabel)
	{
		 Session session = sessionFactory.getCurrentSession();                
         StringBuffer buf = new StringBuffer("select distinct c.positionInRow from CabinetItem as c inner join c.dataCenterLocation as loc ");
         buf.append(" where loc.dataCenterLocationId = ").append( locationId );
         
         if (rowLabel != null) {
         	buf.append(" and c.rowLabel = '" ).append( rowLabel.trim() ).append("'");
         }
         
         buf.append(" order by c.positionInRow");
         @SuppressWarnings("unchecked")
			List<Integer> recList = session.createQuery( buf.toString() ).list();
	
         return recList;  
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<ValueIdDTO> getAllCabinets(Long locationId)
	{
		Session session = sessionFactory.getCurrentSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(CabinetItem.class);
		
		proList.add(Projections.property("itemId"), "data");
		proList.add(Projections.property("itemName"), "label");
		criteria.createAlias("classLookup","classLookup");
		criteria.createAlias("dataCenterLocation","dataCenterLocation");
		criteria.createAlias("model", "model");
		//TODO: For commented code below, we probably need to do an outer join (Criteria.LEFT_JOIN) 
		//      if we need to filter against subclass, if not cabinets without subclass are not shown!
		criteria.createAlias("statusLookup","statusLookup", Criteria.LEFT_JOIN);
		
		criteria.addOrder(Order.asc("itemName"));
		
		criteria.add(Restrictions.eq("classLookup.lkpValueCode", SystemLookup.Class.CABINET));
		criteria.add(Restrictions.eq("dataCenterLocation.dataCenterLocationId", locationId));
		criteria.add(Restrictions.isNotNull("model") ); // Do not return cabinets without a model
		criteria.add(Restrictions.and(Restrictions.isNotNull("statusLookup"),     
                Restrictions.ne("statusLookup.lkpValueCode", SystemLookup.ItemStatus.ARCHIVED)
                ));
		
		moveHome.filterMoveCabinets(criteria);
				
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}
}
