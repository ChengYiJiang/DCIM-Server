/**
 * 
 */
package com.raritan.tdz.circuit.validators;

import com.raritan.tdz.circuit.validators.EnoughPowerResult.PowerChainResultHandler;

/**
 * @author prasanna
 *
 */
public class EnoughPowerResult {
	public static enum PowerChainResultHandler { POWER_NODE_HANDLED, PROCESS_NEXT_POWER_NODE, PROCESS_NODE_ERROR }
	private EnoughPowerResult.PowerChainResultHandler resultHandler = EnoughPowerResult.PowerChainResultHandler.POWER_NODE_HANDLED;
	private Object paramForNextChain = null;
	
	public EnoughPowerResult(PowerChainResultHandler resultHandler){
		this.resultHandler = resultHandler;
	}
	
	public EnoughPowerResult(PowerChainResultHandler resultHandler, Object paramForNextChain){
		this.resultHandler = resultHandler;
		this.paramForNextChain = paramForNextChain;
	}
	
	public EnoughPowerResult.PowerChainResultHandler getResultHandler() {
		return resultHandler;
	}
	public void setResultHandler(EnoughPowerResult.PowerChainResultHandler resultHandler) {
		this.resultHandler = resultHandler;
	}
	public Object getParamForNextChain() {
		return paramForNextChain;
	}
	public void setParamForNextChain(Object paramForNextChain) {
		this.paramForNextChain = paramForNextChain;
	}
}
