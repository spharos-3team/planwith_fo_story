package com.planwith.planwith_fo_story.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;

public final class StoryPlaceImage {

	private static final int IMAGE_URL_MAX_LENGTH = 500;
	private static final int MIN_IMAGE_ORDER = 1;
	private static final int MAX_IMAGE_ORDER = 5;

	private final Long storyPlaceImageId;
	private final String imageUrl;
	private final int imageOrder;
	private final LocalDateTime createdAt;

	private StoryPlaceImage(Long storyPlaceImageId, String imageUrl, int imageOrder, LocalDateTime createdAt) {
		this.storyPlaceImageId = storyPlaceImageId;
		this.imageUrl = requireImageUrl(imageUrl);
		this.imageOrder = requireImageOrder(imageOrder);
		this.createdAt = Objects.requireNonNull(createdAt, "이미지 생성 시각은 필수입니다.");
	}

	public static StoryPlaceImage create(String imageUrl, int imageOrder, LocalDateTime createdAt) {
		return new StoryPlaceImage(null, imageUrl, imageOrder, createdAt);
	}

	public static StoryPlaceImage restore(
			Long storyPlaceImageId,
			String imageUrl,
			int imageOrder,
			LocalDateTime createdAt
	) {
		return new StoryPlaceImage(storyPlaceImageId, imageUrl, imageOrder, createdAt);
	}

	private static String requireImageUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			throw new InvalidStoryStateException("장소 이미지 URL은 필수입니다.");
		}
		String trimmed = imageUrl.trim();
		if (trimmed.length() > IMAGE_URL_MAX_LENGTH) {
			throw new InvalidStoryStateException("장소 이미지 URL은 500자를 초과할 수 없습니다.");
		}
		return trimmed;
	}

	private static int requireImageOrder(int imageOrder) {
		if (imageOrder < MIN_IMAGE_ORDER || imageOrder > MAX_IMAGE_ORDER) {
			throw new InvalidStoryStateException("장소 이미지 순서는 1부터 5까지만 허용됩니다.");
		}
		return imageOrder;
	}

	public Long storyPlaceImageId() {
		return storyPlaceImageId;
	}

	public String imageUrl() {
		return imageUrl;
	}

	public int imageOrder() {
		return imageOrder;
	}

	public LocalDateTime createdAt() {
		return createdAt;
	}
}
