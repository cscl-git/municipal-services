package org.egov.integration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Columns {

	@JsonProperty("ColumnMaps")
	private ColumnsMaps columnMaps;

}
