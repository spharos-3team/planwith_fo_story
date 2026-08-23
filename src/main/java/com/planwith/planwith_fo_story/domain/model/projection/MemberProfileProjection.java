package com.planwith.planwith_fo_story.domain.model.projection;

import java.time.LocalDateTime;
import java.util.Objects;

import com.planwith.planwith_fo_story.domain.model.MemberStatus;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

/**
 * Member 서비스에서 수신한 프로필 Projection. 원본 회원이 아니다.
 */
public final class MemberProfileProjection {

	private final MemberUuid memberUuid;
	private final String nickname;
	private final String profileImage;
	private final MemberStatus memberStatus;
	private final long sourceVersion;
	private final LocalDateTime synchronizedAt;

	public MemberProfileProjection(
			MemberUuid memberUuid,
			String nickname,
			String profileImage,
			MemberStatus memberStatus,
			long sourceVersion,
			LocalDateTime synchronizedAt
	) {
		this.memberUuid = Objects.requireNonNull(memberUuid, "Member UUID is required.");
		this.nickname = nickname == null ? "" : nickname;
		this.profileImage = profileImage;
		this.memberStatus = memberStatus == null ? MemberStatus.ACTIVE : memberStatus;
		this.sourceVersion = Math.max(0L, sourceVersion);
		this.synchronizedAt = Objects.requireNonNull(synchronizedAt, "Synchronized at is required.");
	}

	public boolean isNewerThan(long currentSourceVersion) {
		return sourceVersion >= currentSourceVersion;
	}

	public MemberUuid memberUuid() {
		return memberUuid;
	}

	public String nickname() {
		return nickname;
	}

	public String profileImage() {
		return profileImage;
	}

	public MemberStatus memberStatus() {
		return memberStatus;
	}

	public long sourceVersion() {
		return sourceVersion;
	}

	public LocalDateTime synchronizedAt() {
		return synchronizedAt;
	}
}
