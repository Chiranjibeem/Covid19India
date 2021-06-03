package com.covid19.india.Covid19India.model;

import java.io.Serializable;
import java.util.List;

public class StateStatusReport implements Serializable {

    private static final long serialVersionUID = 5388741908181692854L;

    private String name;
    private int confirmedCase;
    private int deceasedCase;
    private int recoveredCase;
    private int activeCase;
    private List<DistrictData> districtData;
    private String districtDataWithCase;

    public StateStatusReport(String name, int confirmedCase, int deceasedCase, int recoveredCase, int activeCase){
        this.name = name;
        this.confirmedCase = confirmedCase;
        this.deceasedCase = deceasedCase;
        this.recoveredCase =recoveredCase;
        this.activeCase = activeCase;
    }

    public String getName() {
        return name;
    }

    public int getConfirmedCase() {
        return confirmedCase;
    }

    public int getDeceasedCase() {
        return deceasedCase;
    }

    public int getRecoveredCase() {
        return recoveredCase;
    }

    public int getActiveCase() {
        return activeCase;
    }

    public List<DistrictData> getDistrictData() {
        return districtData;
    }

    public String getDistrictDataWithCase() {
        return districtDataWithCase;
    }

    public void setDistrictData(List<DistrictData> districtData) {
        this.districtData = districtData;
    }

    public void setDistrictDataWithCase(String districtDataWithCase) {
        this.districtDataWithCase = districtDataWithCase;
    }
}
