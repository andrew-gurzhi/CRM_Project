package com.ewp.crm.controllers;

import com.ewp.crm.configs.ImageConfig;
import com.ewp.crm.models.User;
import com.ewp.crm.models.dto.GoogleUserDTO;
import com.ewp.crm.service.google.GoogleOAuthService;
import com.ewp.crm.service.interfaces.NotificationService;
import com.ewp.crm.service.interfaces.RoleService;
import com.ewp.crm.service.interfaces.TelegramService;
import com.ewp.crm.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@PropertySource("file:./slackbot.properties")
public class UserController {

	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	private final UserService userService;
	private final RoleService roleService;
	private final ImageConfig imageConfig;
	private final NotificationService notificationService;
	private final TelegramService telegramService;
	private GoogleOAuthService googleOAuthService;

	@Autowired
	public UserController(UserService userService,
						  RoleService roleService,
						  ImageConfig imageConfig,
						  NotificationService notificationService,
						  TelegramService telegramService, GoogleOAuthService googleOAuthService) {
		this.userService = userService;
		this.roleService = roleService;
		this.imageConfig = imageConfig;
		this.notificationService = notificationService;
		this.telegramService = telegramService;
		this.googleOAuthService = googleOAuthService;
	}

	@Value("${slackbot.domain}")
	private String slackBotDomain;


	@GetMapping(value = "/admin/user/{id}")
	@PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'HR', 'MENTOR')")
	public ModelAndView clientInfo(@PathVariable Long id,
								   @AuthenticationPrincipal User userFromSession) {
		ModelAndView modelAndView = new ModelAndView("user-info");
		User userFromDB = userService.get(id);
		userFromDB.setPassword("");
		modelAndView.addObject("user", userFromDB);
		modelAndView.addObject("roles", roleService.getAll());
		modelAndView.addObject("maxSize", imageConfig.getMaxImageSize());
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));
		modelAndView.addObject("slackBotDomain", slackBotDomain);
		return modelAndView;
	}

	@GetMapping(value = "/admin/user/add")
	@PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'HR')")
	public ModelAndView addUser(@AuthenticationPrincipal User userFromSession) {
		ModelAndView modelAndView = new ModelAndView("add-user");
		modelAndView.addObject("roles", roleService.getAll());
		modelAndView.addObject("maxSize", imageConfig.getMaxImageSize());
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));
		modelAndView.addObject("slackBotDomain", slackBotDomain);
		return modelAndView;
	}

	@GetMapping(value = "/user/register")
	public ModelAndView registerUser() {
		ModelAndView modelAndView = new ModelAndView("user-registration");
		modelAndView.addObject("maxSize", imageConfig.getMaxImageSize());
		modelAndView.addObject("GoogleAuthorizationUrl",googleOAuthService.oAuth20Service(true).getAuthorizationUrl());
		return modelAndView;
	}

	@GetMapping(value = "/user/customize")
	@PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN', 'USER', 'HR', 'MENTOR')")
	public ModelAndView getUserCustomize(@AuthenticationPrincipal User userFromSession) {
		ModelAndView modelAndView = new ModelAndView("user-customize");
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));
		modelAndView.addObject("userCustomize", userService.get(userFromSession.getId()));
		modelAndView.addObject("isTelegramAuthenticated", telegramService.isAuthenticated());
		modelAndView.addObject("isTdlibInstalled", telegramService.isTdlibInstalled());
		return modelAndView;
	}

	@PostMapping(value = "/user/autoAnswer")
	@PreAuthorize("hasAnyAuthority('OWNER', 'HR')")
	public ModelAndView changeAutoAnswer(@RequestParam String text,
											@AuthenticationPrincipal User userFromSession) {
	    userFromSession.setAutoAnswer(text);
		userService.update(userFromSession);
		return new ModelAndView("redirect:/user/customize");
	}
	@GetMapping(value = "/user/autoAnswer")
	@PreAuthorize("hasAnyAuthority('OWNER', 'HR')")
	public ModelAndView getAutoAnswerView(@AuthenticationPrincipal User userFromSession) {
		ModelAndView modelAndView = new ModelAndView("user-autoanswer");
		modelAndView.addObject("userCustomize",userFromSession);
		return modelAndView;
	}

	//  Register google account wia  OAuth2
	@GetMapping(value = {"/googleoauth2/register"})
	public String ouath2RegisterUser(
			@RequestParam(required = false) String code,
			Map<String, Object> model) throws InterruptedException, ExecutionException, IOException {

		User user = googleOAuthService.getGoogleUserDTO(code);
		if (user != null) {
			return "redirect:/1";
		} else {
			return "redirect:/login";
		}

	}

}
