package com.planwith.planwith_fo_story.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

public final class Story {

	private static final int TITLE_MAX_LENGTH = 200;
	private static final int COVER_IMAGE_MAX_LENGTH = 500;

	private final Long storyId;
	private final StoryUuid storyUuid;
	private final MemberUuid memberUuid;
	private final UUID scheduleUuid;
	private final boolean scheduleVisible;
	private final String title;
	private final String content;
	private final String coverImageUrl;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final boolean commentEnabled;
	private final VisibilityScope visibilityScope;
	private final AiModerationStatus aiModerationStatus;
	private final long viewCount;
	private final long storyLikeCount;
	private final long storyCommentCount;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
	private final LocalDateTime deletedAt;
	private final List<StoryVisitCountry> visitCountries;
	private final List<StoryPlace> places;
	private final List<StoryTag> tags;
	private final List<StoryVisibilityMember> visibilityMembers;

	private Story(
			Long storyId,
			StoryUuid storyUuid,
			MemberUuid memberUuid,
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
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt,
			List<StoryVisitCountry> visitCountries,
			List<StoryPlace> places,
			List<StoryTag> tags,
			List<StoryVisibilityMember> visibilityMembers
	) {
		this.storyId = storyId;
		this.storyUuid = Objects.requireNonNull(storyUuid, "Story UUID is required.");
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.scheduleUuid = scheduleUuid;
		this.scheduleVisible = scheduleVisible;
		this.title = requireTitle(title);
		this.content = requireContent(content);
		this.coverImageUrl = requireCoverImageUrl(coverImageUrl);
		this.startDate = Objects.requireNonNull(startDate, "여행 시작일은 필수입니다.");
		this.endDate = Objects.requireNonNull(endDate, "여행 종료일은 필수입니다.");
		this.commentEnabled = commentEnabled;
		this.visibilityScope = Objects.requireNonNull(visibilityScope, "Visibility scope is required.");
		this.aiModerationStatus = Objects.requireNonNull(aiModerationStatus, "AI moderation status is required.");
		this.viewCount = Math.max(0L, viewCount);
		this.storyLikeCount = Math.max(0L, storyLikeCount);
		this.storyCommentCount = Math.max(0L, storyCommentCount);
		this.createdAt = Objects.requireNonNull(createdAt, "Created at is required.");
		this.updatedAt = Objects.requireNonNull(updatedAt, "Updated at is required.");
		this.deletedAt = deletedAt;
		this.visitCountries = List.copyOf(visitCountries == null ? List.of() : visitCountries);
		this.places = List.copyOf(places == null ? List.of() : places);
		this.tags = List.copyOf(tags == null ? List.of() : tags);
		this.visibilityMembers = List.copyOf(visibilityMembers == null ? List.of() : visibilityMembers);
		validatePeriod(this.startDate, this.endDate);
	}

	public static Story create(
			StoryUuid storyUuid,
			MemberUuid memberUuid,
			UUID scheduleUuid,
			boolean scheduleVisible,
			String title,
			String content,
			String coverImageUrl,
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
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope == null ? VisibilityScope.ALL : visibilityScope,
				AiModerationStatus.UNVERIFIED,
				0L,
				0L,
				0L,
				createdAt,
				createdAt,
				null,
				List.of(),
				List.of(),
				List.of(),
				List.of()
		);
	}

	public static Story restore(
			Long storyId,
			StoryUuid storyUuid,
			MemberUuid memberUuid,
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
			LocalDateTime createdAt,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt,
			List<StoryVisitCountry> visitCountries,
			List<StoryPlace> places,
			List<StoryTag> tags,
			List<StoryVisibilityMember> visibilityMembers
	) {
		return new Story(
				storyId,
				storyUuid,
				memberUuid,
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				aiModerationStatus,
				viewCount,
				storyLikeCount,
				storyCommentCount,
				createdAt,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public Story update(
			MemberUuid actor,
			UUID scheduleUuid,
			boolean scheduleVisible,
			String title,
			String content,
			String coverImageUrl,
			LocalDate startDate,
			LocalDate endDate,
			LocalDateTime updatedAt
	) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public Story changeVisibility(MemberUuid actor, VisibilityScope visibilityScope, LocalDateTime updatedAt) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public Story changeCommentEnabled(MemberUuid actor, boolean commentEnabled, LocalDateTime updatedAt) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public Story delete(MemberUuid actor, LocalDateTime deletedAt) {
		ensureOwner(actor);
		ensureActive();
		return copy(
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				deletedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public Story replaceChildren(
			List<StoryVisitCountry> visitCountries,
			List<StoryPlace> places,
			List<StoryTag> tags,
			List<StoryVisibilityMember> visibilityMembers,
			LocalDateTime updatedAt
	) {
		ensureActive();
		return copy(
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public Story projectLikeCount(long storyLikeCount) {
		return restore(
				storyId,
				storyUuid,
				memberUuid,
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				aiModerationStatus,
				viewCount,
				storyLikeCount,
				storyCommentCount,
				createdAt,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
		);
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public boolean isOwnedBy(MemberUuid actor) {
		return memberUuid.equals(actor);
	}

	private Story copy(
			UUID scheduleUuid,
			boolean scheduleVisible,
			String title,
			String content,
			String coverImageUrl,
			LocalDate startDate,
			LocalDate endDate,
			boolean commentEnabled,
			VisibilityScope visibilityScope,
			LocalDateTime updatedAt,
			LocalDateTime deletedAt,
			List<StoryVisitCountry> visitCountries,
			List<StoryPlace> places,
			List<StoryTag> tags,
			List<StoryVisibilityMember> visibilityMembers
	) {
		return new Story(
				storyId,
				storyUuid,
				memberUuid,
				scheduleUuid,
				scheduleVisible,
				title,
				content,
				coverImageUrl,
				startDate,
				endDate,
				commentEnabled,
				visibilityScope,
				aiModerationStatus,
				viewCount,
				storyLikeCount,
				storyCommentCount,
				createdAt,
				updatedAt,
				deletedAt,
				visitCountries,
				places,
				tags,
				visibilityMembers
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

	private static String requireCoverImageUrl(String coverImageUrl) {
		if (coverImageUrl == null || coverImageUrl.isBlank()) {
			throw new InvalidStoryStateException("커버 이미지 URL은 필수입니다.");
		}
		String trimmed = coverImageUrl.trim();
		if (trimmed.length() > COVER_IMAGE_MAX_LENGTH) {
			throw new InvalidStoryStateException("커버 이미지 URL은 500자를 초과할 수 없습니다.");
		}
		return trimmed;
	}

	private static void validatePeriod(LocalDate startDate, LocalDate endDate) {
		if (endDate.isBefore(startDate)) {
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

	public boolean scheduleVisible() {
		return scheduleVisible;
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

	public long viewCount() {
		return viewCount;
	}

	public long storyLikeCount() {
		return storyLikeCount;
	}

	public long storyCommentCount() {
		return storyCommentCount;
	}

	public LocalDateTime createdAt() {
		return createdAt;
	}

	public LocalDateTime updatedAt() {
		return updatedAt;
	}

	public LocalDateTime deletedAt() {
		return deletedAt;
	}

	public List<StoryVisitCountry> visitCountries() {
		return visitCountries;
	}

	public List<StoryPlace> places() {
		return places;
	}

	public List<StoryTag> tags() {
		return tags;
	}

	public List<StoryVisibilityMember> visibilityMembers() {
		return visibilityMembers;
	}
}
