package com.planwith.planwith_fo_story.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_story.adapter.in.kafka.StoryInboundEventConsumer;
import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.SpringDataStoryOutboxRepository;
import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.StoryOutboxJpaEntity;
import com.planwith.planwith_fo_story.adapter.out.persistence.outbox.StoryOutboxRelay;
import com.planwith.planwith_fo_story.adapter.out.persistence.story.SpringDataStoryRepository;
import com.planwith.planwith_fo_story.application.command.CreateStoryCommand;
import com.planwith.planwith_fo_story.application.command.DeleteStoryCommand;
import com.planwith.planwith_fo_story.application.command.IncreaseStoryViewCountCommand;
import com.planwith.planwith_fo_story.application.command.UpdateStoryCommand;
import com.planwith.planwith_fo_story.application.event.StoryAiUsageRecordedEvent;
import com.planwith.planwith_fo_story.application.event.StoryDeletedEvent;
import com.planwith.planwith_fo_story.application.port.in.StoryCommandUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryProjectionUseCase;
import com.planwith.planwith_fo_story.application.port.in.StoryQueryUseCase;
import com.planwith.planwith_fo_story.application.port.out.ScheduleOwnershipPort;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationPort;
import com.planwith.planwith_fo_story.application.port.out.StoryAiModerationResult;
import com.planwith.planwith_fo_story.application.port.out.StoryCommandPort;
import com.planwith.planwith_fo_story.application.port.out.StoryEventOutboxPort;
import com.planwith.planwith_fo_story.application.port.out.StoryEventPublisher;
import com.planwith.planwith_fo_story.application.port.out.StoryFeedMemberQueryPort;
import com.planwith.planwith_fo_story.application.port.out.StoryFeedMembershipQueryPort;
import com.planwith.planwith_fo_story.application.port.out.StoryNicknameSearchPort;
import com.planwith.planwith_fo_story.application.port.out.StoryOutboxMessage;
import com.planwith.planwith_fo_story.application.query.GetMyStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetMyStoryListQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryDetailQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryFeedQuery;
import com.planwith.planwith_fo_story.application.query.GetStoryListQuery;
import com.planwith.planwith_fo_story.application.query.SearchStoryQuery;
import com.planwith.planwith_fo_story.application.query.StoryDetailView;
import com.planwith.planwith_fo_story.application.query.StoryFeedType;
import com.planwith.planwith_fo_story.application.query.StoryFeedView;
import com.planwith.planwith_fo_story.application.query.StoryListView;
import com.planwith.planwith_fo_story.application.query.StorySearchType;
import com.planwith.planwith_fo_story.application.query.StorySortType;
import com.planwith.planwith_fo_story.composition.domain.CommentUiPolicy;
import com.planwith.planwith_fo_story.composition.domain.CommentUiState;
import com.planwith.planwith_fo_story.config.StoryKafkaProperties;
import com.planwith.planwith_fo_story.config.StoryOutboxProperties;
import com.planwith.planwith_fo_story.domain.exception.InvalidStoryStateException;
import com.planwith.planwith_fo_story.domain.exception.MemberAuthenticationRequiredException;
import com.planwith.planwith_fo_story.domain.exception.StoryAccessDeniedException;
import com.planwith.planwith_fo_story.domain.exception.StoryNotFoundException;
import com.planwith.planwith_fo_story.domain.model.AiModerationStatus;
import com.planwith.planwith_fo_story.domain.model.VisibilityScope;

@SpringBootTest
@ActiveProfiles("test")
class StoryFullFlowIntegrationTest {

	@Autowired
	private StoryCommandUseCase storyCommandUseCase;

	@Autowired
	private StoryQueryUseCase storyQueryUseCase;

	@Autowired
	private StoryProjectionUseCase storyProjectionUseCase;

	@Autowired
	private StoryCommandPort storyCommandPort;

	@Autowired
	private StoryEventOutboxPort storyEventOutboxPort;

	@Autowired
	private SpringDataStoryRepository storyRepository;

	@Autowired
	private SpringDataStoryOutboxRepository outboxRepository;

	@Autowired
	private StoryKafkaProperties kafkaProperties;

	@Autowired
	private StoryOutboxProperties outboxProperties;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private Clock clock;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@MockitoBean
	private StoryAiModerationPort storyAiModerationPort;

	@MockitoBean
	private ScheduleOwnershipPort scheduleOwnershipPort;

	@MockitoBean
	private StoryFeedMemberQueryPort storyFeedMemberQueryPort;

	@MockitoBean
	private StoryFeedMembershipQueryPort storyFeedMembershipQueryPort;

	@MockitoBean
	private StoryNicknameSearchPort storyNicknameSearchPort;

	@BeforeEach
	void setUp() {
		outboxRepository.deleteAll();
		storyRepository.deleteAll();
		jdbcTemplate.update("delete from story_membership_projection");
		jdbcTemplate.update("delete from story_member_projection");
	}

	@Test
	@DisplayName("01-08, 17, 18 - create, nested validation, detail and schedule visibility")
	void createsNestedStoryValidatesLimitsAndReadsDetail() {
		UUID authorUuid = UUID.randomUUID();
		UUID scheduleUuid = UUID.randomUUID();
		when(scheduleOwnershipPort.isOwnedBy(scheduleUuid, authorUuid)).thenReturn(true);

		assertThatThrownBy(() -> storyCommandUseCase.create(createCommand(
				null, null, false, VisibilityScope.ALL, false, List.of(), countriesWithImages(1)
		))).isInstanceOf(MemberAuthenticationRequiredException.class);

		StoryDetailView created = storyCommandUseCase.create(createCommand(
				authorUuid,
				scheduleUuid,
				false,
				VisibilityScope.ALL,
				false,
				List.of(),
				List.of(
						country("Korea", "Seoul", 5),
						country("Japan", "Tokyo", 1)
				)
		));

		assertThat(created.visitCountries()).hasSize(2);
		assertThat(created.places()).hasSize(2);
		assertThat(created.places().get(0).images()).hasSize(5);
		assertThat(created.scheduleUuid()).isEqualTo(scheduleUuid.toString());
		assertThat(storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()), null
		)).scheduleUuid()).isNull();
		assertThat(storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(created.storyUuid()), authorUuid
		)).scheduleUuid()).isEqualTo(scheduleUuid.toString());

		assertThatThrownBy(() -> storyCommandUseCase.create(createCommand(
				authorUuid, null, false, VisibilityScope.ALL, false, List.of(), countriesWithImages(6)
		))).isInstanceOf(InvalidStoryStateException.class);

		StoryDetailView commentDisabled = storyCommandUseCase.create(createCommand(
				authorUuid, null, false, VisibilityScope.ALL, false, List.of(), countriesWithImages(1), false
		));
		assertThat(CommentUiPolicy.resolve(commentDisabled.commentEnabled(), authorUuid))
				.isEqualTo(CommentUiState.DISABLED);
	}

	@Test
	@DisplayName("09-12 - MEMBER, PRIVATE and MEMBERSHIP access policies")
	void enforcesVisibilityPoliciesWithMembershipProjection() {
		UUID authorUuid = UUID.randomUUID();
		UUID designatedUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();

		StoryDetailView memberStory = create(authorUuid, "member", VisibilityScope.MEMBER, List.of());
		assertThatThrownBy(() -> detail(memberStory, null)).isInstanceOf(StoryAccessDeniedException.class);

		StoryDetailView privateStory = create(
				authorUuid, "private", VisibilityScope.PRIVATE, List.of(designatedUuid)
		);
		assertThat(detail(privateStory, designatedUuid).storyUuid()).isEqualTo(privateStory.storyUuid());
		assertThatThrownBy(() -> detail(privateStory, otherUuid)).isInstanceOf(StoryAccessDeniedException.class);

		StoryDetailView membershipStory = create(
				authorUuid, "membership", VisibilityScope.MEMBERSHIP, List.of()
		);
		assertThatThrownBy(() -> detail(membershipStory, designatedUuid))
				.isInstanceOf(StoryAccessDeniedException.class);

		consume(kafkaProperties.getTopics().getMembershipSubscribed(), """
				{"eventUuid":"%s","memberUuid":"%s","creatorUuid":"%s","membershipUuid":"%s","sourceVersion":1}
				""".formatted(UUID.randomUUID(), designatedUuid, authorUuid, UUID.randomUUID()));
		assertThat(detail(membershipStory, designatedUuid).storyUuid()).isEqualTo(membershipStory.storyUuid());
	}

	@Test
	@DisplayName("13-16 - AI results, token usage relay and requestId deduplication")
	void verifiesAiAndRelaysUsageExactlyOncePerRequestId() {
		when(storyAiModerationPort.moderate(any(), any())).thenReturn(Optional.of(
				new StoryAiModerationResult(true, 12, 3, 15, "omni-moderation-latest")
		));
		StoryDetailView verified = createWithAi(UUID.randomUUID(), "verified");
		assertThat(detail(verified, UUID.fromString(verified.memberUuid())).aiModerationStatus())
				.isEqualTo(AiModerationStatus.VERIFIED);

		when(storyAiModerationPort.moderate(any(), any())).thenReturn(Optional.of(
				new StoryAiModerationResult(false, 9, 1, 10, "omni-moderation-latest")
		));
		StoryDetailView rejected = createWithAi(UUID.randomUUID(), "rejected");
		assertThat(detail(rejected, UUID.fromString(rejected.memberUuid())).aiModerationStatus())
				.isEqualTo(AiModerationStatus.UNVERIFIED);

		List<StoryOutboxJpaEntity> usages = outboxRepository.findAll().stream()
				.filter(outbox -> StoryAiUsageRecordedEvent.EVENT_TYPE.equals(outbox.eventType()))
				.toList();
		assertThat(usages).hasSize(2).allSatisfy(outbox -> {
			assertThat(outbox.payload()).contains("\"requestId\":\"" + outbox.eventUuid() + "\"");
			assertThat(outbox.payload()).contains("\"totalTokens\":");
		});

		StoryOutboxJpaEntity usage = usages.get(0);
		long countBeforeDuplicate = outboxRepository.count();
		storyEventOutboxPort.save(new StoryOutboxMessage(
				usage.eventUuid().toString(),
				usage.aggregateType(),
				usage.aggregateUuid().toString(),
				usage.eventType(),
				usage.payload()
		));
		assertThat(outboxRepository.count()).isEqualTo(countBeforeDuplicate);

		StoryEventPublisher publisher = mock(StoryEventPublisher.class);
		when(publisher.publish(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
		new StoryOutboxRelay(
				outboxRepository, publisher, outboxProperties, kafkaProperties, clock
		).relayUnpublishedEvents();
		verify(publisher).publish(
				kafkaProperties.getTopics().getTokenUsage(),
				usage.aggregateUuid().toString(),
				usage.payload()
		);
	}

	@Test
	@DisplayName("19-30 - Kafka counters, sorting, search, feeds and my stories")
	void synchronizesMsaProjectionsAndQueriesEveryListSurface() {
		UUID viewerUuid = UUID.randomUUID();
		UUID followedAuthor = UUID.randomUUID();
		UUID otherAuthor = UUID.randomUUID();
		StoryDetailView first = create(followedAuthor, "first", VisibilityScope.ALL, List.of());
		StoryDetailView second = create(otherAuthor, "second", VisibilityScope.ALL, List.of());
		UUID firstUuid = UUID.fromString(first.storyUuid());
		UUID secondUuid = UUID.fromString(second.storyUuid());

		consume(kafkaProperties.getTopics().getLikeCreated(), counterPayload(secondUuid, 1));
		consume(kafkaProperties.getTopics().getLikeCreated(), counterPayload(secondUuid, 2));
		consume(kafkaProperties.getTopics().getCommentCreated(), counterPayload(firstUuid, 1));
		storyCommandUseCase.increaseViewCount(new IncreaseStoryViewCountCommand(firstUuid));
		storyCommandUseCase.increaseViewCount(new IncreaseStoryViewCountCommand(firstUuid));

		assertThat(list(StorySortType.LATEST).items().get(0).storyUuid()).isEqualTo(second.storyUuid());
		assertThat(list(StorySortType.VIEW).items().get(0).storyUuid()).isEqualTo(first.storyUuid());
		assertThat(list(StorySortType.LIKE).items().get(0).storyUuid()).isEqualTo(second.storyUuid());
		assertThat(detail(first, followedAuthor).storyCommentCount()).isEqualTo(1L);
		assertThat(detail(second, otherAuthor).storyLikeCount()).isEqualTo(2L);

		assertThat(search(StorySearchType.COUNTRY, "Korea", null).items())
				.extracting(item -> item.storyUuid()).contains(first.storyUuid(), second.storyUuid());
		assertThat(search(StorySearchType.CITY, "Seoul", null).items())
				.extracting(item -> item.storyUuid()).contains(first.storyUuid(), second.storyUuid());
		when(storyNicknameSearchPort.findMemberUuidsByNickname("traveler"))
				.thenReturn(Set.of(followedAuthor));
		assertThat(search(StorySearchType.NICKNAME, "traveler", null).items())
				.extracting(item -> item.storyUuid()).containsExactly(first.storyUuid());

		when(storyFeedMemberQueryPort.findEligibleFollowingAuthors(viewerUuid))
				.thenReturn(Optional.of(Set.of(followedAuthor)));
		assertThat(feed(viewerUuid, StoryFeedType.FOLLOWING).items())
				.extracting(item -> item.storyUuid()).containsExactly(first.storyUuid());
		when(storyFeedMemberQueryPort.findEligibleFollowingAuthors(viewerUuid)).thenReturn(Optional.empty());
		assertThat(feed(viewerUuid, StoryFeedType.FOLLOWING).items())
				.extracting(item -> item.storyUuid()).contains(second.storyUuid(), first.storyUuid());

		StoryDetailView membership = create(
				followedAuthor, "membership-feed", VisibilityScope.MEMBERSHIP, List.of()
		);
		consume(kafkaProperties.getTopics().getMembershipSubscribed(), """
				{"eventUuid":"%s","memberUuid":"%s","creatorUuid":"%s","membershipUuid":"%s","sourceVersion":2}
				""".formatted(UUID.randomUUID(), viewerUuid, followedAuthor, UUID.randomUUID()));
		when(storyFeedMembershipQueryPort.findJoinedCreatorUuids(viewerUuid)).thenReturn(Set.of(followedAuthor));
		when(storyFeedMemberQueryPort.filterEligibleAuthors(Set.of(followedAuthor)))
				.thenReturn(Optional.of(Set.of(followedAuthor)));
		assertThat(feed(viewerUuid, StoryFeedType.MEMBERSHIP).items())
				.extracting(item -> item.storyUuid()).containsExactly(membership.storyUuid());

		assertThat(storyQueryUseCase.getMyStories(new GetMyStoryListQuery(
				followedAuthor, null, null, null, null, null, 0, 20
		)).items()).extracting(item -> item.storyUuid())
				.contains(first.storyUuid(), membership.storyUuid());
		assertThat(storyQueryUseCase.getMyStoryDetail(new GetMyStoryDetailQuery(
				followedAuthor, firstUuid
		)).storyUuid()).isEqualTo(first.storyUuid());
	}

	@Test
	@DisplayName("31-35 - ownership, AI reset, soft delete, hiding and deleted event")
	void updatesThenSoftDeletesAndHidesStoryFromEveryQuery() {
		UUID authorUuid = UUID.randomUUID();
		UUID otherUuid = UUID.randomUUID();
		StoryDetailView created = create(authorUuid, "lifecycle", VisibilityScope.ALL, List.of());
		UUID storyUuid = UUID.fromString(created.storyUuid());
		storyCommandPort.updateAiModerationStatus(
				storyUuid, AiModerationStatus.VERIFIED, LocalDateTime.now(clock)
		).orElseThrow();

		assertThatThrownBy(() -> storyCommandUseCase.update(update(otherUuid, created, "changed")))
				.isInstanceOf(StoryAccessDeniedException.class);
		StoryDetailView updated = storyCommandUseCase.update(update(authorUuid, created, "changed"));
		assertThat(updated.aiModerationStatus()).isEqualTo(AiModerationStatus.UNVERIFIED);

		storyCommandUseCase.delete(new DeleteStoryCommand(authorUuid, storyUuid));

		assertThat(storyCommandPort.findByStoryUuid(storyUuid)).get().satisfies(story ->
				assertThat(story.isDeleted()).isTrue());
		assertThatThrownBy(() -> detail(updated, authorUuid)).isInstanceOf(StoryNotFoundException.class);
		assertThat(list(StorySortType.LATEST).items())
				.extracting(item -> item.storyUuid()).doesNotContain(updated.storyUuid());
		assertThat(search(StorySearchType.COUNTRY, "Korea", null).items())
				.extracting(item -> item.storyUuid()).doesNotContain(updated.storyUuid());
		StoryOutboxJpaEntity deletedEvent = outboxRepository.findAll().stream()
				.filter(outbox -> StoryDeletedEvent.EVENT_TYPE.equals(outbox.eventType()))
				.findFirst()
				.orElseThrow();
		assertThat(deletedEvent.payload()).contains(storyUuid.toString());

		StoryEventPublisher publisher = mock(StoryEventPublisher.class);
		when(publisher.publish(any(), any(), any())).thenReturn(CompletableFuture.completedFuture(null));
		new StoryOutboxRelay(
				outboxRepository, publisher, outboxProperties, kafkaProperties, clock
		).relayUnpublishedEvents();
		verify(publisher).publish(
				kafkaProperties.getTopics().getStoryDeleted(),
				storyUuid.toString(),
				deletedEvent.payload()
		);
	}

	private StoryDetailView create(
			UUID authorUuid,
			String title,
			VisibilityScope visibilityScope,
			List<UUID> visibilityMembers
	) {
		return storyCommandUseCase.create(createCommand(
				authorUuid, null, false, visibilityScope, false, visibilityMembers, countriesWithImages(1), title, true
		));
	}

	private StoryDetailView createWithAi(UUID authorUuid, String title) {
		return storyCommandUseCase.create(createCommand(
				authorUuid, null, false, VisibilityScope.ALL, true, List.of(), countriesWithImages(1), title, true
		));
	}

	private StoryDetailView detail(StoryDetailView story, UUID viewerUuid) {
		return storyQueryUseCase.getDetail(new GetStoryDetailQuery(
				UUID.fromString(story.storyUuid()), viewerUuid
		));
	}

	private StoryListView list(StorySortType sort) {
		return storyQueryUseCase.getList(new GetStoryListQuery(null, null, 0, 20, sort));
	}

	private StoryListView search(
			StorySearchType type,
			String keyword,
			UUID viewerUuid
	) {
		return storyQueryUseCase.search(new SearchStoryQuery(type, keyword, viewerUuid, 0, 20));
	}

	private StoryFeedView feed(
			UUID viewerUuid,
			StoryFeedType feedType
	) {
		return storyQueryUseCase.getFeed(new GetStoryFeedQuery(
				viewerUuid, 0, 20, StorySortType.LATEST, feedType
		));
	}

	private void consume(String topic, String payload) {
		new StoryInboundEventConsumer(storyProjectionUseCase, objectMapper, kafkaProperties).consume(topic, payload);
	}

	private static String counterPayload(UUID storyUuid, long sourceVersion) {
		return """
				{"eventUuid":"%s","targetType":"STORY","targetUuid":"%s","sourceVersion":%d}
				""".formatted(UUID.randomUUID(), storyUuid, sourceVersion);
	}

	private static UpdateStoryCommand update(UUID actorUuid, StoryDetailView story, String content) {
		return new UpdateStoryCommand(
				actorUuid,
				UUID.fromString(story.storyUuid()),
				null,
				false,
				story.title(),
				content,
				story.coverImageUrl(),
				story.startDate(),
				story.endDate(),
				story.commentEnabled(),
				story.visibilityScope(),
				false,
				countriesWithImages(1),
				story.tags(),
				List.of()
		);
	}

	private static CreateStoryCommand createCommand(
			UUID memberUuid,
			UUID scheduleUuid,
			boolean scheduleVisible,
			VisibilityScope visibilityScope,
			boolean aiVerificationRequested,
			List<UUID> visibilityMembers,
			List<CreateStoryCommand.Country> countries
	) {
		return createCommand(
				memberUuid, scheduleUuid, scheduleVisible, visibilityScope, aiVerificationRequested,
				visibilityMembers, countries, "full-flow", true
		);
	}

	private static CreateStoryCommand createCommand(
			UUID memberUuid,
			UUID scheduleUuid,
			boolean scheduleVisible,
			VisibilityScope visibilityScope,
			boolean aiVerificationRequested,
			List<UUID> visibilityMembers,
			List<CreateStoryCommand.Country> countries,
			boolean commentEnabled
	) {
		return createCommand(
				memberUuid, scheduleUuid, scheduleVisible, visibilityScope, aiVerificationRequested,
				visibilityMembers, countries, "full-flow", commentEnabled
		);
	}

	private static CreateStoryCommand createCommand(
			UUID memberUuid,
			UUID scheduleUuid,
			boolean scheduleVisible,
			VisibilityScope visibilityScope,
			boolean aiVerificationRequested,
			List<UUID> visibilityMembers,
			List<CreateStoryCommand.Country> countries,
			String title,
			boolean commentEnabled
	) {
		return new CreateStoryCommand(
				memberUuid,
				scheduleUuid,
				scheduleVisible,
				title,
				"integration content",
				"https://img.example/cover.png",
				LocalDate.of(2026, 8, 1),
				LocalDate.of(2026, 8, 10),
				commentEnabled,
				visibilityScope,
				aiVerificationRequested,
				countries,
				List.of("integration"),
				visibilityMembers
		);
	}

	private static List<CreateStoryCommand.Country> countriesWithImages(int imageCount) {
		return List.of(country("Korea", "Seoul", imageCount));
	}

	private static CreateStoryCommand.Country country(String countryName, String cityName, int imageCount) {
		List<CreateStoryCommand.PlaceImage> images = java.util.stream.IntStream.rangeClosed(1, imageCount)
				.mapToObj(order -> new CreateStoryCommand.PlaceImage(
						"https://img.example/place-" + order + ".png", order
				))
				.toList();
		return new CreateStoryCommand.Country(
				countryName,
				0,
				List.of(new CreateStoryCommand.City(
						cityName,
						0,
						List.of(new CreateStoryCommand.Place(cityName + " place", 0, images))
				))
		);
	}
}
