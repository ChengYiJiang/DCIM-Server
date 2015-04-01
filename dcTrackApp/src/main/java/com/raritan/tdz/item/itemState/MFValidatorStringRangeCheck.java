/**
 * 
 */
package com.raritan.tdz.item.itemState;

/**
 * @author prasanna
 *
 */
public class MFValidatorStringRangeCheck implements
		MandatoryFieldValidatorRangeCheck {

	String defaultValue = null;
	String errorCode = null;
	
	public MFValidatorStringRangeCheck(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	
	
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}



	/* (non-Javadoc)
	 * @see com.raritan.tdz.item.itemState.MandatoryFieldValidatorRangeCheck#checkRange(java.lang.Object)
	 */
	@Override
	public boolean checkRange(Object clientValue) {
		String value = (String)clientValue;
		return value != null && !value.isEmpty();
	}

	@Override
	public String getDefaultLabel() {
		// TODO Auto-generated method stub
		return defaultValue;
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

}
