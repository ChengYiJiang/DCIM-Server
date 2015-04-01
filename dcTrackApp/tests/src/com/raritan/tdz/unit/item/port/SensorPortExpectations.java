package com.raritan.tdz.unit.item.port;

import java.util.List;
import java.util.Map;

import org.jmock.Mockery;

import com.raritan.tdz.domain.SensorPort;

public interface SensorPortExpectations {

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public abstract void createFindUsedPorts(Mockery jmockContext, Long itemId,
			List<SensorPort> recList);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfFindUsedPorts(org.jmock.Mockery, java.lang.Long, java.util.List)
	 */
	public abstract void createOneOfFindUsedPorts(Mockery jmockContext,
			Long itemId, List<SensorPort> recList);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	public abstract void createRead(Mockery jmockContext, Long portId,
			SensorPort retValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfRead(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	public abstract void createOneOfRead(Mockery jmockContext, Long portId,
			SensorPort retValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	public abstract void createLoadEvictedPort(Mockery jmockContext,
			Long portId, SensorPort retValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfLoadEvictedPort(org.jmock.Mockery, java.lang.Long, com.raritan.tdz.domain.SensorPort)
	 */
	public abstract void createOneOfLoadEvictedPort(Mockery jmockContext,
			Long portId, SensorPort retValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	public abstract void createGetFieldsValue(Mockery jmockContext,
			Map<String, Object> fieldValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.util.Map)
	 */
	public abstract void createOneOfGetFieldsValue(Mockery jmockContext,
			Map<String, Object> fieldValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	public abstract void createGetFieldsValue(Mockery jmockContext,
			Long portId, List<String> fieldList, Map<String, Object> fieldValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createOneOfGetFieldsValue(org.jmock.Mockery, java.lang.Long, java.util.List, java.util.Map)
	 */
	public abstract void createOneOfGetFieldsValue(Mockery jmockContext,
			Long portId, List<String> fieldList, Map<String, Object> fieldValue);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createUpdate(org.jmock.Mockery)
	 */
	public abstract void createUpdate(Mockery jmockContext);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createMerge(org.jmock.Mockery)
	 */
	public abstract void createMerge(Mockery jmockContext);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createMergeOnly(org.jmock.Mockery)
	 */
	public abstract void createMergeOnly(Mockery jmockContext);

	/* (non-Javadoc)
	 * @see com.raritan.tdz.unit.item.port.SensorPortExpectations#createCreate(org.jmock.Mockery)
	 */
	public abstract void createCreate(Mockery jmockContext);

}