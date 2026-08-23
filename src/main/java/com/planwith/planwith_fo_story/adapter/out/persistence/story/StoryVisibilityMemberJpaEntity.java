package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "story_visibility_member")
class StoryVisibilityMemberJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_visibility_member_id")
	private Long storyVisibilityMemberId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "story_id", nullable = false)
	private StoryJpaEntity story;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	protected StoryVisibilityMemberJpaEntity() {
	}

	StoryVisibilityMemberJpaEntity(StoryJpaEntity story, UUID memberUuid, LocalDateTime createdAt) {
		this.story = story;
		this.memberUuid = memberUuid;
		this.createdAt = createdAt;
	}

	Long storyVisibilityMemberId() {
		return storyVisibilityMemberId;
	}

	UUID memberUuid() {
		return memberUuid;
	}

	LocalDateTime createdAt() {
		return createdAt;
	}
}
