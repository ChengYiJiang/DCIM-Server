/**
 * 
 */
package com.raritan.tdz.item.rulesengine;


import java.util.ArrayList;
import java.util.List;
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
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
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
public class ItemSubClassMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;
	private ModelItemSubclassMap modelItemSubclassMap;
	
	private static final String itemClass = "classLkpValue";
	private static final String itemClassCode = "classLkpValueCode";
	private static final String itemMounting = "mounting";
	private static final String itemFormfactor = "formFactor";
	
	ProjectionList proList;

	public ItemSubClassMethodCallback(SessionFactory sessionFactory){
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
		
		criteria.setProjection(proList);
		
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		criteria.add(Restrictions.eq(filterField, filterValue));
		
		Map<String,Object> modelDetailsMap = (Map<String, Object>) criteria.uniqueResult();
		
		
		String value = "";
		Long valueId = new Long(0);
		

		if (modelDetailsMap != null){
			if (modelDetailsMap.get(itemClass) != null && modelDetailsMap.get(itemMounting) != null && modelDetailsMap.get(itemFormfactor) != null){
				Long valueCode = modelItemSubclassMap.getSubclassValueCode(modelDetailsMap.get(itemClass).toString(), modelDetailsMap.get(itemMounting).toString(), modelDetailsMap.get(itemFormfactor).toString());
				LksData subclassLks = getSubClass(valueCode);
				if (subclassLks != null){
					value = subclassLks.getLkpValue();
					valueId = subclassLks.getLkpValueCode();
				}
				else
					value = "";
			}
		}
			
		
		uiViewComponent.getUiValueIdField().setValue(value);
		uiViewComponent.getUiValueIdField().setValueId(valueId);
	}
	
	
	private LksData getSubClass(Long subclassLkpValueCode) {
		
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(LksData.class);
		//criteria.add(Restrictions.eq("lkpValueCode", subclassLks.getLkpValueCode()));
        criteria.add(Restrictions.eq("lkpValueCode", subclassLkpValueCode));
		LksData result = (LksData) criteria.uniqueResult();
		return result;
	}
	
	

	private void addItemClass(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemClass)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"), Criteria.LEFT_JOIN);
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemClass);
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": "";

		proList.add(Projections.property(aliasForProjection + "lkpValue"), itemClass);
	}
	
	private void addItemClassCode(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemClass)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"));
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemClassCode);
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": "";

		proList.add(Projections.property(aliasForProjection + "lkpValueCode"), itemClassCode);
	}
	
	private void addItemMounting(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, itemMounting)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"),Criteria.LEFT_JOIN);
			}
		}
		
		String traceStr = getFieldTrace(entityName, itemMounting);
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": "";
		
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
