package com.raritan.tdz.util;

import java.util.Map;
import java.util.Set;

/**
 * Given the DTO and a domain root object, this will create a trace for the dto field to domain field if-and-only-if 
 * the field name in dto is the same as the field name in domain object.
 * @author prasanna
 */
public interface DtoToDomainObjectTrace {
	
	/**
	 * This will return all the trace values as a list. It will be in the following format:<br><br>
	 * [firstNode]<br>
	 * [firstNode].[secondNode]<br>
	 * ...<br>
	 * ...<br>
	 * [firstNode].[secondNode]...[lastNode]<br>
	 * @return list
	 */
	public Set<String> getAllTrace();
	
	/**
	 * This will return all the trace values as a list given the depth. It will be in the following format:<br><br>
	 * [firstNode]<br>
	 * [firstNode].[secondNode]<br>
	 * ...<br>
	 * ...<br>
	 * [firstNode].[secondNode]...[lastNode(depth)]<br>
	 * @param depth
	 * @return list
	 */
	public Set<String> getAllTrace(int depth);
	
	/**
	 * This will return all the trace values as a list given the depth. It will be in the following format:<br><br>
	 * [firstNode(startDepth)]<br>
	 * [firstNode].[secondNode]<br>
	 * ...<br>
	 * ...<br>
	 * [firstNode].[secondNode]...[lastNode(endDepth)]<br>
	 * @param startDepth
	 * @param endDepth
	 * @return list
	 */
	public Set<String> getAllTrace(int startDepth, int endDepth);
	
	/**
	 * This will return all the leaf nodes from the trace.
	 * @return
	 */
	public Set<String> getAllLeafNodes();
	
	/**
	 * This will return all the first nodes in the trace
	 * @return list
	 */
	public Set<String> getAllFirstNodes();
	
	/**
	 * This will return all the trace values excluding the last node for each trace. This will be in the following format:<br><br>
	 * [firstNode(startDepth)]<br>
	 * [firstNode].[secondNode]<br>
	 * ...<br>
	 * ...<br>
	 * [firstNode].[secondNode]...[lastNode(excludeLeafNode)]
	 * @return list
	 */
	public Set<String> getAllTraceExcludeLeafNode();
	
	/**
	 * This will return the trace value to the leafNode map. The map's key will be leafNode and the value 
	 * will be trace excluding the leaf node. This will be in the following format:<br><br>
	 * key = [LeafNode]<br>
	 * value = [firstNode].[secondNode]...[lastNode(excludeLeafNode)]
	 * @return map
	 */
	public Map<String, String> getLeafToTraceMap();
	
	/**
	 * Given a dtoObjectName, returns the trace as firstNode.secondNode...lastNode
	 * @param dtoObjectName
	 * @return
	 */
	public String getTrace(String dtoObjectName);
	
	
	/**
	 * Given a dtoObjectName, returns the trace excluding last node 
	 * @param dtoObjectName
	 * @return
	 */
	public String getTraceExcludeLeafNode(String dtoObjectName);
	
	/**
	 * Given the dtoObjectName, returns first node
	 * @param dtoObjectName
	 * @return
	 */
	public String getFirstNode(String dtoObjectName);
	
	/**
	 * Given the dtoObjectName, returns last node
	 * @param dtoObjectName
	 * @return
	 */
	public String getLastNode(String dtoObjectName);

}
