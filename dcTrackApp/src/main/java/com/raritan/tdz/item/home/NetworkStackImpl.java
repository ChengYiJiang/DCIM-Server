package com.raritan.tdz.item.home;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.UiComponentDTO;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;

/**
 * Network stack implementation.
 * @author Andrew Cohen
 */
public class NetworkStackImpl extends ItItemObject implements NetworkStack {

	public NetworkStackImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Item> getChildItems() throws DataAccessException {
		List<Item> items = null;
		
		if (item.getCracNwGrpItem() != null) {
			Session session = sessionFactory.getCurrentSession();
			try {
				Criteria c = session.createCriteria(Item.class);
				c.createAlias("cracNwGrpItem", "sibling");
				c.add( Restrictions.eq("sibling.itemId", item.getCracNwGrpItem().getItemId() ));
				c.add( Restrictions.ne("itemId", item.getItemId()));
				c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
				c.addOrder(Order.asc("sibling.numPorts"));
				//c.addOrder(Order.asc("sibling.itemId"));
				
				items = c.list();
			}
			catch(Throwable t){
				 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), t));
			}
		}
		
		if (items == null) {
			items = Collections.emptyList();
		}
		
		return items;
	}
	
	@Override
	public int getChildItemCount() throws DataAccessException {
		// TODO: optimize using count query
		return getChildItems().size();
	}

	@Override
	public Set<Long> getSubclassLookupValueCodes() {
		Set<Long> codes = new HashSet<Long>(1);
		codes.add( SystemLookup.SubClass.NETWORK_STACK );
		return Collections.unmodifiableSet( codes );
	}

	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.equals(ItItem.class);
	}

	@Override
	public boolean deleteItem()	throws ClassNotFoundException, BusinessValidationException,	Throwable {
		//Set the sibling_item_id	
		//This function return the sibling stacks link to this item
		//List of order by num_ports, item_id
		List<Item> stackList = getChildItems();
		
		if(stackList.size() > 0){
			Item sibling = stackList.get(0);
			
			if((item.getItemId() == sibling.getCracNwGrpItem().getItemId())){
				switchSiblingItemId(item.getItemId(), sibling.getItemId());
			}
		}
		
		return super.deleteItem();
	}	
	
	private void switchSiblingItemId(long oldSiblingId, long newSiblingId) throws ClassNotFoundException, BusinessValidationException,	Throwable {
		Session session = this.sessionFactory.getCurrentSession();
				
		Query q = session.createSQLQuery("update dct_items set sibling_item_id = :newSiblingId where sibling_item_id = :oldSiblingId");
		q.setLong("newSiblingId", newSiblingId);
		q.setLong("oldSiblingId", oldSiblingId);
		int ret = q.executeUpdate();
		
		if (log.isDebugEnabled()) {
			log.debug("Item Delete: Updated  " + ret + " Sibling Items");
		}
	}
	
	@Override
	protected Map<String, UiComponentDTO> saveItem(Object itemDomain, String unit)
			throws Throwable {
		
		//If it is a stack of single item, we need to ensure that item name and the grouping name are same
		if (getChildItemCount() == 0){
			ItItem item = (ItItem)itemDomain;
			item.setGroupingName(item.getItemName());
			item.setCracNwGrpItem(item);
			item.setNumPorts(1);
		}
		
		return super.saveItem(itemDomain,unit);
	}
	
	protected void postSaveItem(Long itemId,Object itemDomain, UserInfo sessionUser, Boolean isUpdate) throws DataAccessException{
		super.postSaveItem(itemId, itemDomain, sessionUser, isUpdate);
		log.debug("####### In NetworkStackImpl.postSaveItem");
		
		//propagate OS, OSI layer and other fields to all stacks
		ItItem item = (ItItem)itemDomain;
		if (item != null && item.getPropagateFields()){
				propagateValuesToStacks( itemId, item);
		}//if
	}
	
	private void replicateProperties( ItItem source, ItItem dest){
		if( source != null & dest != null &&
				null != dest.getClassLookup() && dest.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.NETWORK) ){
			dest.setItemAlias(item.getItemAlias());
			ItemServiceDetails isd = dest.getItemServiceDetails();
			isd.setFunctionLookup(item.getItemServiceDetails().getFunctionLookup());
			isd.setItemAdminTeamLookup(item.getItemServiceDetails().getItemAdminTeamLookup());
			isd.setItemAdminUser(item.getItemServiceDetails().getItemAdminUser());
			isd.setPurposeLookup(item.getItemServiceDetails().getPurposeLookup());
			isd.setDepartmentLookup(item.getItemServiceDetails().getDepartmentLookup());
			dest.setItemServiceDetails(isd);
			dest.setOsiLayerLookup(source.getOsiLayerLookup());
			dest.setOsLookup(source.getOsLookup());
		}//if
	}
	
	private void propagateValuesToStacks(Long itemId, ItItem parent) throws DataAccessException {
		int numChildren = getChildItemCount();
		if( numChildren > 0){
			List<Item> children = getChildItems();
			for( Item child : children){
				//TODO: For some reason the children that we get from hibernate does not have actual objects. It has
				//TODO: some sort of a proxy object (javassit objects)! However the data within it are just fine
				//TODO: For now force fetch the item based on the itemId. We need to fix this in the future
				//TODO: to make it more efficient.
				ItItem chld = (ItItem) sessionFactory.getCurrentSession().get(ItItem.class, child.getItemId());
				replicateProperties(parent, chld);
			}//for
		}//if
	}
}
