package com.covid19.india.Covid19India.controller;

import com.covid19.india.Covid19India.model.*;
import com.covid19.india.Covid19India.repository.CovidAccessStatusReposiory;
import com.covid19.india.Covid19India.repository.CovidErrorStatusReposiory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class Covid19Controller {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CovidAccessStatusReposiory accessStatusReposiory;

    @Autowired
    private CovidErrorStatusReposiory errorStatusReposiory;

    private String covidControllerStatus = "";
    @Value("${stateWiseReportEndpoint}")
    private String stateWiseReportEndpoint;       //https://api.covid19india.org/csv/latest/state_wise.csv
    @Value("${dailyStateWiseReportEndpoint}")
    private String dailyStateWiseReportEndpoint;  //https://api.covid19india.org/csv/latest/state_wise_daily.csv
    @Value("${dailyVaccineReportEndpoint}")
    private String dailyVaccineReportEndpoint;  //http://api.covid19india.org/csv/latest/cowin_vaccine_data_statewise.csv
    @Value("${districtWiseReportEndpoint}")
    private String districtWiseReportEndpoint;      //https://api.covid19india.org/csv/latest/district_wise.csv

    @GetMapping("/")
    public ModelAndView getCountryDashboard(Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<StateWiseStatusReport> summerizedStatusReports = getReport(stateWiseReportEndpoint);
            List<DailyStateWiseStatusReport> dailyStateWiseStatusReports = getReport(dailyStateWiseReportEndpoint);
            List<VaccineStatusReport> dailyVaccineStatusReports = getReport(dailyVaccineReportEndpoint);
            List<DistrictWiseStatusReport> districtWiseStatusReports = getReport(districtWiseReportEndpoint);

            List<StateStatusReport> stateStatusReports = getStateStatusReports(summerizedStatusReports,districtWiseStatusReports);

            StateWiseStatusReport countryStatusReport = new StateWiseStatusReport();
            if(CollectionUtils.isNotEmpty(summerizedStatusReports)) {
                countryStatusReport = summerizedStatusReports.stream().filter(countryStatus -> countryStatus.getState() != null && "TT".equalsIgnoreCase(countryStatus.getState_code())).collect(Collectors.toList()).get(0);
            }
            StateStatusReport countryStatus = new StateStatusReport("India", countryStatusReport.getConfirmed(),
                    countryStatusReport.getDeaths(), countryStatusReport.getRecovered(), countryStatusReport.getActive(),"", countryStatusReport.getState_code());

            model.addAttribute("stateAllStatuses", stateStatusReports);
            model.addAttribute("stateAllStatus", countryStatus);

            if(CollectionUtils.isNotEmpty(dailyStateWiseStatusReports)) {
                dailyStateWiseStatusReports = dailyStateWiseStatusReports.stream().sorted(Comparator.comparing(DailyStateWiseStatusReport::getDate_YMD).reversed()).collect(Collectors.toList()).subList(0,30);
                List<DailyStateWiseStatusReport> dailyConfirmedCountryStatusReprts = dailyStateWiseStatusReports.stream().filter(i->"Confirmed".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
                List<DailyStateWiseStatusReport> dailyRecoveredCountryStatusReprts = dailyStateWiseStatusReports.stream().filter(i->"Recovered".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
                List<DailyStateWiseStatusReport> dailyDeceasedCountryStatusReprts = dailyStateWiseStatusReports.stream().filter(i->"Deceased".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());

                model.addAttribute("confirmedCaseData", dailyConfirmedCountryStatusReprts.stream().map(DailyStateWiseStatusReport::getTT).toArray());
                model.addAttribute("recoveredCaseData", dailyRecoveredCountryStatusReprts.stream().map(DailyStateWiseStatusReport::getTT).toArray());
                model.addAttribute("deceasedCaseData",  dailyDeceasedCountryStatusReprts.stream().map(DailyStateWiseStatusReport::getTT).toArray());
                model.addAttribute("confirmedCaseHeader", dailyConfirmedCountryStatusReprts.stream().map(i -> i.getDate().substring(0, 7).replaceAll("-"," ")).toArray());
                model.addAttribute("recoveredCaseHeader", dailyRecoveredCountryStatusReprts.stream().map(i -> i.getDate().substring(0, 7).replaceAll("-"," ")).toArray());
                model.addAttribute("deceasedCaseHeader", dailyDeceasedCountryStatusReprts.stream().map(i -> i.getDate().substring(0, 7).replaceAll("-"," ")).toArray());
            }

            if(CollectionUtils.isNotEmpty(dailyVaccineStatusReports)){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dailyVaccineStatusReports = dailyVaccineStatusReports.stream().filter(i->"India".equalsIgnoreCase(i.getState())).
                        map(i-> {
                            VaccineStatusReport vaccineStatusReport = new VaccineStatusReport();
                            vaccineStatusReport.setState(i.getState());
                            vaccineStatusReport.setUpdated_On(LocalDate.parse(i.getUpdated_On(),formatter).toString());
                            vaccineStatusReport.setTotal_Covaxin_Administered(i.getTotal_Covaxin_Administered());
                            vaccineStatusReport.setTotal_CoviShield_Administered(i.getTotal_CoviShield_Administered());
                            return vaccineStatusReport;
                        }).
                        sorted(Comparator.comparing(VaccineStatusReport::getUpdated_On).reversed()).collect(Collectors.toList()).subList(0,15);
                model.addAttribute("totalCovaxinAdministered",dailyVaccineStatusReports.stream().map(VaccineStatusReport::getTotal_Covaxin_Administered).toArray());
                model.addAttribute("totalCoviShieldAdministered",dailyVaccineStatusReports.stream().map(VaccineStatusReport::getTotal_CoviShield_Administered).toArray());
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-mm-dd");
                SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yy");
                model.addAttribute("updatedOnDate",dailyVaccineStatusReports.stream().map(i->{
                    String dateWithFormat = "";
                    try {
                        dateWithFormat = format2.format(format1.parse(i.getUpdated_On()));
                        dateWithFormat = dateWithFormat.substring(0,7).replaceAll("-"," ");
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    return dateWithFormat;
                }).toArray());
            }

            TrackUser trackUser = new TrackUser();
            trackUser.setUserHost(InetAddress.getByName(request.getRemoteAddr()).getHostName());
            trackUser.setIpAddress(request.getRemoteAddr());
            trackUser.setAccessURL("/countryDashboard");
            accessStatusReposiory.saveAndFlush(trackUser);
            return new ModelAndView("countryDashboard");

        } catch (Exception e) {
            covidControllerStatus = e.getMessage();
            try {
                ErrorStatus error = new ErrorStatus();
                error.setUserHost(InetAddress.getByName(request.getRemoteAddr()).getHostName());
                error.setIpAddress(request.getRemoteAddr());
                error.setAccessURL("/countryDashboard");
                error.setErrorMessgae(e.getMessage());
                errorStatusReposiory.saveAndFlush(error);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
        return new ModelAndView("serviceDown");
    }

    private List<StateStatusReport> getStateStatusReports(List<StateWiseStatusReport> stateWiseStatusReports,List<DistrictWiseStatusReport> districtWiseStatusReports) {
        List<StateStatusReport> stateStatusReportsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(stateWiseStatusReports)) {
            stateWiseStatusReports = stateWiseStatusReports.stream().filter(country -> country.getState_code() != null && !"TT".equalsIgnoreCase(country.getState_code())).
                    filter(c -> !"State Unassigned".equalsIgnoreCase(c.getState())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(stateWiseStatusReports) && CollectionUtils.isNotEmpty(districtWiseStatusReports)) {
                final Map<String,List<DistrictWiseStatusReport>> stateDistrictMapping = districtWiseStatusReports.stream().filter(d->d.getState_Code()!=null).
                        collect(Collectors.groupingBy(DistrictWiseStatusReport::getState_Code, Collectors.toList()));
                stateWiseStatusReports.forEach(countryReport -> {
                    List<DistrictWiseStatusReport> districtWiseStatuses = new ArrayList<>();
                    if(stateDistrictMapping.get(countryReport.getState_code())!=null) {
                        districtWiseStatuses = stateDistrictMapping.get(countryReport.getState_code());
                    }
                    StateStatusReport stateStatusReport = new StateStatusReport(countryReport.getState(),
                            countryReport.getConfirmed(), countryReport.getDeaths(), countryReport.getRecovered(), countryReport.getActive(),countryReport.getState_Notes(),countryReport.getState_code());
                    stateStatusReport.setDistrictData(districtWiseStatuses);
                    stateStatusReportsList.add(stateStatusReport);
                });
            }
        }
        return stateStatusReportsList;
    }

    private File downloadReportFile(String endpoint) {
        File file = restTemplate.execute(endpoint, HttpMethod.GET, null, clientHttpResponse -> {
            File ret = File.createTempFile(endpoint, ".csv");
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
        return file;
    }

    private List getReport(String endpoint) {
        List statusReports = new ArrayList();
        HeaderColumnNameTranslateMappingStrategy strategy
                = new HeaderColumnNameTranslateMappingStrategy<>();
        File file = downloadReportFile(endpoint);
        if(endpoint.endsWith("latest/state_wise.csv")){
            statusReports = new ArrayList<StateWiseStatusReport>();
            strategy.setType(StateWiseStatusReport.class);
            strategy.setColumnMapping(StateWiseStatusReport.getColumnMapping());
        }
        else if(endpoint.endsWith("latest/state_wise_daily.csv")){
            statusReports = new ArrayList<DailyStateWiseStatusReport>();
            strategy.setType(DailyStateWiseStatusReport.class);
            strategy.setColumnMapping(DailyStateWiseStatusReport.getColumnMapping());
        }
        else if(endpoint.endsWith("latest/cowin_vaccine_data_statewise.csv")){
            statusReports = new ArrayList<VaccineStatusReport>();
            strategy.setType(VaccineStatusReport.class);
            strategy.setColumnMapping(VaccineStatusReport.getColumnMapping());
        }
        else if(endpoint.endsWith("latest/district_wise.csv")){
            statusReports = new ArrayList<DistrictWiseStatusReport>();
            strategy.setType(DistrictWiseStatusReport.class);
            strategy.setColumnMapping(DistrictWiseStatusReport.getColumnMapping());
        }

        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(file));
            CsvToBean csvToBean = new CsvToBean();
            csvToBean.setMappingStrategy(strategy);
            csvToBean.setCsvReader(csvReader);
            statusReports = csvToBean.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return statusReports;
    }

    @GetMapping("/error")
    public ModelAndView errorPage() {
        return new ModelAndView("error");
    }

    @GetMapping("/stateStatus")
    @ResponseBody
    public StateStatusJsonResponse getStateStatusJsonResponse(){
        List<StateWiseStatusReport> summerizedStatusReports = getReport(stateWiseReportEndpoint);
        List<DistrictWiseStatusReport> districtWiseStatusReports = getReport(districtWiseReportEndpoint);
        List<StateStatusReport> stateStatusReportList = getStateStatusReports(summerizedStatusReports,districtWiseStatusReports);
        StateStatusJsonResponse stateStatusJsonResponse = new StateStatusJsonResponse();
        stateStatusJsonResponse.setData(stateStatusReportList);
        return stateStatusJsonResponse;
    }
}
