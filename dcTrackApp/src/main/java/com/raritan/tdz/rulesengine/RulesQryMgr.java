/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Criterion;


import com.raritan.dctrack.xsd.DcTrack;

/**
 * @author prasanna
 *
 */
public interface RulesQryMgr {
	
	public void createCriteria(String remoteRef, String xPath, String altDataKey);
	public void addAlias(String remoteRef, String xPath, String altDataKey) throws HibernateException, ClassNotFoundException;
	public void addProjection(String remoteRef, String xPath, String altDataKey) throws HibernateException, ClassNotFoundException;
	public void addFilter(String remoteRef, String filterField, Object filterValue, String operator) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException;
	public void addFilter(Map<String, Filter> filterMap);
	public void clearAll();

	public List getData(Session session);
	
	
}
