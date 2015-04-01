/**
 * 
 */
package com.raritan.tdz.util;



import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UniqueValidator;

/**
 * @author bozana
 *
 */
public class UniqueValidatorBase implements UniqueValidator {
	private static Logger log = Logger.getLogger("UniqueValidator");
	
	protected SessionFactory sessionFactory;
	public SessionFactory getSessionFactory(){ return this.sessionFactory; }
	
	public UniqueValidatorBase(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Boolean isUnique(String entityName, String entityProperty, Object value, String siteCode) throws DataAccessException, HibernateException, ClassNotFoundException{
		return isUnique(entityName, entityProperty, value, siteCode, -1L, null, null);
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.UniqueValidator#isUnique(java.lang.String, java.lang.Object)
	 */
	@Override
	public Boolean isUnique(String entityName, String entityProperty, Object value, String siteCode, Long parentId, String ignoreProperty, Object ignorePropertyValue) throws DataAccessException, HibernateException, ClassNotFoundException{
		Class<?> entityClass;
        entityClass = Class.forName(entityName);
		Session session = this.sessionFactory.getCurrentSession();

		ObjectTracer oTracer = new ObjectTracer();
		oTracer.traceObject(entityClass, entityProperty);
		String traceStr= oTracer.toString();		
		log.debug("traceStr=" + traceStr + ", entityName=" + entityName + ", entityProperty=" + entityProperty + ", value=" + value);

		Criteria criteria = session.createCriteria(entityName);
		List<String> aliases  = oTracer.getAliases(traceStr);

		for( String alias: aliases){
			String alias_name = alias.replace(".", "_");
			criteria.createAlias(alias, alias_name);
			log.debug("adding alias: " + alias_name + "for " + alias);
		}
		
		StringBuffer restrictionStr = new StringBuffer();
		if( aliases.size() > 0){
			restrictionStr.append(aliases.get(aliases.size()-1)).append(".");
		}
		restrictionStr.append(entityProperty);		
		log.debug("restrictionStr=" + restrictionStr);

		criteria.add(Restrictions.eq(restrictionStr.toString(), value).ignoreCase());
		addSiteRestriction(criteria, siteCode, parentId);
		addParentRestriction(criteria,parentId);
		addAdditionalRestrictions(criteria);
		
		if (ignoreProperty != null && ignorePropertyValue != null) {
			Object val = ignorePropertyValue;
			if (val instanceof Number) {
				val = ((Number)val).longValue();
			}
			criteria.add(Restrictions.ne(ignoreProperty, val));
		}

        criteria.setProjection(Projections.rowCount());
        Long numElems = (Long) criteria.uniqueResult();
        log.debug(entityName + ": " + entityProperty + ": , value=" + value + ", numElems=" + numElems);

        return (numElems > 0 ? false : true);
	}




	
	protected void addSiteRestriction(Criteria criteria, String siteCode, Long parentId) {
		//Do nothing here. the subclass may perform site restriction functionality
	}
	
	protected void addParentRestriction(Criteria criteria, Long parentId){
		//Do nothing here. the subclass may perform parent restriction functionality
	}
	
	protected void addAdditionalRestrictions(Criteria criteria){
		//Do nothing here. the subclass may perform some additional restriction functionality.
	}

}
