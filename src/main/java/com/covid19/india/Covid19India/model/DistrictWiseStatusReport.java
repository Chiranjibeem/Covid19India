package com.covid19.india.Covid19India.model;

import java.io.Serializable;
import java.util.HashMap;

public class DistrictWiseStatusReport implements Serializable {

    private static final long serialVersionUID = 3914984846626572303L;
    private String State_Code;
    private String State;
    private String District;
    private int Confirmed;
    private int Active;
    private int Recovered;
    private int Deceased;
    private int Migrated_Other;
    private int Delta_Confirmed;
    private int Delta_Active;
    private int Delta_Recovered;
    private int Delta_Deceased;
    private String District_Notes;
    private String Last_Updated;

    private static HashMap<String,String> columnMapping = new HashMap<>();

    static {
        columnMapping.put("State_Code","State_Code");
        columnMapping.put("State","State");
        columnMapping.put("District","District");
        columnMapping.put("Confirmed","Confirmed");
        columnMapping.put("Active","Active");
        columnMapping.put("Recovered","Recovered");
        columnMapping.put("Deceased","Deceased");
        columnMapping.put("Migrated_Other","Migrated_Other");
        columnMapping.put("Delta_Confirmed","Delta_Confirmed");
        columnMapping.put("Delta_Active","Delta_Active");
        columnMapping.put("Delta_Recovered","Delta_Recovered");
        columnMapping.put("Delta_Deceased","Delta_Deceased");
        columnMapping.put("District_Notes","District_Notes");
        columnMapping.put("Last_Updated","Last_Updated");
    }

    public String getState_Code() {
        return State_Code;
    }

    public String getState() {
        return State;
    }

    public String getDistrict() {
        return District;
    }

    public int getConfirmed() {
        return Confirmed;
    }

    public int getActive() {
        return Active;
    }

    public int getRecovered() {
        return Recovered;
    }

    public int getDeceased() {
        return Deceased;
    }

    public int getMigrated_Other() {
        return Migrated_Other;
    }

    public int getDelta_Confirmed() {
        return Delta_Confirmed;
    }

    public int getDelta_Active() {
        return Delta_Active;
    }

    public int getDelta_Recovered() {
        return Delta_Recovered;
    }

    public int getDelta_Deceased() {
        return Delta_Deceased;
    }

    public String getDistrict_Notes() {
        return District_Notes;
    }

    public String getLast_Updated() {
        return Last_Updated;
    }

    public static HashMap<String, String> getColumnMapping() {
        return columnMapping;
    }
}


			