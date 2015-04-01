/**
 * 
 */
package com.raritan.tdz.item.itemState;

/**
 * @author prasanna
 *
 */
public class MFValidatorLongRangeCheck implements
		MandatoryFieldValidatorRangeCheck {

	Long lowerLimit = null;
	Long upperLimit = null;
	String defaultValue = null;
	String errorCode = null;
	
	public MFValidatorLongRangeCheck(Long lowerLimit, Long upperLimit,String defaultValue) {
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
		this.defaultValue = defaultValue;
	}
	
	public MFValidatorLongRangeCheck(Long lowerLimit,String defaultValue) {
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
		Long value = (Long)clientValue;
		boolean result = false;
		
		if (lowerLimit == null && upperLimit == null){
			throw new IllegalArgumentException("Invalid upper and lower limits. Fix the bean. This should never occur in field");
		}
		
		if (upperLimit == null){
			result = value != null ? value.longValue() > lowerLimit.longValue() : false;
		} else {
			result = value != null ? value.longValue() > lowerLimit.longValue() && value.longValue() < upperLimit.longValue():false;
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
