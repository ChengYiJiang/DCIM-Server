/**
 * 
 */
package com.raritan.tdz.unit.item.validators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.home.CircuitPDHome;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.dao.ItemFinderDAO;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.validators.ItemPortAssociationValidator;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class ItemPortAssociationValidatorTest extends UnitTestBase {
	  private Errors errors;
	  
	  @Autowired
	  private UnitTestDatabaseIdGenerator unitTestIdGenerator;
	  
	  @Autowired
	  private ItemPortAssociationValidator<DataPort> itemPortAssociationValidator;
	  
	  @Autowired
	  private DataPortDAO dataPortDAO;
	  
	  @Autowired
	  private ItemDAO itemDAO;
	  
	  @BeforeMethod
	  public void beforeMethod() {
		  errors = getErrorObject(CircuitPDHome.class);
		  
	  }
	  
	  @Test(expectedExceptions={IllegalArgumentException.class})
	  public void testInvalidArgs(){
		  Item item = new Item();
		  itemPortAssociationValidator.validate(item, errors);
	  }
	  
	  @Test
	  public void testValidPortAssociationWithItem(){
		  final Long itemId = unitTestIdGenerator.nextId();
		  final Long portId = unitTestIdGenerator.nextId();
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(item);
		  
		  item.addDataPort(dataPort);
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId)); will(returnValue(dataPort));
			}});
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId);
		  itemPortAssociationValidator.validate(targetMap, errors);
		  Assert.assertFalse(errors.hasErrors());
	  }
	  
	  @Test
	  public void testInvalidPortAssociationWithItem(){
		  final Long itemId = unitTestIdGenerator.nextId();
		  final Long portId = unitTestIdGenerator.nextId();
		  
		  final Item item = new Item();
		  item.setItemId(itemId);
		  
		  final DataPort dataPort = new DataPort();
		  dataPort.setPortId(portId);
		  dataPort.setItem(null);
		  
		  //item.addDataPort(dataPort);
		  
			jmockContext.checking(new Expectations() {{
				allowing(itemDAO).getItem(with(itemId)); will(returnValue(item));
				allowing(dataPortDAO).read(with(portId)); will(returnValue(dataPort));
			}});
		  
		  Map<String,Object> targetMap = getTargetMap(portId,itemId);
		  itemPortAssociationValidator.validate(targetMap, errors);
		  Assert.assertTrue(errors.hasErrors());
	  }
	  
	  Map<String,Object> getTargetMap(Long portId, Long itemId){
		  Map<String,Object> targetMap = new HashMap<String, Object>();
		  targetMap.put("portId",portId);
		  targetMap.put("itemId",itemId);
		  return targetMap;
	  }
}
