/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.StepListener;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;
import com.raritan.tdz.dctimport.logger.ImportLogger;

/**
 * @author prasanna
 *
 */
public class ImportStepListener {
	
	// Logger for all Import operations
	private Logger log = Logger.getLogger("ImportLogger");
	
	private AtomicInteger currentCount = new AtomicInteger() ;
	private AtomicInteger skipCount = new AtomicInteger();
	
	public static String VALIDATION_STEP = "validation";
	public static String IMPORT_STEP = "import";
	
	private StepExecution stepExecution;
	
	private String stepName;
	
	private boolean cleanLogger = false;
	
	@Autowired
	private ImportLogger importLogger;
	
	private ImportErrorHandler importErrorHandler;
	
	@Autowired
	private DCTHeaderFieldSetMapper dctImportHeaderMapper;
	
	public ImportStepListener(ImportErrorHandler importErrorHandler){
		this.importErrorHandler = importErrorHandler;
	}
	

	public int getCurrentCount() {
		return currentCount.get();
	}

	public int getSkipCount() {
		return skipCount.get();
	}

	
	public String getStepName() {
		return stepName;
	}
	
	

	public boolean isCleanLogger() {
		return cleanLogger;
	}

	public void setCleanLogger(boolean cleanLogger) {
		this.cleanLogger = cleanLogger;
	}

	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		stepName = stepExecution.getStepName();
	}

	@AfterStep
	public ExitStatus afterStep(StepExecution stepExecution) throws Exception {
		if (this.cleanLogger && (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED) && importErrorHandler.getErrors().size() <= 0)){
			importLogger.clean(false);
		}
		if (dctImportHeaderMapper != null){
			dctImportHeaderMapper.setOriginalHeader(null);
			dctImportHeaderMapper.setUsed(false);
		}
		return stepExecution.getExitStatus();
	}


	@AfterChunk
	public void afterChunk(ChunkContext context) throws Exception {
		if (stepExecution != null){
			currentCount.set(stepExecution.getReadCount());
			skipCount.set(stepExecution.getSkipCount());
			log.info(stepExecution.getStepName() + ": " + currentCount + " skipped: " + skipCount);
			
			importLogger.commit();
		}
		
		
	}
	
	public void resetCounts(){
		currentCount.set(0);
		skipCount.set(0);
	}
}
