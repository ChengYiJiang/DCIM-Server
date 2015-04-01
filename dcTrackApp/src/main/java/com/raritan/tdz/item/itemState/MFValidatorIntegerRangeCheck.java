/**
 * 
 */
package com.raritan.tdz.item.itemState;

/**
 * @author prasanna
 *
 */
public class MFValidatorIntegerRangeCheck implements
		MandatoryFieldValidatorRangeCheck {

	Integer lowerLimit = null;
	Integer upperLimit = null;
	String defaultValue = null;
	String errorCode = null;
	
	public MFValidatorIntegerRangeCheck(Integer lowerLimit, Integer upperLimit, String defaultValue) {
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.defaultValue = defaultValue;
	}
	
	public MFValidatorIntegerRangeCheck(Integer lowerLimit,String defaultValue) {
		this.lowerLimit = lowerLimit;
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
		Integer value = (Integer)clientValue;
		boolean result = false;
		
		if (lowerLimit == null && upperLimit == null){
			throw new IllegalArgumentException("Invalid upper and lower limits. Fix the bean. This should never occur in field");
		}
		
		if (upperLimit == null){
			result = value != null ? value.intValue() > lowerLimit.intValue() : false;
		} else {
			result = value != null ? value.intValue() > lowerLimit.intValue() && value.intValue() < upperLimit.intValue() : false;
		}
		
		return result;
	}

	@Override
	public String getDefaultLabel() {
		return defaultValue;
	}

	@Override
	public String getErrorCode() {
		return errorCode;
	}

}
