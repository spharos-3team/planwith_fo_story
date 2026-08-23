package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "story_visit_city")
class StoryVisitCityJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_visit_city_id")
	private Long storyVisitCityId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "story_visit_country_id", nullable = false)
	private StoryVisitCountryJpaEntity country;

	@Column(name = "city_name", nullable = false, length = 100)
	private String cityName;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoryPlaceJpaEntity> places = new ArrayList<>();

	protected StoryVisitCityJpaEntity() {
	}

	StoryVisitCityJpaEntity(StoryVisitCountryJpaEntity country, String cityName, int displayOrder) {
		this.country = country;
		this.cityName = cityName;
		this.displayOrder = displayOrder;
	}

	Long storyVisitCityId() {
		return storyVisitCityId;
	}

	String cityName() {
		return cityName;
	}

	int displayOrder() {
		return displayOrder;
	}

	List<StoryPlaceJpaEntity> places() {
		return places;
	}

	void replacePlaces(List<StoryPlaceJpaEntity> next) {
		this.places.clear();
		this.places.addAll(next);
	}
}
