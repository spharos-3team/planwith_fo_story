package com.planwith.planwith_fo_story.adapter.in.web;

import java.util.UUID;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_story.adapter.in.web.dto.StoryDetailResponse;
import com.planwith.planwith_fo_story.adapter.in.web.dto.StoryListResponse;
import com.planwith.planwith_fo_story.adapter.in.web.dto.MyStoryListResponse;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.GetMyStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetMyStoryListQuery;
import com.planwith.planwith_fo_story.application.query.SearchStoryQuery;
import com.planwith.planwith_fo_story.application.query.StorySearchType;
import com.planwith.planwith_fo_story.application.query.StorySortType;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID viewerUuid,
			@PathVariable UUID storyUuid
	) {
		log.info("StoryQueryController : GET getDetail : 스토리 상세 조회 요청");
		return ResponseEntity.ok(StoryDetailResponse.from(
				storyQueryUseCase.getDetail(new GetStoryDetailQuery(storyUuid, viewerUuid))
		));
	}

	// 스토리 목록 조회
	@GetMapping
	public ResponseEntity<StoryListResponse> getList(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID viewerUuid,
			@RequestParam(required = false) UUID authorUuid,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "LATEST") StorySortType sort
	) {
		log.info("StoryQueryController : GET getList : 스토리 목록 조회 요청");
		return ResponseEntity.ok(StoryListResponse.from(
				storyQueryUseCase.getList(new GetStoryListQuery(authorUuid, viewerUuid, page, size, sort))
		));
	}

	@GetMapping("/search")
	public ResponseEntity<StoryListResponse> search(
			@RequestHeader(value = "X-Auth-User-Id", required = false) UUID viewerUuid,
			@RequestParam StorySearchType type,
			@RequestParam @NotBlank @Size(max = 100) String keyword,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		log.info("StoryQueryController : GET search : 스토리 검색 요청");
		return ResponseEntity.ok(StoryListResponse.from(
				storyQueryUseCase.search(new SearchStoryQuery(type, keyword, viewerUuid, page, size))
		));
	}

	@GetMapping("/me")
	public ResponseEntity<MyStoryListResponse> getMyStories(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
			@RequestParam(required = false) @Size(max = 100) String country,
			@RequestParam(required = false) @Size(max = 100) String city,
			@RequestParam(required = false) VisibilityScope visibilityScope,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelStartDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelEndDate,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		log.info("StoryQueryController : GET getMyStories : 내 스토리 목록 조회 요청");
		return ResponseEntity.ok(MyStoryListResponse.from(storyQueryUseCase.getMyStories(
				new GetMyStoryListQuery(
						memberUuid,
						country,
						city,
						visibilityScope,
						travelStartDate,
						travelEndDate,
						page,
						size
				)
		)));
	}

	@GetMapping("/me/{storyUuid}")
	public ResponseEntity<StoryDetailResponse> getMyStoryDetail(
			@RequestHeader("X-Auth-User-Id") UUID memberUuid,
			@PathVariable UUID storyUuid
	) {
		log.info("StoryQueryController : GET getMyStoryDetail : 내 스토리 상세 조회 요청");
		return ResponseEntity.ok(StoryDetailResponse.from(
				storyQueryUseCase.getMyStoryDetail(new GetMyStoryDetailQuery(memberUuid, storyUuid))
		));
	}

}
