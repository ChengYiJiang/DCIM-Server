/**
 * 
 */
package com.raritan.tdz.unit.circuit.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.validators.PowerCircuitValidator;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.ItItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.powerchain.home.PowerChainLookup.ConnectorLookup;
import com.raritan.tdz.unit.tests.SystemLookupInitUnitTest;
import com.raritan.tdz.unit.tests.UnitTestBase;
import com.raritan.tdz.unit.tests.UnitTestDatabaseIdGenerator;

/**
 * @author prasanna
 *
 */
public class PowerCircuitValidatorTest extends UnitTestBase {

	  @Autowired(required=true)
	  private PowerCircuitValidator powerCircuitValidator;
	  
	  @Autowired(required=true)
	  private ItemDAO itemDAO;
	  
	  @Autowired(required=true)
	  private SystemLookupInitUnitTest systemLookupUnitTest;
	  
	  @Autowired
	  private UnitTestDatabaseIdGenerator idGenerator;
	  
	  @Autowired
	  PowerPortDAO powerPortDAO;
	  
	  @Autowired
	  PowerCircuitDAO powerCircuitDAO;
	  
	  @Autowired
	  PowerConnDAO powerConnDAO;
	  
	  @Test
	  public void testValidCircuitWithEnoughPower(){
		  
	  }
	  
	  @Test
	  public void testValidCircuitWithNotEnoughPower(){
		  
	  }
	  
	  @Test
	  public void testAddNewFloorPDUToChain(){
		  
	  }
	  
	  @Test
	  public void testAddExistingFloorPDUToChain(){
		  
	  }
	  
	  private PowerCircuit getValidPowerCircuit(){
		  Long powerCircuitId = idGenerator.nextId();
		  PowerCircuit powerCircuit = new PowerCircuit();
		  return powerCircuit;
	  }
	  
	  private PowerCircuit getValidPartialCircuit(){
		  PowerCircuit powerCircuit = new PowerCircuit();
		  return powerCircuit;
	  }
	  
	  private ItItem getItItem(long classLkpValueCode, long subclassLkpValueCode){
		  long itemId = idGenerator.nextId();
		  ItItem itItem = new ItItem();
		  itItem.setItemId(itemId);
		  itItem.setClassLookup(systemLookupUnitTest.getLks(classLkpValueCode));
		  if (subclassLkpValueCode > 0)
			  itItem.setSubclassLookup(systemLookupUnitTest.getLks(subclassLkpValueCode));
		  itItem.setStatusLookup(systemLookupUnitTest.getLks(SystemLookup.ItemStatus.PLANNED));
		  return itItem;
	  }
	  
	  private MeItem getMeItem(long classLkpValueCode, long subclassLkpValueCode){
		  long itemId = idGenerator.nextId();
		  MeItem meItem = new MeItem();
		  meItem.setItemId(itemId);
		  meItem.setClassLookup(systemLookupUnitTest.getLks(classLkpValueCode));
		  if (subclassLkpValueCode > 0)
			  meItem.setSubclassLookup(systemLookupUnitTest.getLks(subclassLkpValueCode));
		  meItem.setStatusLookup(systemLookupUnitTest.getLks(SystemLookup.ItemStatus.PLANNED));
		  return meItem;
	  }
	  
	  private PowerPort getPowerPort(long portSubClassLkpValueCode, Item item){
		  long portId = idGenerator.nextId();
		  PowerPort powerPort = new PowerPort();
		  powerPort.setPortId(portId);
		  powerPort.setPortSubClassLookup(systemLookupUnitTest.getLks(portSubClassLkpValueCode));
		  powerPort.setItem(item);
		  
		  ConnectorLkuData connectorLookup = new ConnectorLkuData();
		  long connectorLkuId = 1L;
		  powerPort.setConnectorLookup(connectorLookup);
		  
		  return powerPort;
	  }
	  
	  private PowerConnection getPowerConnection(PowerPort srcPort, PowerPort dstPort){
		  long powerConnectionId = idGenerator.nextId();
		  PowerConnection pwrConnection = new PowerConnection();
		  pwrConnection.setConnectionId(powerConnectionId);
		  pwrConnection.setSourcePowerPort(srcPort);
		  pwrConnection.setDestPowerPort(dstPort);
		  return pwrConnection;
	  }
}
