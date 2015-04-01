package com.raritan.tdz.controllers.assetmgmt.exceptions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.raritan.tdz.exception.BusinessValidationException;


public class DCTRestAPIException extends Exception {
	private final Logger log = Logger.getLogger(this.getClass());
	
	private static final long serialVersionUID = 1L;
	private HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; 
	
	private String msg;	
	private Object errors;
	private final String dfltError = "ApiError";
	private Map<String, String> warnings;
	private String trace;
	 
	public DCTRestAPIException(HttpStatus status){
		super("");
		setStatus(status);
		setMsg(status.getReasonPhrase());
	}
	
	public DCTRestAPIException(String msg, HttpStatus status){
		super(msg);
		setStatus(status);
		setMsg(status.getReasonPhrase());
	}
	
	public DCTRestAPIException(String msg, HttpStatus status, StackTraceElement[] trace){
		super(msg);
		setStatus(status);
		setMsg(msg);
		setErrors(dfltError);
		setTrace(trace);
	}
	
	public DCTRestAPIException(HttpStatus status, Throwable thr){
		super(thr.getMessage());
		setStatus(status);
		setTrace(thr.getStackTrace());
		if( thr.getMessage() != null){
			setMsg(thr.getMessage());
		}else{
			setMsg(status.getReasonPhrase());
		}
		if( thr instanceof BusinessValidationException){
			BusinessValidationException be = (BusinessValidationException)thr;
			setErrors(be.getValidationErrorsMap());
			setWarnings(getWarningMessageMap(be));
			if( log.isDebugEnabled()){
				if(be.getValidationErrorsMap() != null ){
					for (String key: be.getValidationErrorsMap().keySet()){
						log.debug("ERROR: " + key + ": " + be.getValidationErrorsMap().get(key));
					}
				}
				if( getWarningMessageMap(be) != null ){
					for (String key: getWarningMessageMap(be).keySet()){
						log.debug("WARN: " + key + ": " + getWarningMessageMap(be).get(key));
					}
				}
			}
		}else{
			setErrors(dfltError);
			thr.printStackTrace();
		}
	}
	public String getMessage(){
		String msg = super.getMessage();
		if( msg == null) return "";
		else return msg;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public Map<String, String> getWarnings() {
		return warnings;
	}

	public void setWarnings(Map<String, String> warnings) {
		this.warnings = warnings;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	public void setTrace(StackTraceElement [] trace){
		StringBuffer tr = new StringBuffer();
		for ( StackTraceElement s : trace ){
			tr.append(s.getClassName());
			tr.append(".");
			tr.append(s.getMethodName());
			tr.append(":");
			tr.append(s.getLineNumber());
			tr.append(",");
		}
		this.trace = tr.toString();
	}
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public Map<String, ?>getExceptionDetails(){
		Map<String, Object>retval = new LinkedHashMap<String, Object>();
		retval.put("errors", getErrors());
		retval.put("warnings", warnings);
		retval.put("messages", msg);
		retval.put("trace", trace);
		return retval;
	}

	public Object getErrors() {
		return errors;
	}

	public void setErrors(Object errors) {
		this.errors = errors;
	}
	
	private Map<String,String> getWarningMessageMap(BusinessValidationException be){
		Map<String,String> resultMap = new HashMap<String, String>();
		Map<String,BusinessValidationException.Warning> warningMap = be.getWarnings();
		for (Map.Entry<String, BusinessValidationException.Warning> entry:warningMap.entrySet() ){
			BusinessValidationException.Warning warning = entry.getValue();
			resultMap.put(entry.getKey(), warning.getWarningMessage());
		}
		return resultMap;
	}
}
