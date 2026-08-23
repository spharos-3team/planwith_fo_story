package com.planwith.planwith_fo_story.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

public final class StoryVisibilityMember {

	private final Long storyVisibilityMemberId;
	private final MemberUuid memberUuid;
	private final LocalDateTime createdAt;

	private StoryVisibilityMember(Long storyVisibilityMemberId, MemberUuid memberUuid, LocalDateTime createdAt) {
		this.storyVisibilityMemberId = storyVisibilityMemberId;
		this.memberUuid = Objects.requireNonNull(memberUuid, "회원 UUID는 필수입니다.");
		this.createdAt = Objects.requireNonNull(createdAt, "지정 공개 생성 시각은 필수입니다.");
	}

	public static StoryVisibilityMember create(MemberUuid memberUuid, LocalDateTime createdAt) {
		return new StoryVisibilityMember(null, memberUuid, createdAt);
	}

	public static StoryVisibilityMember restore(
			Long storyVisibilityMemberId,
			MemberUuid memberUuid,
			LocalDateTime createdAt
	) {
		return new StoryVisibilityMember(storyVisibilityMemberId, memberUuid, createdAt);
	}

	public Long storyVisibilityMemberId() {
		return storyVisibilityMemberId;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public LocalDateTime createdAt() {
		return createdAt;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StoryVisibilityMember that)) {
			return false;
		}
		return memberUuid.equals(that.memberUuid);
	}

	@Override
	public int hashCode() {
		return memberUuid.hashCode();
	}
}
