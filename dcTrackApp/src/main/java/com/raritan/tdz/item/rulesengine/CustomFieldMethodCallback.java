/**
 * 
 */

package com.raritan.tdz.item.rulesengine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
/**
 * @author Basker
 *
 */
public class CustomFieldMethodCallback implements RemoteRefMethodCallback {

	private SessionFactory sessionFactory;
	
	public CustomFieldMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRefMethodCallback#fillValue(com.raritan.dctrack.xsd.UiComponent, java.lang.String, java.lang.Object, java.lang.String, com.raritan.tdz.rulesengine.RemoteRef)
	 */
	@Override
	public void fillValue(UiComponent uiViewComponent, String filterField,
			Object filterValue, String operator, RemoteRef remoteRef, Object additionalArgs)
			throws Throwable {
		//First get the custom fields for the given item
		Session session = sessionFactory.getCurrentSession();
		Criteria c= session.createCriteria(CustomItemDetails.class);
		c.createAlias("item", "item");
		c.add(Restrictions.eq("item.itemId", filterValue));
		
		//Populate the uiViewComponent fields as a map.
		@SuppressWarnings("unchecked")
		List<CustomItemDetails> resultList = c.list();
		
		//After hashmap
		Map<String, Map<String, Object>> hashMap = new HashMap<String, Map<String, Object>>();
		for (CustomItemDetails cid: resultList) {
			final Map<String, Object> fieldInfo = getCustomFieldInfo(cid, session);
			hashMap.put(cid.getCustomAttrNameLookup().getLkuValue(), fieldInfo);
		}
		uiViewComponent.getUiValueIdField().setValue(hashMap);
	}

	private Map<String, Object> getCustomFieldInfo(CustomItemDetails cid, Session session) {
		final Long customLkuId = cid.getCustomAttrNameLookup().getLkuId();
		Map<String, Object> fieldInfo = new HashMap<String, Object>( 3 );
		
		fieldInfo.put("value", cid.getAttrValue());
		fieldInfo.put("lkuId", customLkuId.toString());
		
		Criteria f = session.createCriteria( FieldDetails.class );
		f.createAlias("field", "field");
		f.createAlias("field.customLku", "customLku");
		f.setProjection( Projections.projectionList()
				.add( Projections.property("fieldDetailId") )
				.add( Projections.property("field.sortOrder") )
				.add( Projections.property("isRequiedAtSave"))
		);
		f.add(Restrictions.eq("customLku.lkuId", customLkuId));
		f.add(Restrictions.eq("classLks.lksId", cid.getItem().getClassLookup().getLksId()));
		
		
		Object[] result = (Object[])f.uniqueResult();
		
		if (result != null) {
			fieldInfo.put("fieldDetailId", ((Long)result[0]).toString());
			fieldInfo.put("sortOrder", (Integer)result[1]);
			fieldInfo.put("required", (Boolean)result[2]);
		}
		
		return fieldInfo;
	}
}
