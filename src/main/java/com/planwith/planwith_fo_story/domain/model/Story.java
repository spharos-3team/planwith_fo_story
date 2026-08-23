package com.planwith.planwith_fo_story.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

public final class Story {

	private static final int TITLE_MAX_LENGTH = 200;
	private static final int COVER_IMAGE_MAX_LENGTH = 500;
	private static final int VISIT_COUNTRY_MAX_LENGTH = 100;
	private static final int VISIT_CITY_MAX_LENGTH = 100;
	private static final int VISIT_PLACE_MAX_LENGTH = 255;

	private final Long storyId;
	private final StoryUuid storyUuid;
	private final MemberUuid memberUuid;
	private final UUID scheduleUuid;
	private final String title;
	private final String content;
	private final String coverImageUrl;
	private final String visitCountry;
	private final String visitCity;
	private final String visitPlace;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final boolean commentEnabled;
	private final VisibilityScope visibilityScope;
	private final AiModerationStatus aiModerationStatus;
	private final long storyLikeCount;
	private final LocalDateTime createdAt;
	private final LocalDateTime deletedAt;

	private Story(
			Long storyId,
			StoryUuid storyUuid,
			MemberUuid memberUuid,
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
			LocalDateTime createdAt,
			LocalDateTime deletedAt
	) {
		this.storyId = storyId;
		this.storyUuid = Objects.requireNonNull(storyUuid, "Story UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.scheduleUuid = scheduleUuid;
		this.title = requireTitle(title);
		this.content = requireContent(content);
		this.coverImageUrl = optionalMax(coverImageUrl, COVER_IMAGE_MAX_LENGTH, "coverImageUrl");
		this.visitCountry = optionalMax(visitCountry, VISIT_COUNTRY_MAX_LENGTH, "visitCountry");
		this.visitCity = optionalMax(visitCity, VISIT_CITY_MAX_LENGTH, "visitCity");
		this.visitPlace = optionalMax(visitPlace, VISIT_PLACE_MAX_LENGTH, "visitPlace");
		this.startDate = startDate;
		this.endDate = endDate;
		this.commentEnabled = commentEnabled;
		this.visibilityScope = Objects.requireNonNull(visibilityScope, "Visibility scope is required.");
		this.aiModerationStatus = Objects.requireNonNull(aiModerationStatus, "AI moderation status is required.");
		this.storyLikeCount = Math.max(0L, storyLikeCount);
		this.createdAt = Objects.requireNonNull(createdAt, "Created at is required.");
		this.deletedAt = deletedAt;
		validatePeriod(startDate, endDate);
	}

	public static Story create(
			StoryUuid storyUuid,
			MemberUuid memberUuid,
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
			LocalDateTime createdAt
	) {
		return new Story(
				null,
				storyUuid,
				memberUuid,
				scheduleUuid,
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope == null ? VisibilityScope.ALL : visibilityScope,
				AiModerationStatus.UNVERIFIED,
				0L,
				createdAt,
				null
		);
	}

	public static Story restore(
			Long storyId,
			StoryUuid storyUuid,
			MemberUuid memberUuid,
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
			LocalDateTime createdAt,
			LocalDateTime deletedAt
	) {
		return new Story(
				storyId,
				storyUuid,
				memberUuid,
				scheduleUuid,
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				aiModerationStatus,
				storyLikeCount,
				createdAt,
				deletedAt
		);
	}

	public Story update(
			MemberUuid actor,
			String title,
			String content,
			String coverImageUrl,
			String visitCountry,
			String visitCity,
			String visitPlace,
			LocalDate startDate,
			LocalDate endDate
	) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				deletedAt
		);
	}

	public Story changeVisibility(MemberUuid actor, VisibilityScope visibilityScope) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				deletedAt
		);
	}

	public Story changeCommentEnabled(MemberUuid actor, boolean commentEnabled) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				deletedAt
		);
	}

	public Story delete(MemberUuid actor, LocalDateTime deletedAt) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				Objects.requireNonNull(deletedAt, "Deleted at is required.")
		);
	}

	public Story projectLikeCount(long storyLikeCount) {
		return new Story(
				storyId,
				storyUuid,
				memberUuid,
				scheduleUuid,
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				aiModerationStatus,
				storyLikeCount,
				createdAt,
				deletedAt
		);
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public boolean isOwnedBy(MemberUuid actor) {
		return memberUuid.equals(actor);
	}

	private Story copy(
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
			LocalDateTime deletedAt
	) {
		return new Story(
				storyId,
				storyUuid,
				memberUuid,
				scheduleUuid,
				title,
				content,
				coverImageUrl,
				visitCountry,
				visitCity,
				visitPlace,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				aiModerationStatus,
				storyLikeCount,
				createdAt,
				deletedAt
		);
	}

	private void ensureOwner(MemberUuid actor) {
		if (!isOwnedBy(actor)) {
			throw new StoryAccessDeniedException();
		}
	}

	private void ensureActive() {
		if (isDeleted()) {
			throw new InvalidStoryStateException("삭제된 스토리는 변경할 수 없습니다.");
		}
	}

	private static String requireTitle(String title) {
		if (title == null || title.isBlank()) {
			throw new InvalidStoryStateException("스토리 제목은 필수입니다.");
		}
		String trimmed = title.trim();
		if (trimmed.length() > TITLE_MAX_LENGTH) {
			throw new InvalidStoryStateException("스토리 제목은 200자를 초과할 수 없습니다.");
		}
		return trimmed;
	}

	private static String requireContent(String content) {
		if (content == null || content.isBlank()) {
			throw new InvalidStoryStateException("스토리 본문은 필수입니다.");
		}
		return content;
	}

	private static String optionalMax(String value, int maxLength, String fieldName) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			throw new InvalidStoryStateException(fieldName + " 길이가 허용 범위를 초과했습니다.");
		}
		return trimmed;
	}

	private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
			throw new InvalidStoryStateException("여행 종료일은 시작일보다 빠를 수 없습니다.");
		}
	}

	public Long storyId() {
		return storyId;
	}

	public StoryUuid storyUuid() {
		return storyUuid;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public UUID scheduleUuid() {
		return scheduleUuid;
	}

	public String title() {
		return title;
	}

	public String content() {
		return content;
	}

	public String coverImageUrl() {
		return coverImageUrl;
	}

	public String visitCountry() {
		return visitCountry;
	}

	public String visitCity() {
		return visitCity;
	}

	public String visitPlace() {
		return visitPlace;
	}

	public LocalDate startDate() {
		return startDate;
	}

	public LocalDate endDate() {
		return endDate;
	}

	public boolean commentEnabled() {
		return commentEnabled;
	}

	public VisibilityScope visibilityScope() {
		return visibilityScope;
	}

	public AiModerationStatus aiModerationStatus() {
		return aiModerationStatus;
	}

	public long storyLikeCount() {
		return storyLikeCount;
	}

	public LocalDateTime createdAt() {
		return createdAt;
	}

	public LocalDateTime deletedAt() {
		return deletedAt;
	}
}
