/**
 * 
 */
package com.raritan.tdz.item.rulesengine;


import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author prasanna
 *
 */
public class ParentOfItemMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;

	private static final String itemMounting = "mounting";
	private static final String itemFormfactor = "formFactor";
	
	ProjectionList proList;

	public ParentOfItemMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiData, java.util.Map, com.raritan.tdz.rulesengine.RemoteRefAttributes)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable {

		String remoteEntityName = remoteRef.getRemoteType(uiViewComponent.getUiValueIdField().getRemoteRef());
		
		
		Session session = sessionFactory.getCurrentSession();
		
		Criteria criteria = session.createCriteria(remoteEntityName);
		criteria.add(Restrictions.eq(filterField, filterValue));
		criteria.createAlias("parentItem", "parent", Criteria.LEFT_JOIN);
		
	
		proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("parent.itemName"), "parentItemName"));
		proList.add(Projections.alias(Projections.property("parent.itemId"), "parentItemId"));
	
		
		criteria.setProjection(proList);
		
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		// criteria.add(Restrictions.eq(filterField, filterValue));
		
		Map<String,Object> parentItemMap = (Map<String, Object>) criteria.uniqueResult();
		
		if (parentItemMap != null && parentItemMap.get("parentItemId") != null){
			uiViewComponent.getUiValueIdField().setValueId((Long)parentItemMap.get("parentItemId"));
			uiViewComponent.getUiValueIdField().setValue(parentItemMap.get("parentItemName").toString());
		}
		
	}
}
