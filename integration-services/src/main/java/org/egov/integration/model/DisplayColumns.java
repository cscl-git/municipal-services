package org.egov.integration.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class DisplayColumns {

	private String tenantId;
	private String parameter1;
	private String parameter1Format;
	private String parameter2;
	private String parameter2Format;
	private String endPoint;
	private String applicationType;

	public int hashCode() {
		int hashcode = 99;
		hashcode += tenantId.hashCode() + endPoint.hashCode() + applicationType.hashCode()+ parameter1.hashCode()+ parameter1Format.hashCode()
		+ parameter2.hashCode() + parameter2Format.hashCode();
		return hashcode;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DisplayColumns) {
			DisplayColumns pp = (DisplayColumns) obj;
			return (pp.tenantId.equals(this.tenantId) && pp.parameter1 == this.parameter1 && pp.endPoint == this.endPoint
					&& pp.applicationType == this.applicationType 
					&& pp.parameter1Format == this.parameter1Format && pp.parameter2 == this.parameter2 && pp.parameter2Format == this.parameter2Format);
		} else {
			return false;
		}
	}

}
