package com.raritan.tdz.mfr.dao;

import com.raritan.tdz.dao.Dao;
import com.raritan.tdz.domain.ModelMfrDetails;

public interface ModelMfrDetailsDAO extends Dao<ModelMfrDetails> {

	/**
	 * get the manufacturer by its name
	 * @param mfrName
	 * @return
	 */
	public ModelMfrDetails getMfrByName(String mfrName);
	
}
