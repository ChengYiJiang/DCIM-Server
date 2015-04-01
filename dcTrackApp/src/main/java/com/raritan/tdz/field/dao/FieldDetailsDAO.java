package com.raritan.tdz.field.dao;

import java.util.List;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.field.domain.FieldDetails;

public interface FieldDetailsDAO extends Dao<FieldDetails> {

	public List<FieldDetails> getFieldDetails(List<Long> fieldIds, Long classLookupValueCode);
	
}
