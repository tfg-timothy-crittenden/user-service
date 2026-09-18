package com.timcritt.tfg.domain.event;

public record UserProfileUpdatedEvent(
        Long userId,
        long version,
        String firstName,
        String lastName
) {}