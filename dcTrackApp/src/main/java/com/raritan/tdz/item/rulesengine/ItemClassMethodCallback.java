/**
 * 
 */
package com.raritan.tdz.item.rulesengine;


import java.util.ArrayList;
import java.util.Map;

import javax.persistence.criteria.JoinType;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * @author prasanna
 *
 */
public class ItemClassMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;
	private ModelItemSubclassMap modelItemSubclassMap;
	
	private static final String itemClass = "classLkpValue";
	private static final String itemSubClass = "subclassLkpValue";
	private static final String itemClassCode = "classLkpValueCode";
	private static final String itemMounting = "mounting";
	private static final String itemFormfactor = "formFactor";
	
	ProjectionList proList;

	public ItemClassMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}
	
	

	public ModelItemSubclassMap getModelItemSubclassMap() {
		return modelItemSubclassMap;
	}



	public void setModelItemSubclassMap(ModelItemSubclassMap modelItemSubclassMap) {
		this.modelItemSubclassMap = modelItemSubclassMap;
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
	
		proList = Projections.projectionList();
		
		addItemClass(remoteEntityName, criteria);
		addItemClassCode(remoteEntityName, criteria);
		addItemMounting(remoteEntityName, criteria);
		addItemFormfactor(remoteEntityName, criteria);
		addItemSubClass(remoteEntityName,criteria);
		
		criteria.setProjection(proList);
		
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		criteria.add(Restrictions.eq(filterField, filterValue));
		
		Map<String,Object> modelDetailsMap = (Map<String, Object>) criteria.uniqueResult();
		
		
		String value = "";
		Long valueId = new Long(0);
		

		if (modelDetailsMap != null){
			if (modelDetailsMap.get(itemSubClass) != null && modelDetailsMap.get(itemClass) != null){
				String classOfItem = (String)modelDetailsMap.get(itemClass);
				String subclassOfItem = (String)modelDetailsMap.get(itemSubClass);
				value = classOfItem + " / " + subclassOfItem;
				valueId = (Long)modelDetailsMap.get(itemClassCode);
			}else if (modelDetailsMap.get(itemClass) != null && modelDetailsMap.get(itemMounting) != null && modelDetailsMap.get(itemFormfactor) != null){
				value = modelItemSubclassMap.getSubclass(modelDetailsMap.get(itemClass).toString(), modelDetailsMap.get(itemMounting).toString(), modelDetailsMap.get(itemFormfactor).toString());
				valueId = (Long)modelDetailsMap.get(itemClassCode);
			}else if (modelDetailsMap.get(itemClass) != null){
				StringBuffer valueBuffer = new StringBuffer();
				valueBuffer.append(modelDetailsMap.get(itemClass).toString());
				//If the item has a subclass however there is no model, we need to still add the class / subclass string. 
				if (modelDetailsMap.get(itemSubClass) != null){
					valueBuffer.append(" / ");
					valueBuffer.append(modelDetailsMap.get(itemSubClass));
				}
				value = valueBuffer.toString();
				valueId = (Long)modelDetailsMap.get(itemClassCode);
			}
		}
			
		
		uiViewComponent.getUiValueIdField().setValue(value);
		uiViewComponent.getUiValueIdField().setValueId(valueId);
	}
	
	private void addItemClass(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemClass)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"), Criteria.LEFT_JOIN);
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemClass);
		String defaultAlias = entityName.contains("Item") ? "model." : "";
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;

		proList.add(Projections.property(aliasForProjection + "lkpValue"), itemClass);
	}
	
	
	private void addItemSubClass(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{
		
		//Let us do this only for Item class as no other class have itemSubClass information
		if (entityName.equals(Item.class.getName()))
		{
	
			for (String alias: getAliases(entityName, itemSubClass)){
				if (!criteria.toString().contains(alias.replace(".", "_"))){
					criteria.createAlias(alias, alias.replace(".", "_"), Criteria.LEFT_JOIN);
				}
			}
			
			String traceStr = getFieldTrace(entityName, itemSubClass);
			String defaultAlias = entityName.contains("Item") ? "model." : "";
			String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;
	
			proList.add(Projections.property(aliasForProjection + "lkpValue"), itemSubClass);
		}
	}

	private void addItemClassCode(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemClass)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"));
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemClassCode);
		String defaultAlias = entityName.contains("Item") ? "model." : "";
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;

		proList.add(Projections.property(aliasForProjection + "lkpValueCode"), itemClassCode);
	}
	
	private void addItemMounting(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemMounting)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"),Criteria.LEFT_JOIN);
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemMounting);
		String defaultAlias = entityName.contains("Item") ? "model." : "";
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;
		
		proList.add(Projections.property(aliasForProjection + itemMounting), itemMounting);
	}
	
	private void addItemFormfactor(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemFormfactor)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"),Criteria.LEFT_JOIN);
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemFormfactor);
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": "";
		proList.add(Projections.property(aliasForProjection + itemFormfactor), itemFormfactor);
	}
	
	private ArrayList<String> getAliases(String remoteType, String fieldName) throws ClassNotFoundException{
		ArrayList<String> aliases = new ArrayList<String>();
		String traceStr = getFieldTrace(remoteType, fieldName);
		String aliasStr = traceStr.contains(".") ? traceStr.substring(0,  traceStr.lastIndexOf(".")) : null;
		if (aliasStr != null){
			StringBuffer buffer = new StringBuffer();
			for (String token: aliasStr.split("\\.")){
				buffer.append(token);
				aliases.add(buffer.toString());
				buffer.append(".");
			}
		}
		return aliases;
	}
	
	private String getFieldTrace(String remoteType, String fieldName) throws ClassNotFoundException{
		//Create Alias
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser", new UserIdTracerHandler());
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString();
		return trace;
	}
}
