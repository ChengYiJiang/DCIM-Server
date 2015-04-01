/**
 * 
 */
package com.raritan.tdz.unit.item.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.mapping.IdGenerator;
import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.item.validators.BladeWithConnectionsLocationValidator;
import com.raritan.tdz.item.validators.ItemWithConnectionsLocationValidator;
import com.raritan.tdz.item.validators.PowerOutletWithConnectionsLocationValidator;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.dao.DataPortFinderDAO;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;
import com.raritan.tdz.port.dao.SensorPortFinderDAO;
import com.raritan.tdz.port.validators.ItemPortAssociationValidator;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class ItItemWithConnectionsLocationValidatorTest extends UnitTestBase {
	  private Errors errors;
	  
	  @Autowired
	  private UnitTestDatabaseIdGenerator unitTestIdGenerator;
	  
	  @Autowired
	  private ItemPortAssociationValidator<DataPort> itemPortAssociationValidator;
	  
	  @Autowired
	  private DataPortFinderDAO dataPortDAO;
	  
	  @Autowired
	  PowerPortFinderDAO powerPortDAO;
		
	  @Autowired
	  SensorPortFinderDAO sensorPortDAO;
	  
	  @Autowired
	  private ItemDAO itemDAO;
	  
	  @Autowired
	  private ItemWithConnectionsLocationValidator itItemWithConnectionsValidator;
	  
	  @Autowired
	  private BladeWithConnectionsLocationValidator bladeItemWithConnectionsValidator;
	  
	  @Autowired
	  private PowerOutletWithConnectionsLocationValidator powerOutletWithConnectionsValidator;
	  
	  @BeforeMethod
	  public void beforeMethod() {
		  errors = getErrorObject(IPortInfo.class);
		  
	  }
	
	  @Test
	  public void testUnusedPortsOnItItem(){
		  final ItItem item = createItItemWithUnusedPorts();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		 // dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		//  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		 // sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertFalse(errors.hasErrors());
	  }
	  
	  @Test
	  public void testUsedDataPortsOnItItem(){
		  final ItItem item = createItItemWithUnusedPorts();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		  dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		//  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		 // sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  
	  @Test
	  public void testUsedPowerPortsOnItItem(){
		  final ItItem item = createItItemWithUnusedPorts();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		 // dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		 // sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  @Test
	  public void testUsedSensorPortsOnItItem(){
		  final ItItem item = createItItemWithUnusedPorts();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		 // dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		 // powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		  sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  @Test
	  public void testUsedAllTypeOfPortsOnItItem(){
		  final ItItem item = createItItemWithUnusedPorts();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		  dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		  sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(item.getItemId())); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  @Test
	  public void testUsedAllTypeOfPortsOnItItemWithCabinetFilled(){
		  ItItem item = createItItemWithUnusedPorts();
		  item.setParentItem(new CabinetItem());
		  
		  final long itemId = item.getItemId();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		  dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		  sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(itemId)); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(itemId)); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(itemId)); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertTrue(errors.hasErrors());
		  for (ObjectError error:errors.getAllErrors()){
			  Assert.assertTrue((error.getArguments()[1]).toString().contains("U Position"));
		  }
	  }
	  
	  @Test
	  public void testUsedAllTypeOfPortsOnItItemWithUPositionFilled(){
		  ItItem item = createItItemWithUnusedPorts();
		  item.setuPosition(10);
		  
		  final long itemId = item.getItemId();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		  dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		  sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(itemId)); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(itemId)); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(itemId)); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertTrue(errors.hasErrors());
		  for (ObjectError error:errors.getAllErrors()){
			  Assert.assertTrue((error.getArguments()[1]).toString().contains("Cabinet"));
		  }
	  }
	  
	  @Test
	  public void testUsedAllTypeOfPortsOnItItemWithCabinetAndUPositionFilled(){
		  ItItem item = createItItemWithUnusedPorts();
		  item.setParentItem(new Item());
		  item.setuPosition(10);
		  
		  final long itemId = item.getItemId();
		  final List<DataPort> dataPorts = new ArrayList<DataPort>();
		  dataPorts.addAll(item.getDataPorts());
		  
		  final List<PowerPort> powerPorts = new ArrayList<PowerPort>();
		  powerPorts.addAll(item.getPowerPorts());
		  
		  final List<SensorPort> sensorPorts = new ArrayList<SensorPort>();
		  sensorPorts.addAll(item.getSensorPorts());
		  
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).findUsedPorts(with(itemId)); will(returnValue(dataPorts));
			  oneOf(powerPortDAO).findUsedPorts(with(itemId)); will(returnValue(powerPorts));
			  oneOf(sensorPortDAO).findUsedPorts(with(itemId)); will(returnValue(sensorPorts));
		  }});
		  
		  itItemWithConnectionsValidator.validate(getTargetMap(item), errors);
		  
		  Assert.assertFalse(errors.hasErrors());
	  }
	  
	  private ItItem createItItemWithUnusedPorts(){
		  final long itemId = unitTestIdGenerator.nextId();
		  final long dpPortId = unitTestIdGenerator.nextId();
		  final long ppPortId = unitTestIdGenerator.nextId();
		  final long spPortId = unitTestIdGenerator.nextId();
		  
		  ItItem itItem = new ItItem();
		  itItem.setItemId(itemId);
		  
		  DataPort dp = new DataPort();
		  dp.setPortId(dpPortId);
		  
		  PowerPort pp = new PowerPort();
		  pp.setPortId(ppPortId);
		  
		  SensorPort sp = new SensorPort();
		  sp.setPortId(spPortId);
		  
		  
		  itItem.addDataPort(dp);
		  itItem.addPowerPort(pp);
		  itItem.addSensorPort(sp);
		  
		  
	
		  
		  return itItem;
	  }
	  
	  private Item createItemWithUnusedPorts(){
		  final long itemId = unitTestIdGenerator.nextId();
		  final long dpPortId = unitTestIdGenerator.nextId();
		  final long ppPortId = unitTestIdGenerator.nextId();
		  final long spPortId = unitTestIdGenerator.nextId();
		  
		  Item item = new Item();
		  item.setItemId(itemId);
		  
		  DataPort dp = new DataPort();
		  dp.setPortId(dpPortId);
		  
		  PowerPort pp = new PowerPort();
		  pp.setPortId(ppPortId);
		  
		  SensorPort sp = new SensorPort();
		  sp.setPortId(spPortId);
		  
		  
		  item.addDataPort(dp);
		  item.addPowerPort(pp);
		  item.addSensorPort(sp);
		  
		  
		  return item;
	  }
	  
	  private Item createItemWithUsedDataPorts(){
		  Item item = createItItemWithUnusedPorts();
		  
		  Iterator<DataPort> dataPortIterator = item.getDataPorts().iterator();
		  Iterator<PowerPort> powerPortIterator = item.getPowerPorts().iterator();
		  Iterator<SensorPort> sensorPortIterator = item.getSensorPorts().iterator();
		  
		  while (dataPortIterator.hasNext()){
			  dataPortIterator.next().setUsed(true);
		  }
		  
		  return item;
	  }
	  
	  private Item createItemWithUsedPowerPorts(){
		  Item item = createItItemWithUnusedPorts();
		  
		  Iterator<DataPort> dataPortIterator = item.getDataPorts().iterator();
		  Iterator<PowerPort> powerPortIterator = item.getPowerPorts().iterator();
		  Iterator<SensorPort> sensorPortIterator = item.getSensorPorts().iterator();
		  
		  while (powerPortIterator.hasNext()){
			  powerPortIterator.next().setUsed(true);
		  }
		  
		  return item;
	  }
	  
	  private Item createItemWithUsedSensorPorts(){
		  Item item = createItItemWithUnusedPorts();
		  
		  Iterator<DataPort> dataPortIterator = item.getDataPorts().iterator();
		  Iterator<PowerPort> powerPortIterator = item.getPowerPorts().iterator();
		  Iterator<SensorPort> sensorPortIterator = item.getSensorPorts().iterator();
		  
		  while (sensorPortIterator.hasNext()){
			  sensorPortIterator.next().setUsed(true);
		  }
		  
		  return item;
	  }
	  
	  private Map<String,Object> getTargetMap(Item item){
		  Map<String,Object> targetMap = new HashMap<String, Object>();
		  targetMap.put(errors.getObjectName(),item);
		  return targetMap;
	  }
}
