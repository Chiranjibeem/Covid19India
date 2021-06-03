package com.covid19.india.Covid19India.model;

import java.util.List;

public class CoronaStatus {


    public List<DistrictData> districtData;

    public String state;

    public String statecode;

    public List<DistrictData> getDistrictData() {
        return districtData;
    }

    public void setDistrictData(List<DistrictData> districtData) {
        this.districtData = districtData;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatecode() {
        return statecode;
    }

    public void setStatecode(String statecode) {
        this.statecode = statecode;
    }

    @Override
    public String toString() {
        return "CoronaStatus [districtData = " + districtData + ", state = " + state + "]";
    }
}
