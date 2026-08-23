package com.planwith.planwith_fo_story.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_story.adapter.in.web.dto.StoryDetailResponse;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.StoryListView;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
@Tag(name = "story-query", description = "Story query API")
public class StoryQueryController {

	private final StoryQueryUseCase storyQueryUseCase;

	// 스토리 상세 조회
	@GetMapping("/{storyUuid}")
	public ResponseEntity<StoryDetailResponse> getDetail(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID viewerUuid,
			@PathVariable UUID storyUuid
	) {
		log.info("StoryQueryController : GET getDetail : 스토리 상세 조회 요청");
		return ResponseEntity.ok(StoryDetailResponse.from(
				storyQueryUseCase.getDetail(new GetStoryDetailQuery(storyUuid, viewerUuid))
		));
	}

	// 스토리 목록 조회
	@GetMapping
	public ResponseEntity<StoryListView> getList(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID viewerUuid,
			@RequestParam UUID authorUuid,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		log.info("StoryQueryController : GET getList : 스토리 목록 조회 요청");
		return ResponseEntity.ok(storyQueryUseCase.getList(new GetStoryListQuery(authorUuid, viewerUuid, page, size)));
	}

}
