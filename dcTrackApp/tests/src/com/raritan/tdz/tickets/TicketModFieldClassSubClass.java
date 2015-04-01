package com.raritan.tdz.tickets;

import java.util.Set;


import org.junit.Assert;
import org.testng.annotations.Test;

import com.raritan.tdz.domain.TicketFields;
import com.raritan.tdz.domain.Tickets;
import com.raritan.tdz.exception.BusinessValidationException;


public class TicketModFieldClassSubClass extends TicketsTestBase {

	
	private void generatedClassSubClassUsingModel(String make, String model, String itemClass, String subClass, String expectedClass, String expectedSubClass) throws BusinessValidationException {
		
		Tickets tickets = createTicketFields(make, model, itemClass, subClass);
		
		validateExpectedClassSubClass(tickets, expectedClass, expectedSubClass);
	}

	private void validateExpectedClassSubClass(Tickets tickets,
		String expectedClass, String expectedSubClass) {
		
		TicketFields ticketFields = null;

		try {
			Set<TicketFields> fields = tickets.getTicketFields();
			for (TicketFields field: fields) {
				if (field.getIsModified()) {
					ticketFields = field;
					break;
				}
			}
			
			String ticketClass = ticketFields.getItemClass();
			String ticketSubClass = ticketFields.getSubClass();
			
			Assert.assertTrue("expected class not generated: class = " + ticketClass + " | expected = " + expectedClass, ticketClass.equals(expectedClass));
			Assert.assertTrue("expected subclass not generated: subclass = " + ticketSubClass + " | expected = " + expectedSubClass, ticketSubClass.equals(expectedSubClass));
		}
		finally {
			testCleanupTicket(tickets.getTicketId());
		}
		
	}
	
	@Test
	public final void ticketGenClassSubclassForDevBladeUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Sun Microsystems", "Exx00", "Network", "NetworkStack", "Device", "Blade Server");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForDevBladeChassisUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Sun Microsystems", "E4x00", "Network", "NetworkStack", "Device", "Blade Chassis");
		
	}

	@Test
	public final void ticketGenClassSubclassForDevRackableFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("HP", "Proliant DL140 G3", "Network", "NetworkStack", "Device", "Standard");
		
	}

	@Test
	public final void ticketGenClassSubclassForDevFSFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("HP", "StorageWorks XP10000", "Network", "NetworkStack", "Device", "Standard");
		
	}

	// @Test
	public final void ticketGenClassSubclassForDevNRFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Dell", "BN-DEV-NR-1", "Network", "NetworkStack", "Device", "Standard");
		
	}

	// @Test
	public final void ticketGenClassSubclassForDevZUFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("", "BN-DEV-NR-1", "Network", "NetworkStack", "Device", "Standard");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForNwBladeUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Dell", "PowerEdge 1955blade", "Device", "Standard", "Network", "Blade");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForNwRackableChassisUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Cisco Systems", "C8540-CHAS13", "Device", "Standard", "Network", "Chassis");
		
	}

	@Test
	public final void ticketGenClassSubclassForNwFSFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Ciena", "ActivFlex 5430", "Device", "Standard", "Network", "NetworkStack");
		
	}

	@Test
	public final void ticketGenClassSubclassForNwRackableFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Cisco Systems", "AIR-WLC4404", "Device", "Standard", "Network", "NetworkStack");
		
	}

	// @Test
	public final void ticketGenClassSubclassForNwNRFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Dell", "BN-NW-NR-1", "Device", "Standard", "Network", "NetworkStack");
		
	}

	// @Test
	public final void ticketGenClassSubclassForNwZUFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Dell", "BN-NW-NR-1", "Device", "Standard", "Network", "NetworkStack");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForDpRFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Panduit", "1RU-Fiber Panel-FRME1U FC 24-port", "Device", "Standard", "Data Panel", "");
		
	}

	// @Test
	public final void ticketGenClassSubclassForDpNrFixedUsingModel() throws BusinessValidationException {
		
		// generatedClassSubClassUsingModel("Panduit", "1RU-Fiber Panel-FRME1U FC 24-port", "Device", "Standard", "Data Panel", "");
		
	}
	
	// @Test
	public final void ticketGenClassSubclassForDpZuFixedUsingModel() throws BusinessValidationException {
		
		// generatedClassSubClassUsingModel("Panduit", "1RU-Fiber Panel-FRME1U FC 24-port", "Device", "Standard", "Data Panel", "");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForPoBusFixedUsingModel() throws BusinessValidationException { 
		
		generatedClassSubClassUsingModel("Universal Electric Corporation", "Starline Type 01", "Device", "Standard", "Power Outlet", "Busway Outlet");
		
	}

	@Test
	public final void ticketGenClassSubclassForPoNrFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Hubbel", "Electrical Outlet Box", "Device", "Standard", "Power Outlet", "Whip Outlet");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForRpduZuFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("APC", "NET7", "Device", "Standard", "Rack PDU", "");
		
	}

	// @Test
	public final void ticketGenClassSubclassForRpduNrFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Raritan", "BN-RPDU-NR-1", "Device", "Standard", "Rack PDU", "");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForRpduRackFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Raritan", "Dominion PX DPCR20-20", "Device", "Standard", "Rack PDU", "");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForCabinetFs4peUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Corning", "45RU-Rack UDF-BAY-23E-07-Z75", "Device", "Standard", "Cabinet", "");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForProbeRackFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Geist", "RSE2X16", "Device", "Standard", "Probe", "");
		
	}
	
	// @Test
	public final void ticketGenClassSubclassForProbeZuFixedUsingModel() throws BusinessValidationException {
		
		// generatedClassSubClassUsingModel("Geist", "RSE2X16", "Device", "Standard", "Probe", "");
		
	}

	// @Test
	public final void ticketGenClassSubclassForProbeBNrFixedUsingModel() throws BusinessValidationException {
		
		// generatedClassSubClassUsingModel("Geist", "RSE2X16", "Device", "Standard", "Probe", "");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForFpduFsFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Liebert", "Precision Power 50-225 kVA, 4PB", "Device", "Standard", "Floor PDU", "");
		
	}

	@Test
	public final void ticketGenClassSubclassForUpsFsFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Liebert", "Series 610", "Device", "Standard", "UPS", "");
		
	}

	@Test
	public final void ticketGenClassSubclassForCracFsFixedUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Liebert", "UP 249W", "Device", "Standard", "CRAC", "");
		
	}

	@Test
	public final void ticketGenClassSubclassForPassiveRFixedUsingModel() throws BusinessValidationException { 
		
		generatedClassSubClassUsingModel("Panduit", "1RU-Wire Manager-NM1", "Device", "Standard", "Passive", "Standard");
		
	}

	@Test
	public final void ticketGenClassSubclassForPassiveRBpUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("Hubbel", "1RU-Blanking Plate-Hubbell", "Device", "Standard", "Passive", "Blanking Plate");
		
	}
	
	@Test
	public final void ticketGenClassSubclassForPassiveRShelfUsingModel() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("CPI", "1RU-Shelf-12751-719", "Device", "Standard", "Passive", "Shelf");
		
	}

	/*----------------*/
	/* Setting the class subclass when the model cannot be found */
	/*----------------*/

	@Test
	public final void ticketUseClassSubclassForDevVM() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Device", "Virtual Machine", "Device", "Virtual Machine");
		
	}

	@Test
	public final void ticketUseClassSubclassForDevBladeServer() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Device", "Blade Server", "Device", "Blade Server");
		
	}

	@Test
	public final void ticketUseClassSubclassForDevBladeChassis() throws BusinessValidationException { // failed
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Device", "Blade Chassis", "Device", "Blade Chassis");
		
	}

	@Test
	public final void ticketUseClassSubclassForDevStandard() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Device", "Standard", "Device", "Standard");
		
	}

	@Test
	public final void ticketUseClassSubclassForNwBlade() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Network", "Blade", "Network", "Blade");
		
	}

	@Test
	public final void ticketUseClassSubclassForNwChassis() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Network", "Chassis", "Network", "Chassis");
		
	}

	@Test
	public final void ticketUseClassSubclassForNwNs() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Network", "NetworkStack", "Network", "NetworkStack");
		
	}

	@Test
	public final void ticketUseClassSubclassForDp() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Data Panel", "", "Data Panel", "");
		
	}

	@Test
	public final void ticketUseClassSubclassForPoBusway() throws BusinessValidationException { 
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Power Outlet", "Busway", "Power Outlet", "Busway Outlet");
		
	}

	@Test
	public final void ticketUseClassSubclassForPoWhip() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Power Outlet", "Whip Outlet", "Power Outlet", "Whip Outlet");
		
	}
	
	@Test
	public final void ticketUseClassSubclassForRpdu() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Rack PDU", "", "Rack PDU", "");
		
	}

	@Test
	public final void ticketUseClassSubclassForCabinet() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Cabinet", "", "Cabinet", "");
		
	}

	@Test
	public final void ticketUseClassSubclassForCabinetContainer() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Cabinet", "Container", "Cabinet", "Container");
		
	}

	@Test
	public final void ticketUseClassSubclassForProbe() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Probe", "", "Probe", "");
		
	}
	
	@Test
	public final void ticketUseClassSubclassForFpdu() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Floor PDU", "", "Floor PDU", "");
		
	}

	@Test
	public final void ticketUseClassSubclassForFpduLocal() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Floor PDU", "Local", "Floor PDU", "Local");
		
	}

	@Test
	public final void ticketUseClassSubclassForFpduRemote() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Floor PDU", "Remote", "Floor PDU", "Remote");
		
	}

	@Test
	public final void ticketUseClassSubclassForFpduBusway() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Floor PDU", "Busway", "Floor PDU", "Busway");
		
	}
	
	@Test
	public final void ticketUseClassSubclassForUps() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "UPS", "", "UPS", "");
		
	}
	
	@Test
	public final void ticketUseClassSubclassForCrac() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "CRAC", "", "CRAC", "");
		
	}
	
	@Test
	public final void ticketUseClassSubclassForPassiveStd() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Passive", "Standard", "Passive", "Standard");
		
	}

	@Test
	public final void ticketUseClassSubclassForPassiveBp() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Passive", "Blanking Plate", "Passive", "Blanking Plate");
		
	}

	@Test
	public final void ticketUseClassSubclassForPassiveShelf() throws BusinessValidationException {
		
		generatedClassSubClassUsingModel("bla", "bla bla", "Passive", "Shelf", "Passive", "Shelf");
		
	}




	
}
