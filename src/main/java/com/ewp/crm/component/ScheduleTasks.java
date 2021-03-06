package com.ewp.crm.component;

import com.ewp.crm.exceptions.member.NotFoundMemberList;
import com.ewp.crm.exceptions.parse.ParseClientException;
import com.ewp.crm.exceptions.util.FBAccessTokenException;
import com.ewp.crm.exceptions.util.VKAccessTokenException;
import com.ewp.crm.models.Client;
import com.ewp.crm.models.ClientHistory;
import com.ewp.crm.models.MessageTemplate;
import com.ewp.crm.models.Notification;
import com.ewp.crm.models.PotentialClient;
import com.ewp.crm.models.ProjectProperties;
import com.ewp.crm.models.SMSInfo;
import com.ewp.crm.models.SocialProfile;
import com.ewp.crm.models.SocialProfile.SocialNetworkType;
import com.ewp.crm.models.Student;
import com.ewp.crm.models.User;
import com.ewp.crm.service.email.MailingService;
import com.ewp.crm.service.interfaces.ClientHistoryService;
import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.FacebookService;
import com.ewp.crm.service.interfaces.MailSendService;
import com.ewp.crm.service.interfaces.PotentialClientService;
import com.ewp.crm.service.interfaces.ProjectPropertiesService;
import com.ewp.crm.service.interfaces.ReportService;
import com.ewp.crm.service.interfaces.SMSInfoService;
import com.ewp.crm.service.interfaces.SMSService;
import com.ewp.crm.service.interfaces.SendNotificationService;
import com.ewp.crm.service.interfaces.SlackService;
import com.ewp.crm.service.interfaces.SocialProfileService;
import com.ewp.crm.service.interfaces.StatusService;
import com.ewp.crm.service.interfaces.StudentService;
import com.ewp.crm.service.interfaces.TelegramService;
import com.ewp.crm.service.interfaces.UserService;
import com.ewp.crm.service.interfaces.VKService;
import com.ewp.crm.service.interfaces.VkMemberService;
import com.ewp.crm.service.interfaces.vkcampaigns.VkCampaignService;
import com.ewp.crm.util.patterns.ValidationPattern;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
@PropertySource(value = "file:./skype-message.properties", encoding = "Cp1251")
@PropertySource(value = "file:./advertisement-report.properties", encoding = "UTF-8")
public class ScheduleTasks {
	private final ProjectProperties projectProperties;

    private final VKService vkService;

	private final PotentialClientService potentialClientService;

	private final ClientService clientService;

	private final StudentService studentService;

	private final StatusService statusService;

	private final SocialProfileService socialProfileService;

	private final SMSService smsService;

	private final SMSInfoService smsInfoService;

	private final MailSendService mailSendService;

	private final SendNotificationService sendNotificationService;

	private final ClientHistoryService clientHistoryService;

	private final FacebookService facebookService;

	private final VkMemberService vkMemberService;

	private final ReportService reportService;

	private final ProjectPropertiesService projectPropertiesService;

	private Environment env;

	private final MailingService mailingService;

	private final VkCampaignService vkCampaignService;

    private final TelegramService telegramService;

    private final SlackService slackService;

	private static Logger logger = LoggerFactory.getLogger(ScheduleTasks.class);

	private String adReportTemplate;

	private final UserService userService;
	private final boolean autoSetUser = false;

	@Autowired
	public ScheduleTasks(VKService vkService, PotentialClientService potentialClientService,
						 ClientService clientService, StudentService studentService,
						 StatusService statusService, ProjectPropertiesService projectPropertiesService,
						 MailingService mailingService, SocialProfileService socialProfileService, SMSService smsService,
						 SMSInfoService smsInfoService, SendNotificationService sendNotificationService,
						 ClientHistoryService clientHistoryService,
						 VkMemberService vkMemberService, FacebookService facebookService,
						 MailSendService mailSendService, Environment env, ReportService reportService,
						 VkCampaignService vkCampaignService, TelegramService telegramService,
						 SlackService slackService, UserService userService) {
		this.vkService = vkService;
		this.potentialClientService = potentialClientService;
		this.clientService = clientService;
		this.studentService = studentService;
		this.statusService = statusService;
		this.socialProfileService = socialProfileService;
		this.smsService = smsService;
		this.smsInfoService = smsInfoService;
		this.mailSendService = mailSendService;
		this.sendNotificationService = sendNotificationService;
		this.clientHistoryService = clientHistoryService;
		this.facebookService = facebookService;
		this.vkMemberService = vkMemberService;
	    this.reportService = reportService;
		this.env = env;
		this.mailingService = mailingService;
		this.projectPropertiesService = projectPropertiesService;
		this.vkCampaignService = vkCampaignService;
		this.adReportTemplate = env.getProperty("template.daily.report");
		this.telegramService = telegramService;
		this.slackService = slackService;
		this.projectProperties = projectPropertiesService.getOrCreate();
		this.userService = userService;
	}

	private void addClientFromVk(Client newClient) {
		statusService.getFirstStatusForClient().ifPresent(newClient::setStatus);
		newClient.setState(Client.State.NEW);
		if (!newClient.getSocialProfiles().isEmpty()) {
			newClient.getSocialProfiles().get(0).setSocialNetworkType(SocialNetworkType.VK);
		}
		clientHistoryService.createHistory("vk").ifPresent(newClient::addHistory);
		vkService.fillClientFromProfileVK(newClient);
		Optional<String> optionalEmail = newClient.getEmail();
		if (optionalEmail.isPresent() && !optionalEmail.get().matches(ValidationPattern.EMAIL_PATTERN)) {
			newClient.setClientDescriptionComment(newClient.getClientDescriptionComment() + System.lineSeparator() + env.getProperty("messaging.client.email.error-in-field") + optionalEmail.get());
		}

	   	if (autoSetUser) {
		   userService.getUserToOwnCard().ifPresent(newClient::setOwnerUser);
	   	}
		clientService.addClient(newClient, null);
		logger.info("New client with id {} has added from VK", newClient.getId());
	}

    @Scheduled(cron = "0 0 7 * * *")
    private void sendBirthdayMails() {
        Optional<MessageTemplate> messageTemplateBirthDay = Optional.ofNullable(projectProperties.getBirthDayMessageTemplate());
        if (messageTemplateBirthDay.isPresent()) {
            String messageBirthDay = messageTemplateBirthDay.get().getOtherText();
            LocalDate today = LocalDate.now();
            int dayOfMonthToday = today.getDayOfMonth();
            int monthToday = today.getMonthValue();

            List<Client> clients = clientService.getAll();
            for (Client currentClient : clients) {
                LocalDate birthDate = currentClient.getBirthDate();
                int clientDayOfBirth = birthDate.getDayOfMonth();
                int monthOfBirth = birthDate.getMonthValue();

                if ((dayOfMonthToday == clientDayOfBirth) && (monthToday == monthOfBirth)) {
                    if (currentClient.getEmail().isPresent() && !currentClient.getEmail().get().isEmpty()) {
                        mailSendService.sendSimpleNotification(currentClient.getId(), messageBirthDay);
                    }
                }
            }
        }
    }


	@Scheduled(fixedRate = 5_000)
	private void handleRequestsFromVk() {
		if (vkService.hasTechnicalAccountToken()) {
			try {
				Optional<List<String>> newMassages = vkService.getNewMassages();
				if (newMassages.isPresent()) {
					for (String message : newMassages.get()) {
						try {
							Client newClient = vkService.parseClientFromMessage(message);
							String s = newMassages.orElse(Collections.emptyList()).toString().replaceAll("<br><br>","<br>");
							ClientHistory clientHistory = new ClientHistory(s,ZonedDateTime.now(ZoneId.systemDefault()),ClientHistory.Type.SOCIAL_REQUEST);
							newClient.addHistory(clientHistory);
							addClientFromVk(newClient);
						} catch (ParseClientException e) {
							logger.error(e.getMessage());
						}
					}
				}
			} catch (VKAccessTokenException ex) {
				logger.error(ex.getMessage());
			}
		}
	}

	@Scheduled(fixedRate = 5_000)
	private void handleRequestsFromVkCommunityMessages() {
		Optional<List<Long>> newUsers = vkService.getUsersIdFromCommunityMessages();
		if (newUsers.isPresent()) {
			for (Long id : newUsers.get()) {
				Optional<Client> newClient = vkService.getClientFromVkId(id);
				if (newClient.isPresent()) {
					SocialProfile socialProfile = newClient.get().getSocialProfiles().get(0);
					if (!(socialProfileService.getSocialProfileBySocialIdAndSocialType(socialProfile.getSocialId(), "vk").isPresent())) {
						addClientFromVk(newClient.get());
					}
				}
			}
		}
	}

	@Scheduled(fixedRate = 6_000)
	private void checkClientActivationDate() {
		for (Client client : clientService.getChangeActiveClients()) {
			client.setPostponeDate(null);
			client.setHideCard(false);
			sendNotificationService.sendNotificationType(client.getClientDescriptionComment(), client, client.getOwnerUser(), Notification.Type.POSTPONE);
			clientService.updateClient(client);
		}
	}

	@Scheduled(fixedDelay = 6_000)
	private void sendMailing() {
        mailingService.sendMessages();
	}

	@Scheduled(cron = "0 0 * * * *")
	private void getSlackProfiles() {
		slackService.tryLinkSlackAccountToAllStudents();
	}

	@Scheduled(fixedRate = 600_000)
	private void addFacebookMessageToDatabase() {
		try {
			facebookService.getFacebookMessages();
		} catch (FBAccessTokenException e) {
			logger.error("Facebook access token has not got", e);
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	private void sendDailyAdvertisementReport() {
		logger.debug("Start filling ad report");
        LocalDate date = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		String formattedDate = date.format(formatter);
		logger.debug("Ad report for {}", formattedDate);
		adReportTemplate = adReportTemplate.replace("{date}", formattedDate);
        vkService.sendDailyAdvertisementReport(adReportTemplate);
		logger.debug("Finish filling ad report");
	}

	@Scheduled(cron = "0 0 10 01 * ?")
	private void buildAndSendReport() {
		// ToDo рассылка отчета
	}

	@Scheduled(fixedRate = 600_000)
	private void checkSMSMessages() {
		logger.info("start checking sms statuses");
		List<SMSInfo> queueSMS = smsInfoService.getSMSByIsChecked(false);
		for (SMSInfo sms : queueSMS) {
			Optional<String> status = smsService.getStatusMessage(sms.getSmsId());
			if (status.isPresent()) {
				if (!status.get().equals("queued")) {
					if (status.get().equals("delivered")) {
						sms.setDeliveryStatus(env.getProperty("messaging.client.phone.sms.delivered"));
					} else if (sms.getClient() == null) {
						logger.error("Can not create notification with empty SMS client, SMS message: {}", sms);
						sms.setDeliveryStatus(env.getProperty("messaging.client.phone.sms.status-not-found"));
					} else {
						String deliveryStatus = determineStatusOfResponse(status.get());
						sendNotificationService.sendNotificationType(deliveryStatus, sms.getClient(), sms.getUser(), Notification.Type.SMS);
						sms.setDeliveryStatus(deliveryStatus);
					}
					sms.setChecked(true);
					smsInfoService.update(sms);
				}
			}
		}
	}

	private String determineStatusOfResponse(String status) {
		String info;
		switch (status) {
			case "delivery error":
				info = env.getProperty("messaging.client.phone.calls.delivery-error");
				break;
			case "invalid mobile phone":
				info = env.getProperty("messaging.client.phone.calls.invalid-mobile-phone");
				break;
			case "incorrect id":
				info = env.getProperty("messaging.client.phone.calls.incorrect-id");
				break;
			default:
				info = env.getProperty("messaging.client.phone.calls.unknown-error");
		}
		return info;
	}

	/**
	 * Sends payment notification to student's contacts.
	 */
	@Scheduled(fixedDelay = 3600000)
	private void sendPaymentNotifications() {
		ProjectProperties properties = projectPropertiesService.getOrCreate();
		if (properties.isPaymentNotificationEnabled() && properties.getPaymentMessageTemplate() != null && properties.getPaymentNotificationTime() != null) {
			LocalTime time = properties.getPaymentNotificationTime().truncatedTo(ChronoUnit.HOURS);
			LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.HOURS);
			if (properties.isPaymentNotificationEnabled() && now.equals(time)) {
				for (Student student : studentService.getStudentsWithTodayNotificationsEnabled()) {
					if (student.getLastPaymentNotification() == null ||
							ChronoUnit.DAYS.between(student.getLastPaymentNotification(), LocalDateTime.now()) > 1) {
						MessageTemplate template = properties.getPaymentMessageTemplate();
						Long clientId = student.getClient().getId();
						if (student.isNotifyEmail()) {
							mailSendService.sendSimpleNotification(clientId, template.getTemplateText());
						}
						if (student.isNotifySMS()) {
							smsService.sendSimpleSMS(clientId, template.getOtherText());
						}
						if (student.isNotifyVK()) {
							vkService.simpleVKNotification(clientId, template.getOtherText());
						}
						if (student.isNotifySlack()) {
							slackService.trySendSlackMessageToStudent(clientId, template.getOtherText());
						}
						student.setLastPaymentNotification(LocalDateTime.now());
						studentService.update(student);
					} else {
						logger.info("Payment notification is already sent today to student with id {}", student.getId());
					}
				}
			}
		} else {
			logger.info("Payment notification properties not set!");
		}
	}

    /**
     * Sends end-of-trial notification to student's contacts.
     */
    @Scheduled(fixedDelay = 3600000)
    private void sendTrialNotifications() {
        ProjectProperties properties = projectPropertiesService.getOrCreate();
        if (properties.isTrialNotificationEnabled() && properties.getTrialMessageTemplate() != null && properties.getTrialNotificationTime() != null) {
            LocalTime time = properties.getTrialNotificationTime().truncatedTo(ChronoUnit.HOURS);
            LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.HOURS);
            if (properties.isTrialNotificationEnabled() && now.equals(time)) {
				logger.info("start end-of-trial notification");
                for (Student student : studentService.getStudentsWithTodayTrialNotificationsEnabled()) {
					MessageTemplate template = properties.getTrialMessageTemplate();
					Long clientId = student.getClient().getId();
					if (student.isNotifyEmail()) {
						mailSendService.sendSimpleNotification(clientId, template.getTemplateText());
					}
					if (student.isNotifySMS()) {
						smsService.sendSimpleSMS(clientId, template.getOtherText());
					}
					if (student.isNotifyVK()) {
						vkService.simpleVKNotification(clientId, template.getOtherText());
					}
					if (student.isNotifySlack()) {
						slackService.trySendSlackMessageToStudent(clientId, template.getOtherText());
					}
                }
            }
        } else {
            logger.info("End-of-trial notification properties not set!");
        }
    }

    @Scheduled(fixedRate = 60_000)
    private void fetchTelegramMessages() {
        if (telegramService.isTdlibInstalled() && telegramService.isAuthenticated()) {
            TdApi.Chats chats = telegramService.getChats();
            for (int i = 0; i < chats.chatIds.length; i++) {
                telegramService.getUnreadMessagesFromChat(chats.chatIds[i], 1);
            }
        }
    }

	/**
	 * Sends friend requests from all VK friends campaigns
	 * once per 36 minutes == 40 per day (VK day limit) 2_160_000
	 */
	@Scheduled(fixedRate = 2_160_000)
	private void addOneFriendForEachCampaign() {
		logger.info("Scheduled task to add next VK friend for all campaigns been fired");
		vkCampaignService.nextAttemptCycle();
	}
}