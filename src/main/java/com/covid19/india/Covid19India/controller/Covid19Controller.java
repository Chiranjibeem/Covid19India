package com.covid19.india.Covid19India.controller;

import com.covid19.india.Covid19India.model.CoronaStatus;
import com.covid19.india.Covid19India.model.DistrictData;
import com.covid19.india.Covid19India.model.ErrorStatus;
import com.covid19.india.Covid19India.model.ReportData;
import com.covid19.india.Covid19India.model.StateAllStatus;
import com.covid19.india.Covid19India.model.TrackUser;
import com.covid19.india.Covid19India.repository.CovidAccessStatusReposiory;
import com.covid19.india.Covid19India.repository.CovidErrorStatusReposiory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class Covid19Controller {

    public static HashMap<String, String> stateCode = new HashMap<>();

    @PostConstruct
    public void initializeStateMap(){
        stateCode.put("AN", "Andaman and Nicobar Islands");
        stateCode.put("AP", "Andhra Pradesh");
        stateCode.put("AD", "Andhra Pradesh");
        stateCode.put("AR", "Arunachal Pradesh");
        stateCode.put("AS", "Assam");
        stateCode.put("BR", "Bihar");
        stateCode.put("CH", "Chandigarh");
        stateCode.put("CT", "Chhattisgarh");
        stateCode.put("DN", "Dadra and Nagar Haveli");
        stateCode.put("DD", "Daman and Diu");
        stateCode.put("DL", "Delhi");
        stateCode.put("GA", "Goa");
        stateCode.put("GJ", "Gujarat");
        stateCode.put("HR", "Haryana");
        stateCode.put("HP", "Himachal Pradesh");
        stateCode.put("JK", "Jammu and Kashmir");
        stateCode.put("JH", "Jharkhand");
        stateCode.put("KA", "Karnataka");
        stateCode.put("KL", "Kerala");
        stateCode.put("LA", "Ladakh");
        stateCode.put("LD", "Lakshadweep");
        stateCode.put("MP", "Madhya Pradesh");
        stateCode.put("MH", "Maharashtra");
        stateCode.put("MN", "Manipur");
        stateCode.put("ML", "Meghalaya");
        stateCode.put("MZ", "Mizoram");
        stateCode.put("NL", "Nagaland");
        stateCode.put("OR", "Odisha");
        stateCode.put("PY", "Puducherry");
        stateCode.put("PB", "Punjab");
        stateCode.put("RJ", "Rajasthan");
        stateCode.put("SK", "Sikkim");
        stateCode.put("TN", "Tamil Nadu");
        stateCode.put("TG", "Telangana");
        stateCode.put("TR", "Tripura");
        stateCode.put("UP", "Uttar Pradesh");
        stateCode.put("UT", "Uttarakhand");
        stateCode.put("WB", "West Bengal");
    }

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    CovidAccessStatusReposiory accessStatusReposiory;

    @Autowired
    CovidErrorStatusReposiory errorStatusReposiory;

    @Value("${covidStatusAPI}")
    private String covidStatusAPI;// = "https://api.covid19india.org/v2/state_district_wise.json";
    @Value("${dailyConfirmedCase}")
    private String dailyConfirmedCase;// = "https://api.covid19india.org/states_daily_csv/confirmed.csv";
    @Value("${dailyDeceasedCase}")
    private String dailyDeceasedCase;// = "https://api.covid19india.org/states_daily_csv/deceased.csv";
    @Value("${dailyRecoveredCase}")
    private String dailyRecoveredCase;// = "https://api.covid19india.org/states_daily_csv/recovered.csv";
    private String covidControllerStatus = "";

    public List<CoronaStatus> fetchCovidStatusDistrictWise() throws Exception {
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
            List<ReportData> confirmedCaseList = getAllReport(dailyConfirmedCase);
            List<ReportData> deceasedCaseList = getAllReport(dailyDeceasedCase);
            List<ReportData> recoveredCaseList = getAllReport(dailyRecoveredCase);

            model.addAttribute("stateAllStatuses", getAllStateStatus(confirmedCaseList, deceasedCaseList, recoveredCaseList));

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
            List<ReportData> confirmedCaseList = getAllReport(dailyConfirmedCase);
            List<ReportData> deceasedCaseList = getAllReport(dailyDeceasedCase);
            List<ReportData> recoveredCaseList = getAllReport(dailyRecoveredCase);

            List<StateAllStatus> stateAllStatuses = getAllStateStatus(confirmedCaseList, deceasedCaseList, recoveredCaseList);
            List<CoronaStatus> statusList = fetchCovidStatusDistrictWise();

            Iterator<StateAllStatus> iterator = stateAllStatuses.iterator();
            while (iterator.hasNext()) {
                StateAllStatus stateAllStatus = iterator.next();
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

            StateAllStatus stateAllStatus = new StateAllStatus("India", stateAllStatuses.stream().mapToInt(StateAllStatus::getConfirmedCase).sum(),
                    stateAllStatuses.stream().mapToInt(StateAllStatus::getDeceasedCase).sum(),
                    stateAllStatuses.stream().mapToInt(StateAllStatus::getRecoveredCase).sum());
            model.addAttribute("stateAllStatuses", stateAllStatuses);
            model.addAttribute("stateAllStatus", stateAllStatus);
            model.addAttribute("confirmedCaseData", confirmedCaseList.stream().map(i -> i.getTT()).toArray());
            model.addAttribute("recoveredCaseData", recoveredCaseList.stream().map(i -> i.getTT()).toArray());
            model.addAttribute("deceasedCaseData", deceasedCaseList.stream().map(i -> i.getTT()).toArray());
            model.addAttribute("confirmedCaseHeader", confirmedCaseList.stream().map(i -> i.getDate().substring(0, 2)).toArray());
            model.addAttribute("recoveredCaseHeader", recoveredCaseList.stream().map(i -> i.getDate().substring(0, 2)).toArray());
            model.addAttribute("deceasedCaseHeader", deceasedCaseList.stream().map(i -> i.getDate().substring(0, 2)).toArray());

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

    public List<StateAllStatus> getAllStateStatus(List<ReportData> confirmedCaseList, List<ReportData> deceasedCaseList, List<ReportData> recoveredCaseList) {
        List<StateAllStatus> stateAllStatuses = new ArrayList<>();
        StateAllStatus stateAllStatus = new StateAllStatus(stateCode.get("AN"),
                confirmedCaseList.stream().mapToInt(ReportData::getAN).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getAN).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getAN).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("AP"),
                confirmedCaseList.stream().mapToInt(ReportData::getAP).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getAP).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getAP).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("AR"),
                confirmedCaseList.stream().mapToInt(ReportData::getAR).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getAR).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getAR).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("AS"),
                confirmedCaseList.stream().mapToInt(ReportData::getAS).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getAS).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getAS).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("BR"),
                confirmedCaseList.stream().mapToInt(ReportData::getBR).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getBR).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getBR).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("CH"),
                confirmedCaseList.stream().mapToInt(ReportData::getCH).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getCH).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getCH).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("CT"),
                confirmedCaseList.stream().mapToInt(ReportData::getCT).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getCT).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getCT).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("DD"),
                confirmedCaseList.stream().mapToInt(ReportData::getDD).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getDD).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getDD).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("DL"),
                confirmedCaseList.stream().mapToInt(ReportData::getDL).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getDL).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getDL).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("DN"),
                confirmedCaseList.stream().mapToInt(ReportData::getDN).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getDN).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getDN).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("GA"),
                confirmedCaseList.stream().mapToInt(ReportData::getGA).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getGA).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getGA).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("GJ"),
                confirmedCaseList.stream().mapToInt(ReportData::getGJ).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getGJ).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getGJ).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("HP"),
                confirmedCaseList.stream().mapToInt(ReportData::getHP).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getHP).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getHP).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("HR"),
                confirmedCaseList.stream().mapToInt(ReportData::getHR).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getHR).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getHR).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("JH"),
                confirmedCaseList.stream().mapToInt(ReportData::getJH).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getJH).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getJH).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("JK"),
                confirmedCaseList.stream().mapToInt(ReportData::getJK).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getJK).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getJK).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("KA"),
                confirmedCaseList.stream().mapToInt(ReportData::getKA).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getKA).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getKA).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("KL"),
                confirmedCaseList.stream().mapToInt(ReportData::getKL).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getKL).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getKL).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("LA"),
                confirmedCaseList.stream().mapToInt(ReportData::getLA).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getLA).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getLA).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("LD"),
                confirmedCaseList.stream().mapToInt(ReportData::getLD).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getLD).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getLD).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("MH"),
                confirmedCaseList.stream().mapToInt(ReportData::getMH).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getMH).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getMH).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("ML"),
                confirmedCaseList.stream().mapToInt(ReportData::getML).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getML).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getML).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("MN"),
                confirmedCaseList.stream().mapToInt(ReportData::getMN).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getMN).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getMN).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("MP"),
                confirmedCaseList.stream().mapToInt(ReportData::getMP).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getMP).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getMP).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("MZ"),
                confirmedCaseList.stream().mapToInt(ReportData::getMZ).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getMZ).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getMZ).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("NL"),
                confirmedCaseList.stream().mapToInt(ReportData::getNL).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getNL).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getNL).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("OR"),
                confirmedCaseList.stream().mapToInt(ReportData::getOR).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getOR).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getOR).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("PB"),
                confirmedCaseList.stream().mapToInt(ReportData::getPB).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getPB).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getPB).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("PY"),
                confirmedCaseList.stream().mapToInt(ReportData::getPY).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getPY).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getPY).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("RJ"),
                confirmedCaseList.stream().mapToInt(ReportData::getRJ).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getRJ).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getRJ).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("SK"),
                confirmedCaseList.stream().mapToInt(ReportData::getSK).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getSK).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getSK).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("TG"),
                confirmedCaseList.stream().mapToInt(ReportData::getTG).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getTG).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getTG).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("TN"),
                confirmedCaseList.stream().mapToInt(ReportData::getTN).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getTN).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getTN).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("TR"),
                confirmedCaseList.stream().mapToInt(ReportData::getTR).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getTR).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getTR).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("UP"),
                confirmedCaseList.stream().mapToInt(ReportData::getUP).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getUP).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getUP).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("UT"),
                confirmedCaseList.stream().mapToInt(ReportData::getUT).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getUT).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getUT).sum());
        stateAllStatuses.add(stateAllStatus);

        stateAllStatus = new StateAllStatus(stateCode.get("WB"),
                confirmedCaseList.stream().mapToInt(ReportData::getWB).sum(),
                deceasedCaseList.stream().mapToInt(ReportData::getWB).sum(),
                recoveredCaseList.stream().mapToInt(ReportData::getWB).sum());
        stateAllStatuses.add(stateAllStatus);

        return stateAllStatuses;

    }

    public File downloadReportFile(String type) {
        File file = restTemplate.execute(type, HttpMethod.GET, null, clientHttpResponse -> {
            File ret = File.createTempFile(type, ".csv");
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });
        return file;
    }

    public List<ReportData> getAllReport(String type) {
        File file = downloadReportFile(type);
        List<ReportData> reportList = new ArrayList<>();
        HeaderColumnNameTranslateMappingStrategy<ReportData> strategy
                = new HeaderColumnNameTranslateMappingStrategy<ReportData>();
        strategy.setType(ReportData.class);
        strategy.setColumnMapping(ReportData.hashMap);

        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(file));
            CsvToBean csvToBean = new CsvToBean();
            csvToBean.setMappingStrategy(strategy);
            csvToBean.setCsvReader(csvReader);
            reportList = csvToBean.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return reportList;
    }

    @GetMapping("/error")
    public ModelAndView errorPage() {
        return new ModelAndView("error");
    }

}
