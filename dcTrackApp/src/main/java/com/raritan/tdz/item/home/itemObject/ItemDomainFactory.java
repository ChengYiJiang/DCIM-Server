/**
 * 
 */
package com.raritan.tdz.item.home.itemObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.rulesengine.ModelItemSubclassMap;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;

/**
 * This will produce item domain based on a few 
 * parameters such as modelId/itemId
 * @author prasanna
 *
 */
public class ItemDomainFactory {
	
	private final Map<Long, Class<?>> classLkpToDomainClassMap = Collections.unmodifiableMap(new HashMap<Long,Class<?>>(){{
		put(SystemLookup.Class.CABINET,CabinetItem.class);
		put(SystemLookup.Class.CRAC,MeItem.class);
		put(SystemLookup.Class.CRAC_GROUP,MeItem.class);
		put(SystemLookup.Class.DATA_PANEL,ItItem.class);
		put(SystemLookup.Class.DEVICE,ItItem.class);
		put(SystemLookup.Class.FLOOR_OUTLET,MeItem.class);
		put(SystemLookup.Class.FLOOR_PDU,MeItem.class);
		put(SystemLookup.Class.NETWORK,ItItem.class);
		put(SystemLookup.Class.PROBE,ItItem.class);
		put(SystemLookup.Class.RACK_PDU,MeItem.class);
		put(SystemLookup.Class.UPS,MeItem.class);
		put(SystemLookup.Class.UPS_BANK,MeItem.class);
		put(SystemLookup.Class.PASSIVE,ItItem.class);
		put(SystemLookup.Class.UPS_BANK,MeItem.class);

	}});
	
	@Autowired(required=true)
	private ItemDAO itemDAO;
	
	@Autowired(required=true)
	private ModelDAO modelDAO;
	
	@Autowired(required=true)
	private SystemLookupFinderDAO systemLookupFinderDAO;
	
	@Autowired
	private ModelItemSubclassMap modelItemSubclassMap;
	

	/**
	 * gets the item given item id
	 * @param itemId
	 * @return
	 */
	public Item findItem(Long itemId){
		return itemDAO.getItem(itemId);
	}
	
	/**
	 * gets the item given itemId in a new session
	 * @param itemId
	 * @return
	 */
	public Item fetchItem(Long itemId){
		return itemDAO.loadItem(itemId);
	}
	
	
	public Item createOrLoad(Long itemId,Long modelId, boolean isVM) throws InstantiationException, IllegalAccessException{
		Item item = null;
		if (itemId != null && itemId > 0) item = fetchItem(itemId);
		else if (modelId != null && modelId > 0) item = createItem(modelId);
		else if (isVM) item = createVMItem();
		return item;
	}
	
	public boolean isSupportedItemClass( Long modelId ){
		boolean retval = true;
		ModelDetails modelDetails = modelDAO.getModelById(modelId);
		if( modelDetails != null && modelDetails.getClassLookup() != null ){
			Class<?> domainClassName = classLkpToDomainClassMap.get(modelDetails.getClassLookup().getLkpValueCode());
			retval = domainClassName != null ? true : false;
		}
		return retval;
	}
	/**
	 * create an item given modelId
	 * @param modelId
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Item createItem(Long modelId) throws InstantiationException, IllegalAccessException{
		Item item = null;
		ModelDetails modelDetails = modelDAO.getModelById(modelId);
		if (modelDetails != null && modelDetails.getClassLookup() != null){
			Class<?> domainClassName = classLkpToDomainClassMap.get(modelDetails.getClassLookup().getLkpValueCode());
			if( domainClassName != null ){
				item = (Item)domainClassName.newInstance();
				item.setModel(modelDetails);
				item.setClassLookup(modelDetails.getClassLookup());
				item.setSubclassLookup(getSubClass(modelDetails));
				item.setStatusLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.ItemStatus.PLANNED).get(0));
			}
		}
		return item;
	}
	
	public Item createVMItem(){
		Item item = new ItItem();
		item.setClassLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.Class.DEVICE).get(0));
		item.setSubclassLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.SubClass.VIRTUAL_MACHINE).get(0));
		item.setStatusLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.ItemStatus.PLANNED).get(0));
		return item;
	}
	
	public Item createContainerItem(Long modelId) throws InstantiationException, IllegalAccessException{
		Item item = null;
		ModelDetails modelDetails = modelDAO.getModelById(modelId);
		if (modelDetails != null && modelDetails.getClassLookup() != null 
				&& modelDetails.getClassLookup().getLkpValueCode().equals(SystemLookup.Class.CABINET)){
			Class<?> domainClassName = classLkpToDomainClassMap.get(modelDetails.getClassLookup().getLkpValueCode());
			item = (Item)domainClassName.newInstance();
			item.setModel(modelDetails);
			item.setClassLookup(modelDetails.getClassLookup());
			item.setSubclassLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.SubClass.CONTAINER).get(0));
			item.setStatusLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.ItemStatus.PLANNED).get(0));
		}
		return item;
	}
	
	/**
	 * create an item given the modelId and setup the subclass
	 * @param modelId
	 * @param subclassLkpValueCode
	 * @return
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Item createItem(Long modelId,Long subclassLkpValueCode) throws InstantiationException, IllegalAccessException{
		Item item = createItem(modelId);
		if (item != null){
			List<LksData> subclassLksList = systemLookupFinderDAO.findByLkpValueCode(subclassLkpValueCode);
			if (subclassLksList != null && subclassLksList.size() == 1){
				item.setSubclassLookup(subclassLksList.get(0));
				item.setStatusLookup(systemLookupFinderDAO.findByLkpValueCode(SystemLookup.ItemStatus.PLANNED).get(0));
			}
		}
		return item;
	}
	
	
	private LksData getSubClass(ModelDetails modelDetails) {

		// TODO: Handle Data Panel subclass properly. For now we need this to save ZeroU data panels for US848.
		if (modelDetails.getClassLookup().getLkpValueCode().equals( SystemLookup.Class.DATA_PANEL )) return null;


		Long subclassLkpValueCode = modelItemSubclassMap.getSubclassValueCode(
            modelDetails.getClassLookup().getLkpValue(),
            modelDetails.getMounting(),
            modelDetails.getFormFactor());
		
		LksData result = null;
		if (subclassLkpValueCode != null && systemLookupFinderDAO.findByLkpValueCode(subclassLkpValueCode).size() == 1){
			result = systemLookupFinderDAO.findByLkpValueCode(subclassLkpValueCode).get(0);
		}
		
		return result;
	}
}
