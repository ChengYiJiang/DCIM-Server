package com.raritan.tdz.powerchain.logger;

import org.springframework.validation.Errors;

public interface PowerChainLogger {
	
	public void log(Errors errors);

}
