package com.raritan.tdz.vbjavabridge.dao;

import org.hibernate.Query;

import com.raritan.tdz.dao.DaoImpl;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;


public class LNEventDAOImpl extends DaoImpl<LNEvent> implements LNEventDAO  {

	@Override
	public int setLnEvent(Long lksId, Long itemId, String customField1, String customField2, String customField3) {
		Query query = this.getSession().getNamedQuery("setLNEvent");
        query.setLong("operationId", lksId);
        query.setLong("itemId", itemId);
        query.setString("field1", customField1);
        query.setString("field2", customField2);
        query.setString("piqHost", customField3);
		return query.executeUpdate();
	}

}
