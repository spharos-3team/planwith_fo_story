package com.planwith.planwith_fo_story.domain.model.vo;

import java.util.Objects;
import java.util.UUID;

public final class StoryUuid {

	private final UUID value;

	private StoryUuid(UUID value) {
		this.value = Objects.requireNonNull(value, "Story UUID is required.");
	}

	public static StoryUuid of(UUID value) {
		return new StoryUuid(value);
	}

	public static StoryUuid of(String value) {
		return new StoryUuid(UUID.fromString(value));
	}

	public static StoryUuid generate() {
		return new StoryUuid(UUID.randomUUID());
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
		if (!(other instanceof StoryUuid that)) {
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
