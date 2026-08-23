package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.planwith.planwith_fo_story.application.port.out.StoryQueryPort;
import com.planwith.planwith_fo_story.application.query.StorySortType;
import com.planwith.planwith_fo_story.application.query.StorySearchType;
import com.planwith.planwith_fo_story.domain.model.Story;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryQueryPersistenceAdapter implements StoryQueryPort {

	private final SpringDataStoryRepository storyRepository;

	@Override
	public Optional<Story> findByStoryUuid(UUID storyUuid) {
		return storyRepository.findByStoryUuid(storyUuid)
				.map(StoryPersistenceMapper::toDomain);
	}

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

	@Override
	public List<Story> findActive(Set<UUID> authorUuids, StorySortType sort, int offset, int size) {
		PageRequest page = page(offset, size);
		List<StoryJpaEntity> entities = authorUuids == null
				? findAllActive(sort, page)
				: findByAuthors(authorUuids, sort, page);
		return entities.stream().map(StoryPersistenceMapper::toDomain).toList();
	}

	@Override
	public List<Story> searchActive(StorySearchType type, String keyword, int offset, int size) {
		PageRequest page = page(offset, size);
		List<StoryJpaEntity> entities = switch (type) {
			case COUNTRY -> storyRepository.searchActiveByCountryName(keyword, page);
			case CITY -> storyRepository.searchActiveByCityName(keyword, page);
			case NICKNAME -> throw new IllegalArgumentException("닉네임 검색은 회원 UUID 조건으로 조회해야 합니다.");
		};
		return entities.stream().map(StoryPersistenceMapper::toDomain).toList();
	}

	private List<StoryJpaEntity> findAllActive(StorySortType sort, PageRequest page) {
		return switch (sort) {
			case LATEST -> storyRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(page);
			case VIEW -> storyRepository.findByDeletedAtIsNullOrderByViewCountDescCreatedAtDesc(page);
			case LIKE -> storyRepository.findByDeletedAtIsNullOrderByStoryLikeCountDescCreatedAtDesc(page);
		};
	}

	private List<StoryJpaEntity> findByAuthors(Set<UUID> authorUuids, StorySortType sort, PageRequest page) {
		if (authorUuids.isEmpty()) {
			return List.of();
		}
		List<UUID> authors = List.copyOf(authorUuids);
		return switch (sort) {
			case LATEST -> storyRepository.findByMemberUuidInAndDeletedAtIsNullOrderByCreatedAtDesc(authors, page);
			case VIEW -> storyRepository.findByMemberUuidInAndDeletedAtIsNullOrderByViewCountDescCreatedAtDesc(authors, page);
			case LIKE -> storyRepository.findByMemberUuidInAndDeletedAtIsNullOrderByStoryLikeCountDescCreatedAtDesc(authors, page);
		};
	}

	private static PageRequest page(int offset, int size) {
		int resolvedSize = Math.max(1, size);
		int page = Math.max(0, offset) / resolvedSize;
		return PageRequest.of(page, resolvedSize);
	}
}
