package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.EmailSenderPort;
import com.timcritt.tfg.application.port.outbound.PasswordEncoderPort;
import com.timcritt.tfg.application.port.outbound.PlatformInvitationRepositoryPort;
import com.timcritt.tfg.application.port.outbound.UserRepositoryPort;
import com.timcritt.tfg.application.service.BatchDeleteResult;
import com.timcritt.tfg.application.service.PlatformInvitationService;
import com.timcritt.tfg.infrastructure.security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.domain.model.RoleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class PlatformInvitationAdapter {

    private final PlatformInvitationService delegate;

    public PlatformInvitationAdapter(PasswordEncoderPort passwordEncoder, PlatformInvitationRepositoryPort repository, EmailSenderPort emailSender, UserRepositoryPort userRepository,
                                     @Value("${app.frontend.invitation-url:http://localhost:5173/signup-with-invitation?token={token}}") String invitationUrlTemplate) {
        this.delegate = new PlatformInvitationService(passwordEncoder, repository, emailSender, userRepository, invitationUrlTemplate);
    }

    public List<PlatformInvitation> findPendingByRoleType(RoleType roleType) {
        return delegate.findPendingByRoleType(roleType);
    }

    @Transactional
    public BatchDeleteResult deleteAllByIds(List<Long> ids) {
        return delegate.deleteAllByIds(ids);
    }

    @Transactional
    public void createAndSendPlatformInvitation(String inviteeEmail, RoleType roleType) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user found in security context");
        }
        delegate.createAndSendPlatformInvitation(principal.getId(), inviteeEmail, roleType);
    }

    @Transactional
    public void resendInvitation(Long invitationId) {
        delegate.resendInvitation(invitationId);
    }

    @Transactional
    public void signupWithInvitationToken(String token, String username, String name, String surname, String password) {
        delegate.signUpWIthInvitationToken(token, username, name, surname, password);
    }
}
