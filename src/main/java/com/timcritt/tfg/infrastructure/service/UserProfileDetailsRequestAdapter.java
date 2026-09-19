package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.UserProfileDetailsRequestUseCase;
import com.timcritt.tfg.application.port.outbound.UserEventPublisherPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.UserProfileDetailsRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserProfileDetailsRequestAdapter {

    private final UserProfileDetailsRequestUseCase delegate;

    public UserProfileDetailsRequestAdapter(
            UserRepositoryPort userRepository,
            UserEventPublisherPort userEventPublisher
    ) {
        this.delegate = new UserProfileDetailsRequestService(
                userRepository,
                userEventPublisher
        );
    }

    @Transactional
    public void publishProfiles(List<Long> userIds) {
        delegate.publishProfiles(userIds);
    }
}