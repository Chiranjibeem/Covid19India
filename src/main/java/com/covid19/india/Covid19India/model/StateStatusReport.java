package com.covid19.india.Covid19India.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class StateStatusReport implements Serializable {

    private static final long serialVersionUID = 5388741908181692854L;

    @JsonProperty("State")
    private String name;
    @JsonProperty("Confirmed")
    private int confirmedCase;
    @JsonProperty("Deceased")
    private int deceasedCase;
    @JsonProperty("Recovered")
    private int recoveredCase;
    @JsonProperty("Active")
    private int activeCase;
    @JsonIgnore
    @JsonIgnoreProperties
    private String stateNotes;
    @JsonIgnore
    @JsonIgnoreProperties
    private String stateCode;
    @JsonProperty("districtData")
    private List<DistrictWiseStatusReport> districtData;

    public StateStatusReport(String name, int confirmedCase, int deceasedCase, int recoveredCase, int activeCase,String stateNotes,String stateCode){
        this.name = name;
        this.confirmedCase = confirmedCase;
        this.deceasedCase = deceasedCase;
        this.recoveredCase = recoveredCase;
        this.activeCase = activeCase;
        this.stateNotes = stateNotes;
        this.stateCode = stateCode;
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

    public List<DistrictWiseStatusReport> getDistrictData() {
        return districtData;
    }

    public void setDistrictData(List<DistrictWiseStatusReport> districtData) {
        this.districtData = districtData;
    }

    public String getStateNotes() {
        return stateNotes;
    }

    public String getStateCode() {
        return stateCode;
    }
}
