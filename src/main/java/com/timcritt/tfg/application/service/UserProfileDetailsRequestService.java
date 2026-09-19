package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.inbound.UserProfileDetailsRequestUseCase;
import com.timcritt.tfg.application.port.outbound.UserEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.domain.event.UserProfileUpdatedEvent;

import java.util.List;

public class UserProfileDetailsRequestService implements UserProfileDetailsRequestUseCase {

    private final UserRepositoryPort userRepository;
    private final UserEventPublisherPort userEventPublisher;

    public UserProfileDetailsRequestService(
            UserRepositoryPort userRepository,
            UserEventPublisherPort userEventPublisher
    ) {
        this.userRepository = userRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    public void publishProfiles(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        userIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .map(userRepository::findById)
                .flatMap(java.util.Optional::stream)
                .forEach(user ->
                        userEventPublisher.publishUserProfileUpdated(
                                new UserProfileUpdatedEvent(
                                        user.getId(),
                                        user.getVersion(),
                                        user.getName(),
                                        user.getSurname()
                                )
                        )
                );
    }
}