package com.raritan.tdz.item.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * UPS bank business implementaiton.
 * @author Andrew Cohen
 */
class UPSBankImpl extends ItemObjectBase implements UPSBank {

	public UPSBankImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
	}
	
	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.Class.UPS_BANK );
		return Collections.unmodifiableSet( codes );
	}

	@Override
	public List<Item> getChildItems() throws DataAccessException {
		// TODO: 
		return new LinkedList<Item>();
	}

	@Override
	public int getChildItemCount() throws DataAccessException {
		int count = 0;
		
		Session session = sessionFactory.getCurrentSession();
		try {
			Query q = session.createQuery("select count(*) from MeItem where upsBankItem.itemId = :itemId");
			q.setLong("itemId", item.getItemId());
			count = (Integer)q.uniqueResult();
		}
		catch(Throwable t){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
		}
		
		return count;
	}
	
	@Override
	public int getLinkedUPSCount() throws DataAccessException {
		int count = 0;
		
		Session session = sessionFactory.getCurrentSession();
		try {
			Query q = session.getNamedQuery("UPSBankLinkedUPSCount");
			q.setLong("itemId", item.getItemId());
			q.setLong("upsCode", SystemLookup.Class.UPS);
			count = ((Long)q.uniqueResult()).intValue();
		}
		catch(Throwable t){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
		}
		
		return count;
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(MeItem.class);
	}
}
