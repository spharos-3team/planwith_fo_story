package com.planwith.planwith_fo_story.domain.model.projection;

import java.util.Objects;

import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

/**
 * Like 서비스에서 수신한 좋아요 수 Projection. 원본 좋아요가 아니다.
 */
public final class StoryLikeCountProjection {

	private final StoryUuid storyUuid;
	private final long likeCount;
	private final long sourceVersion;

	public StoryLikeCountProjection(StoryUuid storyUuid, long likeCount, long sourceVersion) {
		this.storyUuid = Objects.requireNonNull(storyUuid, "Story UUID is required.");
		this.likeCount = Math.max(0L, likeCount);
		this.sourceVersion = Math.max(0L, sourceVersion);
	}

	public StoryLikeCountProjection increment() {
		return new StoryLikeCountProjection(storyUuid, likeCount + 1, sourceVersion + 1);
	}

	public StoryLikeCountProjection decrement() {
		return new StoryLikeCountProjection(storyUuid, Math.max(0L, likeCount - 1), sourceVersion + 1);
	}

	public StoryUuid storyUuid() {
		return storyUuid;
	}

	public long likeCount() {
		return likeCount;
	}

	public long sourceVersion() {
		return sourceVersion;
	}
}
