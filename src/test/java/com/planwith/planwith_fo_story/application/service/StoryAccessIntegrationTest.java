package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.MembershipEntitlementProjectionPort;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;
import com.planwith.planwith_fo_story.domain.model.projection.MembershipEntitlementProjection;
import com.planwith.planwith_fo_story.domain.model.vo.MemberUuid;

@SpringBootTest
@ActiveProfiles("test")
class StoryAccessIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private MembershipEntitlementProjectionPort membershipEntitlementProjectionPort;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
	}

	@Test
	void allScopeIsReadableByGuestAndMember() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.ALL, List.of());

		assertThat(detail(created.storyUuid(), null).title()).isEqualTo("공개 스토리");
		assertThat(detail(created.storyUuid(), UUID.randomUUID()).title()).isEqualTo("공개 스토리");
	}

	@Test
	void memberScopeDeniesGuestAndAllowsLoggedInMember() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.MEMBER, List.of());

		assertThatThrownBy(() -> detail(created.storyUuid(), null))
				.isInstanceOf(StoryAccessDeniedException.class);
		assertThat(detail(created.storyUuid(), UUID.randomUUID()).title()).isEqualTo("공개 스토리");
	}

	@Test
	void membershipScopeAllowsOnlyAuthorSubscriber() {
		UUID authorUuid = UUID.randomUUID();
		UUID subscriberUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.MEMBERSHIP, List.of());
		membershipEntitlementProjectionPort.save(new MembershipEntitlementProjection(
				MemberUuid.of(subscriberUuid),
				MemberUuid.of(authorUuid),
				UUID.randomUUID(),
				true,
				1L,
				LocalDateTime.of(2026, 8, 23, 21, 0)
		));

		assertThat(detail(created.storyUuid(), authorUuid).title()).isEqualTo("공개 스토리");
		assertThat(detail(created.storyUuid(), subscriberUuid).title()).isEqualTo("공개 스토리");
		assertThatThrownBy(() -> detail(created.storyUuid(), otherUuid))
				.isInstanceOf(StoryAccessDeniedException.class);
		assertThatThrownBy(() -> detail(created.storyUuid(), null))
				.isInstanceOf(StoryAccessDeniedException.class);
	}

	@Test
	void privateScopeAllowsAuthorAndDesignatedMemberOnly() {
		UUID authorUuid = UUID.randomUUID();
		UUID designatedUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.PRIVATE, List.of(designatedUuid));

		assertThat(detail(created.storyUuid(), authorUuid).visibilityMemberUuids())
				.containsExactly(designatedUuid.toString());
		assertThat(detail(created.storyUuid(), designatedUuid).title()).isEqualTo("공개 스토리");
		assertThatThrownBy(() -> detail(created.storyUuid(), otherUuid))
				.isInstanceOf(StoryAccessDeniedException.class);
		assertThatThrownBy(() -> detail(created.storyUuid(), null))
				.isInstanceOf(StoryAccessDeniedException.class);
	}

	@Test
	void listAndFeedUseTheSameAccessPolicy() {
		UUID authorUuid = UUID.randomUUID();
		UUID designatedUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		create(authorUuid, VisibilityScope.ALL, List.of());
		create(authorUuid, VisibilityScope.MEMBER, List.of());
		create(authorUuid, VisibilityScope.PRIVATE, List.of(designatedUuid));

		assertThat(storyQueryUseCase.getList(new GetStoryListQuery(authorUuid, null, 0, 20)).items())
				.extracting(item -> item.visibilityScope())
				.containsExactly(VisibilityScope.ALL);
		assertThat(storyQueryUseCase.getList(new GetStoryListQuery(authorUuid, otherUuid, 0, 20)).items())
				.extracting(item -> item.visibilityScope())
				.containsExactlyInAnyOrder(VisibilityScope.MEMBER, VisibilityScope.ALL);
		assertThat(storyQueryUseCase.getList(new GetStoryListQuery(authorUuid, designatedUuid, 0, 20)).items())
				.extracting(item -> item.visibilityScope())
				.containsExactlyInAnyOrder(VisibilityScope.PRIVATE, VisibilityScope.MEMBER, VisibilityScope.ALL);

		assertThat(storyQueryUseCase.getFeed(new GetStoryFeedQuery(null, 0, 20)).items())
				.extracting(item -> item.visibilityScope())
				.containsExactly(VisibilityScope.ALL);
		assertThat(storyQueryUseCase.getFeed(new GetStoryFeedQuery(designatedUuid, 0, 20)).items())
				.extracting(item -> item.visibilityScope())
				.containsExactlyInAnyOrder(VisibilityScope.PRIVATE, VisibilityScope.MEMBER, VisibilityScope.ALL);
	}

	@Test
	void deletedStoryIsNotFoundOnDetail() {
		UUID authorUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, VisibilityScope.ALL, List.of());
		storyCommandUseCase.delete(new DeleteStoryCommand(authorUuid, UUID.fromString(created.storyUuid())));

		assertThatThrownBy(() -> detail(created.storyUuid(), authorUuid))
				.isInstanceOf(StoryNotFoundException.class);
	}

	private StoryDetailView detail(String storyUuid, UUID viewerUuid) {
		return storyQueryUseCase.getDetail(new GetStoryDetailQuery(UUID.fromString(storyUuid), viewerUuid));
	}

	private StoryDetailView create(UUID authorUuid, VisibilityScope visibilityScope, List<UUID> visibilityMemberUuids) {
		return storyCommandUseCase.create(new CreateStoryCommand(
				authorUuid,
				null,
				false,
				"공개 스토리",
				"본문입니다.",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 5),
				true,
				visibilityScope,
				false,
				List.of(new CreateStoryCommand.Country(
						"Korea",
						0,
						List.of(new CreateStoryCommand.City("Seoul", 0, List.of()))
				)),
				List.of(),
				visibilityMemberUuids
		));
	}
}
