package com.raritan.tdz.mfr.dao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.util.Assert;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.domain.ModelMfrDetails;

public class ModelMfrDetailsDAOImpl extends DaoImpl<ModelMfrDetails> implements ModelMfrDetailsDAO {

	@Override
	public ModelMfrDetails getMfrByName(String mfrName) {
		
		Session session = this.getSession();
		Criteria criteria = session.createCriteria(ModelMfrDetails.class);
		criteria.add(Restrictions.eq("mfrName", mfrName));		
		@SuppressWarnings("unchecked")
		List<ModelMfrDetails>list = criteria.list();
		ModelMfrDetails mfr = null;
		
		Assert.isTrue(list.size() <= 1);
		
		if( list.size() == 1) mfr = (ModelMfrDetails)list.get(0);
		
		return mfr;
		
	}

}
