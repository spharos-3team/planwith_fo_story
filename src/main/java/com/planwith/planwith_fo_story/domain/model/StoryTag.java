package com.planwith.planwith_fo_story.domain.model;

import java.util.Objects;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;

public final class StoryTag {

	private static final int TAG_NAME_MAX_LENGTH = 50;

	private final Long storyTagId;
	private final String tagName;

	private StoryTag(Long storyTagId, String tagName) {
		this.storyTagId = storyTagId;
		this.tagName = requireTagName(tagName);
	}

	public static StoryTag create(String tagName) {
		return new StoryTag(null, tagName);
	}

	public static StoryTag restore(Long storyTagId, String tagName) {
		return new StoryTag(storyTagId, tagName);
	}

	private static String requireTagName(String tagName) {
		if (tagName == null || tagName.isBlank()) {
			throw new InvalidStoryStateException("태그명은 필수입니다.");
		}
		String trimmed = tagName.trim();
		if (trimmed.length() > TAG_NAME_MAX_LENGTH) {
			throw new InvalidStoryStateException("태그명은 50자를 초과할 수 없습니다.");
		}
		return trimmed;
	}

	public Long storyTagId() {
		return storyTagId;
	}

	public String tagName() {
		return tagName;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof StoryTag that)) {
			return false;
		}
		return Objects.equals(tagName, that.tagName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(tagName);
	}
}
