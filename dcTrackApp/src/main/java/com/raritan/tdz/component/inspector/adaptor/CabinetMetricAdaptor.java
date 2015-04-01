package com.raritan.tdz.component.inspector.adaptor;

import com.raritan.tdz.component.inspector.dto.CabinetMetricDto;
import com.raritan.tdz.component.inspector.dto.CabinetMetricPowerFeedAmpereDto;
import com.raritan.tdz.component.inspector.dto.CabinetMetricPowerFeedDto;

/**
 * Adaptor for cabinet metrics.
 */
public class CabinetMetricAdaptor {

    /**
     * Convert CabinetMetricDto to JSON String.
     */
    public static String convert(CabinetMetricDto dto) {

        StringBuilder json = new StringBuilder();

        json.append("{");

            json.append("\"cabinet\":{");

                json.append("\"id\":").append(dto.getCabinetId()).append(",");
                json.append("\"name\":\"").append(dto.getCabinetName()).append("\",");
                json.append("\"numberOfItems\":").append(dto.getItemsInCabinet()).append(",");

                json.append("\"power\":{");
                    json.append("\"budgeted\":").append(dto.getBudgetedPower()).append(",");
                    json.append("\"unit\":\"").append(dto.getUnitPower()).append("\"");
                json.append("},");

                json.append("\"heatOutput\":{");
                    json.append("\"budgeted\":").append(((Double) dto.getHeatOutput()).intValue()).append(",");
                    json.append("\"unit\":\"").append(dto.getUnitHeatOutput()).append("\"");
                json.append("},");

                json.append("\"weight\":{");
                    json.append("\"total\":").append(((Double) dto.getTotalWeight()).intValue()).append(",");
                    json.append("\"unit\":\"").append(dto.getUnitWeight()).append("\"");
                json.append("},");

                json.append("\"RUs\":{");
                    json.append("\"total\":").append(dto.getTotalRUs()).append(",");
                    json.append("\"available\":").append(dto.getAvailableRUs()).append(",");
                    json.append("\"largestContiguous\":").append(dto.getLargestContiguousRUs());
                json.append("},");

                json.append("\"ports\":{");
                    json.append("\"copper\":{");
                        json.append("\"available\":").append(dto.getAvailablePortsCopper()).append(",");
                        json.append("\"total\":").append(dto.getTotalPortsCopper());
                    json.append("},");
                    json.append("\"fiber\":{");
                        json.append("\"available\":").append(dto.getAvailablePortsFiber()).append(",");
                        json.append("\"total\":").append(dto.getTotalPortsFiber());
                    json.append("},");
                    json.append("\"coax\":{");
                        json.append("\"available\":").append(dto.getAvailablePortsCoax()).append(",");
                        json.append("\"total\":").append(dto.getTotalPortsCoax());
                    json.append("},");
                    json.append("\"power\":{");
                        json.append("\"available\":").append(dto.getAvailablePortsPower()).append(",");
                        json.append("\"total\":").append(dto.getTotalPortsPower());
                    json.append("}");
                json.append("}");

            json.append("},");

            json.append("\"powerPorts\":[");

            CabinetMetricPowerFeedDto feed;
            CabinetMetricPowerFeedAmpereDto ampere;
            for (int i = 0; i < dto.getPowerFeeds().size(); i++) {
                feed = dto.getPowerFeeds().get(i);

                json.append("{");

                    json.append("\"name\":\"").append(feed.getName()).append("\",");
                    json.append("\"type\":\"").append(feed.getType()).append("\",");
                    json.append("\"inlet\":\"").append(feed.getInlet()).append("\",");
                    json.append("\"connectTo\":\"").append(feed.getConnectTo()).append("\",");

                    //if (feed.getBreakerPoles().size() > 0) {
                        json.append("\"breakerPoles\":[");
                        for(int j = 0; j < feed.getBreakerPoles().size(); j++) {
                            json.append(feed.getBreakerPoles().get(j));
                            if (j < feed.getBreakerPoles().size() - 1) {
                                json.append(",");
                            }
                        }
                        json.append("],");
                    //}

                    if (feed.getAmperes().size() > 0) {
                        json.append("\"amperes\":[");
                        for(int j = 0; j < feed.getAmperes().size(); j++) {
                            ampere = feed.getAmperes().get(j);

                            json.append("{");
                                json.append("\"rated\":").append(ampere.getRated()).append(",");
                                json.append("\"budgeted\":").append(ampere.getBudgeted()).append(",");
                                json.append("\"measured\":").append(ampere.getMeasured());
                            json.append("}");

                            if (j < feed.getAmperes().size() - 1) {
                                json.append(",");
                            }
                        }
                        json.append("],");
                    }

                    json.append("\"source\":\"").append(feed.getSource()).append("\"");

                json.append("}");

                if (i < dto.getPowerFeeds().size() - 1) {
                    json.append(",");
                }
            }

            json.append("]");

        json.append("}");

        return json.toString();
    }
}