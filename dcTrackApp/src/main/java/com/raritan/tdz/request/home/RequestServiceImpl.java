package com.raritan.tdz.request.home;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.request.progress.RequestProgressDTO;
import com.raritan.tdz.request.progress.RequestProgressUpdate;
import com.raritan.tdz.session.FlexUserSessionContext;
import com.raritan.tdz.user.home.UserHome;

/**
 * 
 * @author bunty
 *
 */
public class RequestServiceImpl implements RequestService {

	private static Logger rpLog = Logger.getLogger("RequestProgress");
	
	private RequestHome requestHome;
	
	private RequestProgressUpdate requestProgressUpdate;
	
	@Autowired
	private UserHome userHome;

	public RequestHome getRequestHome() {
		return requestHome;
	}



	public RequestServiceImpl(RequestHome requestHome,
			RequestProgressUpdate requestProgressUpdate) {

		this.requestHome = requestHome;
		this.requestProgressUpdate = requestProgressUpdate;
	}




	public void setRequestHome(RequestHome requestHome) {
		this.requestHome = requestHome;
	}



	@Override
	public RequestProgressDTO getRequestProgress() {
		
		UserInfo userInfo = userHome.getCurrentUserInfo();
		
		// rpLog.warn("service: dto, user:" + userInfo.getSessionId());
		
		/*return requestHome.getRequestProgress(userInfo);*/
		
		RequestProgressDTO dto = requestProgressUpdate.getDto(userInfo);
		
		/*if (dto != null && dto.getBusinessValidationException() != null) {
			rpLog.warn("return dto = " + dto.getBusinessValidationException().getValidationErrors().size() + "\nmessage = " + dto.getBusinessValidationException().getValidationErrors() + "\nerrors = " + dto.getErrors().getErrorCount() + ": " + dto.getErrors());
		}*/
		
		return dto;
		
	}



	@Override
	public void cleanRequestProgressDTO() {
		
		UserInfo userInfo = userHome.getCurrentUserInfo();
	
		rpLog.warn("service: clean, user:" + userInfo.getSessionId());
		
		/*requestHome.cleanProgress(userInfo);*/
		
		requestProgressUpdate.cleanProgress(userInfo);
		
		
	}

}
