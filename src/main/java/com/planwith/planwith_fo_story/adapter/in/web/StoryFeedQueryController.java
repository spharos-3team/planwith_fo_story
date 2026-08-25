package com.planwith.planwith_fo_story.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_story.adapter.in.web.dto.StoryListResponse;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.StoryFeedType;
import com.planwith.planwith_fo_story.application.query.StorySortType;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/story-feeds")
@Tag(name = "story-query", description = "Story query API")
public class StoryFeedQueryController {

	private final StoryQueryUseCase storyQueryUseCase;

	// 스토리 피드 조회
	@GetMapping
	public ResponseEntity<StoryListResponse> getFeed(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID viewerUuid,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "LATEST") StorySortType sort,
			@RequestParam(defaultValue = "FOLLOWING") StoryFeedType feedType
	) {
		log.info("StoryFeedQueryController : GET getFeed : 스토리 피드 조회 요청");
		return ResponseEntity.ok(StoryListResponse.from(
				storyQueryUseCase.getFeed(new GetStoryFeedQuery(viewerUuid, page, size, sort, feedType))
		));
	}
}
