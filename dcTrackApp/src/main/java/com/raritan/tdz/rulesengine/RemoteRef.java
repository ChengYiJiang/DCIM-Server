/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.util.List;

import com.raritan.tdz.util.UnitConverterIntf;

/**
 * @author prasanna
 * This represents the remote reference in the XML for rules engine.
 */
public interface RemoteRef {
	
	public enum RemoteRefConstantProperty {
		TYPE {
			public String toString() {
				return "type";
			}
		},
		FOR_ID {
			public String toString() {
				return "id";
			}
		},
		FOR_VALUE {
			public String toString() {
				return "value";
			}
		},
		FOR_UNIT_CONVERTER {
			public String toString() {
				return "unitConverter";
			}
		}
	}
	
	/**
	 * This will provide the remote Type for a remote reference constant.
	 * Please note that the constant is defined in the XML template.
	 * @param remoteReferenceConstant
	 * @return
	 */
	public String getRemoteType(String remoteReferenceConstant);
	
	/**
	 * This will provide the remoteAlias for the remote reference constant and a given property.
	 * 
	 * @param remoteReferenceConstant
	 * @param property
	 * @return
	 */
	public String getRemoteAlias(String remoteReferenceConstant, RemoteRefConstantProperty property);
	
	/**
	 * This will return back the RemoteReference method call back registered to this remote reference.
	 * @param remoteReferenceConstant
	 * @return
	 */
	public RemoteRefMethodCallback getRemoteRefMethodCallback(String remoteReferenceConstant);
	
	/**
	 * This will return back the RemoteReference method call back using filter registered to this remote reference.
	 * @param remoteReferenceConstant
	 * @return
	 */
	public RemoteRefMethodCallbackUsingFilter getRemoteRefMethodCallbackUsingFilter(String remoteReferenceConstant);

	
	/**
	 * Given the value gets the remote reference key
	 * @param value
	 * @return
	 */
	public List<String> getKey(String value);

	/**
	 * This will return back the UnitConverter method call back registered to this unit converter.
	 * @param unitConverterConstant
	 * @return UnitConverter
	 */
	public UnitConverterIntf getRemoteRefUnitConverter(String unitConverterConstant);
	
	/**
	 * This will provide the remote id for a remote reference constant.
	 * Constant is defined in the XML template.
	 * @param remoteReferenceConstant
	 * @return
	 */
	public String getRemoteId(String remoteReferenceConstant);

}
