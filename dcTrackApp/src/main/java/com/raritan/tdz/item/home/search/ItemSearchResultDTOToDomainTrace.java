/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.DtoToDomainObjectTrace;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * @author prasanna
 *
 */
public class ItemSearchResultDTOToDomainTrace implements DtoToDomainObjectTrace {

	protected HashMap<String, String> trace = new HashMap<String,String>();
	protected HashMap<String, List<Field>> traceFields = new HashMap<String, List<Field>>();
	private String rootClassStr;
	
	public ItemSearchResultDTOToDomainTrace(String rootClassStr){
		this.rootClassStr = rootClassStr;
	}
	
	public void init(){
		Class<?> rootClass;
		try {
			rootClass = Class.forName(rootClassStr);
			createTrace(rootClass);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTrace()
	 */
	@Override
	public Set<String> getAllTrace() {
		Set<String> allTrace =  new LinkedHashSet<String>();
		
		for (String traceStr:trace.values()){
			String[] splitStr = traceStr.split("\\.");
			StringBuffer initStrBuffer = new StringBuffer();
			for (int count = 0; count < splitStr.length; count++){
				initStrBuffer.append(splitStr[count]);
				allTrace.add(initStrBuffer.toString());
				initStrBuffer.append(".");
			}
		}
		
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTrace(int)
	 */
	@Override
	public Set<String> getAllTrace(int depth) {
		Set<String> allTrace =  new LinkedHashSet<String>();
		
		for (String traceStr:trace.values()){
			String[] splitStr = traceStr.split("\\.");
			StringBuffer initStrBuffer = new StringBuffer();
			for (int count = 0; count < (splitStr.length - depth); count++){
				initStrBuffer.append(splitStr[count]);
				allTrace.add(initStrBuffer.toString());
				initStrBuffer.append(".");
			}
		}
		
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTrace(int, int)
	 */
	@Override
	public Set<String> getAllTrace(int startDepth, int endDepth) {
		Set<String> allTrace =  new LinkedHashSet<String>();
		
		for (String traceStr:trace.values()){
			String[] splitStr = traceStr.split("\\.");
			StringBuffer initStrBuffer = new StringBuffer();
			for (int count = startDepth; count < (splitStr.length - endDepth); count++){
				initStrBuffer.append(splitStr[count]);
				allTrace.add(initStrBuffer.toString());
				initStrBuffer.append(".");
			}
		}
		
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllLeafNodes()
	 */
	@Override
	public Set<String> getAllLeafNodes() {
		Set<String> allTrace =  new LinkedHashSet<String>();
		
		for (String traceStr:trace.values()){
			String[] splitStr = traceStr.split("\\.");
			allTrace.add(splitStr[splitStr.length - 1]);
		}
		
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllFirstNodes()
	 */
	@Override
	public Set<String> getAllFirstNodes() {
		Set<String> allTrace =  new LinkedHashSet<String>();
		
		for (String traceStr:trace.values()){
			String[] splitStr = traceStr.split("\\.");
			allTrace.add(splitStr[0]);
		}
		
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getAllTraceExcludeLeafNode()
	 */
	@Override
	public Set<String> getAllTraceExcludeLeafNode() {
		Set<String> allTrace =  new LinkedHashSet<String>();
		
		for (String traceStr:trace.values()){
			String[] splitStr = traceStr.split("\\.");
			StringBuffer initStrBuffer = new StringBuffer();
			for (int count = 0; count < (splitStr.length - 1); count++){
				initStrBuffer.append(splitStr[count]);
				allTrace.add(initStrBuffer.toString());
				initStrBuffer.append(".");
			}
		}
		
		return allTrace;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.util.DtoToDomainObjectTrace#getLeafToTraceMap()
	 */
	@Override
	public Map<String, String> getLeafToTraceMap() {
		HashMap<String,String> leafToTraceMap = new HashMap<String,String>();
		
		for (String traceStr:trace.values()){
			if (traceStr.contains(".")){
				leafToTraceMap.put(traceStr.substring(traceStr.lastIndexOf(".")).split("\\.")[1], traceStr.substring(0, traceStr.lastIndexOf(".")));
			} else {
				leafToTraceMap.put(traceStr, traceStr);
			}
		}
		
		return leafToTraceMap;
	}
	
	@Override
	public String getTrace(String dtoObjectName) {
		return trace.get(dtoObjectName);
	}
	
	@Override
	public String getTraceExcludeLeafNode(String dtoObjectName) {
		String traceExcludeLeafNode = null;
		String traceNodes = getTrace(dtoObjectName);
		traceExcludeLeafNode = (traceNodes != null && traceNodes.contains(".")) ? traceNodes.substring(0, traceNodes.lastIndexOf(".")) : traceNodes;
		
		return traceExcludeLeafNode;
	}

	@Override
	public String getFirstNode(String dtoObjectName) {
		String firstNode = null;
		String traceNodes = getTrace(dtoObjectName);
		firstNode = (traceNodes != null && traceNodes.contains(".")) ? traceNodes.split("\\.")[0] : traceNodes;
		
		return firstNode;
	}

	@Override
	public String getLastNode(String dtoObjectName) {
		String traceExcludeLeafNode = null;
		String traceNodes = getTrace(dtoObjectName);
		traceExcludeLeafNode = (traceNodes != null && traceNodes.contains(".")) ? traceNodes.substring(traceNodes.lastIndexOf(".")).split("\\.")[1] : traceNodes;	
		return traceExcludeLeafNode;
	}

	
	protected void createTrace(Class<?> rootClass) {
		ObjectTracer oTracer = new ObjectTracer();
		oTracer.addHandler("^[a-z].*LkpValue", new LksDataValueTracerHandler());
		oTracer.addHandler("^[a-z].*LkuValue", new LkuDataValueTracerHandler());
		oTracer.addHandler("^[a-z].*LkpValueCode", new LksDataValueCodeTracerHandler());
		oTracer.addHandler("^[a-z].*LkuValueCode", new LkuDataValueCodeTracerHandler());
		oTracer.addHandler("itemAdminUser", new UserIdTracerHandler());
		
		Field[] dtoFields = ItemSearchResultDTOImpl.class.getDeclaredFields();
		for (int i = 0; i < dtoFields.length; i++){
			oTracer.traceObject(rootClass, dtoFields[i].getName());
			List<Field> fields = oTracer.getObjectTrace();
			if (!oTracer.toString().isEmpty()){
				trace.put(dtoFields[i].getName(), oTracer.toString());
				traceFields.put(dtoFields[i].getName(), fields);
			}
		}
	}



}
