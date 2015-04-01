package com.raritan.tdz.dao;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.util.BladeChassisTracerHandler;
import com.raritan.tdz.util.ParentTracerHandler;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * This is generic kind of DAO object loader. Use it in code that is generic and needs to load multiple types of DAOs
 * 
 * @author bozana
 *
 */
public class GenericDAOLoader {
	private Session session; 
	private SessionFactory sessionFactory;
	private final Logger log = Logger.getLogger(GenericDAOLoader.class);
	  
	GenericDAOLoader( SessionFactory sessionFactory ){
	        this.sessionFactory = sessionFactory;
	}

	private List<Field> traceFields(String remoteType, String fieldName)
			throws ClassNotFoundException {
		ObjectTracer objectTrace = new ObjectTracer();
		objectTrace.addHandler("^[ahandleDDResultForNewItem-z].*LkpValue", new LksDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		objectTrace.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		objectTrace.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		objectTrace.addHandler("itemAdminUser.*", new UserIdTracerHandler());
		objectTrace.addHandler("parentItem.*", new ParentTracerHandler());
		objectTrace.addHandler("bladeChassis.*", new BladeChassisTracerHandler());
		List<Field> fields = objectTrace.traceObject(Class.forName(remoteType), fieldName);
		return fields;
	}
	
	/**
	 * 
	 * @param remoteType - string describing entity (e.g. com.raritan.tdz.domain.DataCenterLocationDetails)
	 * 
	 * @param fieldId - name of the id field (as defined in rulesengine.xml) 
	 * 					that you want to load ( e.g. dcTypeLkuValueCode )
	 * @param fieldIdValue - value of the id field ( e.g. (Long) 515 )
	 * 
	 * @return - loaded object (e.g. LksData)
	 * 
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public Object load(String remoteType, String fieldId, Object fieldIdValue) throws ClassNotFoundException {
		Object result = null;

		if(fieldIdValue != null){
			//Create Alias
			List<Field> fields = traceFields(remoteType, fieldId);
				if (fields.size() >= 2 && ((Long)fieldIdValue) > 0){
					Field field = fields.get(fields.size() - 2);
					
					Session session = sessionFactory.getCurrentSession();
					Criteria criteria = session.createCriteria(field.getType());
					criteria.add(Restrictions.eq(fields.get(fields.size() - 1).getName(), fieldIdValue));
					result = criteria.uniqueResult();
				} else {
					result = fieldIdValue;
				}
			}
			return result;
		}

	public Long getId(String entity, String property, String propertyId, String value,
			Map<String, Object> additionalAlias, Map<String, Object> additionalRestriction)
					throws ClassNotFoundException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(entity);

		List<String> aliases = ObjectTracer.getAliases(property);
		for( String alias: aliases){
			String alias_name = alias.replace(".", "_");
			criteria.createAlias(alias, alias_name);
		}
		criteria.add(Restrictions.eq(property, value).ignoreCase());
		
		if(additionalAlias != null && additionalAlias.size() > 0){
			for( String key : additionalAlias.keySet()){
				String alias = (String)additionalAlias.get(key);
				criteria.createAlias(alias,alias);
			}
		}

		if(additionalRestriction != null && additionalRestriction.size() > 0){
			for( String key : additionalRestriction.keySet()){
				criteria.add(Restrictions.eq(key, additionalRestriction.get(key)));
			}
		}

		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property(propertyId));
		criteria.setProjection(proList);
		List list = criteria.list();

		if( list.size() < 1 ){
			log.error("list.size()=0 for: entity=" + entity + ", property=" + property
					+ ", propertyId=" + propertyId + ", value=" + value);
			if(additionalRestriction != null){
				for( String key : additionalRestriction.keySet()){
					log.error("additional restrictions: " + additionalRestriction.get(key));
				}
			}
			return null;
		} else {
			return (Long) list.get(0);
		}
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

}
