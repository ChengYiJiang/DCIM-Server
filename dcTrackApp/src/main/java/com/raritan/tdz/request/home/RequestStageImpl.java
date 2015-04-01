package com.raritan.tdz.request.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;

/**
 * 
 * @author bunty
 *
 */

public class RequestStageImpl implements RequestStage {

	private static Logger rpLog = Logger.getLogger("RequestProgress");
	
	private List<RequestStageHelper> requestStageHelpers;
	
	public List<RequestStageHelper> getRequestStageHelpers() {
		return requestStageHelpers;
	}

	public void setRequestStageHelpers(List<RequestStageHelper> requestStageHelpers) {
		this.requestStageHelpers = requestStageHelpers;
	}

	Long requestStage;
	
	public RequestStageImpl(Long requestStage) {
		
		this.requestStage = requestStage;
	}

	@Override
	public void process(Request request, Error error) {

		// TODO Update the request history using the new request history dao
		if (rpLog.isDebugEnabled()) rpLog.debug("PROCESS REQUEST: Request Stage: " + requestStage);

	}

	@Override
	public RequestMessage process(RequestMessage requestMessage) throws Throwable {

		// TODO Update the request history using the new request history dao
		if (rpLog.isDebugEnabled()) rpLog.debug("PROCESS REQUEST: Request Stage: " + requestStage);

		UserInfo userInfo = requestMessage.getUserInfo();
		
		for (RequestStageHelper requestStageHelper: requestStageHelpers) {

			Errors reqErrors = requestMessage.getErrors();
			
			if (reqErrors.hasErrors()) break;
			
			try {
				requestStageHelper.update(requestMessage.getRequest(), requestStage, userInfo);
			}
			catch (BusinessValidationException bvex) {
				
				@SuppressWarnings("deprecation")
				List<String> requestIssueMsgs = (null != bvex) ? bvex.getValidationErrors() : new ArrayList<String>();
				
				for (String requestIssueMsg: requestIssueMsgs) {
					if (requestIssueMsg.contains("Request regenerated successfully") || requestIssueMsg.contains("Request Issued successfully")) continue;
					
					Object[] errorArgs = { requestIssueMsg };
					reqErrors.rejectValue("request", "Request.IssueFailedFakeCode", errorArgs, requestIssueMsg);
				}
				
			}
			catch (Throwable t) {
				
				String itemName = "<Unknown>";
				
				Object[] errorArgs = { itemName, requestMessage.getRequest().getRequestNo() };
				reqErrors.rejectValue("request", "itemRequest.processBypassFailed", errorArgs, "Request bypass failed: Internal Error");
				
				t.printStackTrace();
				
				break;
				
				// throw new Exception("Request Bypass failed: Internal Error...");
				
			}
		}

		return requestMessage;
	}

}
