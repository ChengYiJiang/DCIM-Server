package com.raritan.tdz.item.home;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.dto.ValueIdDTO;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.itemObject.ItemDomainFactory;
import com.raritan.tdz.item.home.itemObject.ItemObjectTemplate;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.util.ValueIdDTOHolder;

/**
 * Item Object Factory implementation using reflection to create new ItemObject instances.
 * @author Andrew Cohen
 */
public class ItemObjectFactoryImpl implements ItemObjectFactory, ApplicationContextAware {
	
	private final Logger log = Logger.getLogger( this.getClass() );
	
	private Map<String, String> itemObjectBeans;
	
	//TODO: Remove the itemObjectBeans once the itemObjectTemplateBeans work!!!
	private Map<Long,String> itemObjectTemplateBeans;
	
	private SessionFactory sessionFactory;
	
	private ApplicationContext applicationContext;
	
	@Autowired(required=true)
	private ItemDomainFactory itemDomainFactory;
	
	
	public ItemObjectFactoryImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public ItemObjectFactoryImpl(Map<Long,String> itemObjectTemplateBeans){
		this.itemObjectTemplateBeans = itemObjectTemplateBeans;
	}
	
	@Override
	public void setItemClasses( Map<String, String> itemObjectBeans) {
		this.itemObjectBeans = itemObjectBeans;
	}
	
	@Override
	@Transactional(readOnly = true)
	public ItemObject getItemObject(long itemId) {
		Item item = (Item)sessionFactory.getCurrentSession().get(Item.class, itemId);
		if (item != null) {
			return getItemObject( item );
		}
		return null;
	}
	/*
	@Override
	public ItemObject getItemObjectUsingMounting(Item item){
		String itemCls = null;
		ItemObject retval = null;
		LksData lksData = item.getClassLookup();
		if( lksData != null ){
//			if( isFreeStandingItem(item) ){
//				//Free standing items can be Network or Device items
//				//we hard code their key to 99999
//				itemCls = itemObjectBeans.get( (new Long(9999L)).toString() );
//				retval = createItemObject(itemCls, item);
//			}else 
			
			retval = getItemObject(item);
		}
		return retval;
	}
	
	@Override
	public ItemObject getItemObjectUsingMounting(long itemId){
		Item item = (Item)sessionFactory.getCurrentSession().get(Item.class, itemId);
		
		if (item != null) {
			return getItemObjectUsingMounting(item);
		}
		
		return null;
	}*/
	
	@Override
	public ItemObject getItemObject(Item item) {
		if (item == null || itemObjectBeans == null) {
			return null;
		}
		
		
		String mounting = null;
		ModelDetails model = item.getModel();
		if( model != null ) 
			mounting = model.getMounting();
		
		String itemCls = null;
		LksData lksData = item.getSubclassLookup();
		if( lksData != null ){
			if (mounting != null){
				itemCls = itemObjectBeans.get(lksData.getLkpValueCode().toString() + "." + mounting);
			}
			
			if(itemCls == null){
				itemCls = itemObjectBeans.get(lksData.getLkpValueCode().toString());
			}
		}
		
		if (itemCls == null) {
			lksData =  item.getClassLookup();
			if (lksData != null) {
				if (mounting != null){
					itemCls = itemObjectBeans.get(lksData.getLkpValueCode().toString() + "." + mounting);
				}
			
				if(itemCls == null){
					itemCls = itemObjectBeans.get(lksData.getLkpValueCode().toString());
				}
			}
		}
		
		if (itemCls == null) {
			return null;
		}
		
		return createItemObject(itemCls, item);
	}
	
	private boolean isFreeStandingItem(Item item ){

		boolean isFreeStanding = false;
		
		LksData lksData = item.getClassLookup();
		if( lksData.getLkpValueCode() == SystemLookup.Class.NETWORK ||
				lksData.getLkpValueCode() == SystemLookup.Class.DEVICE){
			ModelDetails model = item.getModel();
			if( model != null && model.isMountingFreeStanding()) 
				isFreeStanding = true;
		}
		return isFreeStanding;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Override
	public ItemObjectTemplate getItemObjectFromModelId(Long modelId) throws InstantiationException, IllegalAccessException {
		String classLkpValue = (String) ValueIdDTOHolder.getCurrent().getValue("tiClass");
		Object subclassLkpValueObj = ValueIdDTOHolder.getCurrent().getValue("tiSubClass");
		String subclassLkpValue = subclassLkpValueObj != null ? subclassLkpValueObj.toString() : null;
		
		Item item = null;
		if (classLkpValue != null && classLkpValue.contains("Virtual Machine")){
			item = itemDomainFactory.createVMItem();
		} else if (subclassLkpValue != null && subclassLkpValue.contains("Container")){
			item = itemDomainFactory.createContainerItem(modelId);
		} else {
			item = itemDomainFactory.createItem(modelId);
		}
		return getItemObjectTemplate(item);
	}
	
	@Override
	public ItemObjectTemplate getItemObjectForVM() {
		Item item = itemDomainFactory.createVMItem();
		return getItemObjectTemplate(item);
	}

	@Override
	public ItemObjectTemplate getItemObjectFromItemId(Long itemId) {
		Item item = itemDomainFactory.findItem(itemId);
		return (getItemObjectTemplate(item));
	}

	@Override
	public ItemObjectTemplate getItemObject(Long itemId, List<ValueIdDTO> valueIdDTOList) throws InstantiationException, IllegalAccessException {
		ValueIdDTOHolder.capture(valueIdDTOList);
				
		Long modelId = null;
		if (ValueIdDTOHolder.getCurrent().getValue("cmbModel") != null){
			modelId = ValueIdDTOHolder.getCurrent().getValue("cmbModel") instanceof Integer?((Integer) ValueIdDTOHolder.getCurrent().getValue("cmbModel")).longValue()
				: (Long)ValueIdDTOHolder.getCurrent().getValue("cmbModel");
		}
		
		if( modelId != null && itemDomainFactory.isSupportedItemClass(modelId) == false ){
			return null;
		}
		if (itemId > 0) return getItemObjectFromItemId(itemId);

		return getItemObjectFromModelId(modelId);
	}
	
	private ItemObject createItemObject(String itemCls, Item item) {
		ItemObject itemObj = (ItemObject) applicationContext.getBean(itemCls);
		itemObj.init(item);
		return itemObj;
	}
	
	private ItemObjectTemplate getItemObjectTemplate(Item item) {
		LksData classLksData = item != null ? item.getClassLookup() : null;
		
		if (item != null && item.getClassMountingFormFactorValue() != null){
			String template = itemObjectTemplateBeans.get(item.getClassMountingFormFactorValue());
			if (template != null)
				return ((ItemObjectTemplate) applicationContext.getBean(template));
			else
				return null;
		} else if (classLksData != null){ 
			//If for some reason there is no model defined, we still should be able to display the item
			//and user should be able to edit the make/model
			String template = itemObjectTemplateBeans.get(classLksData.getLksId());
			if( template != null ) return ((ItemObjectTemplate) applicationContext.getBean(template));
		}
		return null;
	}
}
