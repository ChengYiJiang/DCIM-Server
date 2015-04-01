/**
 * 
 */
package com.raritan.tdz.dctimport.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;



/**
 * @author prasanna
 * This resolver will help in executing a specific step based
 * on the API being called by the client. 
 * <p><b>Note:</b> Before starting the job, you must setup the transitionStep.
 * If not it will assume that you want to just perform validation of the
 * import file</p>
 */
public class ImportStepResolveListener {
	


	private TransitionStep transitionToStep = TransitionStep.VALIDATE_STEP;

	public TransitionStep getTransitionToStep() {
		return transitionToStep;
	}

	public void setTransitionToStep(TransitionStep transitionToStep) {
		this.transitionToStep = transitionToStep;
	}
	
	@AfterStep
	public ExitStatus afterStep(StepExecution stepExecution) throws Exception {
		return new ExitStatus(this.transitionToStep.getValue());
	}

}
