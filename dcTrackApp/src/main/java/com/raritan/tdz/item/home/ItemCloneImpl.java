package com.raritan.tdz.item.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.dto.CloneItemDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.ExceptionContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.raritan.tdz.listener.AuditTrailListener;
import org.springframework.beans.factory.annotation.Autowired;

public class ItemCloneImpl implements ItemClone {
	
	private Logger log = Logger.getLogger( this.getClass() );
	
	private ItemObjectFactory itemObjectFactory;
	private ItemDAO itemDAO;
	private ResourceBundleMessageSource messageSource;
	private Item item = null;
	private ItemObject itemObject = null;
	private CloneItemDTO cloneItemDTO = null;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public ItemCloneImpl(ItemObjectFactory itemObjectFactory,
			ItemDAO itemDAO, ResourceBundleMessageSource messageSource) {
		this.itemObjectFactory = itemObjectFactory;
		this.itemDAO = itemDAO;
		this.messageSource = messageSource;
	}

	@Override
	public Long clone(CloneItemDTO recDTO, UserInfo userInfo, ItemHome itemHome) throws DataAccessException, BusinessValidationException{
		cloneItemDTO = recDTO;
		
		if (cloneItemDTO == null || userInfo == null || itemHome == null) {
			throwBusinessValidationException(null,  "ItemValidator.cloneInvalidArgument");
		}
		
		item = itemDAO.loadItem(cloneItemDTO.getItemIdToClone());
		itemObject = this.itemObjectFactory.getItemObject(cloneItemDTO.getItemIdToClone());
				
		//validate data
		validate(itemHome, userInfo);
		
		//some classes don't have children, update this if for those classes
		cloneItemDTO.setIncludeChildren(canHasChildren() && cloneItemDTO.isIncludeChildren());
		
		//This is specifically for the data panel far end flag
		cloneItemDTO.setIncludeFarEndDataPanel(canCloneDataPanels() && cloneItemDTO.isIncludeFarEndDataPanel());

		// for free-standing, clone parent item. This will also clone the FS item
		setParentAssoc();
					
		Long newItemId = itemDAO.cloneItem(cloneItemDTO, userInfo.getUserName());

		try {
			log.info( "clone item, AuditTrailListener().saveAuditDataForInsert, newItemId: " + newItemId );
			
			new AuditTrailListener().saveAuditDataForInsert( newItemId, sessionFactory.getCurrentSession() );		
		} catch ( Exception ex ) {
			log.warn( "Exception from AuditTrailListener().saveAuditDataForInsert, newItemId: " + newItemId, ex );
		}
		
		return newItemId;
	}	

	private void validate(ItemHome itemHome, UserInfo userInfo)	throws BusinessValidationException {
		if (item == null || itemObject == null) {
			Object args[] = { "Unknown item" };
			String code = "ItemValidator.cloneInvalidItem";
			throwBusinessValidationException(args, code);
		}

		// user with view only right cannot clone
		if (userInfo.isViewer()) {
			Object args[] = { userInfo.getUserName() };
			String code = "ItemValidator.CloneAccessDenied";
			throwBusinessValidationException(args, code);
		}

		// check if this class can be clone
		if (!isClonable()) {
			Object args[] = { item.getItemName() };
			String code = "ItemValidator.CloneInvalidClass";
			throwBusinessValidationException(args, code);
		}

		if (cloneItemDTO.getItemIdToClone() < 1) {
			Object args[] = { "Unknown - ID(" + cloneItemDTO.getItemIdToClone() + ")" };
			String code = "ItemValidator.cloneInvalidItem";
			throwBusinessValidationException(args, code);
		}

		if (cloneItemDTO.getQuantity() < 1 || cloneItemDTO.getQuantity() > 999) {
			String code = "ItemValidator.CloneInvalidQuantity";
			throwBusinessValidationException(null, code);
		}

		if (cloneItemDTO.getStatusValueCode() != SystemLookup.ItemStatus.PLANNED
				&& cloneItemDTO.getStatusValueCode() != SystemLookup.ItemStatus.IN_STORAGE) {
			String code = "ItemValidator.CloneInvalidStatus";
			throwBusinessValidationException(null, code);
		}

		//Check that there is enough cabinet
		if (itemObject.isLicenseRequired()){
			if (!itemHome.isLicenseAvailable(cloneItemDTO.getQuantity())){
				Object[] args = {itemHome.getLicenseCount()};
				String code = "ItemValidator.licenseExceeded";
				throwBusinessValidationException (args, code);
			}
		}
		
		if(cloneItemDTO.getClonedItemlocationId() < 0){
			//clone item will have same location as the item to be clone
			cloneItemDTO.setClonedItemlocationId(0);
		}
	}

	@Override
	public boolean isClonable(Long classLkpVC, Long subClassLkpVC) {
		long classLkpValueCode;
		long subClassLkpValueCode;

		if (classLkpVC == null || classLkpVC.longValue() == 0) {
			return false;
		} else {
			classLkpValueCode = classLkpVC.longValue();
			subClassLkpValueCode = subClassLkpVC.longValue();
		}

		// cannot clone panel board by itself
		if (classLkpValueCode == SystemLookup.Class.FLOOR_PDU
				&& subClassLkpValueCode > 0) {
			return false;
		}

		if (classLkpValueCode == SystemLookup.Class.CABINET
				|| classLkpValueCode == SystemLookup.Class.DEVICE
				|| classLkpValueCode == SystemLookup.Class.NETWORK
				|| classLkpValueCode == SystemLookup.Class.UPS
				|| classLkpValueCode == SystemLookup.Class.FLOOR_PDU
				|| classLkpValueCode == SystemLookup.Class.FLOOR_OUTLET
				|| classLkpValueCode == SystemLookup.Class.RACK_PDU
				|| classLkpValueCode == SystemLookup.Class.CRAC
				|| classLkpValueCode == SystemLookup.Class.PROBE
				|| classLkpValueCode == SystemLookup.Class.DATA_PANEL) {
			return true;
		}

		return false;
	}
	
	private boolean isClonable() {
		if(item == null){
			return false; 
		}
		
		long classLkpValueCode = item.getClassLookup().getLkpValueCode();
		long subClassLkpValueCode = item.getSubclassLookup() == null ? -1 : item.getSubclassLookup().getLkpValueCode().longValue();
		
		return isClonable(classLkpValueCode, subClassLkpValueCode);
	}
	
	private boolean canCloneDataPanels(){
		long classLkpValueCode = item.getClassLookup().getLkpValueCode();
		
		boolean result = false;
		
		//Check to see if it is a data panel
		if (classLkpValueCode == SystemLookup.Class.DATA_PANEL)
			result = true;
		else if (classLkpValueCode == SystemLookup.Class.CABINET){
			//If it is a cabinet, then try to see if it contains any data panels
			ItemFinderDAO itemFinderDAO = (ItemFinderDAO)itemDAO;
			List<Long> itemCntList = itemFinderDAO.findChildCountFromParentIdAndClass(item.getItemId(), SystemLookup.Class.DATA_PANEL);
			if (itemCntList.size() == 1){ //The above function will return a list of 1 since it is returning a count
				Long itemCnt = itemCntList.get(0);
				if (itemCnt > 0) result = true;
			}
		}
		return result;
	}
	private boolean canHasChildren() {
		long classLkpValueCode = item.getClassLookup().getLkpValueCode();

		// These classes don't have children
		if (classLkpValueCode == SystemLookup.Class.RACK_PDU
				|| classLkpValueCode == SystemLookup.Class.FLOOR_OUTLET
				|| classLkpValueCode == SystemLookup.Class.PROBE
				|| classLkpValueCode == SystemLookup.Class.CRAC) {
			return false;
		}

		return true;
	}

	private void setParentAssoc(){ //for free-stannding, clone parent item. This will also clone the free-standing 
		if(itemObject instanceof FreeStandingItemObject){ 
			if(item.getParentItem() !=		null){
			cloneItemDTO.setItemIdToClone(item.getParentItem().getItemId());
			cloneItemDTO.setIncludeChildren(true);
			cloneItemDTO.setKeepParentChildAssoc(true); 
			} 
		}
	}
	 
	private void throwBusinessValidationException(Object args[], String code)
			throws BusinessValidationException {
		String msg = messageSource.getMessage(code, args, null);
		BusinessValidationException be = new BusinessValidationException(
				new ExceptionContext(msg, this.getClass()));
		be.addValidationError(msg);
		be.addValidationError(code, msg);
		throw be;
	}

	@Override
	public List<CloneItemDTO> removeChildren(List<CloneItemDTO> recList) throws DataAccessException{		
		for(CloneItemDTO rec:recList){
			if(rec.isIncludeChildren() == false) return recList;
			
			if(rec.getItemIdToClone() < 1) continue;
			
			List<Long> itemIds = itemDAO.getAssociatedItemIds(rec.getItemIdToClone());
				
			for(Long itemId:itemIds){
				for(CloneItemDTO x:recList){
					if(x.getItemIdToClone() == itemId.longValue()){
						x.setItemIdToClone(-1);
					}
				}
			}				
		}
		
		List<CloneItemDTO> retList = new ArrayList<CloneItemDTO>();
		
		for(CloneItemDTO rec:recList){
			if(rec.getItemIdToClone() > 0){
				retList.add(rec);
			}
		}
		
		return retList;
	}
}
