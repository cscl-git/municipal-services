package org.egov.integration.service;

import java.util.List;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.integration.common.CommonConstants;
import org.egov.integration.config.PtConfiguration;
import org.egov.integration.model.Payment;
import org.egov.integration.model.PaymentInfo;
import org.egov.integration.model.PaymentsRequest;
import org.egov.integration.model.ResponseInfoWrapper;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FinanceService {
	@Autowired
	private RequestFactory requestFactory;

	private PtConfiguration config;

	@Autowired
	private DemandService demandService;
	
	

	@Autowired
	public FinanceService(PtConfiguration config) {
		this.config = config;
	}	
	
	public ResponseEntity<ResponseInfoWrapper> generate(PaymentsRequest req) {
		try {		
		 List<Payment> payment=demandService.generate(req);
			return new ResponseEntity<>(ResponseInfoWrapper.builder()
					.responseInfo(ResponseInfo.builder().status(CommonConstants.SUCCESS).build()).responseBody(payment)
					.build(), HttpStatus.OK);
		} catch (HttpClientErrorException e) {
			throw new CustomException(CommonConstants.FINANCE_EXCEPTION_CODE, e.getResponseBodyAsString());
		}
	}

	

	
}
