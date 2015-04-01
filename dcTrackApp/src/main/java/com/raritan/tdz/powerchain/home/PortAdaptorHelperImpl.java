package com.raritan.tdz.powerchain.home;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;

import com.raritan.tdz.cache.ConnectorLkuCache;
import com.raritan.tdz.cache.LksCache;
import com.raritan.tdz.domain.ConnectorLkuData;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;

/**
 * helper class for port adaptor
 * @author bunty
 *
 */
public class PortAdaptorHelperImpl implements PortAdaptorHelper {

	@Autowired(required=true)
	private ConnectorLkuCache connectorLkuCache;
	
	@Autowired(required=true)
	private LksCache lksCache;
	
	@Autowired(required=true)
	private PowerPortDAO powerPortDAO;

	private MeItem getMeItem(Item item, String errorCodeInvalidClass, Errors errors) {
		MeItem meItem = null;
		if (item instanceof MeItem) {
			meItem = (MeItem) item;
		}
		else {
			Item itemUnProxy = initializeAndUnproxy(item);
			if (itemUnProxy instanceof MeItem) {
				meItem = (MeItem) itemUnProxy;
			}
			else {
				Object[] errorArgs = { };
				errors.rejectValue("PowerChain", errorCodeInvalidClass, errorArgs, "Do not support the item class, subclass");
				return null;
			}
		}
		return meItem;
	}
	
	/**
	 * converts the port that can have multiple instances in an item
	 */
	@Override
	public PowerPort convertUniquePortForMeItem(Item item, Long portSubClass, String errorCodeInvalidClass, Errors errors) {
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}

		LksData portSubClassLksData = lksCache.getLksDataUsingLkpCode(portSubClass); 
		
		LksData phaseLegsLksData = lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseLegClass.ABC);
		
		ConnectorLkuData connLkuData = connectorLkuCache.getConnectorLkuData(PowerChainLookup.ConnectorLookup.THREE_PHASE_POLE_BREAKER);
		
		LksData voltLksData = lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS); 
		
		java.util.Date date = new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		
		PowerPort port = new PowerPort();
		port.setItem(item);
		port.setCreationDate(timeStamp);
		
		port.setPortName(item.getItemName());
		port.setPortSubClassLookup(portSubClassLksData);
		port.setSortOrder(getNextAvailableSortOrder(item, portSubClass));
		port.setConnectorLookup(connLkuData);
		port.setColorLookup(null);
		port.setGenderLookup(null);
		port.setPlacementX(0);
		port.setPlacementY(0);
		port.setFaceLookup(null);
		port.setPhaseLookup(meItem.getPhaseLookup());
		port.setVoltsLookup(voltLksData);
		/*List<LksData> voltsLksList =  systemLookupFinderDAO.findByLkpValueAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
		if (null != voltsLksList && voltsLksList.size() == 1) {
			port.setVoltsLookup(systemLookupFinderDAO.findByLkpValueAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS).get(0));
		}*/
		port.setWattsNameplate(0);
		port.setWattsBudget(0);
		port.setAmpsNameplate(meItem.getRatingAmps());
		port.setAmpsBudget(0);
		port.setIsRedundant(false);
		port.setPowerFactor(1);
		port.setPowerFactorActual(-1);
		port.setBreakerPort(null);
		port.setCircuit(null);
		port.setPolesLookup(null);
		port.setPhaseLegsLookup(phaseLegsLksData);
		port.setComments(null);
		port.setUpdateDate(timeStamp);
		port.setBuswayItem(null);
		port.setInputCordPort(null);
		port.setFuseLookup(null);
		port.setCableGradeLookup(null);
		port.setPolePhase(null);
		port.setPolesLookup(null);
		// port.setPiqId(null);
		
		item.addPowerPort(port);
		
		return port;

	}
	
	/**
	 * converts the port that should have only a single instance in an item 
	 */
	@Override
	public PowerPort convertSingletonPortForMeItem(Item item, Long portSubClass, String errorCodeInvalidClass, Errors errors) {
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}

		java.util.Date date = new java.util.Date();
		Timestamp timeStamp = new Timestamp(date.getTime());
		boolean newPort = false;
		
		PowerPort port = (PowerPort) getExistingPort(item, portSubClass, errors); //existingPort; // (PowerPort) getPanelBreakerPort(item, errors);
		if (null == port) {
			newPort = true;
		}
		
		if (newPort) {
			
			LksData portSubClassLksData = lksCache.getLksDataUsingLkpCode(portSubClass);
			
			ConnectorLkuData connLkuData = connectorLkuCache.getConnectorLkuData(PowerChainLookup.ConnectorLookup.THREE_PHASE_POLE_BREAKER);
			
			LksData phaseLegsLksData = lksCache.getLksDataUsingLkpCode(SystemLookup.PhaseLegClass.ABC);
			
			LksData voltLksData = lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
			
			port = new PowerPort();
			port.setItem(item);
			port.setCreationDate(timeStamp);
			port.setPortSubClassLookup(portSubClassLksData);
			port.setSortOrder(1);
			port.setConnectorLookup(connLkuData);
			port.setIsRedundant(false);
			port.setPowerFactor(1);
			port.setPowerFactorActual(-1);
			port.setPhaseLegsLookup(phaseLegsLksData);
			port.setPortName(item.getItemName());
			port.setPhaseLookup(meItem.getPhaseLookup());
			port.setVoltsLookup(voltLksData);
			/*List<LksData> voltsLksList =  systemLookupFinderDAO.findByLkpValueAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
			if (null != voltsLksList && voltsLksList.size() == 1) {
				port.setVoltsLookup(systemLookupFinderDAO.findByLkpValueAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS).get(0));
			}*/
			
		}
		
		port.setAmpsNameplate(meItem.getRatingAmps());
		port.setUpdateDate(timeStamp);

		if (newPort) {
			meItem.addPowerPort(port);
		}
		
		return port;


	}

	@Override
	public PowerPort updateVoltUsingLineVolt(Item item, PowerPort port, String errorCodeInvalidClass, Errors errors) {
		if (null == port) {
			return null;
		}
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}
		//It is expected that parent's value has been already correctly propagated from the object that is 
		//higher in hiararchy. For example, if case of FPDU to UPSB conn, the UPSB rating_v
		//should be already propagated into FPDU's line_volts
		LksData voltLksData = lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getLineVolts())).intValue()), SystemLookup.LkpType.VOLTS);
		port.setVoltsLookup(voltLksData);
		
		return port;
	}
	
	@Override
	public PowerPort updateVoltUsingDestPort(PowerPort port, Errors errors) {
		if (null == port) {
			return null;
		}
		Set<PowerConnection> powerConns = port.getSourcePowerConnections();
		if (powerConns.size() == 0 || powerConns.size() > 1) {
			return port;
		}
		for (PowerConnection conn: powerConns) {
			PowerPort destPort = (PowerPort) conn.getDestPort();
			if (null == destPort) break;
			port.setVoltsLookup(destPort.getVoltsLookup());
			break;
		}
		return port;
	}

	@Override
	public PowerPort updateVoltUsingRatingVolt(Item item, PowerPort port, String errorCodeInvalidClass, Errors errors) {
		if (null == port) {
			return null;
		}
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}

		LksData voltLksData = lksCache.getLksDataUsingLkpAndType(Long.toString((new Double(meItem.getRatingV())).intValue()), SystemLookup.LkpType.VOLTS);
		port.setVoltsLookup(voltLksData);
		
		return port;
	}
	
	@Override
	public PowerPort updateAmpsRatedUsingKVA(Item item, PowerPort port, String errorCodeInvalidClass, Errors errors) {
		if (null == port) {
			return null;
		}
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}

		Long phaseLkpValueCode = (null != meItem.getPhaseLookup() && null != meItem.getPhaseLookup().getLkpValueCode()) ? meItem.getPhaseLookup().getLkpValueCode() : -1;
		
		double normalizeFactor = (phaseLkpValueCode.longValue() == SystemLookup.PhaseIdClass.THREE_DELTA || phaseLkpValueCode.longValue() == SystemLookup.PhaseIdClass.THREE_WYE) ? Math.sqrt(3) : 1;
		
		double deRatedFactor = 0.8;
		
		if (0 != meItem.getRatingV()) {
			port.setAmpsNameplate(meItem.getRatingKva() * 1000 / (meItem.getRatingV() * normalizeFactor * deRatedFactor));
		}
		
		return port;
	}
	
	@Override
	public PowerPort updatePortName(PowerPort port, String portName) {
		if (null == port) {
			return null;
		}
		port.setPortName(portName);
		
		return port;
	}

	/**
	 * gets the existing port for a given type from the item, if exist
	 * @param item
	 * @param errors
	 * @return
	 */
	private IPortInfo getExistingPort(Item item, Long portSubClass, Errors errors) {
		Set<PowerPort> powerPorts = item.getPowerPorts();
		if (null == powerPorts) {
			return null;
		}
		for (PowerPort port: powerPorts) {
			if (port.getPortSubClassLookup().getLkpValueCode().longValue() == portSubClass.longValue()) {
				return port;
			}
		}
		return null;
		
	}

	/**
	 * get the next available sort order for a given port subclass
	 * @param item
	 * @param portSubClass
	 * @return
	 */
	private int getNextAvailableSortOrder(Item item, Long portSubClass) {
		Set<PowerPort> powerPorts = item.getPowerPorts();
		
		if (null == powerPorts) return 1;
		
		int sortOrder = 0;
		for (PowerPort port: powerPorts) {
			if (port.getPortSubClassLookup() != null && port.getPortSubClassLookup().getLkpValueCode().longValue() == portSubClass.longValue()) {
				if ( port.getSortOrder() > sortOrder ) {
					sortOrder = port.getSortOrder();
				}
			}
		}
		
		return (sortOrder + 1);
	}
	

	
	@SuppressWarnings("unchecked")
	public static <T> T initializeAndUnproxy(T entity) {
	    if (entity == null) {
	        throw new 
	           NullPointerException("Entity passed for initialization is null");
	    }

	    Hibernate.initialize(entity);
	    if (entity instanceof HibernateProxy) {
	        entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer()
	                .getImplementation();
	    }
	    return entity;
	}

	@Override
	public PowerPort updatePhaseLookup(Item item, PowerPort port,
			String errorCodeInvalidClass, Errors errors) {
		if (null == port) {
			return null;
		}
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}
		port.setPhaseLookup(meItem.getPhaseLookup());
		return port;
	}
	
	@Override
	public PowerPort updatePhaseUsingDestPort(PowerPort port, Errors errors) {
		if (null == port) {
			return null;
		}
		Set<PowerConnection> powerConns = port.getSourcePowerConnections();
		if (powerConns.size() == 0 || powerConns.size() > 1) {
			return port;
		}
		for (PowerConnection conn: powerConns) {
			PowerPort destPort = (PowerPort) conn.getDestPort();
			if (null == destPort) break;
			port.setPhaseLookup(destPort.getPhaseLookup());
			break;
		}
		return port;
	}
	
	@Override
	public PowerPort updateAmpsRatedUsingDestPort(PowerPort port, Errors errors) {

		if (null == port) {
			return null;
		}
		Set<PowerConnection> powerConns = port.getSourcePowerConnections();
		if (powerConns.size() == 0 || powerConns.size() > 1) {
			return port;
		}
		
		double deRatedFactor = 0.8;
		
		for (PowerConnection conn: powerConns) {
			PowerPort destPort = (PowerPort) conn.getDestPort();
			if (null == destPort) break;
			port.setAmpsNameplate(destPort.getAmpsNameplate() * deRatedFactor);
			break;
		}
		return port;
		
	}

	@Override
	public PowerPort updateAmpsRatedUsingRatingAmps(Item item, PowerPort port,
			String errorCodeInvalidClass, Errors errors) {

		if (null == port) {
			return null;
		}
		MeItem meItem = getMeItem(item, errorCodeInvalidClass, errors);
		if (null == meItem) {
			return null;
		}
		
		port.setAmpsNameplate(meItem.getRatingAmps());
		return port;
	}

	@Override
	public PowerPort updateUsedFlag(PowerPort port, PowerPort oldSrcPort) {
		
		powerPortDAO.changeUsedFlag(port.getPortId(), oldSrcPort.getPortId());
		
		return port;
	}
	
	@Override
	public PowerPort updateUsedFlag(PowerPort port, boolean value) {
		PowerPort powerPort = powerPortDAO.getPortWithConnections(port.getPortId());
		powerPort.setUsed(value);
		if (!powerPort.getUsed()) {
			powerPort.getDestPowerConnections().clear();
		}

		return port;
	}
	
	@SuppressWarnings("unused")
	private void deleteConnection(Set<PowerConnection> conns, long connectionId) {
		
		PowerConnection pp = null;
		Iterator<PowerConnection> itr = conns.iterator();
		while (itr.hasNext()) {
			pp = itr.next();
			if (pp.getConnectionId() == connectionId) {
				itr.remove();
			}
		}
	}
	
}
