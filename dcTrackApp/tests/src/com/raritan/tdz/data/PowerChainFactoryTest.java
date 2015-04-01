
package com.raritan.tdz.data;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.raritan.tdz.data.PowerChainFactory;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.tests.TestBase;

public class PowerChainFactoryTest  extends TestBase  {
	PowerChainFactory powerChainFact;
	ItemFactory itemFact;
	PowerPortFactory portFact;
	PowerConnFactory powerConnFact;
	
	@BeforeMethod
	public void setUp() throws Throwable {
		super.setUp();
		itemFact = (ItemFactory)ctx.getBean("itemFact");
		powerChainFact = (PowerChainFactory)ctx.getBean("powerChainFact");
		portFact = (PowerPortFactory)ctx.getBean("powerPortFact");
		powerConnFact = (PowerConnFactory)ctx.getBean("powerConnFact");
	}
	
	//@Test
	public void testCreateBreakersForPanel() throws Throwable {
		MeItem powerPanel = itemFact.createPowerPanel3PhaseWYE(null, "PB1", SystemLookup.SubClass.LOCAL, null);

		this.addTestItem(powerPanel);
		
		//Add poles to panel
		portFact.createPortsForPanel(powerPanel, 12);

		PowerPort panelBreaker = portFact.createPortsForItem(powerPanel, "Breaker", SystemLookup.PortSubClass.PANEL_BREAKER, 1, SystemLookup.PhaseIdClass.THREE_WYE, "208", 60);
		
		//link input breaker from panel to branch circuit port
		for(PowerPort port:powerPanel.getPowerPorts()){
			powerConnFact.createConnImplicit(port, panelBreaker, null);
		}
		
		powerChainFact.createOnePoleBreakersForPanel(powerPanel, panelBreaker, "120", 15, 4, 1);
		powerChainFact.createTwoPoleBreakersForPanel(powerPanel, panelBreaker, "208", 20, 4, 5);
		powerChainFact.createThreePoleBreakersForPanel(powerPanel, panelBreaker, "208", 60, 4, 9);
		
		powerChainFact.printPowerNode(powerPanel);
	}
	
	@Test
	public void testCreatePowerChain() throws Throwable {
		List<PowerConnection> connList = powerChainFact.createPowerChange("LINE1");
		
		for(PowerConnection conn:connList){
			this.addTestItem(conn.getSourceItem());
		}
		this.addTestItem(powerChainFact.getCurrentUPS());
		
		for(PowerConnection conn:connList){
			powerChainFact.printPowerNode((MeItem)conn.getSourceItem());
		}
		
		powerChainFact.deletePowerChainConns();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
