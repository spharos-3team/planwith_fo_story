package com.planwith.planwith_fo_story.adapter.out.persistence.story;

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
@Table(name = "story_tag")
class StoryTagJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_tag_id")
	private Long storyTagId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "story_id", nullable = false)
	private StoryJpaEntity story;

	@Column(name = "tag_name", nullable = false, length = 50)
	private String tagName;

	protected StoryTagJpaEntity() {
	}

	StoryTagJpaEntity(StoryJpaEntity story, String tagName) {
		this.story = story;
		this.tagName = tagName;
	}

	Long storyTagId() {
		return storyTagId;
	}

	String tagName() {
		return tagName;
	}
}
