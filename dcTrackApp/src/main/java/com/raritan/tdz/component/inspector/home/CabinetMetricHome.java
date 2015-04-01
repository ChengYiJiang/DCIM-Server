package com.raritan.tdz.component.inspector.home;

import java.util.List;

import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.component.inspector.home.impl.PowerPortMetric;
import com.raritan.tdz.domain.Item;

/**
 * Business wrapper for cabinet metrics.
 */
public interface CabinetMetricHome {

    /**
     * Get cabinet metrics.
     * 
     * @param cabinetId
     * @param unit
     * @return CabinetMetricDto
     * @throws Throwable
     */
    public CabinetMetricDto getCabinetMetrics(long cabinetId, String unit_str) throws Throwable;

	public double getCabinetBudgetedPower(Long cabinetId);

	public double getCabinetHeatOutput(Long cabinetId);

	public double getCabinetTotalWeight(Long cabinetId);

	public long getCabinetItemCount(Long cabinetId);

	public int getCabinetTotalAvailableRUs(Long cabinetId);

	public int getCabinetTotalRUs(Long cabinetId);

	public int getCabinetTotalUsedRUs(Long cabinetId);

	public int getCabinetLargestContigRU(Long cabinetId);

	List<PowerPortMetric> processRPDUMetric(Item cabinet,
			CabinetMetricDto cabDto) throws Throwable;

	List<PowerPortMetric> processOutletMetric(Item cabinet,
			CabinetMetricDto cabDto) throws Throwable;
}
