package com.raritan.tdz.util;

import java.util.HashMap;
import java.util.Map;

public class VersionCheck {
	
	public static final Integer MAJOR = 0;
	public static final Integer MINOR = 1;
	public static final Integer DOT   = 2;
	public static final Integer BUILD = 3;
	
	private static Map<Integer, Integer> normalizeVersion (String version) {
		Map<Integer, Integer> versionMap = new HashMap<Integer, Integer>();
		
		if (version != null && version.length() != 0) {
			try {
				String [] splitVers = version.split("[.]");
				
				versionMap.put(MAJOR, Integer.parseInt(splitVers[0]));
				versionMap.put(MINOR, Integer.parseInt(splitVers[1]));
				
	           if (splitVers[2].contains("-")) {
	                String [] dotAndBuildVers = splitVers[2].split("[-]");
	            	versionMap.put(DOT, Integer.parseInt(dotAndBuildVers[0]));
	                versionMap.put(BUILD, Integer.parseInt(dotAndBuildVers[1]));
	            } else {
	            	versionMap.put(DOT, Integer.parseInt(splitVers[2]));
	            }
			} catch (NumberFormatException nfe) {
				//if the version string cannot be parsed then return false.
				// This will set the system to handle 
				// ip address change old way (i.e delete and add rpdu)
				return null;
			}
		}
		return versionMap;
	}
	
	/**
	 * This function verifies input reportedVersion is greater or equal to baseVersion
	 * @param reportedVersion
	 * @param baseVersion
	 */
	public static boolean verifyVersionGreaterOrEqual (String reportedVersion, String baseVersion) {
		boolean result = false;
		
		if (reportedVersion != null && !reportedVersion.isEmpty() && 
				baseVersion != null && !baseVersion.isEmpty()) {
		
			Map<Integer, Integer> nReportedVersion = normalizeVersion (reportedVersion);
			Map<Integer, Integer> nBaseVersion = normalizeVersion (baseVersion);
			
			if ((nReportedVersion != null && nBaseVersion != null) && 
					(nReportedVersion.get(MAJOR) > nBaseVersion.get(MAJOR) || 
					(nReportedVersion.get(MAJOR) == nBaseVersion.get(MAJOR) && nReportedVersion.get(MINOR) >= nBaseVersion.get(MINOR) && nReportedVersion.get(DOT) >= nBaseVersion.get(DOT))) ) {
				result = true;
			}
		}
		return result;
	}
	
}
