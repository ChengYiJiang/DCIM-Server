/**
 * 
 */
package com.raritan.tdz.item.request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.BusinessValidationException.WarningEnum;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.itemState.ItemModifyRoleValidator;
import com.raritan.tdz.item.itemState.ItemStateContext;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.request.dao.RequestDAO;
import com.raritan.tdz.request.home.RequestLookup;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.RequestDTO;

import flex.messaging.log.Log;

/**
 * @author prasanna
 * This aspect will take care of validation for each of the methods
 * defined in ItemRequest interface
 */
@Aspect
public class ItemRequestValidationAspect {
	
	@Autowired
	private MessageSource messageSource;
	
	@Autowired
	private ItemRequestDAO itemRequestDAO;
	
	@Autowired
	private RequestDAO requestDAO;
	
	@Autowired
	private ItemStateContext itemStateContext;
	
	@Autowired
	private ItemModifyRoleValidator itemModifyRoleValidator;

	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private Validator validateParentRequest;
	
	private boolean disableValidate = false;
	
	private List<Long> skipItemIds = new ArrayList<Long>();
	
	private List<Long> skipRequestIds = new ArrayList<Long>();
	
	private final String reSubmit = "resubmitRequest";
	
	private final List<String> validationWarningCodes = Arrays.asList("ItemMoveValidator.parentHasPendingRequest");
	
	private final List<Long> canConvertToVMForClassSubClass = Collections.unmodifiableList(new ArrayList<Long>() {{
		add(SystemLookup.ModelUniqueValue.DeviceStandardRackable);
		add(SystemLookup.ModelUniqueValue.DeviceStandardNonRackable);
		add(SystemLookup.ModelUniqueValue.DeviceStandardFreeStanding);
		add(SystemLookup.ModelUniqueValue.DeviceBladeServer);
	}});


	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public ItemRequestDAO getItemRequestDAO() {
		return itemRequestDAO;
	}

	public void setItemRequestDAO(ItemRequestDAO itemRequestDAO) {
		this.itemRequestDAO = itemRequestDAO;
	}
	
	public ItemStateContext getItemStateContext() {
		return itemStateContext;
	}

	public void setItemStateContext(ItemStateContext itemStateContext) {
		this.itemStateContext = itemStateContext;
	}

	public boolean isDisableValidate() {
		return disableValidate;
	}

	public void setDisableValidate(boolean disableValidate) {
		this.disableValidate = disableValidate;
	}

	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.get*(..))")
	public void isGetMethods() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.*(..))")
	public void allMethods() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.clearErrors(..))")
	public void isClearErrorMethod() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.resubmitRequest(..))")
	public void isResubmitRequest() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.convertToVMRequest(..))")
	public void isConvertToVMRequest() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.itemRequestExistInStages(..))")
	public void isItemRequestExistInStages() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.setItemStatus(..))")
	public void isSetItemStatus() {}
	
	@Pointcut("execution(public * com.raritan.tdz.item.request.ItemRequest.moveItemRequest(..))")
	public void isMoveItemRequest() {}

	@Around("allMethods() && !isGetMethods() && !isClearErrorMethod() && !isResubmitRequest() && !isConvertToVMRequest() && !isItemRequestExistInStages() && !isSetItemStatus() && !isMoveItemRequest()")
	public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
		
		if (isDisableValidate()) {
			return joinPoint.proceed();
		}
		
		List<Long> itemIds = getItemIds(joinPoint);
		
		if (!isDisableValidate()){
			validateHelper(joinPoint);
			
			//Remove all the item ids(if any) that should not be processed further
			itemIds.removeAll(skipItemIds);
		}
		
		Object retVal = joinPoint.proceed();
		
		List<Long> requestIds = new ArrayList<Long>();
		if (retVal != null){
			@SuppressWarnings("unchecked")
			Map<Long,Long> itemIdToRequestIdMap = (Map<Long, Long>) retVal;
			
			for (Long itemId: itemIds){
				requestIds.add(itemIdToRequestIdMap.get(itemId));
			}
		}
		
		itemIds.addAll(skipItemIds); //added back for proper error handling in screen
		
		throwBusinessValidationException(joinPoint, requestIds, false);
		
		return retVal;
	}


	
	@Around("isResubmitRequest()")
	public Object validateResubmit(ProceedingJoinPoint joinPoint) throws Throwable{
		
		if (isDisableValidate()) {
			return joinPoint.proceed();
		}

		List<Long> requestIds = getRequestIds(joinPoint);
		
		if (!isDisableValidate()){
			//Check to see if the user has permission to resubmit this request
			validateUser(joinPoint);
			
			
			//Check to see if all the required fields are in place to resubmit a request
			//We need to consider the new state that the item will go into when request is completed.
			validateMandatoryFields(joinPoint);
			
			//Check to see if the request is one of the following
			//Install Item
			//Convert Item to VM
			//Take Item Off-Site, Bring Item On-Site
			//Decommmission Item (Storage), Decommission Item (Archive),
			//Power-off Item, Power-on Item
			validateRequestType(joinPoint);
			
			//Check to see if the request issued are
			//Request Issued, Request Rejected or Request Update stage.
			validateRequestStage(joinPoint);
			
			//Remove all the item ids(if any) that should not be processed further
			requestIds.removeAll(skipRequestIds);
		}
		
		Object retVal = joinPoint.proceed();
		
		throwBusinessValidationException(joinPoint, requestIds, true);
		
		return retVal;
	}
	

	@Around("isConvertToVMRequest()")
	public Object validateConvertToVM(ProceedingJoinPoint joinPoint) throws Throwable{
		
		if (isDisableValidate()) {
			return joinPoint.proceed();
		}
		
		List<Long> itemIds = getItemIds(joinPoint);
		
		if (!isDisableValidate()){
			//Call the helper to validate everything else
			validateHelper(joinPoint);
			
			//Validate if any of the items cannot be turned into a VM
			validateCanConvertToVM(joinPoint);
			
			//Remove all the item ids(if any) that should not be processed further
			itemIds.removeAll(skipItemIds);
		}
		
		Object retVal = joinPoint.proceed();
		
		List<Long> requestIds = new ArrayList<Long>();
		if (retVal != null){
			@SuppressWarnings("unchecked")
			Map<Long,Long> itemIdToRequestIdMap = (Map<Long, Long>) retVal;
			
			for (Long itemId: itemIds){
				requestIds.add(itemIdToRequestIdMap.get(itemId));
			}
		}
		
		throwBusinessValidationException(joinPoint, requestIds, false);
		
		return retVal;
	}
	
	private void validateCanConvertToVM(ProceedingJoinPoint joinPoint) {
		List<Long> itemIds = getItemIds(joinPoint);
		
		for (Long itemId:itemIds){
			try {
				Item item = itemDAO.read(itemId);
				ItemRequest itemRequest = (ItemRequest) joinPoint.getTarget();
				String itemName = item.getItemName();
				String classLkpValue = item.getClassLookup() != null ? item.getClassLookup().getLkpValue() : "";
				String subclassLkpValue = item.getSubclassLookup() != null ? " " + item.getSubclassLookup().getLkpValue() : "";
				Long itemStatusLkpValueCode = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValueCode() : -1;
				String itemStatusLkpValue = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValue() : "<Unkown>";
				if (!canConvertToVMForClassSubClass.contains(item.getClassMountingFormFactorValue())){
					//Error, cannot convert class/subclass to a VM
					Errors errors = itemRequest.getErrors();
					Object[] errorArgs = {itemName, classLkpValue,subclassLkpValue};
					errors.reject("itemRequest.cannotConvertToVM", errorArgs, "Request unsucessful. Cannot convert to VM");
					
					//Remove this item from list
					skipItemIds.add(itemId);
				}
			} catch (HibernateException e){
				//This is the case when item is not found.
				//We dont need to really process this as this is taken
				//care while actually trying to process the requests
				//in DAO layer.
			}
		}
	}

	//---------------- private methods -------------------------
	
	private void validateRequestType(ProceedingJoinPoint joinPoint) {
		List<Long> itemIds;
		
		Map<Long,Long> itemIdToRequestIdMap = new HashMap<Long, Long>();
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			List<Long> requestIds = getRequestIds(joinPoint);
			itemIds = getItemIdsFromRequestIds(requestIds);
			itemIdToRequestIdMap = getItemIdToRequestIdMap(requestIds);
		}
		else{
			itemIds = getItemIds(joinPoint);
		}
		
		ItemRequest itemRequest = (ItemRequest)joinPoint.getTarget();
		
		//Get all the collected errors from the itemRequest object
		Errors errors = itemRequest.getErrors();
		for (Long itemId: itemIds){
			try {
				Item item = itemDAO.read(itemId);
				//The request type is invalid, error!
				String itemName = item.getItemName();

				
				Request request = itemRequestDAO.getLatestRequest(itemId);
				
				//If the request is null then there are no requests
				if (request != null){
					if (!ItemRequest.ItemRequestType.itemRequestTypeList.contains(request.getRequestType())){
						if (request.getRequestType().equals("Item Move")){
							Object[] errorArgs = {itemName};
							errors.reject("itemRequest.invalidRequestTypeItemMove", errorArgs, "Request unsucessful. Request type invalid");
						}
						else{
							Object[] errorArgs = {itemName, request.getRequestType()};
							errors.reject("itemRequest.invalidRequestType", errorArgs, "Request unsucessful. Request type invalid");
						}
						
						skipItemIds.add(itemId);
						//If it is resubmit we need to remove the corresponding requestIds
						if (joinPoint.getSignature().getName().contains(reSubmit)){
							skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
						}
					}									
				} else {
					
					//Error
					Object[] errorArgs = {itemName};
					errors.reject("itemRequest.noRequestsForItem", errorArgs, "Request unsucessful. No requests on this item");
					
					skipItemIds.add(itemId);
					//If it is resubmit we need to remove the corresponding requestIds
					if (joinPoint.getSignature().getName().contains(reSubmit)){
						skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
					}
				}
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				//If we have data access exception, this could be hibernate error
				//We need to let the user know that this is a system error.
				
				errors.reject("itemRequest.noReqests.single", null, "Request unsucessful. No requests on this item");
				skipItemIds.add(itemId);
				//If it is resubmit we need to remove the corresponding requestIds
				if (joinPoint.getSignature().getName().contains(reSubmit)){
					skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
				}
			}
		}
		
	}
	
	
	private void validateRequestStage(ProceedingJoinPoint joinPoint) {
		List<Long> itemIds;		
		Map<Long,Long> itemIdToRequestIdMap = new HashMap<Long, Long>();
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			List<Long> requestIds = getRequestIds(joinPoint);
			itemIds = getItemIdsFromRequestIds(requestIds);
			itemIdToRequestIdMap = getItemIdToRequestIdMap(requestIds);
		}else{
			itemIds = getItemIds(joinPoint);
		}
		
		ItemRequest itemRequest = (ItemRequest)joinPoint.getTarget();
		
		List<Long> validStages = Collections.unmodifiableList(new ArrayList<Long>() {{
			add(SystemLookup.RequestStage.REQUEST_ISSUED);
			add(SystemLookup.RequestStage.REQUEST_REJECTED);
			add(SystemLookup.RequestStage.REQUEST_UPDATED);
		}});
		//Get all the collected errors from the itemRequest object
		Errors errors = itemRequest.getErrors();
		for (Long itemId: itemIds){
			try {
				Item item = itemDAO.read(itemId);
				//The request type is invalid, error!
				String itemName = item.getItemName();
		
				LksData requestStage = itemRequest.getLatestRequestStage(itemId);
				
				//If the request is null then there are no requests
				if (requestStage != null){
					if (!validStages.contains(requestStage.getLkpValueCode())){
							Object[] errorArgs = {itemName, requestStage.getLkpValue()};
							errors.reject("itemRequest.invalidStage", errorArgs, "Request unsucessful. Request stage is invalid");
							skipItemIds.add(itemId);
							//If it is resubmit we need to remove the corresponding requestIds
							if (joinPoint.getSignature().getName().contains(reSubmit)){
								skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
							}
					}
					
				} else {
					
					//Error
					Object[] errorArgs = {itemName};
					errors.reject("itemRequest.noRequestsForItem", errorArgs, "Request unsucessful. No requests on this item");
					
					skipItemIds.add(itemId);
					//If it is resubmit we need to remove the corresponding requestIds
					if (joinPoint.getSignature().getName().contains(reSubmit)){
						skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
					}
				}
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				//If we have data access exception, this could be hibernate error
				//We need to let the user know that this is a system error.
				errors.reject("itemRequest.noReqests.single", null, "Request unsucessful. No requests on this item");
				skipItemIds.add(itemId);
				//If it is resubmit we need to remove the corresponding requestIds
				if (joinPoint.getSignature().getName().contains(reSubmit)){
					skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
				}
			}
		}
		
	}
	
	
	private void validateHelper(ProceedingJoinPoint joinPoint)
			throws BusinessValidationException {
		//Check to see if the user has permission to submit this request
		validateUser(joinPoint);
		
		//Check to see if there are pending requests 
		validatePendingRequests(joinPoint);
		
		//Check to see the current item status allows it to be transitioned
		validateStates(joinPoint);
		
		//Check to see if all the required fields are in place to submit a request
		//We need to consider the new state that the item will go into when request is completed.
		validateMandatoryFields(joinPoint);

		// check if parent have move request
		validateParentMoveRequest(joinPoint);
		
		//Parent-Child validation
		//CR 48808. We do not need to perform parent-child constraint check on any request.
		//Commenting this code so that in future if we need it, we can resurrect this :-)
		//validateParentChildConstraints(joinPoint);
	}
	
	private void validatePendingRequests(ProceedingJoinPoint joinPoint) {
		List<Long> itemIds = getItemIds(joinPoint);
		ItemRequest itemRequest = (ItemRequest)joinPoint.getTarget();
		
		Errors errors = itemRequest.getErrors();
		Map<Long, Boolean> pendingRequests = new HashMap<Long, Boolean>();
		Long requestTypeLkpValueCode = getRequestTypeValueCode(joinPoint);
		
		try {
			pendingRequests = itemRequestDAO.isPendingRequests(itemIds, requestTypeLkpValueCode, errors);
		} catch (DataAccessException e) {
			if (Log.isDebug())
				e.printStackTrace();
		}
		
		for (Long itemId: itemIds){
			Boolean isPendingRequest = pendingRequests.get(itemId);
			
			if (isPendingRequest != null && isPendingRequest == true){
				skipItemIds.add(itemId);
			}
		}
	}

	private void throwBusinessValidationException(JoinPoint joinPoint, List<Long> successRequestIds, boolean isResubmit) throws BusinessValidationException{
		//Get the item request object from the joinPoint
		ItemRequest itemRequest = (ItemRequest)joinPoint.getTarget();
		
		//Get all the collected errors from the itemRequest object
		Errors errors = itemRequest.getErrors();
		
		//Create a business validation exception out of the errors
		BusinessValidationException e =  new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.FAILURE.value(), this.getClass()));
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				if (validationWarningCodes != null && validationWarningCodes.contains(error.getCode())){
					e.addValidationWarning(msg);
					e.addValidationWarning(error.getCode(), msg, WarningEnum.WARNING_YES_NO);
				} else {
					e.addValidationError(msg);
					e.addValidationError(error.getCode(), msg );
				}
			}
			
			//Also include success messages
			if (successRequestIds != null && successRequestIds.size() > 0){
				
				String successCode = "itemRequest.success.submit";
				if (isResubmit)
					successCode = "itemRequest.success.resubmit";
				
				List<RequestDTO> requestDTOs = itemRequestDAO.getRequestDTOForRequests(successRequestIds);
				//We have to piggyback the success message as errors as well as this is a mixed mode.
				//TODO: We can defnitely improve this next release to see if we can use a different mechanism
				//      to communicate success messages! For now this works!
				for (RequestDTO requestDTO:requestDTOs){
					Object[] successArgs = {requestDTO.getItemName(),requestDTO.getRequestNo()};
					String msg = messageSource.getMessage(successCode, successArgs, Locale.getDefault());
					e.addValidationError(msg);
					e.addValidationError(successCode,msg);
				}
				e.setRequestList(requestDTOs);
			}
			
		}

		if (skipItemIds.size() > 0){
			skipItemIds.clear();
		}
		
		if (skipRequestIds.size() > 0){
			skipRequestIds.clear();
		}
		
		//If we have any validation errors, then throw that.
		if (e.getValidationErrorsList().size() > 0){
			itemRequest.clearErrors();
			throw e;
		}
	}
	
	
	private UserInfo getUserInfo(JoinPoint joinPoint){
		UserInfo userInfo = null;
		Object[] args = joinPoint.getArgs();
		if (args.length > 1 && args[1] != null && args[1] instanceof UserInfo){
			userInfo = (UserInfo) args[1];
		}
		
		return userInfo;
	}
	
	private List<Long> getItemIds(JoinPoint joinPoint){
		List<Long> itemIds = new ArrayList<Long>();
		
		Object[] args = joinPoint.getArgs();
		if (args.length > 0 && args[0] != null && args[0] instanceof List){
			itemIds = (List<Long>) args[0];
		}
		
		if (args.length > 0 && args[0] != null && args[0] instanceof Map){
			Map<Long,Long> map = (HashMap<Long,Long>) args[0];
			itemIds = new ArrayList<Long>(map.keySet());
		}
		
		//find free standing item for a cabinet/container or any item
		//override cabinet id by free standing item id
		itemIds = getFreeStandingItemIds(itemIds);

		return itemIds;
	}
	
	private List<Long> getRequestIds(JoinPoint joinPoint){
		List<Long> requestIds = null;
		Object[] args = joinPoint.getArgs();
		if (args.length > 0 && args[0] != null && args[0] instanceof List){
			requestIds = (List<Long>) args[0];
		}
		
		return requestIds;
	}
	
	private List<Long> getItemIdsFromRequestIds(List<Long> requestIds){
		
		if (null == requestIds || requestIds.size() == 0) return new ArrayList<Long>();
		
		List<Long> itemIds = requestDAO.getItemIdsForRequests(requestIds);
		
		if (null == itemIds) itemIds = new ArrayList<Long>();
		
		return itemIds;
	}
	
	private Map<Long,Long> getItemIdToRequestIdMap(List<Long> requestIds){
		Map<Long,Long> itemIds = new HashMap<Long,Long>();
		
		for (Long requestId:requestIds){
			Request request = itemRequestDAO.loadRequest(requestId);
			itemIds.put(request.getItemId(),requestId);
		}
		
		return itemIds;
	}
	
	private void validateUser(JoinPoint joinPoint) throws BusinessValidationException{
		UserInfo userInfo = getUserInfo(joinPoint);
		List<Long> itemIds;
		
		Map<Long,Long> itemIdToRequestIdMap = new HashMap<Long, Long>();
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			List<Long> requestIds = getRequestIds(joinPoint);
			itemIds = getItemIdsFromRequestIds(requestIds);
			itemIdToRequestIdMap = getItemIdToRequestIdMap(requestIds);
		}
		else{
			itemIds = getItemIds(joinPoint);
		}
		
		ItemRequest itemRequest = (ItemRequest) joinPoint.getTarget();
		
		for (Long itemId: itemIds){
			try {
				Item item = itemDAO.read(itemId);
				String itemName = item.getItemName();
				if (!isPermitted(userInfo, item)){
					Errors errors = itemRequest.getErrors();
					Object[] errorArgs = {itemName};
					errors.reject("itemRequest.isViewer", errorArgs, "Cannot submit request as you do not have permissions");
					skipItemIds.add(itemId);
					skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
				}
			} catch (HibernateException e){
				//This is the case when item is not found.
				//We dont need to really process this as this is taken
				//care while actually trying to process the requests
				//in DAO layer.
			}
		}
	}
	
	private boolean isPermitted(UserInfo userInfo, Item item){
		//Actually we are not doing anything with Transition here. All we are 
		//doing is to check if the current user has permission to perform this operation
		//on the given item.
		//TODO: May be we should consider naming the method properly.
		return (itemModifyRoleValidator.canTransition(item, userInfo));
	}
	
	private void validateStates(JoinPoint joinPoint){
		
		List<Long> itemIds;
		Map<Long,Long> itemIdToRequestIdMap = new HashMap<Long, Long>();
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			List<Long> requestIds = getRequestIds(joinPoint);
			itemIds = getItemIdsFromRequestIds(requestIds);
			itemIdToRequestIdMap = getItemIdToRequestIdMap(requestIds);
		}
		else{
			itemIds = getItemIds(joinPoint);
		}
		
		UserInfo userInfo = getUserInfo(joinPoint);
	
		for (Long itemId:itemIds){
			try {
				Item item = itemDAO.read(itemId);
				List<Long> allowableStates = getAllowableStatusForRequest(joinPoint);
				//Get the status lookup value for the request via annotation defined on each method.
				Long statusLkpValueCode = getStatusLkpValueCode(joinPoint);
				
				LksData newStatusLks = itemRequestDAO.loadLks(statusLkpValueCode);
				
				if (item.getStatusLookup() != null && !allowableStates.contains(item.getStatusLookup().getLkpValueCode())){
					//Fill the errors object
					String itemName = item.getItemName();
					String classLkpValue = item.getClassLookup() != null ? item.getClassLookup().getLkpValue() : "";
					String subclassLkpValue = item.getSubclassLookup() != null ? " " + item.getSubclassLookup().getLkpValue() : "";
					String newStatusLkpValue = newStatusLks != null ? newStatusLks.getLkpValue() : "";
					String statusLkpValue = item.getStatusLookup() != null ? item.getStatusLookup().getLkpValue() : "";
					
					ItemRequest itemRequest = (ItemRequest) joinPoint.getTarget();
					if (itemRequest != null && newStatusLks != null){
						Errors errors = itemRequest.getErrors();
						Object[] errorArgs = {itemName, classLkpValue,subclassLkpValue, getAllowableStatusForRequestLkpValues(joinPoint), 
												statusLkpValue};
						errors.reject("itemRequest.cannotTransition", errorArgs, "Request unsucessful. Cannot transition to new state");
						
						//Let us not try to process the request on this!
						skipItemIds.add(itemId);
						//If it is resubmit we need to remove the corresponding requestIds
						if (joinPoint.getSignature().getName().contains(reSubmit)){
							skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
						}
					}
				}
			} catch (HibernateException e){
				//This is the case when item is not found.
				//We dont need to really process this as this is taken
				//care while actually trying to process the requests
				//in DAO layer.
			}
		}
		
	
	}
	
	
	private Long getRequestTypeValueCode(JoinPoint joinPoint) {
		Long requestTypeLkpValueCode = new Long(-1);
		
		List<Class<?>> classList = new ArrayList<Class<?>>();
		
		for (Object obj : joinPoint.getArgs()){
			String interfaces = Arrays.asList(obj.getClass().getGenericInterfaces()).toString();
			
			
			if (interfaces.contains(List.class.getName())){
				classList.add(List.class);
			}
			else if (interfaces.contains(Map.class.getName())){
				classList.add(Map.class);
			} else {
				classList.add(obj.getClass());
			}
		}
		
		//
		try {
			Method method = ItemRequest.class.getMethod(joinPoint.getSignature().getName(), classList.toArray(new Class<?>[classList.size()]));
			Annotation[] annotations =  method.getAnnotations();
			for (Annotation annotation: annotations){
				if (annotation instanceof RequestedOperation){
					requestTypeLkpValueCode = ((RequestedOperation) annotation).requestLkpValueCode();
				}
			}
		} catch (SecurityException e) {
			if (Log.isDebug())
				e.printStackTrace();
		} catch (NoSuchMethodException e) {
			if (Log.isDebug())
				e.printStackTrace();
		}
		
		return requestTypeLkpValueCode;

	}
	
	private Long getStatusLkpValueCode(JoinPoint joinPoint){
		Long statusLkpValueCode = new Long(-1);
		
		List<Class<?>> classList = new ArrayList<Class<?>>();
		
		for (Object obj : joinPoint.getArgs()){
			String interfaces = Arrays.asList(obj.getClass().getGenericInterfaces()).toString();
			
			
			if (interfaces.contains(List.class.getName())){
				classList.add(List.class);
			}
			else if (interfaces.contains(Map.class.getName())){
				classList.add(Map.class);
			} else {
				classList.add(obj.getClass());
			}
		}
		
		//
		try {
			Method method = ItemRequest.class.getMethod(joinPoint.getSignature().getName(), classList.toArray(new Class<?>[classList.size()]));
			Annotation[] annotations =  method.getAnnotations();
			for (Annotation annotation: annotations){
				if (annotation instanceof RequestedItemStatus){
					statusLkpValueCode = ((RequestedItemStatus) annotation).statusLkpValueCode();
				}
			}
		} catch (SecurityException e) {
			if (Log.isDebug())
				e.printStackTrace();
		} catch (NoSuchMethodException e) {
			if (Log.isDebug())
				e.printStackTrace();
		}
		
		return statusLkpValueCode;
	}
	
	//This provides a concatenated list of lkpValues that can be displayed to the user
	private String getAllowableStatusForRequestLkpValues(JoinPoint joinPoint){
		StringBuffer allowableStatusLkpValues = new StringBuffer();
		List<Long> allowableStatus = getAllowableStatusForRequest(joinPoint);
		for (Long allowableState:allowableStatus){
			LksData lksData = itemRequestDAO.loadLks(allowableState);
			if (lksData != null){
				allowableStatusLkpValues.append(lksData.getLkpValue());
				allowableStatusLkpValues.append(",");
			}
		}
				
		return allowableStatusLkpValues.toString().substring(0,allowableStatusLkpValues.toString().lastIndexOf(","));
	}
	
	private List<Long> getAllowableStatusForRequest(JoinPoint joinPoint){
		List<Long> statusLkpValueCodeList = new ArrayList<Long>();
		
		List<Class<?>> classList = new ArrayList<Class<?>>();
		
		for (Object obj : joinPoint.getArgs()){
			String interfaces = Arrays.asList(obj.getClass().getGenericInterfaces()).toString();
			
			
			if (interfaces.contains(List.class.getName())){
				classList.add(List.class);
			}
			else if (interfaces.contains(Map.class.getName())){
				classList.add(Map.class);
			} else {
				classList.add(obj.getClass());
			}
		}
		
		//
		try {
			Method method = ItemRequest.class.getMethod(joinPoint.getSignature().getName(), classList.toArray(new Class<?>[classList.size()]));
			Annotation[] annotations =  method.getAnnotations();
			for (Annotation annotation: annotations){
				if (annotation instanceof AllowableItemStatusForRequest){
					long[] statusLkpValueCodes = ((AllowableItemStatusForRequest) annotation).statusLkpValueCodes();
					for (long statusLkpValueCode : statusLkpValueCodes){
						statusLkpValueCodeList.add(statusLkpValueCode);
					}
				}
			}
		} catch (SecurityException e) {
			if (Log.isDebug())
				e.printStackTrace();
		} catch (NoSuchMethodException e) {
			if (Log.isDebug())
				e.printStackTrace();
		}
		
		return statusLkpValueCodeList;
	}
	
	private void validateMandatoryFields(JoinPoint joinPoint){
		List<Long> itemIds;
		Map<Long,Long> itemIdToRequestIdMap = new HashMap<Long, Long>();
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			List<Long> requestIds = getRequestIds(joinPoint);
			itemIds = getItemIdsFromRequestIds(requestIds);
			itemIdToRequestIdMap = getItemIdToRequestIdMap(requestIds);
		}
		else{
			itemIds = getItemIds(joinPoint);
		}
		
		ItemRequest itemRequest = (ItemRequest) joinPoint.getTarget();
		for (Long itemId: itemIds){
			try {
				Map<String, String> errorMap = new HashMap<String, String>();
				Errors errors = new MapBindingResult(errorMap, ItemRequest.class.getName());
				Item item = itemDAO.loadItem(itemId); // itemDAO.read(itemId);
				itemStateContext.validateMandatoryFields(item, getStatusLkpValueCode(joinPoint), errors);
				if (errors.hasErrors()){
					skipItemIds.add(itemId);
					//If it is resubmit we need to remove the corresponding requestIds
					if (joinPoint.getSignature().getName().contains(reSubmit)){
						skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
					}
					itemRequest.getErrors().addAllErrors(errors);
				}
			} catch (HibernateException e){
				//This is the case when item is not found.
				//We dont need to really process this as this is taken
				//care while actually trying to process the requests
				//in DAO layer.
				e.printStackTrace();
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				if (Log.isDebug())
					e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				if (Log.isDebug())
					e.printStackTrace();
			}
		}
	}
	
	private void validateParentMoveRequest(JoinPoint joinPoint) {
		List<Long> itemIds;
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			// no information on resubmit
			return;
		}else{
			itemIds = getItemIds(joinPoint);
		}
		
		ItemRequest itemRequest = (ItemRequest) joinPoint.getTarget();
		Long requestTypeLkpValueCode = getRequestTypeValueCode(joinPoint);
		if (requestTypeLkpValueCode.equals(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF) || 
				requestTypeLkpValueCode.equals(SystemLookup.RequestTypeLkp.ITEM_POWER_ON)) {
			return;
		}
		for (Long itemId: itemIds){
			Map<String, String> errorMap = new HashMap<String, String>();
			Errors errors = new MapBindingResult(errorMap, ItemRequest.class.getName());
			Map<String, Object> targetMap = new HashMap<String, Object>();
			targetMap.put(errors.getObjectName(), itemDAO.loadItem(itemId));
			targetMap.put(RequestLookup.RquestIssueValidate, true);
			
			validateParentRequest.validate(targetMap, errors);

			if (errors.hasErrors()) {
				itemRequest.getErrors().addAllErrors(errors);
			}
			
		}
	}
	
	private void validateParentChildConstraints(JoinPoint joinPoint){
		List<Long> itemIds;
		Map<Long,Long> itemIdToRequestIdMap = new HashMap<Long, Long>();
		
		if (joinPoint.getSignature().getName().contains(reSubmit)){
			List<Long> requestIds = getRequestIds(joinPoint);
			itemIds = getItemIdsFromRequestIds(requestIds);
			itemIdToRequestIdMap = getItemIdToRequestIdMap(requestIds);
		}else{
			itemIds = getItemIds(joinPoint);
		}
		
		ItemRequest itemRequest = (ItemRequest) joinPoint.getTarget();
		for (Long itemId: itemIds){
			try {
				Map<String, String> errorMap = new HashMap<String, String>();
				Errors errors = new MapBindingResult(errorMap, ItemRequest.class.getName());
				Item item = itemDAO.read(itemId);
				itemStateContext.validateParentChildConstraint(item, getStatusLkpValueCode(joinPoint), errors);
				if (errors.hasErrors()){
					skipItemIds.add(itemId);
					itemRequest.getErrors().addAllErrors(errors);
					//If it is resubmit we need to remove the corresponding requestIds
					if (joinPoint.getSignature().getName().contains(reSubmit)){
						skipRequestIds.add(itemIdToRequestIdMap.get(itemId));
					}
				}
			} catch (HibernateException e){
				//This is the case when item is not found.
				//We dont need to really process this as this is taken
				//care while actually trying to process the requests
				//in DAO layer.
				e.printStackTrace();
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				if (Log.isDebug())
					e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				if (Log.isDebug())
					e.printStackTrace();
			}
		}
	}

	public List<Long> getFreeStandingItemIds(List<Long> itemIds){
		//find free standing item for a cabinet/container or any item
		//override cabinet id by free standing item id
		List<Long> recList = new ArrayList<Long>();
		
		for(Long id:itemIds){
			Long fsItemId = itemDAO.getFreeStandingItemIdForItem(id);
			
			if(fsItemId != null && fsItemId.equals(id) == false){
				//check to see if item is in list first
				if(itemIds.contains(fsItemId)){
					continue;
				}
				
				recList.add(fsItemId);
			}
			else{
				recList.add(id);
			}
		}
		
		itemIds.clear();
		itemIds.addAll(recList);
		
		return itemIds;
	}	
}
