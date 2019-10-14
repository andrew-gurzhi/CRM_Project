package com.ewp.crm.models;

import javax.persistence.*;

@Entity
@Table(name = "event")
public class Event {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Long id;

	@Column(name = "event_name", nullable = false, unique = true)
	private String name;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "event_category_id")
	private EventCategory eventCategoryid;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EventCategory getEventCategoryid() {
		return eventCategoryid;
	}

	public void setEventCategoryid(EventCategory eventCategoryid) {
		this.eventCategoryid = eventCategoryid;
	}


}
