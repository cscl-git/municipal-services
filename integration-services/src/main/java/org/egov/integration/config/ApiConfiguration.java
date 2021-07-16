package org.egov.integration.config;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Import({ TracerConfiguration.class })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class ApiConfiguration {

	@Value("${app.timezone}")
	private String timeZone;

	@PostConstruct
	public void initialize() {
		TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
	}

	@Bean
	@Autowired
	public MappingJackson2HttpMessageConverter jacksonConverter(ObjectMapper objectMapper) {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setObjectMapper(objectMapper);
		return converter;
	}

	@Value("${ehrms.nic.service.host}")
	private String ehrmsHost;

	@Value("${ehrms.nic.service.employeeDetail.path}")
	private String employeeDetails;
	
	@Value("${ehrms.nic.service.employeeLeaveDetail.path}")
	private String employeeLeaveDetails;
	
	@Value("${ehrms.nic.service.employeeJoiningDetails.path}")
	private String employeeJoiningDetails;
		
	@Value("${ehrms.nic.service.statelist.path}")
	private String hrmsStatelist;

	@Value("${ehrms.nic.service.backlog.path}")
	private String hrmsBacklog;

	@Value("${ehrms.nic.service.empnotification.path}")
	private String hrmsNotification;
	
	@Value("${ehrms.auth.header}")
	private String hrmsAuthHeader;

	@Value("${ehrms.auth.key}")
	private String hrmsAuthKey;

	@Value("${payroll.encryption.key}")
	private String encrptionKey;
	
	
	@Value("${persister.save.eofficeData.topic}")
	private String eofficeSaveTopic;
	
	@Value("${url.shortner.host}")
	private String urlShortnerHost;

	@Value("${url.shortner.path}")
	private String urlShortnerPath;
	
	@Value("${url.shortner.key}")
	private String urlShortnerKey;
	
	@Value("${ehrms.payslip.host}")
	private String PayslipHost;
	
	
	@Value("${egov.demand.create.endpoint}")
	private String demandCreateEndpoint;
	
	@Value("${egov.demand.update.endpoint}")
	private String demandUpdateEndpoint;
	
	@Value("${egov.billingservice.host}")
	private String billingHost;
	
	@Value("${egov.billingservice.fetchBillEndpoint}")
	private String fetchBillEndpoint;
	
	@Value("${egov.demand.minimum.payable.amount}")
	private BigDecimal minimumPayableAmount;
	
	
	
	@Value("${egov.demand.search.endpoint}")
	private String demandSearchEndpoint;
	
	@Value("${egov.service.search.endpoint}")
	private String billingSearchHost;
	
	@Value("${egov.payemnt.create.host}")
	private String collectionHost;
	
	@Value("${egov.service.payment.create.endpoint}")
	private String createPaymentEndpoint;
	
	@Value("${egov.service.opms.host}")
	private String OPMSHost;
	
	
	
	
}
