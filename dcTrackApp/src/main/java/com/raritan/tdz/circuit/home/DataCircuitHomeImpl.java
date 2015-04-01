package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.ObjectError;

import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.util.ProposedCircuitHelper;
import com.raritan.tdz.circuit.validators.DataCircuitValidator;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.ConnectionToMove;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.GlobalConstants;
import com.raritan.tdz.util.LogManager;
import com.raritan.tdz.util.MessageContext;
/**
 *
 * @author Santo Rosario
 *
 */
//@Transactional(rollbackFor = DataAccessException.class)
public class DataCircuitHomeImpl implements DataCircuitHome {

	private SessionFactory sessionFactory;
	private static Logger appLogger = Logger.getLogger(CircuitPDHomeImpl.class);
	private MessageSource messageSource;

	@Autowired
	protected DataPortDAO dataPortDAO;
	
	@Autowired
	protected CircuitDelete circuitDelete;
	
	@Autowired
	protected DataCircuitDAO dataCircuitDAO;

	@Autowired
	private DataCircuitValidator dataCircuitValidator;

	public DataCircuitHomeImpl(SessionFactory sessionFactory, MessageSource messageSource){
		this.sessionFactory = sessionFactory;
		this.messageSource = messageSource;
	}


	//DATA CIRCUIT FUNCTIONS
	@Override
	public List<DataCircuit> viewDataCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException, BusinessValidationException {
		Session session = null;
		DataCircuit  dataCircuit = null;
		List<DataCircuit>  pcList = new ArrayList<DataCircuit>();
		final long proposedCircuitId = ProposedCircuitHelper.getProposedCircuitId( cCriteria );

		//Get Propose circuit
		if (proposedCircuitId > 0) {
			dataCircuit = getProposeCircuit( proposedCircuitId );
			if (dataCircuit == null) {
				String msg = messageSource.getMessage("circuit.proposedCircuitDoesNotExist", null, null);
				BusinessValidationException be = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				be.setErrorCode( ApplicationCodesEnum.DATA_CIR_DOES_NOT_EXIST.errCode() );
				be.addValidationError( msg );
				throw be;
			}
			dataCircuit.setProposed( true );
			pcList.add(dataCircuit);
			return pcList;
		}

		DataConnection connFound = null;
		boolean usingSourcePort = true;

		final String methodName = "viewDataCircuitByCriteria";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			CircuitProc circuitProc = new CircuitProc(this.sessionFactory.getCurrentSession());

			session = this.sessionFactory.getCurrentSession();
			Criteria cr = session.createCriteria(DataCircuit.class);

			//For a port, find the connection Id first, use Connection ID instead of port ID
			if(cCriteria.getPortId() != null && cCriteria.getPortId() != 0){
				DataConnection conn = (DataConnection)circuitProc.getPortConnection(cCriteria.getPortId(), SystemLookup.PortClass.DATA, true);

				if(conn == null && cCriteria.isSecondPortSearched() == false){
					//try again using dest port
					conn = (DataConnection)circuitProc.getPortConnection(cCriteria.getPortId(), SystemLookup.PortClass.DATA, false);

					if(conn == null){    //no connection found for this port
						return pcList;
					}
					usingSourcePort = false;
				}

				if(conn != null){
					cCriteria.setConnectionId(conn.getDataConnectionId());
					connFound = conn;
				}
				else{
					return pcList;
				}
			}
			else{
				//For a start port, find the connection Id first, use start connection ID instead of start port ID
				if(cCriteria.getStartPortId() != null && cCriteria.getStartPortId() != 0){
					DataConnection conn = (DataConnection)circuitProc.getPortConnection(cCriteria.getStartPortId(), SystemLookup.PortClass.DATA, true);

					if(conn == null){
						usingSourcePort = false;

						conn = (DataConnection)circuitProc.getPortConnection(cCriteria.getStartPortId(), SystemLookup.PortClass.DATA, false);
					}

					if(conn != null){
						cCriteria.setStartConnId(conn.getDataConnectionId());
						connFound = conn;
					}
					else{
						return pcList;
					}
				}
			}

			List<Long> circuitIdList = null;

			if(cCriteria.getItemId() != null && cCriteria.getItemId() != 0){
				circuitIdList = circuitProc.getCircuitIds(cCriteria.getItemId(), SystemLookup.PortClass.DATA);
				
				if(circuitIdList == null || circuitIdList.size() == 0) {
					return pcList;
				}
			}

			boolean restrictionsAppled = false;
			//circuit id is primary key, no need to search using other fields
			final long circuitId = cCriteria.getCircuitUID().getCircuitDatabaseId();
			if(circuitId > 0) {
				cr.add(Restrictions.eq("dataCircuitId", circuitId));
				restrictionsAppled = true;
			}
			else if(circuitIdList != null && circuitIdList.size() > 0){
				cr.add(Restrictions.in("dataCircuitId", circuitIdList));
				restrictionsAppled = true;
			}
			else if(cCriteria.getCircuitTrace() != null && cCriteria.getCircuitTrace().trim().length() > 0){
				cr.add(Restrictions.eq("circuitTrace", cCriteria.getCircuitTrace().trim()));
				restrictionsAppled = true;
			}
			else if(cCriteria.getContainCircuitTrace() != null && cCriteria.getContainCircuitTrace().trim().length() > 0){
				cr.add(Restrictions.like("circuitTrace", cCriteria.getContainCircuitTrace().trim(), MatchMode.ANYWHERE));
				restrictionsAppled = true;
			}
			else{
				cr.createAlias("startConnection", "startConn");
				cr.createAlias("endConnection","endConn");
				cr.createAlias("startConn.sourceDataPort", "startPort");

				if(cCriteria.getStartConnId() != null && cCriteria.getStartConnId() > 0){
					cr.add(Restrictions.eq("startConn.dataConnectionId", cCriteria.getStartConnId()));
					restrictionsAppled = true;
				}

				if(cCriteria.getEndConnId() != null && cCriteria.getEndConnId() > 0){
					cr.add(Restrictions.eq("endConn.dataConnectionId", cCriteria.getEndConnId()));
					restrictionsAppled = true;
				}

				if(cCriteria.getLocationId() != null && cCriteria.getLocationId() > 0){
					cr.createAlias("startConn.sourceDataPort.item", "startItem");
					cr.createAlias("startItem.dataCenterLocation", "location");
					cr.add(Restrictions.eq("location.dataCenterLocationId", cCriteria.getLocationId()));
					restrictionsAppled = true;
				}

				if(cCriteria.getConnectionId() != null && cCriteria.getConnectionId()> 0){
					cr.add(Restrictions.like("circuitTrace", "%," + cCriteria.getConnectionId() + ",%"));
					restrictionsAppled = true;
				}
			}

			cr.setFetchMode("startConnection", org.hibernate.FetchMode.JOIN);
			cr.setFetchMode("endConnection", org.hibernate.FetchMode.JOIN);
			
			//Get Records
			cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			List list = null;
			if (restrictionsAppled) {
				list = cr.list();
			}
			else {
				list = new ArrayList<>();  
			}

			String circuitIds = "";

			for(Object pcObject:list)
			{
				dataCircuit = (DataCircuit)pcObject;

				//Do allow duplicates circuit records
				if(circuitIds.indexOf(String.valueOf(dataCircuit.getDataCircuitId())) > 0){
					continue;
				}

				circuitIds = circuitIds + "|" + String.valueOf(dataCircuit.getDataCircuitId());

				List<DataConnection> dataConnections = new ArrayList<DataConnection>();

				//Load connection records
				for(long connectionId:dataCircuit.getConnListFromTrace()){
					DataConnection dataConnection = (DataConnection)session.get(DataConnection.class, connectionId);

					if(dataConnection != null){
						//force item to be loaded by hibernate
						lazyLoadPort(dataConnection.getSourceDataPort());
						lazyLoadPort(dataConnection.getDestDataPort());

						if(dataConnection.getConnectionCord() != null){
							dataConnection.getConnectionCord().getColorLookup();
							dataConnection.getConnectionCord().getCordLookup();
						}
						dataConnections.add(dataConnection);
					}
				}

				dataCircuit.setCircuitConnections(dataConnections);
				pcList.add(dataCircuit);
			}

			if(dataCircuit == null && connFound != null && cCriteria.isSecondPortSearched() == false){
				//create dummy circuit for connections missing dct_circuit record
				dataCircuit = createDummyCircuit(usingSourcePort, connFound);

				pcList.add(dataCircuit);
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_FETCH_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return pcList;
	}

	@Override
	//@Transactional(propagation = Propagation.REQUIRES_NEW)
	public long addDataCircuit(DataCircuit dataCircuit, String origUser, Timestamp origDate)	throws DataAccessException, BusinessValidationException {
		Session session = null;
		Long currentConnectId;
		int sortOrder = 1;
		String circuitTrace = "";
		final String methodName = "addDataCircuit";
		Timestamp creationDate = null;
		Timestamp updateDate = null;
		LksData connStatusLks = null;
		
		session = this.sessionFactory.getCurrentSession();
		
		if(dataCircuit.futureCircuitStatus != null) {
			connStatusLks = SystemLookup.getLksData(session, dataCircuit.futureCircuitStatus);								
		}else {
			connStatusLks = SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED);
		}
		
		//TODO: refactor updating CreatedBy and CreatedDate 
		// HACK HACK HACK
		// since update deletes original circuit and call add to add new circuit, 
		// original circuits Username and CreationDate is sent to this function.
		
		creationDate = updateDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		if (origDate != null) creationDate = origDate;
		if (origUser == null) origUser = dataCircuit.getUserInfo().getUserName();
 		
		DataCircuit newCircuit;
		
		if(dataCircuit.isNewCircuit() == false){
			newCircuit = (DataCircuit) session.get(DataCircuit.class, dataCircuit.getDataCircuitId());
			newCircuit.setCircuitTrace(null);
			newCircuit.setSharedCircuitTrace(null);
		}
		else{
			newCircuit = new DataCircuit();
		}

		try{
			CircuitProc circuitProc = new CircuitProc(this.sessionFactory.getCurrentSession());

			if(dataCircuit.validateCircuit){
				validateCircuit(dataCircuit);
			}

			String shareCircuitTrace = ",,";
			HashMap<Long, String> traceList = getConnIdWihCircuitTrace();
			DataCircuit partialCircuit = getPartialCircuit(dataCircuit);

			if(partialCircuit != null){
				shareCircuitTrace = partialCircuit.getCircuitTrace();
				newCircuit.setSharedCircuitTrace(shareCircuitTrace);
			}

			DataPort firstNode = dataCircuit.getCircuitConnections().get(0).getSourceDataPort();

			if(firstNode.getItem().getClassLookup().getLkpValueCode() != SystemLookup.Class.DATA_PANEL){ //do this for first node of circuit
				//Check to see if a circuit exist with the same start connection
				DataConnection dc = (DataConnection)circuitProc.getPortConnection(firstNode.getPortId(), newCircuit.getCircuitType(), true);

				if(dc != null){
					//Circuit Exist, return
					if(traceList.containsValue(dc.getDataConnectionId()) == false){
						String errMsg = ApplicationCodesEnum.DATA_CIR_PORT_START_CONN.value();
						errMsg = errMsg.replaceAll("<ItemName>", firstNode.getItem().getItemName());
						errMsg = errMsg.replaceAll("<PortName>", firstNode.getPortName());

						BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL.value(), this.getClass()));
						e.addValidationError(errMsg);
						throw e;
					}
				}
			}

			for(DataConnection conn:dataCircuit.getCircuitConnections()){
				//This code handle data panel connections
				DataConnection oldConn = (DataConnection)circuitProc.getPortConnection(conn.getSourceDataPort().getPortId(), com.raritan.tdz.lookup.SystemLookup.PortClass.DATA, true);

				if(oldConn != null){
					currentConnectId = oldConn.getDataConnectionId();

					if(newCircuit.getSharedCircuitTrace() == null){
						if(traceList.containsKey(currentConnectId)){
							shareCircuitTrace = traceList.get(currentConnectId);
							newCircuit.setSharedCircuitTrace(shareCircuitTrace);
						}
					}

					if(shareCircuitTrace.indexOf(currentConnectId.toString()) <= 0 ){
						oldConn.setSourceDataPort(conn.getSourceDataPort());
						oldConn.setDestDataPort(conn.getDestDataPort());
						oldConn.setConnectionType(conn.getConnectionType());
						oldConn.setCreatedBy(origUser);
						oldConn.setUpdateDate(updateDate);
						oldConn.setCreationDate(creationDate);						
						oldConn.setStatusLookup(connStatusLks);								
						oldConn.setSortOrder(sortOrder);
						oldConn.setCircuitDataId(null);

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

				//Save new Cord Information
				ConnectionCord cord = conn.getConnectionCord();

				if(cord != null){
					Long cordId = (Long)session.save(cord);
					cord.setCordId(cordId);
				}

				//Set Sort Order
				conn.setSortOrder(sortOrder);

				//Set Conn Type to Explicit by default
				if(conn.getConnectionType() == null){
					conn.setConnectionType( SystemLookup.getLksData(session, SystemLookup.LinkType.EXPLICIT));
				}

				//Set Status to New by default
				if(conn.getStatusLookup() == null){
					conn.setStatusLookup(connStatusLks);
				}

				conn.setCreatedBy(origUser);
				conn.setCreationDate(creationDate);
				conn.setUpdateDate(updateDate);
				conn.setCircuitDataId(null);

				currentConnectId = (Long)session.save(conn);
				conn.setDataConnectionId(currentConnectId);

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
			if(newCircuit.getEndConnection().getDestDataPort() != null){
				DataConnection conn = new DataConnection();
				conn.setSourceDataPort(newCircuit.getEndConnection().getDestDataPort());
				conn.setStatusLookup(connStatusLks);
				conn.setConnectionType(SystemLookup.getLksData(session, SystemLookup.LinkType.EXPLICIT));
				conn.setCreatedBy(origUser);
				conn.setCreationDate(creationDate);
				conn.setUpdateDate(updateDate);
				conn.setSortOrder(sortOrder);

				currentConnectId = (Long)session.save(conn);
				conn.setDataConnectionId(currentConnectId);

				circuitTrace = circuitTrace + "," + currentConnectId.toString();
				newCircuit.setEndConnection(conn);
			}

			//Create Data Circuit Record using connection records
			newCircuit.setCircuitTrace("," + circuitTrace + ",");

			if(dataCircuit.isNewCircuit()){
				Long cid = (Long)session.save(newCircuit);
				newCircuit.setDataCircuitId(cid);
			}
			else{
				newCircuit.setDataCircuitId(dataCircuit.getDataCircuitId());
				session.save(newCircuit);
			}

			lockCircuit(newCircuit);

			session.flush();

		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL, this.getClass(), e));
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			be.printStackTrace();

			throw be;
		}
		catch(Exception ex){
			ex.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL, this.getClass(), ex));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return newCircuit.getDataCircuitId();
	}


	@Override
	public Long deleteDataCircuitByIds(Collection<Long> circuitIdsToBeDeleted, boolean isUpdate) throws DataAccessException, BusinessValidationException {
		return circuitDelete.deleteDataCircuitByIds(circuitIdsToBeDeleted, isUpdate);
	}


	@Override
	public long updateDataCircuit(DataCircuit dataCircuit) throws DataAccessException, BusinessValidationException {
		final String methodName = "updateDataCircuit";
		String origUser = null;
		Timestamp origDate = null;

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			//Need to validate new circuit before deleting old circuit
			//addDataCircuit will not validate again since this is not a new circuit
			validateCircuit(dataCircuit);

			/* get userName and circuit creation timestamp from cricuit being updated  here */
			DataCircuit dc = (DataCircuit)session.load(DataCircuit.class, dataCircuit.getDataCircuitId());
			if (dc != null) {
				DataConnection conn = dc.getStartConnection();
				if (conn != null) {
					origUser = conn.getCreatedBy();
					origDate = conn.getCreationDate();
				}
			}

			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitId( dataCircuit.getCircuitUID().floatValue() );
			List<DataCircuit> cList = viewDataCircuitByCriteria(cCriteria);

			if(cList != null && cList.size() == 1){
				DataCircuit circuit = cList.get(0);
				String oldCircuitTrace = circuit.getCircuitTrace();
				long oldStartPortId = circuit.getFirstPortId();

				//Delete old circuit
				Collection<Long> recList = new ArrayList<Long>();
				recList.add(circuit.getDataCircuitId());

				deleteDataCircuitByIds(recList, true);

				//save the new circuit ID
				dataCircuit.validateCircuit = false;
				dataCircuit.setSharedCircuitTrace(null);

				cCriteria.setCircuitId( CircuitUID.getCircuitUID(addDataCircuit(dataCircuit, origUser /* original user who created the circuit */, origDate /* circuit creation date */), SystemLookup.PortClass.DATA) );
				dataCircuit.setDataCircuitId( cCriteria.getCircuitUID().getCircuitDatabaseId() );

				//Get the new circuit trace
				DataCircuit newCircuit = this.viewDataCircuitByCriteria(cCriteria).get(0);
				String newCircuitTrace = newCircuit.getCircuitTrace();

				//Find circuits that use old circuit trace
				cCriteria.clear();
				cCriteria.setContainCircuitTrace(oldCircuitTrace);

				long newStartPortId = newCircuit.getFirstPortId();

				for(DataCircuit subCircuit:this.viewDataCircuitByCriteria(cCriteria)){
					String ct = subCircuit.getCircuitTrace();

					if(ct.equals(newCircuitTrace) == false){
						ct = ct.replaceFirst(oldCircuitTrace, newCircuitTrace);
						subCircuit.setCircuitTrace(ct);
						subCircuit.setSharedCircuitTrace(newCircuitTrace);

						//Set the start and end connection id
						if(subCircuit.getStartConnId() > 0){
							DataConnection conn = (DataConnection)session.get(DataConnection.class, subCircuit.getStartConnId());
							subCircuit.setStartConnection(conn);

							conn = (DataConnection)session.get(DataConnection.class, subCircuit.getEndConnId());
							subCircuit.setEndConnection(conn);
						}

						session.update(subCircuit);

						//update connection record where the full circuit joins a partial circuit
						for(DataConnection conn:subCircuit.getCircuitConnections()){
							DataPort destDataPort = conn.getDestDataPort();

							if(destDataPort != null && oldStartPortId != newStartPortId){
								if(destDataPort.getPortId().longValue() == oldStartPortId ){
									destDataPort.setPortId(newStartPortId);
									conn.setDestDataPort(destDataPort);
									session.update(conn);

									break;
								}
							}
							else if(destDataPort != null && oldStartPortId == newStartPortId){
								if(destDataPort.getPortId().longValue() == oldStartPortId ){
									break;
								}
							}
						}

					}
				}
			}
			else{
				BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL.value(), this.getClass()));
				e.addValidationError("Invalid circuit ID. Circuit does not exist.");
				throw e;
			}
		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL, this.getClass(), e));
		}catch(BusinessValidationException be){
			be.printValidationErrors();
			be.printStackTrace();
			throw be;
		}catch(Exception ex){
			ex.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL, this.getClass(), ex));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return dataCircuit.getDataCircuitId();
	}

	@Override
	public DataConnection getConnForPanel(Long panelPortId) throws DataAccessException {
    	Session session = this.sessionFactory.getCurrentSession();
    	CircuitProc circuitProc = new CircuitProc(session);

    	DataConnection conn = (DataConnection) circuitProc.getPortConnection(panelPortId, SystemLookup.PortClass.DATA, false);

    	if(conn == null){
    		conn = (DataConnection) circuitProc.getPortConnection(panelPortId, SystemLookup.PortClass.DATA, true);
    	}

	    return conn;
	}

	@Override
	public DataConnection getPanelToPanelConn(Long portId) throws DataAccessException{
		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(DataConnection.class);
			criteria.add(Restrictions.eq("destDataPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List list = criteria.list();


			for(Object obj:list){
				DataConnection conn = (DataConnection)obj;

				if(conn.isLinkTypeImplicit()){
					return conn;
				}
			}

			//should get one record, for shared connections, return null
			criteria = session.createCriteria(DataConnection.class);
			criteria.add(Restrictions.eq("sourceDataPort.portId", portId) );
			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

			list = criteria.list();

			for(Object obj:list){
				DataConnection conn = (DataConnection)obj;

				if(conn.isLinkTypeImplicit()){
					return conn;
				}
			}
		}catch(HibernateException e){
			 e.printStackTrace();
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}

	//
	// Private methods
	//

	private long saveConnectionCord(long cordId, ConnectionCord cordInfo, ICircuitConnection conn) throws DataAccessException {
		long newCordId;

		try {
			Session session = sessionFactory.getCurrentSession();
			cordInfo.setCordId( cordId );

			if (cordId > 0) {
				session.merge( cordInfo );
			}
			else {
				session.save( cordInfo );
				conn.setConnectionCord( cordInfo );
				session.merge( conn );
			}

			newCordId = cordInfo.getCordId();
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return newCordId;
	}


	@Override
	public void deleteItemDataConnections(long itemId) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		final String methodName = "deleteItemDataConnections";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			Session session = this.sessionFactory.getCurrentSession();

			Criteria criteria = session.createCriteria(DataConnection.class);
			criteria.createAlias("sourceDataPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.eq("item.itemId", itemId) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			List<DataConnection> connList = criteria.list();

			criteria = session.createCriteria(DataConnection.class);
			criteria.createAlias("destDataPort", "port");
			criteria.createAlias("port.item", "item");
			criteria.add(Restrictions.eq("item.itemId", itemId) );

			criteria.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );
			connList.addAll(criteria.list());
			
			if(connList.size() > 0){
				for(DataConnection xconn:connList){
					//delete connection records
					ConnectionCord cord;

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

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_DELETE_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_DELETE_FAIL, this.getClass(), e));
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);
	}

	@Override
    public boolean isLogicalConnectionsExist(long bladeItemId, long chassisItemId) throws DataAccessException {
            final String methodName = "isLogicalConnectionsExist";

            LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

            boolean retValue = this.dataCircuitDAO.isLogicalConnectionsExist(bladeItemId, chassisItemId);
            
            return retValue;
    }

	@Override
	public Long getFanoutCircuitIdForStartPort(long portId) {
		return dataCircuitDAO.getFanoutCircuitIdForStartPort(portId);
	}
	
	

	private DataCircuit createDummyCircuit(boolean usingSourcePort, DataConnection connFound) throws DataAccessException, BusinessValidationException {
		DataCircuit dataCircuit;

		lazyLoadPort(connFound.getSourceDataPort());
		lazyLoadPort(connFound.getDestDataPort());

		//create dummy circuit for connections missing dct_circuit record
		//try a second time using dest_port_id if not null
		//windows client does not set circuit_trace to include connection between cp to faout
		if(usingSourcePort && connFound.getDestDataPort() != null){
			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setStartPortId(connFound.getDestDataPort().getPortId());
			cCriteria.setSecondPortSearched(true);

			List<DataCircuit> tempList = viewDataCircuitByCriteria(cCriteria);

			if(tempList.size() > 0){
				//insert connection at start of list
				dataCircuit = tempList.get(0);
				String circuitTrace = dataCircuit.getCircuitTrace();

				List<DataConnection> connList = dataCircuit.getCircuitConnections();

				//For data panel to data panel connection, the circuit trace might be missing the
				//implicit connection record, add it if necessary
				if(circuitTrace.indexOf(String.valueOf(connFound.getDataConnectionId())) < 1){
					connList.add(0, connFound);
				}

				return dataCircuit;
			}
		}

		if(usingSourcePort == false){//reverse port
			DataPort p = connFound.getDestDataPort();
			connFound.setDestDataPort(connFound.getSourceDataPort());
			connFound.setSourceDataPort(p);
		}

		dataCircuit = new DataCircuit();
		List<DataConnection> connList = new ArrayList<DataConnection>();

		dataCircuit.setCircuitTrace("");
		dataCircuit.setDataCircuitId(-1L);
		dataCircuit.setImplicit(true);

		connList.add(connFound);
		dataCircuit.setStartConnection(connFound);
		dataCircuit.setEndConnection(connFound);

		if(connFound.getDestDataPort() != null){
			DataConnection dc = new DataConnection();
			dc.setSourceDataPort(connFound.getDestDataPort());
			dc.setConnectionCord(connFound.getConnectionCord());
			dc.setConnectionType(connFound.getConnectionType());
			dc.setDataConnectionId(connFound.getDataConnectionId());
			dataCircuit.setEndConnection(dc);
			connList.add(dc);
		}

		dataCircuit.setCircuitConnections(connList);

		return dataCircuit;
	}
	


	public DataCircuit getProposeCircuit(Long proposeCircuitId) throws DataAccessException{
		try{
			DataCircuit circuit = new DataCircuit();
			List<DataConnection> connList = new ArrayList<DataConnection>();
			Session session = this.sessionFactory.getCurrentSession();
			
			Criteria criteria = session.createCriteria(ConnectionToMove.class);
			criteria.add(Restrictions.eq("connType", "data"));
			criteria.add(Restrictions.eq("newCircuitId", proposeCircuitId.intValue()));
			criteria.addOrder(Order.asc("newSortOrder"));

			List list = criteria.list();

			for(Object obj:list){
				ConnectionToMove c =  (ConnectionToMove)obj;

				DataConnection conn = newDataConnFromPropose(c);
				connList.add(conn);
			}

			if(connList.size() > 0){
				circuit.setCircuitConnections(connList);
				circuit.setDataCircuitId(proposeCircuitId);

				completeProposeCircuit(circuit);

				circuit.setStartConnection(connList.get(0));
				circuit.setEndConnection(connList.get(connList.size() - 1));

				return circuit;
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return null;
	}

	public void completeProposeCircuit(DataCircuit circuit) throws DataAccessException{
		try{
			Session session = this.sessionFactory.getCurrentSession();
			List<Long> newConnIdList = circuit.getConnList();
			long connectionId = newConnIdList.get(0);

			Criteria criteria = session.createCriteria(DataCircuit.class);
			criteria.add(Restrictions.like("circuitTrace", "," + String.valueOf(connectionId) + ",", MatchMode.ANYWHERE));

			//find old circuit
			List list = criteria.list();
			List<Long> oldConnIdList = null;

			for(Object obj:list){
				DataCircuit oldCircuit =  (DataCircuit)obj;

				if(oldCircuit.isConnectionInTrace(connectionId) && oldCircuit.isSharedConnection(connectionId) == false){
					oldConnIdList = oldCircuit.getConnListFromTrace();
					circuit.setSharedCircuitTrace(oldCircuit.getSharedCircuitTrace());
					break;
				}
			}

			if(oldConnIdList != null){
				int j = 0;
				List<DataConnection> mergeConnList = new ArrayList<DataConnection>();
				DataConnection conn = null;

				for(int i=0; i<oldConnIdList.size(); i++){
					if((j < newConnIdList.size()) && (newConnIdList.get(j).equals(oldConnIdList.get(i)))){
						conn = circuit.getCircuitConnections().get(j);
						mergeConnList.add(conn);
						j++;
					}
					else{
						conn = (DataConnection)session.get(DataConnection.class, oldConnIdList.get(i));
						mergeConnList.add(conn);
					}

					//System.out.println("Connection Id = " + conn.getConnectionId());
				}

				circuit.setCircuitConnections(mergeConnList);
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
	}

	public long saveProposeCircuit(DataCircuit circuit) throws DataAccessException{
		long proposeCircuitId = -1;
		boolean newCircuit = true;
		Session session = this.sessionFactory.getCurrentSession();
		
		if(circuit.getDataCircuitId() != null && circuit.getDataCircuitId() > 0){
			proposeCircuitId = circuit.getDataCircuitId();
			newCircuit = false;
		}

		try{
			List<DataConnection> connList = circuit.getCircuitConnections();
			Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());

			ConnectionToMove connToMove = null;
			int sortOrder = 1;
			int connStatusId = SystemLookup.getLksDataId(session, SystemLookup.ItemStatus.PLANNED).intValue();
			int circuitId = 0;
			CircuitProc circuitProc = new CircuitProc(session);

			for(DataConnection conn:connList){
				if(newCircuit){
					connToMove = new ConnectionToMove();
					connToMove.setEnteredBy(circuit.getUserInfo().getUserName());
					connToMove.setEnteredOn(currentDate);
					connToMove.setConnType("data");
					connToMove.setNewSortOrder(sortOrder);
					connToMove.setNewStatusId(connStatusId);

					Long id = (Long)session.save(connToMove);

					if(sortOrder == 1){
						circuitId = id.intValue();
						proposeCircuitId = id;
					}

					connToMove.setNewCircuitId(circuitId);

					DataConnection oldConn = (DataConnection)circuitProc.getPortConnection(conn.getSourceDataPort().getPortId(), SystemLookup.PortClass.DATA, true);

					if(oldConn != null){
						connToMove.setTblxConnectId(Long.valueOf(oldConn.getDataConnectionId()).intValue());
					}
				}
				else{
					connToMove =  (ConnectionToMove)session.get(ConnectionToMove.class, conn.getDataConnectionId());
				}

				connToMove.setPortMovingId(Long.valueOf(conn.getSourceDataPort().getPortId()).intValue());

				if(conn.getConnectionType() != null){
					connToMove.setConnTypeLksId(conn.getConnectionType().getLksId());
				}

				if(conn.getDestDataPort() == null){
					connToMove.setNewEndPointId(0);
				}
				else{
					connToMove.setNewEndPointId(Long.valueOf(conn.getDestDataPort().getPortId()).intValue());
				}


				connToMove.setNewComment(conn.getComments());

				if(conn.getConnectionCord() != null){
					connToMove.setNewConnectionLabel(conn.getConnectionCord().getCordLabel());
					connToMove.setNewConnectionLength(conn.getConnectionCord().getCordLength());
				}

				session.save(connToMove);


				//lock ports
				DataPort currentPort = (DataPort)session.get(DataPort.class, conn.getSourceDataPort().getPortId());

				if(currentPort.getItem().getClassLookup().getLkpValueCode() == SystemLookup.Class.DATA_PANEL &&
				   (conn.getDestDataPort() == null || sortOrder == 1)){
					lockDataConn(conn, false, currentDate, session);
				}
				else{
					lockDataConn(conn, true, currentDate, session);
				}

				session.flush();

				sortOrder++;
			}

		}catch(HibernateException e){

			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return proposeCircuitId;
	}

	public DataConnection newDataConnFromPropose(ConnectionToMove c) throws DataAccessException{
		try{
			Session session = this.sessionFactory.getCurrentSession();
			DataConnection conn = new DataConnection();
			Long portId = c.getPortMovingId().longValue();

			DataPort port = (DataPort)session.get(DataPort.class, portId);
			conn.setSourceDataPort(port);

			lazyLoadPort(port);

			if(c.getNewEndPointId() != null && c.getNewEndPointId() > 0){
				portId = c.getNewEndPointId().longValue();
				port = (DataPort)session.get(DataPort.class, portId);
				conn.setDestDataPort(port);

				lazyLoadPort(port);
			}

			conn.setComments(c.getNewComment());
			conn.setCreatedBy(c.getEnteredBy());
			conn.setCreationDate(c.getEnteredOn());
			conn.setDataConnectionId(c.getTblxConnectId().longValue());
			conn.setCircuitDataId(c.getNewCircuitId().longValue());
			conn.setSortOrder(c.getNewSortOrder());

			LksData lksData;

			if(c.getNewStatusId() == null){
				lksData = SystemLookup.getLksData(session, SystemLookup.ItemStatus.PLANNED);
			}
			else{
				lksData = (LksData)session.get(LksData.class, c.getNewStatusId().longValue());
			}
			conn.setStatusLookup(lksData);

			lksData = (LksData)session.get(LksData.class, c.getConnTypeLksId());
			conn.setConnectionType(lksData);

			DataConnection origConn = (DataConnection)session.get(DataConnection.class, c.getTblxConnectId().longValue());
			ConnectionCord cord = null;

			if (origConn != null) {
				cord = origConn.getConnectionCord();
			}

			if(c.getNewConnectionLabel() != null) {
				if (cord == null) {
					cord = new ConnectionCord();
				}
				cord.setCordLabel(c.getNewConnectionLabel());
			}

			if(c.getNewConnectionLength() != null){
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

	public DataCircuit createDummyCircuit(boolean usingSourcePort, DataConnection connFound, CircuitPDHome circuitHome) throws DataAccessException, BusinessValidationException {
		DataCircuit dataCircuit;

		lazyLoadPort(connFound.getSourceDataPort());
		lazyLoadPort(connFound.getDestDataPort());

		//create dummy circuit for connections missing dct_circuit record
		//try a second time using dest_port_id if not null
		//windows client does not set circuit_trace to include connection between cp to faout
		if(usingSourcePort && connFound.getDestDataPort() != null){
			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setStartPortId(connFound.getDestDataPort().getPortId());
			cCriteria.setSecondPortSearched(true);

			List<DataCircuit> tempList = viewDataCircuitByCriteria(cCriteria);

			if(tempList.size() > 0){
				//insert connection at start of list
				dataCircuit = tempList.get(0);
				String circuitTrace = dataCircuit.getCircuitTrace();

				List<DataConnection> connList = dataCircuit.getCircuitConnections();

				//For data panel to data panel connection, the circuit trace might be missing the
				//implicit connection record, add it if necessary
				if(circuitTrace.indexOf(String.valueOf(connFound.getDataConnectionId())) < 1){
					connList.add(0, connFound);
				}

				return dataCircuit;
			}
		}

		if(usingSourcePort == false){//reverse port
			DataPort p = connFound.getDestDataPort();
			connFound.setDestDataPort(connFound.getSourceDataPort());
			connFound.setSourceDataPort(p);
		}

		dataCircuit = new DataCircuit();
		List<DataConnection> connList = new ArrayList<DataConnection>();

		dataCircuit.setCircuitTrace("");
		dataCircuit.setDataCircuitId(-1L);
		dataCircuit.setImplicit(true);

		connList.add(connFound);
		dataCircuit.setStartConnection(connFound);
		dataCircuit.setEndConnection(connFound);

		if(connFound.getDestDataPort() != null){
			DataConnection dc = new DataConnection();
			dc.setSourceDataPort(connFound.getDestDataPort());
			dc.setConnectionCord(connFound.getConnectionCord());
			dc.setConnectionType(connFound.getConnectionType());
			dc.setDataConnectionId(connFound.getDataConnectionId());
			dataCircuit.setEndConnection(dc);
			connList.add(dc);
		}

		dataCircuit.setCircuitConnections(connList);

		return dataCircuit;
	}


	public void lazyLoadPort(DataPort port){
		if(port == null){
			return;
		}

		if(port.getItem() != null){
			port.getItem().getItemId();
		}

		if(port.getConnectorLookup() != null){
			port.getConnectorLookup().getConnCompatList();
			port.getConnectorLookup().getConnCompat2List();
		}

		if(port.getPortSubClassLookup() != null){
			port.getPortSubClassLookup().getLksId();
		}
	}

	public void lockCircuit(DataCircuit circuit) throws DataAccessException{
		List<Long> connIdList = circuit.getConnListFromTrace();
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		Long itemClassCode = null;
		Session session = this.sessionFactory.getCurrentSession();
		
		for(int i=0; i<connIdList.size(); i++){
			DataConnection xconn = (DataConnection)session.get(DataConnection.class, connIdList.get(i));

			if(xconn != null){
				Long portId = xconn.getSourcePortId();
				DataPort port = (DataPort)session.get(DataPort.class, portId);

				itemClassCode = port.getItem().getClassLookup().getLkpValueCode();

				if(xconn.getCircuitDataId() == null){ //set circuit_id
					xconn.setCircuitDataId(circuit.getDataCircuitId());
					session.save(xconn);
				}

				if(itemClassCode == SystemLookup.Class.DATA_PANEL &&  (xconn.getDestDataPort() == null || i == 0)){
					lockDataConn(xconn, false, currentDate, session);
				}
				else{
					lockDataConn(xconn, true, currentDate, session);
				}
			}
		}
	}

	public void lockDataConn(DataConnection conn, boolean lockPort, Timestamp updateDate, Session session) throws DataAccessException{
		if(updateDate == null){
			updateDate = new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		}

		if(conn.getSourceDataPort() != null){
			DataPort port = (DataPort)session.get(DataPort.class, conn.getSourceDataPort().getPortId());

			if(port.getUsed() != lockPort){ //update only if not the same
				port.setUsed(lockPort);
				port.setUpdateDate(updateDate);
				session.update(port);
			}

			//don't disconnect destination port when source port is Virtual or Logical
			if(port.isVirtual() || port.isLogical()){
			   return;
			}
		}

		if(conn.getDestDataPort() != null){
			DataPort port = (DataPort)session.get(DataPort.class, conn.getDestDataPort().getPortId());

			if(port.getUsed() != lockPort){ //update only if not the same
				port.setUsed(lockPort);
				port.setUpdateDate(updateDate);
				session.update(port);
			}
		}
	}

	public DataCircuit createPartialCircuit(DataCircuit circuit) throws DataAccessException{
		//create partial circuit
		DataCircuit partialCircuit = new DataCircuit();
		List<DataConnection> connList = new ArrayList<DataConnection>();
		DataConnection oldConn, newConn;

		for(int i=1; i<circuit.getCircuitConnections().size(); i++){ //skip first connection
			oldConn = circuit.getCircuitConnections().get(i);
			newConn = new DataConnection();
			newConn.setSourceDataPort(oldConn.getSourceDataPort());
			newConn.setDestDataPort(oldConn.getDestDataPort());
			newConn.setConnectionCord(oldConn.getConnectionCord());
			newConn.setConnectionType(oldConn.getConnectionType());

			connList.add(newConn);
		}
		partialCircuit.setCircuitConnections(connList);

		return partialCircuit;
	}

	public DataCircuit getPartialCircuit(DataCircuit dataCircuit) throws DataAccessException, BusinessValidationException {
		DataCircuit partialCircuit = null;

		DataPort firstNode = dataCircuit.getCircuitConnections().get(0).getSourceDataPort();  //first node must be a Virtual

		//circuit length must be greater than two nodes
		if((firstNode.isVirtual() || firstNode.isLogical()) && dataCircuit.getCircuitConnections().size() > 2){
			//Check to see if a circuit exist with the same start connection
			DataPort secondNode = dataCircuit.getCircuitConnections().get(0).getDestDataPort();

			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setStartPortId(secondNode.getPortId());
			cCriteria.setUserInfo(dataCircuit.getUserInfo());
			
			for(DataCircuit pc:viewDataCircuitByCriteria(cCriteria)){
				//Circuit Exist, return
				partialCircuit = pc;
				break;
			}

			if(partialCircuit == null){
				Session session = this.sessionFactory.getCurrentSession();
				partialCircuit = createPartialCircuit(dataCircuit);
				partialCircuit.validateCircuit = false;
				partialCircuit.setUserInfo(dataCircuit.getUserInfo());
				Long circuitId = addDataCircuit(partialCircuit, null, null);

				partialCircuit = (DataCircuit)session.get(DataCircuit.class, circuitId);
			}
		}
		
		return partialCircuit;
	}

	@Override
	public void validateCircuit(DataCircuit circuit) throws DataAccessException, BusinessValidationException{	
		Map<String, String> errorMap = new HashMap<String, String>();
		MapBindingResult errors = new MapBindingResult(errorMap, DataCircuit.class.getName());
		dataCircuitValidator.validate(circuit, errors);
		
		if (errors.hasErrors()){
			List<ObjectError> objectErrors = errors.getAllErrors();
			BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_SAVE_FAIL.value(), this.getClass()));
			
			for (ObjectError error: objectErrors){
				String msg = messageSource.getMessage(error, Locale.getDefault());
				e.addValidationError(msg);
				e.addValidationError(error.getCode(), msg);
			}
			
			throw e;
		}			
	}
	
	private HashMap<Long, String> getConnIdWihCircuitTrace(){
		HashMap<Long, String> traceList = new HashMap<Long, String>();

    	Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery("getDataConnIdWihCircuitTraceQuery");

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			traceList.put((Long)row[0], (String)row[1]);
		}

	    return traceList;
	}
}
