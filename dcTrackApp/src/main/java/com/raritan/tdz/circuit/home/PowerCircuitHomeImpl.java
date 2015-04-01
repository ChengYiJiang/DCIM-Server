package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.util.ProposedCircuitHelper;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.LogManager;
import com.raritan.tdz.util.MessageContext;
import com.raritan.tdz.vpc.home.VPCHome;
/**
 *
 * @author Santo Rosario
 *
 */
//@Transactional(rollbackFor = DataAccessException.class)
public class PowerCircuitHomeImpl implements PowerCircuitHome  {
	private SessionFactory sessionFactory;
	private MessageSource messageSource;
	private static Logger appLogger = Logger.getLogger(PowerCircuitHomeImpl.class);
	
	@Autowired
	protected PowerProc powerProc;

	@Autowired
	protected PowerPortDAO powerPortDAO;

	@Autowired
	protected PowerCircuitDAO powerCircuitDAO;
	
	@Autowired
	protected CircuitDelete circuitDelete;
	
	@Autowired
	protected VPCHome vpcHome;
	
	@Autowired
	protected PowerConnDAO powerConnDAO;
	
	public PowerCircuitHomeImpl(SessionFactory sessionFactory, MessageSource messageSource){
		this.sessionFactory = sessionFactory;
		this.messageSource = messageSource;
	}


	//POWER CIRCUIT FUNCTIONS
	@Override
	public List<PowerCircuit> viewPowerCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException, BusinessValidationException {
		Session session = null;

		PowerCircuit  powerCircuit = null;
		PowerConnection connFound = null;
		List<PowerCircuit>  pcList = new ArrayList<PowerCircuit>();
		//PowerProc powerProc = new PowerProc(this.sessionFactory.getCurrentSession(),messageSource);

		final String methodName = "viewPowerCircuitByCriteria";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);
		
		if (cCriteria.isVpcRequested()) {
			
			PowerCircuit vpcCircuitFromOutlet = vpcHome.getVPCPartialCircuit(cCriteria.getVpcChainLabel(), cCriteria.getLocationId(), cCriteria.getStartPortId(), null);
			
			pcList.add(vpcCircuitFromOutlet);
			cCriteria.setStartPortId(null);
			
			return pcList;
		}
		
		final long proposedCircuitId = ProposedCircuitHelper.getProposedCircuitId( cCriteria );

		//Get Propose circuit
		if (proposedCircuitId > 0) {
			powerCircuit = powerProc.getProposeCircuit( proposedCircuitId );

			if (powerCircuit == null) {
				String msg = messageSource.getMessage("circuit.proposedCircuitDoesNotExist", null, null);
				BusinessValidationException be = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				be.setErrorCode( ApplicationCodesEnum.PWR_CIR_DOES_NOT_EXIST.errCode() );
				be.addValidationError( msg );
				throw be;
			}

			List<PowerConnection> connList = powerCircuit.getCircuitConnections();
			PowerConnection lastConn = powerCircuit.getEndConnection();
			PowerPort destPort = lastConn.getDestPowerPort();

			if (destPort != null) {
				CircuitCriteriaDTO proposedCriteria = new CircuitCriteriaDTO();
				proposedCriteria.setStartPortId( destPort.getPortId() );
				List<PowerCircuit> list =  viewPowerCircuitByCriteria( proposedCriteria );

				if(list.size() > 0){
					PowerCircuit subCircuit = list.get(0);
					powerCircuit.setEndConnection(subCircuit.getEndConnection());

					for(PowerConnection conn:subCircuit.getCircuitConnections()){
						connList.add(conn);
					}
				}
			}

			powerCircuit.setProposed( true );
			pcList.add(powerCircuit);

			return pcList;
		}

		try{
			CircuitProc circuitProc = new CircuitProc(this.sessionFactory.getCurrentSession());

			session = this.sessionFactory.getCurrentSession();

			Criteria cr = session.createCriteria(PowerCircuit.class);

			//For a port, find the connection Id first, use Connection ID instead of port ID
			if(cCriteria.getPortId() != null && cCriteria.getPortId() > 0){
				PowerConnection conn = (PowerConnection)circuitProc.getPortConnection(cCriteria.getPortId(), SystemLookup.PortClass.POWER, true);

				if(conn == null){
					//try again using dest port
					conn = (PowerConnection)circuitProc.getPortConnection(cCriteria.getPortId(), SystemLookup.PortClass.POWER, false);

					if(conn == null){
						return pcList;
					}
				}

				cCriteria.setConnectionId(conn.getPowerConnectionId());
			}
			else{
				//For a start port, find the connection Id first, use start connection ID instead of start port ID
				if(cCriteria.getStartPortId() != null && cCriteria.getStartPortId() != 0){
					PowerConnection conn = (PowerConnection)circuitProc.getPortConnection(cCriteria.getStartPortId(), SystemLookup.PortClass.POWER, true);

					if(conn == null){
						return pcList;
					}

					cCriteria.setStartConnId(conn.getPowerConnectionId());
					connFound = conn;
				}
			}

			List<Long> circuitIdList = null;

			if(cCriteria.getItemId() != null && cCriteria.getItemId() > 0){
				circuitIdList = circuitProc.getCircuitIds(cCriteria.getItemId(), SystemLookup.PortClass.POWER);
				
				if(circuitIdList == null || circuitIdList.size() == 0) {
					return pcList;
				}
			}

			//circuit id is primary key, no need to search using other fields
			final long circuitId = cCriteria.getCircuitUID().getCircuitDatabaseId();
			if(circuitId > 0){
				cr.add(Restrictions.eq("powerCircuitId", circuitId));
			}
			else if(circuitIdList != null){
				cr.add(Restrictions.in("powerCircuitId", circuitIdList));
			}
			else if(cCriteria.getCircuitTrace() != null && cCriteria.getCircuitTrace().trim().length() > 0){
				cr.add(Restrictions.eq("circuitTrace", cCriteria.getCircuitTrace().trim()));
			}
			else if(cCriteria.getContainCircuitTrace() != null && cCriteria.getContainCircuitTrace().trim().length() > 0){
				cr.add(Restrictions.like("circuitTrace", cCriteria.getContainCircuitTrace().trim(), MatchMode.ANYWHERE));
			}
			else{
				cr.createAlias("startConnection", "startConn");
				cr.createAlias("endConnection","endConn");
				cr.createAlias("startConn.sourcePowerPort", "startPort");

				if(cCriteria.getStartConnId() != null && cCriteria.getStartConnId() > 0){
					cr.add(Restrictions.eq("startConn.powerConnectionId", cCriteria.getStartConnId()));
				}

				if(cCriteria.getEndConnId() != null && cCriteria.getEndConnId() > 0){
					cr.add(Restrictions.eq("endConn.powerConnectionId", cCriteria.getEndConnId()));
				}

				if(cCriteria.getLocationId() != null && cCriteria.getLocationId() > 0){
					cr.createAlias("startConn.sourcePowerPort.item", "startItem");
					cr.createAlias("startItem.powerCenterLocation", "location");
					cr.add(Restrictions.eq("location.powerCenterLocationId", cCriteria.getLocationId()));
				}

				if(cCriteria.getConnectionId() != null && cCriteria.getConnectionId() > 0){
					cr.add(Restrictions.like("circuitTrace", "%," + cCriteria.getConnectionId() + ",%"));
				}
			}

			cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
			cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);

			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			List list = cr.list();
			String circuitIds = "";

			for(Object pcObject:list)
			{
				powerCircuit = (PowerCircuit)pcObject;

				//Do not allow duplicates circuit records
				if(circuitIds.indexOf(String.valueOf(powerCircuit.getPowerCircuitId())) > 0){
					continue;
				}

				circuitIds = circuitIds + "|" + String.valueOf(powerCircuit.getPowerCircuitId());

				List<PowerConnection> powerConnections = new ArrayList<PowerConnection>();

				//Load connection records
				for(long connectionId:powerCircuit.getConnListFromTrace()){
					PowerConnection powerConnection = (PowerConnection)session.get(PowerConnection.class, connectionId);

					if(powerConnection != null){
						powerProc.lazyLoadPort(powerConnection.getSourcePowerPort());
						powerProc.lazyLoadPort(powerConnection.getDestPowerPort());

						if(powerConnection.getConnectionCord() != null){
							powerConnection.getConnectionCord().getColorLookup();
							powerConnection.getConnectionCord().getCordLookup();
						}

						powerConnections.add(powerConnection);
					}
				}

				powerCircuit.setCircuitConnections(powerConnections);
				pcList.add(powerCircuit);
			}

			if(cCriteria.isSecondPortSearched() == false){
				if(powerCircuit == null && connFound != null){
					//create dummy circuit for connections missing dct_circuit record
					if(connFound.getDestPowerPort() != null){
						cCriteria.setStartPortId(connFound.getDestPowerPort().getPortId());
						cCriteria.setSecondPortSearched(true);

						for(PowerCircuit cx:viewPowerCircuitByCriteria(cCriteria)){
							//System.out.println("Adding Power Conn Id = " + connFound.getPowerConnectionId());

							cx.getCircuitConnections().add(0, connFound);
							pcList.add(cx);
							powerCircuit = cx;
						}
					}

					if(powerCircuit == null){
						powerCircuit = createEndingPowerCircuit(connFound.getSourcePortId()); 
						//powerProc.createDummyCircuit(connFound);
						pcList.add(powerCircuit);
					}
				}
			}
		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_FETCH_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return pcList;
	}
	

	/**
	 * Adds a new Power circuit in the system.
	 * @param powerCircuit
	 * @return
	 * @throws DataAccessException
	 */

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = ServiceLayerException.class)
	public long addPowerCircuit(PowerCircuit powerCircuit, String origUser, Timestamp origDate)	throws DataAccessException, BusinessValidationException {
		Session session = null;
		Long currentConnectId;
		int sortOrder = 1;
		String circuitTrace = "";
		final String methodName = "addPowerCircuit";
		Timestamp creationDate = null;
		Timestamp updateDate = null;
		LksData connStatusLks = null;
		
		session = this.sessionFactory.getCurrentSession();
		
		if(powerCircuit.futureCircuitStatus != null) {
			connStatusLks = SystemLookup.getLksData(session, powerCircuit.futureCircuitStatus);								
		}else {
			connStatusLks = SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED);
		}

		//TODO: refactor updating CreatedBy and CreatedDate 
		// HACK HACK HACK
		// since update deletes original circuit and call add to add new circuit, 
		// original circuits Username and CreationDate is sent to this function.

		creationDate = updateDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		if (origDate != null)	creationDate = origDate;
		if (origUser == null) origUser = powerCircuit.getUserInfo().getUserName();

		PowerCircuit newCircuit;		

		if(powerCircuit.getPowerCircuitId() != null && powerCircuit.getPowerCircuitId() > 0){
			newCircuit = (PowerCircuit) session.get(PowerCircuit.class, powerCircuit.getPowerCircuitId());
			newCircuit.setSharedCircuitTrace(null);
			newCircuit.setCircuitTrace(null);
		}
		else{
			newCircuit = new PowerCircuit();
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			//PowerProc powerProc = new PowerProc(this.sessionFactory.getCurrentSession(),messageSource);
			CircuitProc circuitProc = new CircuitProc(this.sessionFactory.getCurrentSession());

			powerProc.setupPowerSupplyAmpsValue(powerCircuit);
			if(powerCircuit.validateCircuit){
				List<String> errList = powerProc.validatePowerCircuit(powerCircuit);

				if(errList.size() > 0){
					BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));

					for(String s:errList){
						e.addValidationError(s);
					}

					throw e;
				}

				errList = null;
			}

			String shareCircuitTrace = ",,";
			HashMap<Long, String> traceList = this.getConnIdWihCircuitTrace(powerCircuit.getCircuitType());
			PowerCircuit partialCircuit = getPartialCircuit(powerCircuit, origUser, origDate);

			if(partialCircuit != null){
				shareCircuitTrace = partialCircuit.getCircuitTrace();
				newCircuit.setSharedCircuitTrace(shareCircuitTrace);
				//System.out.println("Shared-Circuit-Trace = " + shareCircuitTrace);
			}

			List<PowerConnection> powerConnections = powerCircuit.getCircuitConnections();
			if (powerConnections == null){
				String err = "Cannot save this circuit. No power connections sent by client ";
				appLogger.error(err);
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError(err);
				throw e;
			}

			PowerConnection powerConnection = powerConnections.get(0);

			if (powerConnection == null){
				String err = "Cannot save this circuit. First connection sent by client is null ";
				appLogger.error(err);
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError(err);
				throw e;
			}

			PowerPort srcPowerPort = powerConnection.getSourcePowerPort();
			if (srcPowerPort == null){
				String err = "Cannot save this circuit. First connection's source port sent by client is null.";
				appLogger.error(err);
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError(err);
				throw e;
			}


			PowerPort firstNode =    powerPortDAO.loadPort(srcPowerPort.getPortId());

			if (firstNode == null){
				String err = "Cannot save this circuit. First connection's source port could not be loaded from database.";
				appLogger.error(err);
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError(err);
				throw e;
			}

			Item firstItem = firstNode.getItem();
			if (firstItem == null){
				String err = "Cannot save this circuit. First connection's source port's associated item could not be loaded from database.";
				appLogger.error(err);
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError(err);
				throw e;
			}

			LksData classLookup = firstItem.getClassLookup();
			if (classLookup == null){
				String err = "Cannot save this circuit. First connection's source port's associated item class could not be loaded from database.";
				appLogger.error(err);
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError(err);
				throw e;
			}

			if(classLookup.getLkpValueCode() != SystemLookup.Class.RACK_PDU){ //do this for first node of circuit
				//Check to see if a circuit exist with the same start connection
				PowerConnection dc = (PowerConnection)circuitProc.getPortConnection(firstNode.getPortId(), newCircuit.getCircuitType(), true);

				if(dc != null){
					//Circuit Exist, return
					if(traceList.containsValue(dc.getPowerConnectionId()) == false){
						String errMsg = ApplicationCodesEnum.PWR_CIR_PORT_START_CONN.value();
						errMsg = errMsg.replaceAll("<ItemName>", firstNode.getItem().getItemName());
						errMsg = errMsg.replaceAll("<PortName>", firstNode.getPortName());

						BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));
						e.addValidationError(errMsg);
						throw e;
					}
				}
			}

			//Create new circuit
			for(PowerConnection conn:powerCircuit.getCircuitConnections()){
				//System.out.println("Port Id = " + conn.getSourcePowerPort().getPortId());
				PowerConnection oldConn = (PowerConnection)circuitProc.getPortConnection(conn.getSourcePowerPort().getPortId(), SystemLookup.PortClass.POWER, true);

				//Add old connection to circuit
				if(oldConn != null){
					currentConnectId = oldConn.getPowerConnectionId();

					if(newCircuit.getSharedCircuitTrace() == null){
						if(traceList.containsKey(currentConnectId)){
							shareCircuitTrace = traceList.get(currentConnectId);
							newCircuit.setSharedCircuitTrace(shareCircuitTrace);
							//System.out.println("Shared-Circuit-Trace = " + shareCircuitTrace);
						}
					}

					if(shareCircuitTrace.indexOf(currentConnectId.toString()) <= 0 ){
						oldConn.setSourcePowerPort(conn.getSourcePowerPort());
						oldConn.setDestPowerPort(conn.getDestPowerPort());

						if(conn.getConnectionType() != null){
							oldConn.setConnectionType(conn.getConnectionType());
						}

						oldConn.setCreatedBy(origUser);
						oldConn.setUpdateDate(updateDate);
						oldConn.setCreationDate(creationDate);						
						oldConn.setStatusLookup(connStatusLks);								
						oldConn.setSortOrder(sortOrder);
						oldConn.setCircuitPowerId(null);

						//Save Cord Information
						ConnectionCord cord = conn.getConnectionCord();

						if(cord != null){
							ConnectionCord oldCord = oldConn.getConnectionCord();

							if(oldCord != null){
								oldCord.setColorLookup(cord.getColorLookup());
								oldCord.setCordLabel(cord.getCordLabel());
								oldCord.setCordLength(cord.getCordLength());
								oldCord.setCordLookup(cord.getCordLookup());
								oldCord.setIsUsed(cord.getIsUsed());
								session.update(oldCord);
							}
							else{
								Long cordId = (Long)session.save(cord);
								cord.setCordId(cordId);
								oldConn.setConnectionCord(cord);
							}
						}

						session.update(oldConn);
					}

					if(sortOrder == 1){
						circuitTrace = currentConnectId.toString();
						newCircuit.setStartConnection(oldConn);
						newCircuit.setEndConnection(oldConn);
					}
					else{
						circuitTrace = circuitTrace + "," + currentConnectId.toString();
						newCircuit.setEndConnection(oldConn);
					}
					sortOrder++;

					continue;
				}

				//Save Cord Information
				ConnectionCord cord = conn.getConnectionCord();

				if(cord != null){
					Long cordId = (Long)session.save(cord);
					cord.setCordId(cordId);
				}

				//Set Sort Order
				conn.setSortOrder(sortOrder);

				//Set Conn Type
				if(conn.getConnectionType() == null){
					conn.setConnectionType(SystemLookup.getLksData(session, SystemLookup.LinkType.EXPLICIT));
				}

				//Set Status
				if(conn.getStatusLookup() == null){
					conn.setStatusLookup(connStatusLks);
				}

				conn.setCreatedBy(origUser);
				conn.setCreationDate(creationDate);
				conn.setUpdateDate(updateDate);
				conn.setCircuitPowerId(null);

				currentConnectId = (Long)session.save(conn);
				conn.setPowerConnectionId(currentConnectId);

				//Set Circuit Trace
				if(sortOrder == 1){
					circuitTrace = currentConnectId.toString();
					newCircuit.setStartConnection(conn);
					newCircuit.setEndConnection(conn);
				}
				else{
					circuitTrace = circuitTrace + "," + currentConnectId.toString();
					newCircuit.setEndConnection(conn);
				}

				sortOrder++;
			}

			//Terminate connection if needed
			if(newCircuit.getEndConnection().getDestPowerPort() != null){
				PowerConnection conn = new PowerConnection();
				conn.setSourcePowerPort(newCircuit.getEndConnection().getDestPowerPort());
				conn.setStatusLookup(connStatusLks);
				conn.setConnectionType(SystemLookup.getLksData(session, SystemLookup.LinkType.EXPLICIT));
				conn.setCreatedBy(origUser);
				conn.setCreationDate(creationDate);
				conn.setUpdateDate(updateDate);
				conn.setSortOrder(sortOrder);
				conn.setCircuitPowerId(null);

				currentConnectId = (Long)session.save(conn);
				conn.setPowerConnectionId(currentConnectId);

				circuitTrace = circuitTrace + "," + currentConnectId.toString();
				newCircuit.setEndConnection(conn);
			}

			//Create Power Circuit Record using connection records
			newCircuit.setCircuitTrace("," + circuitTrace + ",");

			if(powerCircuit.getPowerCircuitId() == null){
				Long cid = (Long)session.save(newCircuit);
				newCircuit.setPowerCircuitId(cid);
			}
			else{
				newCircuit.setPowerCircuitId(powerCircuit.getPowerCircuitId());
				session.save(newCircuit);
			}

			powerProc.lockCircuit(newCircuit);

			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL, this.getClass(), e));
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();

			throw be;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL, this.getClass(), e));
		}
		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return newCircuit.getPowerCircuitId();
	}


	/**
	 * edits existing circuit in the system.
	 * @param powerCircuit
	 * @return
	 * @throws DataAccessException
	 */
	@Override
	public long updatePowerCircuit(PowerCircuit powerCircuit) throws DataAccessException, BusinessValidationException {
		final String methodName = "updatePowerCircuit";
		String origUser = null;
		Timestamp origDate = null;

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			
			//Need to validate new circuit before deleting old circuit
			//addPowerCircuit will not validate again since this is not a new circuit
			powerProc.setupPowerSupplyAmpsValue(powerCircuit);
			List<String> errList = powerProc.validatePowerCircuit(powerCircuit);

			if(errList.size() > 0){
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));

				for(String s:errList){
					e.addValidationError(s);
				}

				throw e;
			}

			/* get userName and circuit creation timestamp from cricuit being updated  here */
			{
				Session session = this.sessionFactory.getCurrentSession();
				PowerCircuit pc = (PowerCircuit)session.load(PowerCircuit.class, powerCircuit.getPowerCircuitId());
				if (pc != null) {
					PowerConnection conn = pc.getStartConnection();
					if (conn != null) {
						origUser = conn.getCreatedBy();
						origDate = conn.getCreationDate();
						
					}
				}
			}
			
			//Load original circuit before update
			Long startPortId;
			String oldTrace;
			PowerCircuit oldCircuit = powerCircuitDAO.read(powerCircuit.getPowerCircuitId());
			startPortId = oldCircuit.getStartPortId();
			oldTrace = oldCircuit.getCircuitTrace();
			
			//Delete old circuit
			Collection<Long> recList = new ArrayList<Long>();
			recList.add(powerCircuit.getPowerCircuitId());
			deletePowerCircuitByIds(recList, true);

			//add circuit
			powerCircuit.validateCircuit = false;
			powerCircuit.setSharedCircuitTrace(null);
			addPowerCircuit(powerCircuit, origUser /* user who create the circuit */, origDate /* original circuit creation timestamp*/);
			
			powerCircuitDAO.processRelatedPowerCircuits(startPortId, oldTrace);
				
		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL, this.getClass(), e));
		}catch(BusinessValidationException be){
			be.printValidationErrors();
			be.printStackTrace();
			throw be;
		}catch(Exception ex){
			ex.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL, this.getClass(), ex));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return powerCircuit.getPowerCircuitId();
	}

	/**
	 * Delete the power circuits from the system
	 * @param circuitIdsToBeDeleted
	 * @throws DataAccessException
	 */
	@Override
	public Long deletePowerCircuitByIds(Collection<Long> circuitIdsToBeDeleted, boolean isUpdate) throws DataAccessException, BusinessValidationException{
		return circuitDelete.deletePowerCircuitByIds(circuitIdsToBeDeleted, isUpdate);
	}
	
	@Override
	public void validateCircuit(PowerCircuit circuit) throws DataAccessException, BusinessValidationException {
		List<String> errList = null;
		ApplicationCodesEnum code = null;
	
		powerProc.setupPowerSupplyAmpsValue((PowerCircuit)circuit);
		//PowerProc powerProc = new PowerProc(this.sessionFactory.getCurrentSession(), getMessageSource());
		errList = powerProc.validatePowerCircuit((PowerCircuit)circuit);
		code = ApplicationCodesEnum.PWR_CIR_SAVE_FAIL;
	
		if(errList.size() > 0) {
			BusinessValidationException e = new BusinessValidationException(new ExceptionContext(code.value(), this.getClass()));
			for(String s:errList){
				e.addValidationError(s);
			}
			throw e;
		}
	}

	@Override
	public List<MeItem> getExtraItemForPowerCircuit(PowerPort breakerPort) throws DataAccessException {
		/*SELECT  i.item_name as panel_name, source.item_name as usp_bank,  Breaker.port_power_id, pdu.item_name pdu_name, pdu.*
		FROM dct_ports_power Breaker
		INNER JOIN dct_items i ON Breaker.item_id = i.item_id
		INNER JOIN dct_items pb ON Breaker.item_id = pb.item_id
		INNER JOIN dct_items pdu ON pb.parent_item_id = pdu.item_id
		INNER JOIN dct_items_me ON pdu.item_id = dct_items_me.item_id
		INNER JOIN dct_items source ON dct_items_me.ups_bank_item_id = source.item_id
		WHERE Breaker.port_power_id = 7805
		*/
		List<MeItem> recList = null;

		try {
			Session session = this.sessionFactory.getCurrentSession();
			Long itemId = breakerPort.getItem().getItemId();

			MeItem itemPB = (MeItem) session.get(MeItem.class, itemId);

			itemId = itemPB.getParentItem().getItemId();
			MeItem itemPDU = (MeItem) session.get(MeItem.class, itemId);

			itemId = itemPDU.getUpsBankItem().getItemId();
			MeItem itemUPSBank = (MeItem) session.get(MeItem.class, itemId);

			recList = new ArrayList<MeItem>();
			recList.add(itemPB);
			recList.add(itemPDU);
			recList.add(itemUPSBank);
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return recList;
	}

	@Override
	public long getPowerPortUsedWatts(Long powerPortId, Long fuseLkuId){
		 return powerCircuitDAO.getPowerWattUsedTotal(powerPortId, fuseLkuId);
	}
	

	@Override
	public double getUpRatedFactorForSite(Long portId){
		return powerProc.getUpRatedFactorForSite(portId);
	}

	@Override
	public double getDeRatedFactorForSite(Long portId){
		return powerProc.getDeRatedFactorForSite(portId);
	}

	@Override
	public boolean isThreePhase(Long phaseLksId) {
		boolean threePhase = false;

		if (this.sessionFactory != null && phaseLksId != null){
			Session session = this.sessionFactory.getCurrentSession();
			LksData phaseLks = (LksData) session.get(LksData.class, phaseLksId);
			if (phaseLks.getLkpValueCode() == SystemLookup.PhaseIdClass.THREE_DELTA
					|| phaseLks.getLkpValueCode() == SystemLookup.PhaseIdClass.THREE_WYE){
				threePhase = true;
			}
		}

		return threePhase;
	}

	@Override
	public void deleteItemPowerConnections(long itemId) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		final String methodName = "deleteItemPowerConnections";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("sourcePowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.eq("item.itemId", itemId) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<PowerConnection> connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

			//For Power Panel, delete connections that end at the breaker
			criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("destPowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.eq("item.itemId", itemId) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);
	}

	@Override
	public void deleteItemsPowerConnections(List<Long> itemIds) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		final String methodName = "deleteItemPowerConnections";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("sourcePowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.in("item.itemId", itemIds) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<PowerConnection> connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

			//For Power Panel, delete connections that end at the breaker
			criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("destPowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.in("item.itemId", itemIds) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);
	}

	@Override
	public void deleteItemsPowerSourceConnections(List<Long> itemIds) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		final String methodName = "deleteItemPowerConnections";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("sourcePowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.in("item.itemId", itemIds) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<PowerConnection> connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);
	}

	
	@Override
	public void deleteItemsPowerDestConnections(List<Long> itemIds) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		final String methodName = "deleteItemPowerConnections";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("destPowerPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.createAlias("port.portSubClassLookup", "portSubClassLookup");
			criteria.add(Restrictions.in("item.itemId", itemIds) );
			// criteria.add(Restrictions.ne("portSubClassLookup.lkpValueCode", SystemLookup.PortSubClass.PANEL_BREAKER) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<PowerConnection> connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);
	}
	
	@Override
	public void deleteItemBuswayConnections(long itemId) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		final String methodName = "deleteItemBuswayConnections";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("sourcePowerPort", "port");
			criteria.createAlias("port.buswayItem", "busItem");
			criteria.add(Restrictions.eq("busItem.itemId", itemId) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<PowerConnection> connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

			//For Power Panel, delete connections that end at the breaker
			criteria = session.createCriteria(PowerConnection.class);
			criteria.createAlias("destPowerPort", "port");
			criteria.createAlias("port.buswayItem", "busItem");
			criteria.add(Restrictions.eq("busItem.itemId", itemId) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			connList = criteria.list();

			if(connList.size() > 0){
				for(PowerConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

					//free ports
					powerProc.lockPowerConn(xconn, false, currentDate, session);

					//delete connection record
					cord = xconn.getConnectionCord();
					session.delete(xconn);

					if(cord != null){
						session.delete(cord);
					}
				}
			}
			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CONN_DEL_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);
	}

	//Temp Fix for CR 49507
	public void freePort(IPortInfo port) throws DataAccessException {
		Timestamp updateDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		Session session = this.sessionFactory.getCurrentSession();

		if (port instanceof DataPort) {
			DataPort obj = (DataPort)port;
			obj.setUsed(false);
			obj.setUpdateDate(updateDate);
			session.update(obj);
		}
		else {
			PowerPort obj = (PowerPort)port;
			obj.setUsed(false);
			obj.setUpdateDate(updateDate);
			session.update(obj);
		}
	}
	
	private PowerCircuit createEndingPowerCircuit(long startPortId) throws DataAccessException{
		PowerCircuit circuit = new PowerCircuit();
		List<PowerConnection> recList = new ArrayList<PowerConnection>();
		
		CircuitProc circuitProc = new CircuitProc(this.sessionFactory.getCurrentSession());
		
		PowerConnection conn = (PowerConnection)circuitProc.getPortConnection(startPortId, SystemLookup.PortClass.POWER, true);
		
		while(conn != null){
			powerProc.lazyLoadPort(conn.getSourcePowerPort());
			powerProc.lazyLoadPort(conn.getDestPowerPort());

			recList.add(conn);
			
			conn = (PowerConnection)circuitProc.getPortConnection(conn.getDestPortId(), SystemLookup.PortClass.POWER, true);
		}
		
		if(recList.size() > 0 ){	
			conn = recList.get(recList.size() - 1);
					
			if(conn.getDestPowerPort() != null){
				PowerConnection dc = new PowerConnection();
				dc.setSourcePowerPort(conn.getDestPowerPort());
				dc.setConnectionType(conn.getConnectionType());
				dc.setPowerConnectionId(conn.getPowerConnectionId());
				recList.add(dc);
			}
		}
		
		circuit.setCircuitConnections(recList);
		circuit.setCircuitTrace("");
		circuit.setPowerCircuitId(-1L);
		circuit.setImplicit(true);
		
		if(recList.size() > 0 ){			
			circuit.setStartConnection(recList.get(0));
			circuit.setEndConnection(recList.get(recList.size() - 1));
		}	
		
		return circuit;
	}
		
	@SuppressWarnings("deprecation")
	@Override
	public void validatePowerCircuit (PowerCircuit powerCircuit) throws ServiceLayerException {
		List<String> errList = powerProc.validatePowerCircuit(powerCircuit);

		if(errList.size() > 0){
			BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_SAVE_FAIL.value(), this.getClass()));

			for(String s:errList){
				e.addValidationError(s);
			}

			throw e;
		}
	}

	@Override
	public void reconnectPowerPorts (ICircuitInfo proposedCircuit, CircuitViewData circuitView) throws ServiceLayerException {
		// get first node in the connection and call reconnectPowerPorts to build new trace		 
		ICircuitConnection proposed = proposedCircuit.getCircuitConnections().get(0);	
		ICircuitConnection orig = powerConnDAO.getConn(circuitView.getStartConnId());			
		
		if (orig != null && proposed != null) { 
			long origSrcPortId = orig.getSourcePortId();
			long origDestPortId = orig.getDestPortId();
			long propSrcPortId = proposed.getSourcePortId();
			long propDestPortId = proposed.getDestPortId();
			
			powerCircuitDAO.reconnectPowerPorts (origSrcPortId, origDestPortId, propSrcPortId, propDestPortId);
		}
	}

	private PowerCircuit createPartialCircuit(PowerCircuit circuit) throws DataAccessException{
		//create partial circuit
		PowerCircuit partialCircuit = new PowerCircuit();
		List<PowerConnection> connList = new ArrayList<PowerConnection>();
		PowerConnection oldConn, newConn;

		for(int i=2; i<circuit.getCircuitConnections().size(); i++){ //skip first and second connections
			oldConn = circuit.getCircuitConnections().get(i);
			newConn = new PowerConnection();
			newConn.setSourcePowerPort(oldConn.getSourcePowerPort());
			newConn.setDestPowerPort(oldConn.getDestPowerPort());
			newConn.setConnectionCord(oldConn.getConnectionCord());
			newConn.setConnectionType(oldConn.getConnectionType());

			connList.add(newConn);
		}
		partialCircuit.setStartConnection(connList.get(0));
		partialCircuit.setEndConnection(connList.get(connList.size() - 1));
		partialCircuit.setCircuitConnections(connList);

		return partialCircuit;
	}
	
	private PowerCircuit getPartialCircuit(PowerCircuit powerCircuit, String userName, Timestamp timeStamp) throws DataAccessException, BusinessValidationException {
		PowerCircuit partialCircuit = null;

		preLoadCircuitPorts(powerCircuit);
		
		if(powerCircuit.isOutletPresent() == false){
			return null;
		}

		//Check to see if a circuit exist with the same start connection
		PowerPort powerPort = powerCircuit.getCircuitConnections().get(0).getDestPowerPort();

		if(powerPort.isRackPduOutlet() == false){
			return null;
		}

		//get input cord of rack pdu
		powerPort = powerCircuit.getCircuitConnections().get(1).getDestPowerPort();

		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setStartPortId(powerPort.getPortId());
		cCriteria.setUserInfo(powerCircuit.getUserInfo());
		
		for(PowerCircuit pc:viewPowerCircuitByCriteria(cCriteria)){
			//Circuit Exist, return
			partialCircuit = pc;
			break;
		}

		//PowerPort firstNode = powerCircuit.getCircuitConnections().get(0).getSourcePowerPort();

		//if(partialCircuit == null && firstNode.getPortSubClassLookup().getLkpValueCode() == SystemLookup.PortSubClass.POWER_SUPPLY){
		if(partialCircuit == null){
			partialCircuit = createPartialCircuit(powerCircuit);
			partialCircuit.validateCircuit = false;
			partialCircuit.setUserInfo(powerCircuit.getUserInfo());
			Long circuitId = addPowerCircuit(partialCircuit, userName, timeStamp);

			Session session = sessionFactory.getCurrentSession();

			partialCircuit = (PowerCircuit)session.get(PowerCircuit.class, circuitId);
		}
		
		return partialCircuit;
	}
	
	private HashMap<Long, String> getConnIdWihCircuitTrace(Long circuitType){
		HashMap<Long, String> traceList = new HashMap<Long, String>();

    	Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery("getPowerConnIdWihCircuitTraceQuery");

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			traceList.put((Long)row[0], (String)row[1]);
		}

	    return traceList;
	}

	private void preLoadCircuitPorts(PowerCircuit circuit){
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
