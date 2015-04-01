/**
 * 
 */
package com.raritan.tdz.dctimport.job;

/**
 * @author prasanna
 *
 */
public enum TransitionStep {
		IMPORT_STEP("import"),
		VALIDATE_STEP("validate");
		
		TransitionStep(String value){
			this.value = value;
		}
		
		private String value;
		
		public String getValue(){
			return value;
		}
}
