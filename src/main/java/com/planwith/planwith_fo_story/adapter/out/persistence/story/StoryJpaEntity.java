package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
		name = "story",
		indexes = {
				@Index(name = "idx_story_member", columnList = "member_uuid, created_at, deleted_at"),
				@Index(name = "idx_story_created", columnList = "created_at, deleted_at"),
				@Index(name = "idx_story_like", columnList = "story_like_count, deleted_at"),
				@Index(name = "idx_story_view", columnList = "view_count, deleted_at"),
				@Index(name = "idx_story_visibility", columnList = "visibility_scope, created_at, deleted_at"),
				@Index(name = "idx_story_schedule", columnList = "schedule_uuid")
		}
)
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

	@Column(name = "schedule_visible", nullable = false)
	private boolean scheduleVisible;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Lob
	@Column(name = "content", nullable = false)
	private String content;

	@Column(name = "cover_image_url", nullable = false, length = 500)
	private String coverImageUrl;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "comment_enabled", nullable = false)
	private boolean commentEnabled;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility_scope", nullable = false, length = 20)
	private VisibilityScope visibilityScope;

	@Enumerated(EnumType.STRING)
	@Column(name = "ai_moderation_status", nullable = false, length = 20)
	private AiModerationStatus aiModerationStatus;

	@Column(name = "view_count", nullable = false)
	private long viewCount;

	@Column(name = "story_like_count", nullable = false)
	private long storyLikeCount;

	@Column(name = "story_comment_count", nullable = false)
	private long storyCommentCount;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoryVisitCountryJpaEntity> visitCountries = new ArrayList<>();

	@OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoryTagJpaEntity> tags = new ArrayList<>();

	@OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<StoryVisibilityMemberJpaEntity> visibilityMembers = new ArrayList<>();

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

	boolean scheduleVisible() {
		return scheduleVisible;
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

	long viewCount() {
		return viewCount;
	}

	long storyLikeCount() {
		return storyLikeCount;
	}

	long storyCommentCount() {
		return storyCommentCount;
	}

	LocalDateTime createdAt() {
		return createdAt;
	}

	LocalDateTime updatedAt() {
		return updatedAt;
	}

	LocalDateTime deletedAt() {
		return deletedAt;
	}

	List<StoryVisitCountryJpaEntity> visitCountries() {
		return visitCountries;
	}

	List<StoryTagJpaEntity> tags() {
		return tags;
	}

	List<StoryVisibilityMemberJpaEntity> visibilityMembers() {
		return visibilityMembers;
	}

	void assignIdentity(UUID storyUuid, UUID memberUuid, LocalDateTime createdAt) {
		this.storyUuid = storyUuid;
		this.memberUuid = memberUuid;
		this.createdAt = createdAt;
	}

	void apply(
			UUID scheduleUuid,
			boolean scheduleVisible,
			String title,
			String content,
			String coverImageUrl,
			LocalDate startDate,
			LocalDate endDate,
			boolean commentEnabled,
			VisibilityScope visibilityScope,
			AiModerationStatus aiModerationStatus,
			long viewCount,
			long storyLikeCount,
			long storyCommentCount,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt
	) {
		this.scheduleUuid = scheduleUuid;
		this.scheduleVisible = scheduleVisible;
		this.title = title;
		this.content = content;
		this.coverImageUrl = coverImageUrl;
		this.startDate = startDate;
		this.endDate = endDate;
		this.commentEnabled = commentEnabled;
		this.visibilityScope = visibilityScope;
		this.aiModerationStatus = aiModerationStatus;
		this.viewCount = viewCount;
		this.storyLikeCount = storyLikeCount;
		this.storyCommentCount = storyCommentCount;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	void applyAiModerationStatus(AiModerationStatus aiModerationStatus, LocalDateTime updatedAt) {
		this.aiModerationStatus = aiModerationStatus;
		this.updatedAt = updatedAt;
	}

	void applyLikeCount(long storyLikeCount) {
		this.storyLikeCount = storyLikeCount;
	}

	void replaceVisitCountries(List<StoryVisitCountryJpaEntity> next) {
		this.visitCountries.clear();
		this.visitCountries.addAll(next);
	}

	void replaceTags(List<StoryTagJpaEntity> next) {
		this.tags.clear();
		this.tags.addAll(next);
	}

	void replaceVisibilityMembers(List<StoryVisibilityMemberJpaEntity> next) {
		this.visibilityMembers.clear();
		this.visibilityMembers.addAll(next);
	}
}
