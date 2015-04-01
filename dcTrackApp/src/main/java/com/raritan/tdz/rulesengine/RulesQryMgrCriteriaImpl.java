/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;

/**
 * @author prasanna
 *
 */
public class RulesQryMgrCriteriaImpl implements RulesQryMgr {

	Map<String, RulesCriteriaProjections> rulesCriteriaProjectionsMap = new HashMap<String, RulesQryMgrCriteriaImpl.RulesCriteriaProjections>();
	
	private RemoteRef remoteReference;
	
	DetachedCriteria criteria;
	ProjectionList projectionList;
	
	private static final Map<String,String> operatorToRestrictionMap = 
			Collections.unmodifiableMap(new HashMap<String,String>() {{
				put("<","lt");
				put(">=","ge");
				put("=","eq");
				put(">","gt");
			}});
	
	
	public RulesQryMgrCriteriaImpl(RemoteRef remoteReference){
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
		RulesCriteriaProjections criteriaProjections =
				rulesCriteriaProjectionsMap.get(remoteReference.getRemoteType(remoteRef));
		if (criteriaProjections == null){
			criteriaProjections = 
					new RulesCriteriaProjections(DetachedCriteria.forEntityName(remoteReference.getRemoteType(remoteRef)), 
								Projections.projectionList());
			rulesCriteriaProjectionsMap.put(remoteReference.getRemoteType(remoteRef), criteriaProjections);
		}
		
	}

	@Override
	public void addAlias(String remoteRef, String xPath, String altDataKey)
			throws HibernateException, ClassNotFoundException {
		
		String remoteType = remoteReference.getRemoteType(remoteRef);
		String remoteAliasForId = remoteReference.getRemoteAlias(remoteRef, RemoteRef.RemoteRefConstantProperty.FOR_ID);
		String remoteAliasForValue = remoteReference.getRemoteAlias(remoteRef, RemoteRef.RemoteRefConstantProperty.FOR_VALUE);
		
		RulesCriteriaProjections criteriaProjections = 
				rulesCriteriaProjectionsMap.get(remoteReference.getRemoteType(remoteRef));

		if (criteriaProjections != null)
		{
			DetachedCriteria criteria = criteriaProjections.getCriteria();
			if (criteria != null){
				
				if (remoteAliasForId != null){
					for (String alias: getAliases(remoteType, remoteAliasForId)){
							if (!criteria.toString().contains(alias.replace(".", "_"))){
								criteria.createAlias(alias, alias.replace(".", "_"),Criteria.LEFT_JOIN);
							}
						}
					}
				}
			
				if (remoteAliasForValue != null){
					for (String alias: getAliases(remoteType, remoteAliasForValue)){
							if (!criteria.toString().contains(alias.replace(".", "_"))){
								criteria.createAlias(alias, alias.replace(".", "_"),Criteria.LEFT_JOIN);
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

		
		//Include the Projection
		ProjectionList proList = Projections.projectionList();
		

		
		if (remoteAliasForId != null && !remoteAliasForId.isEmpty()){
			String traceStr = getFieldTrace(remoteType, remoteAliasForId);
			String aliasForId = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : remoteAliasForId;
			String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "";
			
			proList.add(Projections.alias(Projections.property(aliasForProjection.toString() + aliasForId), xPath + "/uiValueIdField/valueId"));
		}
		if (remoteAliasForValue != null && !remoteAliasForValue.isEmpty()){
			String traceStr = getFieldTrace(remoteType, remoteAliasForValue);
			String aliasForValue = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : remoteAliasForValue;
			String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "";
			
			proList.add(Projections.alias(Projections.property(aliasForProjection.toString() + aliasForValue), xPath + "/uiValueIdField/value"));
		}

		RulesCriteriaProjections criteriaProjections = rulesCriteriaProjectionsMap.get(remoteType);
		
		if (criteriaProjections != null){
			ProjectionList projectionList = criteriaProjections.getProjectionList();
			
			if (projectionList != null)
				projectionList.add(proList);
		}
		
	}

	@Override
	public void addFilter(String remoteRef, String filterField, Object filterValue, String operator) 
			throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String remoteType = remoteReference.getRemoteType(remoteRef);
		
		String traceStr = getFieldTrace(remoteType, filterField);
		String aliasForFilter = traceStr.contains(".") ? traceStr.substring(traceStr.lastIndexOf(".")) : filterField;
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") : "";
		
		RulesCriteriaProjections criteriaProjections =
				rulesCriteriaProjectionsMap.get(remoteType);
		
		Criterion restriction = getOperatorRestriction( aliasForFilter, operator, filterValue, aliasForProjection );
		
		criteriaProjections.getCriteria().add(restriction);
	
	}

	
	
	@Override
	public List getData(Session session) {
		List list = new ArrayList();
		
		for (Map.Entry<String, RulesCriteriaProjections> entry : rulesCriteriaProjectionsMap.entrySet())
		{
			String key = entry.getKey();
			RulesCriteriaProjections criteriaProjections = entry.getValue();
			
			DetachedCriteria criteria = criteriaProjections.getCriteria();
			ProjectionList projectionList = criteriaProjections.getProjectionList();
			
			criteria.setProjection(projectionList);
			criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			Criteria executableCriteria = criteria.getExecutableCriteria(session);
			
			list.addAll(executableCriteria.list());
		}
		return list;
	}

	@Override
	public void clearAll() {
		rulesCriteriaProjectionsMap.clear();
	}
	
	@Override
	public void addFilter(Map<String, Filter> filterMap) {
		// TODO Auto-generated method stub
		
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
		objectTrace.traceObject(Class.forName(remoteType), fieldName);
		String trace = objectTrace.toString();
		return trace;
	}
	
	private Criterion getOperatorRestriction(String filterKey,
			String operator, Object filterValueObject, String alias)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		String restrictionOp = operatorToRestrictionMap.get(operator);
		Method restrictionOpMethod = Restrictions.class.getMethod(restrictionOp, String.class, Object.class);
		
		if (alias != null)
			return((Criterion) restrictionOpMethod.invoke(null, alias.replace(".", "_") +  filterKey, filterValueObject));
		else
			return((Criterion) restrictionOpMethod.invoke(null, filterKey, filterValueObject));
	}

	
	private class RulesCriteriaProjections{
		private DetachedCriteria criteria;
		private ProjectionList projectionList;
		
		/**
		 * @param criteria
		 * @param projectionList
		 */
		public RulesCriteriaProjections(DetachedCriteria criteria,
				ProjectionList projectionList) {
			super();
			this.criteria = criteria;
			this.projectionList = projectionList;
		}
		/**
		 * @return the criteria
		 */
		public DetachedCriteria getCriteria() {
			return criteria;
		}
		/**
		 * @param criteria the criteria to set
		 */
		public void setCriteria(DetachedCriteria criteria) {
			this.criteria = criteria;
		}
		/**
		 * @return the projectionList
		 */
		public ProjectionList getProjectionList() {
			return projectionList;
		}
		/**
		 * @param projectionList the projectionList to set
		 */
		public void setProjectionList(ProjectionList projectionList) {
			this.projectionList = projectionList;
		}
		@Override
		public String toString() {
			return "RulesCriteriaProjections [criteria=" + criteria
					+ ", projectionList=" + projectionList + "]";
		}
		
	}






}
