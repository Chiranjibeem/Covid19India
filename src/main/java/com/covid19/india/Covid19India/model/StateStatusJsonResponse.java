package com.covid19.india.Covid19India.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class StateStatusJsonResponse implements Serializable {

    private static final long serialVersionUID = 4618167458168649173L;

    @JsonProperty("data")
    private List<StateStatusReportJson> data;

    public List<StateStatusReportJson> getData() {
        return data;
    }

    public void setData(List<StateStatusReportJson> data) {
        this.data = data;
    }
}
