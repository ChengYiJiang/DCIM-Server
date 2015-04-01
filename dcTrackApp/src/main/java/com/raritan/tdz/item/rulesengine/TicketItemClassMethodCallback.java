package com.raritan.tdz.item.rulesengine;

import java.util.Map;

import org.hibernate.Query;


import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;

import org.hibernate.transform.Transformers;

import com.raritan.dctrack.xsd.AltUiValueIdFieldMap;
import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.dctrack.xsd.UiValueIdField;

import com.raritan.tdz.rulesengine.Filter;
import com.raritan.tdz.rulesengine.RemoteRef;

import com.raritan.tdz.rulesengine.RemoteRefMethodCallbackUsingFilter;

/**
 * This class provides the class/subclass from the original ticket to the item details page.
 * @author prasanna
 *
 */
public class TicketItemClassMethodCallback implements RemoteRefMethodCallbackUsingFilter {

	private SessionFactory sessionFactory;

	private static final String itemClass = "classLkp";
	private static final String itemSubClass = "subclassLkp";
	private static final String altUiValueIdFieldId = "ticket";

	ProjectionList proList;

	public TicketItemClassMethodCallback(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void fillValue(UiComponent uiViewComponent, Map<String, Filter> filterMap,
				RemoteRef remoteRef, Object additionalArgs) throws Throwable {
		
		//Get the UiValueIdField for the "ticket" alt data.
		UiValueIdField uiValueIdField = null;
		for (AltUiValueIdFieldMap field:uiViewComponent.getAltUiValueIdFieldMap()){
			if (field.getId().equals(altUiValueIdFieldId)){
				uiValueIdField = field.getUiValueIdField();
			}
		}

		String remoteEntityName = remoteRef.getRemoteType(uiValueIdField.getRemoteRef());
		StringBuilder filterKey = new StringBuilder(remoteEntityName).append(":").append(altUiValueIdFieldId);
		
		Session session = sessionFactory.getCurrentSession();
		
		StringBuffer queryString = new StringBuffer();
		queryString.append("select ");
		queryString.append("itemClass as ");
		queryString.append(itemClass);
		queryString.append(" , ");
		queryString.append("subClass as ");
		queryString.append(itemSubClass);
		queryString.append(" from ");
		queryString.append(remoteEntityName);
		queryString.append(" this_");
		queryString.append(" where ");
		Filter filter = filterMap != null && filterKey != null ? filterMap.get(filterKey.toString()):null;
		if (filter != null){
			queryString.append(filterMap.get(filterKey.toString()).toSqlString());
			
			Query query = session.createQuery(queryString.toString());
				
			query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			
			@SuppressWarnings("unchecked")
			Map<String,Object> modelDetailsMap = (Map<String, Object>) query.uniqueResult();
			
			
			String value = null;
			
	
			if (modelDetailsMap != null){
				if (modelDetailsMap.get(itemSubClass) != null && modelDetailsMap.get(itemClass) != null){
					String classOfItem = (String)modelDetailsMap.get(itemClass);
					String subclassOfItem = (String)modelDetailsMap.get(itemSubClass);
					if (subclassOfItem != null && !subclassOfItem.isEmpty()){
						value = classOfItem + " / " + subclassOfItem;
					} else {
						value = classOfItem;
					}
					
				} else if (modelDetailsMap.get(itemClass) != null){
					StringBuffer valueBuffer = new StringBuffer();
					valueBuffer.append(modelDetailsMap.get(itemClass).toString());
					//If the item has a subclass however there is no model, we need to still add the class / subclass string. 
					if (modelDetailsMap.get(itemSubClass) != null && !((String)modelDetailsMap.get(itemSubClass)).isEmpty()){
						valueBuffer.append(" / ");
						valueBuffer.append(modelDetailsMap.get(itemSubClass));
					}
					value = valueBuffer.toString();
				}
			}
			
			if (uiValueIdField != null){
				uiValueIdField.setValue(value);
			}
		}

	}
}
