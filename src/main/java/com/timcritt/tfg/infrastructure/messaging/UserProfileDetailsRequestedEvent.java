package com.timcritt.tfg.infrastructure.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProfileDetailsRequestedEvent(
        String requestId,
        List<Long> userIds
) {
}