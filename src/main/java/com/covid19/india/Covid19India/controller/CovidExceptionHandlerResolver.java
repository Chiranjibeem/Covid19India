package com.covid19.india.Covid19India.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.covid19.india.Covid19India.configure.LocationTrackerConfig;
import com.covid19.india.Covid19India.model.LocationConfigurationException;
import com.covid19.india.Covid19India.model.TrackUserRequest;
import com.covid19.india.Covid19India.repository.TrackerUserRequestRepository;
import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CovidExceptionHandlerResolver {

    @Autowired
    TrackerUserRequestRepository trackUserRequestRepository;
    
    @Autowired
    LocationTrackerConfig locationTrackerConfig;


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request) {
        try {
        	 String locationTrackerResponse = "";
             try {
             	locationTrackerResponse = locationTrackerConfig.getLocation();
             }
             catch(LocationConfigurationException loc) {
             	locationTrackerResponse = loc.getMessage();
             }
             TrackUserRequest trackUser = new TrackUserRequest();
             trackUser.setAccessURL("/countryDashboard");
             trackUser.setClientInformation(locationTrackerResponse);
             trackUser.setErrorMsg(ex.getMessage());
             trackUserRequestRepository.saveAndFlush(trackUser);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return new ModelAndView("error");
    }
}

