package com.planwith.planwith_fo_story.application.port.in;

import com.planwith.planwith_fo_story.application.command.ProjectLikeCountCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMemberProfileCommand;
import com.planwith.planwith_fo_story.application.command.ProjectMembershipEntitlementCommand;

public interface StoryProjectionUseCase {

	void projectMemberProfile(ProjectMemberProfileCommand command);

	void projectLikeCreated(ProjectLikeCountCommand command);

	void projectLikeRemoved(ProjectLikeCountCommand command);

	void projectMembershipSubscribed(ProjectMembershipEntitlementCommand command);

	void projectMembershipCanceled(ProjectMembershipEntitlementCommand command);
}
