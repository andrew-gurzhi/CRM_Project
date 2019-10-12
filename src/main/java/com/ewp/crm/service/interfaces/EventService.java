package com.ewp.crm.service.interfaces;

import com.ewp.crm.models.Event;

import java.util.List;

public interface EventService {
	Event add(Event event);

	List<Event> getAllEvents();
}
