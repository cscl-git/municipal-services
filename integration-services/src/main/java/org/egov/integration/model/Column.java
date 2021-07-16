package org.egov.integration.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Column {
	@JsonProperty("applicationType")
	private String applicationType;
	
	@JsonProperty("parameter1")
	private String parameter1;
	
	@JsonProperty("parameter1Format")
	private String parameter1Format;
	
	@JsonProperty("parameter2")
	private String parametr2;
	
	@JsonProperty("parameter2Format")
	private String parameter2Format;

	@JsonProperty("endPoint")
	private String endPoint;
}
