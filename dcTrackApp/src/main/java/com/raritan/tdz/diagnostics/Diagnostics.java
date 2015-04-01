package com.raritan.tdz.diagnostics;

import org.springframework.validation.Errors;

public interface Diagnostics {

	public void diagnose(Errors errors);
	
}
