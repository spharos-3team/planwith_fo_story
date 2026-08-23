package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "story_place_image")
class StoryPlaceImageJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_place_image_id")
	private Long storyPlaceImageId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "story_place_id", nullable = false)
	private StoryPlaceJpaEntity place;

	@Column(name = "image_url", nullable = false, length = 500)
	private String imageUrl;

	@Column(name = "image_order", nullable = false)
	private int imageOrder;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected StoryPlaceImageJpaEntity() {
	}

	StoryPlaceImageJpaEntity(StoryPlaceJpaEntity place, String imageUrl, int imageOrder, LocalDateTime createdAt) {
		this.place = place;
		this.imageUrl = imageUrl;
		this.imageOrder = imageOrder;
		this.createdAt = createdAt;
	}

	Long storyPlaceImageId() {
		return storyPlaceImageId;
	}

	String imageUrl() {
		return imageUrl;
	}

	int imageOrder() {
		return imageOrder;
	}

	LocalDateTime createdAt() {
		return createdAt;
	}
}
