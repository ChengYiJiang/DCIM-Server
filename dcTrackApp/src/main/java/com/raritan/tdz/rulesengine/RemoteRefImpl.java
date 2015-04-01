/**
 * 
 */
package com.raritan.tdz.rulesengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;

import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.util.UnitConverterIntf;

/**
 * @author prasanna
 *
 */
public class RemoteRefImpl implements RemoteRef {
	
	private static final String remoteType = "remoteType";
	private static final String remoteCallback = "remoteCallback";
	private static final String unitConverter = "unitConverter";
	private static final String id ="id";

	private Map<String, Object> remoteRefMap = new HashMap<String, Object>();
	
	public RemoteRefImpl(Map<String, Object> remoteRefMap){
		this.remoteRefMap = remoteRefMap;
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRef#getRemoteType(java.lang.String)
	 */
	@Override
	public String getRemoteType(String remoteReferenceConstant) {
		StringBuffer key = new StringBuffer();
		key.append(remoteReferenceConstant);
		key.append(".");
		key.append(remoteType);
		return (String)remoteRefMap.get(key.toString());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRef#getRemoteAlias(java.lang.String, com.raritan.tdz.rulesengine.RemoteRef.RemoteRefConstantProperty)
	 */
	@Override
	public String getRemoteAlias(String remoteReferenceConstant,
			RemoteRefConstantProperty property) {
		StringBuffer key = new StringBuffer();
		key.append(remoteReferenceConstant);
		key.append(".");
		key.append(property.toString());
		return (String)remoteRefMap.get(key.toString());
	}
	
	@Override
	public String getRemoteId(String remoteReferenceConstant){
		StringBuffer key = new StringBuffer();
		key.append(remoteReferenceConstant);
		key.append(".");
		key.append(id);
		return (String)remoteRefMap.get(key.toString());
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRef#getRemoteRefMethodCallback(java.lang.String)
	 */
	@Override
	public RemoteRefMethodCallback getRemoteRefMethodCallback(
			String remoteReferenceConstant) {
		StringBuffer key = new StringBuffer();
		key.append(remoteReferenceConstant);
		key.append(".");
		key.append(remoteCallback);
		if (remoteRefMap.get(key.toString()) != null && remoteRefMap.get(key.toString()) instanceof RemoteRefMethodCallback)
			return (RemoteRefMethodCallback)remoteRefMap.get(key.toString());
		else
			return null;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.rulesengine.RemoteRef#getRemoteRefMethodCallback(java.lang.String)
	 */
	@Override
	public UnitConverterIntf getRemoteRefUnitConverter(
			String unitConverterConstant) {
		StringBuffer key = new StringBuffer();
		key.append(unitConverterConstant);
		key.append(".");
		key.append(unitConverter);
		return (UnitConverterIntf)remoteRefMap.get(key.toString());
	}

	@Override
	public List<String> getKey(String value) {
		ArrayList<String> result = new ArrayList<String>();
		for (Map.Entry<String, Object> entry: remoteRefMap.entrySet()){
			String key = entry.getKey();
			Object value1 = entry.getValue();
			
			if (value1.equals(value)){
				result.add(key);
			}
		}
		
		return result;
	}

	@Override
	public RemoteRefMethodCallbackUsingFilter getRemoteRefMethodCallbackUsingFilter(
			String remoteReferenceConstant) {
		StringBuffer key = new StringBuffer();
		key.append(remoteReferenceConstant);
		key.append(".");
		key.append(remoteCallback);
		if (remoteRefMap.get(key.toString()) != null && remoteRefMap.get(key.toString()) instanceof RemoteRefMethodCallbackUsingFilter)
			return (RemoteRefMethodCallbackUsingFilter)remoteRefMap.get(key.toString());
		else
			return null;
	}

}
