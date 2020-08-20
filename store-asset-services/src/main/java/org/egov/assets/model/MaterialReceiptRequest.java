package org.egov.assets.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Hold the Material Receipt Note request information.
 */

public class MaterialReceiptRequest {
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo = null;

	@JsonProperty("materialReceipt")
	private List<MaterialReceipt> materialReceipt = new ArrayList<MaterialReceipt>();

	@JsonProperty("workFlowDetails")
	private WorkFlowDetails workFlowDetails;

	public MaterialReceiptRequest workFlowDetails(WorkFlowDetails workFlowDetails) {
		this.workFlowDetails = workFlowDetails;
		return this;
	}

	public WorkFlowDetails getWorkFlowDetails() {
		return workFlowDetails;
	}

	public void setWorkFlowDetails(WorkFlowDetails workFlowDetails) {
		this.workFlowDetails = workFlowDetails;
	}

	public MaterialReceiptRequest requestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
		return this;
	}

	/**
	 * Get requestInfo
	 * 
	 * @return requestInfo
	 **/

	@NotNull

	@Valid

	public RequestInfo getRequestInfo() {
		return requestInfo;
	}

	public void setRequestInfo(RequestInfo requestInfo) {
		this.requestInfo = requestInfo;
	}

	public MaterialReceiptRequest materialReceipt(List<MaterialReceipt> materialReceipt) {
		this.materialReceipt = materialReceipt;
		return this;
	}

	public MaterialReceiptRequest addMaterialReceiptItem(MaterialReceipt materialReceiptItem) {
		this.materialReceipt.add(materialReceiptItem);
		return this;
	}

	/**
	 * Get materialReceipt
	 * 
	 * @return materialReceipt
	 **/

	@NotNull

	public List<MaterialReceipt> getMaterialReceipt() {
		return materialReceipt;
	}

	public void setMaterialReceipt(List<MaterialReceipt> materialReceipt) {
		this.materialReceipt = materialReceipt;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MaterialReceiptRequest materialReceiptRequest = (MaterialReceiptRequest) o;
		return Objects.equals(this.requestInfo, materialReceiptRequest.requestInfo)
				&& Objects.equals(this.materialReceipt, materialReceiptRequest.materialReceipt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestInfo, materialReceipt);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class MaterialReceiptRequest {\n");

		sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
		sb.append("    materialReceipt: ").append(toIndentedString(materialReceipt)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
