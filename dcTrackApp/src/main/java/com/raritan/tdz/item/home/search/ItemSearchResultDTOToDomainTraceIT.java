/**
 * 
 */
package com.raritan.tdz.item.home.search;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.raritan.tdz.domain.CabinetItem;
import com.raritan.tdz.domain.Item;
import com.raritan.tdz.domain.MeItem;
import com.raritan.tdz.item.dto.ItemSearchResultDTOImpl;
import com.raritan.tdz.util.DtoToDomainObjectTrace;
import com.raritan.tdz.util.LksDataValueCodeTracerHandler;
import com.raritan.tdz.util.LksDataValueTracerHandler;
import com.raritan.tdz.util.LkuDataValueCodeTracerHandler;
import com.raritan.tdz.util.LkuDataValueTracerHandler;
import com.raritan.tdz.util.ObjectTracer;
import com.raritan.tdz.util.UserIdTracerHandler;

/**
 * @author prasanna
 *
 */
public class ItemSearchResultDTOToDomainTraceIT extends ItemSearchResultDTOToDomainTrace {

	public ItemSearchResultDTOToDomainTraceIT(String rootClassStr) {
		super(rootClassStr);
	}

	@Override
	protected void createTrace(Class<?> rootClass) {
		super.createTrace(rootClass);
		
		trace.put("cabinetName", "parentItem.itemName");
		trace.put("chassisName", "bladeChassis.itemName");
		trace.put("chassisUPosition", "bladeChassis.uPosition");
	}



}
