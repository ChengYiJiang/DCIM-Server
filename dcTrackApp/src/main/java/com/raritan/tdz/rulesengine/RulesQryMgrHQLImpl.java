/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Transient;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;

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
public class RulesQryMgrHQLImpl implements RulesQryMgr {
	
	private final String altDataKey = "main";

	Map<String, RulesHQL> rulesCriteriaProjectionsMap = new HashMap<String, RulesQryMgrHQLImpl.RulesHQL>();
	
	private RemoteRef remoteReference;
	
	
	public RulesQryMgrHQLImpl(RemoteRef remoteReference){
		this.remoteReference = remoteReference;
	}

	
	public RemoteRef getRemoteReference() {
		return remoteReference;
	}

	public void setRemoteReference(RemoteRef remoteReference) {
		this.remoteReference = remoteReference;
	}
	
	
	@Override
	public void createCriteria(String remoteRef, String xPath, String altDataKey) {
		String key = getKey(remoteReference.getRemoteType(remoteRef), altDataKey);
		
		RulesHQL criteriaProjections =
				rulesCriteriaProjectionsMap.get(key.toString());
		if (criteriaProjections == null){
			
			criteriaProjections = 
					new RulesHQL(remoteReference.getRemoteType(remoteRef));
			
			rulesCriteriaProjectionsMap.put(key.toString(), criteriaProjections);
		}
		
	}




	@Override
	public void addAlias(String remoteRef, String xPath, String altDataKey)
			throws HibernateException, ClassNotFoundException {
		
		String remoteType = remoteReference.getRemoteType(remoteRef);
		String remoteAliasForId = remoteReference.getRemoteAlias(remoteRef, RemoteRef.RemoteRefConstantProperty.FOR_ID);
		String remoteAliasForValue = remoteReference.getRemoteAlias(remoteRef, RemoteRef.RemoteRefConstantProperty.FOR_VALUE);
		
		RulesHQL criteriaProjections = 
				rulesCriteriaProjectionsMap.get(getKey(remoteReference.getRemoteType(remoteRef),altDataKey));

		if (criteriaProjections != null)
		{
			if (remoteAliasForId != null){
				for (String alias: getAliases(remoteType, remoteAliasForId)){
					if (!criteriaProjections.getOuterJoinClause().contains(alias.replace(".", "_"))){
					criteriaProjections.createAlias(alias, alias.replace(".", "_"));
					}
				}
			}
			
			if (remoteAliasForValue != null){
				for (String alias: getAliases(remoteType, remoteAliasForValue)){
					if (!criteriaProjections.getOuterJoinClause().contains(alias.replace(".", "_"))){
					criteriaProjections.createAlias(alias, alias.replace(".", "_"));
					}
				}
			}

		}
	}

	@Override
	public void addProjection(String remoteRef, String xPath, String altDataKey)
			throws HibernateException, ClassNotFoundException {
		
		String remoteType = remoteReference.getRemoteType(remoteRef);
		String remoteAliasForId = remoteReference.getRemoteAlias(remoteRef, RemoteRef.RemoteRefConstantProperty.FOR_ID);
		String remoteAliasForValue = remoteReference.getRemoteAlias(remoteRef, RemoteRef.RemoteRefConstantProperty.FOR_VALUE);

		addAlias(remoteRef, xPath, "main");

		RulesHQL criteriaProjections = 
				rulesCriteriaProjectionsMap.get(getKey(remoteReference.getRemoteType(remoteRef),altDataKey));
		
		if (remoteAliasForId != null && !remoteAliasForId.isEmpty()){
			String traceStr = getFieldTrace(remoteType, remoteAliasForId);
			String aliasForId = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : remoteAliasForId;
			String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "this_.";
			criteriaProjections.addProjection(aliasForProjection.toString() + aliasForId, xPath + "/uiValueIdField/valueId");
		}
		if (remoteAliasForValue != null && !remoteAliasForValue.isEmpty()){
			String traceStr = getFieldTrace(remoteType, remoteAliasForValue);
			if (traceStr != null){
				String aliasForValue = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : remoteAliasForValue;
				String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "this_.";
				criteriaProjections.addProjection(aliasForProjection.toString() + aliasForValue, xPath + "/uiValueIdField/value");
			}
		}
	}

	@Override
	public void addFilter(String remoteRef, String filterField, Object filterValue, String operator) 
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
//		String remoteType = remoteReference.getRemoteType(remoteRef);
//		
//		String traceStr = getFieldTrace(remoteType, filterField);
//		String aliasForFilter = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : filterField;
//		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "";
//		
//		RulesHQL criteriaProjections =
//				rulesCriteriaProjectionsMap.get(remoteType);
//		
//		criteriaProjections.addRestriction( aliasForFilter, operator, filterValue, aliasForProjection );
		
		for (Map.Entry<String, RulesHQL> entry : rulesCriteriaProjectionsMap.entrySet()){
			String key = entry.getKey();
			if (key != null){
				String remoteType = key.substring(0,key.lastIndexOf(":"));
				String traceStr = getFieldTrace(remoteType, filterField);
				String aliasForFilter = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : filterField;
				String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "";
				
				RulesHQL criteriaProjections = entry.getValue();
				criteriaProjections.addRestriction(aliasForFilter, operator, filterValue, aliasForProjection);
			}
		}
		
	}

	
	
	@Override
	public List getData(Session session) {
		List list = new ArrayList();
		
		for (Map.Entry<String, RulesHQL> entry : rulesCriteriaProjectionsMap.entrySet())
		{
			String key = entry.getKey();
			if (key != null){
				RulesHQL criteriaProjections = entry.getValue();
				
				if (!criteriaProjections.isDontExecute()){
					String selectClause = criteriaProjections.getSelectClause();
					String fromClause = criteriaProjections.getFromClause();
					String outerJoinClause = criteriaProjections.getOuterJoinClause();
					String whereClause = criteriaProjections.getWhereClause();
					
					StringBuffer queryString = new StringBuffer();
					queryString.append(selectClause);
					queryString.append(" ");
					queryString.append(fromClause);
					queryString.append(" ");
					queryString.append(outerJoinClause);
					queryString.append(" ");
					queryString.append(whereClause);
					
					
					try {
						Query query = session.createQuery(queryString.toString());
						query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
						list.addAll(query.list());
					} catch (Throwable t){
						t.printStackTrace();
					}
				}
			
			}
		}
		return list;
	}

	@Override
	public void clearAll() {
		for (Map.Entry<String, RulesHQL> entry : rulesCriteriaProjectionsMap.entrySet()){
			String key = entry.getKey();
			if (key != null){
				RulesHQL criteriaProjections = entry.getValue();
				criteriaProjections.setDontExecute(false);
			}
		}
	}
	

	@Override
	public void addFilter(Map<String, Filter> filterMap) {
		for (Map.Entry<String, RulesHQL> entry : rulesCriteriaProjectionsMap.entrySet()){
			String key = entry.getKey();
			if (key != null){
				RulesHQL criteriaProjections = entry.getValue();
				criteriaProjections.addRestriction(getFilter(filterMap,key));
			}
		}
	}
	
	private Filter getFilter(Map<String,Filter> filterMap, String key){
		String altDataKey = key.substring(key.lastIndexOf(":") + 1);
		return filterMap.get(key) != null ? filterMap.get(key):filterMap.get(altDataKey);
	}
	
	private ArrayList<String> getAliases(String remoteType, String fieldName) throws ClassNotFoundException{
		ArrayList<String> aliases = new ArrayList<String>();
		String traceStr = getFieldTrace(remoteType, fieldName);
		if (traceStr != null){
			String aliasStr = traceStr.contains(".") ? traceStr.substring(0,  traceStr.lastIndexOf(".")) : null;
			if (aliasStr != null){
				StringBuffer buffer = new StringBuffer();
				for (String token: aliasStr.split("\\.")){
					buffer.append(token);
					aliases.add(buffer.toString());
					buffer.append(".");
				}
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
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		List<Field> fields = objectTrace.traceObject(Class.forName(remoteType), fieldName);
		//Skip Transient fields
		if (fields != null && fields.size() > 0){
			Field lastField = fields.get(fields.size() - 1);
			if (lastField != null && lastField.getAnnotation(Transient.class) != null)
				return null;
		}
		
		String trace = objectTrace.toString();
		return trace;
	}
	
	
	private String getKey(String remoteRef, String altDataKey) {
		StringBuffer key = new StringBuffer();
		key.append(remoteRef);
		key.append(":");
		key.append(altDataKey);
		return key.toString();
	}

	
	private class RulesHQL{
		private StringBuffer fromClause = new StringBuffer();
		private StringBuffer selectClause;
		private StringBuffer outerJoinClause = new StringBuffer();
		private StringBuffer restrictionClause;
		private List<StringBuffer> projectionList = new ArrayList<StringBuffer>();
		private boolean dontExecute = false;
		
		public RulesHQL(String entityName){
			super();
			fromClause.append("from ");
			fromClause.append(entityName);
			fromClause.append(" as this_ ");
		}
		
		public String getFromClause() {
			return fromClause.toString();
		}
	
		public String getSelectClause() {
			selectClause = new StringBuffer("select ");
			for (StringBuffer projection : projectionList){
				selectClause.append(projection);
				selectClause.append(",");
			}
			selectClause.deleteCharAt(selectClause.lastIndexOf(","));
			return selectClause.toString();
		}
		
		public String getOuterJoinClause() {
			return outerJoinClause.toString();
		}
		
		public synchronized String getWhereClause(){
			return restrictionClause.toString();
		}
		
		public synchronized void addProjection(String property, String alias){
			StringBuffer projection = new StringBuffer();
			projection.append(" ");
			projection.append(property);
			projection.append(" as ");
			
			//Alias in a HQL does not take special characters that we use in the XPATH
			//Therefore, this code just replace them with some known characters.
			String selectAlias = alias.replace("/", "_SL_");
			selectAlias = selectAlias.replace("[", "_LB_");
			selectAlias = selectAlias.replace("]", "_RB_");
			selectAlias = selectAlias.replace("@", "_A_");
			selectAlias = selectAlias.replace("'", "_Q_");
			selectAlias = selectAlias.replace("=", "_EQ_");
			projection.append(selectAlias);
			
			projectionList.add(projection);
		}
		
		public void createAlias(String associationPath, String alias){
			outerJoinClause.append(" left join ");
			outerJoinClause.append("this_.");
			outerJoinClause.append(associationPath);
			outerJoinClause.append(" ");
			outerJoinClause.append(alias);
		}
		
		public synchronized void addRestriction(String filterKey,
			String operator, Object filterValueObject, String alias){
			restrictionClause = new StringBuffer();
			restrictionClause.append(" where ");
			if (alias.isEmpty())
				restrictionClause.append("this_.");
			else
				restrictionClause.append(alias.replace(".", "_"));
			restrictionClause.append(filterKey);
			restrictionClause.append(" ");
			restrictionClause.append(operator);
			restrictionClause.append(" ");
			restrictionClause.append(filterValueObject.toString());
		}
		
		public synchronized void addRestriction(Filter filter){
			if (filter == null){
				setDontExecute(true);
				return;
			}
			restrictionClause = new StringBuffer();
			restrictionClause.append(" where ");
			restrictionClause.append(filter.toSqlString());
		}

		public synchronized boolean isDontExecute() {
			return dontExecute;
		}

		public synchronized void setDontExecute(boolean dontExecute) {
			this.dontExecute = dontExecute;
		}
	}


}
