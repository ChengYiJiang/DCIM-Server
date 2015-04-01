package com.raritan.tdz.component.inspector.dao;

import java.util.HashMap;
import java.util.List;

import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.domain.HstCabinetUsage;
import com.raritan.tdz.domain.HstPortsData;
import com.raritan.tdz.domain.HstPortsPower;

/**
 * Data access object for cabinet metrics.
 */
public interface CabinetMetricDao {

	public double getCabinetBudgetedPower(Long cabinetId);

	public double getCabinetHeatOutput(Long cabinetId);

	public double getCabinetTotalWeight(Long cabinetId);

	public long getCabinetItemCount(Long cabinetId);

	public int getCabinetTotalAvailableRUs(Long cabinetId);

	public int getCabinetTotalRUs(Long cabinetId);

	public int getCabinetTotalUsedRUs(Long cabinetId);

	public int getCabinetLargestContigRU(Long cabinetId);

    public CabinetMetricDto getCabinetRUInfo(Long cabinetId);

	public HstCabinetUsage getHstCabinetUsage(Long cabinetId);

	public List<HstPortsData> getHstPortData(Long cabinetId);

	public List<HstPortsPower> getHstPortPower(Long cabinetId);

	//public List<?> getCabinetListBudgetedPower(Long locationId);

	public List<?> getCabinetListBudgetedPower(String locationCode);

	public HashMap<Long, Float> getItemsEffectivePower(List<Long> itemIds);
}