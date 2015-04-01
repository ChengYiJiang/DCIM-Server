package com.raritan.tdz.port.home;

import org.springframework.validation.Errors;

public class DataPanelDataPortObjectCollection extends DataPortObjectCollection {
	public DataPanelDataPortObjectCollection( IPortObjectFactory portObjectFactory ) {
		
		super(portObjectFactory);
	}

	
	@Override
	public void validate(Errors errors) {
		// No validation for data panels, 
	}

}
