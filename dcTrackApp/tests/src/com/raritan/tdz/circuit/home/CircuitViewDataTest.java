package com.raritan.tdz.circuit.home;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.PowerWattUsedSummary;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

/**
 * @author Santo Rosario
 */
public class CircuitViewDataTest extends TestBase {

	
	private CircuitPDHome home;
	private CircuitPDService service;
	

	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		home = (CircuitPDHome)ctx.getBean("circuitPDHome");
		service = (CircuitPDService)ctx.getBean("circuitPDService");
	}
	
	@AfterMethod
	public void tearDown() throws Throwable {
		super.tearDown();
	}


	@Test
	public final void testGetCircuitList() throws Throwable {
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitType(30000L);
		
		int count = 0;
		
		for(CircuitViewData cir:home.viewCircuitPDList(cCriteria)){
			System.out.println(cir.getCircuitTrace());
			count++;
			
			if(count > 10) break;
		}
		count = 0;
		
		cCriteria.setCircuitType(20000L);
		
		for(CircuitViewData cir:home.viewCircuitPDList(cCriteria)){
			System.out.println(cir.getCircuitTrace());
			count++;
			
			if(count > 10) break;
		}
		
		testCreateEndingPowerCircuit();
	}
	
	//@Test
	public final PowerCircuit testCreateEndingPowerCircuit() throws DataAccessException{
		Long startPortId = 1952L;
		PowerCircuit circuit = new PowerCircuit();
		List<PowerConnection> recList = new ArrayList<PowerConnection>();
		
		CircuitProc circuitProc = new CircuitProc(this.sf.getCurrentSession());
		
		PowerConnection conn = (PowerConnection)circuitProc.getPortConnection(startPortId, SystemLookup.PortClass.POWER, true);
		
		while(conn != null){
			recList.add(conn);
			
			conn = (PowerConnection)circuitProc.getPortConnection(conn.getDestPortId(), SystemLookup.PortClass.POWER, true);
		}
		
		if(recList.size() > 0 ){
			circuit.setCircuitConnections(recList);
			circuit.setStartConnection(recList.get(0));
			circuit.setEndConnection(recList.get(recList.size() - 1));
		}	
		
		return circuit;
	}	
	
	@Test
	public final void getPowerWattUsedSummary() {
		long portPowerId = 7805;
		Query q = sf.getCurrentSession().getNamedQuery("getPowerWattUsedSummary");
		q.setLong("portPowerId", portPowerId);
		q.setLong("powerSupplyPortId", -1);
		q.setLong("inputCordPortId", -1);
		q.setLong("fuseLkuId", -1);
		q.setResultTransformer(Transformers.aliasToBean(PowerWattUsedSummary.class));
		
		for(Object obj:q.list()){
			obj = null;
		}
		
	}	
	
	@Test
	public final void sampleCodeTest(){
		List<CircuitCriteriaDTO> recList = getAssociatedCircuitsForItem(5163);
		List<CircuitViewData> cirList = new ArrayList<CircuitViewData>();
		
		for(CircuitCriteriaDTO cr:recList){			
			try {
				List<CircuitViewData> temp = home.viewCircuitPDList(cr);
				cirList.addAll(temp);
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		matchCircuitStatus(cirList);

	}

    //TODO: Move this code to circuit
	private void matchCircuitStatus(List<CircuitViewData> dataCirList){
		//dataCirList is a list circuits associated with a particular item.
		for(CircuitViewData cir:dataCirList){
			for(CircuitViewData cir2:dataCirList){
				if(!(cir.getCircuitType().equals(cir2.getCircuitType()))) continue;

				if(cir.getCircuitId() == cir2.getCircuitId()) continue;
				
				if(cir.getEndPortId().equals(cir2.getEndPortId())){
					if(cir.isStatusPlanned() == false && cir2.isStatusPlanned()){
						cir2.setStatusLksCode(cir.getStatusLksCode());
						cir2.setStatus(cir.getStatus());
						
						//TODO: use new code that Basker is doing to set the circuit status
						Session session = this.sf.getCurrentSession();
						DataConnection conn  = (DataConnection)session.get(DataConnection.class, cir2.getStartConnId());
						conn.setStatusLookup(SystemLookup.getLksData(session, cir.getStatusLksCode()));
						session.update(conn);
						session.flush();
					}
				}
				
			}
		}
	}

	private List<CircuitCriteriaDTO> getAssociatedCircuitsForItem(long itemId){
		List<CircuitCriteriaDTO> recList = new ArrayList<CircuitCriteriaDTO>();

		 /*
		    <return-scalar column="circuit_id" type="long"/>
		    <return-scalar column="trace_len" type="long"/>
		    <return-scalar column="circuit_type" type="long"/>
		    */

    	Session session = this.sf.getCurrentSession();
    	Query query =  session.getNamedQuery("getAssociatedCircuitsForItem");
    	query.setLong("itemId", itemId);

    	CircuitCriteriaDTO cCriteria;

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;
			Long circuitId = (Long)row[0];
			Long circuitType = (Long)row[2];

			cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitType);
			cCriteria.setCircuitId(CircuitUID.getCircuitUID(circuitId, circuitType));

			recList.add(cCriteria);
		}

	    return recList;
	}
	
}


