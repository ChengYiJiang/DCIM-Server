/**
 * 
 */
package com.raritan.tdz.item.home;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.UniqueValidatorBase;
import com.raritan.tdz.util.UniqueValidatorParentRestriction;

/**
 * @author prasanna
 *
 */
public class ItemNameUniqueValidator extends UniqueValidatorBase {
	@Autowired
	private ItemDAO itemDAO;
	
	Map<Long,UniqueValidatorParentRestriction> parentRestrictionMap = new HashMap<Long, UniqueValidatorParentRestriction>();
	
	public ItemNameUniqueValidator(SessionFactory sessionFactory, Map<Long,UniqueValidatorParentRestriction> parentRestrictionMap) {
		super(sessionFactory);
		this.parentRestrictionMap = parentRestrictionMap;
	}

	@Override
	protected void addSiteRestriction(Criteria criteria, String siteCode, Long parentId) {
		if ((siteCode == null || siteCode.isEmpty()) && (parentId != null && parentId > 0)){
			Item parentItem = itemDAO.getItem(parentId);
			siteCode =  parentItem != null && parentItem.getDataCenterLocation() != null ? parentItem.getDataCenterLocation().getCode() : getDefaultSiteCode();
		} else if (siteCode == null || siteCode.isEmpty()){
			siteCode = getDefaultSiteCode();
		}
		
		criteria.createAlias("dataCenterLocation","dataCenterLocation",Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("dataCenterLocation.code",siteCode));
	}

	private String getDefaultSiteCode() {
		String siteCode;
		Criteria locationCriteria = 
				sessionFactory.getCurrentSession().createCriteria("com.raritan.tdz.domain.DataCenterLocationDetails");
		locationCriteria.setProjection(Projections.property("code"));
		locationCriteria.add(Restrictions.eq("defaultSite", new Boolean(true)));
		
		siteCode = (String) locationCriteria.uniqueResult();
		return siteCode;
	}
	
	@Override
	protected void addAdditionalRestrictions(Criteria criteria){
		
		criteria.createAlias("classLookup","classLookup",Criteria.LEFT_JOIN);
		
		removePassiveRestriction(criteria);
		
		criteria.createAlias("statusLookup", "statusLookup");
		criteria.add(Restrictions.ne("statusLookup.lkpValueCode",SystemLookup.ItemStatus.ARCHIVED));
		criteria.add(Restrictions.ne("statusLookup.lkpValueCode",SystemLookup.ItemStatus.HIDDEN));
	}
	
	@Override
	protected void addParentRestriction(Criteria criteria, Long parentId){
		if (parentId != null && parentId > 0){
			
			//Find unique value for the parent 
			Item parentItem = itemDAO.getItem(parentId);
			Long uniqueValue = parentItem != null ? parentItem.getClassMountingFormFactorValue():null;
			
			
			//Excepting the floor pdu panel (parent being floor pdu), for everything else we do not want to consider power panel 
			//during the name check
			//TODO: This is a bit of an ugly hack. Need to see if there is a better solution.
			if (!uniqueValue.equals(SystemLookup.ModelUniqueValue.FloorPDUFreeStanding)){
				removePowerPanelRestriction(criteria);
			}
			
			if (uniqueValue != null){
				UniqueValidatorParentRestriction restriction = parentRestrictionMap.get(uniqueValue);
				if (restriction != null){
					restriction.addParentRestriction(criteria, parentId);
				}
			}
		} else {
			removePowerPanelRestriction(criteria);
		}
	}
	
	private void removePowerPanelRestriction(Criteria criteria) {
		
		Criterion notFloorPduItem = Restrictions.ne("classLookup.lkpValueCode",SystemLookup.Class.FLOOR_PDU);

		criteria.createAlias("subclassLookup","subclassLookup",Criteria.LEFT_JOIN);
		Criterion subClassNull = Restrictions.isNull("subclassLookup");
		
		Criterion notPowerPanel = Restrictions.or(notFloorPduItem, subClassNull);
		
		criteria.add(notPowerPanel);
		
	}
	
	private void removePassiveRestriction(Criteria criteria) {
		
		Criterion notPassive = Restrictions.ne("classLookup.lkpValueCode",SystemLookup.Class.PASSIVE);

		criteria.add(notPassive);
		
	}
	
}
 
