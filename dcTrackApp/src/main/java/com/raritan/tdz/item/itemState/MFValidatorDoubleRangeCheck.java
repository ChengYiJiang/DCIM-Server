/**
 * 
 */
package com.raritan.tdz.item.itemState;

/**
 * @author prasanna
 *
 */
public class MFValidatorDoubleRangeCheck implements
		MandatoryFieldValidatorRangeCheck {

	Integer lowerLimit = null;
	Integer upperLimit = null;
	String defaultValue = null;
	String errorCode = null;
	
	public MFValidatorDoubleRangeCheck(Integer lowerLimit, Integer upperLimit, String defaultValue) {
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.defaultValue = defaultValue;
	}
	
	public MFValidatorDoubleRangeCheck(Integer lowerLimit,String defaultValue) {
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
			result = value != null ? value.doubleValue() > lowerLimit.doubleValue() : false;
		} else {
			result = value != null ? value.doubleValue() > lowerLimit.doubleValue() && value.doubleValue() < upperLimit.doubleValue() : false;
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
