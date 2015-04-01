package com.raritan.tdz.component.inspector.dto;

import java.util.ArrayList;
import java.util.List;

import com.raritan.tdz.util.UnitConverterLookup;

/**
 * Data transfer object for cabinet metric.
 */
public class CabinetMetricDto {

    public static final String UNIT_POWER = "W";
    public static final String UNIT_HEAT_OUTPUT_SI = "kW";
    public static final String UNIT_HEAT_OUTPUT_US = "BTU/hr";
    public static final String UNIT_WEIGHT_SI = "kg";
    public static final String UNIT_WEIGHT_US = "lbs";

    private long cabinetId;
    private String cabinetName;
    private long unit;

    private double budgetedPower;
    private double heatOutput;
    private double totalWeight;
    private long itemsInCabinet;
    private int availableRUs;
    private int totalRUs;
    private int largestContiguousRUs;

    private int availablePortsCopper;
    private int totalPortsCopper;
    private int availablePortsFiber;
    private int totalPortsFiber;
    private int availablePortsCoax;
    private int totalPortsCoax;
    private int availablePortsPower;
    private int totalPortsPower;

    private List<CabinetMetricPowerFeedDto> powerFeeds = new ArrayList<CabinetMetricPowerFeedDto>();

    public long getCabinetId() {
        return cabinetId;
    }

    public void setCabinetId(long cabinetId) {
        this.cabinetId = cabinetId;
    }

    public String getCabinetName() {
        return cabinetName;
    }

    public void setCabinetName(String cabinetName) {
        this.cabinetName = cabinetName;
    }

    public long getUnit() {
        return unit;
    }

    public void setUnit(long unit) {
        this.unit = unit;
    }

    public String getUnitPower() {
        return UNIT_POWER;
    }

    public String getUnitHeatOutput() {
        if (unit == UnitConverterLookup.US_UNIT) {
            return UNIT_HEAT_OUTPUT_US;
        } else {
            return UNIT_HEAT_OUTPUT_SI;
        }
    }

    public String getUnitWeight() {
        if (unit == UnitConverterLookup.US_UNIT) {
            return UNIT_WEIGHT_US;
        } else {
            return UNIT_WEIGHT_SI;
        }
    }

    public double getBudgetedPower() {
        return budgetedPower;
    }

    public void setBudgetedPower(double budgetedPower) {
        this.budgetedPower = budgetedPower;
    }

    public double getHeatOutput() {
        return heatOutput;
    }

    public void setHeatOutput(double heatOutput) {
        this.heatOutput = heatOutput;
    }

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public long getItemsInCabinet() {
        return itemsInCabinet;
    }

    public void setItemsInCabinet(long itemsInCabinet) {
        this.itemsInCabinet = itemsInCabinet;
    }

    public int getAvailableRUs() {
        return availableRUs;
    }

    public void setAvailableRUs(int availableRUs) {
        this.availableRUs = availableRUs;
    }

    public int getTotalRUs() {
        return totalRUs;
    }

    public void setTotalRUs(int totalRUs) {
        this.totalRUs = totalRUs;
    }

    public int getLargestContiguousRUs() {
        return largestContiguousRUs;
    }

    public void setLargestContiguousRUs(int largestContiguousRUs) {
        this.largestContiguousRUs = largestContiguousRUs;
    }

    public int getAvailablePortsCopper() {
        return availablePortsCopper;
    }

    public void setAvailablePortsCopper(int availablePortsCopper) {
        this.availablePortsCopper = availablePortsCopper;
    }

    public int getTotalPortsCopper() {
        return totalPortsCopper;
    }

    public void setTotalPortsCopper(int totalPortsCopper) {
        this.totalPortsCopper = totalPortsCopper;
    }

    public int getAvailablePortsFiber() {
        return availablePortsFiber;
    }

    public void setAvailablePortsFiber(int availablePortsFiber) {
        this.availablePortsFiber = availablePortsFiber;
    }

    public int getTotalPortsFiber() {
        return totalPortsFiber;
    }

    public void setTotalPortsFiber(int totalPortsFiber) {
        this.totalPortsFiber = totalPortsFiber;
    }

    public int getAvailablePortsCoax() {
        return availablePortsCoax;
    }

    public void setAvailablePortsCoax(int availablePortsCoax) {
        this.availablePortsCoax = availablePortsCoax;
    }

    public int getTotalPortsCoax() {
        return totalPortsCoax;
    }

    public void setTotalPortsCoax(int totalPortsCoax) {
        this.totalPortsCoax = totalPortsCoax;
    }

    public int getAvailablePortsPower() {
        return availablePortsPower;
    }

    public void setAvailablePortsPower(int availablePortsPower) {
        this.availablePortsPower = availablePortsPower;
    }

    public int getTotalPortsPower() {
        return totalPortsPower;
    }

    public void setTotalPortsPower(int totalPortsPower) {
        this.totalPortsPower = totalPortsPower;
    }

    public List<CabinetMetricPowerFeedDto> getPowerFeeds() {
        return powerFeeds;
    }

    public void setPowerFeeds(List<CabinetMetricPowerFeedDto> powerFeeds) {
        this.powerFeeds = powerFeeds;
    }

    public void addPowerFeed(CabinetMetricPowerFeedDto powerFfeed) {
        powerFeeds.add(powerFfeed);
    }

    @Override
    public String toString() {
        return "CabinetMetricDto [cabinetId=" + cabinetId + ", cabinetName="
                + cabinetName + ", unit=" + unit + ", budgetedPower="
                + budgetedPower + ", heatOutput=" + heatOutput
                + ", totalWeight=" + totalWeight + ", itemsInCabinet="
                + itemsInCabinet + ", availableRUs=" + availableRUs
                + ", totalRUs=" + totalRUs + ", largestContiguousRUs="
                + largestContiguousRUs + ", availablePortsCopper="
                + availablePortsCopper + ", totalPortsCopper="
                + totalPortsCopper + ", availablePortsFiber="
                + availablePortsFiber + ", totalPortsFiber=" + totalPortsFiber
                + ", availablePortsCoax=" + availablePortsCoax
                + ", totalPortsCoax=" + totalPortsCoax
                + ", availablePortsPower=" + availablePortsPower
                + ", totalPortsPower=" + totalPortsPower + ", powerFeeds="
                + powerFeeds + "]";
    }
}