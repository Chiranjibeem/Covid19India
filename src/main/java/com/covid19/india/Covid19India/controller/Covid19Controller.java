package com.covid19.india.Covid19India.controller;

import com.covid19.india.Covid19India.model.*;
import com.covid19.india.Covid19India.repository.CovidAccessStatusReposiory;
import com.covid19.india.Covid19India.repository.CovidErrorStatusReposiory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

@Controller
public class Covid19Controller {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CovidAccessStatusReposiory accessStatusReposiory;

    @Autowired
    private CovidErrorStatusReposiory errorStatusReposiory;

    @Value("${covidStatusAPI}")
    private String covidStatusAPI;// = "https://api.covid19india.org/v2/state_district_wise.json";

    private String covidControllerStatus = "";

    @Value("${countryReportEndpoint}")
    private String countryReportEndpoint;       //https://api.covid19india.org/csv/latest/state_wise.csv
    @Value("${dailyCountryReportEndpoint}")
    private String dailyCountryReportEndpoint;  //https://api.covid19india.org/csv/latest/state_wise_daily.csv
    @Value("${dailyVaccineReportEndpoint}")
    private String dailyVaccineReportEndpoint;  //http://api.covid19india.org/csv/latest/cowin_vaccine_data_statewise.csv

    private List<CoronaStatus> fetchCovidStatusDistrictWise() throws Exception {
        List<CoronaStatus> statusList = null;
        String response = restTemplate.getForObject(covidStatusAPI, String.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        Collection<CoronaStatus> readValues = new ObjectMapper().readValue(
                response, new TypeReference<Collection<CoronaStatus>>() {
                }
        );
        statusList = readValues.stream().sorted((i, j) -> i.getState().compareTo(j.getState())).collect(Collectors.toList());
        return statusList;
    }

    @GetMapping("/districtDashboard")
    public ModelAndView fetchCovidAllDistrictStatus(Model model) {
        List<CoronaStatus> statusList = null;
        InetAddress inetAddress = null;
        try {
            statusList = fetchCovidStatusDistrictWise();
            model.addAttribute("statusList", statusList);

            inetAddress = InetAddress.getLocalHost();
            TrackUser trackUser = new TrackUser();
            trackUser.setUserHost(inetAddress.getHostName());
            trackUser.setIpAddress(String.valueOf(inetAddress.getAddress()));
            trackUser.setAccessURL("/districtDashboard");
            accessStatusReposiory.saveAndFlush(trackUser);

        } catch (Exception e) {
            covidControllerStatus = e.getMessage();
            try {
                inetAddress = InetAddress.getLocalHost();
                ErrorStatus error = new ErrorStatus();
                error.setUserHost(inetAddress.getHostName());
                error.setIpAddress(String.valueOf(inetAddress.getAddress()));
                error.setAccessURL("/districtDashboard");
                error.setErrorMessgae(e.getMessage());
                errorStatusReposiory.saveAndFlush(error);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
        return new ModelAndView("districtDashboard");
    }

    @GetMapping("/stateDashboard")
    public ModelAndView fetchCovidAllStateStatus(Model model) {
        InetAddress inetAddress = null;
        try {
            List<CountryStatusReport> CountryStatusReports = getStateWiseReport(countryReportEndpoint);

            model.addAttribute("stateAllStatuses", getStateStatusReports(CountryStatusReports));

            inetAddress = InetAddress.getLocalHost();

            TrackUser trackUser = new TrackUser();
            trackUser.setUserHost(inetAddress.getHostName());
            trackUser.setIpAddress(String.valueOf(inetAddress.getAddress()));
            trackUser.setAccessURL("/stateDashboard");
            accessStatusReposiory.saveAndFlush(trackUser);
        } catch (Exception e) {
            covidControllerStatus = e.getMessage();
            try {
                inetAddress = InetAddress.getLocalHost();
                ErrorStatus error = new ErrorStatus();
                error.setUserHost(inetAddress.getHostName());
                error.setIpAddress(String.valueOf(inetAddress.getAddress()));
                error.setAccessURL("/stateDashboard");
                error.setErrorMessgae(e.getMessage());
                errorStatusReposiory.saveAndFlush(error);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
        return new ModelAndView("stateDashboard");
    }

    @GetMapping("/")
    public ModelAndView fetchCovidCountryStatus(Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            List<CountryStatusReport> countryStatusReports = getStateWiseReport(countryReportEndpoint);
            CountryStatusReport countryStatusReport = new CountryStatusReport();
            if(CollectionUtils.isNotEmpty(countryStatusReports)) {
                countryStatusReport = countryStatusReports.stream().filter(countryStatus -> countryStatus.getState() != null && "TT".equalsIgnoreCase(countryStatus.getState_code())).collect(Collectors.toList()).get(0);
            }

            List<StateStatusReport> stateAllStatusList = getStateStatusReports(countryStatusReports);
            List<CoronaStatus> statusList = fetchCovidStatusDistrictWise();

            Iterator<StateStatusReport> iterator = stateAllStatusList.iterator();
            while (iterator.hasNext()) {
                StateStatusReport stateAllStatus = iterator.next();
                Iterator<CoronaStatus> itr = statusList.iterator();
                while (itr.hasNext()) {
                    CoronaStatus status = itr.next();
                    if (stateAllStatus.getName().equalsIgnoreCase(status.getState())) {
                        stateAllStatus.setDistrictData(status.getDistrictData());
                    }
                }
                StringBuffer buffer = new StringBuffer();
                if (stateAllStatus.getDistrictData() != null && stateAllStatus.getDistrictData().size() > 0) {
                    for (DistrictData districtData : stateAllStatus.getDistrictData()) {
                        buffer.append(districtData.getDistrict() + "-" + districtData.getConfirmed() + "<br/>");
                    }
                    stateAllStatus.setDistrictDataWithCase(buffer.toString());
                } else {
                    stateAllStatus.setDistrictDataWithCase("-");
                }
            }

            List<DailyCountryStatusReport> dailyCountryStatusReports = getStateWiseReport(dailyCountryReportEndpoint);

            StateStatusReport countryStatus = new StateStatusReport("India", countryStatusReport.getConfirmed(),
                    countryStatusReport.getDeaths(),countryStatusReport.getRecovered(),countryStatusReport.getActive());

            model.addAttribute("stateAllStatuses", stateAllStatusList);
            model.addAttribute("stateAllStatus", countryStatus);

            if(CollectionUtils.isNotEmpty(dailyCountryStatusReports)) {
                dailyCountryStatusReports = dailyCountryStatusReports.stream().sorted(Comparator.comparing(DailyCountryStatusReport::getDate_YMD).reversed()).collect(Collectors.toList()).subList(0,30);
                List<DailyCountryStatusReport> dailyConfirmedCountryStatusReprts = dailyCountryStatusReports.stream().filter(i->"Confirmed".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
                List<DailyCountryStatusReport> dailyRecoveredCountryStatusReprts = dailyCountryStatusReports.stream().filter(i->"Recovered".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
                List<DailyCountryStatusReport> dailyDeceasedCountryStatusReprts = dailyCountryStatusReports.stream().filter(i->"Deceased".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());

                model.addAttribute("confirmedCaseData", dailyConfirmedCountryStatusReprts.stream().map(DailyCountryStatusReport::getTT).toArray());
                model.addAttribute("recoveredCaseData", dailyRecoveredCountryStatusReprts.stream().map(DailyCountryStatusReport::getTT).toArray());
                model.addAttribute("deceasedCaseData",  dailyDeceasedCountryStatusReprts.stream().map(DailyCountryStatusReport::getTT).toArray());
                model.addAttribute("confirmedCaseHeader", dailyConfirmedCountryStatusReprts.stream().map(i -> i.getDate().substring(0, 7).replaceAll("-"," ")).toArray());
                model.addAttribute("recoveredCaseHeader", dailyRecoveredCountryStatusReprts.stream().map(i -> i.getDate().substring(0, 7).replaceAll("-"," ")).toArray());
                model.addAttribute("deceasedCaseHeader", dailyDeceasedCountryStatusReprts.stream().map(i -> i.getDate().substring(0, 7).replaceAll("-"," ")).toArray());
            }

            List<VaccineStatusReport> dailyVaccineStatusReports = getStateWiseReport(dailyVaccineReportEndpoint);
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

    private List<StateStatusReport> getStateStatusReports(List<CountryStatusReport> countryStatusReports) {
        List<StateStatusReport> stateStatusReports = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(countryStatusReports)) {
            countryStatusReports = countryStatusReports.stream().filter(country -> country.getState_code() != null && !"TT".equalsIgnoreCase(country.getState_code())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(countryStatusReports)) {
                countryStatusReports.forEach(countryReport -> {
                    StateStatusReport stateStatusReport = new StateStatusReport(countryReport.getState(),
                            countryReport.getConfirmed(), countryReport.getDeaths(), countryReport.getRecovered(), countryReport.getActive());
                    stateStatusReports.add(stateStatusReport);
                });
            }
        }
        return stateStatusReports;
    }

    private File downloadReportFile(String endpoint) {
        File file = restTemplate.execute(endpoint, HttpMethod.GET, null, clientHttpResponse -> {
            File ret = File.createTempFile(endpoint, ".csv");
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
        return file;
    }

    private List getStateWiseReport(String endpoint) {
        List statusReports = new ArrayList();
        HeaderColumnNameTranslateMappingStrategy strategy
                = new HeaderColumnNameTranslateMappingStrategy<>();
        File file = downloadReportFile(endpoint);
        if(endpoint.endsWith("latest/state_wise.csv")){
            statusReports = new ArrayList<CountryStatusReport>();
            strategy.setType(CountryStatusReport.class);
            strategy.setColumnMapping(CountryStatusReport.getColumnMapping());
        }
        else if(endpoint.endsWith("latest/state_wise_daily.csv")){
            statusReports = new ArrayList<DailyCountryStatusReport>();
            strategy.setType(DailyCountryStatusReport.class);
            strategy.setColumnMapping(DailyCountryStatusReport.getColumnMapping());
        }
        else if(endpoint.endsWith("latest/cowin_vaccine_data_statewise.csv")){
            statusReports = new ArrayList<VaccineStatusReport>();
            strategy.setType(VaccineStatusReport.class);
            strategy.setColumnMapping(VaccineStatusReport.getColumnMapping());
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

}
