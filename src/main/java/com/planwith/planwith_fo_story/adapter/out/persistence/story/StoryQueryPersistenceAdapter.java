package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryQueryPort;
import com.planwith.planwith_fo_story.domain.model.Story;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryQueryPersistenceAdapter implements StoryQueryPort {

	private final SpringDataStoryRepository storyRepository;

	@Override
	public Optional<Story> findActiveByStoryUuid(UUID storyUuid) {
		return storyRepository.findByStoryUuidAndDeletedAtIsNull(storyUuid)
				.map(StoryPersistenceMapper::toDomain);
	}

	@Override
	public List<Story> findActiveByMemberUuid(UUID memberUuid, int offset, int size) {
		return storyRepository
				.findByMemberUuidAndDeletedAtIsNullOrderByCreatedAtDesc(memberUuid, page(offset, size))
				.stream()
				.map(StoryPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<Story> findRecentActive(int offset, int size) {
		return storyRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(page(offset, size))
				.stream()
				.map(StoryPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<Story> findPopularActive(int size) {
		return storyRepository.findByDeletedAtIsNullOrderByStoryLikeCountDescCreatedAtDesc(page(0, size))
				.stream()
				.map(StoryPersistenceMapper::toDomain)
				.toList();
	}

	private static PageRequest page(int offset, int size) {
		int resolvedSize = Math.max(1, size);
		int page = Math.max(0, offset) / resolvedSize;
		return PageRequest.of(page, resolvedSize);
	}
}
