package org.egov.tl.web.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.validation.annotation.Validated;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A Object holds the basic data for a Trade License
 */
@ApiModel(description = "A Object holds the basic data for a Trade License")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-18T17:06:11.263+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeLicenseDetail   {

        @JsonProperty("id")
        @Size(max=64)
        private String id;

        @JsonProperty("surveyNo")
        @Size(max=64)
        private String surveyNo;

        @Size(max=64)
        @JsonProperty("subOwnerShipCategory")
        private String subOwnerShipCategory;

        @Size(max=64)
        @JsonProperty("structureType")
        private String structureType;

        @JsonProperty("operationalArea")
        private Double operationalArea;

        @JsonProperty("noOfEmployees")
        private Integer noOfEmployees;

        @JsonProperty("adhocExemption")
        private BigDecimal adhocExemption;

        @JsonProperty("adhocPenalty")
        private BigDecimal adhocPenalty;

        @Size(max=1024)
        @JsonProperty("adhocExemptionReason")
        private String adhocExemptionReason;

        @Size(max=1024)
        @JsonProperty("adhocPenaltyReason")
        private String adhocPenaltyReason;

        @NotNull
        @JsonProperty("owners")
        @Valid
        @Builder.Default
        private List<OwnerInfo> owners = new ArrayList<>();

              /**
   * License can be created from different channels
   */
  public enum ChannelEnum {
    COUNTER("COUNTER"),
    
    CITIZEN("CITIZEN"),
    
    DATAENTRY("DATAENTRY");

    private String value;

    ChannelEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ChannelEnum fromValue(String text) {
      for (ChannelEnum b : ChannelEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

        @JsonProperty("channel")
        private ChannelEnum channel;

        @NotNull
        @Valid
        @JsonProperty("address")
        private Address address;

        @NotNull
        @JsonProperty("tradeUnits")
        @Valid
        @Builder.Default
        private List<TradeUnit> tradeUnits = new ArrayList<>();

        @JsonProperty("accessories")
        @Valid
        private List<Accessory> accessories;

        @JsonProperty("applicationDocuments")
        @Valid
        private List<Document> applicationDocuments;

        @JsonProperty("verificationDocuments")
        @Valid
        private List<Document> verificationDocuments;

        @JsonProperty("additionalDetail")
        private JsonNode additionalDetail;

        @Valid
        @JsonProperty("institution")
        private Institution institution;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;


        public TradeLicenseDetail addOwnersItem(OwnerInfo ownersItem) {
            if(this.owners==null)
                this.owners = new ArrayList<>();
            if(!this.owners.contains(ownersItem))
                this.owners.add(ownersItem);
            return this;
        }

        public TradeLicenseDetail addTradeUnitsItem(TradeUnit tradeUnitsItem) {
            if(this.tradeUnits==null)
                this.tradeUnits = new ArrayList<>();
            if(!this.tradeUnits.contains(tradeUnitsItem))
                this.tradeUnits.add(tradeUnitsItem);
            return this;
        }

        public TradeLicenseDetail addAccessoriesItem(Accessory accessoriesItem) {
            if (this.accessories == null) {
            this.accessories = new ArrayList<>();
            }
            if(!this.accessories.contains(accessoriesItem))
                this.accessories.add(accessoriesItem);
            return this;
        }

        public TradeLicenseDetail addApplicationDocumentsItem(Document applicationDocumentsItem) {
            if (this.applicationDocuments == null) {
            this.applicationDocuments = new ArrayList<>();
            }
            if(!this.applicationDocuments.contains(applicationDocumentsItem))
                this.applicationDocuments.add(applicationDocumentsItem);
            return this;
        }

        public TradeLicenseDetail addVerificationDocumentsItem(Document verificationDocumentsItem) {
            if (this.verificationDocuments == null) {
            this.verificationDocuments = new ArrayList<>();
            }
            if(!this.verificationDocuments.contains(verificationDocumentsItem))
                this.verificationDocuments.add(verificationDocumentsItem);
            return this;
        }

}

