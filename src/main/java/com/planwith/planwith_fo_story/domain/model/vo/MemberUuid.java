package com.planwith.planwith_fo_story.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class MemberUuid {

	private final UUID value;

	private MemberUuid(UUID value) {
		this.value = Objects.requireNonNull(value, "Member UUID is required.");
	}

	public static MemberUuid of(UUID value) {
		return new MemberUuid(value);
	}

	public static MemberUuid of(String value) {
		return new MemberUuid(UUID.fromString(value));
	}

	public UUID value() {
		return value;
	}

	public String asString() {
		return value.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MemberUuid that)) {
			return false;
		}
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
