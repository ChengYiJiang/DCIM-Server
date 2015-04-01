/**
 * 
 */
package com.raritan.tdz.dctimport.integration.routers;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author prasanna
 *
 */
public class ImportNormalizerResolverImpl implements ImportNormalizerResolver {
	
	private final Map<String, String> patternToNormalizerMap;
	
	public ImportNormalizerResolverImpl(Map<String, String> patternToNormalizerMap){
		this.patternToNormalizerMap = patternToNormalizerMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.dctimport.integration.routers.ImportNormalizerResolver#resolve(java.lang.String)
	 */
	@Override
	public String resolve(String line) {
		String normalizer = null;
		boolean found = false;
		for (Map.Entry<String, String> entry: patternToNormalizerMap.entrySet()){
			String patternStr = entry.getKey();
			normalizer = entry.getValue();
			Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);
			if (pattern.matcher(line).matches()){
				found = true;
				break;
			}
		}
		
		if (!found) normalizer = PASSTHROGUH_NORMALIZER;
		
		return normalizer;
	}

}
