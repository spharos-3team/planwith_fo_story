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
@Table(name = "story_visit_country")
class StoryVisitCountryJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_visit_country_id")
	private Long storyVisitCountryId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "story_id", nullable = false)
	private StoryJpaEntity story;

	@Column(name = "country_name", nullable = false, length = 100)
	private String countryName;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoryVisitCityJpaEntity> cities = new ArrayList<>();

	protected StoryVisitCountryJpaEntity() {
	}

	StoryVisitCountryJpaEntity(StoryJpaEntity story, String countryName, int displayOrder) {
		this.story = story;
		this.countryName = countryName;
		this.displayOrder = displayOrder;
	}

	Long storyVisitCountryId() {
		return storyVisitCountryId;
	}

	String countryName() {
		return countryName;
	}

	int displayOrder() {
		return displayOrder;
	}

	List<StoryVisitCityJpaEntity> cities() {
		return cities;
	}

	void replaceCities(List<StoryVisitCityJpaEntity> next) {
		this.cities.clear();
		this.cities.addAll(next);
	}
}
