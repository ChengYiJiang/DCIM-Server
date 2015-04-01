package com.raritan.tdz.circuit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.raritan.tdz.circuit.dto.WireNodeInterface;
import com.raritan.tdz.domain.CircuitItemViewData;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.ConnectorCompat;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.dto.PortConnectorCompatListDTO;
import com.raritan.tdz.dto.PortConnectorDTO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.util.UnitConverterHelperImpl;
import com.raritan.tdz.util.UnitConverterImpl;
import com.raritan.tdz.views.ItemObject;

public class CircuitProc {
	public static ItemObject newItemObject(CircuitItemViewData item){
		ItemObject rec = new ItemObject();
		
		rec.setClassLksCode(item.getClassLkpValueCode());
		rec.setUPosition(item.getUPosition());
		rec.setSlotPosition(item.getSlotPosition());
		rec.setClassLksDesc(item.getClassLkpValue());
		rec.setItemId(item.getItemId());
		rec.setItemName(item.getItemName());		
		rec.setParentItemId(item.getParentItemId());
		rec.setParentItemName(item.getParentItemName());			
		rec.setStatusLksDesc(item.getStatusLkpValue());
		rec.setStatusLksCode(item.getStatusLkpValueCode());
		rec.setLocationId(item.getLocationId());
		rec.setLocationName(item.getDcName());
		rec.setLocationCode(item.getCode());
		rec.setModelId(item.getModelId());
		rec.setMake(item.getSysModelMfrName());
		rec.setModelName(item.getModelName());
		rec.setRuHeight(item.getRuHeight());
		rec.setMountedRailsPosDesc(item.getMountedrailsLkpValue());						
		rec.setSubClassLksDesc(item.getSubclassLkpValue());
		rec.setSubClassLksCode(item.getSubclassLkpValueCode());
		rec.setChassisId(item.getChassisId());
		rec.setChassisItemName(item.getChassisItemName());
		rec.setVmClusterId(item.getVmClusterLkuId());
		rec.setVmClusterName(item.getVmClusterName());
		rec.setPsRedundancy(item.getPsredundancy());
		rec.setRedundancy(item.getRedundancy());
		rec.setOriginLkpValueCode(item.getOriginLkpValueCode());
		
		if(rec.getSubClassLksCode() != null){
			rec.setItemClassFilter(rec.getClassLksCode() + "-" + rec.getSubClassLksCode());
			long subclass = rec.getSubClassLksCode();
			
			if(subclass == SystemLookup.SubClass.BLADE_CHASSIS || subclass == SystemLookup.SubClass.CHASSIS){
				rec.setChassisId(item.getItemId());
			}
		}
		else{
			rec.setItemClassFilter(rec.getClassLksCode().toString());
		}
		
		rec.setFrontImage(item.getFrontImage());
		rec.setBackImage(item.getRearImage());
		rec.setPolesQty(item.getPolesQty());
		rec.setRatingKva(item.getRatingKva());
		rec.setRatingKW(item.getRatingKw());
		rec.setRatingV(item.getRatingV());
		rec.setLineVolts(item.getLineVolts());
		rec.setPhaseVolts(item.getPhaseVolts());
		rec.setPhaseLksDesc(item.getPhaseLksDesc());		
		
		rec.setPowerPortCountFree(item.getFreePowerPortCount().longValue());			
		rec.setDataPortCountFree(item.getFreeDataPortCount().longValue());
		rec.setInputCordCountFree(item.getFreeInputCordCount().longValue());
		rec.setDataPortCount(0L);
		
		return rec;
	}
		
	public static PortConnectorDTO newPortConnectorDTO(ConnectorLkuData connector){
		if(connector == null){
			return null;
		}
		
		PortConnectorDTO pc = new PortConnectorDTO();		
		pc.setAttribute(connector.getAttribute());
		pc.setConnectorId(connector.getConnectorId());
		pc.setConnectorName(connector.getConnectorName());
		pc.setDescription(connector.getDescription());
		pc.setImagePath(connector.getImagePath());
		pc.setTypeName(connector.getTypeName());
				
		HashMap<Long, ConnectorCompat> tempList = new HashMap<Long, ConnectorCompat>();
		
		if(connector.getConnCompatList() != null && connector.getConnCompatList().size() > 0){
			for(ConnectorCompat cc:connector.getConnCompatList()){
				tempList.put(cc.getConnectorCompatId(), cc);
			}
		}
		
		if(connector.getConnCompat2List() != null && connector.getConnCompat2List().size() > 0){
			for(ConnectorCompat cc:connector.getConnCompat2List()){
				tempList.put(cc.getConnectorCompatId(), cc);
			}
		}
		
        if(tempList != null){	
        	List<PortConnectorCompatListDTO> compatList = new ArrayList<PortConnectorCompatListDTO>();
        	
        	com.raritan.tdz.domain.ConnectorLkuData lku;
        	
        	for(ConnectorCompat cc:tempList.values()){
        		PortConnectorCompatListDTO obj = new PortConnectorCompatListDTO();
        		lku = cc.getConnectorLookup();
        		
        		if(lku.getConnectorId() == connector.getConnectorId()){
        			lku = cc.getConnector2Lookup();
        		}
        		
        		obj.setConnectorCompatId(cc.getConnectorCompatId());        		
        		obj.setConnectorName(lku.getConnectorName());
        		obj.setConnectorId(lku.getConnectorId());
        		compatList.add(obj);
        	}
        	 
        	pc.setConnCompatList(compatList);
        	/*
        	System.out.println("Main Connector: " + connector.getConnectorName());
        	
        	for(PortConnectorCompatListDTO c:compatList){
        		System.out.println("\tCompat Connector: " + c.getConnectorName());
        	}*/
        }
        
        return pc;
	}
	
	public static ConnectionCord newConnCord(WireNodeInterface c, UserInfo userInfo){
		if(c == null){
			return null;
		}
		
		if(c.getCordLabel() == null && c.getCordLength() < 1 
				&& (c.getCordLkuId() == null || c.getCordLkuId() == 0) 
				&& (c.getCordColor() == null || c.getCordColor() == 0)){
			return null;
		}
		
		ConnectionCord cord = new ConnectionCord();	
		
		if(c.getCordId() != null){
			cord.setCordId(c.getCordId());
		}
		
		// normalize here
		Integer length = c.getCordLength();
		if (null != userInfo) {
			UnitConverterImpl lenghtConverter = new UnitConverterImpl(UnitConverterImpl.FEET_TO_METER);
			lenghtConverter.setUnitConverterHelper(new UnitConverterHelperImpl());
			Object len = lenghtConverter.normalize(new Integer(c.getCordLength()), ((userInfo != null) ? userInfo.getUnits() : "1"));
			if (len instanceof Double) {
				length = ((Double) len).intValue();
			}
			else if (len instanceof Integer) {
				length = (Integer) len;
			}
			else if (len instanceof Float) {
				length = ((Float) len).intValue();
			}
		}
		cord.setCordLabel(c.getCordLabel());
		cord.setCordLength(length);
		
		if(c.getCordColor() != null && c.getCordColor() > 0){
			cord.setColorLookup(new LkuData(c.getCordColor()));
		}
		
		if(c.getCordLkuId() != null && c.getCordLkuId() > 0){
			cord.setCordLookup(new LkuData(c.getCordLkuId()));
		}
		
		return cord;
	}		
}
