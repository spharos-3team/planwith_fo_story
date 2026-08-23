package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.domain.model.Story;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoryCommandPersistenceAdapter implements StoryCommandPort {

	private final SpringDataStoryRepository storyRepository;

	@Override
	@Transactional
	public Story save(Story story) {
		StoryJpaEntity entity = story.storyUuid() == null
				? new StoryJpaEntity()
				: storyRepository.findByStoryUuid(story.storyUuid().value()).orElseGet(StoryJpaEntity::new);
		StoryPersistenceMapper.apply(story, entity);
		Story saved = StoryPersistenceMapper.toDomain(storyRepository.save(entity));
		log.debug("StoryCommandPersistenceAdapter : save : 스토리 저장 완료 - storyUuid={}", saved.storyUuid());
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Story> findByStoryUuid(UUID storyUuid) {
		return storyRepository.findByStoryUuid(storyUuid).map(StoryPersistenceMapper::toDomain);
	}
}
