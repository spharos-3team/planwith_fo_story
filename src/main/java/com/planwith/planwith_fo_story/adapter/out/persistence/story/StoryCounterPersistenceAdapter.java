package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryCounterPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoryCounterPersistenceAdapter implements StoryCounterPort {

	private final SpringDataStoryRepository storyRepository;

	@Override
	@Transactional
	public boolean incrementViewCount(UUID storyUuid) {
		return storyRepository.incrementViewCount(storyUuid) == 1;
	}

	@Override
	@Transactional
	public boolean changeLikeCount(UUID storyUuid, long delta) {
		return storyRepository.changeLikeCount(storyUuid, delta) == 1;
	}

	@Override
	@Transactional
	public boolean changeCommentCount(UUID storyUuid, long delta) {
		return storyRepository.changeCommentCount(storyUuid, delta) == 1;
	}
}
