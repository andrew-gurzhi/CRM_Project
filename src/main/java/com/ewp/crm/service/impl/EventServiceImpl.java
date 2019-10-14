package com.ewp.crm.service.impl;

import com.ewp.crm.models.Event;
import com.ewp.crm.repository.interfaces.EventRepository;
import com.ewp.crm.service.interfaces.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl extends CommonServiceImpl<Event> implements EventService {

private final EventRepository eventRepository;

	@Autowired
	public EventServiceImpl(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	@Override
	public Event add(Event event) {

		return eventRepository.saveAndFlush(event);
	}

	@Override
	public List<Event> getAllEvents() {
		return eventRepository.findAll();
	}
}
