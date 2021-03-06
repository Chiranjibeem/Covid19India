package com.covid19.india.Covid19India.model;

import java.io.Serializable;

public class StateStatusReport implements Serializable {

    private static final long serialVersionUID = 5388741908181692854L;

    private String name;
    private int confirmedCase;
    private int deceasedCase;
    private int recoveredCase;
    private int activeCase;
    private String stateNotes;
    private String stateCode;

    public StateStatusReport(String name, int confirmedCase, int deceasedCase, int recoveredCase, int activeCase, String stateNotes, String stateCode) {
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

    public String getStateNotes() {
        return stateNotes;
    }

    public String getStateCode() {
        return stateCode;
    }
}
