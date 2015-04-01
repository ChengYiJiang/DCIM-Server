/**
 *
 */
package com.raritan.tdz.item.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.raritan.tdz.beanvalidation.home.BeanValidationHome;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.ItemServiceDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.home.ItemDomainAdaptor;
import com.raritan.tdz.item.home.SavedItemData;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.lookup.SystemLookup;


/**
 * Performs any common set of validation for all types of items.
 * @author prasanna
 *
 */
public class ItemValidatorCommon implements Validator {

	@Autowired
	private ItemRequest itemRequest;

	@Autowired
	private ItemDomainAdaptor itemDomainAdaptor;

	@Autowired
	private BeanValidationHome beanValidationHome;

	@Autowired
	protected ItemStateContext itemStateContext;


	private Logger log = Logger.getLogger(getClass());


	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.getSuperclass().equals(Item.class);
	}

	/* (non-Javadoc)
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Override
	public void validate(Object target, Errors errors) {
		@SuppressWarnings("unchecked")
		
		Map<String, Object> targetMap = (Map<String,Object>)target;
		if (targetMap != null) {
			if (targetMap.size() != 2)  throw new IllegalArgumentException("ItemValidatorCommon: Invalid arguments");

			Item item = (Item)targetMap.get(errors.getObjectName());
			if (item == null) {
				throw new IllegalArgumentException ("Item cannot be null");
			}
			
			UserInfo userInfo = (UserInfo) targetMap.get(UserInfo.class.getName());
			if (userInfo == null) { 
				throw new IllegalArgumentException ("UserInfo cannot be null"); 
			}
			
			if (itemDomainAdaptor.getValidationErrors() != null && itemDomainAdaptor.getValidationErrors().hasErrors())
				errors.addAllErrors(itemDomainAdaptor.getValidationErrors());
	
			Errors beanValidationErrors = beanValidationHome.validate(item); 
			if (beanValidationErrors != null)
				errors.addAllErrors(beanValidationErrors);
	
			//Validate based on state
			stateValidation(item, userInfo, errors);
			
			itemStateContext.isTransitionPermittedForUser(item, userInfo, errors);
	
			// Validate contract date, install date, etc
			validateDates(errors, item);
	
			// Validate Type and function
			validateTypeAndFunction(errors,item);
	
			//Validate editability based on the request
			validateRequestEditItem(item, userInfo, errors);
	
			//Validate editability based on requestStateChange
			validateRequestStateChange(item, userInfo, errors);
		} else {
			throw new IllegalArgumentException("ItemValidatorCommon: Invalid (null) arguments");
		}
	}

	protected void validateRequestEditItem(Object itemDomain, UserInfo userInfo, Errors errors) {
		Item item = (Item) itemDomain;
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_APPROVED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_ISSUED);
		requestStages.add(SystemLookup.RequestStage.WORK_ORDER_COMPLETE);

		List<Long> itemIds = new ArrayList<Long>();
		
		//If item is the result of a item move request, use the original item id
		itemIds.add(item.getItemToMoveId() == null ? item.getItemId() : item.getItemToMoveId());

		validateRequestStage(itemDomain, userInfo, errors, requestStages, itemIds, "ItemValidator.itemHasRequestCannotEdit");
	}

	protected void validateRequestStateChange(Object itemDomain, UserInfo userInfo, Errors errors) {
		Item item = (Item) itemDomain;
		List<Long> requestStages = new ArrayList<Long>();
		requestStages.add(SystemLookup.RequestStage.REQUEST_ISSUED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_UPDATED);
		requestStages.add(SystemLookup.RequestStage.REQUEST_REJECTED);

		List<Long> itemIds = new ArrayList<Long>();
		
		//If item is the result of a item move request, use the original item id
		itemIds.add(item.getItemToMoveId() == null ? item.getItemId() : item.getItemToMoveId());

		Item origItem = (item.getItemId() > 0) ? SavedItemData.getCurrentItem().getSavedItem() : null;
		if (origItem != null && null != origItem.getStatusLookup() && null != item.getStatusLookup() &&
				origItem.getStatusLookup().getLkpValueCode().longValue() != item.getStatusLookup().getLkpValueCode().longValue()) {
			validateRequestStage(itemDomain, userInfo, errors, requestStages, itemIds, "ItemValidator.itemHasRequestCannotEditStatus");
		}
	}

	protected void validateRequestStage(Object itemDomain, UserInfo userInfo, Errors errors, List<Long> requestStages, List<Long> itemIds, String errorCode) {
		Item item = (Item) itemDomain;
		Map<Long, List<Request>> requestMap;
		try {
			requestMap = itemRequest.getRequests(itemIds, requestStages, userInfo);
			List<Request> requests = null;
			if (null != requestMap) {
				requests = requestMap.get(item.getItemId());
			}
			if (null != requests && requests.size() > 0) {
				Object[] errorArgs = { };
				errors.rejectValue("cmbStatus", errorCode, errorArgs, "The item request stage do not allow item edit.");
			}
			} catch (BusinessValidationException e) {
				errors.rejectValue("cmbStatus", errorCode, null, "The item request stage do not allow item edit.");
				if (log.isDebugEnabled())
					e.printStackTrace();
			} catch (DataAccessException e) {
				errors.rejectValue("cmbStatus", errorCode, null, "The item request stage do not allow item edit.");
				if (log.isDebugEnabled())
					e.printStackTrace();
			}
	}

	private void validateDates(Errors errors, Item item) {
		ItemServiceDetails detail = item.getItemServiceDetails();

		if(detail == null) return;

		java.util.Date contractStart = detail.getContractBeginDate();
		java.util.Date contractExpire = detail.getContractExpireDate();
		java.util.Date purchaseDate = detail.getPurchaseDate();
		java.util.Date installDate = detail.getInstallDate();

		//Contract Start Date <= Contract Expire Date And Contract Start Date >= Purchase Date
		if ((contractStart != null && contractExpire != null) && (contractStart.equals(contractExpire) || contractStart.after(contractExpire))){
			errors.rejectValue("contractBeginDate", "ItemValidator.contractBeginDate1", null, "Contract Start Date must be less than Contract End Date");
		}

		if ((contractStart != null && purchaseDate != null) && contractStart.before(purchaseDate)){
			errors.rejectValue("contractBeginDate", "ItemValidator.contractBeginDate2", null, "Contract Start Date must be greater than or equal to Purchase Date");
		}

		//if ((contractStart != null && installDate != null) && installDate.after(contractStart)){	//removed this restrication to fix for CR 51063
		//	errors.rejectValue("installDate", "ItemValidator.contractBeginDate3", null, "Installation Date must be less than or equal to Contract Start Date");
		//}

		//Purchase Date <= Installation Date  And Contract Expired Date >= Purchase Date
		if ((purchaseDate != null && installDate != null) && purchaseDate.after(installDate)){
			errors.rejectValue("purchaseDate", "ItemValidator.purchaseDate1", null, "Purchase Date must be less than or equal to Installation Date");
		}

		if ((purchaseDate != null && contractExpire != null) && (purchaseDate.equals(contractExpire) || purchaseDate.after(contractExpire))){
			errors.rejectValue("purchaseDate", "ItemValidator.purchaseDate2", null, "Purchase Date must be less than Contract End Date");
		}

		//Installation Date <= Contract Start Date
		if ((installDate != null && contractExpire != null) && (installDate.equals(contractExpire) || installDate.after(contractExpire))){
			errors.rejectValue("installDate", "ItemValidator.installDate1", null, "Installation Date must be less than Contract End Date");
		}
	}

	protected void validateTypeAndFunction(Errors errors, Item item) {
		LksData classLksData = item.getClassLookup();

		if (item.getItemServiceDetails() != null && item.getItemServiceDetails().getPurposeLookup() != null){
			LkuData typeLkuData = item.getItemServiceDetails().getPurposeLookup();
			LksData typeLksData = typeLkuData.getLksData();
			// If the purposeLksData does not belong to the item class capture that as an error
			if ((typeLksData == null || classLksData == null) || !typeLksData.getLkpValueCode().equals(classLksData.getLkpValueCode())){
				String typeLkpValue = typeLksData != null ? typeLkuData.getLkuValue() : "<Unknown>";
				String classLkpValue = classLksData != null ? classLksData.getLkpValue() : "<Unknown>";
				Object[] errorArgs = { typeLkpValue,classLkpValue };
				errors.rejectValue("purposeLookup", "ItemValidator.incorrectType", errorArgs, "This type is not associated with this item class");
			}
		}

		if (item.getItemServiceDetails() != null && item.getItemServiceDetails().getFunctionLookup() != null){
			LkuData functionLkuData = item.getItemServiceDetails().getFunctionLookup();
			LksData functionLksData = functionLkuData.getLksData();
			// If the functionLksData does not belong to the item class capture that as an error
			if ((functionLksData == null || classLksData == null )|| !functionLksData.getLkpValueCode().equals(classLksData.getLkpValueCode())){
				String functionLkuValue = functionLkuData != null ? functionLkuData.getLkuValue() : "<Unknown>";
				String classLkpValue = classLksData != null ? classLksData.getLkpValue() : "<Unknown>";
				Object[] errorArgs = { functionLkuValue, classLkpValue };
				errors.rejectValue("purposeLookup", "ItemValidator.incorrectFunction", errorArgs, "This function is not associated with this item class");
			}
		}
	}

	protected void stateValidation(Object target, UserInfo sessionUser, Errors errors){
		//Validate based on state
		itemStateContext.validateAllButReqFields(target, sessionUser, errors);
	}

}
