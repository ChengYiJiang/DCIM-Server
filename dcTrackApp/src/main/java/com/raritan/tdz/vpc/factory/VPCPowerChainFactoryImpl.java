/**
 * 
 */
package com.raritan.tdz.vpc.factory;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.vpc.home.VPCDebug;

/**
 * @author prasanna
 *
 */
public class VPCPowerChainFactoryImpl implements VPCPowerChainFactory {

	@Autowired
	private ItemDAO itemDAO;
	
	private VPCItemFactory vpcItemFactory;
	
	private List<VPCConnection> vpcConnections;
	
	private String powerChainLabel;
	
	public VPCPowerChainFactoryImpl(VPCItemFactory vpcItemFactory, List<VPCConnection> vpcConnections, String powerChainLabel){
		//Note that for VPC-A this should be vpcItemFactory for VPC-A
		//For VPC-B this should be vpcItemFactory for VPC-B
		this.vpcItemFactory = vpcItemFactory;
		this.vpcConnections = vpcConnections;
		this.powerChainLabel = powerChainLabel;
		
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.vpc.factory.VPCPowerChainFactory#createPowerChain(java.lang.Long, org.springframework.validation.Errors)
	 */
	@Override
	public void create(Long locationId, Errors errors) throws BusinessValidationException {

		// This will create and persist the vpc items
		Map<String, List<Item>> vpcItems = vpcItemFactory.create(locationId);
		
		VPCDebug.debug("# of items = " + vpcItems.size());

		// Create the Power Chain
		createPortsAndConnections(vpcItems);
		
		VPCDebug.info("VPCitems created, location id: " + locationId);
		
	}
	
	private void createPortsAndConnections(Map<String, List<Item>> vpcItems) throws BusinessValidationException {
		
		for (VPCConnection connector: vpcConnections) {
			
			connector.create(vpcItems);
		}
		
	}
	
	@Override
	public void delete(Long locationId, UserInfo userInfo, Errors errors) throws ClassNotFoundException, BusinessValidationException, Throwable {

		itemDAO.deleteVPCItems(locationId, powerChainLabel);
		
	}
	
}
