package com.raritan.tdz.circuit.home;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.changemgmt.home.ChangeMgmtHome26;
import com.raritan.tdz.circuit.dao.DataCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dao.PowerConnDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.circuit.dto.CircuitDTO;
import com.raritan.tdz.circuit.service.CircuitPDService;
import com.raritan.tdz.domain.CircuitUID;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.ConnectionToMove;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ICircuitConnection;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.IPortInfo;
import com.raritan.tdz.domain.LkuData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.UserInfo;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.exception.ServiceLayerException;
import com.raritan.tdz.home.DataCenterLocationHome;
import com.raritan.tdz.home.ItemHome;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.home.ItemObjectFactory;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.page.dto.ListCriteriaDTO;
import com.raritan.tdz.page.dto.ListResultDTO;
import com.raritan.tdz.page.dto.LookupOptionDTO;
import com.raritan.tdz.page.home.PaginatedHome;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.home.PortHome;
import com.raritan.tdz.user.home.UserHome;
import com.raritan.tdz.util.ApplicationCodesEnum;
import com.raritan.tdz.util.ExceptionContext;
import com.raritan.tdz.util.RequestDTO;
import com.raritan.tdz.vpc.home.VPCHome;
/**
 *
 * @author Santo Rosario
 *
 */
//@Transactional(rollbackFor = DataAccessException.class)
public class CircuitPDHomeImpl implements CircuitPDHome,PaginatedHome {

	private SessionFactory sessionFactory;
	private static Logger appLogger = Logger.getLogger(CircuitPDHomeImpl.class);
	private ItemHome itemHome = null;
	private ItemObjectFactory itemObjectFactory = null;
	private PortHome portHome = null;
	private ChangeMgmtHome26 changeMgmt = null;
	private DataCenterLocationHome dcHome = null;
	private CircuitSearch circuitSearch = null;
	private PaginatedHome paginatedHome;

	private MessageSource messageSource = null;
	PowerProc powerProc = null;

	@Autowired
	protected PowerPortDAO powerPortDAO;

	@Autowired
	protected DataPortDAO dataPortDAO;

	@Autowired
	protected PowerCircuitDAO powerCircuitDAO;
	
	@Autowired
	protected ItemDAO itemDAO;
	
	@Autowired
	protected CircuitDelete circuitDelete;
	
	@Autowired
	private VPCHome vpcHome;
	
	@Autowired
	protected DataCircuitDAO dataCircuitDAO;

	@Autowired
	protected PowerConnDAO powerConnDAO;
	
	@Autowired
	private UserHome userHome;
	
	@Autowired(required = true)
	DataCircuitHome dataCircuitHome;

	@Autowired(required = true)
	PowerCircuitHome powerCircuitHome;
	
	public CircuitPDHomeImpl(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	public void setItemObjectFactory(ItemObjectFactory itemObjectFactory) {
		this.itemObjectFactory = itemObjectFactory;
	}

	@Override
	public ItemObjectFactory getItemObjectFactory() {
		return itemObjectFactory;
	}

	public void setItemHome(ItemHome itemHome) {
		this.itemHome = itemHome;
	}

	public PortHome getPortHome() {
		return portHome;
	}

	public void setPortHome(PortHome portHome) {
		this.portHome = portHome;
	}

	public ChangeMgmtHome26 getChangeMgmt() {
		return changeMgmt;
	}

	public void setChangeMgmt(ChangeMgmtHome26 changeMgmt) {
		this.changeMgmt = changeMgmt;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public PowerProc getPowerProc() {
		return powerProc;
	}

	public void setPowerProc(PowerProc powerProc) {
		this.powerProc = powerProc;
	}

	public DataCenterLocationHome getDcHome() {
		return dcHome;
	}

	public void setDcHome(DataCenterLocationHome dcHome) {
		this.dcHome = dcHome;
	}

	public void setCircuitSearch(CircuitSearch circuitSearch) {
		this.circuitSearch = circuitSearch;
	}

	@Override
	public CircuitViewData getCircuitViewData(CircuitCriteriaDTO cCriteria) throws DataAccessException {
		List<CircuitViewData> pcList = viewCircuitPDList(cCriteria);

		if(pcList != null && pcList.size() > 0){
			return pcList.get(0);
		}

		return null;
	}

	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	public List<? extends ICircuitInfo> viewCircuitByCriteria(CircuitCriteriaDTO cCriteria) throws DataAccessException, BusinessValidationException {
		Long type = cCriteria.getCircuitType();
		if (type == null)
			throw new IllegalArgumentException();

		if (type == SystemLookup.PortClass.DATA) {
			return dataCircuitHome.viewDataCircuitByCriteria(cCriteria);
		}
		else if (type == SystemLookup.PortClass.POWER) {
			return powerCircuitHome.viewPowerCircuitByCriteria(cCriteria);
		}

		return new LinkedList<ICircuitInfo>();
	}


	@Override
	public void validateCircuit(ICircuitInfo circuit) throws DataAccessException, BusinessValidationException {
		List<String> errList = null;
		ApplicationCodesEnum code = null;

		if (circuit instanceof DataCircuit) {
			dataCircuitHome.validateCircuit((DataCircuit)circuit);
		}
		else {
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
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public ProposedCircuitInfo saveProposedCircuit(long circuitId, ConnectionRequest disconnectReq, Long newCircuitId) throws DataAccessException {
		if (newCircuitId == null) newCircuitId = 0L;

		ProposedCircuitInfo propCircuit = new ProposedCircuitInfo();

		try {
			Session session = sessionFactory.getCurrentSession();
			UserInfo currentUserInfo =  disconnectReq.getUserInfo();
			Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());


			for(ICircuitConnection obj:disconnectReq.getConnections()){
				if(obj instanceof PowerConnection){
					PowerConnection conn = (PowerConnection)obj;
					conn.setSourcePowerPort(powerPortDAO.loadPort(conn.getSourcePortId()));
				}
				else{
					DataConnection conn = (DataConnection)obj;
					conn.setSourceDataPort(dataPortDAO.loadPort(conn.getSourcePortId()));
				}
			}
			
			for(ICircuitConnection obj:disconnectReq.getRelatedRequest().getConnections()){
				if(obj instanceof PowerConnection){
					PowerConnection conn = (PowerConnection)obj;
					conn.setSourcePowerPort(powerPortDAO.loadPort(conn.getSourcePortId()));
				}
				else{
					DataConnection conn = (DataConnection)obj;
					conn.setSourceDataPort(dataPortDAO.loadPort(conn.getSourcePortId()));
				}
			}
			
			ConnectionToMove connToMove = null;
			int sortOrder = 1;
			int connStatusId = SystemLookup.getLksDataId(session, SystemLookup.ItemStatus.PLANNED).intValue();

			ConnectionRequest reconnectReq = disconnectReq.getRelatedRequest();

			List<? extends ICircuitConnection> origConns = disconnectReq.getConnections();
			List<? extends ICircuitConnection> newConns = reconnectReq.getConnections();
			final int numConns = origConns.size();
			//boolean lockConnections = true;

			for (int i=0; i < numConns; i++) {
				ICircuitConnection origConn = origConns.get(i);
				ICircuitConnection newConn = newConns.get(i);

				connToMove = new ConnectionToMove();
				connToMove.setEnteredBy( currentUserInfo.getUserName()) ;
				connToMove.setEnteredOn( currentDate) ;
				connToMove.setConnType( newConn.getCircuitType().toLowerCase() );
				connToMove.setNewSortOrder( sortOrder++ );
				connToMove.setNewStatusId( connStatusId );
				connToMove.setTblxConnectId( ((Long)origConn.getConnectionId()).intValue() );

				if (newCircuitId > 0) {
					connToMove.setNewCircuitId( newCircuitId.intValue() );
				}

				connToMove.setPortMovingId( ((Long)newConn.getSourcePortId()).intValue() );
				connToMove.setNewEndPointId( ((Long)newConn.getDestPortId()).intValue() );

				connToMove.setNewComment( newConn.getComments() );

				if (newConn.getConnectionType() != null) {
					connToMove.setConnTypeLksId(newConn.getConnectionType().getLksId());
				}

				// Update cord information if there are changes
				ConnectionCord origCord = origConn.getConnectionCord();
				ConnectionCord newCord = newConn.getConnectionCord();
				if (diffConnectionCord(origCord, newCord)) {
					final String newCordLabel = newCord.getCordLabel();
					final int newCordLength = newCord.getCordLength();
					final LkuData cordLku = newCord.getCordLookup();
					if (cordLku != null) {
						connToMove.setNewConnectionType( cordLku.getLkuId().intValue() );
					}
					if (origCord == null || (newCordLabel != null && !newCordLabel.equals(origCord.getCordLabel()))) {
						connToMove.setNewConnectionLabel( newCordLabel );
					}
					if (origCord == null || (newCordLength != origCord.getCordLength())) {
						connToMove.setNewConnectionLength( newCordLength );
					}
				}

				if (newCircuitId > 0) {
					session.save( connToMove );
				}
				else {
					newCircuitId = (Long)session.save( connToMove );
					connToMove.setNewCircuitId( newCircuitId.intValue() );
					session.merge( connToMove );
				}

//				if (lockConnections) {
//					lockConnections = this.lockConnection(newConn);
//				}
				propCircuit.addConnToUpdate( newConn );
			}
		}
		catch(HibernateException e){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		propCircuit.setCircuitId( newCircuitId.longValue() );

		return propCircuit;
	}

	@Override
	@Transactional
	public ProposedCircuitInfo deleteProposedCircuit(CircuitDTO circuit) throws DataAccessException, BusinessValidationException {
		final CircuitUID circuitUID = circuit.getCircuitUID();

		ProposedCircuitInfo propCircuit = new ProposedCircuitInfo();
		propCircuit.setCircuitId( circuitUID.getCircuitDatabaseId() );

		// Load original circuit
		ICircuitInfo origCircuit = getOriginalCircuitForProposed(circuitUID.getCircuitDatabaseId(), circuit.getCircuitType());

		// Load the proposed circuit
		ICircuitInfo proposedCircuit = null;
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitType( circuit.getCircuitType() );
		cCriteria.setProposeCircuitId( circuit.getProposeCircuitId() );
		List<? extends ICircuitInfo> circuitList = this.viewCircuitByCriteria( cCriteria );
		if (circuitList != null && !circuitList.isEmpty()) {
			proposedCircuit = circuitList.get(0);
		}

		List<? extends ICircuitConnection> origConns = origCircuit.getCircuitConnections();
		List<? extends ICircuitConnection> propConns = proposedCircuit.getCircuitConnections();

		try {
			for (int i = 0; i < propConns.size(); i++) {
				ICircuitConnection propConn = propConns.get( i );
				ICircuitConnection origConn = i < origConns.size() ? origConns.get( i ) : null;

				if (propConn != null) {
					IPortInfo sourcePort = propConn.getSourcePort();

					// Delete all requests associated with proposed circuit.
					// This will internally remove connections to move and free up ports.
					Collection<Long> requestIds = changeMgmt.getRequestsForConnection( propConn );
					for (Long requestId : requestIds) {
						changeMgmt.deleteRequest(requestId, true);
					}

					// Compare source port with original connection to see what proposed ports can be free
					if (origConn != null) {
						IPortInfo origSourcePort = origConn.getSourcePort();
						if (sourcePort != null) {
							if (origSourcePort == null || (sourcePort.getPortId() != origSourcePort.getPortId())) {
								//only need to free up source port CR 49507
								freePort(propConn.getSourcePort());
							}
						}
					}
				}
			}
		}
		catch (HibernateException e){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch (org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return propCircuit;
	}


	@Override
	@Transactional(readOnly = true)
	public ICircuitConnection getConnection(ConnectionToMove connToMove) throws DataAccessException {
		if (connToMove == null) return null;
		ICircuitConnection conn = null;

		try {
			Session session = sessionFactory.getCurrentSession();
			Class<?> connClass = connToMove.isDataConnection() ? DataConnection.class : PowerConnection.class;
			conn = (ICircuitConnection)session.get(connClass, connToMove.getTblxConnectId().longValue());
		}
		catch(HibernateException e){
			 throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return conn;
	}

/*
	@Override
	public HashMap<Long, Item> getItemChassis(){
		HashMap<Long, Item> chassis = new HashMap<Long, Item>();

    	Session session = this.sessionFactory.getCurrentSession();

		Criteria cr = session.createCriteria(ItItem.class);
		cr.setFetchMode("bladeChassis", org.hibernate.FetchMode.JOIN);
		cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		for (Object rec:cr.list()) {
			ItItem item = (ItItem)rec;

			if(item.getBladeChassis() != null){
				chassis.put(item.getItemId(), item.getBladeChassis());
			}
		}

	    return chassis;
	}

	@Override
	public HashMap<Long, LkuData> getItemVmCluster() {
		HashMap<Long, LkuData> vmCluster = new HashMap<Long, LkuData>();

    	Session session = this.sessionFactory.getCurrentSession();

		Criteria cr = session.createCriteria(ItItem.class);
		cr.setFetchMode("vmClusterLookup", org.hibernate.FetchMode.JOIN);
		cr.setResultTransformer( Criteria.DISTINCT_ROOT_ENTITY );

		for (Object rec:cr.list()) {
			ItItem item = (ItItem)rec;

			if(item.getVmClusterLookup() != null){
				vmCluster.put(item.getItemId(), item.getVmClusterLookup());
			}
		}

	    return vmCluster;
	}
*/
	@Override
	public HashMap<Long, CircuitRequestInfo> getCircuitRequestInfo(Long circuitType, boolean useProposeCircuitId){
		HashMap<Long, CircuitRequestInfo> reqInfo = new HashMap<Long, CircuitRequestInfo>();

    	String queryName = "";

    	//this code will change once Change Management is implemented with the new tables structure
    	if(circuitType == SystemLookup.PortClass.DATA){
    		queryName = "getDataCircuitRequestInfoQuery";
    	}
    	else{
    		queryName = "getPowerCircuitRequestInfoQuery";
    	}

    	Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery(queryName);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			CircuitRequestInfo req = new CircuitRequestInfo(row);

			if(useProposeCircuitId){
				if(req.getProposeCircuitId() != null && req.getProposeCircuitId() > 0){
					reqInfo.put(req.getProposeCircuitId(), req);
				}
			}
			else{
				reqInfo.put(req.getCircuitId(), req);
			}
		}

	    return reqInfo;
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
	public List<ConnectedPort> viewAllConnectedPorts() throws DataAccessException {
		List<ConnectedPort> recList = new ArrayList<ConnectedPort>();
		Session session = this.sessionFactory.getCurrentSession();

		Query q = session.getNamedQuery("getConnectedPortQuery");
        List result = q.list();

        for(ListIterator iter = result.listIterator(); iter.hasNext(); ) {
          	Object[] col = (Object[]) iter.next();

          	ConnectedPort rec = new ConnectedPort();
          	rec.setCircuitId((Long)col[0]);
          	rec.setItemName((String)col[1]);
          	rec.setPortName((String)col[2]);
          	rec.setCircuitType((Long)col[3]);
          	rec.setLocationCode((String)col[4]);

          	recList.add(rec);
        }

		return recList;
	}

	@Override
	public CircuitRequestInfo getRequestInfoForCircuit(Long circuitId, Long circuitType, boolean useProposeCircuitId){
		HashMap<Long, CircuitRequestInfo> reqInfo;
		CircuitRequestInfo req  = new CircuitRequestInfo();

		reqInfo = getCircuitRequestInfo(circuitType, useProposeCircuitId);

		//Check to see if there are pending request
		if(reqInfo.get(circuitId) != null){
			req = reqInfo.get(circuitId);
		}

		return req;
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

	@Override
	@Transactional(readOnly = true)
	public boolean isPartialCircuitInUse(ICircuitInfo circuitInfo) throws DataAccessException {
		if (circuitInfo == null) return false;
		boolean isPartialCircuitInUse = false;

		try {
			Session session = this.sessionFactory.getCurrentSession();
			String query = getPartialCircuitQueryString( circuitInfo.getCircuitType() == SystemLookup.PortClass.DATA );
			Query q = session.createQuery( query );
			q.setString("trace", circuitInfo.getCircuitTrace());
			q.setLong("id", circuitInfo.getCircuitId());

			Long count = (Long)q.uniqueResult();
			isPartialCircuitInUse = count != null && count > 0;
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return isPartialCircuitInUse;
	}

	@Override
	public Set<String> getSharedDataCircuitTraces() throws DataAccessException {
		return getSharedCircuitTraces( true );
	}

	@Override
	public Set<String> getSharedPowerCircuitTraces() throws DataAccessException {
		return getSharedCircuitTraces( false );
	}

	@Override
	@Transactional(readOnly = true)
	public boolean validateCircuitForDisconnect(ICircuitInfo circuitInfo) throws DataAccessException, BusinessValidationException{
		boolean retval=true;

		if(circuitInfo != null){
			String sharedCircuitTrace = circuitInfo.getCircuitTrace();
			appLogger.debug("circuit: " +  circuitInfo.getCircuitId() + "trace: " + sharedCircuitTrace);

			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(circuitInfo.getCircuitType());
			List<CircuitViewData> partialCircuitList = new ArrayList<CircuitViewData>();

			cCriteria.setContainCircuitTrace(sharedCircuitTrace);
			partialCircuitList = this.viewCircuitPDList(cCriteria);

			//If paritalCircuitList size is 1, then there are other items that share circuit and we can disconnect it
			if(partialCircuitList.size() > 1){
				List<Long> recordIds = new ArrayList<Long>();

				String appCode;
				String appCodeReason;
				if(circuitInfo.getCircuitType() == SystemLookup.PortClass.DATA){
					appCode = ApplicationCodesEnum.DATA_CIR_DISCONNECT_FAILED.value();
					appCodeReason = ApplicationCodesEnum.DATA_CIR_CANNOT_DISCONNECT_SHARED.value();
				}else{
					appCode = ApplicationCodesEnum.PWR_CIR_DISCONNECT_FAILED.value();
					appCodeReason = ApplicationCodesEnum.PWR_CIR_CANNOT_DISCONNECT_SHARED.value();
				}
				BusinessValidationException	e = new BusinessValidationException(new ExceptionContext(appCode, this.getClass()));

				String errMsg = "The following items are using this connection:\n";
				e.addValidationError(errMsg);
				for(CircuitViewData circuitView:partialCircuitList){
					if(circuitView.getCircuitId() != circuitInfo.getCircuitId()){
						errMsg = appCodeReason;
						errMsg = errMsg.replaceAll("<ItemName>", circuitView.getStartItemName());
						errMsg = errMsg.replaceAll("<PortName>", circuitView.getStartPortName());

						e.addValidationError(errMsg);
						e.printValidationErrors();
					}
				}

				e.setRecordIds(recordIds);
				retval = false;
				throw e;
			}
		}
		return retval;
	}

	@Override
	public HashMap<Long, String> getConnIdWihCircuitTrace(Long circuitType){
		HashMap<Long, String> traceList = new HashMap<Long, String>();

    	String queryName = "";

    	//this code will change once Change Management is implemented with the new tables structure
    	if(circuitType == SystemLookup.PortClass.DATA){
    		queryName = "getDataConnIdWihCircuitTraceQuery";
    	}
    	else{
    		queryName = "getPowerConnIdWihCircuitTraceQuery";
    	}

    	Session session = this.sessionFactory.getCurrentSession();

    	Query query =  session.getNamedQuery(queryName);

		for (Object rec:query.list()) {
			Object[] row = (Object[]) rec;

			traceList.put((Long)row[0], (String)row[1]);
		}

	    return traceList;
	}

	public ConnectionRequest diffCircuit(ICircuitInfo origCircuit, ICircuitInfo proposedCircuit) throws DataAccessException, BusinessValidationException {
		ConnectionRequest disconnectReq = null;
		List<? extends ICircuitConnection> origConns = origCircuit.getCircuitConnections();
		List<? extends ICircuitConnection> proposedConns = proposedCircuit.getCircuitConnections();
		UserInfo userInfo = proposedCircuit.getUserInfo();
		
		/* Code not needed since DataProc.newDataCircuitFromNodes adds all the nodes.
		if (origCircuit.getCircuitType() == SystemLookup.PortClass.DATA) {
			proposedConns = augmentProposedDataConnections( proposedConns );
		}
		*/

		// This check is also handled on client, but caught here just in case...
		if (origConns.size() != proposedConns.size()) {
			String msg = messageSource.getMessage("circuit.proposedCircuitLengthDifferent", null, null);
			BusinessValidationException be = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
			be.addValidationError( msg );
			throw be;
		}

		// Track cord only changes
		List<ICircuitConnection> origCords = new LinkedList<ICircuitConnection>();
		List<ICircuitConnection> newCords = new LinkedList<ICircuitConnection>();

		int nodeDiffs = 0;

		for (int i=0; i< origConns.size(); i++) {
			boolean connChanged = false;
			ICircuitConnection orig = origConns.get(i);
			ICircuitConnection proposed = proposedConns.get(i);
			final long origSrcPortId = orig.getSourcePortId();
			final long propSrcPortId = proposed.getSourcePortId();
			final long origDestPortId = orig.getDestPortId();
			final long propDestPortId = proposed.getDestPortId();

			if (origSrcPortId != propSrcPortId) {
				nodeDiffs++;
			}

			if (origDestPortId != propDestPortId &&
					origSrcPortId != propSrcPortId) {
				// Source and destination changed
				connChanged = true;
			}
			else if (origDestPortId == propDestPortId &&
						origSrcPortId != propSrcPortId) {
				// Only source port changed
				connChanged = true;
			}
			else if (origDestPortId != propDestPortId &&
					origSrcPortId == propSrcPortId) {
				// Only destination port changed
				connChanged = true;
			}

			if (connChanged) {
				// Issue a pair of disconnect/reconnect requests if connection changed
				if (disconnectReq == null) {
					disconnectReq = new ConnectionRequest(orig, ConnectionRequest.Type.DISCONNECT_AND_MOVE, userInfo);
					disconnectReq.setRelatedRequest( new ConnectionRequest(proposed, ConnectionRequest.Type.RECONNECT, userInfo) );
				}
				else {
					disconnectReq.addConnection( orig );
					disconnectReq.getRelatedRequest().addConnection( proposed );
				}
			}
			else {
				// The connection didn't change, but check if the cord information changed
				ConnectionCord origCord = orig.getConnectionCord();
				ConnectionCord newCord = proposed.getConnectionCord();
				if (diffConnectionCord(origCord, newCord)) {
					origCords.add( orig );
					newCords.add( proposed );
				}
			}
		}

		// Process cord only changes for this connection.
		// This will update the cord information on the original installed circuit.
		final boolean cordsChanged = !origCords.isEmpty() && !newCords.isEmpty();
		if (cordsChanged) {
			for (int i=0; i<origCords.size(); i++) {
				ICircuitConnection origConn = origCords.get(i);
				ICircuitConnection newConn = newCords.get(i);
				long cordId = origConn.getConnectionCord() != null ? origConn.getConnectionCord().getCordId() : 0;
				saveConnectionCord( cordId, newConn.getConnectionCord(), origConn);
			}
		}
		else {
			// No cords changed - ensure that something changed, but not everything
			if (nodeDiffs == 0) {
				String msg = messageSource.getMessage("circuit.newProposedSameAsOriginal", null, null);
				BusinessValidationException be = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				be.addValidationError( msg );
				throw be;
			}
			if (nodeDiffs == origConns.size()) {
				String msg = messageSource.getMessage("circuit.editInstalledChangedAllItems", null, null);
				BusinessValidationException be = new BusinessValidationException(new ExceptionContext(msg, this.getClass()));
				be.addValidationError( msg );
				throw be;
			}
		}

		if (disconnectReq == null) {
			disconnectReq = new ConnectionRequest(ConnectionRequest.Type.DISCONNECT_AND_MOVE, userInfo);
			disconnectReq.setCordsChanged( cordsChanged );
			disconnectReq.setRelatedRequest(new ConnectionRequest(ConnectionRequest.Type.RECONNECT, userInfo));
		}

		return disconnectReq;
	}

	@Override
	@Transactional
	public boolean lockConnection(ICircuitConnection connection) throws DataAccessException {
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		Session session = this.sessionFactory.getCurrentSession();
		Long itemClassCode = null;
		int connNum = connection.getSortOrder() - 1;
		
		if (connection instanceof DataConnection) {			
			DataPort port = this.dataPortDAO.loadPort(connection.getSourcePort().getPortId());
			
			itemClassCode = port.getItem().getClassLookup().getLkpValueCode();
		}
		else {
			PowerPort port = this.powerPortDAO.loadPort(connection.getSourcePort().getPortId());
			
			itemClassCode = port.getItem().getClassLookup().getLkpValueCode();
			
			if (itemClassCode == SystemLookup.Class.RACK_PDU && (connection.getDestPort() == null || connNum == 1)) {
				powerProc.lockPowerConn((PowerConnection)connection, false, currentDate, session);
			}
			else {
				powerProc.lockPowerConn((PowerConnection)connection, true, currentDate, session);
			}

			if (itemClassCode == SystemLookup.Class.FLOOR_OUTLET) {
				return false; //don't change connection between outlet and breaker
			}
		}

		return true;
	}

	@Override
	public void unlockConnection(ICircuitConnection connection) throws DataAccessException {
		//do nothing, done automatically by trigger
	}

	public Long getProposedCircuitId(ICircuitInfo circuit) throws DataAccessException {
		if (circuit == null) return null;
		Long proposedCircuitId = null;
		String queryName = circuit.getCircuitType() == SystemLookup.PortClass.DATA ? "getProposedCircuitIdForDataCircuit" : "getProposedCircuitIdForPowerCircuit";

		try {
			Session session = this.sessionFactory.getCurrentSession();
			Query q = session.getNamedQuery( queryName );
			q.setLong("circuitId", circuit.getCircuitId());
			proposedCircuitId = (Long)q.uniqueResult();
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return proposedCircuitId;
	}

	@SuppressWarnings("unchecked")
	@Override
	public CircuitUID getOriginalCircuitIdForProposed(long proposedCircuitId, long circuitType) throws DataAccessException, BusinessValidationException {
		Long circuitId = null;
		String queryName = circuitType == SystemLookup.PortClass.DATA ? "getOriginalDataCircuitFromProposedId" : "getOriginalPowerCircuitFromProposedId";

		try {
			Session session = this.sessionFactory.getCurrentSession();
			Query q = session.getNamedQuery( queryName );
			q.setLong("proposedCircuitId", proposedCircuitId);
			List<Object[]> results = (List<Object[]>)q.list();

			if (results != null && !results.isEmpty()) {

				if (results.size() > 1) {
					for (Object[] result : results) {
						String sharedTrace = (String)result[1];
						if (sharedTrace == null || sharedTrace.trim().isEmpty()) {
							circuitId = (Long)result[0];
							break;
						}
					}
				}

				if (circuitId == null) {
					circuitId = (Long)results.get(0)[0];
				}
			}
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return new CircuitUID(circuitId, circuitType, false);
	}

	public ICircuitInfo getOriginalCircuitForProposed(long proposedCircuitId, long circuitType) throws DataAccessException, BusinessValidationException {
		ICircuitInfo circuit = null;
		CircuitUID uid = getOriginalCircuitIdForProposed(proposedCircuitId, circuitType);

		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		cCriteria.setCircuitId( uid.floatValue() );
		cCriteria.setCircuitType( circuitType );

		List<? extends ICircuitInfo> circuits = viewCircuitByCriteria( cCriteria );
		if (circuits != null && !circuits.isEmpty()) {
			circuit = circuits.get(0);
		}

		return circuit;
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

	/**
	 * Compare two connection cords. We are not relying on the equals method of the
	 * ConnectionCord object because it is not comparing the LKU values properly.
	 * @param origCord oroginal connection cord
	 * @param newCord proposed connection cord
	 * @return
	 */
	private boolean diffConnectionCord(ConnectionCord origCord, ConnectionCord newCord) {
		if (newCord == null) return false; // Cord did not change
		if (origCord == null && newCord != null) {
			return true;
		}
		else if (origCord != null && newCord != null) {
			final int origLength = origCord.getCordLength();
			final String origLabel = origCord.getCordLabel();
			final LkuData origCordLku = origCord.getCordLookup();
			final LkuData origColorLku = origCord.getColorLookup();
			final int newLength = newCord.getCordLength();
			final String newLabel = newCord.getCordLabel();
			final LkuData newCordLku = newCord.getCordLookup();
			final LkuData newColorLku = newCord.getColorLookup();

			if (origLength != newLength) {
				return true;
			}
			if ((origLabel == null && newLabel != null)||
				(origLabel != null && newLabel != null && !origLabel.equals(newLabel))) {
				return true;
			}
			if ((origCordLku == null && newCordLku != null) ||
					(origCordLku != null && newCordLku != null && !origCordLku.getLkuId().equals(newCordLku.getLkuId()))) {
				return true;
			}
			if ((origColorLku == null && newColorLku != null) ||
					(origColorLku != null && newColorLku != null && !origColorLku.getLkuId().equals(newColorLku.getLkuId()))) {
				return true;
			}
		}

		return false;
	}

	private String getPartialCircuitQueryString(boolean isDataCircuit) {
		StringBuffer b = new StringBuffer("select count(*) from ");
		b.append(isDataCircuit ? "DataCircuit" : "PowerCircuit");
		b.append(" where sharedCircuitTrace = :trace and ");
		b.append(isDataCircuit ? "dataCircuitId" : "powerCircuitId");
		b.append(" != :id");
		return b.toString();
	}

	private Set<String> getSharedCircuitTraces(boolean isDataCircuit) throws DataAccessException {
		Set<String> traces = new HashSet<String>();
		try {
			Session session = this.sessionFactory.getCurrentSession();
			StringBuffer query = new StringBuffer("select distinct(sharedCircuitTrace) from ");
			query.append(isDataCircuit ? "DataCircuit" : "PowerCircuit");
			query.append(" where sharedCircuitTrace is not null");
			Query q = session.createQuery( query.toString() );
			List<String> values = q.list();
			if (values != null) {
				traces.addAll( values );
			}
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		return traces;
	}
	
	@Override
	public HashMap<Long, Long> getProposeCircuitPortsNetWatts() {
	    return powerProc.getProposeCircuitPortsNetWatts();
	}

	@Override
	@Transactional(readOnly = true)
	public long getCircuitTotalCount() throws DataAccessException {
		//long time = System.currentTimeMillis();
		long total = 0;

		try {
			Session session = this.sessionFactory.getCurrentSession();
			Query q = session.createSQLQuery("select count(*) from dct_circuits_data");
			total = ((BigInteger)q.uniqueResult()).longValue();
			q = session.createSQLQuery("select count(*) from dct_circuits_power");
			total += ((BigInteger)q.uniqueResult()).longValue();
		}
		catch(HibernateException e) {
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}
		catch(org.springframework.dao.DataAccessException e){
			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.FAILURE, this.getClass(), e));
		}

		//System.out.println("Count Query: "+(System.currentTimeMillis()-time)+" ms");
		return total;
	}

	@Override
	@Transactional(readOnly = true)
	//User can operate on them if he is gatekeeper or if he or his team created the circuit
	public boolean canOperateOnCircuit(CircuitViewData circuitViewData, boolean isProposedCkt) throws DataAccessException{

		boolean canUserOperate = false;

		String createdBy = null;
		Long teamId = null;
		Long statusLksCode = null;


		//Let us first get the circuitView data since we need the user + team associated with that circuit
		//Get the team and createdBy
		createdBy = circuitViewData.getCreatedBy();
		teamId = circuitViewData.getTeamId();
		statusLksCode = circuitViewData.getStatusLksCode();

		//Now get the user Info from the current user session
		UserInfo userInfo = circuitViewData.getUserInfo();

		if (userInfo != null && (isProposedCkt || (statusLksCode != null && statusLksCode == SystemLookup.ItemStatus.INSTALLED)))
		{
			if (userInfo.isViewer())
				canUserOperate = false;
			else
				canUserOperate = true;

			return canUserOperate;
		}

		if (userInfo != null && userInfo.isAdmin()){ //USER_ACCESS_LEVEL_ADMIN
			//user is admin (s)he can do anything.
			canUserOperate = true;
		} else if (userInfo != null && userInfo.isViewer()){ //USER_ACCESS_LEVEL_VIEWER
			//User is viewer and cannot do anything.
			canUserOperate = false;
		} else if (userInfo != null && createdBy != null && userInfo.getUserName() != null && userInfo.getUserName().equals(createdBy)){
			//Logged in user has created the circuit and so can operate on it.
			canUserOperate = true;
		} else if (teamId != null){
			//Let us get the all users
			Collection<UserInfo> userInfos = this.itemHome.getAllUsers();

			//Based on that we have to find the permissions for the user who has created this circuit.
			for (UserInfo uInfo: userInfos){
				if (uInfo.getUserName() != null && createdBy != null && uInfo.getUserName().equals(createdBy)){
					if (userInfo.isManager() && userInfo.getTeamLookup().getLkuId().equals(teamId)
							&& !userInfo.isAdmin()){
						//if team is null means the ckt was creating by someone who is not associated with any team. Hence this manager cannot
						//delete it. If the ckt is created by a gatekeeper (even though he belongs to same group) then this manager cannot delete
						//it.
						canUserOperate = true;
						break;
					}
				}
			}
		}
		return canUserOperate;
	}

	@Override
	public boolean canCreateCircuit(){
		boolean canCreate = false;

		UserInfo userInfo = userHome.getCurrentUserInfo();

		if (userInfo != null){
			if (userInfo.isViewer()) { //User viewer
				canCreate = false;
			} else {
				canCreate = true;
			}
		}

		return canCreate;
	}

	@Override
	public List<CircuitViewData> viewCircuitPDList(CircuitCriteriaDTO cCriteria) throws DataAccessException {
		return circuitSearch.searchCircuitsRaw( cCriteria );
	}

	public void setPaginatedHome(PaginatedHome paginatedHome) {
		this.paginatedHome = paginatedHome;
	}

	@Transactional(readOnly = true)
	public ListResultDTO getPageList(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {

		return paginatedHome.getPageList(listCriteriaDTO,pageType);
	}

	@Transactional(readOnly = true)
	public List<LookupOptionDTO> getLookupOption(ListCriteriaDTO listCriteriaDTO,String pageType) throws DataAccessException {
		return paginatedHome.getLookupOption(listCriteriaDTO,pageType);
	}
	
	@Transactional(readOnly = true)
	public List<Map> getLookupData(String fieldName,String lkuTypeName,String pageType) throws DataAccessException {
		return paginatedHome.getLookupData(fieldName,lkuTypeName,pageType);
	}

	@Transactional(readOnly = true)
	public ListCriteriaDTO getUserConfig(String pageType) throws DataAccessException{
		return paginatedHome.getUserConfig(pageType);
	}

	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int saveUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return paginatedHome.saveUserConfig(itemListCriteria, pageType);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int deleteUserConfig(ListCriteriaDTO itemListCriteria, String pageType) throws DataAccessException{
		return paginatedHome.deleteUserConfig(itemListCriteria, pageType);
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public ListCriteriaDTO resetUserConfig(ListCriteriaDTO itemListCriteria, String pageType, int fitRows) throws DataAccessException{
		return paginatedHome.resetUserConfig(itemListCriteria, pageType, fitRows);
	}

	@Transactional(readOnly = true)
	public Map getColumnGroup(String pageType) throws DataAccessException {
		return paginatedHome.getColumnGroup(pageType);
	}

	@Transactional(readOnly = true)
	public List<Map> getValueList(ListCriteriaDTO listCriteriaDTO,String pageType) {
		return paginatedHome.getValueList(listCriteriaDTO,pageType);
	}

	public String getItemActionMenuStatus( List<Long> itemIdList ) {
		return paginatedHome.getItemActionMenuStatus( itemIdList );
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
	
	
	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public Map<String, Object> createCircuitRequest(CircuitPDService circuitService, Collection<CircuitCriteriaDTO> requestList, boolean warningConfirmed) throws ServiceLayerException { 
	
		return circuitService.createCircuitRequest(requestList, warningConfirmed);
	}

	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public List<RequestDTO> postProcessRequest(CircuitPDService circuitService, Map<String, Object> requestMap) throws ServiceLayerException {

		return circuitService.postProcessRequest(requestMap);
	}

	@Transactional(noRollbackFor=BusinessValidationException.class, propagation=Propagation.REQUIRES_NEW)
	@Override
	public Map<String, Object> saveCircuitWithDTO(CircuitPDService circuitService, CircuitDTO circuit) throws ServiceLayerException {
		
		return circuitService.saveCircuitWithDTO(circuit);
	}
	
}
