/**
 * 
 */
package com.raritan.tdz.item.rulesengine;


import java.util.ArrayList;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.dctrack.xsd.UiComponent;
import com.raritan.tdz.rulesengine.RemoteRef;
import com.raritan.tdz.rulesengine.RemoteRefMethodCallback;
import com.raritan.tdz.util.UnitConverterImpl;
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
public class ModelDimensionMethodCallback implements RemoteRefMethodCallback {
	
	private SessionFactory sessionFactory;
	
	private static final String Height = "dimH";
	private static final String Width = "dimW";
	private static final String Depth = "dimD";
	
	private UnitConverterImpl inchToMMUnitConverter;

	ProjectionList proList;

	public ModelDimensionMethodCallback(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	public UnitConverterImpl getInchToMMUnitConverter() {
		return inchToMMUnitConverter;
	}

	public void setInchToMMUnitConverter(UnitConverterImpl inchToMMUnitConverter) {
		this.inchToMMUnitConverter = inchToMMUnitConverter;
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
		
		addHeight(remoteEntityName, criteria);
		addWidth(remoteEntityName, criteria);
		addDepth(remoteEntityName, criteria);
		
		criteria.setProjection(proList);
		
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		criteria.add(Restrictions.eq(filterField, filterValue));
		
		@SuppressWarnings("unchecked")
		Map<String,Long> hwdMap = (Map<String, Long>) criteria.uniqueResult();

		StringBuffer resultStr = new StringBuffer();
		if (hwdMap != null){
			Object height = hwdMap.get(Height);
			Object width = hwdMap.get(Width);
			Object depth = hwdMap.get(Depth);
			resultStr.append(additionalArgs != null ?  inchToMMUnitConverter.convert(height, (String)additionalArgs ) : height);
			resultStr.append(" X ");
			resultStr.append(additionalArgs != null ?  inchToMMUnitConverter.convert(width, (String)additionalArgs ) : width);
			resultStr.append(" X ");
			resultStr.append(additionalArgs != null ?  inchToMMUnitConverter.convert(depth, (String)additionalArgs ) : depth);
		}
		
		uiViewComponent.getUiValueIdField().setValueId(0);
		uiViewComponent.getUiValueIdField().setValue(resultStr.toString());
		
	}
	
	private void addHeight(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, Height)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"));
			}
		}
		
		String traceStr = getFieldTrace(entityName, Height);
		String defaultAlias = entityName.contains("Item") ? "model." : "";
		
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;

		proList.add(Projections.property(aliasForProjection + Height), Height);
	}
	
	private void addWidth(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, Width)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"));
			}
		}
		
		String traceStr = getFieldTrace(entityName, Width);
		String defaultAlias = entityName.contains("Item") ? "model." : "";
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;
		
		proList.add(Projections.property(aliasForProjection + Width), Width);
	}
	
	private void addDepth(String entityName, Criteria criteria) throws HibernateException, ClassNotFoundException{

		for (String alias: getAliases(entityName, Depth)){
			if (!criteria.toString().contains(alias.replace(".", "_"))){
				criteria.createAlias(alias, alias.replace(".", "_"));
			}
		}
		
		String traceStr = getFieldTrace(entityName, Depth);
		String defaultAlias = entityName.contains("Item") ? "model." : "";
		String aliasForProjection = traceStr.contains(".") ? traceStr.substring(0,traceStr.lastIndexOf(".")).replace(".", "_") + ".": defaultAlias;
		proList.add(Projections.property(aliasForProjection + Depth), Depth);
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
