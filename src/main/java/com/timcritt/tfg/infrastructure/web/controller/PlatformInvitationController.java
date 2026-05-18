package com.timcritt.tfg.infrastructure.web.controller;


import com.timcritt.tfg.application.service.BatchDeleteResult;
import com.timcritt.tfg.domain.model.PlatformInvitation;
import com.timcritt.tfg.domain.model.RoleType;
import com.timcritt.tfg.infrastructure.service.PlatformInvitationAdapter;
import com.timcritt.tfg.infrastructure.web.PlatformInvitationDtoMapper;
import com.timcritt.tfg.infrastructure.web.dto.PlatformInvitationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform-invitations")
public class PlatformInvitationController {

    private final PlatformInvitationAdapter platformInvitationAdapter;


    public PlatformInvitationController(PlatformInvitationAdapter platformInvitationAdapter) {
        this.platformInvitationAdapter = platformInvitationAdapter;
    }

    @GetMapping("/pending-teachers")
    public ResponseEntity<List<PlatformInvitationDto>> getPendingTeacherInvitations() {
        List<PlatformInvitation> pendingTeacherInvitations = platformInvitationAdapter.findPendingByRoleType(RoleType.TEACHER);
        List<PlatformInvitationDto> platformInvitationDtos = pendingTeacherInvitations.stream().map(PlatformInvitationDtoMapper::toDto).toList();
        return ResponseEntity.ok(platformInvitationDtos);
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<Void> resendInvitation(@PathVariable Long id) {
        platformInvitationAdapter.resendInvitation(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<BatchDeleteResponse> deleteInvitations(@Valid @RequestBody DeleteInvitationsRequest request) {
        BatchDeleteResult result = platformInvitationAdapter.deleteAllByIds(request.ids());
        BatchDeleteResponse response = new BatchDeleteResponse(result.deleted(), result.notFound());
        // 207 Multi-Status when some were not found, 200 when all deleted
        int status = result.notFound().isEmpty() ? 200 : 207;
        return ResponseEntity.status(status).body(response);
    }

    public record DeleteInvitationsRequest(@NotEmpty List<Long> ids) {}
    public record BatchDeleteResponse(List<Long> deleted, List<Long> notFound) {}

}


