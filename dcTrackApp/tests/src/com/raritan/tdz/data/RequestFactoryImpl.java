package com.raritan.tdz.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.domain.ICircuitInfo;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.LksData;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.domain.Request;
import com.raritan.tdz.domain.RequestHistory;
import com.raritan.tdz.domain.RequestPointer;
import com.raritan.tdz.exception.BusinessValidationException;
import com.raritan.tdz.item.dao.ItemDAO;
import com.raritan.tdz.item.request.ItemRequest;
import com.raritan.tdz.item.request.ItemRequestDAO;
import com.raritan.tdz.lookup.SystemLookup;
import com.raritan.tdz.lookup.dao.SystemLookupFinderDAO;
import com.raritan.tdz.move.dao.PortMoveDAO;

public class RequestFactoryImpl implements RequestFactory {
	@Autowired
	ItemRequestDAO itemRequestDAO;
	
	@Autowired
	private ItemDAO itemDAO;
	
	@Autowired
	private SystemLookupFinderDAO systemLookupDAO;
	
	private PortMoveDAO<DataPort> dataPortMoveDAO;	
	
	private PortMoveDAO<PowerPort> powerPortMoveDAO;
	
	private GenericObjectSave requestSave;
	
	public GenericObjectSave getRequestSave() {
		return requestSave;
	}

	public void setRequestSave(GenericObjectSave requestSave) {
		this.requestSave = requestSave;
	}

	public class RequestInfo{
		@Override
		public String toString() {
			return "RequestInfo [reqTypeLks=" + reqTypeLks + ", requestType="
					+ requestType + "]";
		}
		public LksData reqTypeLks;
		public String requestType;
	}
	
	HashMap<Long,RequestInfo> mapStatusToReqType;
	
	public RequestFactoryImpl() {
		
	}
	
	

	public RequestFactoryImpl(PortMoveDAO<DataPort> dataPortMoveDAO,
			PortMoveDAO<PowerPort> powerPortMoveDAO) {
		super();
		this.dataPortMoveDAO = dataPortMoveDAO;
		this.powerPortMoveDAO = powerPortMoveDAO;
	}

	@Override
	public Long save(Request request) {
		return requestSave.save(request);
	}
	
	@Override
	public Request createRequestForGoingToItemStatus(Item item, Long statusValueCode){
		if(mapStatusToReqType.containsKey(statusValueCode)){
			RequestInfo info = mapStatusToReqType.get(statusValueCode);
			
			return createRequest(item, info.requestType, info.reqTypeLks);
		}
		
		return null;
	}
	
	@Override
	public Request createRequestInstalled(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.NEW_ITEM).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.installItem, reqTypeLks);
	}
	
	@Override
	public Request createRequestMove(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_MOVE).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.moveItem, reqTypeLks);
	}
	
	@Override
	public Request createRequestOnSite(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_ON_SITE).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.bringItemOnSite, reqTypeLks);
	}
		
	@Override
	public Request createRequestToArchive(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.decomissionToArchive, reqTypeLks);
	}
	
	@Override
	public Request createRequestToStorage(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.decomissionToStorage, reqTypeLks);
	}
	
	@Override
	public Request createRequestPowerOff(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.powerOff, reqTypeLks);
	}
	
	@Override
	public Request createRequestPowerOn(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_POWER_ON).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.powerOn, reqTypeLks);
	}
	
	@Override
	public Request createRequestOffSite(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_OFF_SITE).get(0);
		return createRequest(item, ItemRequest.ItemRequestType.takeItemOffSite, reqTypeLks);
	}

	@Override
	public List<Request> createRequestConvertToVM(Item item){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.CONVERT_TO_VM).get(0);
		Request req = null;
		List<Request> reqList = new ArrayList<Request>();
		
		try {
			Long vmItemId = itemRequestDAO.createVmItem(item.getItemId());
			Item vmItem = itemDAO.loadItem(vmItemId);
			
			req = createRequest(vmItem, ItemRequest.ItemRequestType.convertToVM, reqTypeLks);
			reqList.add(req);
			
			req = createRequestToStorage(item);
			reqList.add(req);
		} catch (BusinessValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return reqList;
	}
	

	@Override
	public Request createRequestDisconnect(ICircuitInfo circuit){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.DISCONNECT).get(0);
		
		String tableName = circuit.getCircuitType().equals(SystemLookup.PortClass.DATA) ? "dct_ports_data" : "dct_ports_power";
		
		return createRequest(circuit, ItemRequest.ItemRequestType.disconnect, reqTypeLks, tableName);
	}

	@Override
	public Request createRequestConnect(ICircuitInfo circuit){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.CONNECT).get(0);
		
		String tableName = circuit.getCircuitType().equals(SystemLookup.PortClass.DATA) ? "dct_circuits_data" : "dct_circuits_power";
		
		return createRequest(circuit, ItemRequest.ItemRequestType.connect, reqTypeLks, tableName);
	}
	
	@Override
	public Request createRequestReConnect(ICircuitInfo circuit){
		LksData reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.RECONNECT).get(0);
		
		String tableName = circuit.getCircuitType().equals(SystemLookup.PortClass.DATA) ? "dct_circuits_data" : "dct_circuits_power";
		
		return createRequest(circuit, ItemRequest.ItemRequestType.reconnect, reqTypeLks, tableName);
	}	
	
	@Override
	public Request createRequest(Item item, String reqType, LksData reqTypeLks){
		String requestNo = requestSave.getNextRequestNo();
		Request requestObj = new Request();
		//requestObj.setRequestId(item.getItemId() + 10000);
		requestObj.setItemId(item.getItemId());
		requestObj.setRequestNo(requestNo);
		requestObj.setRequestType(reqType);
		requestObj.setRequestTypeLookup(reqTypeLks);
		requestObj.setDescription("Testing " + reqType);
		
		createHistory(requestObj, SystemLookup.RequestStage.REQUEST_ISSUED);
		createRequetPointer(requestObj, "tblEntity");
		
		save(requestObj);
		
		return requestObj;
	}


	public Request createRequest(ICircuitInfo circuit, String reqType, LksData reqTypeLks, String tableName){
		String requestNo = requestSave.getNextRequestNo();
		Request requestObj = new Request();
		//requestObj.setRequestId(item.getItemId() + 10000);
		requestObj.setItemId(circuit.getStartItemId());
		requestObj.setRequestNo(requestNo);
		requestObj.setRequestType(reqType);
		requestObj.setRequestTypeLookup(reqTypeLks);
		requestObj.setDescription("Testing " + reqType);
		
		createHistory(requestObj, SystemLookup.RequestStage.REQUEST_ISSUED);
		RequestPointer p = createRequetPointer(requestObj, tableName);
		
		if(requestObj.isDisconnectReq()){
			p.setRecordId(circuit.getStartPortId());
		}
		else{
			p.setRecordId(circuit.getCircuitId());
		}
		
		save(requestObj);
		
		return requestObj;
	}
	
	public RequestHistory createHistory(Request req, Long stageValueCode){
		RequestHistory h = new RequestHistory();
		req.addRequestHistory(h);
		
		//h.setRequestHistoryId(req.getRequestId() + req.getRequestHistories().size() + 1000);
		h.setCurrent(true);
		h.setRequestDetail(req);
		h.setStageIdLookup(systemLookupDAO.findByLkpValueCode(stageValueCode).get(0));
		
		return h;
	}
	
	@Override
	public RequestPointer createRequetPointer(Request req, String tableName){
		RequestPointer p = new RequestPointer();
		req.addRequestPointer(p);
		p.setRecordId(req.getItemId());
		p.setRequestDetail(req);
		//p.setRequestPointerId(req.getRequestId() + req.getRequestPointers().size() + 1000);
		p.setSortOrder(req.getRequestPointers().size());
		p.setTableName(tableName);
		
		return p;		
	}

	@Override
	public void addPortMoveToRequest(Request request, Item origItem, Item moveItem){
		try {
			dataPortMoveDAO.createPortMoveData(origItem, moveItem, null, null, null, request);
			powerPortMoveDAO.createPortMoveData(origItem, moveItem, null, null, null, request);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
	
	@Override
	public HashMap<Long,RequestInfo> getMapStatusToReqType(){
		if(mapStatusToReqType == null) initMapStatusToReqType();
		
		return mapStatusToReqType;
	}

	private void initMapStatusToReqType() {
		mapStatusToReqType = new HashMap<Long,RequestInfo>();
		RequestInfo ri = new RequestInfo();
		ri.reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.NEW_ITEM).get(0);
		ri.requestType = ItemRequest.ItemRequestType.installItem;
		mapStatusToReqType.put(SystemLookup.ItemStatus.INSTALLED, ri);
		
		ri = new RequestInfo();
		ri.reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_ARCHIVE).get(0);
		ri.requestType = ItemRequest.ItemRequestType.decomissionToArchive;
		mapStatusToReqType.put(SystemLookup.ItemStatus.ARCHIVED, ri);

		ri = new RequestInfo();
		ri.reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_REMOVE_TO_STORAGE).get(0);
		ri.requestType = ItemRequest.ItemRequestType.decomissionToStorage;
		mapStatusToReqType.put(SystemLookup.ItemStatus.IN_STORAGE, ri);

		ri = new RequestInfo();
		ri.reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_POWER_OFF).get(0);
		ri.requestType = ItemRequest.ItemRequestType.powerOff;
		mapStatusToReqType.put(SystemLookup.ItemStatus.POWERED_OFF, ri);

		ri = new RequestInfo();
		ri.reqTypeLks = systemLookupDAO.findByLkpValueCode(SystemLookup.RequestTypeLkp.ITEM_OFF_SITE).get(0);
		ri.requestType = ItemRequest.ItemRequestType.takeItemOffSite;
		mapStatusToReqType.put(SystemLookup.ItemStatus.OFF_SITE, ri);
	}	

}
