package com.planwith.planwith_fo_story.domain.model.projection;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

/**
 * Membership 서비스에서 수신한 구독 권한 Projection. 원본 멤버십이 아니다.
 */
public final class MembershipEntitlementProjection {

	private final MemberUuid memberUuid;
	private final MemberUuid creatorUuid;
	private final UUID membershipUuid;
	private final boolean subscribed;
	private final long sourceVersion;
	private final LocalDateTime synchronizedAt;

	public MembershipEntitlementProjection(
			MemberUuid memberUuid,
			MemberUuid creatorUuid,
			UUID membershipUuid,
			boolean subscribed,
			long sourceVersion,
			LocalDateTime synchronizedAt
	) {
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.creatorUuid = Objects.requireNonNull(creatorUuid, "Creator UUID is required.");
		this.membershipUuid = membershipUuid;
		this.subscribed = subscribed;
		this.sourceVersion = Math.max(0L, sourceVersion);
		this.synchronizedAt = Objects.requireNonNull(synchronizedAt, "Synchronized at is required.");
	}

	public boolean canViewMembershipStories() {
		return subscribed;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public MemberUuid creatorUuid() {
		return creatorUuid;
	}

	public UUID membershipUuid() {
		return membershipUuid;
	}

	public boolean subscribed() {
		return subscribed;
	}

	public long sourceVersion() {
		return sourceVersion;
	}

	public LocalDateTime synchronizedAt() {
		return synchronizedAt;
	}
}
