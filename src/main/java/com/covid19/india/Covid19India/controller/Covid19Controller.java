package com.covid19.india.Covid19India.controller;

import com.covid19.india.Covid19India.configure.LocationTrackerConfig;
import com.covid19.india.Covid19India.model.*;
import com.covid19.india.Covid19India.repository.TrackerUserRequestRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import com.covid19.india.Covid19India.model.ApiResponse;

@Controller
public class Covid19Controller {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private TrackerUserRequestRepository trackerUserRequestRepository;

	@Autowired
	private LocationTrackerConfig locationTrackerConfig;

	@Value("${stateWiseReportEndpoint}")
	private String stateWiseReportEndpoint; // https://api.covid19india.org/csv/latest/state_wise.csv
	@Value("${dailyStateWiseReportEndpoint}")
	private String dailyStateWiseReportEndpoint; // https://api.covid19india.org/csv/latest/state_wise_daily.csv
	@Value("${dailyVaccineReportEndpoint}")
	private String dailyVaccineReportEndpoint; // http://api.covid19india.org/csv/latest/cowin_vaccine_data_statewise.csv
	@Value("${districtWiseReportEndpoint}")
	private String districtWiseReportEndpoint; // https://api.covid19india.org/csv/latest/district_wise.csv

	@GetMapping("/")
	public ModelAndView getCountryDashboard(Model model, HttpServletRequest request, HttpServletResponse response) {
		System.out.println("Start Time :"+new Date());
		try {
			CompletableFuture<List<StateWiseStatusReport>> future1
					= CompletableFuture.supplyAsync(() -> getReport(stateWiseReportEndpoint));
			CompletableFuture<List<DailyStateWiseStatusReport>> future2
					= CompletableFuture.supplyAsync(() -> getReport(dailyStateWiseReportEndpoint));
			CompletableFuture<List<VaccineStatusReport>> future3
					= CompletableFuture.supplyAsync(() -> getReport(dailyVaccineReportEndpoint));

			CompletableFuture<Void> combinedFuture
					= CompletableFuture.allOf(future1, future2, future3);

			List<StateWiseStatusReport> summerizedStatusReports = future1.get();
			List<DailyStateWiseStatusReport> dailyStateWiseStatusReports = future2.get();
			List<VaccineStatusReport> dailyVaccineStatusReports = future3.get();

			List<StateStatusReport> stateStatusReports = summerizedStatusReports.stream()
					.filter(country -> country.getState_code() != null
							&& !"TT".equalsIgnoreCase(country.getState_code()))
					.filter(c -> !"State Unassigned".equalsIgnoreCase(c.getState())).map(summerized -> {
						StateStatusReport stateStatusReport = new StateStatusReport(summerized.getState(),
								summerized.getConfirmed(), summerized.getDeaths(), summerized.getRecovered(),
								summerized.getActive(), summerized.getState_Notes(), summerized.getState_code());
						return stateStatusReport;
					}).collect(Collectors.toList());

			StateWiseStatusReport countryStatusReport = new StateWiseStatusReport();
			if (CollectionUtils.isNotEmpty(summerizedStatusReports)) {
				countryStatusReport = summerizedStatusReports.stream()
						.filter(countryStatus -> countryStatus.getState() != null
								&& "TT".equalsIgnoreCase(countryStatus.getState_code()))
						.collect(Collectors.toList()).get(0);
			}
			StateStatusReportJson countryStatus = new StateStatusReportJson("India", countryStatusReport.getConfirmed(),
					countryStatusReport.getDeaths(), countryStatusReport.getRecovered(),
					countryStatusReport.getActive(), "", countryStatusReport.getState_code());

			model.addAttribute("stateAllStatuses", stateStatusReports);
			model.addAttribute("stateAllStatus", countryStatus);

			if (CollectionUtils.isNotEmpty(dailyStateWiseStatusReports)) {
				dailyStateWiseStatusReports = dailyStateWiseStatusReports.stream()
						.sorted(Comparator.comparing(DailyStateWiseStatusReport::getDate_YMD).reversed())
						.collect(Collectors.toList()).subList(0, 30);
				List<DailyStateWiseStatusReport> dailyConfirmedCountryStatusReprts = dailyStateWiseStatusReports
						.stream().filter(i -> "Confirmed".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
				List<DailyStateWiseStatusReport> dailyRecoveredCountryStatusReprts = dailyStateWiseStatusReports
						.stream().filter(i -> "Recovered".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());
				List<DailyStateWiseStatusReport> dailyDeceasedCountryStatusReprts = dailyStateWiseStatusReports.stream()
						.filter(i -> "Deceased".equalsIgnoreCase(i.getStatus())).collect(Collectors.toList());

				model.addAttribute("confirmedCaseData",
						dailyConfirmedCountryStatusReprts.stream().map(DailyStateWiseStatusReport::getTT).toArray());
				model.addAttribute("recoveredCaseData",
						dailyRecoveredCountryStatusReprts.stream().map(DailyStateWiseStatusReport::getTT).toArray());
				model.addAttribute("deceasedCaseData",
						dailyDeceasedCountryStatusReprts.stream().map(DailyStateWiseStatusReport::getTT).toArray());
				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
				Object[] dateWithFormatArray = dailyConfirmedCountryStatusReprts.stream().map(i -> {
					String dateWithFormat = "";
					try {
						dateWithFormat = format2.format(format1.parse(i.getDate_YMD()));
						dateWithFormat = dateWithFormat.substring(0, 7).replaceAll("-", " ");
					} catch (Exception e) {
						e.printStackTrace();
					}
					return dateWithFormat;
				}).toArray();
				model.addAttribute("confirmedCaseHeader", dateWithFormatArray);
				model.addAttribute("recoveredCaseHeader", dateWithFormatArray);
				model.addAttribute("deceasedCaseHeader",  dateWithFormatArray);
			}

			if (CollectionUtils.isNotEmpty(dailyVaccineStatusReports)) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				dailyVaccineStatusReports = dailyVaccineStatusReports.stream()
						.filter(i -> "India".equalsIgnoreCase(i.getState())).map(i -> {
							VaccineStatusReport vaccineStatusReport = new VaccineStatusReport();
							vaccineStatusReport.setState(i.getState());
							vaccineStatusReport.setUpdated_On(LocalDate.parse(i.getUpdated_On(), formatter).toString());
							vaccineStatusReport.setTotal_Covaxin_Administered(i.getTotal_Covaxin_Administered());
							vaccineStatusReport.setTotal_CoviShield_Administered(i.getTotal_CoviShield_Administered());
							return vaccineStatusReport;
						}).sorted(Comparator.comparing(VaccineStatusReport::getUpdated_On).reversed())
						.collect(Collectors.toList());

				dailyVaccineStatusReports =  dailyVaccineStatusReports.stream().filter(i->LocalDate.now().isAfter(LocalDate.parse(i.getUpdated_On()))).collect(Collectors.toList()).subList(0,15);
				model.addAttribute("totalCovaxinAdministered", dailyVaccineStatusReports.stream()
						.map(VaccineStatusReport::getTotal_Covaxin_Administered).toArray());
				model.addAttribute("totalCoviShieldAdministered", dailyVaccineStatusReports.stream()
						.map(VaccineStatusReport::getTotal_CoviShield_Administered).toArray());
				SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat format2 = new SimpleDateFormat("dd-MMM-yyyy");
				model.addAttribute("updatedOnDate", dailyVaccineStatusReports.stream().map(i -> {
					String dateWithFormat = "";
					try {
						dateWithFormat = format2.format(format1.parse(i.getUpdated_On()));
						dateWithFormat = dateWithFormat.substring(0, 7).replaceAll("-", " ");
					} catch (Exception e) {
						e.printStackTrace();
					}
					return dateWithFormat;
				}).toArray());
			}
			System.out.println("End Time :"+new Date());
			return new ModelAndView("countryDashboard");
		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			String locationTrackerResponse = "";
			try {
				locationTrackerResponse = locationTrackerConfig.getLocation();
			} catch (Exception loc) {
				locationTrackerResponse = loc.getMessage();
			}
			TrackUserRequest trackUser = new TrackUserRequest();
			trackUser.setAccessURL("/error");
			trackUser.setClientInformation(locationTrackerResponse);
			trackUser.setErrorMsg(errorMessage);
			trackerUserRequestRepository.saveAndFlush(trackUser);
		}
		return new ModelAndView("serviceDown");
	}

	private List<StateStatusReportJson> getStateStatusReports(List<StateWiseStatusReport> stateWiseStatusReports,
			List<DistrictWiseStatusReport> districtWiseStatusReports) {
		List<StateStatusReportJson> stateStatusReportsList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(stateWiseStatusReports)) {
			stateWiseStatusReports = stateWiseStatusReports.stream().filter(
					country -> country.getState_code() != null && !"TT".equalsIgnoreCase(country.getState_code()))
					.filter(c -> !"State Unassigned".equalsIgnoreCase(c.getState())).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(stateWiseStatusReports)
					&& CollectionUtils.isNotEmpty(districtWiseStatusReports)) {
				final Map<String, List<DistrictWiseStatusReport>> stateDistrictMapping = districtWiseStatusReports
						.stream().filter(d -> d.getState_Code() != null)
						.collect(Collectors.groupingBy(DistrictWiseStatusReport::getState_Code, Collectors.toList()));
				stateWiseStatusReports.forEach(countryReport -> {
					List<DistrictWiseStatusReport> districtWiseStatuses = new ArrayList<>();
					if (stateDistrictMapping.get(countryReport.getState_code()) != null) {
						districtWiseStatuses = stateDistrictMapping.get(countryReport.getState_code());
					}
					StateStatusReportJson stateStatusReport = new StateStatusReportJson(countryReport.getState(),
							countryReport.getConfirmed(), countryReport.getDeaths(), countryReport.getRecovered(),
							countryReport.getActive(), countryReport.getState_Notes(), countryReport.getState_code());
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
		HeaderColumnNameTranslateMappingStrategy strategy = new HeaderColumnNameTranslateMappingStrategy<>();
		File file = downloadReportFile(endpoint);
		if (endpoint.endsWith("latest/state_wise.csv")) {
			statusReports = new ArrayList<StateWiseStatusReport>();
			strategy.setType(StateWiseStatusReport.class);
			strategy.setColumnMapping(StateWiseStatusReport.getColumnMapping());
		} else if (endpoint.endsWith("latest/state_wise_daily.csv")) {
			statusReports = new ArrayList<DailyStateWiseStatusReport>();
			strategy.setType(DailyStateWiseStatusReport.class);
			strategy.setColumnMapping(DailyStateWiseStatusReport.getColumnMapping());
		} else if (endpoint.endsWith("latest/cowin_vaccine_data_statewise.csv")) {
			statusReports = new ArrayList<VaccineStatusReport>();
			strategy.setType(VaccineStatusReport.class);
			strategy.setColumnMapping(VaccineStatusReport.getColumnMapping());
		} else if (endpoint.endsWith("latest/district_wise.csv")) {
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
	public StateStatusJsonResponse getStateStatusJsonResponse() {
		List<StateWiseStatusReport> summerizedStatusReports = getReport(stateWiseReportEndpoint);
		List<DistrictWiseStatusReport> districtWiseStatusReports = getReport(districtWiseReportEndpoint);
		List<StateStatusReportJson> stateStatusReportList = getStateStatusReports(summerizedStatusReports,
				districtWiseStatusReports);
		StateStatusJsonResponse stateStatusJsonResponse = new StateStatusJsonResponse();
		stateStatusJsonResponse.setData(stateStatusReportList);
		return stateStatusJsonResponse;
	}

	@PostMapping("/trackUserRequest")
	@ResponseBody
	public ApiResponse saveClientInformation(@RequestBody String clientInformation){
		TrackUserRequest trackUserRequest = new TrackUserRequest();
		trackUserRequest.setAccessURL("/countryDashboard");
		trackUserRequest.setClientInformation(clientInformation);
		trackerUserRequestRepository.saveAndFlush(trackUserRequest);
		ApiResponse apiResponse = new ApiResponse("success",200,"Client Details Saved Successfully","Sucess");
		return apiResponse;
	}
}
