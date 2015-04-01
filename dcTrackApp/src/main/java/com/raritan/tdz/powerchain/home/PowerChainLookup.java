package com.raritan.tdz.powerchain.home;

/**
 * Constants used for the power chain 
 * @author bunty
 *
 */
public class PowerChainLookup {

	public static class Action {
		
		/**
		 * updates all the existing power chain items
		 */
		public static final String POWER_CHAIN_UPDATE_ALL_PORTS_AND_CONN = "PowerChainUpdateAllBreakerPortAndConnections";
		
		/**
		 * 1. create panel breaker port
		 * 2. make connections from panel breaker to branch circuit breaker
		 * 3. make connection from panel breaker to PDU Input breaker
		 */
		public static final String POWER_PANEL_CREATE_BREAKER_PORT = "PowerPanelCreateBreakerPortAndConnections";
		/**
		 * update panel breaker values (amps)
		 */
		public static final String POWER_PANEL_UPDATE_BREAKER_VALUES = "PowerPanelUpdateBreakerPortValues";
		/**
		 * make connections from new branch circuit breaker to panel breaker
		 */
		public static final String POWER_PANEL_ADD_BRANCH_CIRCUIT_BREAKER = "PowerPanelUpdateNewBreakerConnections";
		
		/**
		 * create FPDU input breaker port
		 */
		public static final String FLOOR_PDU_CREATE_BREAKER_PORT = "FloorPDUCreateBreakerPort";
		/**
		 * create FPDU input breaker port
		 */
		public static final String UPS_BANK_CREATE_BREAKER_PORT = "UPSBankCreateBreakerPort";
		/**
		 * update the FPDU input breaker port value
		 */
		public static final String FLOOR_PDU_UPDATE_BREAKER_VALUES = "FloorPDUUpdateBreakerPortValues";
		/**
		 * 	1. Delete old UPS Bank output port id (custom_field2)
			2. Create new UPS Bank output port (custom_field1)
			3. Update connection from FPDU input breaker port to UPS Bank output breaker port
		 */
		public static final String FLOOR_PDU_BREAKER_PORT_CONNECTION = "FloorPDUUpdateBreakerConnections";
		/**
		 * 	1. remove connections from all Power Panel panel breaker port to FPDU input breaker port
			2. delete connection from FPDU input breaker port to UPS Bank output breaker port
			3. delete UPS Bank output breaker port
			4. delete FPDU input breaker port
			5. remove all Power Panels delete all connections from pole to panel breaker, poles, panel breaker
			6. delete all Power Panels
			7. delete FPDU
		 */
		// No event when FPDU is deleted, handled in CV
		public static final String FLOOR_PDU_DELETE_BREAKER_PORT = "FloorPDUDeleteBreakerPort";
		
		/**
		 * 	1. delete all connection from FPDU input breaker port to UPS Bank output ports
			2. delete all UPS Bank output ports
			3. delete UPS Bank
		 */
		public static final String UPS_BANK_DELETE_BREAKER_PORT = "UPSBankDeleteBreakerPort";
		
		/**
		 * update the ups bank output breaker port values
		 */
		public static final String UPS_BANK_UPDATE_BREAKER_PORT_VALUES = "UPSBankUpdateBreakerPortValues";
		
	}
	
	public static class ConnectorLookup {
		
		public static final long ONE_POLE_BREAKER = 77;
		
		public static final long TWO_POLE_BREAKER = 78;
		
		public static final long THREE_PHASE_POLE_BREAKER = 79;
	}
	
	public static class PowerPanelPoleNumbering {
		
		public static final long ODD_EVEN = 1;
		
		public static final long SEQUENTIAL = 2;
		
	}

}
