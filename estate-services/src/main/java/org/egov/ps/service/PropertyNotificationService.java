package org.egov.ps.service;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.ps.model.Notifications;
import org.egov.ps.model.NotificationsEmail;
import org.egov.ps.model.NotificationsEvent;
import org.egov.ps.model.NotificationsSms;
import org.egov.ps.model.Property;
import org.egov.ps.model.PropertyCriteria;
import org.egov.ps.model.UserDetailResponse;
import org.egov.ps.model.UserResponse;
import org.egov.ps.model.notification.uservevents.Event;
import org.egov.ps.model.notification.uservevents.EventRequest;
import org.egov.ps.repository.PropertyRepository;
import org.egov.ps.util.PSConstants;
import org.egov.ps.util.Util;
import org.egov.ps.web.contracts.EstateDemand;
import org.egov.ps.web.contracts.ExtensionFeeRequest;
import org.egov.ps.web.contracts.PropertyPenaltyRequest;
import org.egov.ps.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PropertyNotificationService {

	@Autowired
	private MDMSService mdmsservice;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	PropertyRepository repository;

	@Autowired
	Util util;

	@Autowired
	private PropertyService propertyService;

	@Autowired
	private UserService userService;

	@Autowired
	private ObjectMapper mapper;


	/**
	 * Invoke process notification on each application in the request
	 * @param estateDemand 
	 */
	public void processDemandNotification(PropertyRequest request, EstateDemand estateDemand) {
		request.getProperties().forEach(property -> {
			/**
			 * generate request info
			 */
			RequestInfo requestInfo = new RequestInfo();
			requestInfo.setMsgId("20170310130900|en_IN");
			/**
			 * Get the notification config from mdms.
			 */
			List<Map<String, Object>> notificationConfigs = mdmsservice.getNotificationConfig(
					PSConstants.PROPERTY_RENT_MDMS_MODULE, requestInfo, property.getTenantId());
			/**
			 * Enrichement of demand date
			 */
			Date date = new Date(estateDemand.getGenerationDate());
			DateFormat f = new SimpleDateFormat("MMM-yyyy");
			property.setDemandDate(f.format(date));

			/**
			 * Enrich amount
			 */
			property.setPaymentAmount(BigDecimal.valueOf(estateDemand.getCollectionPrincipal()));
			/**
			 * Process the notification config
			 */
			property.setNotificationCode(String.format("%s_%s", property.getPropertyDetails().getBranchType(),PSConstants.PROPERTY_RENT));
			property.getPropertyDetails().getOwners().stream().filter(owner->owner.getOwnerDetails().getIsCurrentOwner()).forEach(owner->{ 
				try {
					UserDetailResponse ownerDetail = userService.searchByUserName(owner.getOwnerDetails().getMobileNumber(),
							owner.getTenantId()); 
					ownerDetail.getUser().get(0).getUuid();
					/**
					 * converting into user object
					 */
					User notifyingOwner = mapper.convertValue(ownerDetail, UserResponse.class).getUser().get(0).toCommonUser();
					/**
					 * replacing owner name
					 */
					notifyingOwner.setName(owner.getOwnerDetails().getOwnerName());
					/**
					 * current notifying owner
					 */
					property.setNotifyingOwner(notifyingOwner);
					/**
					 * Enriching requestinfo
					 */
					requestInfo.setUserInfo(notifyingOwner);

					Notifications notification= filterNotification(notificationConfigs, property, requestInfo);
					if(notification!=null) {
						ObjectMapper mapper = new ObjectMapper();
						String applicationJsonString = mapper.writeValueAsString(property);
						processNotification(property,requestInfo,applicationJsonString,notification);
					}
				}  catch (Exception e) {
					log.error("Exception while fetching notification config for the property '{}' '{}'",
							property.getFileNumber(), e);
				}

			});
		});
	}

	public void processPenaltyNotification(PropertyPenaltyRequest propertyPenaltyRequest) {
		propertyPenaltyRequest.getPropertyPenalties().forEach(penalty->{
			/**
			 * Get the notification config from mdms.
			 */
			List<Map<String, Object>> notificationConfigs = mdmsservice.getNotificationConfig(
					PSConstants.PROPERTY_RENT_MDMS_MODULE, propertyPenaltyRequest.getRequestInfo(), penalty.getTenantId());

			/**
			 * get property owner details from DB
			 */
			PropertyCriteria searchCriteria = new PropertyCriteria();
			searchCriteria.setPropertyId(penalty.getProperty().getId());
			searchCriteria.setRelations(Collections.singletonList("owner"));
			searchCriteria.setLimit(1L);
			List<Property> properties = propertyService.searchProperty(searchCriteria, propertyPenaltyRequest.getRequestInfo());
			penalty.setProperty(properties.get(0));
			penalty.getProperty().setNotificationCode(String.format("%s_%s", penalty.getBranchType(),PSConstants.PENALTY));
			penalty.getProperty().getPropertyDetails().getOwners().stream().filter(owner->owner.getOwnerDetails().getIsCurrentOwner()).forEach(owner->{ 
				try {
					UserDetailResponse ownerDetail = userService.searchByUserName(owner.getOwnerDetails().getMobileNumber(),
							owner.getTenantId()); 
					ownerDetail.getUser().get(0).getUuid();
					User notifyingOwner = User.builder().name(owner.getOwnerDetails().getOwnerName())
							.mobileNumber(owner.getOwnerDetails().getMobileNumber())
							.emailId(ownerDetail.getUser().get(0).getEmailId())
							.uuid(ownerDetail.getUser().get(0).getUuid())
							.build();
					penalty.getProperty().setNotifyingOwner(notifyingOwner);

					Notifications notification= filterNotification(notificationConfigs, penalty.getProperty(), propertyPenaltyRequest.getRequestInfo());
					if(notification!=null) {
						ObjectMapper mapper = new ObjectMapper();
						String applicationJsonString = mapper.writeValueAsString(penalty);
						processNotification(penalty.getProperty(),propertyPenaltyRequest.getRequestInfo(),applicationJsonString,notification);
					}  
				}catch (Exception e) {
					log.error("Exception while fetching notification config for the property '{}' '{}'",
							penalty.getProperty().getFileNumber(), e);
				}
			});

		});
	}

	/**
	 * For Extension Fee
	 * @param extensionFeeRequest
	 */
	public void processExtensionFeeNotification(ExtensionFeeRequest extensionFeeRequest) {
		extensionFeeRequest.getExtensionFees().forEach(extensionFee->{
			/**
			 * Get the notification config from mdms.
			 */
			List<Map<String, Object>> notificationConfigs = mdmsservice.getNotificationConfig(
					PSConstants.PROPERTY_RENT_MDMS_MODULE, extensionFeeRequest.getRequestInfo(), extensionFee.getTenantId());

			/**
			 * get property owner details from DB
			 */
			PropertyCriteria searchCriteria = new PropertyCriteria();
			searchCriteria.setPropertyId(extensionFee.getProperty().getId());
			searchCriteria.setRelations(Collections.singletonList("owner"));
			searchCriteria.setLimit(1L);
			List<Property> properties = propertyService.searchProperty(searchCriteria, extensionFeeRequest.getRequestInfo());
			extensionFee.setProperty(properties.get(0));
			extensionFee.getProperty().setNotificationCode(String.format("%s_%s", extensionFee.getBranchType(),PSConstants.EXTENSION_FEE));
			extensionFee.getProperty().getPropertyDetails().getOwners().stream().filter(owner->owner.getOwnerDetails().getIsCurrentOwner()).forEach(owner->{ 
				try {
					UserDetailResponse ownerDetail = userService.searchByUserName(owner.getOwnerDetails().getMobileNumber(),
							owner.getTenantId()); 
					ownerDetail.getUser().get(0).getUuid();
					User notifyingOwner = User.builder().name(owner.getOwnerDetails().getOwnerName())
							.mobileNumber(owner.getOwnerDetails().getMobileNumber())
							.emailId(ownerDetail.getUser().get(0).getEmailId())
							.uuid(ownerDetail.getUser().get(0).getUuid())
							.build();
					extensionFee.getProperty().setNotifyingOwner(notifyingOwner);

					Notifications notification= filterNotification(notificationConfigs, extensionFee.getProperty(), extensionFeeRequest.getRequestInfo());
					if(notification!=null) {
						ObjectMapper mapper = new ObjectMapper();
						String applicationJsonString = mapper.writeValueAsString(extensionFee);
						processNotification(extensionFee.getProperty(),extensionFeeRequest.getRequestInfo(),applicationJsonString,notification);
					}
				}  catch (Exception e) {
					log.error("Exception while fetching notification config for the property '{}' '{}'",
							extensionFee.getProperty().getFileNumber(), e);
				}

			});
		});
	}

	public void processPaymentNotification(Property property,RequestInfo requestInfo, String paymentMode) {
		/**
		 * Get the notification config from mdms.
		 */
		List<Map<String, Object>> notificationConfigs = mdmsservice.getNotificationConfig(
				PSConstants.PROPERTY_RENT_MDMS_MODULE, requestInfo, property.getTenantId());

		/**
		 * Process the notification config
		 */
		if(paymentMode.equalsIgnoreCase(PSConstants.PAYMENT_MODE_ONLINE)) {
			try {
				Notifications notification= filterNotification(notificationConfigs, property, requestInfo);
				if(notification!=null) {
					ObjectMapper mapper = new ObjectMapper();
					String applicationJsonString = mapper.writeValueAsString(property);
					processNotification(property,requestInfo,applicationJsonString,notification);
				}
			}  catch (Exception e) {
				log.error("Exception while fetching notification config for the property '{}' '{}'",
						property.getFileNumber(), e);
			}
		}
		else {
			property.getPropertyDetails().getOwners().stream().filter(owner->owner.getOwnerDetails().getIsCurrentOwner()).forEach(owner->{ 
				try {
					UserDetailResponse ownerDetail = userService.searchByUserName(owner.getOwnerDetails().getMobileNumber(),
							owner.getTenantId()); 
					ownerDetail.getUser().get(0).getUuid();
					User notifyingOwner = User.builder().name(owner.getOwnerDetails().getOwnerName())
							.mobileNumber(owner.getOwnerDetails().getMobileNumber())
							.emailId(ownerDetail.getUser().get(0).getEmailId())
							.uuid(ownerDetail.getUser().get(0).getUuid())
							.build();
					property.setNotifyingOwner(notifyingOwner);

					Notifications notification= filterNotification(notificationConfigs, property, requestInfo);
					if(notification!=null) {
						ObjectMapper mapper = new ObjectMapper();
						String applicationJsonString = mapper.writeValueAsString(property);
						processNotification(property,requestInfo,applicationJsonString,notification);
					}
				}  catch (Exception e) {
					log.error("Exception while fetching notification config for the property '{}' '{}'",
							property.getFileNumber(), e);
				}

			});
		}
	}
	private void processNotification(Property property, RequestInfo requestInfo, String applicationJsonString, Notifications notification) {
		String contentWithPathsEnriched = enrichPathPatternsWithApplication(notification.getContent(),applicationJsonString);

		/**
		 * Send email
		 */
		NotificationsEmail emailConfig = notification.getModes().getEmail();
		if (emailConfig.isEnabled()) {
			if (emailConfig.isValid()) {
				String email = enrichPathPatternsWithApplication(emailConfig.getTo(), applicationJsonString);
				String subject = enrichPathPatternsWithApplication(emailConfig.getSubject(), applicationJsonString);
				this.notificationService.sendEmail(email, subject, contentWithPathsEnriched);
			} else {
				log.warn("Notifications Invalid email config found {}", emailConfig);
			}
		}

		/**
		 * Send SMS
		 */
		NotificationsSms smsConfig = notification.getModes().getSms();
		if (smsConfig.isEnabled()) {
			if (smsConfig.isValid()) {
				String mobileNumber = enrichPathPatternsWithApplication(smsConfig.getTo(), applicationJsonString);
				this.notificationService.sendSMS(mobileNumber, contentWithPathsEnriched);
			} else {
				log.warn("Notifications Invalid sms config found {}", smsConfig);
			}
		}
		/**
		 * Web notification
		 */
		NotificationsEvent eventConfig = notification.getModes().getEvent();
		if(eventConfig.isEnabled() && eventConfig.isValid()) {
			EventRequest eventRequest = getEventsForApplication(contentWithPathsEnriched,requestInfo,property,eventConfig,applicationJsonString);
			if(null != eventRequest)
				util.sendEventNotification(eventRequest);
		}
	}


	public Notifications filterNotification(List<Map<String, Object>> rawNotificationsList, Property property,
			RequestInfo requestInfo) {
		if (CollectionUtils.isEmpty(rawNotificationsList)) {
			log.debug("No notifications configured in MDMS for the file no {} for code {}",
					property.getFileNumber(),property.getNotificationCode());
			return null;
		}

		/**
		 * Deserialize config.
		 */
		ObjectMapper mapper = new ObjectMapper();
		List<Notifications> notificationList = mapper.convertValue(rawNotificationsList,
				new TypeReference<List<Notifications>>() {
		});

		/**
		 * Filter notification object relevant to code of the property.
		 */
		Optional<Notifications> notificationOptional = notificationList.stream()
				.filter(x -> x.getState().equalsIgnoreCase(property.getNotificationCode())).findAny();
		if (!notificationOptional.isPresent()) {
			log.debug("No notification configured for file no {} for code {}",
					property.getFileNumber(),property.getNotificationCode());
			return null;
		}
		return notificationOptional.get();
	}

	private String enrichPathPatternsWithApplication(String sourceString, String applicationJsonString) {
		Pattern p = Pattern.compile("\\{(.*?)\\}");
		Matcher m = p.matcher(sourceString);
		Set<String> allMatches = new HashSet<String>();
		while (m.find()) {
			allMatches.add(m.group());
		}

		String replacedString = allMatches.stream().reduce(sourceString, (result, match) -> {
			String path = match.substring(1, match.length() - 1);
			Object value = (JsonPath.read(applicationJsonString, path));
			return result.replaceAll(String.format("\\{%s\\}", path), "" + value);
		});
		log.debug("Enriched '{}' to '{}' ", sourceString, replacedString);
		return replacedString;
	}

	public EventRequest getEventsForApplication(String message,RequestInfo requestInfo, Property property, NotificationsEvent eventConfig, String applicationJsonString) {
		List<Event> events = new ArrayList<>();
		if(message == null) return null;
		String mobileNumber = enrichPathPatternsWithApplication(eventConfig.getTo(), applicationJsonString);
		String uuid = enrichPathPatternsWithApplication(eventConfig.getTo(), applicationJsonString);
		events = util.createEvent(message,mobileNumber,uuid,requestInfo,property.getTenantId(),property.getNotificationCode(),null,eventConfig.isPayLink());
		if(!CollectionUtils.isEmpty(events)) {
			return EventRequest.builder().requestInfo(requestInfo).events(events).build();
		}else {
			return null;
		}
	}

}