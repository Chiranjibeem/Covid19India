package com.covid19.india.Covid19India.model;

import java.io.Serializable;
import java.util.HashMap;

public class DailyCountryStatusReport implements Serializable{

    private static final long serialVersionUID = 2991519364703765459L;
    private String Date;
    private String Date_YMD;
    private String Status;
    private int TT;

    private static HashMap<String,String> columnMapping = new HashMap<>();

    static {
        columnMapping.put("Date","Date");
        columnMapping.put("Date_YMD","Date_YMD");
        columnMapping.put("Status","Status");
        columnMapping.put("TT","TT");
    }

    public String getDate() {
        return Date;
    }

    public String getDate_YMD() {
        return Date_YMD;
    }

    public String getStatus() {
        return Status;
    }

    public int getTT() {
        return TT;
    }

    public static HashMap<String, String> getColumnMapping() {
        return columnMapping;
    }
}
