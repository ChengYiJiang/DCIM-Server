package com.raritan.tdz.unit.item.port;

import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.domain.PowerPort;
import com.raritan.tdz.port.dao.PowerPortDAO;
import com.raritan.tdz.port.dao.PowerPortFinderDAO;

public class PowerPortExpectationsImpl implements PowerPortExpectations {
	@Autowired
	PowerPortFinderDAO powerPortFinderDAO;

	@Autowired
	private PowerPortDAO powerPortDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createFindUsedPorts(Mockery jmockContext, final Long itemId, final List<PowerPort> recList){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortFinderDAO).findUsedPorts(with(itemId)); will(returnValue(recList));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createOneOfFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createOneOfFindUsedPorts(Mockery jmockContext,  final Long itemId, final List<PowerPort> recList){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(powerPortFinderDAO).findUsedPorts(with(itemId)); will(returnValue(recList));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerPort)
	 */
	@Override
	public void createRead(Mockery jmockContext,  final Long portId, final PowerPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).read(with(portId));will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createOneOfRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerPort)
	 */
	@Override
	public void createOneOfRead(Mockery jmockContext,  final Long portId, final PowerPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(powerPortDAO).read(with(portId));will(returnValue(retValue));
		  }});
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerPort)
	 */
	@Override
	public void createLoadEvictedPort(Mockery jmockContext,  final Long portId, final PowerPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).loadEvictedPort(with(portId)); will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createOneOfLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.PowerPort)
	 */
	@Override
	public void createOneOfLoadEvictedPort(Mockery jmockContext,  final Long portId, final PowerPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(powerPortDAO).loadEvictedPort(with(portId)); will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	@Override
	public void createGetFieldsValue(Mockery jmockContext, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).getFieldsValue( with(PowerPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	@Override
	public void createOneOfGetFieldsValue(Mockery jmockContext, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(powerPortDAO).getFieldsValue( with(PowerPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	@Override
	public void createGetFieldsValue(Mockery jmockContext, final Long portId, final List<String> fieldList, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).getFieldsValue( with(PowerPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	@Override
	public void createOneOfGetFieldsValue(Mockery jmockContext, final Long portId, final List<String> fieldList, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(powerPortDAO).getFieldsValue( with(PowerPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
		  }});
	}

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createUpdate(org.jmock.Mockery)
	 */
	@Override
	public void createUpdate(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).update(with(any(PowerPort.class)));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createMerge(org.jmock.Mockery)
	 */
	@Override
	public void createMerge(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).merge(with(any(PowerPort.class)));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createMergeOnly(org.jmock.Mockery)
	 */
	@Override
	public void createMergeOnly(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).mergeOnly(with(any(PowerPort.class)));
		  }});
	}		
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.PowerPortExpectations#createCreate(org.jmock.Mockery)
	 */
	@Override
	public void createCreate(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(powerPortDAO).create(with(any(PowerPort.class)));
		  }});		  
	}		
	
}
