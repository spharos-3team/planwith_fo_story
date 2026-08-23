package com.planwith.planwith_fo_story.adapter.out.persistence.story;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

public interface SpringDataStoryRepository extends JpaRepository<StoryJpaEntity, Long> {

	Optional<StoryJpaEntity> findByStoryUuid(UUID storyUuid);

	Optional<StoryJpaEntity> findByStoryUuidAndDeletedAtIsNull(UUID storyUuid);

	List<StoryJpaEntity> findByMemberUuidAndDeletedAtIsNullOrderByCreatedAtDesc(UUID memberUuid, Pageable pageable);

	List<StoryJpaEntity> findByMemberUuidInAndDeletedAtIsNullOrderByCreatedAtDesc(List<UUID> memberUuids, Pageable pageable);

	List<StoryJpaEntity> findByMemberUuidInAndDeletedAtIsNullOrderByViewCountDescCreatedAtDesc(
			List<UUID> memberUuids,
			Pageable pageable
	);

	List<StoryJpaEntity> findByMemberUuidInAndDeletedAtIsNullOrderByStoryLikeCountDescCreatedAtDesc(
			List<UUID> memberUuids,
			Pageable pageable
	);

	List<StoryJpaEntity> findByDeletedAtIsNullOrderByCreatedAtDesc(Pageable pageable);

	List<StoryJpaEntity> findByDeletedAtIsNullOrderByViewCountDescCreatedAtDesc(Pageable pageable);

	List<StoryJpaEntity> findByDeletedAtIsNullOrderByStoryLikeCountDescCreatedAtDesc(Pageable pageable);

	@Query("select distinct story from StoryJpaEntity story "
			+ "join story.visitCountries country "
			+ "where story.deletedAt is null "
			+ "and lower(country.countryName) like lower(concat('%', :keyword, '%')) "
			+ "order by story.createdAt desc")
	List<StoryJpaEntity> searchActiveByCountryName(@Param("keyword") String keyword, Pageable pageable);

	@Query("select distinct story from StoryJpaEntity story "
			+ "join story.visitCountries country "
			+ "join country.cities city "
			+ "where story.deletedAt is null "
			+ "and lower(city.cityName) like lower(concat('%', :keyword, '%')) "
			+ "order by story.createdAt desc")
	List<StoryJpaEntity> searchActiveByCityName(@Param("keyword") String keyword, Pageable pageable);

	@Query("select distinct story from StoryJpaEntity story "
			+ "left join story.visitCountries country "
			+ "left join country.cities city "
			+ "where story.memberUuid = :memberUuid "
			+ "and story.deletedAt is null "
			+ "and (:country is null or lower(country.countryName) like lower(concat('%', :country, '%'))) "
			+ "and (:city is null or lower(city.cityName) like lower(concat('%', :city, '%'))) "
			+ "and (:visibilityScope is null or story.visibilityScope = :visibilityScope) "
			+ "and (:travelStartDate is null or story.endDate >= :travelStartDate) "
			+ "and (:travelEndDate is null or story.startDate <= :travelEndDate)")
	List<StoryJpaEntity> findMyStories(
			@Param("memberUuid") UUID memberUuid,
			@Param("country") String country,
			@Param("city") String city,
			@Param("visibilityScope") VisibilityScope visibilityScope,
			@Param("travelStartDate") LocalDate travelStartDate,
			@Param("travelEndDate") LocalDate travelEndDate,
			Pageable pageable
	);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update StoryJpaEntity story set story.viewCount = story.viewCount + 1 "
			+ "where story.storyUuid = :storyUuid and story.deletedAt is null")
	int incrementViewCount(@Param("storyUuid") UUID storyUuid);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update StoryJpaEntity story set story.storyLikeCount = "
			+ "case when story.storyLikeCount + :delta < 0 then 0 else story.storyLikeCount + :delta end "
			+ "where story.storyUuid = :storyUuid and story.deletedAt is null")
	int changeLikeCount(@Param("storyUuid") UUID storyUuid, @Param("delta") long delta);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update StoryJpaEntity story set story.storyCommentCount = "
			+ "case when story.storyCommentCount + :delta < 0 then 0 else story.storyCommentCount + :delta end "
			+ "where story.storyUuid = :storyUuid and story.deletedAt is null")
	int changeCommentCount(@Param("storyUuid") UUID storyUuid, @Param("delta") long delta);
}
