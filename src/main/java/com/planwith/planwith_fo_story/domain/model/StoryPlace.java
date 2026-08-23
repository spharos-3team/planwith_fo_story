package com.planwith.planwith_fo_story.domain.model;

import java.util.List;

import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;

public final class StoryPlace {

	private static final int PLACE_NAME_MAX_LENGTH = 255;
	private static final int MAX_PLACE_IMAGES = 5;

	private final Long storyPlaceId;
	private final Long storyVisitCityId;
	private final String placeName;
	private final int displayOrder;
	private final List<StoryPlaceImage> images;

	private StoryPlace(
			Long storyPlaceId,
			Long storyVisitCityId,
			String placeName,
			int displayOrder,
			List<StoryPlaceImage> images
	) {
		this.storyPlaceId = storyPlaceId;
		this.storyVisitCityId = storyVisitCityId;
		this.placeName = requirePlaceName(placeName);
		this.displayOrder = Math.max(0, displayOrder);
		this.images = List.copyOf(images == null ? List.of() : images);
		validateImageOrders(this.images);
	}

	public static StoryPlace create(
			Long storyVisitCityId,
			String placeName,
			int displayOrder,
			List<StoryPlaceImage> images
	) {
		return new StoryPlace(null, storyVisitCityId, placeName, displayOrder, images);
	}

	public static StoryPlace restore(
			Long storyPlaceId,
			Long storyVisitCityId,
			String placeName,
			int displayOrder,
			List<StoryPlaceImage> images
	) {
		return new StoryPlace(storyPlaceId, storyVisitCityId, placeName, displayOrder, images);
	}

	private static String requirePlaceName(String placeName) {
		if (placeName == null || placeName.isBlank()) {
			throw new InvalidStoryStateException("장소명은 필수입니다.");
		}
		String trimmed = placeName.trim();
		if (trimmed.length() > PLACE_NAME_MAX_LENGTH) {
			throw new InvalidStoryStateException("장소명 길이가 허용 범위를 초과했습니다.");
		}
		return trimmed;
	}

	private static void validateImageOrders(List<StoryPlaceImage> images) {
		if (images.size() > MAX_PLACE_IMAGES) {
			throw new InvalidStoryStateException("장소 이미지는 장소당 최대 5개까지 허용됩니다.");
		}
		long distinctOrders = images.stream().map(StoryPlaceImage::imageOrder).distinct().count();
		if (distinctOrders != images.size()) {
			throw new InvalidStoryStateException("장소 이미지 순서는 중복될 수 없습니다.");
		}
	}

	public Long storyPlaceId() {
		return storyPlaceId;
	}

	public Long storyVisitCityId() {
		return storyVisitCityId;
	}

	public String placeName() {
		return placeName;
	}

	public int displayOrder() {
		return displayOrder;
	}

	public List<StoryPlaceImage> images() {
		return images;
	}
}
