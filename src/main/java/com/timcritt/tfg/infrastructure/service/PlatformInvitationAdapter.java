package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.PlatformInvitationService;
import com.timcritt.tfg.domain.model.RoleType;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PlatformInvitationAdapter {

    private final PlatformInvitationService delegate;

    public PlatformInvitationAdapter(PasswordEncoderPort passwordEncoder, PlatformInvitationRepositoryPort repository, EmailSenderPort emailSender, UserRepositoryPort userRepository) {
        this.delegate = new PlatformInvitationService(passwordEncoder, repository, emailSender, userRepository);
    }

    @Transactional
    public void createAndSendPlatformInvitation(Long createdByUserId, String inviteeEmail, RoleType roleType) {
        //createdByUserId can be passed in via the controller
        delegate.createAndSendPlatformInvitation(createdByUserId, inviteeEmail, roleType);
    }

    @Transactional
    public void signupWithInvitationToken(String token, String username, String name, String surname, String password) {
        delegate.signUpWIthInvitationToken(token, username, name, surname, password);
    }
}
