/**
 * 
 */
package com.raritan.tdz.item.itemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.domain.PowerPortMove;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.rulesengine.RulesNodeEditability;

/**
 * @author prasanna
 *
 */
public class ItemStateContextImpl implements ItemStateContext {
		
	@Autowired
	ModelDAO modelDAO;
	
	@Autowired
	RequestDAO requestDAO;
	
	@Autowired
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired
	PortMoveDAO<PowerPortMove> powerPortMoveDAO;
	
	//The String here must be following the convention below
	//classLookupLkpValueCode:statusLookupLkpValueCode and
	//appropriate ItemState beans should be associated.
	private Map<String, ItemState> itemStateMap;
	
	private SessionFactory sessionFactory;
	
	private HashMap<Long, ArrayList<Long>> statusLookupMap = new HashMap<Long, ArrayList<Long>>(); 
	
	public HashMap<Long, ArrayList<Long>> getStatusLookupMap() {
		return statusLookupMap;
	}

	public void setStatusLookupMap(HashMap<Long, ArrayList<Long>> statusLookupMap) {
		this.statusLookupMap = statusLookupMap;
	}
	
	//This are the invalid states for a given subclass
	//TODO: We should not be needing this if we implement the ItemState based on ModelUniqueValue 
	//      defined in SystemLookup as we can take care of not-only for the subclass but other scenarios as well
	//      THIS IS A TEMP FIX for 3.0.0 until we fix it in the next dot release
	private List<String> invalidSubclassStates; 
	
	
	public ItemStateContextImpl(Map<String, ItemState> itemStateMap, SessionFactory sessionFactory) {
		super();
		this.itemStateMap = itemStateMap;
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.ItemStateContext#getEditability(com.raritan.tdz.domain.Item)
	 */
	@Override
	public RulesNodeEditability getEditability(Item item) {
		return itemStateMap.get(SavedItemData.getItemStateKey(item)).getEditability(item);
	}

	@Override
	public boolean supports(Class<?> arg0) {
		return true;
	}

	@Override
	public void validateAllButReqFields(Object target, UserInfo sessionUser, Errors errors) {
		Item item = (Item)target;
		
		canTransition(item, errors);
		if (itemStateMap.get(SavedItemData.getItemStateKey((Item)target)) != null)
			itemStateMap.get(SavedItemData.getItemStateKey((Item)target)).validateAllButReqFields(target, sessionUser, errors);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		Item item = (Item)target;
		
		canTransition(item, errors);
		if (itemStateMap.get(SavedItemData.getItemStateKey((Item)target)) != null)
			itemStateMap.get(SavedItemData.getItemStateKey((Item)target)).validate(target, errors);
	}

	@Override
	public void onSave(Item item) throws DataAccessException, BusinessValidationException, ClassNotFoundException {
		if (itemStateMap.get(SavedItemData.getItemStateKey(item)) != null)
			itemStateMap.get(SavedItemData.getItemStateKey(item)).onSave(item);
	}
	
	
	
	public List<String> getInvalidSubclassStates() {
		return invalidSubclassStates;
	}

	public void setInvalidSubclassStates(List<String> invalidSubclassStates) {
		this.invalidSubclassStates = invalidSubclassStates;
	}

	private ItemState getCurrentItemState(Item item){
		ItemState itemState = null;
		
		String itemStateKey = SavedItemData.getCurrentItemStateKey();
		
		if (isItemInvalidState(itemStateKey, item)) return null;
		
		if (itemStateKey != null){
			//return the state that is in the database
			itemState = itemStateMap.get(itemStateKey);
		}else{
			//return the state which client currently set
			itemState = itemStateMap.get(SavedItemData.getItemStateKey(item));
		}
		
		return itemState;
	}
	
	
	//TODO: We should not be needing this if we implement the ItemState based on ModelUniqueValue 
	//      defined in SystemLookup as we can take care of not-only for the subclass but other scenarios as well
	//      THIS IS A TEMP FIX for 3.0.0 until we fix it in the next dot release
	private boolean isItemInvalidState(String itemStateKey, Item item){
		String newKey = item.getSubclassLookup() != null?itemStateKey + ":" + item.getSubclassLookup().getLkpValueCode() : itemStateKey;
		return invalidSubclassStates.contains(newKey);
	}

	
	@Override
	public boolean canTransition(Item item, Errors errors) {
		
		boolean result = true;
		
		if (item.getItemId() > 0){
			// CR 46280 - The following hibernate code will NOT get the original item data!
			// It will always get from cache, so we save the original state before the convert()
			//Session session = sessionFactory.getCurrentSession();
			//Item dbItem = (Item) session.load(Item.class, item.getItemId());
			String itemStateKey = SavedItemData.getCurrentItemStateKey();
			Long itemStatusValueCode = SavedItemData.getCurrentItemStatusValueCode();
			
			if (itemStateMap.get(itemStateKey) != null) {
				result = itemStateMap.get(itemStateKey).canTransition(item);
				
				// Check if the item has any pending request(s)
				//If we cannot transition from current state to new state, the add an entry into the errors object
				if (!result)
				{
					LksData itemStatusLks = SystemLookup.getLksData(sessionFactory.getCurrentSession(), itemStatusValueCode);
					String lksStatusValue = itemStatusLks != null ? itemStatusLks.getLkpValue() : "<Unknown>";
					Object[] errorValues = { item.getItemName(), lksStatusValue , (null != item.getStatusLookup()) ? item.getStatusLookup().getLkpValue() : "" };
					errors.rejectValue("itemStatusLookup", "ItemValidator.invalidTransition", errorValues, "Cannot transition to a different state");
				}
				
				// Check if the item has pending requests
				if (result && !itemStatusValueCode.equals(item.getStatusLookup().getLkpValueCode())) {
					Long movingItemId = powerPortMoveDAO.getMovingItemId(item.getItemId());
					Long itemId = (null != movingItemId) ? movingItemId : item.getItemId();
					try {
						List<Request> itemRequests = itemRequestDAO.getPendingItemRequests(itemId); // requestDAO.getAllPendingRequestsForAnItem(itemId);
						result = (null == itemRequests || itemRequests.size() == 0);
					}
					catch (DataAccessException ex) {
						result = false;
					}
					if (!result) {
						Object[] errorValues = { item.getItemName() };
						errors.rejectValue("itemStatusLookup", "ItemValidator.invalidTransitionRequestPending", errorValues, "Cannot transition to a different state, pending request(s)");
						
					}
				}
				
				return result;
			}
		}
		
		//new item that has item status set
		if( item.getItemId() <= 0 && item.getStatusLookup() != null && item.getStatusLookup().getLkpValueCode() != null){
			result = false;
			ModelDetails model = item.getModel() != null ? item.getModel() : null;
			
			List<Long> newItemStates = getStatusList(model);
			for( Long state : newItemStates ){
				if( state.longValue() == item.getStatusLookup().getLkpValueCode().longValue()){
					result = true;
					break;
				}
			}
		}
			
		if (!result){
			String itemName = (item == null) ? "<Unknown>":item.getItemName();
			String itemClass = item.getClassLookup() != null ? item.getClassLookup().getLkpValue():"<Unknown>";
			String itemSubClass = item.getSubclassLookup() != null ? " " + item.getSubclassLookup().getLkpValue() :"";
			String itemStatusLkpValue = item.getStatusLookup() != null ? " " + item.getStatusLookup().getLkpValue() : "<Unknown>";
			Object[] errorValues = { itemName, itemClass, itemSubClass, itemStatusLkpValue };
			errors.rejectValue("itemStatusLookup", "ItemValidator.invalidTransition.newItem", errorValues, "Cannot transition to a different state");
		}
		
		return result;
	}

	@Override
	public List<Long> getAllowableStates(Item item, UserInfo userInfo) {
		List<Long> states = null;
		List<Long> statesToRemove = new ArrayList<Long>();
		
		if( isTransitionPermittedForUser(item, userInfo)){
			StringBuffer key = new StringBuffer();
			key.append(item.getClassLookup().getLkpValueCode());
			key.append(":");
			key.append(item.getStatusLookup().getLkpValueCode());
			if (itemStateMap.get(key.toString()) != null){
				// get allowable states
				states = itemStateMap.get(key.toString()).getAllowableStates();
				
				// identify states that needs to be removed from allowable states
				// 1. based on invalid subclass states
				// 2. based on user permission
				for (Long state: states) {
					if ((item.getClassLookup() != null &&
							isItemInvalidState(item.getClassLookup().getLkpValueCode()+":"+state, item)) ||
							(istransitionToStatePermittedForUser( item, state, userInfo) == false)) {
						statesToRemove.add(state);
					}
				}
				// Hack: currently there is no cleaner way to get the states for VM, PDU , etc,
				states.removeAll(statesToRemove);
				
			}else{
				//TODO: Remove this when we have all the are states defined in the ItemStateChart.xml
				states = new ArrayList<Long>();
				Session session = sessionFactory.getCurrentSession();
				List<LksData> lksDataList = SystemLookup.getLksData(session, SystemLookup.LkpType.ITEM_STATUS);
				for (LksData lksData:lksDataList){
					states.add(lksData.getLkpValueCode());
				}
			}
		}
		return states;
	}

	private boolean isTransitionPermittedForUser( Item item, UserInfo userInfo){
		if (null != item.getStatusLookup()) {
			return istransitionToStatePermittedForUser(item, item.getStatusLookup().getLkpValueCode(), userInfo);
		}
		else {
			return true;
		}
	}

	private boolean istransitionToStatePermittedForUser(Item item,  long lkpValueCode, UserInfo userInfo){
		ItemState itemState = getCurrentItemState(item);
		
		if(itemState == null) return false;
		
		boolean retval = itemState.isTransitionPermittedForUser(item, lkpValueCode, userInfo);
		return retval;
	}

	private boolean statusChanged(Item item) {
		Long dbItemStatusValueCode = (null != SavedItemData.getCurrentItemStatusValueCode()) ? SavedItemData.getCurrentItemStatusValueCode() : null;
		Long itemStatusValueCode = (null != item.getStatusLookup()) ? item.getStatusLookup().getLkpValueCode() : null;
		if (null != dbItemStatusValueCode && null != itemStatusValueCode && dbItemStatusValueCode.longValue() != itemStatusValueCode.longValue()) {
			return true;
		}
		else if ((null == dbItemStatusValueCode && null != itemStatusValueCode) || (null != dbItemStatusValueCode && null == itemStatusValueCode)) {
			return true;
		}
		return false;
	}
	
	public boolean isTransitionPermittedForUser(Item item, UserInfo userInfo, Errors errors) {
		//If we did not have a specific state for this item we do not want a system error to be thrown.
		//This is a valid error and we should provide proper message to the user.
		ItemState itemState = getCurrentItemState(item);
		if (itemState == null){
			//This indicates that the specific state is invalid for the item
			String itemName = (item == null) ? "<Unknown>":item.getItemName();
			Long itemStatusValueCode = SavedItemData.getCurrentItemStatusValueCode();
			String itemClass = item.getClassLookup() != null ? item.getClassLookup().getLkpValue():"<Unknown>";
			String itemSubClass = item.getSubclassLookup() != null ? " " + item.getSubclassLookup().getLkpValue() :"";
			String itemStatusLkpValue = item.getStatusLookup() != null ? " " + item.getStatusLookup().getLkpValue() : "<Unknown>";
			
			if (itemStatusValueCode != null){
				LksData itemStatusLks = SystemLookup.getLksData(sessionFactory.getCurrentSession(), itemStatusValueCode);
				String lksStatusValue = itemStatusLks != null ? itemStatusLks.getLkpValue() : "<Unknown>";
				Object[] errorValues = { itemName, lksStatusValue, itemStatusLkpValue };
				errors.rejectValue("itemStatusLookup", "ItemValidator.invalidTransition", errorValues, "Cannot transition to a different state");
			} else {
				Object[] errorValues = { itemName, itemClass, itemSubClass, itemStatusLkpValue };
				errors.rejectValue("itemStatusLookup", "ItemValidator.invalidTransition.newItem", errorValues, "Cannot transition to a different state");
			}
			//We need to throw an appropriate error
			return false;
		}
		boolean retval = isTransitionPermittedForUser(item, userInfo);
		if( retval == false && null != errors){
			String itemName = (item == null) ? "Null item":item.getItemName();
			String userName = (userInfo == null || userInfo.getUserName()==null) ? "Null userInfo":userInfo.getUserName();
			Object[] errorValues = { itemName, userName, item.getStatusLookup().getLkpValue() };
			errors.rejectValue("itemStatusLookup", "ItemValidator.transitionNotPermitted", errorValues, "Cannot transition to a different state");
		}
		return retval;
	}

	@Override
	public void validateMandatoryFields(Item item, Long newStatusLkpValueCode,
			Errors errors) throws DataAccessException, ClassNotFoundException {
		
		String key = getItemStateKey(item.getClassLookup().getLkpValueCode(),newStatusLkpValueCode);
		ItemState itemState = itemStateMap.get(key);
		
		if (itemState != null)
			itemState.validateMandatoryFields(item, errors, newStatusLkpValueCode);
	}

	@Override
	public void validateParentChildConstraint(Item item,
			Long newStatusLkpValueCode, Errors errors)
			throws DataAccessException, ClassNotFoundException {
		String key = getItemStateKey(item.getClassLookup().getLkpValueCode(),newStatusLkpValueCode);
		ItemState itemState = itemStateMap.get(key);
		
		if (itemState != null)
			itemState.validateParentChildConstraint(item, errors, newStatusLkpValueCode,"itemRequest.parentChildConstraint");
		
	}
	
	private String getItemStateKey(Long classLookupValue, Long statusLookupValue) {
		StringBuffer lookupValue = new StringBuffer();
		
		if (classLookupValue != null && statusLookupValue != null) {
			lookupValue.append( classLookupValue.toString() );
			lookupValue.append(":");
			lookupValue.append( statusLookupValue ); 
		}
		return lookupValue.toString();
	}

	public List<Long> getStatusList(ModelDetails model ) {//throws DataAccessException {

		List<Long> statusList = null;
		
		if (null != model) {
			Long uniqueId = model.getClassMountingFormFactorValue();
			statusList = statusLookupMap.get(uniqueId);
		}
			
		if (null == model || null == statusList || statusList.size() == 0) {
			Long uniqueId = 100L; // VM
			statusList = statusLookupMap.get(uniqueId);
		}
		
		return statusList;
	}
}
