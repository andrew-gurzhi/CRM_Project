package com.ewp.crm.controllers;

import com.ewp.crm.service.interfaces.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@PreAuthorize("hasAnyAuthority('ADMIN', 'USER', 'OWNER', 'HR', 'MENTOR')")
@RequestMapping(value = "/events")
public class EventController {
	private static EventService eventService;
	@Autowired
	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@GetMapping(value = "")
	public ModelAndView addCourse() {
		ModelAndView modelAndView = new ModelAndView("events");
		return modelAndView;
	}
}
