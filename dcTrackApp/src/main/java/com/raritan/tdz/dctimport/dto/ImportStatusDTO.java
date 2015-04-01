/**
 * 
 */
package com.raritan.tdz.dctimport.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.dctimport.job.ImportJobListener;
import com.raritan.tdz.dctimport.job.ImportStepListener;

/**
 * @author prasanna
 *
 */
@JsonAutoDetect(fieldVisibility=Visibility.NONE, getterVisibility=Visibility.NONE, isGetterVisibility=Visibility.NONE, setterVisibility=Visibility.NONE)
public class ImportStatusDTO {

	private ImportErrorHandler importErrorHandler;
	private ImportJobListener importJobListener;
	private List<ImportStepListener> importStepListeners;
	
	private final List<String> additionalErrors;
	private final List<String> additionalMessages;
	
	private static final String RUNNING="Running";
	private static final String FAILED="Failed";
	private static final String COMPLETE="Complete";
	
	
	public ImportStatusDTO() {
		this.additionalErrors = null;
		this.additionalMessages = null;
	}
	
	public ImportStatusDTO(ImportErrorHandler importErrorHandler, ImportJobListener importJobListener, List<ImportStepListener> importStepListeners){
		this.importErrorHandler = importErrorHandler;
		this.importJobListener = importJobListener;
		this.importStepListeners = importStepListeners;
		this.additionalErrors = null;
		this.additionalMessages = null;
	}
	
	
	public ImportStatusDTO(ImportErrorHandler importErrorHandler, ImportJobListener importJobListener, 
									List<ImportStepListener> importStepListeners,List<String> additionalErrors, List<String> additionalMessages){
		this.importErrorHandler = importErrorHandler;
		this.importJobListener = importJobListener;
		this.importStepListeners = importStepListeners;
		this.additionalErrors = additionalErrors;
		this.additionalMessages = additionalMessages;
	}
	
	@JsonProperty("progress")
	public final int getProgress(){
		int totalCount = 0;
		int currentCount = 0;
		int progress = 0;
		
		if (importJobListener != null)
			totalCount = importJobListener.getTotalCount();
		
		if (importStepListeners != null && importStepListeners.size() > 0)
		{
			for (ImportStepListener stepListener:importStepListeners){
				currentCount += stepListener.getCurrentCount();
			}
		}
		
		if (totalCount > 0){
			double difference = (currentCount * 1.0)/(totalCount);
			progress = (int) ((difference) * 100.0);
		}
		
		return progress;
	}
	
	@JsonProperty("step")
	public final String getStepDescription(){
		if (importJobListener != null)
			return importJobListener.getCurrentStepName();
		return ImportStepListener.VALIDATION_STEP;
	}
	
	
	public final long getJobId(){
		if (importJobListener != null)
			return importJobListener.getJobId();
		return 0;
	}
	
	@JsonProperty("isJobRunning")
	public final boolean isJobRunning(){
		if (importJobListener != null)
			return importJobListener.isJobRunning();
		return false;
	}
	
	@JsonProperty("lastUpdatedDate")
	public final Date getLastUpdatedDate(){
		if (importJobListener != null)
			return importJobListener.getLastUpdatedDate();
		return null;
	}
	
	@JsonProperty("messages")
	public final List<String> getMessages(){
		List<String> messages = new ArrayList<String>();
		messages.addAll(getErrors());
		messages.addAll(getWarnings());
		if (additionalMessages != null && additionalMessages.size() > 0 && getProgress() == 100){
			messages.addAll(additionalMessages);
		}
		return messages;
	}
	
	@JsonProperty("errors")
	public final List<String> getErrors(){
		List<String> errors = new ArrayList<String>();
		
		if (additionalErrors != null){
			errors.addAll(additionalErrors);
		}
		
		if (importErrorHandler != null)
			errors.addAll(importErrorHandler.getErrors());
		
		return errors;
	}
	
	@JsonProperty("warnings")
	public final List<String> getWarnings() {
		List<String> warnings = new ArrayList<String>();
		if (importErrorHandler != null)
		 warnings = importErrorHandler.getWarnings();
		return warnings;
	}
	
	@JsonProperty("url")
	public final String getURL() {
		if (importJobListener != null)
			return importJobListener.getLogURL();
		return null;
	}
	
	@JsonProperty("lineNumber")
	public final int getLineNumber(){
		if (importJobListener != null)
			return importJobListener.getLineNumber();
		return 0;
	}
	
	@JsonProperty("status")
	public final String getStatus(){
		String status = "";
		
		//If the JobRunning = true then status is running
		if (isJobRunning()){
			status = RUNNING;
		} else if (!isJobRunning() 
				&& getProgress() >= 0
				&& (getErrors().size() > 0 || getWarnings().size() > 0)){
			//If the JobRunning = false
			//If progress is >= 0 (this way client does not get stuck)
			//If there are errors or warnings
			//The status is failed
			status = FAILED;
		} else if (!isJobRunning()
				&& getProgress() >= 100
				&& getErrors().isEmpty() && getWarnings().isEmpty()){
		
			//If the JobRunning = false
			//If progress is >= 100 (this way client does not get stuck)
			//If there are no errors or warnings
			//The status is completed
			status = COMPLETE;
		}
		
		return status.toString();
	}

}
