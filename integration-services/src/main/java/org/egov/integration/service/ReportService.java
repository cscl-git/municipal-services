package org.egov.integration.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.PreApplicationRunnerImpl;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.common.ModuleNameConstants;
import org.egov.integration.config.ApiConfiguration;
import org.egov.integration.model.DisplayColumns;
import org.egov.integration.model.ReportRequest;
import org.egov.integration.model.RequestData;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.tracer.model.ServiceCallException;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReportService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ApiConfiguration config;

	public ResponseEntity<ResponseInfoWrapper> getData(ReportRequest request) throws JSONException, ParseException {

		DisplayColumns redata = PreApplicationRunnerImpl.getSqlQuery(request.getRequestBody().getModuleName());
		StringBuilder url = new StringBuilder(config.getOPMSHost()).append(redata.getEndPoint());
		JSONObject dataPayload = new JSONObject();
		JSONObject resData1 = new JSONObject();
		Gson gson = new Gson();
		long fromdate = 0;
		long todate = 0;
		JSONObject response = new JSONObject();
		RequestData requests = new RequestData();
		if (redata.getParameter1Format().equalsIgnoreCase("Long")) {
			ZonedDateTime zdt = LocalDate.now(ZoneId.of("Etc/UTC")).atTime(LocalTime.MIDNIGHT)
					.atZone(ZoneId.of("Etc/UTC"));
			fromdate = zdt.toInstant().toEpochMilli();
			ZonedDateTime zdt1 = zdt.plusDays(1);
			todate = zdt1.toInstant().toEpochMilli();

		}
		if (redata.getParameter1Format().equalsIgnoreCase("yyyy-mm-dd")) {

			dataPayload.put(redata.getParameter1(), LocalDate.now());
			dataPayload.put(redata.getParameter2(), LocalDate.now());

		}

		if (request.getRequestBody().getModuleName().equalsIgnoreCase(ModuleNameConstants.PETNOC)) {
			dataPayload.put(redata.getParameter1(), fromdate);
			dataPayload.put(redata.getParameter2(), todate);
			requests = new RequestData(request.getRequestInfo(), request.getRequestBody().getModuleName(), null, null,
					dataPayload, null);

			JsonNode result = fetchResult(url, requests);

			if (result != null) {
				for (JsonNode userInfo : result.get("nocApplicationDetail")) {
					LocalDate date = Instant.ofEpochMilli(userInfo.get("createdTime").asLong())
							.atZone(ZoneId.systemDefault()).toLocalDate();
					response.put(ModuleNameConstants.applicantId, userInfo.get("applicationId"));
					response.put(ModuleNameConstants.applicantSector, userInfo.get("sector"));
					response.put(ModuleNameConstants.applicantSumbmissionDate, date);

				}
			}
		}
		if (request.getRequestBody().getModuleName().equalsIgnoreCase(ModuleNameConstants.HORTICULTURE)) {
			requests = new RequestData(request.getRequestInfo(), request.getRequestBody().getModuleName(), fromdate,
					todate, null, null);

			JsonNode result = fetchResult(url, requests);

			if (result != null) {
				for (JsonNode userInfo : result.get("services")) {
					String startDateString = userInfo.get("createdtime").toString().replaceAll("\"", "");
				    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
				  
					response.put(ModuleNameConstants.applicantId, userInfo.get("service_request_id"
							+ ""));
					response.put(ModuleNameConstants.applicantSector, userInfo.get("locality"));
					response.put(ModuleNameConstants.applicantSumbmissionDate, sdf2.format(sdf.parse(startDateString)));

				}
			}
		}
		if (request.getRequestBody().getModuleName().equalsIgnoreCase(ModuleNameConstants.ECHALLAN)) {

			dataPayload.put("tenantId", "ch.chandigarh");
			dataPayload.put("encroachmentType", "");
			dataPayload.put("sector", "");
			dataPayload.put("siName", "");
			dataPayload.put("status", "");
			Object req = gson.fromJson(dataPayload.toString(), Object.class);

			requests = new RequestData(request.getRequestInfo(), request.getRequestBody().getModuleName(), null, null,
					null, req);

			JsonNode result = fetchResult(url, requests);

			if (result != null) {
				for (JsonNode userInfo : result.get("ResponseBody")) {
					LocalDate date = Instant.ofEpochMilli(userInfo.get("createdTime").longValue())
							.atZone(ZoneId.systemDefault()).toLocalDate();
					response.put(ModuleNameConstants.applicantId, userInfo.get("challanId"));
					response.put(ModuleNameConstants.applicantSector ,userInfo.get("sector"));
					response.put(ModuleNameConstants.applicantSumbmissionDate, date);

				}
			}

		}

		return new ResponseEntity<>(ResponseInfoWrapper.builder()
				.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build())
				.responseBody(gson.fromJson(response.toString(), Object.class)).build(), HttpStatus.OK);
	}

	public JsonNode fetchResult(StringBuilder uri, Object request) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		JsonNode response = null;
		log.info("URI: " + uri.toString());
		try {
			log.info("Request: " + mapper.writeValueAsString(request));
			response = restTemplate.postForObject(uri.toString(), request, JsonNode.class);
		} catch (HttpClientErrorException e) {
			log.error("External Service threw an Exception: ", e);
			throw new ServiceCallException(e.getResponseBodyAsString());
		} catch (Exception e) {
			log.error("Exception while fetching from searcher: ", e);
		}

		return response;

	}
}
