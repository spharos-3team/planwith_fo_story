package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryLikeCountProjectionPort;
import com.planwith.planwith_fo_story.domain.model.projection.StoryLikeCountProjection;
import com.planwith.planwith_fo_story.domain.model.vo.StoryUuid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryLikeCountProjectionAdapter implements StoryLikeCountProjectionPort {

	private final SpringDataStoryRepository storyRepository;

	@Override
	@Transactional
	public void save(StoryLikeCountProjection projection) {
		storyRepository.findByStoryUuid(projection.storyUuid().value())
				.ifPresentOrElse(
						entity -> {
							entity.applyLikeCount(projection.likeCount());
							storyRepository.save(entity);
							log.debug(
									"StoryLikeCountProjectionAdapter : save : 좋아요 수 Projection 반영 - storyUuid={}, likeCount={}",
									projection.storyUuid(),
									projection.likeCount()
							);
						},
						() -> log.warn(
								"StoryLikeCountProjectionAdapter : save : 대상 스토리가 없어 좋아요 수 Projection 생략 - storyUuid={}",
								projection.storyUuid()
						)
				);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<StoryLikeCountProjection> findByStoryUuid(UUID storyUuid) {
		return storyRepository.findByStoryUuid(storyUuid)
				.map(entity -> new StoryLikeCountProjection(
						StoryUuid.of(entity.storyUuid()),
						entity.storyLikeCount(),
						0L
				));
	}
}
