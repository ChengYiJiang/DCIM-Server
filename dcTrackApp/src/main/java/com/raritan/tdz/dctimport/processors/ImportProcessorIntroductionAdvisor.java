/**
 * 
 */
package com.raritan.tdz.dctimport.processors;

import org.springframework.aop.support.DefaultIntroductionAdvisor;

import com.raritan.tdz.dctimport.integration.exceptions.ImportErrorHandler;

/**
 * @author prasanna
 *
 */
public class ImportProcessorIntroductionAdvisor extends
		DefaultIntroductionAdvisor {

	public ImportProcessorIntroductionAdvisor(ImportErrorHandler importErrorHandlerGateway){
		super(new ImportProcessorIntroductionInterceptor(importErrorHandlerGateway));
	}
}
