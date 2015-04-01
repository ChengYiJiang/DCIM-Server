/**
 * 
 */
package com.raritan.tdz.item.rulesengine;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author prasanna
 *
 */
public class BladeChassisNameIdCallback implements RemoteRefMethodCallback {

	private SessionFactory sessionFactory;
	
	public BladeChassisNameIdCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef)
	 */
	@Override
	public void fillValue(UiComponent uiViewCompoent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {
		Session session = sessionFactory.getCurrentSession();
		Criteria c= session.createCriteria(ItItem.class);
		c.createAlias("bladeChassis", "bladeChassis");
		c.setProjection( Projections.projectionList()
				.add( Projections.alias(Projections.property("bladeChassis.itemId"),"chassisId") )
				.add( Projections.alias(Projections.property("bladeChassis.itemName"),"itemName") )
		);
		
		c.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		c.add(Restrictions.eq("itemId", filterValue));
		Map<String,Object> map = (Map<String, Object>) c.uniqueResult();
		
		if (map != null && map.size() > 0){
			if (map.get("itemName") != null)
				uiViewCompoent.getUiValueIdField().setValue(map.get("itemName"));
			
			if (map.get("chassisId") != null)
				uiViewCompoent.getUiValueIdField().setValueId((Long)map.get("chassisId"));
		}

	}

}
