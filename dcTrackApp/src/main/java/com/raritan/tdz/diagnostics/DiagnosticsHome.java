package com.raritan.tdz.diagnostics;

import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.vbjavabridge.domain.LNEvent;

public interface DiagnosticsHome {

	public void processLNEvent(LNEvent event) throws BusinessValidationException;
	
}
