/**
 * 
 */
package com.raritan.tdz.circuit.home;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.Assert;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.validation.Errors;


import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.validators.EnoughPowerValidator;
import com.raritan.tdz.circuit.validators.PowerCircuitValidator;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.tests.TestBase;

/**
 * @author prasanna
 *
 */
public class PowerProcTests extends TestBase{
	
	private PowerProc powerProcHome = null;
	private SessionFactory sessionFactory = null;
	private EnoughPowerValidator enoughPowerValidator = null;
	private PowerCircuitDAO powerCircuitDAO = null;
	private PowerCircuitValidator powerCircuitValidator = null;
	
	/**
	 * @throws Throwable 
	 */
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		
		sessionFactory = (SessionFactory)ctx.getBean("sessionFactory");
		powerProcHome = (PowerProc)ctx.getBean("powerProc");
		enoughPowerValidator = (EnoughPowerValidator)ctx.getBean("enoughPowerValidatorAtUPS");
		powerCircuitDAO = (PowerCircuitDAO)ctx.getBean("powerCircuitDAO");
		powerCircuitValidator = (PowerCircuitValidator)ctx.getBean("powerCircuitValidator");
		log = Logger.getLogger(PowerProc.class);
	}

	
	@Test
	public void testIsEnoughPowerOnUPSExceeds() throws Throwable {
		Errors errors = getErrorObject(PowerConnection.class);
		enoughPowerValidator.checkEnoughPower(0.0, 460000, 1, null, 3004L, null, null, 0, null, errors, null);
		assertTrue(errors.hasErrors());
	}
	
	@Test
	public void testIsEnoughPowerOnUPS() throws Throwable {
		Errors errors = getErrorObject(PowerConnection.class);
		enoughPowerValidator.checkEnoughPower(0.0, 150, 1, null, 3004L, null, null, 0, null, errors, null);
		assertFalse(errors.hasErrors());
	}
	
	@Test
	public void testPowerCircuitValidationStartPS() throws Throwable {
		//Server-01/PS-1/SiteC demo database
		PowerCircuit cir = powerCircuitDAO.getPowerCircuit(12024L);
		Errors errors = getErrorObject(PowerCircuit.class);
		powerCircuitValidator.validate(cir, errors);
		assertFalse(errors.hasErrors());
		
	}
	
	@Test
	public void testValidatePowerCircuit(){
		Session session = null;
		List<PowerCircuit> circuitList = null;
		
		if (sessionFactory != null){
			session = sessionFactory.getCurrentSession();
			
			Criteria pcCriteria = session.createCriteria(PowerCircuit.class);
			circuitList = pcCriteria.list();
			
			for (PowerCircuit circuit:circuitList){
				PowerConnection sConn = circuit.getStartConnection();
				PowerConnection eConn = circuit.getEndConnection();
				
				String connection = null;
				if (sConn.getSourcePowerPort() != null){
					Item item = sConn.getSourcePowerPort().getItem();
					connection = item != null ? item.getItemName() : " ";
				}
				
				if (sConn.getDestPowerPort() != null){
					Item item = sConn.getDestPowerPort().getItem();
					if (item != null){
						connection += " --> "
									+ item.getItemName();
					} else {
						connection += " --> ";
					}
				}
				
				
				log.debug("Start Connection: " + connection);
				
				if (eConn.getSourcePowerPort() != null){
					Item item = eConn.getSourcePowerPort().getItem();
					connection = item != null ? item.getItemName() : " ";
				}
				
				if (eConn.getDestPowerPort() != null){
					Item item = eConn.getDestPowerPort().getItem();
					if (item != null){
						connection += " --> "
									+ item.getItemName();
					} else {
						connection += " --> ";
					}
				}
				
				log.debug("End Connection: " + connection);
				
				try {
					powerProcHome.validatePowerCircuit(circuit);
				} catch (DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	private List<PowerConnection> getCircuitConnections(PowerCircuit powerCircuit, PowerProc powerProc) {
		List<PowerConnection> powerConnections = new ArrayList<PowerConnection>();

		//Load connection records								
		for(long connectionId:powerCircuit.getConnListFromTrace()){
			PowerConnection powerConnection = (PowerConnection)session.get(PowerConnection.class, connectionId);

			if(powerConnection != null){
				powerProc.lazyLoadPort(powerConnection.getSourcePowerPort());
				powerProc.lazyLoadPort(powerConnection.getDestPowerPort());

				powerConnections.add(powerConnection);
			}					
		}
		return powerConnections;
	}
	
	private PowerPort getPowerPort(Long portId){
		PowerPort port = (PowerPort)session.get(PowerPort.class, portId);
		
		return port;
	}

}
