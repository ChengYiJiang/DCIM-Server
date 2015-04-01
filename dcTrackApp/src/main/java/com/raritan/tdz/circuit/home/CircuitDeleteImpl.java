package com.raritan.tdz.circuit.home;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.raritan.tdz.circuit.dao.PowerCircuitDAO;
import com.raritan.tdz.circuit.dto.CircuitCriteriaDTO;
import com.raritan.tdz.domain.CircuitViewData;
import com.raritan.tdz.domain.ConnectionCord;
import com.raritan.tdz.domain.DataCircuit;
import com.raritan.tdz.domain.DataConnection;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerCircuit;
import com.raritan.tdz.domain.PowerConnection;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.exception.DataAccessException;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.SystemLookup.ItemStatus;
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
public class CircuitDeleteImpl implements CircuitDelete {
	private SessionFactory sessionFactory;
	private static Logger appLogger = Logger.getLogger(CircuitPDHomeImpl.class);

	@Autowired
	protected PowerProc powerProc;
	
	@Autowired
	protected CircuitSearch circuitSearch;
	
	@Autowired
	protected PowerCircuitDAO powerCircuitDAO;
	
	public CircuitDeleteImpl(SessionFactory sessionFactory){
		this.sessionFactory = sessionFactory;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.home.CircuitDelete#deletePowerCircuitByIds(java.util.Collection, boolean)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long deletePowerCircuitByIds(Collection<Long> circuitIdsToBeDeleted, boolean isUpdate) throws DataAccessException, BusinessValidationException{

		Session session = null;
		String errMsg = "";
		long retCode = 1;
		final String methodName = "deletePowerCircuitByIds";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			List<Long> recordIds = new ArrayList<Long>();

			if(circuitIdsToBeDeleted != null && circuitIdsToBeDeleted.size() > 0){
				session = this.sessionFactory.getCurrentSession();

				for(Long objectId : circuitIdsToBeDeleted){
					retCode = powerCircuitDAO.deleteCircuit(objectId, isUpdate);
					
					//If result return one than one record, circuit to be deleted is part of another circuit, avoid delete
					if(retCode == -1){
						CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
						BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_DEL_FAIL.value(), this.getClass()));
						PowerCircuit circuit = (PowerCircuit) session.get(PowerCircuit.class, objectId);
						
						cCriteria.setContainCircuitTrace(circuit.getCircuitTrace());
						cCriteria.setCircuitType(SystemLookup.PortClass.POWER);
						
						for(CircuitViewData circuitView:this.circuitSearch.searchCircuitsRaw(cCriteria)){
							if(circuitView.getCircuitId() != circuit.getPowerCircuitId()){
								errMsg = ApplicationCodesEnum.PWR_CIR_CANNOT_DEL_SHARED.value();
								errMsg = errMsg.replaceAll("<ItemName>", circuitView.getStartItemName());
								errMsg = errMsg.replaceAll("<PortName>", circuitView.getStartPortName());

								e.addValidationError(errMsg);
							}
						}

						e.setRecordIds(recordIds);
						retCode = 0;
						throw e;
					}
					recordIds.add(objectId);
					retCode = 1;
				}				
			}
		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_DEL_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.PWR_CIR_DEL_FAIL, this.getClass(), e));
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			be.printStackTrace();

			throw be;
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return retCode;
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.circuit.home.CircuitDelete#deleteDataCircuitByIds(java.util.Collection, boolean)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Long deleteDataCircuitByIds(Collection<Long> circuitIdsToBeDeleted, boolean isUpdate) throws DataAccessException, BusinessValidationException {
		Session session = null;
		Timestamp currentDate =	new Timestamp(java.util.Calendar.getInstance().getTimeInMillis());
		long retCode = 1;
		final String methodName = "deleteDataCircuitByIds";
		boolean isFanout = false;
		String errMsg = "";

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_ENTRY_MSG), appLogger);

		try{
			List<Long> recordIds = new ArrayList<Long>();
			CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
			cCriteria.setCircuitType(SystemLookup.PortClass.DATA);

			if(circuitIdsToBeDeleted != null && circuitIdsToBeDeleted.size() > 0){
				session = this.sessionFactory.getCurrentSession();
				
				for(Long objectId : circuitIdsToBeDeleted){
					DataCircuit circuit = (DataCircuit) session.get(DataCircuit.class, objectId);
					List<CircuitViewData> partialCircuitList = new ArrayList<CircuitViewData>();

					if(circuit != null){
						String circuitTrace = circuit.getCircuitTrace();
						isFanout = isFanoutCircuit(circuit, session);
						
						if(circuitTrace != null && circuitTrace.length() > 0){
							cCriteria.setContainCircuitTrace(circuitTrace);
							partialCircuitList = this.circuitSearch.searchCircuitsRaw(cCriteria);

							//If result return one than one record, circuit to be deleted is part of another circuit, avoid delete
							if(partialCircuitList.size() > 1 && isUpdate == false){
								BusinessValidationException e = new BusinessValidationException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_DELETE_FAIL.value(), this.getClass()));

								e.addValidationError(e.getMessage());
								e.addValidationError("Circuit.cannotDelete", e.getMessage());
								for(CircuitViewData circuitView:partialCircuitList){
									if(circuitView.getCircuitId() != circuit.getDataCircuitId()){
										errMsg = ApplicationCodesEnum.DATA_CIR_CANNOT_DEL_SHARED.value();
										errMsg = errMsg.replaceAll("<ItemName>", circuitView.getStartItemName());
										errMsg = errMsg.replaceAll("<PortName>", circuitView.getStartPortName());

										e.addValidationError(errMsg);
									}
								}

								if (null != e.getValidationErrors() && e.getValidationErrors().size() > 0) {
									e.addValidationError("Circuit.cannotDelete", e.getValidationErrors().toString());
								}
								
								e.setRecordIds(recordIds);
								retCode = 0;
								throw e;
							}

							//Set to null the end connection id that matched the partial circuits
							for(CircuitViewData circuitView:partialCircuitList){
								if(circuitView.getCircuitId() != circuit.getDataCircuitId()){
									DataCircuit cx = (DataCircuit) session.get(DataCircuit.class, circuitView.getCircuitId());
									cx.setEndConnection(null);
									session.update(cx);
								}
							}

							//delete connection records
							DataConnection xconn;
							ConnectionCord cord;
							long stopConnectionId = circuit.getShareStartConnId();

							for(long connectionId:circuit.getConnListFromTrace()){
								if(connectionId == stopConnectionId){
									break;
								}

								if(isSharedDataConnection(connectionId)){ //check again, in case circuit trace is bad
									break;
								}

								xconn = (DataConnection)session.get(DataConnection.class, connectionId);

								if(xconn != null){
									if(xconn.isLinkTypeExplicit() || isFanout){
										//delete connection record
										cord = xconn.getConnectionCord();
										session.delete(xconn);

										if(cord != null){
											session.delete(cord);
										}
										
										if(isFanout) {
											DataPort port = xconn.getSourceDataPort();
											port.setLinkId(null);
											session.update(port);
										}
									}
									else{
										xconn.setCircuitDataId(null);
										xconn.setStatusLookup(SystemLookup.getLksData(session, SystemLookup.ItemStatus.INSTALLED));
										session.update(xconn);
									}
								}
							}
						}
					}


					//delete circuit record
					if(circuit != null){
						if(isUpdate){
							circuit.setCircuitConnections(null);
							circuit.setStartConnection(null);
							circuit.setEndConnection(null);
							circuit.setCircuitTrace(null);
							session.update(circuit);
						}
						else{
							session.delete(circuit);
						}

						session.flush();
					}

					recordIds.add(objectId);
				}

			}
		}catch(HibernateException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_DELETE_FAIL, this.getClass(), e));

		}catch(org.springframework.dao.DataAccessException e){
			e.printStackTrace();

			throw new DataAccessException(new ExceptionContext(ApplicationCodesEnum.DATA_CIR_DELETE_FAIL, this.getClass(), e));
		}
		catch(BusinessValidationException be){
			be.printValidationErrors();
			be.printStackTrace();

			throw be;
		}

		LogManager.debug(new MessageContext(this.getClass(),methodName, GlobalConstants.METHOD_EXIT_MSG), appLogger);

		return retCode;
	}

	private boolean isSharedDataConnection(Long connectionId) throws DataAccessException{
		CircuitCriteriaDTO cCriteria = new CircuitCriteriaDTO();
		String containCircuitTrace = "," + connectionId.toString() + ",";
		cCriteria.setContainCircuitTrace(containCircuitTrace);
		cCriteria.setCircuitType(SystemLookup.PortClass.DATA);

		List<CircuitViewData> partialCircuitList = this.circuitSearch.searchCircuitsRaw(cCriteria);

		if(partialCircuitList.size() > 1){
			return true;
		}

		return false;
	}

	
	private boolean isFanoutCircuit(DataCircuit circuit, Session session) {
		//Circuit is a fanout if first port is a panel and second port is a network
		DataConnection connection = (DataConnection)session.load(DataConnection.class, circuit.getStartConnId());
		Long node1 = connection.getSourceDataPort().getItem().getClassLookup().getLkpValueCode();
		Long node2 = connection.getDestDataPort().getItem().getClassLookup().getLkpValueCode();
		
		return node1.equals(SystemLookup.Class.DATA_PANEL) && node2.equals(SystemLookup.Class.NETWORK);
	}
}
