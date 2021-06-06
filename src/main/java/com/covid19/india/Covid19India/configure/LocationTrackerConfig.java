package com.covid19.india.Covid19India.configure;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.covid19.india.Covid19India.model.LocationConfigurationException;

@Configuration
@Component
public class LocationTrackerConfig {

	@Autowired
	RestTemplate restTemplate;

	public String getLocation() throws Exception {
		String responseBody = "";
		try {
			String ipAddress = getIPAddress();
			String endpoint = "https://tools.keycdn.com/geo.json?host=" + ipAddress;
			HttpHeaders headers = new HttpHeaders();
			headers.set("User-Agent", "keycdn-tools:https://" + ipAddress);
			HttpEntity entity = new HttpEntity(headers);
			ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);
			responseBody = response.getBody();
		} catch (Exception e) {
			throw new LocationConfigurationException("Error Occured While Checking Location : "+e.getMessage());
		}
		return responseBody;
	}

	private String getIPAddress() throws Exception {
		String publicIPAddress = "";
		URL url_name = new URL("http://bot.whatismyipaddress.com");
		try (BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()))) {
			publicIPAddress = sc.readLine().trim();
		}
		return publicIPAddress;
	}
}