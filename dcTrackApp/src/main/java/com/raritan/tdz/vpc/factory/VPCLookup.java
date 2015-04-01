package com.raritan.tdz.vpc.factory;

/**
 * VPC lookup for some constants
 * @author bunty
 *
 */
public class VPCLookup {

	public static class ParamsKey {
		
		public static final String PATH = "vpcPath";
		
		public static final String LOCATIONID = "locationId";
		
		public static final String HIGH_VOLTAGE_LKP = "highVoltage";
		
		public static final String LOW_VOLTAGE_LKP = "lowVoltage";
		
		public static final String NAME_POSTFIX = "namePostfix";
		
	}
	
	public static class DefaultValue {
		
		public static final long kVA = 99999L;
		
		public static final long kW = 99999L;
		
		public static final long Current = 99999L;
		
		public static final int polesQty = 6;
		
		public static final String upsBankRedundancy = "N";
		
		public static final String poLocRef = "VPC";
		
		public static final String poModelName = "Generic Outlet Box";
		
	}
	
}
