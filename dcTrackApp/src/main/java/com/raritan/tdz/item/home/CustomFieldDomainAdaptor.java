/**
 * 
 */
package com.raritan.tdz.item.home;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import com.raritan.tdz.domain.CustomItemDetails;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ValueIDFieldToDomainAdaptor;

/**
 * @author prasanna
 *
 */
public class CustomFieldDomainAdaptor implements ValueIDFieldToDomainAdaptor{

	private SessionFactory sessionFactory;
	
	public CustomFieldDomainAdaptor(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Override
	public Object convert(Object dbObject, ValueIdDTO valueIdDTO)
			throws BusinessValidationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			ClassNotFoundException {
		
		convertCustomFieldsForAnItem(dbObject, valueIdDTO);
		return dbObject;
	}
	
	private void convertCustomFieldsForAnItem (Object itemObj, ValueIdDTO dto) throws ClassNotFoundException {
		Item item = (Item)itemObj;
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("unchecked")
		HashMap<String, String> customFields = ((HashMap<String, String>)dto.getData());
		Iterator<Entry<String, String>> iter = customFields.entrySet().iterator();
		while(iter.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry) iter.next();
			Long lkuid = Long.parseLong((String)entry.getKey());
			String value = entry.getValue().toString();
			LkuData customAttrNameLookup = (LkuData) session.get(LkuData.class, lkuid);
			LkuData customDataTypeLookup = (LkuData) session.get(LkuData.class, SystemLookup.LkuType.DATA_TYPE_STRING);
			CustomItemDetails cid = null;
			Long itemId = item.getItemId();
			boolean isNewCustomField = false;
			if (itemId > 0) { 
				Criteria criteria = session.createCriteria(CustomItemDetails.class);
				criteria.createAlias("item", "item");
				criteria.createAlias("customAttrNameLookup", "custom");
				criteria.add(Restrictions.eq("item.itemId", item.getItemId()));
				criteria.add(Restrictions.eq("custom.lkuId", lkuid));
				cid = (CustomItemDetails)criteria.uniqueResult();
			}
			
			if (cid == null) {
				cid = new CustomItemDetails();
				isNewCustomField = true;
			}
			
			cid.setCustomDataTypeLookup(customDataTypeLookup);
			cid.setCustomAttrNameLookup(customAttrNameLookup);
			cid.setAttrValue(value);
			cid.setItem(item);
			
			if (itemId > 0) {
				if (isNewCustomField) item.addCustomField( cid );
				session.saveOrUpdate( cid );
			} else {
				item.addCustomField(cid);
			}
		}
	}

}
