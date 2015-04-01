package com.raritan.tdz.component.inspector.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object for power feed.
 */
public class CabinetMetricPowerFeedDto {

    public static final String TYPE_RACK_PDU = "rackPDU";
    public static final String TYPE_DIRECT_CONNECTION = "direct";

    private String name = "";
    private String type = "";

    private String inlet = "";
    private String connectTo = "";
    private List<Integer> breakerPoles = new ArrayList<Integer>();

    private List<CabinetMetricPowerFeedAmpereDto> amperes = new ArrayList<CabinetMetricPowerFeedAmpereDto>();
    private String source = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInlet() {
        return inlet;
    }

    public void setInlet(String inlet) {
        this.inlet = inlet;
    }

    public String getConnectTo() {
        return connectTo;
    }

    public void setConnectTo(String connectTo) {
        this.connectTo = connectTo;
    }

    public List<Integer> getBreakerPoles() {
        return breakerPoles;
    }

    public void setBreakerPoles(List<Integer> breakerPoles) {
        this.breakerPoles = breakerPoles;
    }

    public List<CabinetMetricPowerFeedAmpereDto> getAmperes() {
        return amperes;
    }

    public void setAmperes(List<CabinetMetricPowerFeedAmpereDto> amperes) {
        this.amperes = amperes;
    }

    public void addAmpere(CabinetMetricPowerFeedAmpereDto ampere) {
        amperes.add(ampere);
    }

    public void addAmpere(int rated, int budgeted, int measured, long poleLeg) {
        CabinetMetricPowerFeedAmpereDto ampere = new CabinetMetricPowerFeedAmpereDto();

        ampere.setRated(rated);
        ampere.setBudgeted(budgeted);
        ampere.setMeasured(measured);
        ampere.setPoleLeg(poleLeg);

        addAmpere(ampere);
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void addBreakerPole(int breakerPole) {
        breakerPoles.add(breakerPole);
    }

    @Override
    public String toString() {
        return "CabinetMetricPowerFeedDto [name=" + name + ", type=" + type
                + ", inlet=" + inlet + ", connectTo=" + connectTo
                + ", breakerPoles=" + breakerPoles + ", amperes=" + amperes
                + ", source=" + source + "]";
    }
}