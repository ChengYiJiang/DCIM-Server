/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;

/**
 * @author prasanna
 *
 */
public class DCTImportFailureListner implements StepExecutionListener{
	
	
	private ImportErrorHandler importErrorHandler;
	
	public DCTImportFailureListner(ImportErrorHandler importErrorHandler){
		this.importErrorHandler = importErrorHandler;
	}
	
	// Logger for all Import operations
	private Logger log = Logger.getLogger("ImportLogger");
	
	public void onReadError(Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (importErrorHandler.getErrors().size() > 0){
			log.error("Step " + stepExecution.getStepName() + " Failed");
			stepExecution.setTerminateOnly();
			return ExitStatus.FAILED;
		}
		
		return ExitStatus.COMPLETED;
	}
}
