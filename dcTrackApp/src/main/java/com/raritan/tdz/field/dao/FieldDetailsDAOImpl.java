package com.raritan.tdz.field.dao;

import java.util.List;

import org.hibernate.Query;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.field.domain.FieldDetails;

public class FieldDetailsDAOImpl extends DaoImpl<FieldDetails> implements FieldDetailsDAO {

	@Override
	public List<FieldDetails> getFieldDetails(List<Long> fieldIds,
			Long classLookupValueCode) {
		
		Query q = this.getSession().createQuery("select fd from FieldDetails fd left join fetch fd.field f left join fetch fd.classLks c where f.fieldId in (:fieldIds) and c.lkpValueCode = :classLkpValueCode ");
		
		q.setParameterList("fieldIds", fieldIds.toArray());
		q.setLong("classLkpValueCode", classLookupValueCode);
		
		@SuppressWarnings("unchecked")
		List<FieldDetails> fds = q.list();
		
		return fds;
	}

}
