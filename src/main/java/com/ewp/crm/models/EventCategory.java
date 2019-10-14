package com.ewp.crm.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "event_category")
public class EventCategory {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "event_category_id")
		private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}


//	public Set<Event> getEvents() {
//		return events;
//	}

	@Column(name = "event_name", nullable = false, unique = true)
		private String name;

	@OneToMany(mappedBy = "name", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Event> events ;

}
