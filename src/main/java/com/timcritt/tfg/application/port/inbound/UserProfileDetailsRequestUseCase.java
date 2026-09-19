package com.timcritt.tfg.application.port.inbound;

import java.util.List;

public interface UserProfileDetailsRequestUseCase {

    void publishProfiles(List<Long> userIds);
}