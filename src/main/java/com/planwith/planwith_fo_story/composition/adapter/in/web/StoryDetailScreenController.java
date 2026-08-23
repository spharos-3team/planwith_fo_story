package com.planwith.planwith_fo_story.composition.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_story.composition.adapter.in.web.dto.StoryDetailScreenResponse;
import com.planwith.planwith_fo_story.composition.application.port.in.StoryDetailScreenUseCase;
import com.planwith.planwith_fo_story.composition.application.query.GetStoryDetailScreenQuery;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bff/stories")
@Tag(name = "story-detail-screen", description = "Story detail screen composition API")
public class StoryDetailScreenController {

	private final StoryDetailScreenUseCase storyDetailScreenUseCase;

	// 스토리 상세 화면 통합 조회
	@GetMapping("/{storyUuid}")
	public ResponseEntity<StoryDetailScreenResponse> getDetailScreen(
			@RequestHeader(value = "X-Member-UUID", required = false) UUID viewerUuid,
			@PathVariable UUID storyUuid
	) {
		log.info("StoryDetailScreenController : GET getDetailScreen : 스토리 상세 화면 통합 조회 요청");
		return ResponseEntity.ok(StoryDetailScreenResponse.from(
				storyDetailScreenUseCase.compose(new GetStoryDetailScreenQuery(storyUuid, viewerUuid))
		));
	}
}
