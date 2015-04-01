package com.raritan.tdz.field.home;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;

import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.dto.ObjectIdDTO;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.field.domain.FieldDetails;
import com.raritan.tdz.field.domain.Fields;
import com.raritan.tdz.field.dto.FieldDTO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * @author buntyn
 *
 */
public class FieldHomeImpl implements FieldHome {

	private SessionFactory sessionFactory;

	public FieldHomeImpl(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.field.home.FieldHome#getItemFields(java.lang.Long)
	 */
	@Override
	public Map<String, UiComponentDTO> getItemFields(Long classLkpValueCode) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria fieldCriteria = session.createCriteria(FieldDetails.class);

		fieldCriteria.createAlias("classLks", "classLks", Criteria.INNER_JOIN);
		fieldCriteria.createAlias("field", "field", Criteria.INNER_JOIN);

		 // * select * from dct_item_fields;

		ProjectionList fieldProList = Projections.projectionList();
		fieldProList.add(Projections.alias(Projections.property("field.uiComponentId"), "uiComponentId"));
		fieldProList.add(Projections.alias(Projections.property("isRequiedAtSave"), "IsRequiedAtSave"));
		fieldCriteria.setProjection(fieldProList);

		fieldCriteria.add(Restrictions.eq("classLks.lkpValueCode", classLkpValueCode));

		fieldCriteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		@SuppressWarnings("unchecked")
		List<HashMap<?, ?>> fieldMap = fieldCriteria.list();
		assert(fieldMap != null);
		Map<String, UiComponentDTO> returnVal = new HashMap<String, UiComponentDTO>();
		//returnVal = generateFieldMapData(fieldMap);
		return returnVal;
	}

	private Long getFieldDetailsId(final FieldDTO fieldDTO) {
		Session session = this.sessionFactory.getCurrentSession();
		Criteria fieldCriteria = session.createCriteria(FieldDetails.class);

		fieldCriteria.createAlias("classLks", "classLks", Criteria.INNER_JOIN);
		fieldCriteria.createAlias("field", "field", Criteria.INNER_JOIN);

		 // * select * from dct_item_fields;

		ProjectionList fieldProList = Projections.projectionList();
		fieldProList.add(Projections.alias(Projections.property("fieldDetailId"), "FieldDetailId"));
		fieldCriteria.setProjection(fieldProList);

		fieldCriteria.add(Restrictions.eq("field.uiComponentId", fieldDTO.getUiComponentId()));
		if (fieldDTO.getClassLksId() == -1) { // use lku for restrictions
			fieldCriteria.add(Restrictions.eq("field.customLku.lkuId", fieldDTO.getClassLkuId()));
		} else { // use lks for restrictions
			fieldCriteria.add(Restrictions.eq("classLks.lksId", fieldDTO.getClassLksId()));
		}

		fieldCriteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		@SuppressWarnings("unchecked")
		List<HashMap<?, ?>> fieldMap = fieldCriteria.list();
		assert(fieldMap != null);
		Long returnVal = -1L;
		returnVal = getFieldDetailsId(fieldMap);
		return returnVal;
	}

	private Long getFieldDetailsId(List<HashMap<?, ?>> fieldMap) {
		Long fieldDetailId = -1L;
		Iterator<HashMap<?, ?>> tableMap = fieldMap.iterator();
		assert(tableMap != null);
		while (tableMap.hasNext()) {
			HashMap<?,?> row = tableMap.next();
			System.out.println(row.size());
			for (Iterator<?> it = row.entrySet().iterator(); it.hasNext(); ) {
				@SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry) it.next();
				Object key = entry.getKey();
				Object value = entry.getValue();
				assert(key != null);
				assert(value != null);
				System.out.println("key = " + key.toString() + " value = " + value.toString());
				if (key.toString().equals("FieldDetailsId")) {
					fieldDetailId = Long.parseLong(value.toString());
				}
			}
		}

		return fieldDetailId;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.field.home.FieldHome#updateItemFields(java.util.List)
	 */
	@Override
	public Map<String, UiComponentDTO> updateItemFields(List<FieldDTO> values) {
		// TODO Auto-generated method stub
		Long fieldDetailsId = -1L;
		Iterator<FieldDTO> it = values.iterator();
		while (it.hasNext()) {
			FieldDTO dto = it.next();
			assert(dto != null);
			Session session = sessionFactory.getCurrentSession();
			/* Serializable */ fieldDetailsId = getFieldDetailsId(dto);
			FieldDetails fieldDetails = (FieldDetails) session.get(FieldDetails.class, fieldDetailsId);
			if (null != fieldDetails) {
				fieldDetails.setIsRequiedAtSave(dto.getIsRequired());
				session.update(fieldDetails);
			}

		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FieldDetails> getFieldDetailsList(long classValueCode) throws DataAccessException {
		List<FieldDetails> recList = null;

		try {
			Session session = sessionFactory.getCurrentSession();
			Criteria c = session.createCriteria(FieldDetails.class);
			c.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			if (classValueCode != 0L) {
				c.createAlias("classLks", "classLks");
				c.add(Restrictions.eq("classLks.lkpValueCode", classValueCode));
			}
			recList = c.list();
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATIONS_SETTING_FETCH_FAILED, this.getClass(), t));
		}

		return recList;
 	}

	@Override
	public void updateFieldDetail(FieldDetails fieldDetail) throws DataAccessException {
		Session session = null;

		try {
			session = sessionFactory.getCurrentSession();
			session.update(fieldDetail);
		}
		catch (Throwable t) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.APPLICATION_SETTING_UPDATE_FAILED, this.getClass(), t));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FieldDetails> getFieldDetail( String uiComponentId, Long fieldDetailId, Long classValueCode) {
		Session session = sessionFactory.getCurrentSession();

		Criteria c = session.createCriteria(FieldDetails.class);

		if (0L != classValueCode) {
			c.createAlias("classLks", "classLks");
			c.add(Restrictions.eq("classLks.lkpValueCode", classValueCode));
		}

		c.createAlias("field", "field");
		if(fieldDetailId != null){
			c.add(Restrictions.eq("fieldDetailId", fieldDetailId));
		}
		else{
			c.add(Restrictions.eq("field.uiComponentId", uiComponentId));
		}

		c.addOrder( Order.desc("field.sortOrder") );

		return ((List<FieldDetails>) c.list());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean isThisFieldRequiredAtSave( String uiComponentId, Long fieldDetailId, Long classValueCode) {
		Session session = sessionFactory.getCurrentSession();

		Criteria c = session.createCriteria(FieldDetails.class);
		c.add(Restrictions.eq("isRequiedAtSave", true));

		if (0L != classValueCode) {
			c.createAlias("classLks", "classLks");
			c.add(Restrictions.eq("classLks.lkpValueCode", classValueCode));
		}

		if(fieldDetailId != null){
			c.add(Restrictions.eq("fieldDetailId", fieldDetailId));
		}
		else{
			c.createAlias("field", "field");
			c.add(Restrictions.eq("field.uiComponentId", uiComponentId));
		}

		ProjectionList fieldProList = Projections.projectionList();
		// fieldProList.add(Projections.property("isRequiedAtSave"));
		fieldProList.add(Projections.rowCount());
		c.setProjection(fieldProList);
		

		return ((long) c.uniqueResult() > 0);
		
		// return (c.list().size() > 0 ? true : false);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<FieldDetails> getFieldDetail( Long classValueCode) {
		Session session = sessionFactory.getCurrentSession();

		Criteria c = session.createCriteria(FieldDetails.class);
		/*c.createAlias("field", "field");*/

		if (0L != classValueCode) {
			c.createAlias("classLks", "classLks");
			c.add(Restrictions.eq("classLks.lkpValueCode", classValueCode));
		}

		return ((List<FieldDetails>) c.list());
	}
	
	@Override
	public Map<String, Boolean> getFieldRequiredDetail( Long classValueCode ) {

		if (null == classValueCode) return new HashMap<String, Boolean>();
		
		Session session = sessionFactory.getCurrentSession();

		Criteria c = session.createCriteria(FieldDetails.class);
		c.createAlias("field", "field");
		
		if (0L != classValueCode) {
			c.createAlias("classLks", "classLks");
			c.add(Restrictions.eq("classLks.lkpValueCode", classValueCode));
		}
		
		ProjectionList proList = Projections.projectionList();
		proList.add(Projections.property("field.uiComponentId"), "id");
		proList.add(Projections.property("isRequiedAtSave"), "value");
		c.setProjection(proList);
		
		c.setResultTransformer(Transformers.aliasToBean(ObjectIdDTO.class));

		@SuppressWarnings("unchecked")
		List<ObjectIdDTO> valueList = c.list();
		
		Map<String, Boolean> portIdActionMap = new HashMap<String, Boolean>();
		for (ObjectIdDTO dto: valueList) {
			portIdActionMap.put((String) (dto.getId()), (Boolean) (dto.getValue()));
		}
		
		return portIdActionMap;
	}


	@Override
	public String getDefaultName(String uiId) {
		Session session = sessionFactory.getCurrentSession();

		Criteria c = session.createCriteria(Fields.class);
		c.setProjection(Projections.property("defaultName"));
		c.add(Restrictions.eq("uiComponentId", uiId));

		List list = c.list();
		if (list.size() > 0)
			return (String) list.get(0);
		else
			return "";
	}
}