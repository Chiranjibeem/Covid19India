package com.covid19.india.Covid19India.model;

import java.util.List;

public class StateAllStatus {

    private String name;
    private int confirmedCase;
    private int deceasedCase;
    private int recoveredCase;
    private List<DistrictData> districtData;
    private String districtDataWithCase;

    public StateAllStatus(String name,int confirmedCase,int deceasedCase,int recoveredCase){
        this.name = name;
        this.confirmedCase = confirmedCase;
        this.deceasedCase = deceasedCase;
        this.recoveredCase =recoveredCase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getConfirmedCase() {
        return confirmedCase;
    }

    public void setConfirmedCase(int confirmedCase) {
        this.confirmedCase = confirmedCase;
    }

    public int getDeceasedCase() {
        return deceasedCase;
    }

    public void setDeceasedCase(int deceasedCase) {
        this.deceasedCase = deceasedCase;
    }

    public int getRecoveredCase() {
        return recoveredCase;
    }

    public void setRecoveredCase(int recoveredCase) {
        this.recoveredCase = recoveredCase;
    }

    public List<DistrictData> getDistrictData() {
        return districtData;
    }

    public void setDistrictData(List<DistrictData> districtData) {
        this.districtData = districtData;
    }

    public String getDistrictDataWithCase() {
        return districtDataWithCase;
    }

    public void setDistrictDataWithCase(String districtDataWithCase) {
        this.districtDataWithCase = districtDataWithCase;
    }
}
