package com.covid19.india.Covid19India.model;

import java.io.Serializable;
import java.util.HashMap;

public class VaccineStatusReport implements Serializable {

    private static final long serialVersionUID = 8570787010058511499L;

    private String Updated_On;
    private String State;
    private int Total_Covaxin_Administered;
    private int Total_CoviShield_Administered;
    private static HashMap<String,String> columnMapping = new HashMap<>();

    static {
        columnMapping.put("Updated On","Updated_On");
        columnMapping.put("State","State");
        columnMapping.put("Total Covaxin Administered","Total_Covaxin_Administered");
        columnMapping.put("Total CoviShield Administered","Total_CoviShield_Administered");
    }

    public String getUpdated_On() {
        return Updated_On;
    }

    public int getTotal_Covaxin_Administered() {
        return Total_Covaxin_Administered;
    }

    public int getTotal_CoviShield_Administered() {
        return Total_CoviShield_Administered;
    }

    public static HashMap<String,String> getColumnMapping(){
        return columnMapping;
    }

    public String getState() {
        return State;
    }

    public void setUpdated_On(String updated_On) {
        Updated_On = updated_On;
    }

    public void setState(String state) {
        State = state;
    }

    public void setTotal_Covaxin_Administered(int total_Covaxin_Administered) {
        Total_Covaxin_Administered = total_Covaxin_Administered;
    }

    public void setTotal_CoviShield_Administered(int total_CoviShield_Administered) {
        Total_CoviShield_Administered = total_CoviShield_Administered;
    }

    public static void setColumnMapping(HashMap<String, String> columnMapping) {
        VaccineStatusReport.columnMapping = columnMapping;
    }
}
