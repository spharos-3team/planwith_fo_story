package com.planwith.planwith_fo_story.adapter.out.persistence.projection;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.planwith.planwith_fo_story.domain.model.MemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "story_member_projection")
class StoryMemberProjectionJpaEntity {

	@Id
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "member_uuid", length = 36)
	private UUID memberUuid;

	@Column(name = "nickname", nullable = false, length = 20)
	private String nickname;

	@Column(name = "profile_image", length = 1000)
	private String profileImage;

	@Enumerated(EnumType.STRING)
	@Column(name = "member_status", nullable = false, length = 20)
	private MemberStatus memberStatus;

	@Column(name = "source_version", nullable = false)
	private long sourceVersion;

	@Column(name = "synchronized_at", nullable = false)
	private LocalDateTime synchronizedAt;

	protected StoryMemberProjectionJpaEntity() {
	}

	StoryMemberProjectionJpaEntity(UUID memberUuid) {
		this.memberUuid = memberUuid;
	}

	UUID memberUuid() {
		return memberUuid;
	}

	String nickname() {
		return nickname;
	}

	String profileImage() {
		return profileImage;
	}

	MemberStatus memberStatus() {
		return memberStatus;
	}

	long sourceVersion() {
		return sourceVersion;
	}

	LocalDateTime synchronizedAt() {
		return synchronizedAt;
	}

	void apply(String nickname, String profileImage, MemberStatus memberStatus, long sourceVersion, LocalDateTime synchronizedAt) {
		this.nickname = nickname == null ? "" : nickname;
		this.profileImage = profileImage;
		this.memberStatus = memberStatus;
		this.sourceVersion = sourceVersion;
		this.synchronizedAt = synchronizedAt;
	}
}
