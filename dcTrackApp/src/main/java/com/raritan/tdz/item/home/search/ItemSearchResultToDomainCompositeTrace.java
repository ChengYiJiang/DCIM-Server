/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.raritan.tdz.util.DtoToDomainObjectTrace;

/**
 * @author prasanna
 *
 */
public class ItemSearchResultToDomainCompositeTrace implements
		DtoToDomainObjectTrace {
	
	List<DtoToDomainObjectTrace> traceList;
	public ItemSearchResultToDomainCompositeTrace(List<DtoToDomainObjectTrace> traceList){
		this.traceList = traceList;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTrace()
	 */
	@Override
	public Set<String> getAllTrace() {
		Set<String> allTrace = new LinkedHashSet<String>();
		for (DtoToDomainObjectTrace trace : traceList){
			allTrace.addAll(trace.getAllTrace());
		}
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTrace(int)
	 */
	@Override
	public Set<String> getAllTrace(int depth) {
		Set<String> allTrace = new LinkedHashSet<String>();
		for (DtoToDomainObjectTrace trace : traceList){
			allTrace.addAll(trace.getAllTrace(depth));
		}
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTrace(int, int)
	 */
	@Override
	public Set<String> getAllTrace(int startDepth, int endDepth) {
		Set<String> allTrace = new LinkedHashSet<String>();
		for (DtoToDomainObjectTrace trace : traceList){
			allTrace.addAll(trace.getAllTrace(startDepth, endDepth));
		}
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllLeafNodes()
	 */
	@Override
	public Set<String> getAllLeafNodes() {
		Set<String> allTrace = new LinkedHashSet<String>();
		for (DtoToDomainObjectTrace trace : traceList){
			allTrace.addAll(trace.getAllLeafNodes());
		}
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllFirstNodes()
	 */
	@Override
	public Set<String> getAllFirstNodes() {
		Set<String> allTrace = new LinkedHashSet<String>();
		for (DtoToDomainObjectTrace trace : traceList){
			allTrace.addAll(trace.getAllFirstNodes());
		}
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTraceExcludeLeafNode()
	 */
	@Override
	public Set<String> getAllTraceExcludeLeafNode() {
		Set<String> allTrace = new LinkedHashSet<String>();
		for (DtoToDomainObjectTrace trace : traceList){
			allTrace.addAll(trace.getAllTraceExcludeLeafNode());
		}
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getLeafToTraceMap()
	 */
	@Override
	public Map<String, String> getLeafToTraceMap() {
		Map<String, String> leafToTraceMap = new HashMap<String, String>();
		for (DtoToDomainObjectTrace trace : traceList){
			leafToTraceMap.putAll(trace.getLeafToTraceMap());
		}
		return leafToTraceMap;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getTrace(java.lang.String)
	 */
	@Override
	public String getTrace(String dtoObjectName) {
		String traceStr = null;
		for (DtoToDomainObjectTrace trace : traceList){
			traceStr = trace.getTrace(dtoObjectName);
			if (traceStr != null)
				break;
		}
		return traceStr;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getTraceExcludeLeafNode(java.lang.String)
	 */
	@Override
	public String getTraceExcludeLeafNode(String dtoObjectName) {
		String traceStr = null;
		for (DtoToDomainObjectTrace trace : traceList){
			traceStr = trace.getTraceExcludeLeafNode(dtoObjectName);
			if (traceStr != null)
				break;
		}
		return traceStr;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getFirstNode(java.lang.String)
	 */
	@Override
	public String getFirstNode(String dtoObjectName) {
		String traceStr = null;
		for (DtoToDomainObjectTrace trace : traceList){
			traceStr = trace.getFirstNode(dtoObjectName);
			if (traceStr != null)
				break;
		}
		return traceStr;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getLastNode(java.lang.String)
	 */
	@Override
	public String getLastNode(String dtoObjectName) {
		String traceStr = null;
		for (DtoToDomainObjectTrace trace : traceList){
			traceStr = trace.getLastNode(dtoObjectName);
			if (traceStr != null)
				break;
		}
		return traceStr;
	}

}
