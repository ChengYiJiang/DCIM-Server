/**
 * 
 */
package com.raritan.tdz.unit.tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;

import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.ModelDetails;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.ConnectorLookupFinderDAO;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.lookup.dao.UserLookupFinderDAO;
import com.raritan.tdz.model.dao.ModelDAO;
import com.raritan.tdz.unit.item.ItemClass;
import com.raritan.tdz.unit.item.ItemSubClass;

/**
 * @author prasanna
 *
 */
public class SystemLookupInitUnitTest {
	
	@Autowired
	protected Mockery jmockContext;
	  
	@Autowired
	private SystemLookupFinderDAO systemLookupFinderDAO;
	 
	@Autowired
	private ModelDAO modelDAO;
	
	@Autowired
	ConnectorLookupFinderDAO connectorLookupDAO;
	
	@Autowired
	UserLookupFinderDAO userLookupFinderDAO;
	
	@Autowired
	ItemDAO itemDAO;

	private long dbId = new Random().nextInt(1000);
	 
	private HashMap<Long, LksData> systemLookupMap = new HashMap<Long, LksData>();


	protected long getNextId(Long lkpValueCode){
		
		if(lkpValueCode == null) lkpValueCode = 0L;
		
		switch(lkpValueCode.intValue()){
		case (int) SystemLookup.Class.DEVICE:
			return ItemClass.DeviceId;
		case (int) SystemLookup.Class.NETWORK:
			return ItemClass.NetworkId;
		case (int) SystemLookup.Class.CABINET:
			return ItemClass.CabinetId;
		case (int) SystemLookup.Class.CRAC:
			return ItemClass.CabinetId;
		case (int) SystemLookup.Class.DATA_PANEL:
			return ItemClass.DataPanelId;
		case (int) SystemLookup.Class.FLOOR_PDU:
			return ItemClass.FloorPduId;
		case (int) SystemLookup.Class.FLOOR_OUTLET:
			return ItemClass.PowerOutletId;
		case (int) SystemLookup.Class.PROBE:
			return ItemClass.ProbeId;
		case (int) SystemLookup.Class.RACK_PDU:
			return ItemClass.RackPduId;
		case (int) SystemLookup.Class.UPS:
			return ItemClass.UpsId;
			//SubClass
		case (int) SystemLookup.SubClass.BLADE:
			return ItemSubClass.Blade;
		case (int) SystemLookup.SubClass.BLADE_CHASSIS:
			return ItemSubClass.BladeChassis;
		case (int) SystemLookup.SubClass.BLADE_SERVER:
			return ItemSubClass.BladeServer;
		case (int) SystemLookup.SubClass.BLANKING_PLATE:
			return ItemSubClass.BlankingPlate;
		case (int) SystemLookup.SubClass.BUSWAY:
			return ItemSubClass.Busway;
		case (int) SystemLookup.SubClass.BUSWAY_OUTLET:
			return ItemSubClass.BuswayOutlet;
		case (int) SystemLookup.SubClass.CHASSIS:
			return ItemSubClass.Chassis;
		case (int) SystemLookup.SubClass.CONTAINER:
			return ItemSubClass.Container;
		case (int) SystemLookup.SubClass.LOCAL:
			return ItemSubClass.Local;
		case (int) SystemLookup.SubClass.NETWORK_STACK:
			return ItemSubClass.NetworkStack;
		case (int) SystemLookup.SubClass.RACKABLE:
			return ItemSubClass.Standard;
		case (int) SystemLookup.SubClass.REMOTE:
			return ItemSubClass.Remote;
		case (int) SystemLookup.SubClass.SHELF:
			return ItemSubClass.Shelf;
		case (int) SystemLookup.SubClass.VIRTUAL_MACHINE:
			return ItemSubClass.VM;
		case (int) SystemLookup.SubClass.WHIP_OUTLET:
			return ItemSubClass.WhipOutlet;	
		
		default:
			return dbId++;
		}		
		  
	}

	public void init(){
		//initLookups();
		loadSysLookupFromFile();
		loadUserLookupFromFile();
		loadModelFromFile();
		loadConnectorFromFile();
		loadItemDelete();
	}
	
	@SuppressWarnings({ "serial", "unchecked" })
	private void loadItemDelete() {
		
		final List<Long> emptyList = new ArrayList<Long>() {};
		
		jmockContext.checking(new Expectations() {{
			allowing(itemDAO).getItemIdsToDelete(with(any(List.class))); will(returnValue(emptyList));
			allowing(itemDAO).getPanelItemIdsToDelete(with(any(List.class))); will(returnValue(emptyList));
			allowing(itemDAO).getBuswayPowerPanelConnectedPowerOutlet(with(any(List.class))); will(returnValue(emptyList));
			allowing(itemDAO).getLocalPowerPanelConnectedPowerOutlet(with(any(List.class))); will(returnValue(emptyList));
			allowing(itemDAO).getRemotePowerPanelConnectedPowerOutlet(with(any(List.class))); will(returnValue(emptyList));
			allowing(itemDAO).clearPowerOutletAssociationWithPanel(with(any(List.class))); will(returnValue(emptyList));
		}});
		
	}

	public Long getLksId(Long lkpValueCode){
		LksData lksData =  systemLookupMap.get(lkpValueCode);
		if (lksData != null)
			return lksData.getLksId();
		else
			return null;
	}
	
	public LksData getLks(Long lkpValueCode){
		LksData lksData =  systemLookupMap.get(lkpValueCode);
		return lksData;
	}
	
	 private void initLookups() {
			initSystemLookup(SystemLookup.Class.class,null);
			initSystemLookup(SystemLookup.SubClass.class,null);
			initSystemLookup(SystemLookup.ItemStatus.class,null);
			List<Long> portSubClassIgnoreList = new ArrayList<Long>(){{ add(SystemLookup.PortSubClass.BRANCH_CIRCUIT_BREAKER); }};
			initSystemLookup(SystemLookup.PortSubClass.class,null);
			initSystemLookup(SystemLookup.LinkType.class,null);
			initSystemLookup(SystemLookup.PhaseIdClass.class,null);
			List<Long> portVoltsIgnoreList = new ArrayList<Long>(){{ add(SystemLookup.VoltClass.V_120_240); }};
			initSystemLookupVolts(SystemLookup.VoltClass.class,portVoltsIgnoreList);
			initSystemLookup(SystemLookup.RequestStage.class,null);
			initSystemLookup(SystemLookup.MediaType.class,null);
		}
	  
	  private void initSystemLookup(
			Class<?> systemLookupStaticClass, List<Long> ignoreList) {
		
		  for (Field field: systemLookupStaticClass.getDeclaredFields()){
			  try {
				if (field.getType().equals(long.class)){
					final Long lkpValueCode = field.getLong(null);
					if (ignoreList !=null && ignoreList.contains(lkpValueCode)) continue;
					final LksData lks = new LksData();
						lks.setLkpValueCode(lkpValueCode);
						lks.setLksId(getNextId(lkpValueCode));
						lks.setLkpValue(getLkpValue(field));
						systemLookupMap.put(lkpValueCode, lks);
						final List<LksData> lksList = new ArrayList<LksData>(){{ add(lks);}};
						Expectations expectations = new Expectations();
						expectations.allowing(systemLookupFinderDAO).findByLkpValueCode(expectations.with(lkpValueCode)); expectations.will(expectations.returnValue(lksList));
						jmockContext.checking(expectations);
					
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
	  
	  private void initSystemLookupVolts(
			Class<?> systemLookupStaticClass, List<Long> ignoreList) {
		
		  for (Field field: systemLookupStaticClass.getDeclaredFields()){
			  try {
				if (field.getType().equals(long.class)){
					final Long lkpValueCode = field.getLong(null);
					if (ignoreList !=null && ignoreList.contains(lkpValueCode)) continue;
					final LksData lks = new LksData();
						lks.setLkpValueCode(lkpValueCode);
						lks.setLksId(getNextId(lkpValueCode));
						String lkpValueWithV = getLkpValue(field);
						lks.setLkpValue(lkpValueWithV.substring(lkpValueWithV.lastIndexOf(" ")).trim());
						systemLookupMap.put(lkpValueCode, lks);
						final List<LksData> lksList = new ArrayList<LksData>(){{ add(lks);}};
						Expectations expectations = new Expectations();
						expectations.allowing(systemLookupFinderDAO).findByLkpValueCode(expectations.with(lkpValueCode)); expectations.will(expectations.returnValue(lksList));
						jmockContext.checking(expectations);
					
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
	  
	  private String getLkpValue(Field field){
		  String fieldName = field.getName();
		  
		  return toCamelCase(fieldName);
	  }
	  
	  static String toCamelCase(String s){
		  String[] parts = s.split("_");
		  StringBuilder camelCaseString = new StringBuilder();
		  for (String part : parts){
			  camelCaseString.append(toProperCase(part));
			  camelCaseString.append(" ");
		  }
		  
		  String retVal = camelCaseString.toString();
		  
		  return retVal.substring(0,retVal.lastIndexOf(" "));
	  }
	  
	  static String toProperCase(String s){
		  return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
	  }
	  
	  private void loadSysLookupFromFile(){
		  
		try {
		 BufferedReader reader = new BufferedReader(new FileReader("tests/resources/systemLookup.dat"));
		  String line = null;
		  
		  //lks_id|lkp_type_name|lkp_value_code|attribute|lkp_value|parent_lks_id|sort_order|old_id
		  line = reader.readLine();
		  
		  while ((line = reader.readLine()) != null) {
			  String[] data = line.split("\\|");
			  final LksData lks = new LksData();
			  
			  lks.setLksId(Long.valueOf(data[0]));
			  lks.setLkpTypeName(data[1]);
			  lks.setLkpValueCode(Long.valueOf(data[2]));
			  lks.setAttribute(data[3]);
			  lks.setLkpValue(data[4]);
				
			  systemLookupMap.put(lks.getLkpValueCode(), lks);
			  final List<LksData> lksList = new ArrayList<LksData>(){{ add(lks);}};
			  
			  jmockContext.checking(new Expectations() {{
					allowing(systemLookupFinderDAO).findByLkpValueCode(with(lks.getLkpValueCode())); will(returnValue(lksList));
					allowing(systemLookupFinderDAO).findByLkpValueAndType(with(lks.getLkpValue()), with(lks.getLkpTypeName())); will(returnValue(lksList));
				}});			  
		  }		  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }

	  private void loadUserLookupFromFile(){
		  
		try {
		 BufferedReader reader = new BufferedReader(new FileReader("tests/resources/userLookup.dat"));
		  String line = null;
		  String[] data = null;
		  //lku_id, lku_type_name, lku_attribute, lku_value, lks_id, sort_order, old_id
		  line = reader.readLine();
		  
		  while ((line = reader.readLine()) != null) {
			  data = line.split("\\|");
			  
			  final LkuData rec = new LkuData();			  
			  rec.setLkuId(Long.valueOf(data[0]));
			  rec.setLkuTypeName(data[1]);
			  rec.setLkuAttribute(data[2]);
			  
			  rec.setLkuValue(data[3]);
				
			  final List<LkuData> recList = new ArrayList<LkuData>(){{ add(rec);}};
			  
			  //System.out.println(line);
			  
			  jmockContext.checking(new Expectations() {{
					allowing(userLookupFinderDAO).findById(with(rec.getLkuId())); will(returnValue(recList));
					allowing(userLookupFinderDAO).findByLkpValueAndTypeCaseInsensitive(with(rec.getLkuValue()), with(rec.getLkuTypeName())); will(returnValue(recList));
				}});
				
		  }		  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  	  
	  private void loadModelFromFile(){		  
		try {
		  BufferedReader reader = new BufferedReader(new FileReader("tests/resources/models.dat"));
		  String line = null;
		  
		  //model_id|model_name|sys_model_mfr_name|lkp_value_code|mounting|form_factor|ru_height
		  line = reader.readLine();
		  
		  while ((line = reader.readLine()) != null) {
			  String[] data = line.split("\\|");
			  final ModelDetails rec = new ModelDetails();
			  
			  rec.setModelDetailId(Long.valueOf(data[0]));
			  rec.setModelName(data[1]);
			  rec.setSysModelMfrName(data[2]);
			  
			  LksData classLks = systemLookupFinderDAO.findByLkpValueCode(Long.valueOf(data[3])).get(0);
			  rec.setClassLookup(classLks);
			  
			  rec.setMounting(data[4]);
			  rec.setFormFactor(data[5]);
			  rec.setRuHeight(Integer.parseInt(data[6]));
				
			  //final List<ModelDetails> recList = new ArrayList<ModelDetails>(){{ add(rec);}};
			  
			  Expectations expectations = new Expectations();
			  expectations.allowing(modelDAO).read(expectations.with(rec.getModelDetailId())); expectations.will(expectations.returnValue(rec));
			  jmockContext.checking(expectations);
		  }		  
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  
	  private void loadConnectorFromFile(){		  
			try {
			  BufferedReader reader = new BufferedReader(new FileReader("tests/resources/connectors.dat"));
			  String line = null;
			  
			  //lku_connector_id, lku_type_name, connector_name, attribute, description
			  line = reader.readLine();
			  
			  while ((line = reader.readLine()) != null) {
				  	String[] data = line.split("\\|");
				  
					final ConnectorLkuData rec = new ConnectorLkuData();
					rec.setConnectorId(Long.valueOf(data[0]));
					rec.setTypeName(data[1]);
					rec.setConnectorName(data[2]);
					
					if(data.length > 3){
						rec.setAttribute(data[3]);
					}
					
					final List<ConnectorLkuData> recList = new ArrayList<ConnectorLkuData>(){{ add(rec);}};
					
					jmockContext.checking(new Expectations() {{
						allowing(connectorLookupDAO).findByNameCaseInsensitive(with(rec.getConnectorName())); will(returnValue(recList));
						allowing(connectorLookupDAO).findByName(with(rec.getConnectorName())); will(returnValue(recList));
						allowing(connectorLookupDAO).findById(with(rec.getConnectorId())); will(returnValue(recList));
					}});
			  }		  
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }	  
}
