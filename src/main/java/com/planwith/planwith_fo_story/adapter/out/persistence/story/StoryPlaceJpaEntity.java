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
@Table(name = "story_place")
class StoryPlaceJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_place_id")
	private Long storyPlaceId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "story_id", nullable = false)
	private StoryJpaEntity story;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "story_visit_city_id")
	private StoryVisitCityJpaEntity city;

	@Column(name = "place_name", nullable = false, length = 255)
	private String placeName;

	@Column(name = "display_order", nullable = false)
	private int displayOrder;

	@OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoryPlaceImageJpaEntity> images = new ArrayList<>();

	protected StoryPlaceJpaEntity() {
	}

	StoryPlaceJpaEntity(StoryJpaEntity story, StoryVisitCityJpaEntity city, String placeName, int displayOrder) {
		this.story = story;
		this.city = city;
		this.placeName = placeName;
		this.displayOrder = displayOrder;
	}

	Long storyPlaceId() {
		return storyPlaceId;
	}

	Long storyVisitCityId() {
		return city == null ? null : city.storyVisitCityId();
	}

	String placeName() {
		return placeName;
	}

	int displayOrder() {
		return displayOrder;
	}

	List<StoryPlaceImageJpaEntity> images() {
		return images;
	}

	void replaceImages(List<StoryPlaceImageJpaEntity> next) {
		this.images.clear();
		this.images.addAll(next);
	}
}
