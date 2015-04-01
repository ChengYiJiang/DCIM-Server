/**
 * 
 */
package com.raritan.tdz.item.rulesengine;


import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;

/**
 * @author prasanna
 *
 */
public class ItemEAssetTagMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;
	
	ProjectionList proList;

	public ItemEAssetTagMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiData, java.util.Map, com.raritan.tdz.rulesengine.RemoteRefAttributes)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs) throws Throwable {
		
		Session session = sessionFactory.getCurrentSession();
		
		Criteria c = session.createCriteria(Item.class);
		c.setProjection(Projections.property("raritanAssetTag"));
		c.add(Restrictions.eq("itemId", (Long)filterValue));

		String raritanAssetTag = (String) c.uniqueResult();
		
		uiViewComponent.getUiValueIdField().setValue(raritanAssetTag);
		
		//Set the lock status based on the if the tag is verified or not.
		uiViewComponent.setLockStatus(isItemTagVerified((Long)filterValue));
	}
	
	private Boolean isItemTagVerified(Long itemId) {
		Session session = sessionFactory.getCurrentSession();
	
		Criteria c = session.createCriteria(Item.class); 
		c.add(Restrictions.eq("itemId", itemId));

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("isAssetTagVerified"), "isAssetTagVerified");
		c.setProjection(proList);
		
		// c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);

		Boolean eAssetTagVerified = (Boolean) c.uniqueResult();
		
		if (null == eAssetTagVerified) return true;
		
		return eAssetTagVerified;
	}
}
