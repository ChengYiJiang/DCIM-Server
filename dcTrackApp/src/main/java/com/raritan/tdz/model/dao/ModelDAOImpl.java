package com.raritan.tdz.model.dao;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.springframework.util.Assert;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.dto.ModelDTO;

public class ModelDAOImpl extends DaoImpl<ModelDetails> implements ModelDAO  {

	@Override
	public ModelDetails loadModel(Long modelId) throws DataAccessException {
		Criteria modelCriteria = this.getSession().createCriteria(ModelDetails.class);
		
		modelCriteria.add(Restrictions.eq("modelDetailId", modelId));
	
		ModelDetails model = (ModelDetails) modelCriteria.uniqueResult();
		return model;
	}

	@Override
	public List<ModelDetails> getAllBladeModelForMake(long makeId) {
		Criteria criteria = this.getSession().createCriteria(ModelDetails.class);
		criteria.createAlias("modelMfrDetails", "modelMfrDetails", Criteria.INNER_JOIN);
		criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", makeId));
		criteria.add(Restrictions.eq("mounting", "Blade"));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("modelDetailId"));
		@SuppressWarnings("unchecked")
		
		List<ModelDetails> list = criteria.list();
		
		return list;
	}

	@Override
	public List<ModelDetails> getAllBladeModelForMake(long makeId, long classLkpValueCode) {
		Criteria criteria = this.getSession().createCriteria(ModelDetails.class);
		criteria.createAlias("modelMfrDetails", "modelMfrDetails", Criteria.LEFT_JOIN);
		criteria.createAlias("classLookup", "classLookup", Criteria.LEFT_JOIN);
		criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", makeId));
		criteria.add(Restrictions.eq("mounting", "Blade"));
		criteria.add(Restrictions.eq("classLookup.lkpValueCode", classLkpValueCode));
		criteria.add(Restrictions.eq("formFactor", SystemLookup.FormFactor.FULL));
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		criteria.addOrder(Order.asc("modelDetailId"));
		@SuppressWarnings("unchecked")
		List<ModelDetails> list = criteria.list();
		return list;
	}
	

	public List getModels(Long mfrId) {
		Criteria criteria = this.getSession().createCriteria(ModelDetails.class);
		criteria.createAlias("modelMfrDetails", "modelMfrDetails", Criteria.LEFT_JOIN);
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.alias(Projections.property("modelDetailId"), "modelId"));
		proList.add(Projections.alias(Projections.property("modelName"), "modelName"));

		criteria.setProjection(proList);
		criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", mfrId));
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		List modelTOs = criteria.list();
		
		return modelTOs;
	}	
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ValueIdDTO> getAllModels() {
		Session session = this.getSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		
		proList.add(Projections.property("modelDetailId"), "data");
		proList.add(Projections.property("modelName"), "label");
		criteria.addOrder(Order.asc("modelName"));

		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}

	@Override
	public List<ValueIdDTO> getModels(Long mfrId, List<Long> includeClasses, List<Long> excludeClasses) {
		Session session = this.getSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		
		proList.add(Projections.property("modelDetailId"), "data");
		proList.add(Projections.property("modelName"), "label");
		criteria.createAlias("modelMfrDetails","modelMfrDetails");
		criteria.addOrder(Order.asc("modelName"));
		
		if (mfrId != null) {
			criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", mfrId));
		}
		
		addItemClassCriteria(criteria, false, includeClasses, excludeClasses);
		
		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ValueIdDTO.class));
		
		return criteria.list();
	}
	

	@Override
	public List<ModelDTO> getAllModelsForMake(long mfrId, List<Long> includeClasses, List<Long> excludeClasses) {
		Session session = this.getSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("classLookup","classLookup");
		criteria.createAlias("modelMfrDetails","modelMfrDetails");
		
		proList.add(Projections.property("modelDetailId"), "modelId");
		proList.add(Projections.property("modelName"), "modelName");
		proList.add(Projections.property("ruHeight"), "ruHeight");
		proList.add(Projections.property("classLookup.lksId"), "classLksId");
		proList.add(Projections.property("mounting"), "mounting");
		proList.add(Projections.property("formFactor"), "formFactor");
		proList.add(Projections.property("isStandard"), "isStandard");
		
		criteria.addOrder(Order.asc("modelName"));
		
		if (mfrId >= 0) {
			criteria.add(Restrictions.eq("modelMfrDetails.modelMfrDetailId", mfrId));
		}
        		
		addItemClassCriteria(criteria, true, includeClasses, excludeClasses);
        
        // Filtered out the deleted models
        criteria.add(Restrictions.not(Restrictions.eq("statusLookup.lksId", new Long(2))));		

		criteria.setProjection(proList);
		criteria.setResultTransformer(Transformers.aliasToBean(ModelDTO.class));
		
		return criteria.list();
	}

	@Override
	public Long getModelIdByName(String modelName) {
		Long retval = -1L;
		Session session = this.getSession();
		ProjectionList proList = Projections.projectionList();
		
		Criteria criteria = session.createCriteria(ModelDetails.class);
		
		proList.add(Projections.property("modelDetailId"));
		criteria.add(Restrictions.eq("modelName", modelName));		
		criteria.setProjection(proList);		
		List<Long>list = criteria.list();
		Assert.isTrue(list.size() <= 1);
		if( list.size() == 1) retval = list.get(0);
		return retval;

	}


	@Override
	public ModelDetails getModelByName(String modelName) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.add(Restrictions.eq("modelName", modelName));		
		List<ModelDetails>list = criteria.list();
		ModelDetails model = null;
		
		if( list.size() >= 1) model = (ModelDetails)list.get(0);
		
		return model;

	}

	@Override
	public ModelDetails getModelById(Long modelId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.add(Restrictions.eq("modelDetailId", modelId));		
		List<ModelDetails>list = criteria.list();
		ModelDetails model = null;
		
		Assert.isTrue(list.size() <= 1);
		
		if( list.size() == 1) model = (ModelDetails)list.get(0);
		
		return model;

	}
		
	

	/**
	 * This may be removed once we have the seperate tables for model ports 
	 * which contain subclass.
	 * @param modelId
	 * @return
	 */
	@Override
	public Long getClassLkpValueCode(long modelId) {
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.createAlias("classLookup", "classLookup");
		criteria.setProjection(Projections.property("classLookup.lkpValueCode"));
		criteria.add(Restrictions.eq("modelDetailId", modelId));
		
		Long classLookupValueCode = (Long) criteria.uniqueResult();
		return classLookupValueCode;
	}	
	

	private void addItemClassCriteria(Criteria c, boolean addedClassLookupAlias, List<Long> includeClasses, List<Long> excludeClasses) {
		
		if (includeClasses != null && !includeClasses.isEmpty()) {
			if (!addedClassLookupAlias) c.createAlias("classLookup","classLookup");
			addedClassLookupAlias = true;
			c.add( Restrictions.in("classLookup.lkpValueCode", includeClasses) );
		}
			
		if (excludeClasses != null && !excludeClasses.isEmpty()) {
			if (!addedClassLookupAlias) c.createAlias("classLookup","classLookup");
			c.add( Restrictions.not( Restrictions.in("classLookup.lkpValueCode", excludeClasses) ) );
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> getModelMountingAndRuHeight(long modelId) {
        Session session = this.getSession();
        Criteria criteria = session.createCriteria(ModelDetails.class);
		criteria.setProjection((Projections.projectionList()
				.add(Projections.alias(Projections.property("mounting"), "mounting"))
				.add(Projections.alias(Projections.property("ruHeight"), "ruHeight"))));
        criteria.add(Restrictions.eq("modelDetailId", modelId));
		criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		return (Map<String, Object>) criteria.uniqueResult();
	}

	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getModelMountingAndRuHeight(long modelId, List<String> projections) {
		Session session = this.getSession();
	    Criteria criteria = session.createCriteria(ModelDetails.class);
        
        ProjectionList proList = Projections.projectionList();
        for (String s: projections) {
            proList.add (Projections.alias(Projections.property(s), s));
        }
        criteria.setProjection(proList);

        criteria.add(Restrictions.eq("modelDetailId", modelId));
        criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        return (Map<String, Object>) criteria.uniqueResult();
	}
}
