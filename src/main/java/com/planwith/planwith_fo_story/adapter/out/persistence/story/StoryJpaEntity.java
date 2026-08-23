package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "story")
class StoryJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "story_id")
	private Long storyId;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "story_uuid", nullable = false, unique = true, length = 36)
	private UUID storyUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", nullable = false, length = 36)
	private UUID memberUuid;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "schedule_uuid", length = 36)
	private UUID scheduleUuid;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Lob
	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "cover_image_url", length = 500)
	private String coverImageUrl;

	@Column(name = "visit_country", length = 100)
	private String visitCountry;

	@Column(name = "visit_city", length = 100)
	private String visitCity;

	@Column(name = "visit_place", length = 255)
	private String visitPlace;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "comment_enabled", nullable = false)
	private boolean commentEnabled;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility_scope", nullable = false, length = 20)
	private VisibilityScope visibilityScope;

	@Enumerated(EnumType.STRING)
	@Column(name = "ai_moderation_status", nullable = false, length = 20)
	private AiModerationStatus aiModerationStatus;

	@Column(name = "story_like_count", nullable = false)
	private long storyLikeCount;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	protected StoryJpaEntity() {
	}

	Long storyId() {
		return storyId;
	}

	UUID storyUuid() {
		return storyUuid;
	}

	UUID memberUuid() {
		return memberUuid;
	}

	UUID scheduleUuid() {
		return scheduleUuid;
	}

	String title() {
		return title;
	}

	String content() {
		return content;
	}

	String coverImageUrl() {
		return coverImageUrl;
	}

	String visitCountry() {
		return visitCountry;
	}

	String visitCity() {
		return visitCity;
	}

	String visitPlace() {
		return visitPlace;
	}

	LocalDate startDate() {
		return startDate;
	}

	LocalDate endDate() {
		return endDate;
	}

	boolean commentEnabled() {
		return commentEnabled;
	}

	VisibilityScope visibilityScope() {
		return visibilityScope;
	}

	AiModerationStatus aiModerationStatus() {
		return aiModerationStatus;
	}

	long storyLikeCount() {
		return storyLikeCount;
	}

	LocalDateTime createdAt() {
		return createdAt;
	}

	LocalDateTime deletedAt() {
		return deletedAt;
	}

	void assignIdentity(UUID storyUuid, UUID memberUuid, LocalDateTime createdAt) {
		this.storyUuid = storyUuid;
		this.memberUuid = memberUuid;
		this.createdAt = createdAt;
	}

	void apply(
			UUID scheduleUuid,
			String title,
			String content,
			String coverImageUrl,
			String visitCountry,
			String visitCity,
			String visitPlace,
			LocalDate startDate,
			LocalDate endDate,
			boolean commentEnabled,
			VisibilityScope visibilityScope,
			AiModerationStatus aiModerationStatus,
			long storyLikeCount,
			LocalDateTime deletedAt
	) {
		this.scheduleUuid = scheduleUuid;
		this.title = title;
		this.content = content;
		this.coverImageUrl = coverImageUrl;
		this.visitCountry = visitCountry;
		this.visitCity = visitCity;
		this.visitPlace = visitPlace;
		this.startDate = startDate;
		this.endDate = endDate;
		this.commentEnabled = commentEnabled;
		this.visibilityScope = visibilityScope;
		this.aiModerationStatus = aiModerationStatus;
		this.storyLikeCount = storyLikeCount;
		this.deletedAt = deletedAt;
	}

	void applyLikeCount(long storyLikeCount) {
		this.storyLikeCount = storyLikeCount;
	}
}
