package com.raritan.tdz.request.progress;

import org.springframework.validation.Errors;

import com.raritan.tdz.exception.BusinessValidationException;

/**
 * 
 * @author bunty
 *
 */
public class RequestProgressDTO {

	// This holds information as start, in_progress, finish
	// Update by intercepting public void process(List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) @Before to start
	// Update by intercepting public void process(List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) @After to finish
	// Update by channel intercepter to in_progress OR by private void processRelatedRequests(Request request, List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) @Before
	Long progressState;
	
	// Update by intercepting public void process(List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) @Before
	Long totalNumOfRequest;

	// private void processRelatedRequests(Request request, List<Request> requests, UserInfo userInfo, Errors errors, Boolean requestByPass) @Before
	Long processingRequestNumber;

	// Update by channel intercepter of different stages 
	String requestDescription;
	
	// Update by validate channel intercepter ("Validating Request") and execute channel intercepter ("Approving Request..., Issuing Work Order... , etc.") of different stages
	String requestStage;
	
	// Updated by validate channel intercepter 
	Errors errors;
	
	// Prepared when the client makes the request to get status
	BusinessValidationException businessValidationException;

	public Long getProgressState() {
		return progressState;
	}

	public void setProgressState(Long progressState) {
		this.progressState = progressState;
	}

	public Long getTotalNumOfRequest() {
		return totalNumOfRequest;
	}

	public void setTotalNumOfRequest(Long totalNumOfRequest) {
		this.totalNumOfRequest = totalNumOfRequest;
	}

	public Long getProcessingRequestNumber() {
		return processingRequestNumber;
	}

	public void setProcessingRequestNumber(Long processingRequestNumber) {
		this.processingRequestNumber = processingRequestNumber;
	}

	public String getRequestDescription() {
		return requestDescription;
	}

	public void setRequestDescription(String requestDescription) {
		this.requestDescription = requestDescription;
	}

	public String getRequestStage() {
		return requestStage;
	}

	public void setRequestStage(String requestStage) {
		this.requestStage = requestStage;
	}

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		
		if (null != this.errors) {
		
			if (null != errors) this.errors.addAllErrors(errors);
			
		}
		else {
			this.errors = errors;
		}
		
	}

	public BusinessValidationException getBusinessValidationException() {
		return businessValidationException;
	}

	public void setBusinessValidationException(
			BusinessValidationException businessValidationException) {
		this.businessValidationException = businessValidationException;
	}

	@Override
	public String toString() {
		return "RequestProgressDTO [progressState=" + progressState
				+ ", totalNumOfRequest=" + totalNumOfRequest
				+ ", processingRequestNumber=" + processingRequestNumber
				+ ", requestDescription=" + requestDescription
				+ ", requestStage=" + requestStage + ", errors=" + errors
				+ ", businessValidationException="
				+ businessValidationException + "]";
	}

	public RequestProgressDTO copy() {
		RequestProgressDTO dto = new RequestProgressDTO();
		dto.setProcessingRequestNumber(this.getProcessingRequestNumber());
		dto.setProgressState(this.getProgressState());
		dto.setRequestDescription(this.getRequestDescription());
		dto.setRequestStage(this.getRequestStage());
		dto.setTotalNumOfRequest(this.getTotalNumOfRequest());
		
		return dto;
	}
	
}
