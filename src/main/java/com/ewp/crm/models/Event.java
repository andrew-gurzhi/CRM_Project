package com.ewp.crm.models;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long id;

	@Column(name = "event_name", nullable = false, unique = true)
	private String name;

	@Column(name = "event_category", nullable = false, unique = true)
	private String category;
}
