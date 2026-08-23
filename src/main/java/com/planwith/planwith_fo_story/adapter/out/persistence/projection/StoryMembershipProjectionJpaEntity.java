package com.planwith.planwith_fo_story.adapter.out.persistence.projection;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "story_membership_projection")
class StoryMembershipProjectionJpaEntity {

	@EmbeddedId
	private StoryMembershipProjectionId id;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(name = "membership_uuid", length = 36)
	private UUID membershipUuid;

	@Column(name = "subscribed", nullable = false)
	private boolean subscribed;

	@Column(name = "source_version", nullable = false)
	private long sourceVersion;

	@Column(name = "synchronized_at", nullable = false)
	private LocalDateTime synchronizedAt;

	protected StoryMembershipProjectionJpaEntity() {
	}

	StoryMembershipProjectionJpaEntity(UUID memberUuid, UUID creatorUuid) {
		this.id = new StoryMembershipProjectionId(memberUuid, creatorUuid);
	}

	StoryMembershipProjectionId id() {
		return id;
	}

	UUID membershipUuid() {
		return membershipUuid;
	}

	boolean subscribed() {
		return subscribed;
	}

	long sourceVersion() {
		return sourceVersion;
	}

	LocalDateTime synchronizedAt() {
		return synchronizedAt;
	}

	void apply(UUID membershipUuid, boolean subscribed, long sourceVersion, LocalDateTime synchronizedAt) {
		this.membershipUuid = membershipUuid;
		this.subscribed = subscribed;
		this.sourceVersion = sourceVersion;
		this.synchronizedAt = synchronizedAt;
	}

	@Embeddable
	static class StoryMembershipProjectionId implements Serializable {

		private static final long serialVersionUID = 1L;

		@JdbcTypeCode(SqlTypes.CHAR)
		@Column(name = "member_uuid", length = 36)
		private UUID memberUuid;

		@JdbcTypeCode(SqlTypes.CHAR)
		@Column(name = "creator_uuid", length = 36)
		private UUID creatorUuid;

		protected StoryMembershipProjectionId() {
		}

		StoryMembershipProjectionId(UUID memberUuid, UUID creatorUuid) {
			this.memberUuid = memberUuid;
			this.creatorUuid = creatorUuid;
		}

		UUID memberUuid() {
			return memberUuid;
		}

		UUID creatorUuid() {
			return creatorUuid;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof StoryMembershipProjectionId that)) {
				return false;
			}
			return Objects.equals(memberUuid, that.memberUuid)
					&& Objects.equals(creatorUuid, that.creatorUuid);
		}

		@Override
		public int hashCode() {
			return Objects.hash(memberUuid, creatorUuid);
		}
	}
}
