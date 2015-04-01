package com.raritan.tdz.unit.item.port;

import java.util.List;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.beans.factory.annotation.Autowired;
import com.raritan.tdz.domain.DataPort;
import com.raritan.tdz.port.dao.DataPortDAO;
import com.raritan.tdz.port.dao.DataPortFinderDAO;

public class DataPortExpectationsImpl implements DataPortExpectations {
	@Autowired
	DataPortFinderDAO dataPortFinderDAO;

	@Autowired
	private DataPortDAO dataPortDAO;
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createFindUsedPorts(Mockery jmockContext, final Long itemId, final List<DataPort> recList){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortFinderDAO).findUsedPorts(with(itemId)); will(returnValue(recList));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createOneOfFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	@Override
	public void createOneOfFindUsedPorts(Mockery jmockContext,  final Long itemId, final List<DataPort> recList){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortFinderDAO).findUsedPorts(with(itemId)); will(returnValue(recList));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataPort)
	 */
	@Override
	public void createRead(Mockery jmockContext,  final Long portId, final DataPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).read(with(portId));will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createOneOfRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataPort)
	 */
	@Override
	public void createOneOfRead(Mockery jmockContext,  final Long portId, final DataPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).read(with(portId));will(returnValue(retValue));
		  }});
	}
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataPort)
	 */
	@Override
	public void createLoadEvictedPort(Mockery jmockContext,  final Long portId, final DataPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).loadEvictedPort(with(portId)); will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createOneOfLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.DataPort)
	 */
	@Override
	public void createOneOfLoadEvictedPort(Mockery jmockContext,  final Long portId, final DataPort retValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).loadEvictedPort(with(portId)); will(returnValue(retValue));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	@Override
	public void createGetFieldsValue(Mockery jmockContext, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).getFieldsValue( with(DataPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	@Override
	public void createOneOfGetFieldsValue(Mockery jmockContext, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).getFieldsValue( with(DataPort.class), with("portId"), with(any(Object.class)), with(any( List.class )));will(returnValue(fieldValue) );
		  }});
	}	
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	@Override
	public void createGetFieldsValue(Mockery jmockContext, final Long portId, final List<String> fieldList, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).getFieldsValue( with(DataPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	@Override
	public void createOneOfGetFieldsValue(Mockery jmockContext, final Long portId, final List<String> fieldList, final Map<String, Object> fieldValue){
		  jmockContext.checking(new Expectations() {{ 
			  oneOf(dataPortDAO).getFieldsValue( with(DataPort.class), with("portId"), with(portId), with(fieldList));will(returnValue(fieldValue) );
		  }});
	}

	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createUpdate(org.jmock.Mockery)
	 */
	@Override
	public void createUpdate(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).update(with(any(DataPort.class)));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createMerge(org.jmock.Mockery)
	 */
	@Override
	public void createMerge(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).merge(with(any(DataPort.class)));
		  }});
	}

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createMergeOnly(org.jmock.Mockery)
	 */
	@Override
	public void createMergeOnly(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).mergeOnly(with(any(DataPort.class)));
		  }});
	}		
	
	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.DataPortExpectations#createCreate(org.jmock.Mockery)
	 */
	@Override
	public void createCreate(Mockery jmockContext){
		  jmockContext.checking(new Expectations() {{ 
			  allowing(dataPortDAO).create(with(any(DataPort.class)));
		  }});		  
	}		
	
}
