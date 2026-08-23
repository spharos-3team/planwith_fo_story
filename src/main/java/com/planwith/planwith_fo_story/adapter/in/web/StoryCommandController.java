package com.planwith.planwith_fo_story.adapter.in.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_story.adapter.in.web.dto.ChangeStoryCommentEnabledRequest;
import com.planwith.planwith_fo_story.adapter.in.web.dto.ChangeStoryVisibilityRequest;
import com.planwith.planwith_fo_story.adapter.in.web.dto.CreateStoryRequest;
import com.planwith.planwith_fo_story.adapter.in.web.dto.UpdateStoryRequest;
import com.planwith.planwith_fo_story.application.command.ChangeStoryCommentEnabledCommand;
import com.planwith.planwith_fo_story.application.command.ChangeStoryVisibilityCommand;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.MemberAuthenticationRequiredException;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stories")
@Tag(name = "story-command", description = "Story command API")
public class StoryCommandController {

	private final StoryCommandUseCase storyCommandUseCase;

	// 스토리 생성
	@PostMapping
	public ResponseEntity<StoryDetailView> create(
			@RequestHeader("X-Member-UUID") UUID actorUuid,
			@Valid @RequestBody CreateStoryRequest request
	) {
		log.info("StoryCommandController : POST create : 스토리 생성 요청");
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(storyCommandUseCase.create(request.toCommand(requireActor(actorUuid))));
	}

	// 스토리 수정
	@PutMapping("/{storyUuid}")
	public ResponseEntity<StoryDetailView> update(
			@RequestHeader("X-Member-UUID") UUID actorUuid,
			@PathVariable UUID storyUuid,
			@Valid @RequestBody UpdateStoryRequest request
	) {
		log.info("StoryCommandController : PUT update : 스토리 수정 요청");
		return ResponseEntity.ok(storyCommandUseCase.update(new UpdateStoryCommand(
				requireActor(actorUuid),
				storyUuid,
				request.scheduleUuid(),
				request.resolvedScheduleVisible(),
				request.title(),
				request.content(),
				request.coverImageUrl(),
				request.startDate(),
				request.endDate()
		)));
	}

	// 스토리 삭제
	@DeleteMapping("/{storyUuid}")
	public ResponseEntity<Void> delete(
			@RequestHeader("X-Member-UUID") UUID actorUuid,
			@PathVariable UUID storyUuid
	) {
		log.info("StoryCommandController : DELETE delete : 스토리 삭제 요청");
		storyCommandUseCase.delete(new DeleteStoryCommand(requireActor(actorUuid), storyUuid));
		return ResponseEntity.noContent().build();
	}

	// 스토리 공개범위 변경
	@PatchMapping("/{storyUuid}/visibility")
	public ResponseEntity<StoryDetailView> changeVisibility(
			@RequestHeader("X-Member-UUID") UUID actorUuid,
			@PathVariable UUID storyUuid,
			@Valid @RequestBody ChangeStoryVisibilityRequest request
	) {
		log.info("StoryCommandController : PATCH changeVisibility : 스토리 공개범위 변경 요청");
		return ResponseEntity.ok(storyCommandUseCase.changeVisibility(new ChangeStoryVisibilityCommand(
				requireActor(actorUuid),
				storyUuid,
				request.visibilityScope()
		)));
	}

	// 스토리 댓글 허용 변경
	@PatchMapping("/{storyUuid}/comment-enabled")
	public ResponseEntity<StoryDetailView> changeCommentEnabled(
			@RequestHeader("X-Member-UUID") UUID actorUuid,
			@PathVariable UUID storyUuid,
			@Valid @RequestBody ChangeStoryCommentEnabledRequest request
	) {
		log.info("StoryCommandController : PATCH changeCommentEnabled : 스토리 댓글 허용 변경 요청");
		return ResponseEntity.ok(storyCommandUseCase.changeCommentEnabled(new ChangeStoryCommentEnabledCommand(
				requireActor(actorUuid),
				storyUuid,
				request.commentEnabled()
		)));
	}

	private static UUID requireActor(UUID actorUuid) {
		if (actorUuid == null) {
			throw new MemberAuthenticationRequiredException();
		}
		return actorUuid;
	}
}
