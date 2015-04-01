package com.raritan.tdz.unit.item.port;

import java.util.List;
import java.util.Map;

import org.jmock.Mockery;

import com.raritan.tdz.domain.PowerPort;

public interface PowerPortExpectations {

	public abstract void createFindUsedPorts(Mockery jmockContext, Long itemId,
			List<PowerPort> recList);

	public abstract void createOneOfFindUsedPorts(Mockery jmockContext,
			Long itemId, List<PowerPort> recList);

	public abstract void createRead(Mockery jmockContext, Long portId,
			PowerPort retValue);

	public abstract void createOneOfRead(Mockery jmockContext, Long portId,
			PowerPort retValue);

	public abstract void createLoadEvictedPort(Mockery jmockContext,
			Long portId, PowerPort retValue);

	public abstract void createOneOfLoadEvictedPort(Mockery jmockContext,
			Long portId, PowerPort retValue);

	public abstract void createGetFieldsValue(Mockery jmockContext,
			Map<String, Object> fieldValue);

	public abstract void createOneOfGetFieldsValue(Mockery jmockContext,
			Map<String, Object> fieldValue);

	public abstract void createGetFieldsValue(Mockery jmockContext,
			Long portId, List<String> fieldList, Map<String, Object> fieldValue);

	public abstract void createOneOfGetFieldsValue(Mockery jmockContext,
			Long portId, List<String> fieldList, Map<String, Object> fieldValue);

	public abstract void createUpdate(Mockery jmockContext);

	public abstract void createMerge(Mockery jmockContext);

	public abstract void createMergeOnly(Mockery jmockContext);

	public abstract void createCreate(Mockery jmockContext);

}