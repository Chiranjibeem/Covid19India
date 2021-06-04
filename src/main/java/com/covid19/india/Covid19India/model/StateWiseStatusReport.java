package com.covid19.india.Covid19India.model;

import java.io.Serializable;
import java.util.HashMap;

public class StateWiseStatusReport implements Serializable {

    private static final long serialVersionUID = -6381908060908088889L;

    private String State;
    private int Confirmed;
    private int Recovered;
    private int Deaths;
    private int Active;
    private String Last_Updated_Time;
    private int Migrated_Other;
    private String State_code;
    private int Delta_Confirmed;
    private int Delta_Recovered;
    private int Delta_Deaths;
    private String State_Notes;

    private static HashMap<String,String> columnMapping = new HashMap<>();

    static {
        columnMapping.put("State","State");
        columnMapping.put("Confirmed","Confirmed");
        columnMapping.put("Recovered","Recovered");
        columnMapping.put("Deaths","Deaths");
        columnMapping.put("Active","Active");
        columnMapping.put("Last_Updated_Time","Last_Updated_Time");
        columnMapping.put("Migrated_Other","Migrated_Other");
        columnMapping.put("State_code","State_code");
        columnMapping.put("Delta_Confirmed","Delta_Confirmed");
        columnMapping.put("Delta_Recovered","Delta_Recovered");
        columnMapping.put("Delta_Deaths","Delta_Deaths");
        columnMapping.put("State_Notes","State_Notes");
    }

    public static HashMap<String,String> getColumnMapping(){
        return columnMapping;
    }

    public String getState() {
        return State;
    }

    public int getConfirmed() {
        return Confirmed;
    }

    public int getRecovered() {
        return Recovered;
    }

    public int getDeaths() {
        return Deaths;
    }

    public int getActive() {
        return Active;
    }

    public String getLast_Updated_Time() {
        return Last_Updated_Time;
    }

    public int getMigrated_Other() {
        return Migrated_Other;
    }

    public String getState_code() {
        return State_code;
    }

    public int getDelta_Confirmed() {
        return Delta_Confirmed;
    }

    public int getDelta_Recovered() {
        return Delta_Recovered;
    }

    public int getDelta_Deaths() {
        return Delta_Deaths;
    }

    public String getState_Notes() {
        return State_Notes;
    }
}
