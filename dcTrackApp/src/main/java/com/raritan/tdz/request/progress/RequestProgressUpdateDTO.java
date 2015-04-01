package com.raritan.tdz.request.progress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.home.RequestHelper;
import com.raritan.tdz.util.BusinessExceptionHelper;

/**
 * 
 * @author bunty
 *
 */
public class RequestProgressUpdateDTO implements RequestProgressUpdate {

	private static Logger log = Logger.getLogger("RequestProgress");
	
	private SessionToObjectFactory<RequestProgressDTO> requestProgressDtoFactory;
	private SessionToObjectFactory<ReadWriteLock> requestProgressRWLockFactory;
	private SessionToObjectFactory<AtomicLong> requestProgressCountFactory;

	private BusinessExceptionHelper businessExceptionHelper;
	
	@Autowired(required=true)
	private RequestHelper requestHelper;

	
	public RequestProgressUpdateDTO(
			SessionToObjectFactory<RequestProgressDTO> requestProgressDtoFactory,
			SessionToObjectFactory<ReadWriteLock> requestProgressRWLockFactory,
			SessionToObjectFactory<AtomicLong> requestProgressCountFactory,
			BusinessExceptionHelper businessExceptionHelper) {
		super();
		this.requestProgressDtoFactory = requestProgressDtoFactory;
		this.requestProgressRWLockFactory = requestProgressRWLockFactory;
		this.requestProgressCountFactory = requestProgressCountFactory;
		this.businessExceptionHelper = businessExceptionHelper;

	}

	@Override
	public void startProgress(List<Request> requests, UserInfo userInfo, Errors errors) throws InstantiationException, IllegalAccessException {
		
		ReadWriteLock rwLock = null;
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			if (null == rwLock) rwLock = requestProgressRWLockFactory.create(userInfo);
		
			rwLock.writeLock().lock();
			// requestProgressDtoFactory.clear(userInfo);
			AtomicLong requestProgressCount = requestProgressCountFactory.get(userInfo);
			if (null == requestProgressCount) requestProgressCount =  requestProgressCountFactory.create(userInfo);
			requestProgressCount.incrementAndGet();


			RequestProgressDTO dto = requestProgressDtoFactory.create(userInfo);
			
			Errors dtoErrors = getErrorObject(errors);
			dtoErrors.addAllErrors(errors);
			dto.setErrors(dtoErrors);
			dto.setProcessingRequestNumber(0L);
			dto.setProgressState(RequestProgressLookup.state.REQUEST_PROGRESS_START);
			dto.setRequestDescription(null);
			dto.setRequestStage(RequestProgressLookup.requestStageProgress.REQUEST_PROGRESS_START);
			dto.setTotalNumOfRequest((long) requests.size());
			
		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("startProgress: DTO info: " + getDto(userInfo));
		}
	}

	@Override
	public void endProgress(UserInfo userInfo, Errors refErrors) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return;

			/*Errors reqErrors = dto.getErrors();
			if (null != reqErrors && reqErrors.hasErrors() && dto.getProgressState().equals(RequestProgressLookup.state.REQUEST_PROGRESS_RUNNING)) {
				Errors errors = getErrorObject(refErrors);
				errors.rejectValue("request", "itemRequest.processingComplete", "The status of your request can be monitored in Classic View from the Change Control > Requests screen.");
				dto.setErrors(errors);
			}*/
			dto.setProgressState(RequestProgressLookup.state.REQUEST_PROGRESS_FINISH);
			dto.setRequestStage(RequestProgressLookup.requestStageProgress.REQUEST_PROGRESS_END);

			AtomicLong requestProgressCount = requestProgressCountFactory.get(userInfo);
			requestProgressCount.decrementAndGet();
			
		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("endProgress: DTO info: " + getDto(userInfo));
		}

	}

	@Override
	public void cleanProgress(UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
		
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.clear(userInfo);
			dto.setErrors(null);
			dto.setProcessingRequestNumber(null);
			dto.setProgressState(null);
			dto.setRequestDescription(null);
			dto.setRequestStage(null);
			dto.setTotalNumOfRequest(null);
			// dto = null;
			
		}
		finally {
			
			if (null != rwLock) {
				rwLock.writeLock().unlock();
				requestProgressRWLockFactory.clear(userInfo);
				rwLock = null;
				
				if (log.isDebugEnabled()) log.debug("cleanProgress: DTO info: " + getDto(userInfo));
			}
		}

	}

	@Override
	public void nextRequestStart(Request request, Errors refErrors, UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return;

			/*Errors reqErrors = dto.getErrors();
			Errors errors = getErrorObject(refErrors);
			if (null != reqErrors && reqErrors.hasErrors()) {
				errors.rejectValue("request", "Request.addLineSeperator", "");
			}
			// Add the request description before the start of a new request process 
			{
				Object[] errorArgs = { request.getRequestNo(), request.getDescription() };
				errors.rejectValue("request", "Request.nextRequestStart", errorArgs, "Request: " + request.getRequestNo() + ", " + request.getDescription());
				dto.setErrors(errors);
			}*/

			if (dto.getProcessingRequestNumber() != dto.getTotalNumOfRequest()) {
				dto.setProcessingRequestNumber(dto.getProcessingRequestNumber() + 1);
			}
			dto.setProgressState(RequestProgressLookup.state.REQUEST_PROGRESS_RUNNING);
			String itemName = requestHelper.getItemName(request.getItemId());
			// dto.setRequestDescription("Request number:" + request.getRequestNo() + ", " + itemName + ", " + request.getDescription());
			dto.setRequestDescription(itemName + ", " + "Request: " + request.getRequestNo() + ", " + request.getDescription());
			dto.setRequestStage("");
			/*dto.setRequestStage(RequestProgressLookup.requestStageProgress.REQUEST_PROGRESS_START);*/
			
		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("nextRequestStart: DTO info: " + getDto(userInfo));
		}

	}
	
	@Override
	public void nextRequestHeader(Request request, Errors refErrors, UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return;

			Errors reqErrors = dto.getErrors();
			
			if (null == reqErrors) return;
			
			Errors errors = getErrorObject(reqErrors);
			if (null != reqErrors && reqErrors.hasErrors()) {
				
				errors.rejectValue("request", "Request.addLineSeperator", "");
			}
				// Add the request description before the start of a new request process
			if (null != request && null != request.getRequestNo() && null != request.getDescription()) {
				Object[] errorArgs = { request.getRequestNo(), request.getDescription() };
				errors.rejectValue("request", "Request.nextRequestStart", errorArgs, "Request: " + request.getRequestNo() + ", " + request.getDescription());
			}
			dto.setErrors(errors);

		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("nextRequestStart: DTO info: " + getDto(userInfo));
		}

	}


	@Override
	public void updateRequestStage(Request request, Long requestStage, UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return;
			String itemName = requestHelper.getItemName(request.getItemId());
			// dto.setRequestDescription("Request number:" + request.getRequestNo() + ", " + itemName + ", " + request.getDescription());
			dto.setRequestDescription(itemName + ", " + "Request: " + request.getRequestNo() + ", " + request.getDescription());
			dto.setRequestStage(RequestProgressLookup.itemRequestStageToRequestProgress.get(requestStage));
		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("updateRequestStage: DTO info: " + getDto(userInfo));
		}

	}

	@Override
	public void updateStageCompleteMessage(Request request, Errors refErrors, UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return;
			
			String requestStage = requestHelper.getRequestStageLkpValue(request);
			
			Errors errors = getErrorObject(refErrors);
			String itemName = requestHelper.getItemName(request.getItemId());
			String errorMsg = (refErrors.hasErrors()) ? "Failed" : "Successful";
			
			Object[] errorArgs = { itemName, request.getRequestNo(), requestStage, errorMsg };
			errors.rejectValue("request", "Request.updateStage", errorArgs, itemName + ", Request: " + request.getRequestNo() + ", " + requestStage + ":" + errorMsg);
			
			dto.setErrors(errors);
		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("updateErrors: DTO info: " + getDto(userInfo));
		}
		
		
	}
	
	@Override
	public void updateErrors(Errors errors, UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return;
			
			rwLock.writeLock().lock();
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return;
			
			dto.setErrors(errors);
		}
		finally {
			
			if (null != rwLock) rwLock.writeLock().unlock();
			
			if (log.isDebugEnabled()) log.debug("updateErrors: DTO info: " + getDto(userInfo));
		}

	}
	
	@Override
	public RequestProgressDTO getDto(UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return null;
			
			rwLock.readLock().lock();
			
			RequestProgressDTO dto = requestProgressDtoFactory.get(userInfo);
			
			if (null == dto) return null;
			
			RequestProgressDTO copyDto = dto.copy();
			
			copyDto.setBusinessValidationException((null != dto.getErrors()) ? businessExceptionHelper.getBusinessValidationException(null, dto.getErrors(), null) : null);
			
			return copyDto;
			
		}
		finally {
			
			if (null != rwLock) rwLock.readLock().unlock();
		}
		
	}
	
	@Override
	public boolean active(UserInfo userInfo) {

		ReadWriteLock rwLock = null;
		
		try {
			
			rwLock = requestProgressRWLockFactory.get(userInfo);
			
			if (null == rwLock) return false;
			
			rwLock.readLock().lock();
			
			AtomicLong requestProgressCount = requestProgressCountFactory.get(userInfo);
			
			return (requestProgressCount.get() > 0);
			
		}
		finally {
			
			if (null != rwLock) rwLock.readLock().unlock();
		}

	}
	
	private MapBindingResult getErrorObject(Errors refErrors) {
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = null;
		errors = new MapBindingResult( errorMap, (null != refErrors) ? refErrors.getObjectName() : Request.class.getName() );
		return errors;
		
	}

}
