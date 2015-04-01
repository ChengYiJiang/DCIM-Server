
package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.circuit.validators.PowerCircuitValidator;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.ConnectionToMove;
import com.raritan.tdz.domain.DataCenterLocaleDetails;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;


@Transactional
public class PowerProc {
	@Autowired
	PowerCircuitValidator powerCircuitValidator;
	
	@Autowired
	PowerPortDAO powerPortDAO;
	
	SessionFactory sessionFactory = null;

	MessageSource messageSource;

	private Logger log = Logger.getLogger(this.getClass());

	public PowerProc(){

	}

	public PowerProc(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}


	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public PowerCircuit getProposeCircuit(Long proposeCircuitId) throws DataAccessException{

		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			try{
				PowerCircuit circuit = new PowerCircuit();
				List<PowerConnection> connList = new ArrayList<PowerConnection>();

				Criteria criteria = session.createCriteria(ConnectionToMove.class);
				criteria.add(Restrictions.eq("connType", "power"));
				criteria.add(Restrictions.eq("newCircuitId", proposeCircuitId.intValue()));
				criteria.addOrder(Order.asc("newSortOrder"));

				List list = criteria.list();

				for(Object obj:list){
					ConnectionToMove c =  (ConnectionToMove)obj;

					PowerConnection conn = newPowerConnFromPropose(c);
					connList.add(conn);
				}

				if(connList.size() > 0){
					circuit.setCircuitConnections(connList);
					circuit.setPowerCircuitId(proposeCircuitId);
					circuit.setStartConnection(connList.get(0));
					circuit.setEndConnection(connList.get(connList.size() - 1));

					return circuit;
				}

			}catch(HibernateException e){

				 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

			}catch(org.springframework.dao.DataAccessException e){

				throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			}
		}

		return null;
	}

	public PowerConnection newPowerConnFromPropose(ConnectionToMove c) throws DataAccessException{
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			try{
				PowerConnection conn = new PowerConnection();
				Long portId = c.getPortMovingId().longValue();

				PowerPort port = (PowerPort)session.get(PowerPort.class, portId);
				conn.setSourcePowerPort(port);

				lazyLoadPort(port);

				if(c.getNewEndPointId() != null && c.getNewEndPointId() > 0){
					portId = c.getNewEndPointId().longValue();
					port = (PowerPort)session.get(PowerPort.class, portId);
					conn.setDestPowerPort(port);

					lazyLoadPort(port);
				}

				conn.setComments(c.getNewComment());
				conn.setCreatedBy(c.getEnteredBy());
				conn.setCreationDate(c.getEnteredOn());
				conn.setPowerConnectionId(c.getTblxConnectId().longValue());
				conn.setCircuitPowerId(c.getNewCircuitId().longValue());
				conn.setSortOrder(c.getNewSortOrder());

				LksData lksData;

				if(c.getNewStatusId() == null){
					lksData = SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED);
				}
				else{
					lksData = (LksData)session.get(LksData.class, c.getNewStatusId().longValue());
				}
				conn.setStatusLookup(lksData);

				if(c.getConnTypeLksId() != null){
					lksData = (LksData)session.get(LksData.class, c.getConnTypeLksId());
					conn.setConnectionType(lksData);
				}

				PowerConnection origConn = (PowerConnection)session.get(PowerConnection.class, c.getTblxConnectId().longValue());
				ConnectionCord cord = null;

				if (origConn != null) {
					cord = origConn.getConnectionCord();
				}

				if (c.getNewConnectionLabel() != null) {
					if (cord == null) {
						cord = new ConnectionCord();
					}
					cord.setCordLabel(c.getNewConnectionLabel());
				}

				if (c.getNewConnectionLength() != null){
					if (cord == null) {
						cord = new ConnectionCord();
					}
					cord.setCordLength(c.getNewConnectionLength());
				}

				if (c.getNewConnectionType() != null) {
					LkuData cordLku = (LkuData)session.get(LkuData.class, c.getNewConnectionType().longValue());
					if (cordLku != null) {
						if (cord == null) {
							cord = new ConnectionCord();
						}
						cord.setCordLookup( cordLku );
					}
				}

				conn.setConnectionCord( cord );

				return conn;

			}catch(HibernateException e){

				 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

			}catch(org.springframework.dao.DataAccessException e){

				throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
			}
		}
		return null;
	}


	private class PowerChain{
		public Long breakerid;
		public Long PanelID;
		public Long PDUID;
		public Long BankID;
	    public boolean IsEnough;
	    public String BottleNeck;
	}

	private class PowerMeasuredAmps{
		public double CurrentMeasuredA;
		public double CurrentMeasuredB;
		public double CurrentMeasuredC;
		public double CurrentMeasuredN;
		public double Volts;
	    public String FromNodes;
	    public String IsFromDownStream;
	}

	public boolean lockPowerConn(PowerConnection conn, boolean isUsed, Timestamp updateDate, Session session) throws DataAccessException{
		PowerPort sourcePort=null, destPort = null;

		if(conn.getSourcePowerPort() != null){
//			sourcePort = (PowerPort)sess.get(PowerPort.class, conn.getSourcePowerPort().getPortId());
			sourcePort = conn.getSourcePowerPort();
			sourcePort = (PowerPort)session.get(PowerPort.class, sourcePort.getPortId());
		}

		if(conn.getDestPowerPort() != null){
//			destPort = (PowerPort)sess.get(PowerPort.class, conn.getDestPowerPort().getPortId());
			destPort = conn.getDestPowerPort();
			//destPort = (PowerPort)session.get(PowerPort.class, sourcePort.getPortId());
			destPort = (PowerPort)session.get(PowerPort.class, destPort.getPortId());
		}

		if(sourcePort != null && destPort != null){
			//If item Id is the same, this could be a Rack PDU were Output Port is Connected to Input Port
			//this connection should not be deleted
			if(isUsed && sourcePort.getItem().getItemId() == destPort.getItem().getItemId()){
				return false;  //do nothing
			}
		}

		if (sourcePort != null) {
			if(sourcePort.getUsed() != isUsed){ //update only if not the same, this avoid firing the trigger multiple times
				sourcePort.setUsed(isUsed);
				sourcePort.setUpdateDate(updateDate);
				
				if(sourcePort.isPowerSupply() && isUsed == false){
					sourcePort.setAmpsBudget(0);
					sourcePort.setAmpsNameplate(0);
				}
				
				session.update(sourcePort);
			}
			
		}

		if (destPort != null) {
			if(destPort.getUsed() != isUsed){ //update only if not the same
				destPort.setUsed(isUsed);
				destPort.setUpdateDate(updateDate);
				session.update(destPort);
			}
		}

		return true;
	}

	/**
	 * This will setup the power supply amps based on the next node
	 * that a specific power supply is connected to such as a Rack PDU
	 * output port or the Floor outlet
	 * @param powerCircuit
	 */
	public void setupPowerSupplyAmpsValue(PowerCircuit powerCircuit){
		//Get the first connection
		PowerConnection conn = powerCircuit.getStartConnection();
		
		//Get the power supply port
		PowerPort srcPort = conn.getSourcePowerPort();
		
		//Get the outlet port
		PowerPort destPort = conn.getDestPowerPort();
		
		//Load the ports based on the ids
		PowerPort dbSrcPort = srcPort != null ? powerPortDAO.read(srcPort.getPortId()) : null;
		PowerPort dbDstPort = destPort != null ? powerPortDAO.read(destPort.getPortId()) : null;
		
		//Set up amps if it is a power supply
		if (dbSrcPort != null && (dbSrcPort.isPowerSupply())){
			setAmpsValue(dbSrcPort, dbDstPort);
		}
	}
	
	private void setAmpsValue(PowerPort port, PowerPort outletPort){
		double sq=1, ampsD=0, ampsR=0, volts = 0.0;
		
		if (port == null)
			return;
		
		if (outletPort != null && outletPort.getVoltsLookup() != null)
			volts = Double.parseDouble(outletPort.getVoltsLookup().getLkpValue());

		if(port.isThreePhaseVoltage()){
			sq = Math.sqrt(3.0);
		}

		ampsD = (port.getWattsBudget() / port.getPowerFactor()) / (volts * sq);
		ampsR = (port.getWattsNameplate() / port.getPowerFactor()) / (volts * sq);

		port.setAmpsNameplate(ampsR);  //need to Round(AmpsR, 2) ?????????
		port.setAmpsBudget(ampsD);
	}
	
	public List<String> validatePowerCircuit(PowerCircuit circuit) throws DataAccessException{
		List<String> resultErrors = new ArrayList<String>();
		
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, PowerCircuit.class.getName());
		powerCircuitValidator.validate(circuit, errors);
		
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				resultErrors.add(msg);
			}
		}
			
		return resultErrors;		
	}

	public PowerPort getPowerPort(Long portId){
		PowerPort port = null;
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			if (portId != null)
				port = (PowerPort)session.get(PowerPort.class, portId);
		}

		return port;
	}

	public void lazyLoadPort(PowerPort port){
		if(port == null){
			return;
		}

		if(port.deRatingFactor == 0){
			if (port.getItem() != null) {
				DataCenterLocaleDetails loc = port.getItem().getDataCenterLocation().getDcLocaleDetails();
				String country = loc.getCountry().toUpperCase();

				if(country.equals("UNITED STATES")){
					port.deRatingFactor = 0.80;
					port.upRatingFactor = 1.25;
				}
				else{
					port.deRatingFactor = 1.0;
					port.upRatingFactor = 1.0;
				}
			}
		}

		if(port.getItem() != null){
			port.getItem().getItemId();
		}

		if(port.getConnectorLookup() != null){
			port.getConnectorLookup().getConnCompatList();
			port.getConnectorLookup().getConnCompat2List();
		}

		port.getVoltsLookup();
		port.getPhaseLookup();
		port.getBreakerPort();
		port.getFuseLookup();
		port.getPhaseLegsLookup();
	}

	public double getDeRatedFactorForSite(Long portId){
		if(portId != null && portId > 0 && sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			PowerPort port = (PowerPort)session.get(PowerPort.class, portId);
			DataCenterLocaleDetails loc = port.getItem().getDataCenterLocation().getDcLocaleDetails();
			String country = loc.getCountry().toUpperCase();

			if(country.equals("UNITED STATES")){
				return 0.80;
			}
		}

		return 1;
	}

	public double getUpRatedFactorForSite(Long portId){
		if(portId != null && portId > 0 && sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();
			PowerPort port = (PowerPort)session.get(PowerPort.class, portId);
			DataCenterLocaleDetails loc = port.getItem().getDataCenterLocation().getDcLocaleDetails();
			String country = loc.getCountry().toUpperCase();

			if(country.equals("UNITED STATES")){
				return 1.25;
			}
		}

		return 1;
	}

	public void lockCircuit(PowerCircuit circuit) throws DataAccessException{
		List<Long> connIdList = circuit.getConnListFromTrace();
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		Long itemClassCode = null;

		Session session = sessionFactory.getCurrentSession();

		for(int i=0; i<connIdList.size(); i++){
			PowerConnection xconn = (PowerConnection)session.load(PowerConnection.class, connIdList.get(i));

			if(xconn != null){
				Long portId = xconn.getSourcePortId();
				PowerPort port = (PowerPort)session.load(PowerPort.class, portId);

				itemClassCode = port.getItem().getClassLookup().getLkpValueCode();

				if(xconn.getCircuitPowerId() == null){ //set circuit_id
					xconn.setCircuitPowerId(circuit.getCircuitId());
					session.save(xconn);
				}

				// lock ports
				lockPowerConn(xconn, true, currentDate, session);

				if (itemClassCode == SystemLookup.Class.FLOOR_OUTLET) {
					break; //don't change connection between outlet and breaker
				}
			}
		}
	}

	public long getRPDUFuseUsedWatts(Long inputCordPortId, Long fuseLkuId){
		long usedWatts = 0;
		if (sessionFactory != null){
			Session session = sessionFactory.getCurrentSession();

	    	Query query =  session.getNamedQuery("getRPDUFuseUsedWatts");
	    	query.setLong("inputCordId", inputCordPortId);
	    	query.setLong("fuseLkuId", fuseLkuId);
	    	Long watts = (Long)query.uniqueResult();

	    	if(watts != null){
	    		usedWatts = watts;
	    	}
		}

	    return usedWatts;
	}

	public long getRPDUInputCordUsedWatts(Long inputCordPortId){
		long usedWatts = 0;
		Session session = sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getRPDUInputCordUsedWatts");
    	query.setLong("inputCordId", inputCordPortId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getBreakerUsedWatts(Long breakerPortId) {
		long usedWatts = 0;

    	Session session = sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getBreakerUsedWatts");
    	query.setLong("breakerPortId", breakerPortId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getPanelBoardUsedWatts(Long panelItemId) {
		long usedWatts = 0;

    	Session session = sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getPanelBoardUsedWatts");
    	query.setLong("panelItemId", panelItemId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getFloorPDUUsedWatts(Long pduItemId) {
		long usedWatts = 0;

    	Session session = sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getFloorPDUUsedWatts");
    	query.setLong("pduItemId", pduItemId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getUpsBankUsedWatts(Long upsBankItemId) {
		long usedWatts = 0;

    	Session session = sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getUpsBankUsedWatts");
    	query.setLong("upsBankItemId", upsBankItemId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getRPDURecpUsedWatts(Long recPortId) {
		long usedWatts = 0;

    	Session session = sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getRPDURecpUsedWatts");
    	query.setLong("portId", recPortId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getRPDUInputCordFreeWatts(Long inputCordPortId){
		long freeWatts = 0;

    	Session session = this.sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getRPDUInputCordFreeWatts");
    	query.setLong("inputCordId", inputCordPortId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		freeWatts = watts;
    	}

	    return freeWatts;
	}

	public long getOutLetUsedWatts(Long outletPortId){
		long usedWatts = 0;

    	Session session = this.sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getOutletUsedWatts");
    	query.setLong("outletPortId", outletPortId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	public long getPowerPortUsedWatts(Long portId){
		long usedWatts = 0;

    	Session session = this.sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getPowePortUsedWatts");
    	query.setLong("powerPortId", portId);
    	Long watts = (Long)query.uniqueResult();

    	if(watts != null){
    		usedWatts = watts;
    	}

	    return usedWatts;
	}

	private PowerPort getFirstPort(PowerPort sPort, int nodeNumber) {
		PowerPort firstPort = null;

		if (sPort != null){
			LksData sItemClass = sPort.getItem().getClassLookup();

			if(nodeNumber == 1 && sItemClass.getLkpValueCode() != SystemLookup.Class.RACK_PDU)
				firstPort = sPort;
		}

		return firstPort;
	}


	public HashMap<Long, Long> getProposeCircuitPortsNetWatts(){
		HashMap<Long, Long> portList = new HashMap<Long, Long>();

    	Session session = this.sessionFactory.getCurrentSession();
    	Query query =  session.getNamedQuery("getProposeCircuitPortsNetWatts");

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			//store port_power_id and net_watts in hash table
			portList.put((Long)row[0], (Long)row[1]);
		}

	    return portList;
	}

	//Let us get the first port from the database if it exists.
	//This will be useful when user changes power supplies
	private PowerPort getFirstPortOriginalFromDB(PowerCircuit circuit,
			Session session) {
		PowerPort sPort = null;
		try {
			if (circuit != null && circuit.isNewCircuit() == false){
				Long circuitId = circuit.getCircuitId();
				//Try to load that circuit from database
				PowerCircuit circuitDB = (PowerCircuit) session.load(PowerCircuit.class, circuitId);
				PowerConnection connection = circuitDB.getStartConnection();
				sPort = connection.getSourcePowerPort();
			}
		} catch (HibernateException e){
			//Ignore this exception since that circuit is non existent and we will be handling
			//null port in the main code.
			if (log.isDebugEnabled())
				e.printStackTrace();
		}
		return sPort;
	}
	
	public void preLoadCircuitPorts(PowerCircuit circuit){
		if(circuit == null || circuit.getCircuitConnections() == null) return;
		
		PowerPort port;
		
		for(PowerConnection conn:circuit.getCircuitConnections()){
			try {
				port = this.powerPortDAO.loadPort(conn.getSourcePortId());
				conn.setSourcePowerPort(port);
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		}
		
		for(PowerConnection conn:circuit.getCircuitConnections()){
			try {
				if(conn.getDestPowerPort() != null){
					port = this.powerPortDAO.loadPort(conn.getDestPortId());
					conn.setDestPowerPort(port);
				}
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
		}
	}

}
