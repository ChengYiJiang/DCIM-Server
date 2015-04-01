package com.raritan.tdz.request.home;

import java.util.List;

import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.UserInfo;

/**
 * 
 * @author bunty
 *
 */

public class RequestMessage {

	Request request;
	
	List<Request> requests;

	Errors errors;
	
	Boolean requestByPass;
	
	UserInfo userInfo;

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public List<Request> getRequests() {
		return requests;
	}

	public void setRequests(List<Request> requests) {
		this.requests = requests;
	}

	public Boolean getRequestByPass() {
		return requestByPass;
	}

	public void setRequestByPass(Boolean requestByPass) {
		this.requestByPass = requestByPass;
	}

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public RequestMessage(Request request, List<Request> requests) {
		this.request = request;
		this.requests = requests;
	}

	public RequestMessage(Request request, List<Request> requests,
			Boolean requestByPass) {
		this.request = request;
		this.requests = requests;
		this.requestByPass = requestByPass;
	}

	public RequestMessage(Request request, List<Request> requests,
			Errors errors, Boolean requestByPass) {
		this.request = request;
		this.requests = requests;
		this.errors = errors;
		this.requestByPass = requestByPass;
	}

	public RequestMessage(Request request, List<Request> requests,
			Errors errors, Boolean requestByPass, UserInfo userInfo) {
		this.request = request;
		this.requests = requests;
		this.errors = errors;
		this.requestByPass = requestByPass;
		this.userInfo = userInfo;
	}
	
	
}
