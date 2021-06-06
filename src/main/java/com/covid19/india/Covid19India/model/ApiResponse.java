package com.covid19.india.Covid19India.model;

public class ApiResponse {

    @com.fasterxml.jackson.annotation.JsonProperty("status")
    private String status;
    @com.fasterxml.jackson.annotation.JsonProperty("code")
    private int code;
    @com.fasterxml.jackson.annotation.JsonProperty("data")
    private String data;
    @com.fasterxml.jackson.annotation.JsonProperty("message")
    private String message;

    public ApiResponse(String status, int code, String data, String message) {
        this.status = status;
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
