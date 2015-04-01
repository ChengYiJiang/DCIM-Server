
package com.raritan.tdz.unit.item.port;

import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.domain.SensorPort;
import com.raritan.tdz.port.dao.SensorPortDAO;
import com.raritan.tdz.port.dao.SensorPortFinderDAO;

public class SensorPortExpectationsImpl implements SensorPortExpectations {
	@Autowired
	SensorPortFinderDAO sensorPortFinderDAO;

	@Autowired
	private SensorPortDAO sensorPortDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createFindUsedPorts(Mockery jmockContext, final Long itemId, final List<SensorPort> recList){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortFinderDAO).findUsedPorts(with(itemId)); will(returnValue(recList));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createOneOfFindUsedPorts(Mockery jmockContext,  final Long itemId, final List<SensorPort> recList){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(sensorPortFinderDAO).findUsedPorts(with(itemId)); will(returnValue(recList));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	@Override
	public void createRead(Mockery jmockContext,  final Long portId, final SensorPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).read(with(portId));will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	@Override
	public void createOneOfRead(Mockery jmockContext,  final Long portId, final SensorPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(sensorPortDAO).read(with(portId));will(returnValue(retValue));
		  }});
	}
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	@Override
	public void createLoadEvictedPort(Mockery jmockContext,  final Long portId, final SensorPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).loadEvictedPort(with(portId)); will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	@Override
	public void createOneOfLoadEvictedPort(Mockery jmockContext,  final Long portId, final SensorPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(sensorPortDAO).loadEvictedPort(with(portId)); will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	@Override
	public void createGetFieldsValue(Mockery jmockContext, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).getFieldsValue( with(SensorPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	@Override
	public void createOneOfGetFieldsValue(Mockery jmockContext, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(sensorPortDAO).getFieldsValue( with(SensorPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	@Override
	public void createGetFieldsValue(Mockery jmockContext, final Long portId, final List<String> fieldList, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).getFieldsValue( with(SensorPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	@Override
	public void createOneOfGetFieldsValue(Mockery jmockContext, final Long portId, final List<String> fieldList, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(sensorPortDAO).getFieldsValue( with(SensorPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
		  }});
	}

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createUpdate(org.jmock.Mockery)
	 */
	@Override
	public void createUpdate(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).update(with(any(SensorPort.class)));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createMerge(org.jmock.Mockery)
	 */
	@Override
	public void createMerge(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).merge(with(any(SensorPort.class)));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createMergeOnly(org.jmock.Mockery)
	 */
	@Override
	public void createMergeOnly(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).mergeOnly(with(any(SensorPort.class)));
		  }});
	}		
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createCreate(org.jmock.Mockery)
	 */
	@Override
	public void createCreate(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(sensorPortDAO).create(with(any(SensorPort.class)));
		  }});		  
	}		
	
}
