package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.infrastructure.service.UserProfileDetailsRequestAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserProfileDetailsRequestedEventListener {

    private final ObjectMapper objectMapper;
    private final UserProfileDetailsRequestAdapter profileDetailsRequestAdapter;

    public UserProfileDetailsRequestedEventListener(
            ObjectMapper objectMapper,
            UserProfileDetailsRequestAdapter profileDetailsRequestAdapter
    ) {
        this.objectMapper = objectMapper;
        this.profileDetailsRequestAdapter = profileDetailsRequestAdapter;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.user-profile-details-requested:user.profile-details-requested.v1}",
            groupId = "${app.kafka.groups.user-profile-details-requested:user-service-profile-details-requested}"
    )
    public void onUserProfileDetailsRequested(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException(
                    "User profile details requested payload is required"
            );
        }

        try {
            UserProfileDetailsRequestedEvent event =
                    objectMapper.readValue(
                            payload,
                            UserProfileDetailsRequestedEvent.class
                    );

            if (event == null
                    || event.userIds() == null
                    || event.userIds().isEmpty()) {
                throw new IllegalArgumentException(
                        "User profile details requested event requires userIds"
                );
            }

            profileDetailsRequestAdapter.publishProfiles(
                    event.userIds()
            );

            log.info(
                    "Processed user profile details request requestId={}, userIds={}",
                    event.requestId(),
                    event.userIds()
            );

        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(
                    "Failed to parse user profile details requested event",
                    ex
            );
        }
    }
}