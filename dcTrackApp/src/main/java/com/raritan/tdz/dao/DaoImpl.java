package com.raritan.tdz.dao;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.ExceptionContext;

import flex.messaging.log.Log;

/**
 * A typesafe implementation of CRUD
 */
public class DaoImpl<T extends Serializable> implements Dao<T>, DaoFinderExecutor<T>, DaoUpdateExecutor<T>
{
	private SessionFactory sessionFactory;
    protected Class<T> type;
    
    private Logger log = Logger.getLogger(getClass());

    public DaoImpl() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        type = (Class) pt.getActualTypeArguments()[0];
    }
    
    public DaoImpl(Class<T> type){
    	this.type = type;
    }

    @Override
    public Long create(T o)
    {
    	Session session = getSession();
    	
        Long id = (Long) session.save(o);
        
        session.flush();
        
        // session.refresh(o);
        
        return id;
    }

    @Override
    public T read(Long id)
    {
        return (T) getSession().get(type, id);
    }

    @Override
    public void update(T o)
    {
    	Session session = getSession();
    	
    	session.update(o);
        
        session.flush();
        
        // session.refresh(o);        
    }
    
    @Override
    public void update(List<T> objects)
    {
    	Session session = getSession();
    	for (T o : objects) {
    		session.update(o);
    	}
        session.flush();
    }
    
    @Override
	public T merge(T transientObject) {
    	Session session = getSession();
    	T persistedObject = (T) session.merge(transientObject);
    	session.flush();
//    	session.refresh(transientObject);
    	return persistedObject;
	}
    
    @Override
	public void mergeOnly(T transientObject) {
    	Session session = getSession();
    	session.merge(transientObject);
    	// session.flush();
//    	session.refresh(transientObject);
	}


    @Override
	public void merge(List<T> transientObjects) {
    	Session session = getSession();
    	for (T transientObject : transientObjects) {
    		session.merge(transientObject);
    	}
    	session.flush();
	}

    @Override
	public void mergeOnly(Set<T> transientObjects) {
    	Session session = getSession();
    	for (T transientObject : transientObjects) {
    		session.merge(transientObject);
    	}
	}

    
    @Override
    public void delete(T o)
    {
    	Session session = getSession();
    	session.delete(o);
        session.flush();
    }
    
    @Override
    public Session getSession()
    {
    	return sessionFactory.getCurrentSession();
    }

    public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public Session getNewSession()
    {
   		Session newSession = sessionFactory.openSession();
    	
    	return newSession;
    }
    
    @Override
    public void  closeNewSession(Session newSession){
    	if(newSession != null){
    		newSession.close();
    	}
    }

	@Override
	public List<T> executeFinder(Method method, Object[] queryArgs, boolean readOnly) {
		final String queryName = queryNameFromMethod(method);
		final Query namedQuery = getSession().getNamedQuery(queryName);
		
		String aliasToBeanClassName = getAliasToBeanClassName(method);
		
		namedQuery.setReadOnly(readOnly);
		//String[] namedParameters = namedQuery.getNamedParameters();
		for (int i = 0; i < queryArgs.length; i++){
			Object arg = queryArgs[i];
			namedQuery.setParameter(i, arg);
		}
		
		if (aliasToBeanClassName != null){
			try {
				namedQuery.setResultTransformer(Transformers.aliasToBean(Class.forName(aliasToBeanClassName)));
			} catch (ClassNotFoundException e) {
				log.info("A ClassNotFoundException occured.");
				log.debug(e);
				return new ArrayList<T>();
			}
			catch (Exception ex) {

				throw new DAORuntimeException(ex.getMessage());
			}
		}
		
		List<T> result = (List<T>) namedQuery.list();
		
		return result == null ? new ArrayList<T>()  : result;
	}
	
	private String queryNameFromMethod(Method finderMethod){
		return type.getSimpleName() + "." + finderMethod.getName();
	}

	@Override
	public List<T> executeFetch(Method method, Object[] queryArgs) {
		final String queryName = queryNameFromMethod(method);
		
		String aliasToBeanClassName = getAliasToBeanClassName(method);

		Session session = getNewSession();
		try {
			final Query namedQuery = session.getNamedQuery(queryName);
			//String[] namedParameters = namedQuery.getNamedParameters();
			for (int i = 0; i < queryArgs.length; i++){
				Object arg = queryArgs[i];
				namedQuery.setParameter(i, arg);
			}
			if (aliasToBeanClassName != null){
				namedQuery.setResultTransformer(Transformers.aliasToBean(Class.forName(aliasToBeanClassName)));
			}
			List<T> result = (List<T>) namedQuery.list();
			
			return result == null ? new ArrayList<T>()  : result;
		} catch (ClassNotFoundException e) {
			log.info("A ClassNotFoundException occured.");
			log.debug(e);
		} finally {
            closeNewSession(session);
        }
		
		return new ArrayList<T>();
	}
	
	@Override
	public int executeUpdate(Method method, Object[] queryArgs) throws DataAccessException {
		final String queryName = queryNameFromMethod(method);
		int id = -1;
		try {
			final Query namedQuery = getSession().getNamedQuery(queryName);
			for (int i = 0; i < queryArgs.length; i++){
				Object arg = queryArgs[i];
				namedQuery.setParameter(i, arg);
			}
			
			id = namedQuery.executeUpdate();
		} catch (HibernateException e){
			log.debug(e);
			throw new DataAccessException(new ExceptionContext("Exception in trying to execute update of data", this.getClass(), e));
		}
		
		return id;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T initializeAndUnproxy(T entity) {
	    if (entity == null) {
	        throw new 
	           NullPointerException("Entity passed for initialization is null");
	    }

	    Hibernate.initialize(entity);
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
	                .getImplementation();
	    }
	    return entity;
	}

	
	@Override
	@SuppressWarnings("unchecked")
	public Map<String, Object> getFieldsValue ( Class<?> clazz, String idField, Object id, List<String> fields ) {
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(clazz);
		criteria.add(Restrictions.eq(idField, id));
		ProjectionList projList = Projections.projectionList();
		for (String field: fields) {
			projList.add(Projections.alias(Projections.property(field), field));
		}
		criteria.setProjection(projList);
		// criteria.add(Restrictions.eq(idField, id));
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return (Map<String, Object>) criteria.uniqueResult();
	}

	
	//--------------- Private methods ------------------------------------

	private String getAliasToBeanClassName(Method method) {
		Annotation[] annotations = method.getAnnotations();
		
		String aliasToBeanClassName = null;
		if (annotations.length > 0){
			try {
				for (Annotation annotation: annotations){
					if (annotation instanceof DAOAliasToBeanType){
						aliasToBeanClassName = ((DAOAliasToBeanType) annotation).beanType();
					}
				}
			} catch (SecurityException e) {
				if (Log.isDebug())
					e.printStackTrace();
			}	
		}
		return aliasToBeanClassName;
	}



	
}
